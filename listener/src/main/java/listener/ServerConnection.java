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
    private Cracker cracker;

    public PrintWriter getOutToServer() {
        return this.outToServer;
    }

    public void setOutToClient(PrintWriter writer) {
        this.outToClient = writer;
    }

    public ServerConnection(Socket socket, Cracker cracker) {
        this.cracker = cracker;
        this.socket = socket;
    }

    public void run() {
        try {
            this.outToServer = new PrintWriter(this.socket.getOutputStream(), true);
            this.inFromServer = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            String inputLine;
            int msgNum = 0;
            while ((inputLine = inFromServer.readLine()) != null) {
                msgNum++;
                // output messages from server
                System.out.println("[server->client]: " + inputLine);
                switch (msgNum) {
                    case 1:
                        this.cracker.setN(inputLine);
                        break;
                    case 2:
                        this.cracker.setG(inputLine);
                        break;
                    case 3:
                        this.cracker.setGS(inputLine);

                        // by this point, we have all the information
                        // needed to crack DHM
                        this.cracker.crackDHM();
                        break;
                    default:
                        this.cracker.addEncryptedMessage(inputLine);
                }

                // pass them to the client
                if (this.outToClient != null) {
                    this.outToClient.println(inputLine);
                }

                // server seems to hang so break after last operation
                if (msgNum == 9) {
                    break;
                }
            }

            this.cracker.decryptWhenReady(false);
        }
        catch (IOException e) {
            System.out.println("Something has gone wrong with the server connection!");
            System.out.println(e.getMessage());
        }
    }
}
