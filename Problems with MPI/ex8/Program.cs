using System;
using System.Collections.Generic;
using MPI;
namespace ex8
{
    class Program
    {
        /*
         * Computes the power of a NxN Matrix
         */
        public static void Worker(int nrProcess, int nrProcessess)
        {
            bool stopCondition = false;
            while (!stopCondition)
            {
                int orderNumber = Communicator.world.Receive<int>(0, 0);
                stopCondition = Communicator.world.Receive<bool>(0, 1);
                List<List<int>> a = Communicator.world.Receive<List<List<int>>>(0, 2);
                List<List<int>> b = Communicator.world.Receive<List<List<int>>>(0, 3);
                int noPerProcess = (a.Count * b[0].Count) / Communicator.world.Size;
                int computed = 0;
                List<int> result = new List<int>();
                while(computed < noPerProcess)
                {
                    int element = ComputeElement(a, b, orderNumber);
                    result.Add(element);
                    orderNumber++;
                    computed++;
                }
                Communicator.world.Send(result, 0, 0);
            }
        }

        public static List<List<int>> Master(List<List<int>> matrix, int power)
        {
            return MatrixPower(matrix, power, 0);
        }

        public static List<List<int>> MatrixPower(List<List<int>> matrix, int n, int depth)
        {
            if (n == 1)
                return matrix;
            List<List<int>> half = MatrixPower(matrix, n / 2, depth + 1);
            bool breakMessage = false;
            if(depth == 0)
            {
                breakMessage = true;
            }
            if (n % 2 == 0)
                return MultiplyMaster(half, half, breakMessage);
            return MultiplyMaster(matrix, MultiplyMaster(half, half, false), breakMessage);
        }

        public static List<List<int>> MultiplyMaster(List<List<int>> a, List<List<int>> b, bool stopCondition) {
            int noPerProcess = (a.Count * b[0].Count) / Communicator.world.Size;
            int orderNumber = 1;
            for(int i=1; i<Communicator.world.Size; i++)
            {
                Communicator.world.Send<int>(orderNumber, i, 0);
                Communicator.world.Send<bool>(stopCondition, i, 1);
                Communicator.world.Send<List<List<int>>>(a, i, 2);
                Communicator.world.Send<List<List<int>>>(b, i, 3);
                orderNumber += noPerProcess;
            }
            List<List<int>> result = new List<List<int>>();
            for(int i=0; i<a.Count; i++)
            {
                result.Add(new List<int>());
                for (int j = 0; j < b[0].Count; j++)
                    result[i].Add(0);
            }
            noPerProcess += (a.Count * b[0].Count) % Communicator.world.Size;
            int computed = 0;
            while (computed < noPerProcess)
            {
                int element = ComputeElement(a, b, orderNumber);
                KeyValuePair<int, int> coordinates = GetCoordinatesByOrderNumber(result[0].Count, orderNumber);
                result[coordinates.Key][coordinates.Value] = element;
                orderNumber++;
                computed++;
            }
            orderNumber = 1;
            for(int i=1; i<Communicator.world.Size; i++)
            {
                List<int> elements = Communicator.world.Receive<List<int>>(i, 0);
                foreach(int val in elements)
                {
                    KeyValuePair<int, int> coordinates = GetCoordinatesByOrderNumber(result[0].Count, orderNumber);
                    result[coordinates.Key][coordinates.Value] = val;
                    orderNumber++;
                }
            }
            return result;
        }

        public static int ComputeElement(List<List<int>> a, List<List<int>> b, int orderNumber)
        {
            KeyValuePair<int, int> coordinates = GetCoordinatesByOrderNumber(b[0].Count, orderNumber);
            int sum = 0;
            for(int i=0; i<a[0].Count; i++)
            {
                sum += a[coordinates.Key][i] * b[i][coordinates.Value];
            }
            return sum;
        }
        public static KeyValuePair<int, int> GetCoordinatesByOrderNumber(int noColumns, int orderNo)
        {
            if (orderNo % noColumns == 0)
                return new KeyValuePair<int, int>(orderNo / noColumns - 1, noColumns - 1);
            return new KeyValuePair<int, int>(orderNo / noColumns, orderNo % noColumns - 1);
        }

        static void Main(string[] args)
        {
            MPI.Environment.Run(ref args, communicator =>
            {
                if (Communicator.world.Rank == 0)
                {
                    List<List<int>> matrix = new List<List<int>>() { new List<int>() { 1, 2,3}, 
                                                                     new List<int>() { 1,2,3},
                                                                     new List<int>(){0,0,1 } };
                    List<List<int>> result = Master(matrix, 3);
                    String s = "";
                    foreach(List<int> line in result)
                    {
                        foreach (int val in line)
                            s += val + " ";
                        s += "\n";
                    }
                    Console.WriteLine(s);
                }
                else
                {
                    Worker(Communicator.world.Rank, Communicator.world.Size);
                }
            });
        }
    }
}
