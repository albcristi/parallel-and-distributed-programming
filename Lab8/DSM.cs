using MPI;
using System;
using System.Collections.Generic;
using System.Text;

namespace Lab8
{
    class DSM
    {
        private Dictionary<string, List<int>> subscribers  = new Dictionary<string, List<int>>();
        private Dictionary<string, int> values = new Dictionary<string, int>();
        public DSM()
        {
            values.Add("foo1", 0);
            values.Add("foo2", 0);
            values.Add("foo3", 0);
            subscribers.Add("foo1", new List<int>());
            subscribers.Add("foo2", new List<int>());
            subscribers.Add("foo3", new List<int>());
        }

        public void SubscribeToVariable(string variable)
        {
            if (!ContainsVariable(variable))
                return;
            this.subscribers[variable].Add(Communicator.world.Rank);
            Message msg = new Message(new SubscribeMessage(Communicator.world.Rank, variable));
            this.SendAllMessage(msg);
        }

        public void SubscribeToVariable(string variable, int processRank)
        {
            if (!ContainsVariable(variable))
                return;
            this.subscribers[variable].Add(processRank);
        }


        public void SendShutDownMessage()
        {
            Message msg = new Message(new ExitMessage());
            SendAllMessage(msg);
        }

        public void SendAllMessage(Message message)
        {
            for (int procNumber = 0; procNumber < Communicator.world.Size; ++procNumber)
            { 
                if (procNumber == Communicator.world.Rank)
                    continue;
                Communicator.world.Send(message, procNumber, 0);
                
            }
        }

        public void ModifyVariableValue(string variable, int value)
        {
            if (!IsSubscribedToVariable(variable))
                return;
            this.values[variable] = value;
        }

        public bool ContainsVariable(string variable)
        {
            return subscribers.ContainsKey(variable);
        }

        public bool IsSubscribedToVariable(string variable)
        {
            if (!ContainsVariable(variable))
                return false;
            return subscribers[variable].Contains(Communicator.world.Rank);
        }

        public void updateVariable(string variable, int value)
        {
            if (!IsSubscribedToVariable(variable))
                return;
            values[variable] = value;
        }

        public void UpdateVariableInitiator(string variable, int value)
        {
            if (!IsSubscribedToVariable(variable))
                return;
            this.values[variable] = value;
            Message msg = new Message(new UpdateMessage(variable, value));
            this.SendToSubscribersOnly(msg, variable);
        }
        public void SendToSubscribersOnly(Message message, string variable)
        {
            for(int procIndex = 0; procIndex < subscribers[variable].Count; ++procIndex)
            {
                if (subscribers[variable][procIndex] == Communicator.world.Rank)
                    continue;
                Communicator.world.Send(message, subscribers[variable][procIndex], 0);
            }
        }

        public bool DoCompareAndExhange(string variable, int compValue, int newValue)
        {
            if (!IsSubscribedToVariable(variable))
                return false;
            if (values[variable] != compValue)
                return false;
            values[variable] = newValue;
            return true;
        }
        public bool DoCompareAndExchangeInitiator(string variable, int compValue, int newValue)
        {
            bool isOk;
            if (!IsSubscribedToVariable(variable))
                return false;
            if (values[variable] == compValue)
                values[variable] = newValue;
            isOk = values[variable] == compValue;
            Console.WriteLine("Initiator compare and echange status:" + isOk);
            Message msg = new Message(new ChangeMessage(variable, compValue, newValue));
            SendToSubscribersOnly(msg, variable);
            return true;
        }

    }

   
}
