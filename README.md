# Parallel and Distributed Programming

I will add some of the laboratory tasks that I solve during my PDP class

## Task 1

General requirements
  - The problems will require to execute a number of independent operations, that operate on shared data.
  - There shall be several threads launched at the beginning, and each thread shall execute a lot of operations. The operations to be executed are to be randomly choosen, and with randomly choosen parameters.
  - The main thread shall wait for all other threads to end and, then, it shall check that the invariants are obeyed.
  - The operations must be synchronized in order to operate correctly. Write, in a documentation, the rules (which mutex what invariants it protects).
  - You shall play with the number of threads and with the granularity of the locking, in order to asses the performance issues. Document what tests have you done, on what hardware platform, for what size of the data, and what was the time consumed.
  
### Problem Statement - Supermarket inventory

There are several types of products, each having a known, constant, unit price. In the begining, we know the quantity of each product. 
We must keep track of the quantity of each product, the amount of money (initially zero), and the list of bills, corresponding to sales. Each bill is a list of items and quantities sold in a single operation, and their total price.
We have sale operations running concurrently, on several threads. Each sale decreases the amounts of available products (corresponding to the sold items), increases the amount of money, and adds a bill to a record of all sales.
From time to time, as well as at the end, an inventory check operation shall be run. It shall check that all the sold products and all the money are justified by the recorded bills.

## Task 2

### Goal of this laboratory task

Create two threads, a producer and a consumer, with the producer feeding the consumer.


### Requirement

Compute the scalar product of two vectors.

### Details for implementation

Create two threads. The first thread (producer) will compute the products of pairs of elements - one from each vector - and will feed the second thread. The second thread (consumer) will sum up the products computed by the first one. The two threads will behind synchronized with a condition variable and a mutex. The consumer will be cleared to use each product as soon as it is computed by the producer thread.

## Task 3

### Goal of this laboratory task


Divide a simple task between threads. The task can easily be divided in sub-tasks requiring no cooperation at all. See the caching effects, and the costs of creating threads and of switching between threads.

### Requirement

Write several programs to compute the product of two matrices.
Have a function that computes a single element of the resulting matrix.
Have a second function whose each call will constitute a parallel task (that is, this function will be called on several threads in parallel). This function will call the above one several times consecutively to compute several elements of the resulting matrix. Consider the following ways of splitting the work betweeb tasks (for the examples, consider the final matrix being 9x9 and the work split into 4 tasks):

1. Each task computes consecutive elements, going row after row. So, task 0 computes rows 0 and 1, plus elements 0-1 of row 2 (20 elements in total); task 1 computes the remainder of row 2, row 3, and elements 0-3 of row 4 (20 elements); task 2 computes the remainder of row 4, row 5, and elements 0-5 of row 6 (20 elements); finally, task 3 computes the remaining elements (21 elements).

2. Each task computes consecutive elements, going column after column. This is like the previous example, but interchanging the rows with the columns: task 0 takes columns 0 and 1, plus elements 0 and 1 from column 2, and so on.

3. Each task takes every k-th element (where k is the number of tasks), going row by row. So, task 0 takes elements (0,0), (0,4), (0,8), (1,3), (1,7), (2,2), (2,6), (3,1), (3,5), (4,0), etc.

For running the tasks, also implement 2 approaches:

1. Create an actual thread for each task (use the low-level thread mechanism from the programming language);
2. Use a thread pool.
