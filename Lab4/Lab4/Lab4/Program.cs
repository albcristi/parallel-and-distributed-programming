using System;
using System.Collections.Generic;


namespace Lab4
{
    class Program
    {
        static void Main(string[] args)
        {
            List<string> hosts = new List<string>();
            hosts.Add("en.wikipedia.org/wiki/Lenna");
            hosts.Add("www.terraform.io/");
            hosts.Add("en.wikipedia.org/wiki/Canada");
            hosts.Add("aws.amazon.com/");

            //Type1WithCallbacks t1 = new Type1WithCallbacks(hosts);
            //t1.StartExecution();

            //Type2Tasks t2 = new Type2Tasks(hosts);
            //t2.StartExecution();

            Type3AsyncAwait t3 = new Type3AsyncAwait(hosts);
            t3.StartExecution();
            Console.ReadLine();
        }
    }
}
