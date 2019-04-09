#include <stdio.h>
#include <mpi.h>

int main(int argc, char *argv[])
{
	int rank, p;
	
	// Explore the world
	MPI_Init(&argc,&argv);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Comm_size(MPI_COMM_WORLD, &p);
	
	// say hello
	printf("Hello world from %d of %d\n", rank, p);
	
	MPI_Finalize();
}