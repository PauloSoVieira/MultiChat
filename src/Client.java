import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean running = true;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    @Override
    public void run() {

        try {
            client = new Socket("localhost", 9000);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            //Threat experience
            Input input = new Input();
            Thread thread = new Thread(input);
            thread.start();


            String text;
            while ((text = in.readLine()) != null) {
                System.out.println(text);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //User left chat

    public void shutdown() {
        running = false;
        try {
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // Input fix
    //TODO put quit , nick .... more

    class Input implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                while (running) {
                    String message = inputReader.readLine();
                    if (message.equals("quit")) {
                        out.println("quit");
                        inputReader.close();
                        shutdown();
                    } else {
                        out.println(message);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
