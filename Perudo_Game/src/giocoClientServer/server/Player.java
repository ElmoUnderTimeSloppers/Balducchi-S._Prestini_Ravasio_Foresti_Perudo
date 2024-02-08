package giocoClientServer.server;

import java.io.IOException;

public class Player implements Runnable{
    public String username;                     // The username of the player
    public boolean isEliminated = false;        // Check if the player has been eliminated
    public Connection myConnection;             // The connection between client and server
    public int numberOfDice;                    // The number of dice the player has
    public Results results;                     // The results of those dices
    public Game gameConnectedTo;                // The game the player is connected to
    boolean c = false;

    /**
     * save the player information in the server that are acquired through client
     * @param username Username of the player
     * @param nDice number of dice
     * @param maxDieValue max value of the dice
     * @param myConnection the connection
     * @throws IOException can happen
     */
    Player(String username, int nDice, int maxDieValue, Connection myConnection, Game gameConnectedTo) throws IOException {
        results = new Results(maxDieValue);
        this.username = username;
        this.numberOfDice = nDice;
        this.myConnection = myConnection;
        this.gameConnectedTo = gameConnectedTo;
    }

    /**
     * Throws all the dices
     */
    public void Throw(){
        results.Roll(numberOfDice);     //generate all the dices of the player
    }

    /**
     * get the results as a string
     * @return the results as a string
     */
    public String rToString(){
        return results.toString();      //print the result of the dices
    }


    @Override
    public void run() {     // start receiving message from client
        try{
            String message;
            while(myConnection.client.isConnected()){
                message = myConnection.receiveFromClient();
                if(message.equals("calza") || message.equals("ng \u0001")){                                        //if someone calls calza then the server stop the current player
                    gameConnectedTo.callCalza(this);
                }
                else if(message.equals("pong")){
                    c = false;
                    break;
                }
                System.out.println(username + ": " + message);
            }
        }
        catch (IOException e){
            System.out.println("A player disconnected");
        }
    }

    /**
     * Start receiving from the client
     */
    public void startReceiving(){
        c = true;
        Thread receiveMessage = new Thread(this);
        receiveMessage.start();
    }

    /**
     * Stops receiving from the client
     * @throws IOException can happen
     */
    public void stopReceiving() throws IOException {
        myConnection.ping();
    }
}
