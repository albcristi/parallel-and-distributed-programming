using System;
using System.Collections.Generic;
using MPI;

namespace ex10
{
    [Serializable]
    class Node
    {
        public int color = 1;
        public int node = 1;
        public List<Node> neighbours = new List<Node>();
    }
    class Program
    {
        public static int noColors = 1;

        public static void Worker()
        {
            int startNode = Communicator.world.Receive<int>(0, 0);
            List<Node> nodes = Communicator.world.Receive<List<Node>>(0, 1);
            int maxColors = Communicator.world.Receive<int>(0, 2);
            for (int i = startNode; i < startNode+nodes.Count/Communicator.world.Size; i++)
            {
                List<Node> partial = new List<Node>();
                partial.AddRange(nodes);
                partial = DoColoring(partial, i, maxColors);
                if (partial != null && noColors <= maxColors)
                    for (int idx = 0; idx < nodes.Count; idx++)
                    {
                        if (partial[idx].color != 1)
                            nodes[idx].color = partial[idx].color;
                    }
            }
            Communicator.world.Send<int>(noColors, 0, 0);
            if (maxColors < noColors)
                Communicator.world.Send(new List<Node>(), 0, 1);
            else
                Communicator.world.Send(nodes, 0, 1);
        }

        public static List<Node> Master(List<Node> nodes, int maxColors)
        {
            int noPerProcess = nodes.Count / Communicator.world.Size;
            int startNode = 0;
            for(int i=1; i<Communicator.world.Size; i++)
            {
                Communicator.world.Send(startNode, i, 0);
                Communicator.world.Send(nodes, i, 1);
                Communicator.world.Send(maxColors, i, 2);
                startNode += noPerProcess;
            }

            for(int i=startNode; i<nodes.Count; i++)
            {
                List<Node> partial = new List<Node>();
                partial.AddRange(nodes);
                partial = DoColoring(partial, i, maxColors);
                if(partial != null)
                    for(int idx = 0; idx<nodes.Count; idx++)
                    {
                        if (partial[idx].color != 1)
                            nodes[idx].color = partial[idx].color;
                    }
            }

            for(int i=1; i<Communicator.world.Size; i++)
            {
                int maxLocal = Communicator.world.Receive<int>(i, 0);
                if (maxLocal > noColors)
                    noColors = maxLocal;
                List<Node> partialCollored = Communicator.world.Receive<List<Node>>(i, 1);
                if (noColors <= maxColors)
                    for (int index = 0; index < partialCollored.Count; index++)
                        if (partialCollored[index].color != 1)
                            nodes[index].color = partialCollored[index].color;
            }
            Console.WriteLine(noColors);
            if (noColors > maxColors)
                return null;
            return nodes;
        }

        public static List<Node> DoColoring(List<Node> nodes, int startNode, int maxAccepted)
        {
            List<bool> visited = new List<bool>();
            for (int node = 0; node < nodes.Count; node++)
                visited.Add(false);
            visited[startNode] = true;
            Queue<Node> queue = new Queue<Node>();
            queue.Enqueue(nodes[startNode]);
            while (queue.Count > 0)
            {
                Node top = queue.Dequeue();
                foreach(Node neighbour in top.neighbours)
                {
                    if(neighbour.color == top.color)
                    {
                        neighbour.color += 1;
                    }
                    noColors = Math.Max(noColors, Math.Max(neighbour.color, top.color));
                    if (noColors > maxAccepted) // IN CASE WE ARE NOT ALLOWED TO EXCEED
                        return null;
                    if (!visited[neighbour.node])
                    {
                        visited[neighbour.node] = true;
                        queue.Enqueue(neighbour);
                    }
                }
            }
            return nodes;
        }

        static void Main(string[] args)
        {
            /*
             * k Coloring.
             * we have k colors and n nodes with some relations(we will have an edge between 2 nodes that
             * can not have the same color.
             * If enough colors ==> also builds the colored graph
             */

            MPI.Environment.Run(ref args, communicator =>
            {
                if (Communicator.world.Rank == 0)
                {
                    int n = 4;
                    int maxColors = 2;
                    List<Node> nodes = new List<Node>();
                    for(int i=0; i<n; i++)
                    {
                        Node node = new Node();
                        node.node = i;
                        nodes.Add(node);
                    }
                    nodes[0].neighbours = new List<Node>() { nodes[2], nodes[3] };
                    nodes[2].neighbours = new List<Node>() { nodes[0], nodes[3] };
                    nodes[3].neighbours = new List<Node>() { nodes[0], nodes[2] };
                    List<Node> res = Master(nodes, maxColors);
                    if (noColors <= maxColors)
                        Console.WriteLine(noColors);
                    else
                        Console.WriteLine("Not enough colors");
                    if (res != null) {
                        foreach (Node node in res)
                        {
                            Console.WriteLine("NODE: " + node.node+" with color:"+node.color);
                        }
                    }
                }
                else
                {
                    Worker();
                }
            });
        }
    }
}
