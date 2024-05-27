package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static ServerSocket ss;      // the server socket

    /**
     * constructor used to start the server
     * @param ss ServerSocket
     */
    public Server(ServerSocket ss) {
        Server.ss = ss;
    }

    /**
     * starts the server
     */
    public void startServer(){
        try{
            while(!ss.isClosed()){
                Socket s = ss.accept();
                Connection c = new Connection(s); // for every socket that connects there is a new connection

                Thread t = new Thread(c); // a new tread will start for each connection
                t.start();
            }
        }
        catch (IOException e){
            System.out.println("error");
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(new ServerSocket(12345));
        server.startServer();
    }
}
