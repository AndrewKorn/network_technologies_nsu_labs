#include "copyList.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int timeout = 3;

void print_list(CopyList* copyList) {
    printf("Current connections:\n");
    while (copyList != NULL) {
        printf("%s\n", copyList->value.ip);
        copyList = copyList->next;
    }
    printf("_______________________\n");
}

CopyList* update_list(CopyList* copyList, Copy copy) {
    if (copyList == NULL) {
        CopyList* node = malloc(sizeof(CopyList));
        node->value = copy;
        node->next = NULL;
        print_list(node);
        return node;
    }

    int twins = 0;
    CopyList* tmp = copyList;
    CopyList* prev;
    while (tmp != NULL) {
        if (strcmp(tmp->value.ip, copy.ip) == 0) {
            tmp->value.update_time = time(NULL);
            twins++;
        }

        if (difftime(time(NULL), tmp->value.update_time) > timeout) {
            prev->next = tmp->next;
            free(tmp);
            print_list(copyList);
            return copyList;
        }

        prev = tmp;
        tmp = tmp->next;
    }

    if (twins == 0) {
        CopyList* node = malloc(sizeof(CopyList));
        node->value = copy;
        node->next = NULL;
        prev->next = node;
        print_list(copyList);
        return copyList;
    }

    return copyList;
}



void free_list(CopyList* copyList) {
    CopyList* tmp;

    while (copyList != NULL) {
        tmp = copyList;
        copyList = copyList->next;
        free(tmp);
    }
}