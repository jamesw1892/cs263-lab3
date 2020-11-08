package listener;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Object to be created in Program.java and passed to both
 * ClientConnection.java and ServerConnection.java to enable
 * them to communicate synchronously by saving data here.
 * 
 * First it will be used to gather the DHM information.
 * Once all this is saved, it will crack the DHM key and save that.
 * Then it will gather the encrypted messages.
 * Finally, it will decrypt the messages using the DHM key.
 */
public class Cracker {
	private BigInteger n, g, gc, gs, key;
	private ArrayList<String> encryptedMessages;
	private boolean readyToDecrypt;
	private Decoder decoder;

	// constants for cipher
    public static final String CIPHER = "AES";
	public static final String CIPHER_DETAILS = "AES/ECB/NoPadding";	
	public static final int KEY_SIZE_BYTES = 16;

    public Cracker() {
		this.encryptedMessages = new ArrayList<>();
		this.readyToDecrypt = false;
		this.decoder = Base64.getDecoder();
	}

	public void addEncryptedMessage(String encryptedMessage) {
		this.encryptedMessages.add(encryptedMessage);
	}

	public void setN(String n) {
		this.n = new BigInteger(n);
	}

	public void setG(String g) {
		this.g = new BigInteger(g);
	}

	public void setGC(String gc) {
		this.gc = new BigInteger(gc);
	}

	public void setGS(String gs) {
		this.gs = new BigInteger(gs);
	}

	public void crackDHM() {

		boolean foundC = false;
		boolean foundS = false;

		BigInteger c = BigInteger.ZERO;
		BigInteger s = BigInteger.ZERO;

		BigInteger current = BigInteger.ONE;
		BigInteger ans;

		while (current.compareTo(this.n) <= 0 && (!foundC || !foundS)) {

			ans = this.g.modPow(current, this.n);

			if (ans.equals(this.gc)) {
				c = new BigInteger(current.toString());
				foundC = true;
			} else if (ans.equals(this.gs)) {
				s = new BigInteger(current.toString());
				foundS = true;
			}

			current = current.add(BigInteger.ONE);
		}

		if (c.equals(BigInteger.ZERO)) {
			throw new CrackingException("Unable to crack DHM");
		}

		BigInteger key1 = this.gc.modPow(s, this.n);
		BigInteger key2 = this.gs.modPow(c, this.n);

		if (key1.equals(key2)) {
			System.out.println("       [cracker]: Successfully cracked DHM, key = " + key1);
			this.key = key1;
		} else {
			System.out.println(key1);
			System.out.println(key2);
			throw new CrackingException("Keys don't match");
		}
	}

	public void decryptWhenReady(boolean isClient) {
		String prefix = isClient ? "Client": "Server";
		System.out.println("       [cracker]: " + prefix + " is ready to decrypt");
		if (this.readyToDecrypt) {
			this.decrypt();
		} else {
			this.readyToDecrypt = true;
		}
	}

	public void decrypt() {

		// add key to array of correct size, surrounded by zeros
		byte[] keyBytes = new byte[KEY_SIZE_BYTES];
		byte[] temp = this.key.toByteArray();
		System.arraycopy(temp, 0, keyBytes, 0, temp.length);

		// set up cipher
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, CIPHER);
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_DETAILS);
			cipher.init(Cipher.DECRYPT_MODE, keySpec);

			System.out.println("       [cracker]: Decrypted messages:");

			for (String encryptedMessage: this.encryptedMessages) {

				// decode the base64-encoded message
				byte[] encryptedMessageBytes = this.decoder.decode(encryptedMessage);

				// decipher it
				String msg = new String(cipher.doFinal(encryptedMessageBytes));

				// output it
				System.out.println(msg);
			}
		} catch (Exception e) {
			throw new CrackingException(e.getMessage());
		}
	}
}