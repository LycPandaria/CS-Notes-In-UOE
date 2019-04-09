#include <stdio.h>

int main(void) {
	printf("I am running with user id: %d\n", getuid());
	printf("I am running with effective user id: %d\n", geteuid());
	
	return 0;
}
