import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private ArrayList<connect> clients;
    private ServerSocket server;
    private boolean running = true;
    private ExecutorService pool;

    public Server() {
        clients = new ArrayList<>();
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9000);
            pool = Executors.newCachedThreadPool();
            while (running) {
                Socket client = server.accept();
                connect c = new connect(client);
                clients.add(c);
                pool.execute(c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class connect implements Runnable {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String name;

        public connect(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(client.getOutputStream(), true);
                out.println("Enter your name:");
                name = in.readLine();
                if (name == null) {
                    System.out.println("invalid name you will be dc");
                    shutdown();
                }
                out.println("Welcome, " + name + "!");
                broadcast(name + " joined the chat");
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("nick")) {
                        String[] data = message.split(" ", 2);
                        if (data.length == 2) {
                            String new_name = data[1];
                            broadcast(name + " changed name to " + new_name);
                            name = new_name;
                            out.println("You are now known as " + new_name);
                        }
                    } else if (message.startsWith("quit")) {
                        shutdown();
                        return;
                    } else {
                        broadcast(name + ": " + message);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void shutdown() {
            try {
                running = false;
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
                synchronized (clients) {
                    clients.remove(this);
                    broadcast(name + " has left the chat");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void send(String message) {
            out.println(message);
        }

        public void broadcast(String message) {
            System.out.println(message);

            for (connect c : clients) {
                if (c != null) {
                    c.send(message);
                }

            }
        }
    }
}
