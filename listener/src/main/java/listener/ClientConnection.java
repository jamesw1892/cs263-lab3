package listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnection implements Runnable {
    private Socket socket;
    private ServerConnection serverConnection;
    private PrintWriter out;
    private BufferedReader in;

    public PrintWriter getOut() {
        return this.out;
    }

    public ClientConnection(Socket socket, ServerConnection serverConnection) {
        this.socket = socket;
        this.serverConnection = serverConnection;
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
                System.out.println("[client->server]: " + inputLine);
                this.serverConnection.getOutToServer().println(inputLine);
            }
        }
        catch (IOException e) {
            System.out.println("Something has gone wrong with the server connection!");
            System.out.println(e.getMessage());
        }
    }
}
