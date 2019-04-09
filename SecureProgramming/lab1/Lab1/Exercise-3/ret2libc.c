#include <stdlib.h>
#include <stdio.h>
#include <string.h>

size_t overflow(FILE *file)
{
	char buffer[12];
	return fread(buffer, sizeof(char), 40, file);
}

int main(int argc, char *argv[])
{
	if (argc != 2)
	{
		printf("Place your input file as the first argument");
		exit(EXIT_FAILURE);
	}

	FILE *file;
	file = fopen(argv[1], "r");
	
	overflow(file);

	printf("To win you should exec /bin/sh");
	printf("\n");

	return EXIT_SUCCESS;
}
	

	
