using System;
using System.Collections.Generic;
using MPI;

namespace ex7
{
    class Program
    {
        /*
         * Multiply two big numbers
         */

        public static void Worker(int nrProcess, int nrProcesses)
        {
            int startIndex = Communicator.world.Receive<int>(0, 0);
            int nrDigitsPerProcess = Communicator.world.Receive<int>(0, 1);
            List<int> a = Communicator.world.Receive<List<int>>(0, 2);
            List<int> b = Communicator.world.Receive<List<int>>(0, 3);
            int computed = 0;
            List<int> result = new List<int>();
            for (int i = 0; i < b.Count + a.Count; i++)
                result.Add(0);
            Console.WriteLine(nrDigitsPerProcess);
            while (startIndex < b.Count && computed < nrDigitsPerProcess)
            {
                List<int> partialResult = DoMultiplication(a, b[startIndex], startIndex);
                String ss = "";
                for (int index = 0; index < partialResult.Count; index++)
                {
                    ss += partialResult[index] + " ";
                    result[index] = result[index] + partialResult[index];
                    if (result[index] > 9)
                    {
                        int c;
                        c = result[index] / 10;
                        result[index] = result[index] % 10;
                        result[index + 1] += c;

                    }
                }
                computed++;
                startIndex++;
            }
            while (nrDigitsPerProcess!=0 && result[result.Count - 1] == 0)
                result.RemoveAt(result.Count - 1);
            Communicator.world.Send<List<int>>(result, 0, 4);
        }

        public static List<int> Master(List<int> a, List<int> b)
        {
            int nrDigitsPerProccess = b.Count / Communicator.world.Size;
            int startIndex = 0;
            b.Reverse();
            a.Reverse();
            for(int i=1; i<Communicator.world.Size; i++)
            {
                Communicator.world.Send(startIndex, i, 0);
                Communicator.world.Send(nrDigitsPerProccess, i, 1);
                Communicator.world.Send(a, i, 2);
                Communicator.world.Send(b, i, 3);
                startIndex += nrDigitsPerProccess;
            }
            nrDigitsPerProccess += b.Count % Communicator.world.Size;
            int computed = 0;
            List<int> result = new List<int>();
            for (int i = 0; i < b.Count + a.Count; i++)
                result.Add(0);

            while (startIndex<b.Count && computed < nrDigitsPerProccess)
            {
                List<int>partialResult = DoMultiplication(a, b[startIndex], startIndex);
                String ss = "";
                for(int index=0; index<partialResult.Count; index++)
                {
                    ss += partialResult[index] + " ";
                    result[index] = result[index] + partialResult[index];
                    if(result[index] > 9)
                    {
                        int c;
                        c = result[index] / 10;
                        result[index] = result[index] % 10;
                        result[index + 1] += c;

                    }
                }
                Console.WriteLine(ss);
                computed++;
                startIndex++;
            }
            for(int i=1; i<Communicator.world.Size; i++)
            {
                List<int> partialResult = Communicator.world.Receive<List<int>>(i, 4);
                for (int index = 0; index < partialResult.Count; index++)
                {
                    result[index] = result[index] + partialResult[index];
                    if (result[index] > 9)
                    {
                        int c;
                        c = result[index] / 10;
                        result[index] = result[index] % 10;
                        result[index + 1] += c;

                    }
                }
            }
            result.Reverse();
            while (result[0] == 0)
                result.RemoveAt(0);
            String s = "";
            foreach (int val in result)
                s += val + "  ";
            Console.WriteLine(0+"--->"+s);
            return result;
        }

        public static List<int> DoMultiplication(List<int> a, int digit, int exponent)
        {
            List<int> result = new List<int>();
            int c = 0;
            while (c < exponent)
            {
                result.Add(0);
                c++;
            }
            int carry = 0;
            foreach(int val in a)
            {
                int res = (val * digit + carry) % 10;
                carry = (val * digit + carry) / 10;
                result.Add(res);
            }
            if (carry > 0)
            {
                result.Add(carry);
            }
            Console.WriteLine("end product");
         return result;
        }
        static void Main(string[] args)
        {
            MPI.Environment.Run(ref args, communicator =>
            { 
                if (Communicator.world.Rank == 0)
                {
                    List<int> b = new List<int>() { 9,9,9,9 };
                    List<int> a = new List<int>() { 9,9,9};
                    if(a.Count < b.Count)
                    {
                        List<int> aux = a;
                        a = b;
                        b = aux;
                    }
                    Master(a, b);
                }
                else
                {
                    Worker(Communicator.world.Rank, Communicator.world.Size);
                }
            });

        }
    }
}
