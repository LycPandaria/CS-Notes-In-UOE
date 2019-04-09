
aquadA: aquadPartA.c
	 mpicc --std=c99 -Wall -o aquadPartA aquadPartA.c stack.h stack.c -lm

aquadA_run: aquadA
	mpirun -c 5 ./aquadPartA

aquadB: aquadPartB.c
	 mpicc --std=c99 -Wall -o aquadPartB aquadPartB.c stack.h stack.c -lm

aquadB_run: aquadB
	mpirun -c 5 ./aquadPartB
