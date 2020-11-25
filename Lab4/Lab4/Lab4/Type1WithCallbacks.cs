using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

namespace Lab4
{
    class Type1WithCallbacks
    {
        private List<string> hosts;

        public Type1WithCallbacks(List<string> hosts)
        {
            this.hosts = hosts;
        }

        public void StartExecution()
        {
            for (int i = 0; i < hosts.Count; i++) {
                this.FetchData(i + 1, hosts[i]);
            }
        }

        private void FetchData(int identificator, string host)
        {
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
            if(host.Split('/').Length > 1)
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
            // we innitiate the connection via the socket
            request.socket.BeginConnect(request.ipEndpoint,
                                        BeginConnectCallbackMethod,
                                        request);
            
        }

        private void BeginConnectCallbackMethod(IAsyncResult result)
        {
            // we get the request that has been passed as state
            // and cast it back to its original class
            MyHttpRequest request = (MyHttpRequest) result.AsyncState;

            // we check to see if the received request is correct or not
            if (request == null)
                return;
            // print message to inform user about the status of the
            // operations
            Console.WriteLine("Identificator " + request.identificator +
                ": Socket Connection for host=" + request.host);

            HttpParser httpParser = new HttpParser();
            byte[] data = Encoding.ASCII
                .GetBytes(httpParser.CreateGetRequestString(request.host, request.endpoint));

            // we now move to the BeginSent
            request.socket.BeginSend(data,
                                        0,
                                        data.Length,
                                        0,
                                        BeginSendCallbackMethod,
                                        request);
        }

        private void BeginSendCallbackMethod(IAsyncResult result)
        {
            // we get the request that has been passed as state
            // and cast it back to its original class
            MyHttpRequest request = (MyHttpRequest)result.AsyncState;
            if (request == null)
                return;
            int sentBytes = request.socket.EndSend(result);

            // print message to inform user about the status of the
            // operations
            Console.WriteLine("Identificator " + request.identificator +
                ": Socket Connection for host=" + request.host+
                " has sent "+sentBytes+" bytes");
            // move to the next step
            request.socket.BeginReceive(
                request.buffer,
                0,
                MyHttpRequest.bufferSize,
                0,
                BeginReceiveCallbackMethod,
                request);
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

                    if(body.Length < contentLength)
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
                        Console.WriteLine("Identificator " + request.identificator +
                              ": Socket Connection for host=" + request.host +
                              " received response!!!");
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
                }
            }
            catch(Exception exception)
            {
                Console.WriteLine(exception.ToString());
            }
        }
    }
}
