package giocoClientServer.server;

import java.io.IOException;

public class Player {
    public String username;
    public boolean isEliminated = false;
    public Connection myConnection;
    public int numberOfDice;
    public Results results;
    Player(String username, int nDice, int maxDiceValue, Connection myConnection) throws IOException {
        results = new Results(maxDiceValue);
        this.username = username;
        this.numberOfDice = nDice;
        this.myConnection = myConnection;
    }

    public void Throw(){
        results.Roll(numberOfDice);
    }

    public String rToString(){
        return results.toString();
    }

}
