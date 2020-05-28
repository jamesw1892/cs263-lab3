package listener;

import java.net.*;
import java.io.*;
import java.math.BigInteger;

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

            // our secret: this value is arbitrary 
            int mySecret = 1; 

            // choose two prime numbers (arbitrary) and send them 
            // to the client, although normally they should be
            // large and randomly chosen 
            BigInteger p = BigInteger.valueOf(Primes.PRIMES[0]);
            BigInteger g = BigInteger.valueOf(Primes.PRIMES[1]);

            out.println(p);
            out.println(g);

            // retrieve a message from the client
            BigInteger rmsg = new BigInteger(in.readLine());

            // derive the key from the client's message and our
            // secret
            BigInteger powed_k = rmsg.pow(mySecret);
            BigInteger k = powed_k.mod(p);

            // derive a message to send to the client from our
            // secret at the primary number; then send it to the client 
            BigInteger powed = g.pow(mySecret);
            BigInteger msg = powed.mod(p);
            out.println(msg);

            // read messages and try to the pass them on to the actual server
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("listener [from client]: " + inputLine);
                this.serverConnection.getOutToServer().println(inputLine);
            }
        }
        catch (IOException e) {
            System.out.println("Something has gone wrong with the server connection!");
            System.out.println(e.getMessage());
        }
    }
}
