// stackdemo.c
// Simple illustration of the use of the provided stack
//
// Compile: gcc -o stackdemo stackdemo.c stack.h stack.c
//
// Run:  ./stackdemo

#include <stdio.h>
#include <stdlib.h>
#include "stack.h"

int main(int argc, char **argv) {

  int i;
  double x, y, *data;
  double points[2];

  stack *stack = new_stack();

  for (i=0; i<10; i++) {
    points[0]=0.1*i;
    points[1]=0.2*i;
    push(points, stack);
  }

  for (i=0; i<10; i++) {
    data = pop(stack);
    x = data[0];
    y = data[1];
    fprintf(stdout, "%2.2f, %2.2f\n", x, y);
  }

  return 0;
}
