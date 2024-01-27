package giocoClientServer.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

public class Connection implements Runnable{

    public static LinkedList<Connection> clientList = new LinkedList<>();       // A list of all the connection in the server
    public static LinkedList<Game> gameList = new LinkedList<>();               // A list of all the game that are being played     // What game this client is going to play in
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
        boolean c;          // Variable that decides if you can continue
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
                sendToClient("""
                        Do you want to join or create a game?\s
                        1. join
                        2. create""");
                message = receiveFromClient();
                //  Option 1: Joining an existing lobby
                if(message.equals("1")){
                    if(gameList.isEmpty()){         // check if there are any lobbies
                        sendToClient("There are no game at the moment");
                        c = false;
                    }
                    else{
                        do{
                            c = false;
                            printPublicGame();
                            sendToClient("Insert the game ID (if it's a private lobby it won't be in the list above)");
                            message = receiveFromClient();      // receive the lobby ID from the client
                            for(Game g : gameList){
                                if(g.ID.equals(message) && !g.hasStarted && g.playerList.size()<g.maxPlayer){   // if a lobby with that ID exist then it connect to it
                                    g.addPlayer(new Player(username, g.startingDice, g.maxDieValue, this, g));
                                    c = true;
                                }

                            }
                            if(!c){
                                sendToClient("A game with that ID doesn't exist, it has already started or reached the max player");
                            }
                        } while(!c);
                    }
                }
                // Option 2: Creating a custom lobby
                else if(message.equals("2")){
                    // We have 4 customizable option for the lobby
                    boolean isPublic = true;// IF THE LOBBY IS PUBLIC OR PRIVATE
                    String decision = "";
                    int maxPlayer = -1;     // MAXIMUM AMOUNT OF PLAYER (MIN 2, MAX 6)
                    int minPlayer = -1;     // MINIMUM AMOUNT OF PLAYER TO START (MIN 2, MAX maxPlayer)
                    int maxDieValue = -1;  // MAXIMUM VALUE OF THE DIE (MIN 2, MAX 20)
                    int startingDice = -1;  // THE NUMBER OF STARTING DICE (MIN 1, MAX 10)
                    do{
                        try{
                            c = true;
                            // PUBLIC OR PRIVATE
                            if(!decision.equals("1") && !decision.equals("2")){
                                sendToClient("""
                                        Do you want the lobby to be private or public
                                        1. Public
                                        2. Private""");
                                decision = receiveFromClient();
                                if(decision.equals("1")){
                                    isPublic = true;
                                }
                                else if(decision.equals("2")){
                                    isPublic = false;
                                }
                                else{
                                    sendToClient("Error repeat");
                                    c = false;
                                }
                            }
                            // MAX PLAYER
                            if(maxPlayer<1 && c){
                                sendToClient("Insert the game maximum number of player (max = 6)");
                                maxPlayer = Integer.parseInt(receiveFromClient());
                                if(maxPlayer>6 || maxPlayer<2){
                                    sendToClient("Error repeat");
                                    maxPlayer = -1;
                                    c = false;
                                }
                            }
                            // MIN PLAYER
                            if((minPlayer<1) && c){
                                sendToClient("Insert the game minimum number of player (minimum = 2)");
                                minPlayer = Integer.parseInt(receiveFromClient());
                                if(minPlayer<2 || minPlayer>maxPlayer){
                                    sendToClient("Error repeat");
                                    minPlayer = -1;
                                    c = false;
                                }
                            }
                            // MAX DICE VALUE
                            if((maxDieValue<1) && c){
                                sendToClient("Insert the dice max value (max = 20)");
                                maxDieValue = Integer.parseInt(receiveFromClient());
                                if(maxDieValue>20 || maxDieValue<=1){
                                    sendToClient("Error repeat");
                                    maxDieValue = -1;
                                    c = false;
                                }
                            }
                            // STARTING NUMBER OF DICE
                            if((startingDice < 1) && c){
                                sendToClient("Insert the number of starting dice (max = 10)");
                                startingDice = Integer.parseInt(receiveFromClient());
                                if(startingDice>10 || startingDice < 1){
                                    sendToClient("Error repeat");
                                    startingDice = -1;
                                    c = false;
                                }
                            }
                        } catch (Exception e){
                            sendToClient("Error repeat");
                            c = false;
                        }
                    } while(!c);
                    gameList.add(new Game(maxPlayer, minPlayer, maxDieValue, startingDice, this, isPublic)); // Create a new game with this option
                }
                else{
                    c = false;
                    sendToClient("Please insert a valid option");
                }
            } while(!c);
            // FINISH
        }
        catch (IOException e){
            try {
                disconnect();
            } catch (IOException ex) {
                System.out.println("error");
            }
        }
    }

    /**
     * A function that sent a message to the client, to use insert the message in the parameter
     * @param message message
     * @throws IOException can happen
     */
    public void sendToClient(String message) throws IOException {
        out.writeUTF(message);
    }

    /**
     * A function used to receive messages from the client, it will return the messages as a String
     * @return returns the message that the client wrote
     * @throws IOException can happen
     */
    public String receiveFromClient() throws IOException {
        return in.readUTF();
    }

    /**
     * Disconnect the client deleting the connection
     * @throws IOException can happen
     */
    public void disconnect() throws IOException {
        try{
            clientList.remove(this);
            System.out.println(this.username + " has disconnected");
            client.close();
            this.out = null;
            this.in = null;
            this.client = null;
            this.username = null;
        } catch (Exception e){
            System.out.println("error");
        }
    }

    /**
     * Function that prints all the public games
     * @throws IOException can happen
     */
    public void printPublicGame() throws IOException {
        int i = 1;
        sendToClient("Public lobbies:");
        for(Game g : gameList){
            if(g.isPublic && !g.hasStarted){
                sendToClient(i + ". " + g.ID + " (" + g.playerList.size() + "/" + g.maxPlayer + ")");
                i++;
            }
        }
        sendToClient("--------------");
    }

    /**
     * removes the game from the game the list
     * @param g the game to remove
     */
    public static void removeGame(Game g){
        gameList.remove(g);
    }

    public void ping() throws IOException {
        sendToClient("ping");
    }
}


