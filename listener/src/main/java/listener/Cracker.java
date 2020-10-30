package listener;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;

/**
 * Crack messages in different thread
 */
public class Cracker implements Runnable {
    private byte[][] messages;
    private BigInteger n, g, gc, gs;
    private Base64.Decoder base64Decoder = new Base64.getDecoder();

    public Cracker(ArrayList<String> messages) {

        if (messages.size() < 4) {
            throw new RuntimeException("Not enough messages sent!");
        }

        this.messages = new byte[messages.size() - 4][];
        for (int index = 0; index < message.size(); index++) {

            switch (index) {
                case 0: this.n  = new BigInteger(messages.get(0)); break;
                case 1: this.g  = new BigInteger(messages.get(1)); break;
                case 2: this.gc = new BigInteger(messages.get(2)); break;
                case 3: this.gs = new BigInteger(messages.get(3)); break;
                default:
                    this.messages[index - 4] = base64Decoder.decode(message.get(index));
            }
        }

        // output known numbers
        System.out.println("n = " + n);
        System.out.println("g = " + g);
        System.out.println("g^c mod n = " + gc);
        System.out.println("g^s mod n = " + gs);
    }

    public void bruteForce() {

        BigInteger c, s;

        BigInteger logInterval = new BigInteger("100");
        BigInteger zero = new BigInteger("0");

        boolean foundC = false;
        boolean foundS = false;
        BigInteger one = new BigInteger("1");
        BigInteger i = new BigInteger("0");
        while (!(foundC && foundS)) {

            // calc i^g mod n
            BigInteger ans = i.modPow(g, n);

            // compare to c^g mod n and s^g mod n
            if (ans.equals(gc)) {
                c = i;
                foundC = true;
            } else if (ans.equals(gs)) {
                s = i;
                foundS = true;
            }

            i.add(one);
            if (i.remainder(logInterval).equals(zero)) {
                System.out.println("Got to " + i);
            }
        }

        System.out.println("s = " + s + ", c = " + c);

    }

    public void run() {

        bruteForce();

    }
}
