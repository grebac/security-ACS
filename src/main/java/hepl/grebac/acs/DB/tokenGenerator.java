package hepl.grebac.acs.DB;

import java.security.SecureRandom;

public class tokenGenerator {

    public static String generateRandomToken(int length) {
        byte[] randomBytes = new byte[length / 2];  // Since each byte is represented by two hexadecimal characters
        new SecureRandom().nextBytes(randomBytes);
        return bytesToHex(randomBytes);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexStringBuilder = new StringBuilder();
        for (byte b : bytes) {
            hexStringBuilder.append(String.format("%02x", b));
        }
        return hexStringBuilder.toString();
    }

    public static void main(String[] args) {
        String randomToken = generateRandomToken(8);  // Adjust the length as needed
        System.out.println("Random Token: " + randomToken);
    }
}
