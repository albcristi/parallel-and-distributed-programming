using MPI;
using System;
using System.Collections.Generic;

namespace ex12
{
    class Program
    {
        static int noSolutions = 0;
        static List<List<int>> solutions = new List<List<int>>();

        // TODO: create solution check for nQueens problem
        public static bool IsSolution(List<int> placements)
        {
            return true;
        }
        public static void Worker(int myId, int nrProcs)
        {

        }

        public static void BuildPermutations(List<int> partial, int n)
        {
            if(partial.Count == n && IsSolution(partial))
            {
                noSolutions++;
                solutions.Add(new List<int>());
                solutions[solutions.Count - 1].AddRange(partial);
                return;
            }

            for(int i=0; i<n; i++)
                if (!partial.Contains(i))
                {
                    partial.Add(i);
                    BuildPermutations(partial, n);
                    partial.Remove(i);
                }
        }

        public static void MakePlacements(int start, int howMany, int n)
        {
            List<List<int>> results = new List<List<int>>();
            int computed = 0;
            while (computed < howMany)
            {
                List<int> partial = new List<int>();
                partial.Add(start);
                BuildPermutations(partial, n);
                computed++;
                start++;
            }
        }
        public static List<List<int>> Master(int n)
        {
            int start = 0;
            int noPerProcess = n / Communicator.world.Size;
            for(int i=1; i<Communicator.world.Size; i++)
            {
                Communicator.world.Send(start, i, 0);
                Communicator.world.Send(n, i, 1);
                start += noPerProcess;
            }
            noPerProcess += n % Communicator.world.Size;
            MakePlacements(start, noPerProcess, n);
            for(int i=1; i<Communicator.world.Size; i++)
            {
                solutions.AddRange(Communicator.world.Receive<List<List<int>>>(i, 0));
                noSolutions += Communicator.world.Receive<int>(i, 1);
            }
            return solutions;
        }
        static void Main(string[] args)
        {
            // n Queens problem: return all possible placements (also computes
            // the number of such placements)
            // ALSO SOLUTION OF: permutations of N that satisfy a given property (modify the IsSolution method)
            // MAYBE RENAME IT
            MPI.Environment.Run(ref args, communicator =>
            {
                if (Communicator.world.Rank == 0)
                {
                    //todo: get input
                    int n = 4;
                    List<List<int>> placements = Master(n);
                    Console.WriteLine(noSolutions);
                    foreach(List<int> solution in placements)
                    {
                        String s = "";
                        foreach (int val in solution)
                            s += val + " ";
                        Console.WriteLine(s);
                    }
                }
                else
                {
                    Worker(Communicator.world.Rank, Communicator.world.Size);
                }
            });
        }
    }
}
