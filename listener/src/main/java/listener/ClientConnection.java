package listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;

public class ClientConnection implements Runnable {
    private Socket socket;
    private ServerConnection serverConnection;
    private PrintWriter out;
    private BufferedReader in;
    private PrintWriter logger;

    public PrintWriter getOut() {
        return this.out;
    }

    public ClientConnection(Socket socket, ServerConnection serverConnection, PrintWriter logger) {
        this.socket = socket;
        this.serverConnection = serverConnection;
        this.logger = logger;
    }

    public void run() {
        try {
            // once connected, create readers and writers for the client
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            this.serverConnection.setOutToClient(this.out);

            // read messages and try to the pass them on to the actual server
            String inputLine;
            while ((inputLine = in.readLine()) != null) {

                // output messages from client to a file too
                String msg = Instant.now() + " [client->server]: " + inputLine;
                System.out.println(msg);
                logger.println(msg);
                logger.flush();

                // pass them to the server
                this.serverConnection.getOutToServer().println(inputLine);
            }
        }
        catch (IOException e) {
            System.out.println("Something has gone wrong with the server connection!");
            System.out.println(e.getMessage());
        }
    }
}
