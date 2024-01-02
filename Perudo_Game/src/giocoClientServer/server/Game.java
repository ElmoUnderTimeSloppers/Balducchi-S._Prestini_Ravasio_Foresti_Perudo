package giocoClientServer.server;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

public class Game {

    int numberOfRestingPlayer = 0;
    int numberOfDice = -1;
    int valueOfDice = -1;
    int index = 0;
    public boolean isPublic = true;
    public boolean hasStarted = false;
    private LinkedList<Integer> results = new LinkedList<>();
    public int maxPlayer;
    public int minPlayer;
    public int maxDiceValue;
    public int startingDice;
    public String ID;
    LinkedList<Player> playerList = new LinkedList<>();

    public Game(int maxPlayer, int minPlayer, int maxDiceValue, int startingDice, Connection host, boolean isPublic) throws IOException {
        this.maxPlayer = maxPlayer;
        this.minPlayer = minPlayer;
        this.maxDiceValue = maxDiceValue;
        this.startingDice = startingDice;
        this.isPublic = isPublic;
        ID = String.valueOf(new Random().nextInt(10000, 100000));
        playerList.add(new Player(host.username, startingDice, maxDiceValue, this, host));
        playerList.getFirst().myConnection.sendToClient("ID = " + ID);
        new Thread(new Runnable() {
            @Override
            public void run() {

                String message;
                try{
                    index = new Random().nextInt(0,playerList.size());
                    playerList.getFirst().myConnection.sendToClient("Write start to begin the game (you have to reach the minimum player)");
                    do{
                        message = playerList.getFirst().myConnection.receiveFromClient();
                        if(message.equals("start") && minPlayer <= playerList.size()){
                            hasStarted = true;
                        }
                    }while(!hasStarted);
                    reStart();
                    numberOfRestingPlayer = playerList.size();
                    do{
                        startNewTurn();
                    } while(numberOfRestingPlayer > 1);
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
        String tempMessage = "";
        String dudoOrNot = "";
        Player previousPlayer = getPrevious();
        Player tempPlayer = playerList.get(index);;
        boolean c = true;
        try{
            broadcast("it's the turn of: " + tempPlayer.username);
            tempPlayer.myConnection.sendToClient("It's your turn");
            if(valueOfDice > 0){
                do{
                    c = true;
                    tempPlayer.myConnection.sendToClient("do you think the statement is correct?\n" + "1. Yes, it's correct\n" + "2. Dudo, it's not correct");
                    dudoOrNot = tempPlayer.myConnection.receiveFromClient();
                    if(dudoOrNot.equals("2")){
                        broadcast(tempPlayer.username + " claims dudo, lets check");
                        printDiceAll();
                        if(dudo(valueOfDice, numberOfDice)){
                            broadcast("The dudo is false, " + tempPlayer.username + " gets one of his dice taken away");
                            tempPlayer.numberOfDice--;
                            if(tempPlayer.numberOfDice<1){
                                broadcast(tempPlayer.username + " has finished his dices, he is eliminated");
                                numberOfRestingPlayer--;
                                tempPlayer.isEliminated = true;
                            }
                        }
                        else{
                            broadcast("The dudo is true, " + previousPlayer.username + " gets one of his dice taken away");
                            previousPlayer.numberOfDice--;
                            if(previousPlayer.numberOfDice<1){
                                broadcast(previousPlayer.username + " has finished his dices, he is eliminated");
                                numberOfRestingPlayer--;
                                previousPlayer.isEliminated = true;
                            }
                        }
                        reStart();
                    }
                    else if(dudoOrNot.equals("1")){
                        tempPlayer.myConnection.sendToClient("Ok");
                    }
                    else{
                        c = false;
                        tempPlayer.myConnection.sendToClient("Please insert a valid option");
                    }
                } while(!c);
            }
            numberOfDice = -1;
            valueOfDice = -1;
            do{
                c = true;
                if(valueOfDice < 0 && !tempPlayer.isEliminated){
                    tempPlayer.myConnection.sendToClient("decide the value of the number you want to search (for the jolly write j) (MAX = " + maxDiceValue + ")");
                    tempMessage = tempPlayer.myConnection.receiveFromClient();
                    if(tempMessage.equals("j")){
                        valueOfDice = 1;
                    }
                    else{
                        try {
                            if(Integer.parseInt(tempMessage) <= maxDiceValue && Integer.parseInt(tempMessage) >= 2)
                                valueOfDice = Integer.parseInt(tempMessage);
                            else{
                                tempPlayer.myConnection.sendToClient("Error repeat");
                                c = false;
                            }
                        }
                        catch (Exception e){
                            valueOfDice = -1;
                            tempPlayer.myConnection.sendToClient("Error repeat");
                            c = false;
                        }
                    }
                }
                if(numberOfDice < 0  && !tempPlayer.isEliminated){
                    try{
                        tempPlayer.myConnection.sendToClient("decide what is the number of dice that has the value you input (it has to be less then how many are actually there do be correct)");
                        numberOfDice = Integer.parseInt(tempPlayer.myConnection.receiveFromClient());
                    }
                    catch (IOException e){
                        numberOfDice = -1;
                        tempPlayer.myConnection.sendToClient("Error repeat");
                        c = false;
                    }
                }
            } while(!c);
            if(!tempPlayer.isEliminated)
                broadcast(tempPlayer.username + " claims that there are at least " + numberOfDice + " with the value " + valueOfDice);
            do{
                if(index == playerList.size()-1) index = 0;
                else index++;
            } while(playerList.get(index).isEliminated);
            previousPlayer = tempPlayer;
        } catch (IOException e){
            tempPlayer.myConnection.disconnect();
        }
    }

    private boolean dudo(int diceValue, int numberOfDice){
        if(count(diceValue) < numberOfDice) return false;
        else return true;
    }

    private Player getPrevious(){
        int i = index;
        do{
            if(i == 0) i = playerList.size()-1;
            else i--;
        } while(playerList.get(i).isEliminated);
        return playerList.get(i);
    }

    private void reStart() throws IOException {
        rollAll();
        addResults();
        printDicePrivate();
    }
}
