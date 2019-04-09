#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define EPSILON 1e-3
#define F(arg)  cosh(arg)*cosh(arg)*cosh(arg)*cosh(arg)
#define A 0.0
#define B 5.0

double quad (double, double, double, double, double);

int main(int argc, char **argv) {
  double area = quad(A, B, F(A), F(B), (F(A)+F(B)) * (B-A)/2);
  fprintf(stdout, "Area = %lf\n", area);
  return 0;
}

double quad(double left, double right, double fleft, double fright, double lrarea) {
  double mid, fmid, larea, rarea;
  
  mid = (left + right) / 2;
  fmid = F(mid);
  larea = (fleft + fmid) * (mid - left) / 2;
  rarea = (fmid + fright) * (right - mid) / 2;
  if( fabs((larea + rarea) - lrarea) > EPSILON ) {
    larea = quad(left, mid, fleft, fmid, larea);
    rarea = quad(mid, right, fmid, fright, rarea);
  }
  return (larea + rarea);
}
