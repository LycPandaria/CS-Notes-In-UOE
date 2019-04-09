#include <stdio.h>
#include <stdlib.h>

const char NOTICEBOARD[] = "/tmp/noticeboard.txt";

int main(int argc, char *argv[])
{
	char buffer[140];
	FILE *nb;

	if (argc < 2)
	{
		printf("Give your message as the first argument\n");
		exit(EXIT_FAILURE);
	}

	strcpy(buffer, argv[1]);

	if ((nb = fopen(NOTICEBOARD, "a+")) == NULL)
	{ perror("failed to open file");
		exit(EXIT_FAILURE);
	}

	fprintf(nb, "\n%s\n", buffer);

	fclose(nb);

	return EXIT_SUCCESS;
  }
