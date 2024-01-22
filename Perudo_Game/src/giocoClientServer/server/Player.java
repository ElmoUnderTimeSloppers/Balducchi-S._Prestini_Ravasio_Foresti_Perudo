package giocoClientServer.server;

import java.io.IOException;
import java.util.Scanner;

public class Player implements Runnable{
    public String username;                     // The username of the player
    public boolean isEliminated = false;        // Check if the player has been eliminated
    public Connection myConnection;             // The connection between client and server
    public int numberOfDice;                    // The number of dice the player has
    public Results results;                     // The results of those dices
    public Game gameConnectedTo;

    /**
     * save the player information in the server that are acquired through client
     * @param username Username of the player
     * @param nDice number of dice
     * @param maxDiceValue max value of the dice
     * @param myConnection the connection
     * @throws IOException can happen
     */
    Player(String username, int nDice, int maxDiceValue, Connection myConnection, Game gameConnectedTo) throws IOException {
        results = new Results(maxDiceValue);
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
            String message = "";
            while(myConnection.client.isConnected() && !message.equals("pong")){
                message = myConnection.receiveFromClient();
                if(message.equals("calza")){                                        //if someone calls calza then the server stop the current player
                    gameConnectedTo.callCalza(this);
                }
                System.out.println(message);
            }
        }
        catch (IOException e){
            try {
                this.gameConnectedTo.removePlayer(this);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void startReceiving(){
        Thread receiveMessage = new Thread(this);
        receiveMessage.start();
    }
    public void stopReceiving() throws IOException {
        myConnection.ping();


    }
}
