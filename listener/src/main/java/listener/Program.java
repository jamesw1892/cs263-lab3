package listener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

public class Program {
    // the hostname of the machine the server runs on
    private static String serverHost = "localhost";
    // the port we should listen on
    private static String portForClients = "0";
    // the port of the server we should connect to
    private static String portOfActualServer = null;

    // whether to crack the existing connection rather than
    // blocking the client and letting the user input
    // messages to the server directly using username and
    // password found by cracking it
    private static boolean toCrack = false;

    /**
     * Tries to load the server's port number from a file.
     * @return Returns the port as a String or null if it could not be loaded.
     */
    private static String loadServerPort() {
        try {
            String home = System.getProperty("user.home");
            File file = Paths.get(home, ".cs263-lab3-server").toFile();
            
            // the file should exist 
            if(!file.exists()) {
                return null;
            }

            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            String result = br.readLine();
            br.close();
            reader.close();

            return result;
        }
        catch(IOException ex) {
            System.err.println("WARNING: Unable to load the server port from ~/.cs263-lab3-server, the server may not be running");
            return null;
        }
    }

    /**
     * Tries to determine the server's port and the port we should listen on
     * by inspecting environment variables, command-line arguments, and files. 
     * @param args
     * @return
     */
    private static void getConfig(String[] args) {
        // is the hostname of the server specified in an environment variable?
        String env = System.getenv("SERVER_HOST");
        if(env != null) {
            serverHost = env;
        }

        // is the server's port specified in an environment variable?
        String serverPort = System.getenv("SERVER_PORT");
        if(serverPort != null) {
            portOfActualServer = serverPort;
        }

        // is the listener's port specified in an environment variable?
        String listenerPort = System.getenv("LISTENER_PORT");
        if(listenerPort != null) {
            portForClients = listenerPort;
        }

        // try to parse command-line parameters, these may override 
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--server-port")) {
                if(i+1 < args.length) {
                    portOfActualServer = args[i+1];
                }
                else {
                    System.err.println("--server-port has no value");
                }
            } else if(args[i].equals("--server-host")) {
                if(i+1 < args.length) {
                    serverHost = args[i+1];
                }
                else {
                    System.err.println("--server-host has no value");
                }
            } else if(args[i].equals("--listener-port")) {
                if(i+1 < args.length) {
                    portForClients = args[i+1];
                }
                else {
                    System.err.println("--listener-port has no value");
                }
            } else if (args[i].equals("--crack")) {
                toCrack = true;
            }
        }

        // if we still do not have the server's port, let's try to load it
        // from a file
        if (portOfActualServer == null) {
            portOfActualServer = loadServerPort();
        }
    }

    /**
     * Stores the port number of the listener in a file so that the other process
     * knows which port to connect to.
     * @param port The port number to store.
     */
    private static void savePort(int port) {
        try {
            String home = System.getProperty("user.home");
            File file = Paths.get(home, ".cs263-lab3-listener").toFile();
            
            // the file should not exist 
            if(file.exists()) {
                System.err.println("WARNING: ~/.cs263-lab3-listener already exists, the listener may not have been shut down properly");

                file.delete();
            }

            // write the port number to the file
            file.createNewFile();

            FileWriter writer = new FileWriter(file);
            writer.write(Integer.toString(port));
            writer.close();

            // delete the file on exit
            file.deleteOnExit();
        } catch (IOException e) {
            System.err.println("Unable to store port:");
            System.err.println(e.toString());
        }
    }

    private static void crack() throws IOException {

        // create cracker
        Cracker cracker = new Cracker();

        // connect to the actual server
        System.out.printf("Student-in-the-middle is connecting to server at %s:%s...\n", serverHost, portOfActualServer);
        Socket serverSocket = new Socket(serverHost, Integer.parseInt(portOfActualServer));
        ServerConnection serverConnection = new ServerConnection(serverSocket, cracker);
        Thread serverThread = new Thread(serverConnection);

        // listen for incoming connections and accept the first
        ServerSocket socketForClients = new ServerSocket(Integer.parseInt(portForClients));
        System.out.printf("Student-in-the-middle is listening on port %d...\n", socketForClients.getLocalPort());
        savePort(socketForClients.getLocalPort());

        Socket clientSocket = socketForClients.accept();
        ClientConnection clientConnection = new ClientConnection(clientSocket, serverConnection, cracker);
        Thread clientThread = new Thread(clientConnection);
        clientThread.start();
        serverThread.start();
    }

    private static void runUserClient() throws IOException {

        System.out.printf("Student-in-the-middle is connecting to server at %s:%s...\n", serverHost, portOfActualServer);
        Socket serverSocket = new Socket(serverHost, Integer.parseInt(portOfActualServer));
        UserClient userClient = new UserClient(serverSocket);
        Thread serverThread = new Thread(userClient);
        serverThread.start();
    }

    // the main entry point for this hot mess of an application
    public static void main(String[] args) {        
        getConfig(args);

        try {
            if (toCrack) {
                crack();
            } else {

                // instead, block the client and interact with the server directly
                // with user input after using credentials stolen from client
                runUserClient();
            }
        } catch (IOException e) {
            System.out.println("Well, something has gone wrong hasn't it:");
            System.out.println(e.getMessage());
        }
    }
}