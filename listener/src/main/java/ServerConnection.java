package listener;

import java.net.*;
import java.io.*;
import java.math.BigInteger;

public class ServerConnection implements Runnable {
    private Socket socket;
    private PrintWriter outToServer;
    private BufferedReader inFromServer;
    private PrintWriter outToClient;

    public PrintWriter getOutToServer() {
        return this.outToServer;
    }

    public void setOutToClient(PrintWriter writer) {
        this.outToClient = writer;
    }

    public ServerConnection(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            this.outToServer =
                new PrintWriter(this.socket.getOutputStream(), true);
            this.inFromServer =
                new BufferedReader(
                    new InputStreamReader(this.socket.getInputStream()));

            // retrieve the two prime numbers from the server
            // we use BigIntegers since the calculations we perform
            // later may exceed 
            BigInteger p = new BigInteger(inFromServer.readLine());
            BigInteger g = new BigInteger(inFromServer.readLine());

            // our secret: this value is arbitrary 
            int mySecret = 1; 

            // calculate the message to send
            BigInteger powed = g.pow(mySecret);
            BigInteger msg = powed.mod(p);
    
            // send the secret to the server
            outToServer.println(msg);

            // read the server's message and compute the key from it
            // k is the shared key
            BigInteger rmsg = new BigInteger(inFromServer.readLine());
            BigInteger powed_k = rmsg.pow(mySecret);
            BigInteger k = powed_k.mod(p); 

            String inputLine;
            while ((inputLine = inFromServer.readLine()) != null) {
                System.out.println("listener [from server]: " + inputLine);
                if(this.outToClient != null) {
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
