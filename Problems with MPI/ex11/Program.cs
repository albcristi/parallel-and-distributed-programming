using MPI;
using System;
using System.Collections.Generic;

namespace ex11
{
    class Program
    {
        public static KeyValuePair<int, int> GetCoordinatesByOrderNumber(int noColumns, int orderNo)
        {
            if (orderNo % noColumns == 0)
                return new KeyValuePair<int, int>(orderNo / noColumns - 1, noColumns - 1);
            return new KeyValuePair<int, int>(orderNo / noColumns, orderNo % noColumns - 1);
        }

        public static void Worker(int myId, int nrProcs)
        {
            int order = Communicator.world.Receive<int>(0, 0);
            List<List<int>> a = Communicator.world.Receive<List<List<int>>>(0, 1);
            List<List<int>> b = Communicator.world.Receive<List<List<int>>>(0, 2);
            int noPerProcess = (a.Count * a[0].Count) / Communicator.world.Size;
            int computed = 0;
            List<int> partial = new List<int>();
            while (computed < noPerProcess)
            {
                KeyValuePair<int, int> coordinates = GetCoordinatesByOrderNumber(a[0].Count, order);
                partial.Add(a[coordinates.Key][coordinates.Value] + b[coordinates.Key][coordinates.Value]);
                computed++;
                order++;
            }
            Communicator.world.Send(partial, 0, 0);
        }

        //master
        public static List<List<int>> Primes(List<List<int>> a, List<List<int>> b, int nrProcs)
        {
            List<List<int>> result = new List<List<int>>();
            for(int i=0; i<a.Count; i++)
            {
                result.Add(new List<int>());
                for (int j = 0; j < a[0].Count; j++)
                    result[i].Add(0);
            }

            int order = 1;
            int noPerProcess = (result.Count * result[0].Count) / Communicator.world.Size;
            for(int i=1; i<Communicator.world.Size; i++)
            {
                Communicator.world.Send(order, i, 0);
                Communicator.world.Send(a, i, 1);
                Communicator.world.Send(b, i, 2);
                order += noPerProcess;
            }
            noPerProcess += (result.Count * result[0].Count) % Communicator.world.Size;
            int computed = 0;
            while (computed < noPerProcess)
            {
                KeyValuePair<int, int> coordinates = GetCoordinatesByOrderNumber(result[0].Count, order);
                result[coordinates.Key][coordinates.Value] = a[coordinates.Key][coordinates.Value]
                    + b[coordinates.Key][coordinates.Value];
                order++;
                computed++;
            }
            order = 1;
            for(int i=1; i < Communicator.world.Size; i++)
            {
                List<int> partial = Communicator.world.Receive<List<int>>(i, 0);
                foreach(int val in partial)
                {
                    KeyValuePair<int, int> coordinates = GetCoordinatesByOrderNumber(result[0].Count, order);
                    result[coordinates.Key][coordinates.Value] = val;
                    order++;
                }
            }
            return result;
        }

        static void Main(string[] args)
        {
            // Sum matrices, matrices of same diminesions 
            MPI.Environment.Run(ref args, communicator =>
            {
                if (Communicator.world.Rank == 0)
                {
                    List<List<int>> a = new List<List<int>>()
                    {
                        new List<int>(){1,2,3},
                        new List<int>(){-1,2,-3}
                    };
                    List<List<int>> b = new List<List<int>>()
                    {
                        new List<int>(){1,2,3 },
                        new List<int>(){1,4,3}
                    };
                    List<List<int>> result = Primes(a, b, Communicator.world.Size);
                    String res = "";
                    foreach(List<int> line in result)
                    {
                        foreach (int val in line)
                            res += val + " ";
                        res += "\n";
                    }
                    Console.WriteLine(res);
                }
                else
                {
                    Worker(Communicator.world.Rank, Communicator.world.Size);
                }
            });
        }
    }
}
