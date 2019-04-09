#include <stdio.h>
#include <stdlib.h>

#define PASS "123456789"

int main(int argc, char *argv[]) { 
        char password[10];
        int correct = 0;

        if (argc != 2) {
                printf("Usage: ./authentication <password>\n");
                exit(EXIT_FAILURE);
       	}

        strcpy(password, argv[1]);

        if (!strcmp(password, PASS)) {
                correct	= 1;
        }

        if (correct) {
                printf("Correct	Password\n");
                printf("Running some privileged	command\n");
        } else {
                printf("Wrong Password\n");
        }

        return EXIT_SUCCESS;
}
