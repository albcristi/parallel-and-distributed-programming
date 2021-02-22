using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using MPI;

namespace ex4
{
    class Program
    {
        public static List<int> result = new List<int>();

        static void PrintList(List<int> l)
        {
            var s = "";
            foreach (var v in l)
                //Console.Write(v + " "+"process: "+ Communicator.world.Rank);
                s += v + " ";

            Console.WriteLine(s);
        }


        static bool condition(List<int> l)
        {
            return l[0] % 2 == 0;
        }


        static void Master()
        {
            //back(new List<int>(), 3);
            int n = 3;
            for (int i = 1; i < Communicator.world.Size; i++)
            {
                Communicator.world.Send(n, i, 0);
            }
            var start = Communicator.world.Rank * n / Communicator.world.Size;
            var end = ((Communicator.world.Rank + 1) * n) / Communicator.world.Size;
            //Console.WriteLine("n: {0}, start: {1}, end: {2}", n, start, end);
            var res = 0;

            for (int i = start; i < end; i++)
            {
                var l = new List<int>();
                l.Add(i);
                back(l, n);
                if (result.Count != 0)
                    break;
            }
            if(result.Count != 0)
            {
                Console.WriteLine("RESULT FROM 0");
                PrintList(result);

            }
            for (int i = 1; i < Communicator.world.Size; i++)
            {
                List<int> resPartial;
                resPartial= Communicator.world.Receive<List<int>>(i, 0);
                if (resPartial.Count != 0)
                {
                    Console.WriteLine("RESULT FROM "+i);
                    PrintList(resPartial);
                }
            }
            Console.WriteLine("result for cunt: {0} in process {1}", res, Communicator.world.Rank);

        }

        static void Slave()
        {
            var n = Communicator.world.Receive<int>(0, 0);

            var start = Communicator.world.Rank * n / Communicator.world.Size;
            var end = ((Communicator.world.Rank + 1) * n) / Communicator.world.Size;
            //Console.WriteLine("n: {0}, start: {1}, end: {2}", n, start, end);
            for (int i = start; i < end; i++)
            {
                var l = new List<int>();
                l.Add(i);
                back(l, n);
                if (result.Count != 0)
                    break;
            }
            //Console.WriteLine("result for cunt: {0} in process {1}", res, Communicator.world.Rank);
            // params: from, to
           // PrintList(result);
            Communicator.world.Send<List<int>>(result, 0, 0);
        }


        static void back(List<int> conf, int n)
        {
            if (conf.Count == n)
            {
                // PrintList(conf);
                if (condition(conf) && result.Count == 0)
                    result.AddRange(conf);
            }

            else
            {
     
                for (int i = 0; i < n; i++)
                {
                    if (conf.Contains(i)) continue;
                    conf.Add(i);
                    back(conf, n);
                    if (result.Count != 0)
                        return;
                    conf.RemoveAt(conf.Count - 1);
                }
            }
        }
        static void Main(string[] args)
        {
            MPI.Environment.Run(ref args, communicator => {
                if (Communicator.world.Rank == 0)
                {
                    Master();
                }
                else
                { //
                  
                    Slave();
                   
                }
            });
        }
    }
}
