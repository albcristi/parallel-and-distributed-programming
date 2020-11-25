using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Lab4
{
    class HttpParser
    {
        /*
         * mple parser for the HTTP protocol:
         *  -  get the header lines 
         *  -  content-lenght(header line).
         */
        public const Int32 httpPortNumber = 80;

        public HttpParser()
        {

        }

        public string GetResponseBody(string response)
        {
            var body = response.Split(new[] {"\r\n\r\n"}, StringSplitOptions.RemoveEmptyEntries);
            if (body.Length <=1)
                return "";
            return body[1];

        }

        public int GetContentLenght(string response)
        {
            /*
             * This method gets the response from a get request
             * and returns the value corresponding to the
             * content-length field from a Http GET request
             */
            var lines = response.Split('\r', '\n');
            foreach (var line in lines)
            {
                string[] splittedLine = line.Split(':');
                if (splittedLine.Length >= 2
                    && string.Compare("Content-Length", splittedLine[0], StringComparison.Ordinal) == 0)
                {
                    return int.Parse(splittedLine[1]);
              
                }
            }
            return 0;
        }

        public string CreateGetRequestString(string host, string endpoint)
        {
            /*
             * This method creates the string corresponding to a HTTP GET request
             * for an endpoint from a given host (method parameters)
             */
            return "GET " + endpoint + " HTTP/1.1\r\n" +
                   "Host: " + host + "\r\n" +
                   "User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36\r\n" +
                   "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,#1#*;q=0.8\r\n" +
                   "Accept-Language: en-US,en;q=0.9,ro;q=0.8\r\n" +
                   "Accept-Encoding: gzip, deflate\r\n" +
                   "Connection: keep-alive\r\n" +
                   "Upgrade-Insecure-Requests: 1\r\n" +
                   "Pragma: no-cache\r\n" +
                   "Cache-Control: no-cache\r\n" +
                   "Content-Length: 0\r\n\r\n";
        }

        public bool ContainsHeader(string content)
        {
            return content.Contains("\r\n\r\n");
        }

    }
}
