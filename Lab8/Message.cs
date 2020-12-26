using MPI;
using System;
using System.Collections.Generic;
using System.Text;

namespace Lab8
{
    [Serializable]
    class Message
    {
        public SubscribeMessage subscribe = null;
        public UpdateMessage update = null;
        public ChangeMessage change = null;
        public ExitMessage exitMessage = null;
        public int initiator = Communicator.world.Rank;

        public Message(SubscribeMessage subscribe)
        {
            this.subscribe = subscribe;
        }

        public Message(UpdateMessage update)
        {
            this.update = update;
        }

        public Message(ChangeMessage change)
        {
            this.change = change;
        }

        public Message(ExitMessage exitMessage)
        {
            this.exitMessage = exitMessage;
        }
    }

    [Serializable]
    class SubscribeMessage
    {
        public int processRank;
        public string variable;

        public SubscribeMessage(int processRank, string variable)
        {
            this.processRank = processRank;
            this.variable = variable;
        }
    }

    [Serializable]
    class UpdateMessage
    {
        public string variable;
        public int newValue;

        public UpdateMessage(string variable, int newValue)
        {
            this.variable = variable;
            this.newValue = newValue;
        }
    }

    [Serializable]
    class ChangeMessage
    {
        public string variable;
        public int oldValue;
        public int newValue;

        public ChangeMessage(string variable, int oldValue, int newValue)
        {
            this.variable = variable;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }

    [Serializable]
    class ExitMessage
    {
        public ExitMessage()
        {

        }
    }
}
