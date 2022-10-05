#include "sender.h"
#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <pthread.h>

void* sender_work(void* arg) {
    char** argv = (char**) arg;

    char* multicast_group = argv[1];
    int port = atoi(argv[2]);

    int sfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sfd < 0) {
        perror("sender socket\n");
        pthread_exit(NULL);
    }

    struct sockaddr_in addr;
    bzero(&addr, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_port = htons(port);
    addr.sin_addr.s_addr = inet_addr(multicast_group);

    char* message = "Hello world\n";
    while (1) {
        int nbytes = sendto(sfd,message,strlen(message),0,(struct sockaddr*) &addr,sizeof(addr));
        if (nbytes < 0) {
            perror("sendto");
            pthread_exit(NULL);
        }
        sleep(5);
    }
}