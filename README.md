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

