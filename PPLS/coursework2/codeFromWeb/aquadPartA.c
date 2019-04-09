/** * This needs to be compiled with the -std=c99 flag. For example on DICE this is:
 *  /usr/lib64/openmpi/bin/mpicc -std=c99 -o aquadPartA aquadPartA.c stack.h stack.c
 *
 *
 * The farmer sends an initial item of work to a single worker to start processing. This uses
 * MPI_Send as opposed to MPI_Isend as there is nothing else that will need to be done until work has completed
 * so blocking until the buffer is free is fine.
 *
 *
 * For both the worker and farmer MPI_Probe is used before MPI_Recv. This is because
 * different sized data is sent between processes so that it is required to get the
 * type of the message before it is possible to know how much data to receive (i.e.
 * what the count argument needs to be for MPI_Recv). This could have been worked around
 * by padding the data sent but that seemed inelegant. The blocking versions
 * were used as using the non blocking versions wouldn't have given any advantage. They would
 * have had to busy wait which is worse than blocking.
 *
 *
 * To reduce the complexity of the program rather than have a worker send a message
 * when it has no more work or storing state to attempt to deduce when a worker is
 * inactive, as soon as work is sent back from a worker it is known to be inactive
 *
 * This means that it is impossible to send back multiple messages from the worker (as doing so would
 * invalidate the assumption above). So in the case where two tasks need to be spawned rather than sending
 * two request for more work containing (left, mid) and (mid, right) a single message is sent. As the
 * worker knows the left and right sent to the process (as it still has the buffer) the worker 
 * doesn't need to send anything back other than an empty message saying that more work needs to be allocated.
 * From informal testing this seemed to be slightly faster than sending three items of data back.
 *
 *
 * Until all workers complete the farmer waits for the data from any worker.
 * The call to MPI_Probe (checking what type of data is coming) is blocking however 
 * this doesn't matter as for a worker to become inactive it has to send some data to the
 * farmer. This means that unless there isn't any work it wouldn't be possible to
 * get into a state where the farmer is blocked waiting for a message and a worker is inactive.
 *
 * After some data has been received the farmer then sends work to any worker that doesn't
 * currently have a task assigned. For the most part at this point there will only
 * be one task that doesn't have any work allocated (the task that has just sent some
 * data back to the farmer). The only case where more work would need to be sent is initially when
 * the amount of work is scaling up from 1 to n processes.
 *
 * The messages from farmer to worker are sent using MPI_Isend which doesn't block. This
 * is slightly more complex as the result from popping the work off the stack (i.e. the buffer passed to MPI_Isend)
 * then needs to be stored until it is known that MPI_Isend has finished with the buffer.
 * Rather than fafing with checking this it is assumed (a valid assumption) that the buffer is finished
 * with when we get the next result from the worker at which point it is freed. This
 * didn't make much (if any) performance difference possibly due to the fact that the buffer
 * is so small.
 *
 *
 * The worker preforms the work of the algorithm, taking two input values and either
 * producing a single result to add to the total or sends a (single) message with
 * two new tasks to preform to the farmer. The farmer sends a message to the worker to halt
 * when the algorithm finishes running. The worker uses the blocking versions of Probe, Recv and
 * Send. When receiving data there is no harm in blocking (if there is no data there is nothing
 * that it could do instead). When sending data it needs to wait for a task from the worker
 * in response to its message so blocking until the buffer is available for use again doesn't
 * cause a delay.
 *
 * 
 * In no case was MPI_Bsend used. This is because it provided no advantage over MPI_Isend
 * but added additional complexity. MPI_Bsend requires the user to define the buffer space 
 * (via MPI_Buffer_Attach) and because of this allows the reuse of the send buffer.
 * There is no need to reuse the send buffer, due to the send buffer being allocated on the heap
 * and being able to deallocate when we are sure that it has been used without adding any
 * additional complexity. Because of this reason MPI_Isend is preferred over MPI_Bsend
 * because the additional complexity of allocating buffers(MPI_Buffer_attach) with MPI_Bsend
 * for no gain.
 */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <mpi.h>
#include <time.h>
#include <unistd.h>
#include <stdbool.h>
#include "stack.h"


#define EPSILON 1e-3
#define F(arg)  cosh(arg)*cosh(arg)*cosh(arg)*cosh(arg)
#define A 0.0
#define B 5.0

#define SLEEPTIME 1

#define FARMER_ID 0


enum Tags {
    TAG_HALT,  //Sent from farmer to workers when needed to halt
    TAG_WORK,  //Send from farmer to workers with work to do
    TAG_MORE,  //Sent from workers to the farmer with more tasks to do
    TAG_RESULT //Sent from workers to farmers with a partial result
};

typedef struct {
    double* buffer;
    bool isWorking;
} WorkerData;

int* tasks_per_process;

double farmer(int);

void worker(int);

int main(int argc, char** argv ) {
    int i, myid, numprocs;
    double area;

    MPI_Init(&argc, &argv);
    MPI_Comm_size(MPI_COMM_WORLD, &numprocs);
    MPI_Comm_rank(MPI_COMM_WORLD, &myid);

    if(numprocs < 2) {
        fprintf(stderr, "ERROR: Must have at least 2 processes to run\n");
        MPI_Finalize();
        exit(1);
    }

    if (myid == FARMER_ID) {
        tasks_per_process = (int*) calloc(numprocs, sizeof(int));
    }

    if (myid == FARMER_ID) {
        area = farmer(numprocs);
    } else {
        worker(myid);
    }

    if(myid == FARMER_ID) {
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
    //unused but a required for a argument. Out here so that it doesn't go out of scope
    MPI_Request ignored_request;
    double totalArea = 0;

    int numWorkingTasks = 0;

    WorkerData* tasks = calloc(numprocs, sizeof(WorkerData));
    stack* workStack = new_stack();

    double initial[2] = {A,B};
    push(initial, workStack);

    while ( numWorkingTasks != 0 || !is_empty(workStack) ){

        for ( int i = 1; i < numprocs; ++i ){
            if ( !is_empty(workStack) && !tasks[i].isWorking ){
                double* work = tasks[i].buffer = pop(workStack);

                ++numWorkingTasks;
                ++tasks_per_process[i];
                tasks[i].isWorking = true;
                MPI_Isend(work, 2, MPI_DOUBLE, i, TAG_WORK, MPI_COMM_WORLD, &ignored_request);
            }
        }

        //probe must be used to work out which tag (and so how much data) is going to be received
        MPI_Status status;
        MPI_Probe(MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &status);

        if ( status.MPI_TAG == TAG_RESULT ){
            double partialResult;
            MPI_Recv(&partialResult, 1, MPI_DOUBLE, MPI_ANY_SOURCE, TAG_RESULT, MPI_COMM_WORLD, &status);
            totalArea += partialResult;
        }else{
            MPI_Recv(NULL, 0, MPI_DOUBLE, MPI_ANY_SOURCE, TAG_MORE, MPI_COMM_WORLD, &status);

            double* buffer = tasks[status.MPI_SOURCE].buffer;
            double mid = (buffer[0] + buffer[1])/2;

            double lhs[2] = {buffer[0], mid};
            push(lhs, workStack);
            double rhs[2] = {mid, buffer[1]};
            push(rhs, workStack);
        }
        //as a task has sent some data, it no longer has any work to do and so
        //can be flagged as not working
        tasks[status.MPI_SOURCE].isWorking = false;
        --numWorkingTasks;

        //now can free work, if there isn't work this should be null and so is fine to free
        free(tasks[status.MPI_SOURCE].buffer);
        tasks[status.MPI_SOURCE].buffer = NULL; //avoids having to remember which are free
    }

    for ( int i = 0; i < numprocs; ++i ){
        free(tasks[i].buffer);
    }
    free(tasks);
    free_stack(workStack);

    //when the task has finished we need to send a command to the workers for
    //them to halt.
    for ( int i = 1; i < numprocs; ++i ){
        MPI_Send(NULL, 0, MPI_DOUBLE, i, TAG_HALT, MPI_COMM_WORLD);
    }

    return totalArea;
}

void worker(int mypid) {
    MPI_Status status;

    while ( true ){
        MPI_Probe(FARMER_ID, MPI_ANY_TAG, MPI_COMM_WORLD, &status);

        if ( status.MPI_TAG == TAG_HALT ){
            MPI_Recv(NULL, 0, MPI_DOUBLE, FARMER_ID, TAG_HALT, MPI_COMM_WORLD, &status);
            break;
        }

        double data[2];
        MPI_Recv(data, 2, MPI_DOUBLE, FARMER_ID, TAG_WORK, MPI_COMM_WORLD, &status);

        usleep(SLEEPTIME);

        double left = data[0];
        double right = data[1];
        double mid = (left + right) / 2;

        double fleft = F(left);
        double fright = F(right);
        double fmid = F(mid);

        double larea = (fleft + fmid) * (mid - left) / 2;
        double rarea = (fmid + fright) * (right - mid) / 2;
        double lrarea = (fleft + fright) * ((right - left)/2);

        if( fabs((larea + rarea) - lrarea) > EPSILON ) {
            //send the fact that the result should be split back to the farmer. The farmer knows
            //what was sent to the worker and so is capable of dividing up the data
            MPI_Send(NULL, 0, MPI_DOUBLE, FARMER_ID, TAG_MORE, MPI_COMM_WORLD);
        }else{
            double total = larea + rarea;
            MPI_Send(&total, 1, MPI_DOUBLE, FARMER_ID, TAG_RESULT, MPI_COMM_WORLD);
        }
    }
}
