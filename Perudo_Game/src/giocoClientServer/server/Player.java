package giocoClientServer.server;

import giocoClient.Results;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Player {
    public String username;
    public Connection myConnection;
    private boolean isConnected;
    public int numberOfDice;
    private Game gameConnectedTo;
    public Results results;
    Player(String username, int nDice, int maxDiceValue, Game gameConnectedTo, Connection myConnection) throws IOException {
        results = new Results(maxDiceValue);
        this.username = username;
        this.numberOfDice = nDice;
        this.gameConnectedTo = gameConnectedTo;
        this.myConnection = myConnection;
    }

    public void Throw(){
        results.Roll(numberOfDice);
    }

    public String rToString(){
        return results.toString();
    }

}
