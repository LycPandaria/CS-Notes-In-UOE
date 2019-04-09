#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void vuln(char *name, int index)
{
  char buf[100] = "Let's see if it works!?!!";
  char *ptr = buf + index;
  strcpy(ptr, name);
  printf("Welcome %s\n", ptr);
}

int main(int argc, char *argv[])
{
  vuln(argv[1], atoi(argv[2]));
  printf("Returned to main\n");
  return 0;
}
