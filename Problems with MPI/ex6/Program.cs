using System;
using System.Collections.Generic;
using MPI;

namespace ex6
{

    class Program
    {
        // no subsets of N of lenght K that satisify a certain property


        static int localCount = 0;

        public static bool Pred(List<int> lst)
        {
            return lst[0] % 2 == 0;
        }

        public static void BuildSubsetsOfLengthK(List<int> elems, int n,int k)
        {
            if (elems.Count == k)
            {
                if (Pred(elems))
                    localCount += 1;
            }
            else
            {
                for (int i = elems[elems.Count-1]+1; i < n; i++)
                {
                    elems.Add(i);
                    BuildSubsetsOfLengthK(elems, n, k);
                    elems.RemoveAt(elems.Count - 1);
                }

            }
        }

        static void Master(int n, int k)
        {
            // in order to balance the work, each process will take every mTH element
            // where m = Nr Of Proccesses

            for (int i = 1; i < Communicator.world.Size; i++)
            {
                Communicator.world.Send(n, i, 0);
                Communicator.world.Send(k, i, 1);
            }
            int start = Communicator.world.Size-1;
            int step = Communicator.world.Size;
            while(start<=(n-k))
            {
                List<int> lst = new List<int>();
                lst.Add(start);
                BuildSubsetsOfLengthK(lst, n, k);
                start += step;
            }
            Console.WriteLine(Communicator.world.Rank + "-->" + localCount);
            for (int i=1; i<Communicator.world.Size; i++)
            {
                int localResult = Communicator.world.Receive<int>(i, 0);
                localCount += localResult;
            }
            Console.WriteLine("Result:" + localCount);
        }



        static void Worker(int nrProcess, int nrProcesses)
        {
            int n = Communicator.world.Receive<int>(0, 0);
            int k = Communicator.world.Receive<int>(0, 1);
            int start = Communicator.world.Rank - 1;
            int step = Communicator.world.Size;
            while(start <= (n-k))
            {
                List<int> lst = new List<int>();
                lst.Add(start);
                BuildSubsetsOfLengthK(lst, n, k);
                start += step;
                Console.WriteLine(start);
            }
            Console.WriteLine(Communicator.world.Rank + "-->" + localCount);
            Communicator.world.Send(localCount, 0, 0);
        }

        static void Main(string[] args)
        {
            MPI.Environment.Run(ref args, communicator =>
            {
                if (Communicator.world.Rank == 0)
                {
                    //get input
                    int n = 3;
                    int k = 2;
                    Master(n,k);
                }
                else
                {
                    Worker(Communicator.world.Rank, Communicator.world.Size);
                }
            });
        }
    }
}
