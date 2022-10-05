#include "receiver.h"
#include "sender.h"
#include <stdio.h>
#include <pthread.h>

int main(int argc, char* argv[]) {
    if (argc != 3) {
        printf("Invalid number of arguments\n Need 2: multicast group IP and port\n");
        return 1;
    }

    pthread_t rec_tid, sen_tid;
    int status;
    if ((status = pthread_create(&rec_tid, NULL, receiver_work, argv)) != 0) {
        perror("receiver thread create\n");
        return 1;
    }

    if ((status = pthread_create(&sen_tid, NULL, sender_work, argv)) != 0) {
        perror("sender thread create\n");
        return 1;
    }

    if ((status = pthread_join(rec_tid, NULL)) != 0) {
        perror("receiver join\n");
        return 1;
    }

    if ((status = pthread_join(sen_tid, NULL)) != 0) {
        perror("sender join\n");
        return 1;
    }

    return 0;
}