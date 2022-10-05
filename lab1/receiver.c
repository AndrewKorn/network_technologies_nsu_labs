#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <arpa/inet.h>
#include <time.h>
#include <pthread.h>
#include "copyList.h"
#include "receiver.h"

void* receiver_work(void* arg) {
    char** argv = (char**)arg;

    char* multicast_group = argv[1];
    int port = atoi(argv[2]);

    int sfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sfd < 0) {
        perror("receiver socket\n");
        pthread_exit(NULL);
    }

    int optval = 1;
    if (setsockopt(sfd, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof(optval)) < 0) {
        perror("receiver setsockopt\n");
        pthread_exit(NULL);
    }

    struct sockaddr_in addr;
    bzero(&addr, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_port = htons(port);
    addr.sin_addr.s_addr = htonl(INADDR_ANY);

    if (bind(sfd, (struct sockaddr*) &addr, sizeof(addr) )< 0) {
        perror("receiver bind\n");
        pthread_exit(NULL);
    }

    struct ip_mreq mreq;
    mreq.imr_multiaddr.s_addr = inet_addr(multicast_group);
    mreq.imr_interface.s_addr = htonl(INADDR_ANY);

    if (setsockopt(sfd, IPPROTO_IP, IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq)) < 0) {
        perror("receiver optval\n");
        pthread_exit(NULL);
    }


    CopyList* copyList = NULL;

    while (1) {
        char msgbuf[BUFSIZ];
        int addrlen = sizeof(addr);
        int nbytes = recvfrom(sfd,msgbuf,BUFSIZ,0,(struct sockaddr *) &addr,&addrlen);
        if (nbytes < 0) {
            perror("recvfrom");
            free_list(copyList);
            pthread_exit(NULL);
        }

        Copy copy;
        copy.ip = malloc(sizeof (char) * 20);
        inet_ntop(AF_INET, &addr.sin_addr, copy.ip, 20);
        copy.update_time = time(NULL);

        copyList = update_list(copyList, copy);
    }
}