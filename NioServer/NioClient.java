import java.io.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;

public class NioClient {
    public static void main(String args[]) {
        try {
            if (args.length != 2) {
                System.err.println(
                                   "Usage: java NioClient <host name> <port number>");
                System.exit(1);
            }
 
            String hostName = args[0];
            int portNumber = Integer.parseInt(args[1]);
            InetSocketAddress addr = new InetSocketAddress(hostName, portNumber);
            SocketChannel socket = SocketChannel.open(addr);

            // BufferedReader in =
            //     new BufferedReader
            //     (new InputStreamReader(Channels.newInputStream(socket)));
            // PrintWriter out =
            //     new PrintWriter(Channels.newOutputStream(socket));

            BufferedReader stdIn =
                new BufferedReader(new InputStreamReader(System.in));
            String input;
            while((input = stdIn.readLine()) != null) {
                var time = System.currentTimeMillis();
                ByteBuffer buffer = ByteBuffer.wrap(input.getBytes());
                socket.write(buffer);
                buffer.clear();
                // out.println(input);
                ByteBuffer out = ByteBuffer.allocate(256);
                socket.read(out);
                String response = new String(out.array()).trim();
                System.out.println("Response: " + response);
                System.out.println("Time: " +
                                   (System.currentTimeMillis() - time));
            }
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }
}
                // try (
                //      Socket echoSocket = new Socket(hostName, portNumber);
                //      PrintWriter out =
                //      new PrintWriter(echoSocket.getOutputStream(), true);
                //      BufferedReader in =
                //      new BufferedReader(new
                //                         InputStreamReader(echoSocket.getInputStream()));
                //      BufferedReader stdIn =
                //      new BufferedReader(
                //                         new InputStreamReader(System.in))
                //      ) {
                //     String userInput;
                //     while ((userInput = stdIn.readLine()) != null) {
                //         var time = System.currentTimeMillis();
                //         out.println(userInput);
                //         System.out.println("Response: " + in.readLine());
                //         System.out.println("Time: " + (System.currentTimeMillis() - time));
                //     }
                // } catch (UnknownHostException e) {
                //     System.err.println("Don't know about host " + hostName);
                //     System.exit(1);
                // } catch (IOException e) {
                //     System.err.println("Couldn't get I/O for the connection to " +
                //                        hostName);
                //     System.exit(1);
                // } 
            // }
        // }
    // }
// }
