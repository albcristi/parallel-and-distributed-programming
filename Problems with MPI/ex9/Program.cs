using MPI;
using System;
using System.Collections.Generic;

namespace ex9
{
    class Program
    {
        public static void Worker(int me, int noProcesses)
        {
            int start = Communicator.world.Receive<int>(0, 0);
            List<int> p = Communicator.world.Receive<List<int>>(0, 1);
            List<int> q = Communicator.world.Receive<List<int>>(0, 2);
            int local = 0;
            int noPerProcess = p.Count / Communicator.world.Size;
            int computed = 0;
            while (computed < noPerProcess)
            {
                local += p[start] * q[start];
                start++;
                computed++;
            }
            Communicator.world.Send(local, 0, 0);
        }

        public static int Master(List<int> v, List<int> q)
        {
            int noPerProcess = v.Count / Communicator.world.Size;
            int start = 0;
            for(int i=1; i < Communicator.world.Size; i++)
            {
                Communicator.world.Send(start, i, 0);
                Communicator.world.Send(v, i, 1);
                Communicator.world.Send(q, i, 2);
                start += noPerProcess;
            }
            noPerProcess += v.Count % Communicator.world.Size;
            int dotProduct = 0;
            int computed = 0;
            while (computed < noPerProcess)
            {
                dotProduct += v[start] * q[start];
                start++;
                computed++;
            }
            for(int i=1;i<Communicator.world.Size; i++)
            {
                dotProduct += Communicator.world.Receive<int>(i, 0);
            }
            return dotProduct;
        }
        static void Main(string[] args)
        {
            MPI.Environment.Run(ref args, communicator =>
            {
                if (Communicator.world.Rank == 0)
                {
                    List<int> v = new List<int>() { 1, 2, 3 };
                    List<int> q = new List<int>() { 1, 1, 1 };
                    Console.WriteLine(Master(v, q));
                }
                else
                {
                    Worker(Communicator.world.Rank, Communicator.world.Size);
                }
            });
        }
    }
}
