package giocoClientServer.server;

import java.io.IOException;

public class Player {
    public String username;
    public boolean isEliminated = false;
    public Connection myConnection;
    public int numberOfDice;
    public Results results;

    /**
     * save the player information in the server that are acquired through client
     * @param username
     * @param nDice
     * @param maxDiceValue
     * @param myConnection
     * @throws IOException
     */
    Player(String username, int nDice, int maxDiceValue, Connection myConnection) throws IOException {
        results = new Results(maxDiceValue);
        this.username = username;
        this.numberOfDice = nDice;
        this.myConnection = myConnection;
    }

    public void Throw(){
        results.Roll(numberOfDice);     //generate all the dices of the player
    }

    public String rToString(){
        return results.toString();      //print the result of the dices
    }

}
