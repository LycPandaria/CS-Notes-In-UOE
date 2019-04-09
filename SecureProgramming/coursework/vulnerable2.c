#include <stdio.h>
#include <string.h>

char password[64], username[64];

int main(int argc, char **argv) {
	char display[132];
	
	if(argc != 3){
		fprintf(stderr, "bad arguments\n");
		return -1;
	}
	
	strncpy(username, argv[1], sizeof(username));
	strncpy(password, argv[2], sizeof(password));
	
	display[0] = 0;
	strcat(display, username);
	strcat(display, ":");
	strcat(display, password);
	
	printf(display);
	
	return 0;
}