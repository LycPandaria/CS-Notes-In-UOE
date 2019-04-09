/*

The implementation is almost same with TaskFarmer. The difference is that in this task the number of tasks will increase from 1 task to thousands, 
and then reduce to 0. We have to build a structure to meet the needs of dynamic task producing and consuming. More than that, we have to keep an eye 
on the state of the workers, because no task in farmer may not mean the completion of computation, working workers may result in more tasks to be assigned. 
As long as there are no more task in the task stack and no more working wokers, then we can finish the computation.

In term of a farmer, a task stack is used in this program to store tasks(In this case, a task is a pair of points). Every time farmer wants to 
assign a task, farmer pop up a task and send a task to a worker using MPI_Send with TAG_TASK.   And when the farmer probes a message with TAG_NOT_QULIFIED), 
the farmer will receive 4 points from this message and combine them into two subtask, push into the task stack, then assign to workers later on.
If the farmer gets a message with tag(TAG_DONE), means that this area computed with this pair of points is good enough, 
the farmer will add the result to total area and continues assigning tasks. Farmer also has to track the state of workers, 
so a array of int to indicate the worker is working or not. 
It helps the farmer to assign tasks properly and determine when to finish the computation. Finally, when the task stack is empty and 
all worker stops working, a message with tag(TAG_NO_MORE) is sent by MPI_Send to stop all workers.

In term of workers, each time they received a task(a pair of points), workers will compute area to find while the result is qualified. 
If the result is good (<=EPSILON), a message with tag(TAG_DONE) and result will send to farmer indicating that this result is a good 
approximate of the area of the two points. Otherwise, worker will divide these two points into 2 new subtasks(4 points), 
sending back to farmer with TAG_NOT_QULIFIED. So the farmer will know and add these two subtasks into stack.

An MPI_Probe is also used because messages with different tags contain data with different length. 
The usage of MPI_Probe makes us create a suitable receiving buffer in advance and avoid errors.
 */ 

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <mpi.h>
#include "stack.h"

#define EPSILON 1e-3
#define F(arg)  cosh(arg)*cosh(arg)*cosh(arg)*cosh(arg)
#define A 0.0
#define B 5.0

#define FARMER 0
#define SLEEPTIME 1

// Tag message
#define TAG_TASK 1
#define TAG_DONE 2
#define TAG_NOT_QULIFIED 3
#define TAG_NO_MORE 4

// Worker state
#define WORKING 1
#define NOTWORKING 0

int *tasks_per_process;

double farmer(int);

void worker(int);

int main(int argc, char **argv ) {
  int i, myid, numprocs;
  double area, a, b;

  MPI_Init(&argc, &argv);
  MPI_Comm_size(MPI_COMM_WORLD,&numprocs);
  MPI_Comm_rank(MPI_COMM_WORLD,&myid);

  if(numprocs < 2) {
    fprintf(stderr, "ERROR: Must have at least 2 processes to run\n");
    MPI_Finalize();
    exit(1);
  }

  if (myid == 0) { // Farmer
    // init counters
    tasks_per_process = (int *) malloc(sizeof(int)*(numprocs));
    for (i=0; i<numprocs; i++) {
      tasks_per_process[i]=0;
    }
  }

  if (myid == 0) { // Farmer
    area = farmer(numprocs);
  } else { //Workers
    worker(myid);
  }

  if(myid == 0) {
    fprintf(stdout, "Area=%lf\n", area);
    fprintf(stdout, "\nTasks Per Process\n");
    for (i=0; i<numprocs; i++) {
      fprintf(stdout, "%d\t", i);
    }
    fprintf(stdout, "\n");
    for (i=0; i<numprocs; i++) {
      fprintf(stdout, "%d\t", tasks_per_process[i]);
    }
    fprintf(stdout, "\n");
    free(tasks_per_process);
  }
  MPI_Finalize();
  return 0;
}

double farmer(int numprocs) {
  int i, tag, who;
  MPI_Status status;
  double *data, temp;
  // store the total area
  double totalArea = 0.0;
  // start point
  double initData[2] = {A, B};
  double leftPart[2] = {0.0, 0.0};
  double rightPart[2] = {0.0, 0.0};
  double newtask[4];	// receive new task from wokers
  // stack to track the process
  stack *stack = new_stack();

  // an array to indicate worker state, 0 means not working, 1 means working
  
  int *workerState = calloc(numprocs, sizeof(int));
  int numOfWorkingWorker = 0; // track the number of working wokers
  // init worker array
  for(i = 0; i < numprocs; i++){
	workerState[i] = NOTWORKING;
  }

  push(initData, stack);  // put start point to stack

  // compute area
  while(!is_empty(stack) || numOfWorkingWorker!=0) {

    for(i=1; i<numprocs; i++){
      // is this worker is idle and there is task in the stack
      if(workerState[i] == NOTWORKING && !is_empty(stack)){
        // assign task the this worker
        data = pop(stack);  // get the data pair we want to send to the worker
        MPI_Send(data, 2, MPI_DOUBLE, i, TAG_TASK, MPI_COMM_WORLD);

        // record
        workerState[i] = WORKING;
        // update
        tasks_per_process[i]++;
        numOfWorkingWorker++;
      }
    }

    // get tag
    MPI_Probe(MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &status);
    tag = status.MPI_TAG;
    who = status.MPI_SOURCE;
    if(tag == TAG_DONE){
      // means this part is qualified
      // receive part of result into temp
      MPI_Recv(&temp, 1, MPI_DOUBLE, who, tag, MPI_COMM_WORLD, &status);
      totalArea+=temp;

    } else if (tag == TAG_NOT_QULIFIED){
      // means this data set is not qualified, we have to reassign
      MPI_Recv(newtask, 4, MPI_DOUBLE, who, tag, MPI_COMM_WORLD, &status);

      // set new pair to left and right part
      leftPart[0] = newtask[0];
      leftPart[1] = newtask[1];
      rightPart[0] = newtask[2];
      rightPart[1] = newtask[3];

      // push new data pair to be processed into stack
      push(leftPart, stack);
      push(rightPart, stack);
    } else {
      printf("OH, we get a message with unknown tag\n");
    }

    //update
    numOfWorkingWorker--;
	workerState[who] = NOTWORKING;

  }

  for(i = 1; i < numprocs; i++){
    MPI_Send(NULL, 0, MPI_DOUBLE, i, TAG_NO_MORE, MPI_COMM_WORLD);
  }

  return totalArea;
}

void worker(int mypid) {
  double left, right, fleft, fright, mid, fmid;
  double  larea, rarea, partarea;
  double result;  // store good result
  double task[2]; // data pair this worker has to do
  double newtask[4];	// new tasks 
  int tag = TAG_TASK;
  int firstreceive = 1;
  MPI_Status status;	

  while(1){
    // wait another message
    MPI_Probe(FARMER, MPI_ANY_TAG, MPI_COMM_WORLD, &status);
    tag = status.MPI_TAG;
    if(tag == TAG_NO_MORE){
      break;
    }
    MPI_Recv(task, 2, MPI_DOUBLE, FARMER, tag, MPI_COMM_WORLD, &status);

    usleep(SLEEPTIME);
    // compute
    left = task[0];
    fleft = F(left);
    right = task[1];
    fright = F(right);
    mid = (left + right) / 2;
    fmid = F(mid);
    larea = (fleft + fmid) * (mid - left) / 2;
    rarea = (fmid + fright) * (right - mid) / 2;
    partarea = (fleft + fright) * ((right - left)/2);

    //printf("I`m %d, I am doing with %2.2f and %2.2f", mypid, left, right);
    if(fabs((larea + rarea) - partarea) > EPSILON){
      // this data part is not qualified
      // send message to framer that this part result is not good
	  newtask[0] = left;
	  newtask[1] = mid;
	  newtask[2] = mid;
	  newtask[3] = right;
	  
      MPI_Send(newtask, 4, MPI_DOUBLE, FARMER, TAG_NOT_QULIFIED, MPI_COMM_WORLD);
    } else{
      // this data part is good
      result = larea + rarea;
      MPI_Send(&result, 1, MPI_DOUBLE, FARMER, TAG_DONE, MPI_COMM_WORLD);
    }
  }
}
