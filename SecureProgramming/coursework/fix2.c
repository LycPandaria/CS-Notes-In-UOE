#include <stdio.h>
#include <string.h>

#define SIZE 16
#define DIS_SIZE 34
char password[SIZE], username[SIZE];

int main(int argc, char **argv) {
	char display[DIS_SIZE];
	
	if(argc != 3){
		fprintf(stderr, "bad arguments\n");
		return -1;
	}
	
	strncpy(username, argv[1], SIZE-1);
	username[SIZE-1] = '\0';
	strncpy(password, argv[2], SIZE-1);
	username[SIZE-1] = '\0';
	
	display[0] = 0;
	strncat(display, username, SIZE-1);
	strncat(display, ":", 1);
	strncat(display, password, DIS_SIZE-strlen(display)-1);
	
	printf(display);
	
	return 0;
}