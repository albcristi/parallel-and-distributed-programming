using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Threading;
using System.Net;
using System.Net.Sockets;

namespace Lab4
{
    class MyHttpRequest
    {
        // unique identificator for the request
        public int identificator;

        // name of the host
        public string host;

        // corresponding endpoint for the request
        public string endpoint;

        // client socket
        public Socket socket;

        // where we will put the content
        public StringBuilder content = new StringBuilder();

        // size for the buffer where we
        // will have bytes
        public static int bufferSize = 512;

        // buffer where we will receive data
        // from the request
        public byte[] buffer = new byte[bufferSize];

        // network endpoint as an IP address and a port number.
        public IPEndPoint ipEndpoint;

        // threading mechanism
        public ManualResetEvent isConnected = new ManualResetEvent(false);
        public ManualResetEvent isSent = new ManualResetEvent(false);
        public ManualResetEvent isReceived = new ManualResetEvent(false);

        public MyHttpRequest(string host,
                             string endpoint,
                             Socket socket,
                             IPEndPoint ipEndpoint,
                             int identificator)
        {
            this.host = host;
            this.endpoint = endpoint;
            this.socket = socket;
            this.ipEndpoint = ipEndpoint;
            this.identificator = identificator;
        }
    }
}
