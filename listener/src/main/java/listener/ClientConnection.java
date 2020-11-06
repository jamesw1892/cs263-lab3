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
    private Cracker cracker;

    public PrintWriter getOut() {
        return this.out;
    }

    public ClientConnection(Socket socket, ServerConnection serverConnection, Cracker cracker) {
        this.socket = socket;
        this.serverConnection = serverConnection;
        this.cracker = cracker;
    }

    public void run() {
        try {
            // once connected, create readers and writers for the client
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            this.serverConnection.setOutToClient(this.out);

            int msgNum = 0;

            // read messages and try to the pass them on to the actual server
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                msgNum++;
                // output messages from client
                System.out.println("[client->server]: " + inputLine);

                if (msgNum == 1) {
                    this.cracker.setGC(inputLine);
                } else {
                    this.cracker.addEncryptedMessage(inputLine);
                }

                // pass them to the server
                this.serverConnection.getOutToServer().println(inputLine);
            }

            this.cracker.decryptWhenReady(true);
        }
        catch (IOException e) {
            System.out.println("Something has gone wrong with the server connection!");
            System.out.println(e.getMessage());
        }
    }
}
