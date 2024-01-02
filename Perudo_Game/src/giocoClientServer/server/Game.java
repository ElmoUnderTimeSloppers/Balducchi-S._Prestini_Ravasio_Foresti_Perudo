package giocoClientServer.server;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

public class Game {

    boolean hasStarted = false;
    private LinkedList<Integer> results = new LinkedList<>();
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
        ID = String.valueOf(new Random().nextInt(10000, 100000));
        playerList.add(new Player(host.username, startingDice, maxDiceValue, this, host));
        playerList.getFirst().myConnection.sendToClient("ID = " + ID);
        new Thread(new Runnable() {
            @Override
            public void run() {

                String message;
                try{
                    playerList.getFirst().myConnection.sendToClient("Write start to begin the game (you have to reach the minimum player)");
                    do{
                        message = playerList.getFirst().myConnection.receiveFromClient();
                        if(message.equals("start") && minPlayer <= playerList.size()){
                            hasStarted = true;
                        }
                    }while(!hasStarted);
                    startNewTurn();
                } catch(Exception e){
                    try {
                        playerList.getFirst().myConnection.disconnect();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }).start();
    }
    public void addPlayer(Player p) throws IOException {
        playerList.add(p);
        broadcast(p.username + " has connected, the player count is " + playerList.size() + "/" + maxPlayer);
    }
    public void removePlayer(String username1) throws IOException {
        for(int i=0; i<playerList.size(); i++){
            if(playerList.get(i).username.equals(username1)){
                playerList.remove(i);
                broadcast(username1 + " has disconnected");
            }
        }
    }
    private void broadcast(String message) throws IOException {
        for(Player p : playerList){
            p.myConnection.sendToClient(message);
        }
    }
    private void rollAll(){
        for(Player p : playerList){
            p.Throw();
        }
        addResults();
    }

    private void printDicePrivate() throws IOException {
        for(Player p : playerList){
            p.myConnection.sendToClient(p.rToString());
        }
    }
    private void printDiceAll() throws IOException {
        for(Player p : playerList){
            broadcast(p.rToString());
        }
    }

    private void addResults(){
        results.clear();
        for(Player p : playerList){
            results.addAll(p.results.results);
        }
    }

    private int count(int n){
        int c = 0;
        for(int r : results){
            if(r == n || r == 1){
                c++;
            }
        }
        return c;
    }

    private void startNewTurn() throws IOException {
        int numberOfDice = -1;
        int valueOfDice = -1;
        String dudoOrNot = "";
        Connection tempPlayer;
        boolean c = true;
        rollAll();
        addResults();
        printDicePrivate();
        int index = new Random().nextInt(1,playerList.size());
        while(playerList.size()>1){
            tempPlayer = playerList.get(index).myConnection;
            broadcast("it's the turn of: " + tempPlayer.username);
            tempPlayer.sendToClient("It's your turn");
            if(valueOfDice > 0){
                do{
                    tempPlayer.sendToClient("do you think the statement is correct?\n" + "1. Yes, it's correct\n" + "2. Dudo, it's not correct");
                    dudoOrNot = tempPlayer.receiveFromClient();
                    if(dudoOrNot.equals("2")){
                        broadcast(tempPlayer.username + " claims dudo, lets check");
                    }
                    else if(dudoOrNot.equals("1")){
                        tempPlayer.sendToClient("Ok");
                    }
                    else{
                        c = false;
                        tempPlayer.sendToClient("Please insert a valid option");
                    }
                } while(!c);

            }
            tempPlayer.sendToClient("decide the value of the number you want to search (for the jolly write j) (MAX = " + maxDiceValue + ")");

        }
    }
}
