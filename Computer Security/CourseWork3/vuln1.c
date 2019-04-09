#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char **argv){
  int correct_pwd = 0;

  char pwd[10];
  char entered_pwd[10];
  
  FILE *fptr = fopen("password.txt", "r");

  if(fptr == NULL){
    printf("Error file opening!");
    exit(1);
  }

  fscanf(fptr, "%s", pwd);
  fclose(fptr);

  strcpy(entered_pwd, argv[1]);
  
  if(strcmp(entered_pwd, pwd)){
    printf("Incorrect password\n");
  }
  else{
    correct_pwd = 1;
    printf("Correct password\n");
  }
  if(correct_pwd){
    printf("\nYou are now logged in with root access\n");
  }
  return 0;
}
