package main

import (
	"bufio"
	"bytes"
	"fmt"
	"io"
	"log"
	"net"
	"os"
	"strconv"
	"strings"
	time2 "time"
)

type FileInfo struct {
	fileName string
	fileSize int
}

type Speed struct {
	current int
	max     int
	average int
}

func getFileInfo(bufReader *bufio.Reader) FileInfo {
	fileName, err := bufReader.ReadString('\n')
	if err != nil {
		fmt.Println(err)
		return FileInfo{"", 0}
	}

	fileSize, err := bufReader.ReadString('\n')
	if err != nil {
		fmt.Println(err)
		return FileInfo{"", 0}
	}

	fileName = strings.Trim(fileName, "\n")
	fileSize = strings.Trim(fileSize, "\n")

	fileSizeInt, _ := strconv.Atoi(fileSize)

	return FileInfo{fileName: fileName, fileSize: fileSizeInt}
}

func copyToFile(newFile *os.File, c byte) {
	wrapper := []byte{c}
	_, err := io.Copy(newFile, bytes.NewReader(wrapper))
	if err != nil {
		fmt.Println(err)
		return
	}
}

func calculateSpeed(begin *time2.Time, speed Speed, readBytes *int) {
	diffTime := time2.Now().Sub(*begin)
	if diffTime > 3*time2.Second {
		speed.current = 1000 * *readBytes / int(diffTime.Milliseconds())
		if speed.current > speed.max {
			speed.max = speed.current
		}
		fmt.Println("speed: ", speed.current, "bytes per second")
		*begin = time2.Now()
		*readBytes = 0
	}
}

func sendSuccessStatus(connection net.Conn, success bool) {
	if success {
		_, err := connection.Write([]byte{1})
		if err != nil {
			fmt.Println(err)
			return
		}
	} else {
		_, err := connection.Write([]byte{0})
		if err != nil {
			fmt.Println(err)
			return
		}
	}
}

func gettingFileRoutine(connection net.Conn) {
	defer connection.Close()

	bufReader := bufio.NewReader(connection)

	fileInfo := getFileInfo(bufReader)

	fmt.Println("file name: ", fileInfo.fileName)
	fmt.Println("file size: ", fileInfo.fileSize)
	newFile, err := os.Create("./uploads/" + fileInfo.fileName)
	if err != nil {
		fmt.Println(err)
		return
	}

	fmt.Println("Start getting file")
	fmt.Println("____________________________")

	start := time2.Now()
	begin := time2.Now()
	readBytes := 0
	total := 0
	speed := Speed{0, 0, 0}

	for {
		c, err := bufReader.ReadByte()
		if err != nil {
			break
		}

		readBytes++
		total++
		if total >= fileInfo.fileSize {
			break
		}

		copyToFile(newFile, c)
		calculateSpeed(&begin, speed, &readBytes)
	}

	end := time2.Now()

	if total == readBytes {
		speed.current = 1000 * readBytes / int(end.Sub(begin).Milliseconds())
		if speed.current > speed.max {
			speed.max = speed.current
		}
		fmt.Println("speed: ", speed.current, " bytes per second")
	}

	speed.average = 1000 * total / int(end.Sub(start).Milliseconds())

	fmt.Println("____________________________")
	fmt.Println("File was received")
	fmt.Println("Max speed: ", speed.max)
	fmt.Println("Average speed: ", speed.average)

	newFileStat, err := newFile.Stat()
	if err != nil {
		fmt.Println(err)
		return
	}

	newFileSize := int(newFileStat.Size())
	success := fileInfo.fileSize == newFileSize
	sendSuccessStatus(connection, success)
}

func main() {
	args := os.Args
	if len(args) != 2 {
		fmt.Println("Invalid number of arguments. Needs port number")
		return
	}

	PORT := ":" + args[1]
	listener, err := net.Listen("tcp", PORT)
	if err != nil {
		log.Fatal(err)
		return
	}

	for {
		connection, err := listener.Accept()
		if err != nil {
			connection.Close()
			log.Fatal(err)
		}

		go gettingFileRoutine(connection)
	}
}
