package hepl.grebac.acs.encryption;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class KeyPairManager {
    private KeyPair keyPair;

    public KeyPairManager(String publicKey) {
        this.keyPair = getRSAKeyPair(publicKey);
    }

    private KeyPair getRSAKeyPair(String publicKey) {
        try {
            // Construct a relative path
            String keystoreRelativePath = "src/main/resources/keys/" + publicKey;

            // Build the absolute path
            String rootPath = System.getProperty("user.dir");
            Path absolutePath = Paths.get(rootPath, keystoreRelativePath);

            // Load the keystore
            char[] keystorePassword = "heplPass".toCharArray();
            FileInputStream keystoreFile = new FileInputStream(absolutePath.toString());

            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(keystoreFile, keystorePassword);

            // Retrieve key pair from keystore
            String alias = "clientKeys";  // Replace with the alias of your key pair
            char[] keyPassword = "heplPass".toCharArray();  // Replace with the password of your key pair

            return this.keyPair = getKeyPairFromKeyStore(keystore, alias, keyPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static KeyPair getKeyPairFromKeyStore(KeyStore keystore, String alias, char[] keyPassword) throws Exception {
        // Retrieve the private key and certificate chain from the keystore
        PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, keyPassword);
        Certificate[] certChain = keystore.getCertificateChain(alias);

        // Assume that the first certificate in the chain is the end-entity certificate
        X509Certificate x509Cert = (X509Certificate) certChain[0];
        PublicKey publicKey = x509Cert.getPublicKey();

        // Create a KeyPair from the retrieved keys
        return new KeyPair(publicKey, privateKey);
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }
}
