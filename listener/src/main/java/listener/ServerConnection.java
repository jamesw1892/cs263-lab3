package listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;

public class ServerConnection implements Runnable {
    private Socket socket;
    private PrintWriter outToServer;
    private BufferedReader inFromServer;
    private PrintWriter outToClient;
    private PrintWriter logger;

    public PrintWriter getOutToServer() {
        return this.outToServer;
    }

    public void setOutToClient(PrintWriter writer) {
        this.outToClient = writer;
    }

    public ServerConnection(Socket socket, PrintWriter logger) {
        this.logger = logger;
        this.socket = socket;
    }

    public void run() {
        try {
            this.outToServer = new PrintWriter(this.socket.getOutputStream(), true);
            this.inFromServer = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            String inputLine;
            while ((inputLine = inFromServer.readLine()) != null) {

                // output messages from server to a file too
                String msg = Instant.now() + " [server->client]: " + inputLine;
                System.out.println(msg);
                logger.println(msg);
                logger.flush();

                // pass them to the client
                if (this.outToClient != null) {
                    this.outToClient.println(inputLine);
                }
            }
        }
        catch (IOException e) {
            System.out.println("Something has gone wrong with the server connection!");
            System.out.println(e.getMessage());
        }
    }
}
