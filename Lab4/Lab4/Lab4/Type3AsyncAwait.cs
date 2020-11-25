using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace Lab4
{
    class Type3AsyncAwait
    {
        private List<string> hosts;
        private List<Task> tasks;

        public Type3AsyncAwait(List<string> hosts)
        {
            this.hosts = hosts;
            this.tasks = new List<Task>();
        }

        public void StartExecution()
        {
            for (int i = 0; i < hosts.Count; i++)
            {
                tasks.Add(Task.Factory
                    .StartNew(FetchData, (i + 1, hosts[i])));
            }
            Task.WaitAll(tasks.ToArray());
        }

        public async void FetchData(object arguments)
        {
            // unwrap the contet from arguments
            // namely, like in taks1, we will
            // pass to FetchData host and an
            // identifier
            int identificator = ((ValueTuple<int, string>)arguments).Item1;
            string host = ((ValueTuple<int, string>)arguments).Item2;

            // retrieve the IP address of the host, based on the DNS
            IPAddress hostIP = Dns.GetHostEntry(host.Split('/')[0]).AddressList[0];


            // prepare endpoint
            IPEndPoint ipEndpoint = new IPEndPoint(hostIP, HttpParser.httpPortNumber);
            // create the client socker for the TCP/IP protocol
            Socket client = new Socket(hostIP.AddressFamily,
                                       SocketType.Stream,
                                       ProtocolType.Tcp);
            // prepare the endpoint from host and the host 
            string endpoint = "/";
            if (host.Split('/').Length > 1)
            {
                endpoint = host.Substring(host.IndexOf("/", StringComparison.Ordinal));
            }
            host = host.Split('/')[0];
            // we create an instance of MyHttpRequest
            MyHttpRequest request = new MyHttpRequest(host,
                                                      endpoint,
                                                      client,
                                                      ipEndpoint,
                                                      identificator);
            await this.SocketBeginConnect(request);
            await this.SocketBeginSend(request);
            await this.SocketBeginReceive(request);
            string[] content = request.content.ToString().Split('\r', '\n');
            string cont = "";
            foreach (string line in content)
            {
                cont += line + "\n";
            }
            Console.WriteLine("Identificator: " + request.identificator + "\n" + cont);
            request.socket.Shutdown(SocketShutdown.Both);
            request.socket.Close();
        }

        private async Task SocketBeginConnect(MyHttpRequest request)
        {
            request.socket.BeginConnect(request.ipEndpoint,
                                        BeginConnectCallbackMethod,
                                        request);
            await Task.FromResult<object>(request.isConnected.WaitOne());
        }

        private void BeginConnectCallbackMethod(IAsyncResult result)
        {
            MyHttpRequest request = (MyHttpRequest)result.AsyncState;

            try
            {
                Console.WriteLine("Identifier: " + request.identificator +
                    " socket made connection to " + request.host);
                request.socket.EndConnect(result);
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }
            request.isConnected.Set();
        }

        private async Task SocketBeginSend(MyHttpRequest request)
        {
            HttpParser httpParser = new HttpParser();
            byte[] data = Encoding.ASCII.GetBytes(httpParser.CreateGetRequestString(request.host, request.endpoint));
            request.socket.BeginSend(data,
                                     0,
                                     data.Length,
                                     0,
                                     BeginSendCallbackMethod,
                                     request);
            await Task.FromResult<object>(request.isSent.WaitOne());
        }

        private void BeginSendCallbackMethod(IAsyncResult result)
        {

            MyHttpRequest request = (MyHttpRequest)result.AsyncState;
            try
            {
                int noBytes = request.socket.EndSend(result);
                Console.WriteLine("Identificator: " + request.identificator +
                    "\nSocket sent " + noBytes + " bytes to server");
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }
            request.isSent.Set();
        }

        private async Task SocketBeginReceive(MyHttpRequest request)
        {
            request.socket.BeginReceive(request.buffer,
                                        0,
                                        MyHttpRequest.bufferSize,
                                        0,
                                        BeginReceiveCallbackMethod,
                                        request);
            await Task.FromResult<object>(request.isReceived.WaitOne());
        }

        private void BeginReceiveCallbackMethod(IAsyncResult result)
        {
            // we get the request that has been passed as state
            // and cast it back to its original class
            MyHttpRequest request = (MyHttpRequest)result.AsyncState;
            if (request == null)
                return;
            try
            {
                int noBytes = request.socket.EndReceive(result);
                HttpParser httpParser = new HttpParser();
                request.content.Append(Encoding.ASCII.GetString(request.buffer, 0, noBytes));

                if (!httpParser.ContainsHeader(request.content.ToString()))
                {
                    request.socket.BeginReceive(request.buffer,
                                                0,
                                                MyHttpRequest.bufferSize,
                                                0,
                                                BeginReceiveCallbackMethod,
                                                request);
                }
                else
                {
                    string body = httpParser.GetResponseBody(request.content.ToString());
                    int contentLength = httpParser.GetContentLenght(request.content.ToString());

                    if (body.Length < contentLength)
                    {
                        request.socket.BeginReceive(request.buffer,
                                                0,
                                                MyHttpRequest.bufferSize,
                                                0,
                                                BeginReceiveCallbackMethod,
                                                request);
                    }
                    else
                    {

                        request.isReceived.Set();
                    }
                }
            }
            catch (Exception exception)
            {
                Console.WriteLine(exception.ToString());
            }

        }
    }
}
