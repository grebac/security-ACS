package hepl.grebac.acs;

import hepl.caberg.tokenapp.Web.tokenRequestTemplate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.security.Security;

@SpringBootApplication
public class AcsApplication {

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

				if(isUserDataCorrect()) {
					System.out.println("User data is correct");
					writer.write("ACK\n");
					writer.flush();

					writer.write(giveUserToken(tokenRequest.getBankNumber() + tokenRequest.getDate()) + "\n");
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

				String msg =  reader.readLine();
				System.out.println("Received message: " + msg);

				if (isTokenValid(new Token(msg))) {
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

	private static boolean isTokenValid(Token token) {
		return false;
	}


	private static boolean isUserDataCorrect() {
		return true;
	}

	private static String giveUserToken(String token) {
		return "Updated : " + token;
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
