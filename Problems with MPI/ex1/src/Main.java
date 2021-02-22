import mpi.MPI;

public class Main {
    /*
    Write a program that computes the convolution:
           n-1
    c(i) = SUM a(j) * b(i-j) , for simplicity take (i-j)%N
           j=0
     */

    public static int[] master(int[] a, int[] b, int size){
        int nrElements = size/MPI.COMM_WORLD.Size();
        int start = 0;
        int end = 0;
        for(int i=0; i<MPI.COMM_WORLD.Size(); i++){
            if(i+1==MPI.COMM_WORLD.Size()){
                nrElements += size%MPI.COMM_WORLD.Size();
                end += nrElements;
            }
            else{
                end += nrElements;
                int[] buff = new int[3];
                buff[0] = a.length;
                buff[1] = start;
                buff[2] = end;
                MPI.COMM_WORLD.Send(buff, 0, 3, MPI.INT, i+1,0);
                MPI.COMM_WORLD.Send(a, 0, a.length, MPI.INT, i+1, 1);
                MPI.COMM_WORLD.Send(b, 0, a.length, MPI.INT, i+1,2);
                start = end;
            }
        }

        int[] result = new int[a.length];
        int[] partialResultMaster = computeElements(a,b,end, start);
        nrElements = size/MPI.COMM_WORLD.Size();
        int index = 0;
        for(int i=0; i<MPI.COMM_WORLD.Size(); i++){
            if(i+1==MPI.COMM_WORLD.Size()){
                nrElements += size%MPI.COMM_WORLD.Size();
                end += nrElements;
                for(int j=0; j<nrElements; j++){
                    result[index] = partialResultMaster[j];
                    System.out.println(result[index]);
                    index++;
                }
            }
            else{
               int[] partial = new int[end-start];
               MPI.COMM_WORLD.Recv(partial, 0,end-start, MPI.INT, i+1, 4);
               for(int j=0; j<nrElements; j++) {
                   result[index] = partial[j];
                   System.out.println(result[index]);
                   index++;
               }
            }
        }
        return result;
    }

    public static int[] computeElements(int[] a, int[] b, int end, int start){
        int[] partialResult = new int[end-start];
        for(int i=0; i<end; i++){
            partialResult[i] = 0;
            for(int j=0; j<a.length; j++){
                partialResult[i] += a[j]*b[Math.abs((i-j)%a.length)];
            }
        }
        return partialResult;
    }

    public static void worker(){
        int[] buff = new int[3];
        MPI.COMM_WORLD.Recv(buff, 0, 3, MPI.INT, 0, 0);
        int[] a = new int[buff[0]];
        int[] b = new int[buff[0]];
        MPI.COMM_WORLD.Recv(a,0,buff[0], MPI.INT, 0, 1);
        MPI.COMM_WORLD.Recv(b,0,buff[0], MPI.INT, 0, 2);
        int[] result = computeElements(a,b,buff[2], buff[1]);
        MPI.COMM_WORLD.Send(result, 0, result.length, MPI.INT, 0, 4);
    }


    public static void main(String[] args) {
	// write your code here
        MPI.Init(args);
        int me = MPI.COMM_WORLD.Rank();
        if(me == 0){
            int[] a =  {1,2,3,4};
            int[] b = {1,0,0,0};
            master(a,b,a.length);
        }
        else{
            worker();
        }
        MPI.Finalize();
    }
}
