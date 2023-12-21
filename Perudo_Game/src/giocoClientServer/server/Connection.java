package giocoClientServer.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

public class Connection implements Runnable{

    public static LinkedList<Connection> clientList = new LinkedList<>();
    private Socket client;
    private DataOutputStream out;
    private DataInputStream in;
    private String username;

    public Connection(Socket client) throws IOException {
        this.client = client;
        out = new DataOutputStream(this.client. getOutputStream());
        in = new DataInputStream(this.client.getInputStream());
        this.username = in.readUTF();
        for(Connection c : clientList){
            if(c.username.equals(this.username)){
                this.username = this.username + "27";
            }
        }
        clientList.add(this);
        System.out.println(this.username + " has connected");
        broadcast(this.username + " has connected");
    }


    @Override
    public void run() {
        String message;

        try{
            while(client.isConnected()){
                message = in.readUTF();
                System.out.println(message);
                broadcast(message);
            }
        }
        catch (IOException e){
            this.disconnect();
        }
    }

    private void broadcast(String message){
        for(Connection c : clientList){
            try{
                if(!c.username.equals(this.username)){
                    c.out.writeUTF(message);

                }
            }
            catch (IOException e){
                this.disconnect();
            }

        }
    }

    private void disconnect(){
        clientList.remove(this);
        System.out.println(this.username + " has disconnected");
        broadcast(this.username + " has disconnected");
    }
}
