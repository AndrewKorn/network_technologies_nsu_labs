package main

import (
	"crypto/md5"
	"fmt"
	"io"
	"net"
	"os"
	"strconv"
)

type FileInfo struct {
	fileName string
	fileSize int
	checkSum string
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

func getFileInfo(filePath string) FileInfo {
	fileStat, err := os.Stat(filePath)
	if err != nil {
		fmt.Println(err)
		return emptyFileInfo()
	}

	fileName := fileStat.Name()
	fileSize := int(fileStat.Size())
	checkSum := getCheckSum(filePath)
	return FileInfo{fileName: fileName, fileSize: fileSize, checkSum: checkSum}
}

func sendFileInfo(connection net.Conn, fileInfo FileInfo) {
	_, err := connection.Write([]byte(fileInfo.fileName + "\n"))
	if err != nil {
		fmt.Println(err)
		return
	}

	_, err = connection.Write([]byte(strconv.Itoa(fileInfo.fileSize) + "\n"))
	if err != nil {
		fmt.Println(err)
		return
	}

	_, err = connection.Write([]byte(fileInfo.checkSum + "\n"))
	if err != nil {
		fmt.Println(err)
		return
	}
}

func sendFile(connection net.Conn, filePath string) {
	file, _ := os.Open(filePath)

	sendBuf := make([]byte, 1024)
	for {
		_, err := file.Read(sendBuf)
		if err == io.EOF {
			file.Close()
			break
		}
		connection.Write(sendBuf)
	}
}

func getSuccessStatus(connection net.Conn) {
	endStatus := make([]byte, 1)
	_, err := connection.Read(endStatus)
	if err != nil {
		fmt.Println(err)
		return
	}

	if endStatus[0] == 1 {
		fmt.Println("File was successfully sent")
	} else {
		fmt.Println("File was sent with troubles")
	}
}

func main() {
	args := os.Args
	if len(args) != 4 {
		fmt.Println("Invalid number of arguments. Needs file path, server ip and server port")
		return
	}

	filePath := args[1]
	serverIp := args[2]
	serverPort := args[3]

	connection, err := net.Dial("tcp", serverIp+":"+serverPort)
	if err != nil {
		fmt.Println(err)
		return
	}

	fmt.Println("Connection established, start transferring file")

	fileInfo := getFileInfo(filePath)
	sendFileInfo(connection, fileInfo)
	sendFile(connection, filePath)
	getSuccessStatus(connection)

	connection.Close()
}
