package listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class UserClient implements Runnable {
    private Socket socket;
    private Base64.Encoder encoder;
    private Base64.Decoder decoder;
    private Cipher encipherer;
    private Cipher decipherer;

    public UserClient(Socket socket) {
        this.socket = socket;
        this.decoder = Base64.getDecoder();
        this.encoder = Base64.getEncoder();
    }

    private String encrypt(String message) {

        try {

            // encipher
            byte[] encryptedBytes = this.encipherer.doFinal(message.getBytes());
            // encode to base64
            return this.encoder.encodeToString(encryptedBytes);

        } catch (Exception e) {
            throw new CrackingException(e.getMessage());
        }
    }

    private String decrypt(String encryptedMessageBase64) {

        // decode from base64
        byte[] encryptedMessageBytes = this.decoder.decode(encryptedMessageBase64);

        // decipher
        try {
            return new String(this.decipherer.doFinal(encryptedMessageBytes));
        } catch (Exception e) {
            throw new CrackingException(e.getMessage());
        }
    }

    private void setUpCipher(BigInteger key) {

        // derive key
        byte[] keyBytes = new byte[Cracker.KEY_SIZE_BYTES];
        byte[] temp = key.toByteArray();
		System.arraycopy(temp, 0, keyBytes, 0, temp.length);

		// set up cipher
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, Cracker.CIPHER);
		try {
		    this.encipherer = Cipher.getInstance(Cracker.CIPHER);
            this.encipherer.init(Cipher.ENCRYPT_MODE, keySpec);
            
            this.decipherer = Cipher.getInstance(Cracker.CIPHER);
            this.decipherer.init(Cipher.DECRYPT_MODE, keySpec);

		} catch (Exception e) {
			throw new CrackingException(e.getMessage());
		}
    }

    public void run() {

        try {
            PrintWriter outToServer = new PrintWriter(this.socket.getOutputStream(), true);
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            // ----------- start perform DHM with server --------------

            // get first 2 inputs from server which are n and g respectively
            BigInteger n = new BigInteger(inFromServer.readLine());
            BigInteger g = new BigInteger(inFromServer.readLine());

            // pick own secret number, p, and send g^p mod n
            BigInteger p = new BigInteger("11");
            outToServer.println(g.modPow(p, n).toString());

            // get gs = g^s mod n
            BigInteger gs = new BigInteger(inFromServer.readLine());
            
            // calculate key = gs^p mod n = g^(p*s) mod n
            BigInteger key = gs.modPow(p, n);

            // ----------- end perform DHM with server ---------------

            // set up AES cipher using key
            this.setUpCipher(key);

            // get initial message from server
            inFromServer.readLine();

            // send username, password and first command
            outToServer.println(this.encrypt("LordBalaclava"));
            outToServer.println(this.encrypt("Mw3JfcBRA0HyylpIQc0vvQ=="));
            outToServer.println(this.encrypt("ls"));
            System.out.println("[client->server]: ls");

            // repeatedly print message from server and allow user to respond
            Scanner scanner = new Scanner(System.in);
            String inputLine;
            while ((inputLine = inFromServer.readLine()) != null) {
                // output messages from server
                System.out.println("[server->client]: " + this.decrypt(inputLine));

                // respond with encrypted input from user
                System.out.print("Input what to send to server: ");
                String inputFromUser = scanner.nextLine();
                outToServer.println(this.encrypt(inputFromUser));
            }
        } catch (IOException e) {
            System.out.println("Something has gone wrong with the connection to the server!");
            System.out.println(e.getMessage());
        }
    }
}