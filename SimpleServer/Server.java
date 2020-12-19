import java.net.*;
import java.io.*;
import java.lang.Thread;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    int portNumber;
    int count;
    Map<Integer, Socket> clientsMap;
    Map<Integer, BufferedReader> clientsInputs;
    Map<Integer, PrintWriter> clientsOutputs;
    Queue<Task> tasks;
    final int tasksThreadsCount = 5;
    public Server(int portNumber) {
        this.portNumber = portNumber;
        count = 0;
        clientsMap = new ConcurrentHashMap<Integer, Socket>();
        clientsInputs = new ConcurrentHashMap<Integer, BufferedReader>();
        clientsOutputs = new ConcurrentHashMap<Integer, PrintWriter>();
        tasks = new ConcurrentLinkedQueue<Task>();
        List<Thread> threads = new ArrayList<Thread>();

        Thread clientsChecker = new Thread(new ClientsChecker(clientsInputs,
                                                              tasks));

        clientsChecker.start();
        List<Thread> tasksThreads = new ArrayList<Thread>();
        for (int i = 0; i < tasksThreadsCount; i++) {
            tasksThreads.add(new Thread(new RequestsProcesser(this,
                                                              clientsOutputs,
                                                              tasks)));
        }
        for (Thread t : tasksThreads) {
            t.start();
        }
        try (ServerSocket serverSocket =
             new ServerSocket(this.portNumber)) {
            while(true) {
                System.out.println("Waiting for clients connection..");
                Socket clientSocket = serverSocket.accept();
                count++;
                System.out.println("Total threads: " + count);
                clientsMap.put(count, clientSocket);
                try {
                    clientsInputs.put(count, new BufferedReader(
                                                                new InputStreamReader(clientSocket.getInputStream())));
                    clientsOutputs.put(count, new PrintWriter(
                                                              clientSocket.getOutputStream(), true));
                    
                    
                }
                catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public static void main(String[] args) throws IOException {
        
        if (args.length != 1) {
            System.err.println("Usage: java Server <port number>");
            System.exit(1);
        }
        int portNumber = Integer.parseInt(args[0]);
        Server s = new Server(portNumber);
    }

    public static String getResponse(String request) {
        try {
            String line1 = "";
            String line2 = "";
            char o = '+';
            String operations = "+-*/";
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
            System.out.println(e.getMessage());
            return "Error";
        }
    }

    static class ClientsChecker implements Runnable {
        Map<Integer, BufferedReader> clientsInputs;
        Queue<Task> tasks;
        public ClientsChecker(Map<Integer, BufferedReader> clientsInputs,
                              Queue<Task> tasks) {
            this.clientsInputs = clientsInputs;
            this.tasks = tasks;
        }
        public void run() {
            while(true) {
                for (Map.Entry<Integer, BufferedReader> c
                         : clientsInputs.entrySet()) {
                    try {
                        if (c.getValue().ready()) {
                            tasks.add(new Task(c.getKey(),
                                               c.getValue().readLine()));
                        }
                    }
                    catch(Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    static class RequestsProcesser implements Runnable {
        Server server;
        Map<Integer, PrintWriter> clientsOutputs;
        Queue<Task> tasks;
        public RequestsProcesser(Server server,
                                 Map<Integer, PrintWriter> clientsOutputs,
                                 Queue<Task> tasks) {
            this.server = server;
            this.clientsOutputs = clientsOutputs;
            this.tasks = tasks;
        }
        public void run() {
            while(true) {
                Task t = tasks.poll();
                if (t != null) {
                    try {
                        clientsOutputs.get(t.getId())
                            .println(server.getResponse(t.getRequest()));
                    }
                    catch(Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }        
    }
    
    static class CalcExecutor implements Runnable {
        private final Socket socket;
        public CalcExecutor(Socket socket) {
            this.socket = socket;
        }
        public void run() {
            try(PrintWriter out =
                new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));){
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    var startTime = System.currentTimeMillis();
                    String line1 = "";
                    String line2 = "";
                    char o = '+';
                    String operations = "+-*/";
                    for (int i = 0; i < inputLine.length(); i++) {
                        if (operations.indexOf(inputLine.charAt(i)) == -1) {
                            if (inputLine.charAt(i) != ' ')
                                line1 += inputLine.charAt(i);
                        }
                        else {
                            o = inputLine.charAt(i);
                            for (int j  = i+1; j < inputLine.length(); j++) {
                                if (inputLine.charAt(j) != ' ')
                                    line2 += inputLine.charAt(j);
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
                    out.println(Integer.toString(res));
                    System.out.println("Processing time: " +
                                       (System.currentTimeMillis() - startTime));
                }
            }
            catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port "
                                   + socket.getPort() + " or listening for a connection");
                System.out.println(e.getMessage());
            }
            finally {
            }
        }
    }
    static class Task {
        Integer id;
        String request;
        public Task(Integer id, String request) {
            this.id = id;
            this.request = request;
        }
        public Integer getId() {
            return id;
        }
        public String getRequest() {
            return request;
        }
    }
}

