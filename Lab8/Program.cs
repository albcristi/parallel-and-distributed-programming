using System;
using System.Threading;
using MPI;

namespace Lab8
{
    class Program
    {
      
        static void listenerThread(Object dsmObject)
        {
            DSM dsm;
            try
            {
                dsm = (DSM)dsmObject;
            }
            catch (Exception)
            {
                return;
            }
         
            while (true)
            {
                Message message = Communicator.world.Receive<Message>(Communicator.anySource, Communicator.anyTag);
                if (message.exitMessage != null)
                {
                    Console.WriteLine("Proc.Rank: " + Communicator.world.Rank + ": received shut down message");
                    break;
                }
                
                if (message.subscribe != null)
                {
                    Console.WriteLine("Proc.Rank: " + Communicator.world.Rank + ", received subscribe message from process: " +
                        message.subscribe.processRank + ", that subscribes to: " + message.subscribe.variable);
                    dsm.SubscribeToVariable(message.subscribe.variable, message.subscribe.processRank);
                }

                if(message.update != null)
                {
                    Console.WriteLine("Proc Rank: " + Communicator.world.Rank + " update on variable: " + message.update.variable + "="
                        + message.update.newValue+". Initiator: "+message.initiator);
                    dsm.updateVariable(message.update.variable, message.update.newValue);
                }

                if(message.change != null)
                {
                    Console.WriteLine("Proc Rank: " + Communicator.world.Rank + " compare and exchange operation: var=" + message.change.variable
                        + "comp value: " + message.change.oldValue + ", new value=" + message.change.newValue);
                    Console.WriteLine("Proc Rank: " + Communicator.world.Rank + " Compare and exhange operation executed with status: "
                        + dsm.DoCompareAndExhange(message.change.variable, message.change.oldValue, message.change.newValue));
                    
                }

            }
        }


        static void Main(string[] args)
        {
            using (new MPI.Environment(ref args))
            {
                {
                    DSM dsm = new DSM();
                    if (Communicator.world.Rank == 0)
                    {
                        Thread thread = new Thread(listenerThread);
                        thread.Start(dsm);
                        dsm.SubscribeToVariable("foo1");
                        dsm.SubscribeToVariable("foo2");
                        dsm.SubscribeToVariable("foo3");
                        ConsoleInterface console = new ConsoleInterface(dsm);
                        console.Run();                     
                        
                    }
                    if (Communicator.world.Rank == 1)
                    {
                        Thread thread = new Thread(listenerThread);
                        thread.Start(dsm);
                        dsm.SubscribeToVariable("foo1");
                        dsm.SubscribeToVariable("foo2");
                        thread.Join();
                    }
                    if(Communicator.world.Rank > 1)
                    {
                        Thread thread = new Thread(listenerThread);
                        thread.Start(dsm);
                        dsm.SubscribeToVariable("foo1");
                        dsm.SubscribeToVariable("foo3");
                        thread.Join();
                    }
                }
            }
        }
    }
}
