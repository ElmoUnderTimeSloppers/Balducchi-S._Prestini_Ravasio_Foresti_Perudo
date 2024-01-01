package giocoClientServer.server;

import giocoClient.Results;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Player {
    public Socket client;
    public DataOutputStream out;
    public DataInputStream in;
    public String username;
    private boolean isConnected;
    public int numberOfDice;
    private Game gameConnectedTo;
    public Results results;
    Player(String username, int nDice, int maxDiceValue, Game gameConnectedTo, Socket client) throws IOException {
        results = new Results(maxDiceValue);
        this.username = username;
        this.numberOfDice = nDice;
        this.gameConnectedTo = gameConnectedTo;
        this.client = client;
        out = new DataOutputStream(this.client.getOutputStream());
        in = new DataInputStream(this.client.getInputStream());
    }

    public void Throw(){
        results.Roll(numberOfDice);
    }

    public String rToString(){
        return results.toString();
    }

}
