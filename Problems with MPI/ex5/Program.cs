using System;
using System.Collections.Generic;
using MPI;

namespace ex5
{
    /*
     * counts how many permutations of N satisfy a given realtion
     * Use MPI
     */
    class Program
    {
        static int localCount = 0;
        
        public static bool Pred(List<int> list)
        {
            return list[0] % 2 == 0;
        }
        
        public static void Worker()
        {
            int start, end, n;
            start = Communicator.world.Receive<int>(0, 0);
            end = Communicator.world.Receive<int>(0, 1);
            n = Communicator.world.Receive<int>(0, 2);
            for(int i=start; i<end; i++)
            {
                List<int> l = new List<int>();
                l.Add(i);
                BuildPermutations(l, n);
            }
            Communicator.world.Send(localCount, 0, 0);
        }
        public static void Master(int n)
        {
            int noPerProcess = n / Communicator.world.Size;
            int start = 0;
            int end = noPerProcess;
            for(int i=1; i<Communicator.world.Size; i++)
            {
                Communicator.world.Send(start, i, 0);
                Communicator.world.Send(end, i, 1);
                Communicator.world.Send(n, i, 2);
                start = end;
                end += noPerProcess;
            }
            end += n % Communicator.world.Size;
            for(int i=start; i<end; i++)
            {
                List<int> startList = new List<int>();
                startList.Add(i);
                BuildPermutations(startList, n);
            }
            for(int i=1; i<Communicator.world.Size; i++)
            {
                localCount += Communicator.world.Receive<int>(i, 0);
            }
            Console.WriteLine("Result: " + localCount);
        }

        public static void BuildPermutations(List<int> conf, int n)
        {
            if (conf.Count == n)
            {
                if (Pred(conf))
                    localCount += 1;
            }

            else
            {
                for (int i = 0; i < n; i++)
                {
                    if (conf.Contains(i)) continue;
                    conf.Add(i);
                    BuildPermutations(conf, n);
                    conf.RemoveAt(conf.Count - 1);
                }

            }
        }

        static void Main(string[] args)
        {
            MPI.Environment.Run(ref args, communicator => {
                if (Communicator.world.Rank == 0)
                {
                    //get input
                    int n = 3;
                    Master(n);
                }
                else
                {
                    Worker();
                }
            });
        }
    }
}
