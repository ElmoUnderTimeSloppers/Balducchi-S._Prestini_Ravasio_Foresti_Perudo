package giocoClientServer.server;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

public class Game implements Runnable {

    public int maxPlayer;
    public int minPlayer;
    public int maxDiceValue;
    public int startingDice;
    public String ID;
    LinkedList<Player> playerList = new LinkedList<>();

    public Game(int maxPlayer, int minPlayer, int maxDiceValue, int startingDice, Connection host) throws IOException {
        this.maxPlayer = maxPlayer;
        this.minPlayer = minPlayer;
        this.maxDiceValue = maxDiceValue;
        this.startingDice = startingDice;
        ID = String.valueOf(new Random().nextInt(0, 100000));
        playerList.add(new Player(host.username, startingDice, maxDiceValue, this, host.client));
        playerList.getFirst().out.writeUTF("ID = " + ID);
    }
    @Override
    public void run() {
        while(true){

        }
    }
    public void addPlayer(Player p) throws IOException {
        playerList.add(p);
        broadcast(p.username + " has connected, the player count is " + playerList.size() + "/" + maxPlayer);
    }
    public void removePlayer(String username1) throws IOException {
        for(int i=0; i<playerList.size(); i++){
            System.out.println("Prova1");
            if(playerList.get(i).username.equals(username1)){
                System.out.println("Prova2");
                playerList.remove(i);
                broadcast(username1 + " has disconnected");
            }
        }
    }
    private void broadcast(String message) throws IOException {
        for(Player p : playerList){
            p.out.writeUTF(message);
        }
    }
}
