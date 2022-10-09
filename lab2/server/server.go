package main

import (
	"bufio"
	"bytes"
	"crypto/md5"
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
	checkSum string
}

type Speed struct {
	current int
	max     int
	average int
}

func emptyFileInfo() FileInfo {
	return FileInfo{"", 0, ""}
}

func getCheckSum(filePath string) string {
	file, err := os.Open(filePath)
	if err != nil {
		panic(err)
	}
	defer file.Close()

	hash := md5.New()
	_, err = io.Copy(hash, file)
	if err != nil {
		panic(err)
	}

	return string(hash.Sum(nil))
}

func getFileInfo(bufReader *bufio.Reader) FileInfo {
	fileName, err := bufReader.ReadString('\n')
	if err != nil {
		fmt.Println(err)
		return emptyFileInfo()
	}

	fileSize, err := bufReader.ReadString('\n')
	if err != nil {
		fmt.Println(err)
		return emptyFileInfo()
	}

	checkSum, err := bufReader.ReadString('\n')
	if err != nil {
		fmt.Println(err)
		return emptyFileInfo()
	}

	fileName = strings.Trim(fileName, "\n")
	fileSize = strings.Trim(fileSize, "\n")
	checkSum = strings.Trim(checkSum, "\n")

	fileSizeInt, _ := strconv.Atoi(fileSize)

	return FileInfo{fileName: fileName, fileSize: fileSizeInt, checkSum: checkSum}
}

func copyToFile(newFile *os.File, c byte) {
	wrapper := []byte{c}
	_, err := io.Copy(newFile, bytes.NewReader(wrapper))
	if err != nil {
		fmt.Println(err)
		return
	}
}

func calculateSpeed(ticker *time2.Ticker, speed *Speed, readBytes *int) {
	select {
	case <-ticker.C:
		speed.current = *readBytes / 3
		if speed.current > speed.max {
			speed.max = speed.current
		}
		fmt.Println("speed: ", speed.current, "bytes per second")
		*readBytes = 0
	default:
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
	readBytes := 0
	total := 0
	speed := Speed{0, 0, 0}
	ticker := time2.NewTicker(3 * time2.Second)

	for {
		c, err := bufReader.ReadByte()
		if err != nil {
			break
		}

		readBytes++
		total++
		if total > fileInfo.fileSize {
			break
		}

		copyToFile(newFile, c)
		calculateSpeed(ticker, &speed, &readBytes)
	}

	end := time2.Now()
	speed.average = 1000 * total / int(end.Sub(start).Milliseconds())

	fmt.Println("____________________________")
	fmt.Println("File was received")
	fmt.Println("Max speed: ", speed.max)
	fmt.Println("Average speed: ", speed.average)

	success := fileInfo.checkSum == getCheckSum("./uploads/"+fileInfo.fileName)
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
