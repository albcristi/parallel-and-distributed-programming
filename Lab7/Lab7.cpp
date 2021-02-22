#include <iostream>
#include <mpi.h>
#include <vector>
#include <time.h>
#include <chrono>
#include <sstream>
#include <stdint.h>
#include <atomic>

using namespace std;
std::atomic<int> currentProcess = 0;

// get random value
int random(int min, int max) //range : [min, max]
{
    static bool FIRST = true;
    if (FIRST)
    {
        srand(time(NULL)); //seeding for the first time only!
        FIRST = false;
    }
    return min + rand() % ((max + 1) - min);
}

// get log base 3 of a number
double logInBase3(int number) {
    return log(number) / log(3);
}

// generates a random polynomial, to be used as input
void generate(vector<int>& v, size_t n)
{
    v.reserve(n);
    for (size_t i = 0; i < n; ++i) {
        v.push_back(random(2, 20));
    }
}

// prepare polynomial
void preparePolynomial(vector<int>& pol, size_t size) {
    for (int index = 0; index < size; ++index)
        pol.push_back(0);
}

// TO STRING FUNCTION FOR POLYNOMIAL
string toString(vector<int>& v) {
    std::ostringstream s;
    for (int i = 0; i < v.size(); i++) {
        s << v[i] << "X^" << i;
        if (i != v.size() - 1)
            s << "+";
    }
    return s.str();
}

// compute coefficient value for a multiplcation of two polynomials
int computeCoefficient(vector<int>& p, vector<int>& q, int index) {
    int value = 0;
    for(int idx = 0; idx <= index; ++idx)
        if (idx < p.size() && index - idx < q.size()) {
            value += p[idx] * q[index - idx];
        }
    return value;
}

// Miltiply sequential
vector<int> multiplySequentialAlgorithm(vector<int>& p, vector<int>& q) {
    vector<int> result;
    for (int index = 0; index < p.size() + q.size() - 1; ++index)
        result.push_back(computeCoefficient(p, q, index));
    return result;
}

// code that will be executed by the worker process
void workerSimpleMultiplication(int proccesNo) {
    int size1, size2, s, e;
    vector <int> p, q, res;
    MPI_Status status;
    MPI_Recv(&size1, 1, MPI_INT, 0, 1, MPI_COMM_WORLD, &status);
    preparePolynomial(p, (size_t) size1);
    MPI_Recv(p.data(), size1, MPI_INT, 0, 2, MPI_COMM_WORLD, &status);
    MPI_Recv(&size2, 1, MPI_INT, 0, 3, MPI_COMM_WORLD, &status);
    preparePolynomial(q, (size_t)size2);
    MPI_Recv(q.data(), size2, MPI_INT, 0, 4, MPI_COMM_WORLD, &status);
    MPI_Recv(&s, 1, MPI_INT, 0, 5, MPI_COMM_WORLD, &status);
    MPI_Recv(&e, 1, MPI_INT, 0, 6, MPI_COMM_WORLD, &status);
    for (int index = 0; index < e - s; ++index) {
        res.push_back(computeCoefficient(p,q,s+index));
    }
    int size = res.size();
    MPI_Ssend(&size, 1, MPI_INT, 0, 7, MPI_COMM_WORLD);
    MPI_Ssend(res.data(), size, MPI_INT, 0, 8, MPI_COMM_WORLD);
}

// code that will be executed by the master process
vector<int> multiplyPolynomialMaster(vector<int>& p, vector<int>& q, int noProcesses) {
    vector<int> result;
    int dP, dQ;
    dP = p.size() - 1; // degree polynomial p
    dQ = q.size() - 1; // degree polynomial q
    // now we have to split work for each process
    // we will use the same approach from the previous
    // laboratory with the parallel simple multiplication
    int startIndex,noPerProcess, proc;
    startIndex = 0;
    noPerProcess = (dP+dQ+1) / noProcesses;
    for (proc = 1; proc < noProcesses; ++proc) {
        // send size p 
        int size1, size2, s, e;
        size1 = p.size();
        MPI_Ssend(&size1, 1, MPI_INT, proc, 1, MPI_COMM_WORLD);
        // send p
        MPI_Ssend(p.data(), size1, MPI_INT, proc, 2, MPI_COMM_WORLD);
        // send size q
        size2 = q.size();
        MPI_Ssend(&size2, 1, MPI_INT, proc, 3, MPI_COMM_WORLD);
        // send 2
        MPI_Ssend(q.data(), size2, MPI_INT, proc, 4, MPI_COMM_WORLD);
        // send start index
        s = startIndex;
        MPI_Ssend(&s, 1, MPI_INT, proc, 5, MPI_COMM_WORLD);
        // send end index
        e = startIndex+noPerProcess;
        MPI_Ssend(&e, 1, MPI_INT, proc, 6, MPI_COMM_WORLD);
        startIndex += noPerProcess;
    }
    preparePolynomial(result, dP + dQ + 1);
    cout << startIndex << endl;
    for (int val = startIndex; val < result.size(); ++val) {
        result[val] = computeCoefficient(p, q, val);
    }
    // computation for the current proccess
    int index = 0;
    for (proc = 1; proc < noProcesses; ++proc) {
        int size;
        vector<int> partialResult;
        MPI_Status status;
        MPI_Recv(&size, 1, MPI_INT, proc, 7, MPI_COMM_WORLD, &status);
        preparePolynomial(partialResult, size);
        MPI_Recv(partialResult.data(), size, MPI_INT, proc, 8, MPI_COMM_WORLD, &status);
        for (auto value : partialResult) {
            result[index] = value;
            index++;
        }
    }

    return result;
}


// karatsuba internal
vector<int> karatsubaInternal(vector<int>& p, vector<int>& q) {
    //(P1(X)+P2(X)) * (Q1(X)+Q2(X)) - P1(X)* Q1(X) - P2(X)*Q2(X)
    if (p.size() <= 10 || q.size() <= 10) {
        vector<int> v = multiplySequentialAlgorithm(p, q);
        v.push_back(0);
        return v;
    }
    int size = p.size() / 2;
    vector<int> p1, p2, q1, q2, p1PLUSp2, q1PLUSq2;
    preparePolynomial(p1, size); preparePolynomial(p2, size); preparePolynomial(q1, size);
    preparePolynomial(q2, size); preparePolynomial(p1PLUSp2, size); preparePolynomial(q1PLUSq2, size);
    for (int i = 0; i < size; ++i) {
        p2[i] = p[i];
        q2[i] = q[i];
        p1[i] = p[size + i];
        q1[i] = q[size + i];
        p1PLUSp2[i] = p1[i] + p2[i]; // (P1(X)+P2(X))
        q1PLUSq2[i] = q1[i] + q2[i]; // (Q1(X)+Q2(X))
    }
    vector<int> prod1, prod2, prod12;
        prod1 = karatsubaInternal(p1, q1);
        prod2 = karatsubaInternal(p2, q2);
        prod12 = karatsubaInternal(p1PLUSp2, q1PLUSq2);
    vector<int> prod, coefficients;
    for (int index = 0; index < p.size(); ++index) {
        prod.push_back(prod12[index] - prod1[index] - prod2[index]);
    }
    preparePolynomial(coefficients, p.size() * 2);
    for (int index = 0; index < p.size(); ++index) {
        coefficients[index] += prod2[index];
        coefficients[index + p.size()] += prod1[index];
        coefficients[index + p.size() / 2] += prod[index];
    }
    return coefficients;
}

// karatsuba for processes
void karatsubaWorker(int processNo) {
    cout << processNo << endl;
    int size1, size2;
    vector<int> p, q;
    MPI_Status status;
    MPI_Recv(&size1, 1, MPI_INT, 0, processNo * 10 + 1, MPI_COMM_WORLD, &status);
    preparePolynomial(p, size1);
    MPI_Recv(p.data(), size1, MPI_INT, 0, processNo * 10 + 2, MPI_COMM_WORLD, &status);
    MPI_Recv(&size2, 1, MPI_INT, 0, processNo * 10 + 3, MPI_COMM_WORLD, &status);
    preparePolynomial(q, size2);
    MPI_Recv(q.data(), size2, MPI_INT, 0, processNo * 10 + 4, MPI_COMM_WORLD, &status);
    vector<int> result = karatsubaInternal(p, q);
    int size;
    size = result.size();
    MPI_Ssend(result.data(), size, MPI_INT, 0, processNo * 10 + 5, MPI_COMM_WORLD);
}

// karatsuba algorithm for master process
vector<int> karatsubaMaster(vector<int>& p, vector<int>& q, int currentDepth, int noProcesses) {
    //(P1(X)+P2(X)) * (Q1(X)+Q2(X)) - P1(X)* Q1(X) - P2(X)*Q2(X)
    if (p.size() <= 10 || q.size()<= 10) {
        vector<int> v = multiplySequentialAlgorithm(p, q);
        v.push_back(0);
        return v;
    }
    int size = p.size() / 2;
    vector<int> p1, p2, q1, q2, p1PLUSp2, q1PLUSq2;
    preparePolynomial(p1, size); preparePolynomial(p2, size); preparePolynomial(q1, size);
    preparePolynomial(q2, size); preparePolynomial(p1PLUSp2, size); preparePolynomial(q1PLUSq2, size);
    for (int i = 0; i < size; ++i) {
        p2[i] = p[i];
        q2[i] = q[i];
        p1[i] = p[size + i];
        q1[i] = q[size+i];
        p1PLUSp2[i] = p1[i] + p2[i]; // (P1(X)+P2(X))
        q1PLUSq2[i] = q1[i] + q2[i]; // (Q1(X)+Q2(X))
    }
    vector<int> prod1, prod2, prod12;
    int id1, id2, id3;
    if ((int)logInBase3(noProcesses) == currentDepth + 1) {
        // we need to send data to processes
        // send data for karatsuba(p1,q1)
        int size1, size2, size21, size22, size31, size32;
        size1 = p1.size();
        currentProcess += 1;
        id1 = currentProcess;
        MPI_Ssend(&size1, 1, MPI_INT, currentProcess, currentProcess * 10 + 1, MPI_COMM_WORLD);
        MPI_Ssend(p1.data(), size1, MPI_INT, currentProcess, currentProcess * 10 + 2, MPI_COMM_WORLD);
        size2 = q1.size();
        MPI_Ssend(&size2, 1, MPI_INT, currentProcess, currentProcess * 10 + 3, MPI_COMM_WORLD);
        MPI_Ssend(q1.data(), size1, MPI_INT, currentProcess, currentProcess * 10 + 4, MPI_COMM_WORLD);
        // send data for karatsuba(p2,q2)
        size21 = p2.size();
        currentProcess += 1;
        id2 = currentProcess;
        MPI_Ssend(&size21, 1, MPI_INT, currentProcess, currentProcess * 10 + 1, MPI_COMM_WORLD);
        MPI_Ssend(p2.data(), size21, MPI_INT, currentProcess, currentProcess * 10 + 2, MPI_COMM_WORLD);
        size22 = q2.size();
        MPI_Ssend(&size22, 1, MPI_INT, currentProcess, currentProcess * 10 + 3, MPI_COMM_WORLD);
        MPI_Ssend(q2.data(), size22, MPI_INT, currentProcess, currentProcess * 10 + 4, MPI_COMM_WORLD);
        //send data for karatsuba(p1Plusp2, q1Plusq2)
        size31 = p1PLUSp2.size();
        currentProcess += 1;
        id3 = currentProcess;
        MPI_Ssend(&size31, 1, MPI_INT, currentProcess, currentProcess * 10 + 1, MPI_COMM_WORLD);
        MPI_Ssend(p1PLUSp2.data(), size31, MPI_INT, currentProcess, currentProcess * 10 + 2, MPI_COMM_WORLD);
        size32 = q1PLUSq2.size();
        MPI_Ssend(&size32, 1, MPI_INT, currentProcess, currentProcess * 10 + 3, MPI_COMM_WORLD);
        MPI_Ssend(q1PLUSq2.data(), size32, MPI_INT, currentProcess, currentProcess * 10 + 4, MPI_COMM_WORLD);

        // TIME TO GET DATA BACK
        MPI_Status status1, status2, status3;
        int size = p1.size() + q1.size() - 1;
        preparePolynomial(prod1, size+1);
        MPI_Recv(prod1.data(), size+1, MPI_INT, id1, id1 * 10 + 5, MPI_COMM_WORLD, &status1);
        size = p2.size() + q2.size() - 1;
        preparePolynomial(prod2, size+1);
        MPI_Recv(prod2.data(), size+1, MPI_INT, id2, id2 * 10 + 5, MPI_COMM_WORLD, &status2);
        size = p1PLUSp2.size() + q1PLUSq2.size() - 1;
        preparePolynomial(prod12, size+1);
        MPI_Recv(prod12.data(), size+1, MPI_INT, id3, id3 * 10 + 5, MPI_COMM_WORLD, &status3);
    }
    else {
        prod1 = karatsubaMaster(p1, q1, currentDepth + 1, noProcesses);
        prod2 = karatsubaMaster(p2, q2, currentDepth + 1, noProcesses);
        prod12 = karatsubaMaster(p1PLUSp2, q1PLUSq2, currentDepth + 1, noProcesses);
    }
    vector<int> prod, coefficients;
    for (int index = 0; index < p.size(); ++index) {
        prod.push_back(prod12[index] - prod1[index] - prod2[index]);
    }
    preparePolynomial(coefficients, p.size() * 2);
    for (int index = 0; index < p.size(); ++index) {
        coefficients[index] += prod2[index];
        coefficients[index + p.size()] += prod1[index];
        coefficients[index + p.size() / 2] += prod[index];
    }
    return coefficients;
}

int main(int argc, char** argv)
{
    MPI_Init(0, 0);
    int me;
    int noProcesses;
    MPI_Comm_size(MPI_COMM_WORLD, &noProcesses);
    MPI_Comm_rank(MPI_COMM_WORLD, &me);

    unsigned n;
    vector<int> p, q;
    if (argc != 2 || 1 != sscanf_s(argv[1], "%u", &n)) {
        fprintf(stderr, "data not found\n");
        return 1;
    }
    

    if (me == 0) {
        // master process
        generate(p, n);
        generate(q, n);
        std::cout << "Generated Data" << std::endl;
        std::cout << "P=" << toString(p) << std::endl;
        std::cout << "Q=" << toString(q) << std::endl;
        //vector<int> polynomialSimpleResult = multiplyPolynomialMaster(p, q, noProcesses);
        vector<int> polynomialSimpleResult = karatsubaMaster(p, q, 0, noProcesses);
        polynomialSimpleResult.pop_back();
        std::cout << "Result:" << endl;
        std::cout << "R=" << toString(polynomialSimpleResult) << std::endl;
    }
    else {
        // worker process
       //workerSimpleMultiplication(me);
       karatsubaWorker(me);
    }
    MPI_Finalize();
    return 0;
}