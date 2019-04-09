#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <mpi.h>

#define MAX_TASKS 100
#define NO_MORE_TASKS MAX_TASKS+1
#define FARMER 0

int main(int argc, char *argv[])
{
	int np, rank;
	
	MPI_Init(&argc, &argv);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Comm_size(MPI_COMM_WORLD, &np);
	if(rank==FARMER)
		farmer(np-1);
	else
		worker();
	
	MPI_Finalize();
}

void farmer(int workers)
{
	int i, task[MAX_TASKS], result[MAX_TASKS], temp, tag, who;
	MPI_Status status;
	
	for(i=0; i < MAX_TASKS; i++)
		task[i] = rand()%5;		// set up some 'tasks'
	
	// Assume at least as many tasks as workers
	for(i=0;i<workers;i++)
		MPI_Send(&task[i], 1, MPI_INT, i+1, i, MPI_COMM_WORLD);
	
	while(i<MAX_TASKS)
	{
		// receive data and reassign task
		MPI_Recv(&temp, 1, MPI_INT, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &status);
		
		// get information from worker
		who = status.MPI_SOURCE;
		tag = status.MPI_TAG;
		// store result
		result[tag] = temp;
		// assgin new task to this worker
		MPI_Send(&task[i], i, MPI_INT, who, i , MPI_COMM_WORLD);
		i++;
	}
	
	// 回收最后一次结果
	for(i=0; i<workers; i++){
		MPI_Recv(&temp, 1, MPI_INT, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &status);
		who=status.MPI_SOURCE;
		tag=status.MPI_TAG;
		result[tag]=temp;
		// send terminate sign
		MPI_Send(&task[i], 1, MPI_INT, who, NO_MORE_TASKS, MPI_COMM_WORLD);
	}
}

void worker(int rank){
	int taskdone = 0;
	int workdone = 0;
	int task, result, tag;
	MPI_Status status;
	
	MPI_Recv(&task, 1, MPI_INT, FARMER, MPI_ANY_TAG, MPI_COMM_WORLD, &status);
	
	tag = status.MPI_TAG;
	while(tag!=NO_MORE_TASKS)
	{
		sleep(task);
		result = rank;
		workdone+=task;		// total work he has done
		taskdone++;	// number of task he has done
		MPI_Send(&result, 1, MPI_INT, FARMER, tag, MPI_COMM_WORLD);	//send message to farmer that he has done this task
		MPI_Recv(&task, 1, MPI_INT, FARMER, MPI_ANY_TAG, MPI_COMM_WORLD, &status);
	}
	
	printf("Worker %d sloved %d tasks totalling %d percentage of work/n", rank, taskdone, workdone);
}



















