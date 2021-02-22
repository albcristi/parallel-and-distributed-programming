import mpi.MPI;

public class Main {
    /*
       Perform Matrix Multiplication
     */

    public static Pair<Integer,Integer>  getElementCoordinates(Integer noColumns, Integer orderNo){
        if(orderNo%noColumns==0)
            return new Pair<>(orderNo/noColumns-1, noColumns-1);
        return new Pair<>(orderNo/noColumns, orderNo%noColumns-1);
    }


    public  static int doProduct(int orderNo, int[][] a, int[][] b){
        int product = 0;
        Pair<Integer, Integer> coordinates = getElementCoordinates(b[0].length, orderNo);
        for(int i=0; i<a.length; i++){
            product += a[coordinates.first][i] * b[i][coordinates.second];
        }
        return  product;
    }


    public static void worker() {
        int[] buff = new int[6];
        MPI.COMM_WORLD.Recv(buff, 0, 6, MPI.INT, 0, 0);
        int[][] a = new int[buff[0]][buff[1]];
        int[][] b = new int[buff[2]][buff[3]];
        MPI.COMM_WORLD.Recv(a, 0, a.length, MPI.OBJECT, 0, 1);
        MPI.COMM_WORLD.Recv(b, 0, b.length, MPI.OBJECT, 0, 2);
        int[] result = new int[buff[5]];
        int order = buff[4];
        for (int i = 0; i < buff[5]; i++) {
            result[i] = doProduct(order, a, b);
            System.out.println(result[i]);
            order++;
        }
        MPI.COMM_WORLD.Send(result,0,result.length, MPI.INT, 0, 4);
    }

    public static int[][] master(int[][] a, int[][] b){
        int[][] result = new int[a.length][b[0].length];
        int noPerProc = (result.length*result[0].length)/MPI.COMM_WORLD.Size();
        int order = 1;
        for(int i=0; i<MPI.COMM_WORLD.Size(); i++){
            if(i+1==MPI.COMM_WORLD.Size()){
                noPerProc += (result.length*result[0].length)%MPI.COMM_WORLD.Size();
            }
            else{
                int[] buff = new int[6];
                buff[0] = a.length;
                buff[1] = a[0].length;
                buff[2] = b.length;
                buff[3] = b[0].length;
                buff[4] = order;
                buff[5] = noPerProc;
                MPI.COMM_WORLD.Send(buff, 0,6,MPI.INT, i+1,0);
                MPI.COMM_WORLD.Send(a,0,a.length, MPI.OBJECT, i+1, 1);
                MPI.COMM_WORLD.Send(b,0,b.length, MPI.OBJECT, i+1, 2);
                order += noPerProc;
            }
        }
        int[] partial = new int[noPerProc];
        for(int i=0; i<noPerProc; i++){
            partial[i] = doProduct(order,a,b);
            order+=1;
        }
        order = 1;
        noPerProc = (result.length*result[0].length)/MPI.COMM_WORLD.Size();
        for(int i=0; i<MPI.COMM_WORLD.Size(); i++){
            Pair<Integer, Integer> coordinates = getElementCoordinates(result[0].length,order);
            if(i+1 == MPI.COMM_WORLD.Size()){
                for (int value : partial) {
                    result[coordinates.first][coordinates.second] = value;
                    order++;
                    coordinates = getElementCoordinates(result[0].length,order);
                }
            }else{
                int[] resWorker = new int[noPerProc];
                MPI.COMM_WORLD.Recv(resWorker,0,noPerProc, MPI.INT, i+1,4);
                for (int value : resWorker) {
                    result[coordinates.first][coordinates.second] = value;
                    order++;
                    coordinates = getElementCoordinates(result[0].length,order);
                }
            }
        }
        StringBuilder s = new StringBuilder("");
        for(int i= 0; i<result.length; i++){
            for(int j=0; j<result[0].length; j++)
                s.append(result[i][j]).append(" ");
            s.append("\n");
        }
        System.out.println(s.toString());
        return result;
    }


    public static void main(String[] args) {
	// write your code here
        MPI.Init(args);
        int me = MPI.COMM_WORLD.Rank();
        if(me == 0){
            int[][] a = {{1,2},{3,4}};
            int[][] b = {{1,2},{3,4}};
            int[][] result = master(a,b);
        }
        else {
            worker();
        }
    }
}

class Pair<T,K> {
    public T first;
    public K second;

    public Pair(T first, K second){
        this.first = first;
        this.second = second;
    }
}
