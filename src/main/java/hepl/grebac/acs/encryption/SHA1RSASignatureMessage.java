package hepl.grebac.acs.encryption;


import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.*;

public class SHA1RSASignatureMessage implements Serializable
{
    private static final String PROVIDER_NAME = "BC";

    private Object objectToSign;
    private byte[] signature;

    public SHA1RSASignatureMessage(Object objectToSign){
        this.objectToSign = objectToSign;
    }

    public void sign(PrivateKey rsaPrivateKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException
    {
        Signature rsaSignatureTool = Signature.getInstance("SHA1withRSA", PROVIDER_NAME);
        rsaSignatureTool.initSign(rsaPrivateKey);
        rsaSignatureTool.update(ObjectToBytes());
        signature = rsaSignatureTool.sign();
    }

    public boolean verify(PublicKey rsaPublicKey, byte[] signatureToVerify) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException
    {
        Signature rsaSignatureTool = Signature.getInstance("SHA1withRSA", PROVIDER_NAME);
        rsaSignatureTool.initVerify(rsaPublicKey);
        rsaSignatureTool.update(ObjectToBytes());
        return rsaSignatureTool.verify(signatureToVerify);
    }

    public Object getObjectToSign(){
        return objectToSign;
    }

    public byte[] getSignature(){
        return signature;
    }

    private byte[] ObjectToBytes() {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)){

            // Serialize the object
            objectOutputStream.writeObject(this.objectToSign);
            byte[] objectBytes = byteArrayOutputStream.toByteArray();

            return objectBytes;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}