package giocoClientServer.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

public class Connection implements Runnable{

    public static LinkedList<Connection> clientList = new LinkedList<>();
    public static LinkedList<Game> gameList = new LinkedList<>();
    private String gameConnectedTo = "NOT CONNECTED";
    public Socket client;
    private DataOutputStream out;
    private DataInputStream in;
    public String username;

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
        boolean c = true;
        try{
            do{
                out.writeUTF("Do you want to join or create a game? \n" + "1. join\n" + "2. create");
                message = in.readUTF();
                if(message.equals("1")){
                    if(gameList.isEmpty()){
                        out.writeUTF("There are no game at the moment");
                        c = false;
                    }
                    else{

                        do{
                            c = true;
                            out.writeUTF("Insert the game ID");
                            message = in.readUTF();
                            for(Game g : gameList){
                                c = false;
                                if(g.ID.equals(message)){
                                    g.addPlayer(new Player(username, g.startingDice, g.maxDiceValue, g, client));
                                    gameConnectedTo = message;
                                    c = true;
                                }
                            }
                            if(!c){
                                out.writeUTF("A game with that ID doesn't exist");
                            }
                        } while(!c);
                    }
                }
                else if(message.equals("2")){
                    System.out.println("prova");
                    c = true;
                    int maxPlayer = -1;
                    int minPlayer = -1;
                    int maxDiceValue = -1;
                    int startingDice = -1;
                    do{
                        try{
                            c = true;
                            if(maxPlayer>6 || maxPlayer<2){
                                out.writeUTF("Insert the game maximum number of player (max = 6)");
                                maxPlayer = Integer.parseInt(in.readUTF());
                                if(maxPlayer>6 || maxPlayer<2){
                                    out.writeUTF("Errore ripetere");
                                    c = false;
                                }
                            }

                            if((minPlayer<2 || minPlayer>maxPlayer) && c){
                                out.writeUTF("Insert the game minimum number of player (minimum = 2)");
                                minPlayer = Integer.parseInt(in.readUTF());
                                if(minPlayer<2 || minPlayer>maxPlayer){
                                    out.writeUTF("Errore ripetere");
                                    c = false;
                                }
                            }

                            if((maxDiceValue>20 || maxDiceValue<=1) && c){
                                out.writeUTF("Insert the dice max value (max = 20");
                                maxDiceValue = Integer.parseInt(in.readUTF());
                                if(maxDiceValue>20 || maxDiceValue<=1){
                                    out.writeUTF("Errore ripetere");
                                    c = false;
                                }
                            }

                            if((startingDice>10 || startingDice < 1) && c){
                                out.writeUTF("Insert the number of starting dice (max = 10)");
                                startingDice = Integer.parseInt(in.readUTF());
                                if(startingDice>10 || startingDice < 1){
                                    out.writeUTF("Errore ripetere");
                                    c = false;
                                }
                            }
                        } catch (Exception e){
                            out.writeUTF("Errore ripetere");
                            c = false;
                        }
                    } while(!c);
                    gameList.add(new Game(maxPlayer, minPlayer, maxDiceValue, startingDice, this));
                }
                else{
                    c = false;
                    out.writeUTF("Pleas insert a valid option");
                }
            } while(!c);
            while (true){
                System.out.println(in.readUTF());
            }


        }
        catch (IOException e){
            try {
                disconnect();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void broadcast(String message) throws IOException {
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
    private void disconnect() throws IOException {
        System.out.println("Prova4");
        try{
            for(Game g : gameList){
                System.out.println("Prova3");
                if(g.ID.equals(gameConnectedTo)){
                    g.removePlayer(username);
                }
            }
            clientList.remove(this);
            System.out.println(this.username + " has disconnected");
            broadcast(this.username + " has disconnected");
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
