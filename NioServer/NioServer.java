import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;


public class NioServer {
    int portNumber;
    public NioServer(int portNumber) {
        try (Selector selector = Selector.open();
             ServerSocketChannel socket = ServerSocketChannel.open()){
            this.portNumber = portNumber;
            InetSocketAddress addr = new InetSocketAddress("localhost", portNumber);
            socket.bind(addr);
            socket.configureBlocking(false);

            int ops = socket.validOps();
            SelectionKey selectionKey = socket.register(selector, ops, null);

            ByteBuffer buffer = ByteBuffer.allocate(256);

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    try {
                        SelectionKey key = iter.next();

                        if (key.isAcceptable()) {
                            SocketChannel client = socket.accept();
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ);
                        }

                        if (key.isReadable()) {
                            try {
                                SocketChannel client = (SocketChannel) key.channel();
                                client.read(buffer);
                                buffer.flip();
                                String request = new String(buffer.array()).trim();
                                buffer.clear();
                                String response = getResponse(request);
                                ByteBuffer out = ByteBuffer.allocate(256);
                                out.put(response.getBytes());
                                out.flip();
                                client.write(out);
                                out.clear();
                            }
                            catch (IOException e) {
                                key.cancel();
                            }
                        }
                    }
                    catch(Exception e) {
                        System.out.println(e);
                    }
                    finally {
                        iter.remove();
                    }
                }
            }
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) throws IOException {
        
        if (args.length != 1) {
            System.err.println("Usage: java NioServer <port number>");
            System.exit(1);
        }
        int portNumber = Integer.parseInt(args[0]);
        NioServer s = new NioServer(portNumber);
    }

    public static String getResponse(String request) {
        try {
            request = request.trim();
            String line1 = "";
            String line2 = "";
            char o = '+';
            String operations = "+-/*";
            for (int i = 0; i < request.length(); i++) {
                if (operations.indexOf(request.charAt(i)) == -1) {
                    if (request.charAt(i) != ' ')
                        line1 += request.charAt(i);
                }
                else {
                    o = request.charAt(i);
                    for (int j  = i+1; j < request.length(); j++) {
                        if (request.charAt(j) != ' ')
                            line2 += request.charAt(j);
                    }
                    break;
                }
            }
            int a = Integer.parseInt(line1);
            int b = Integer.parseInt(line2);
            int res = 0;
            if (o == '+') res = a + b;
            else if (o == '-') res = a-b;
            else if (o == '*') res = a*b;
            else if (o == '/') res = a/b;
            return Integer.toString(res);
        }
        catch (Exception e) {
            System.out.println(e);
            return "Error";
        }
    }
}

