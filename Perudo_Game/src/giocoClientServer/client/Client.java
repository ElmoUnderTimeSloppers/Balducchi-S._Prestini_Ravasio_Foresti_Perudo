package giocoClientServer.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * class Client used to start the client
 */
public class Client {
    private final Socket client;        // the socket of the client
    private final DataOutputStream out; // output stream
    private final DataInputStream in;   // input stream

    /**
     * Constructor of client
     * @param client Socket of the client
     * @throws IOException Throws IOException because of the stream
     */
    public Client(Socket client) throws IOException {
        this.client = client;
        out = new DataOutputStream(this.client.getOutputStream());
        in = new DataInputStream(this.client.getInputStream());
    }

    /**
     * Function used to send messages to the server
     */
    public void sendMessage(){
        try{
            Scanner s = new Scanner(System.in);
            String message;
            while(client.isConnected()){
                message = s.nextLine();
                out.writeUTF(message);
            }
        }
        catch (IOException e){
            System.out.println("Error");
        }
    }

    /**
     * Function to receive messages from the server and print them
     */
    public void receiveMessage(){
        new Thread(() -> {
            try{
                String message;
                while(client.isConnected()){
                    message = in.readUTF();
                    System.out.println(message);
                }
            }
            catch (IOException e){
                System.out.println("Error");
            }
        }).start();
    }

    /**
     * Main
     * @param args  args
     * @throws IOException  Throws IOException because of the stream
     */
    public static void main(String[] args) throws IOException {
        Client c = new Client(new Socket("localhost", 1234));
        c.receiveMessage();
        c.sendMessage();
    }
}

