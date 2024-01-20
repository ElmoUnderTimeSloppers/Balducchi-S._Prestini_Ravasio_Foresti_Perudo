package giocoClientServer.server;

import java.io.IOException;
import java.util.Scanner;

public class Player implements Runnable{
    public String username;                     // The username of the player
    public boolean isEliminated = false;        // Check if the player has been eliminated
    public Connection myConnection;             // The connection between client and server
    public int numberOfDice;                    // The number of dice the player has
    public Results results;                     // The results of those dices
    private final Thread receiveMessage = new Thread(this);
    /*
    * This should be the thread used to receive messages, but it won't stop
    * I'll leave it here for further testing
    */

    /**
     * save the player information in the server that are acquired through client
     * @param username Username of the player
     * @param nDice number of dice
     * @param maxDiceValue max value of the dice
     * @param myConnection the connection
     * @throws IOException can happen
     */
    Player(String username, int nDice, int maxDiceValue, Connection myConnection) throws IOException {
        results = new Results(maxDiceValue);
        this.username = username;
        this.numberOfDice = nDice;
        this.myConnection = myConnection;
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
    public void run() {     // THIS IS A TEST, THE THREAD WON'T STOP I DON'T KNOW WHY
        try{
            String message;
            while(myConnection.client.isConnected()){
                message = myConnection.receiveFromClient();
                System.out.println(message);
            }
        }
        catch (IOException e){
            System.out.println("Error");
        }
    }

    public void startReceiving(){
        synchronized (this){
            receiveMessage.start();
        }
    }
    public void stopReceiving(){
        synchronized (this){
            receiveMessage.interrupt();
        }
    }
}
