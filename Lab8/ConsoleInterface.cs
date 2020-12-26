using System;
using System.Collections.Generic;
using System.Text;

namespace Lab8
{
    class ConsoleInterface
    {
        DSM dsm;

        public ConsoleInterface(DSM dsm)
        {
            this.dsm = dsm;
        }

        private void ShowMenu()
        {
            StringBuilder menu = new StringBuilder("");
            menu.Append("1 - Set value for variable\n");
            menu.Append("2 - Compare var with value\n");
            menu.Append("x - Exit\n");
            Console.WriteLine(menu.ToString());
        }

        private void RunCommand1()
        {
            try
            {
                Console.WriteLine("Enter variable name");
                string variable = Console.ReadLine();
                Console.WriteLine("Enter new value");
                int value = int.Parse(Console.ReadLine());
                dsm.UpdateVariableInitiator(variable, value);
            }
            catch (Exception)
            {

            }
        }

        private void RunCommand2()
        {
            try
            {
                Console.WriteLine("Enter variable name");
                string variable = Console.ReadLine();
                Console.WriteLine("Comparing value");
                int value = int.Parse(Console.ReadLine());
                Console.WriteLine("New value");
                int newValue = int.Parse(Console.ReadLine());
                dsm.DoCompareAndExchangeInitiator(variable, value, newValue);
            }
            catch (Exception)
            {

            }
        }

        private void RunExitCommand()
        {
            dsm.SendShutDownMessage();
        }

        public void Run()
        {
            Boolean terminate = false;
            while (!terminate)
            {
                ShowMenu();
                Console.WriteLine("Write command");
                String command = Console.ReadLine();
                switch (command)
                {
                    case "1":
                        RunCommand1();
                        break;
                    case "2":
                        RunCommand2();
                        break;
                    default:
                        RunExitCommand();
                        terminate = true;
                        break;

                }
            }
        }
    }
}
