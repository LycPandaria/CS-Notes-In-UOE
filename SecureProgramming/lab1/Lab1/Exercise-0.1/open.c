#include<stdio.h>

int main(void) {
        char buffer[25];
        FILE *file = fopen("secret","r");

        if (file == NULL) {
                perror("Cannot open file");
        } else {
                fgets(buffer, sizeof(buffer), file);
                printf("%s\n", buffer);
                fclose(file);
        }

	return 0;
}
