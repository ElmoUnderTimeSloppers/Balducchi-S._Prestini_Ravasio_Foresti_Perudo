package giocoClientServer.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static ServerSocket ss;

    public Server(ServerSocket ss) {
        this.ss = ss;
    }

    public void startServer(){
        try{
            while(!ss.isClosed()){
                Socket s = ss.accept();
                Connection c = new Connection(s);

                Thread t = new Thread(c);
                t.start();
            }
        }
        catch (IOException e){

        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(new ServerSocket(1234));
        server.startServer();
    }
}
