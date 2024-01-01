package giocoClientServer.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket client;
    private DataOutputStream out;
    private DataInputStream in;
    private String username;

    public Client(Socket client, String username) throws IOException {
        this.client = client;
        out = new DataOutputStream(this.client.getOutputStream());
        in = new DataInputStream(this.client.getInputStream());
        this.username = username;
    }

    public void sendMessage(){
        try{
            out.writeUTF(username);
            Scanner s = new Scanner(System.in);
            String message;
            while(client.isConnected()){
                message = s.nextLine();
                out.writeUTF(message);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void receiveMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String message;
                    while(client.isConnected()){
                        message = in.readUTF();
                        System.out.println(message);
                    }
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) throws IOException {
        Scanner s = new Scanner(System.in);
        System.out.println("Inserisci username");
        String un = s.nextLine();
        Client c = new Client(new Socket("localhost", 1234), un);
        c.receiveMessage();
        c.sendMessage();
    }
}

