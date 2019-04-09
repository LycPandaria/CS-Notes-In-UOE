#include <stdio.h>
#include <unistd.h>
int main( )
{
char *name[3];
char large_string[25];

int i;
for( i = 0; i < 25; i++)
   large_string[i] = 'A';

name[0] = "vuln1";
name[1] = large_string;
name[2] = NULL;

execve(name[0], name, NULL);
return 0;
}