package hepl.grebac.acs;

import hepl.caberg.tokenapp.tokens.tokenRequestTemplate;
import hepl.grebac.acs.DB.DBHandler;
import hepl.grebac.acs.encryption.KeyPairManager;
import hepl.grebac.acs.encryption.SHA1RSASignatureMessage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.security.*;

@SpringBootApplication
public class AcsApplication {

	public static DBHandler dbHandler = new DBHandler();

	public static void main(String[] args) throws IOException {
		Security.addProvider(new BouncyCastleProvider());

		System.setProperty("javax.net.ssl.keyStore", "acs.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "heplPass");

		System.setProperty("javax.net.ssl.trustStore", "acsTruststore.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "heplPass");

		SSLServerSocketFactory sslserversocketfactory
				= (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		SSLServerSocket sslserversocketForPortAuth
				= (SSLServerSocket) sslserversocketfactory.createServerSocket(6666);
		SSLServerSocket sslserversocketForPortMoney
				= (SSLServerSocket) sslserversocketfactory.createServerSocket(3333);

		new Thread(() -> {
			HandlePortAuthentication(sslserversocketForPortAuth);
		}).start();

		new Thread(() -> {
			HandlePortMoney(sslserversocketForPortMoney);
		}).start();
	}

	public static void HandlePortAuthentication(SSLServerSocket sslserversocket) {
		while (true) {
			try {
				SSLSocket client = (SSLSocket) sslserversocket.accept();

				var reader = new ObjectInputStream(client.getInputStream());
				var writer = GetBufferedWriter(client);

				var tokenRequest = (tokenRequestTemplate)reader.readObject();
				System.out.println("Received token: " + tokenRequest);
				var signature = (byte[])reader.readObject();
				System.out.println("Received signature: " + signature);

				var publicKeyName = dbHandler.getPublickeyByBankNumber(tokenRequest.getBankNumber());


				if(publicKeyName != null && isSignatureCorrect(tokenRequest, signature, publicKeyName)) {
					System.out.println("User data is correct");

					writer.write("ACK\n");
					writer.flush();

					var token = dbHandler.generateTokenForBanknumber(tokenRequest.getBankNumber());

					writer.write(token + "\n");
					writer.flush();
				}
				else {
					System.out.println("User data is not correct");
					writer.write("NACK\n");
					writer.flush();
				}

				client.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
	}

	public static void HandlePortMoney(SSLServerSocket sslserversocket) {
		while (true) {
			try {
				SSLSocket client = (SSLSocket) sslserversocket.accept();

				//ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
				var reader = GetBufferedReader(client);
				var writer = GetBufferedWriter(client);

				String token =  reader.readLine();
				System.out.println("Received message: " + token);

				if (isTokenValid(token)) {
					System.out.println("Token is valid");
					writer.write("ACK\n");
				} else {
					System.out.println("Token is not valid");
					writer.write("NACK\n");
				}
				writer.flush();

				client.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static boolean isTokenValid(String token) {
		return dbHandler.checkTokenValidity(token);
	}


	private static boolean isSignatureCorrect(tokenRequestTemplate tokenRequestTemplate, byte[] signature, String publicKeyName) {
		var keypairManager = new KeyPairManager(publicKeyName);
		PublicKey publicKey = keypairManager.getKeyPair().getPublic();

		SHA1RSASignatureMessage sha1RSASignatureMessage = new SHA1RSASignatureMessage(tokenRequestTemplate);
		try {
			return sha1RSASignatureMessage.verify(publicKey, signature);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
			throw new RuntimeException(e);
		}
	}

	private static BufferedWriter GetBufferedWriter(SSLSocket sslsocket) throws IOException {
		OutputStream outputstream = sslsocket.getOutputStream();
		BufferedWriter bufferedwriter = new BufferedWriter(new OutputStreamWriter(outputstream));
		return bufferedwriter;
	}

	private static BufferedReader GetBufferedReader(SSLSocket sslsocket) throws IOException {
		InputStream inputstream = sslsocket.getInputStream();
		BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream));
		return bufferedreader;
	}

}
