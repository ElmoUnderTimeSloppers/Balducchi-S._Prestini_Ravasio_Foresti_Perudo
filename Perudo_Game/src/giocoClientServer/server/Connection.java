package giocoClientServer.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

public class Connection implements Runnable{

    public static LinkedList<Connection> clientList = new LinkedList<>();       // A list of all the connection in the server
    public static LinkedList<Game> gameList = new LinkedList<>();               // A list of all the game that are being played
    private String gameConnectedTo = "NOT CONNECTED";                           // What game this client is going to play in
    public Socket client;                                                       // the socket of the client
    private DataOutputStream out;                                               // Used to send out messages to the client
    private DataInputStream in;                                                 // Used to receive the messages from the client
    public String username = "";                                                // the username

    public Connection(Socket client) throws IOException {
        this.client = client;
        // get the output and input stream from the socket
        out = new DataOutputStream(this.client. getOutputStream());
        in = new DataInputStream(this.client.getInputStream());
        clientList.add(this);   // add this connection to the list
        System.out.println("new user has connected");
    }


    @Override
    public void run() {
        String message;     // Temporary String used to receive messages
        boolean c = true;   // Variable that decides if you can continue
        try{
            //          ASKING USERNAME
            do{
                c = true;
                sendToClient("Insert your username");
                message = receiveFromClient();
                for(Connection client : clientList){
                    if(client.username.equals(message)){        // if someone already has that username it won't continue
                        c = false;
                        sendToClient("This username is already used, please use another");
                    }
                }
            } while(!c);
            this.username = message;    // set the username
            //
            //                      PRE LOBBY
            //
            do{
                // Here the client will say if he wants to join or create a lobby
                sendToClient("Do you want to join or create a game? \n" + "1. join\n" + "2. create");
                message = receiveFromClient();
                //  Option 1: Joining an existing lobby
                if(message.equals("1")){
                    if(gameList.isEmpty()){         // check if there are any lobbies
                        sendToClient("There are no game at the moment");
                        c = false;
                    }
                    else{
                        do{
                            c = true;
                            sendToClient("Insert the game ID");
                            message = receiveFromClient();      // reiceve the lobby ID from the client
                            for(Game g : gameList){
                                c = false;
                                if(g.ID.equals(message)){   // if a lobby with that ID exist then it connect to it
                                    g.addPlayer(new Player(username, g.startingDice, g.maxDiceValue, g, this));
                                    gameConnectedTo = message;
                                    c = true;
                                }
                            }
                            if(!c){
                                sendToClient("A game with that ID doesn't exist");
                            }
                        } while(!c);
                    }
                }
                // Option 2: Creating a custom lobby
                else if(message.equals("2")){
                    c = true;
                    // We have 4 customizable option for the lobby
                    int maxPlayer = -1;     // MAXIMUM AMOUNT OF PLAYER (MIN 2, MAX 6)
                    int minPlayer = -1;     // MINIMUM AMOUNT OF PLAYER TO START (MIN 2, MAX maxPlayer)
                    int maxDiceValue = -1;  // MAXIMUM VALUE OF THE DICE (MIN 2, MAX 20)
                    int startingDice = -1;  // THE NUMBER OF STARTING DICE (MIN 1, MAX 10)
                    do{
                        try{
                            c = true;
                            // MAX PLAYER
                            if(maxPlayer>6 || maxPlayer<2){
                                sendToClient("Insert the game maximum number of player (max = 6)");
                                maxPlayer = Integer.parseInt(receiveFromClient());
                                if(maxPlayer>6 || maxPlayer<2){
                                    sendToClient("Error repeat");
                                    c = false;
                                }
                            }
                            // MIN PLAYER
                            if((minPlayer<2 || minPlayer>maxPlayer) && c){
                                sendToClient("Insert the game minimum number of player (minimum = 2)");
                                minPlayer = Integer.parseInt(receiveFromClient());
                                if(minPlayer<2 || minPlayer>maxPlayer){
                                    sendToClient("Error repeat");
                                    c = false;
                                }
                            }
                            // MAX DICE VALUE
                            if((maxDiceValue>20 || maxDiceValue<=1) && c){
                                sendToClient("Insert the dice max value (max = 20");
                                maxDiceValue = Integer.parseInt(receiveFromClient());
                                if(maxDiceValue>20 || maxDiceValue<=1){
                                    sendToClient("Error repeat");
                                    c = false;
                                }
                            }
                            // STARTING NUMBER OF DICE
                            if((startingDice > 10 || startingDice < 1) && c){
                                sendToClient("Insert the number of starting dice (max = 10)");
                                startingDice = Integer.parseInt(receiveFromClient());
                                if(startingDice>10 || startingDice < 1){
                                    sendToClient("Error repeat");
                                    c = false;
                                }
                            }
                        } catch (Exception e){
                            sendToClient("Error repeat");
                            c = false;
                        }
                    } while(!c);
                    gameList.add(new Game(maxPlayer, minPlayer, maxDiceValue, startingDice, this)); // Create a new game with this option
                }
                else{
                    c = false;
                    sendToClient("Pleas insert a valid option");
                }
            } while(!c);
            // FINISH
        }
        catch (IOException e){
            try {
                disconnect();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * A function that sent a message to the client, to use insert the message in the parameter
     * @param message
     * @throws IOException
     */
    public void sendToClient(String message) throws IOException {
        out.writeUTF(message);
    }

    /**
     * A function used to receive messages from the client, it will return the messages as a String
     * @return
     * @throws IOException
     */
    public String receiveFromClient() throws IOException {
        return in.readUTF();
    }

    /**
     * A function that sends a messages to all the client except the one that is in this connection
     * @param message
     * @throws IOException
     */
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

    /**
     * Disconnect the client deleting the connection
     * @throws IOException
     */
    public void disconnect() throws IOException {
        try{
            for(Game g : gameList){
                if(g.ID.equals(gameConnectedTo)){
                    g.removePlayer(username);
                }
            }
            clientList.remove(this);
            System.out.println(this.username + " has disconnected");
            this.out = null;
            this.in = null;
            this.client = null;
            this.username = null;
            this.gameConnectedTo = null;
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
