package service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import utils.ServerUtils;


public class AuthenticationService {
    private ServerUtils serverUtils;
    private Socket clientSocket;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private SecretKey aesKey;

    public AuthenticationService(BufferedWriter bufferedWriter, Socket clientSocket){
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            KeyPair pair = generator.generateKeyPair();
            this.privateKey = pair.getPrivate();
            this.publicKey = pair.getPublic();
            this.serverUtils = new ServerUtils(bufferedWriter);
            this.clientSocket = clientSocket;

        } catch(Exception e) {  
            e.printStackTrace();
        
        }
    }

    public void sendPublicKeyToClient() throws IOException{
        byte[] msgEncriptada = this.publicKey.getEncoded();
        System.out.println("Chave publica antes da base: " + msgEncriptada);
        String messageToSend = "CHAVE_PUBLICA " + Base64.getEncoder().encodeToString(msgEncriptada);
        BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        System.out.println("Sending public key to client:" + messageToSend);
        serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);
    }

    public void decryptSimetricKey(String key)  {
        try {
            byte[] authRequestBytes = Base64.getDecoder().decode(key);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey); // privateKey é a chave privada do servidor
            byte[] aesKeyBytes = cipher.doFinal(authRequestBytes);
    
            // Reconstruir a chave AES
            this.aesKey = new SecretKeySpec(aesKeyBytes, "AES");
            byte[] chave = this.aesKey.getEncoded();
            System.out.println("Chave AES: " + chave);

        }catch(Exception e) {
            e.printStackTrace();
        }

    }

    public String decryptMessageFromClient(String message) throws Exception {
        try {
            byte[] messageBytes = Base64.getDecoder().decode(message);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey); // aesKey é a chave AES criada pelo servidor
            byte[] decryptedMessageBytes = cipher.doFinal(messageBytes);
            String decryptedMessage = new String(decryptedMessageBytes);
            return decryptedMessage;
        } catch (Exception e) {
            e.printStackTrace();
            return "ERRO: Failed to decrypt message";

        }
    }
    public String encryptMessage(String Message){
        try {
            Cipher cif = Cipher.getInstance("AES");
            cif.init(Cipher.ENCRYPT_MODE, this.aesKey);
    
            byte[] buffer = cif.doFinal(Message.getBytes());
            String messageToSend = Base64.getEncoder().encodeToString(buffer);
            return messageToSend;            
        } catch (Exception e) {
            e.printStackTrace();
            return "ERRO: Failed to encrypt message";
        }  
    }

    public void encryptedMessage(String Message){
        try {
            Cipher cif = Cipher.getInstance("AES");
            cif.init(Cipher.ENCRYPT_MODE, this.aesKey);
    
            byte[] buffer = cif.doFinal(Message.getBytes());
            String messageToSend = Base64.getEncoder().encodeToString(buffer);
            BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            serverUtils.sendMessageToUniqueClient(messageToSend, bufWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }  
    } 


}
