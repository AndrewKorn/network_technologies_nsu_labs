#ifndef LAB1_COPYLIST_H
#define LAB1_COPYLIST_H

#include <time.h>

typedef struct Copy {
    char* ip;
    time_t update_time;
} Copy;

typedef struct CopyList {
    Copy value;
    struct CopyList* next;
} CopyList;

void print_list(CopyList* copyList);
void free_list(CopyList* copyList);
CopyList* update_list(CopyList* copyList, Copy copy);

#endif //LAB1_COPYLIST_H
