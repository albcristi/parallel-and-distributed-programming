import mpi.MPI;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Main {

    /*
    Computes all primes up to N
     - master distributes list with all primes till SQRT(N)

     */

    public static void worker(){
        int[] buff = new int[3];
        MPI.COMM_WORLD.Recv(buff, 0,3,MPI.INT, 0, 0);
        int[] primes = new int[buff[0]];
        int startNumber = buff[1];
        MPI.COMM_WORLD.Recv(primes, 0, buff[0], MPI.INT, 0, 1);
        ArrayList<Integer> lst = new ArrayList<>();
        for(int i=0; i<buff[2]; i++)
            if(isPrime(startNumber+i,primes))
                lst.add(startNumber+i);
        int[] arr = new int[lst.size()];
        for(int i =0; i<lst.size(); i++)
            arr[i] =lst.get(i);
        int[] res = new int[1];
        res[0] = lst.size();
        MPI.COMM_WORLD.Send(res, 0,1, MPI.INT, 0, 3);
        if(res[0]>0)
            MPI.COMM_WORLD.Send(arr, 0, lst.size(), MPI.INT, 0, 4);
    }

    public static List<Integer> master(int n, int[] primesTilSqrtN){
        int sqrtN = (int) Math.sqrt(n);
        int noPerProcess = (n-sqrtN+1)/MPI.COMM_WORLD.Size();
        int startNumber = sqrtN;
        for(int i=0; i<MPI.COMM_WORLD.Size(); i++){
            if(i+1==MPI.COMM_WORLD.Size()){
                noPerProcess += (n-sqrtN+1)%MPI.COMM_WORLD.Size();
            }
            else {
                int[] buff = new int[3];
                buff[0] = primesTilSqrtN.length;
                buff[1] = startNumber;
                buff[2] = noPerProcess;
                MPI.COMM_WORLD.Send(buff, 0, buff.length, MPI.INT, i+1, 0);
                MPI.COMM_WORLD.Send(primesTilSqrtN, 0, primesTilSqrtN.length, MPI.INT, i+1,1);
                startNumber+=noPerProcess;
            }
        }
        List<Integer> result = new ArrayList<>();
        for(int i=0; i<noPerProcess; i++){
            if(isPrime(startNumber+i,primesTilSqrtN))
                result.add(i+startNumber);
        }
        for(int i=0; i<MPI.COMM_WORLD.Size()-1;i++){
            int[] buff = new int[1];
            MPI.COMM_WORLD.Recv(buff, 0, 1,MPI.INT, i+1,3);
            if(buff[0]>0){
                int[] res = new int[buff[0]];
                MPI.COMM_WORLD.Recv(res, 0, buff[0],MPI.INT, i+1,4);
                for(int val: res)
                    result.add(val);
            }
        }
        System.out.println(result);
        return result;
    }


    public static boolean isPrime(int n, int[] primes){
        for(int val: primes) {
            if (n % val == 0)
                return false;
        }
        return true;
    }


    public static void main(String[] args) {
	// write your code here
        MPI.Init(args);
        int me = MPI.COMM_WORLD.Rank();
        if(me==0){
            // todo: take user input
            int n = 25;
            int[] primesTilSqrtN = {2,3};
            List<Integer> result = master(n, primesTilSqrtN);
        }
        else{
            worker();
        }
        MPI.Finalize();
    }
}
