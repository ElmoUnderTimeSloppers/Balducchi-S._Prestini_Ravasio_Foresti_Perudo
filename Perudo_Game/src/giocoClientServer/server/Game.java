package giocoClientServer.server;

import java.io.IOException;
import java.net.SocketException;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.Random;

public class Game implements Runnable{

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
        this.maxPlayer = maxPlayer;         //set the max of player in the lobby
        this.minPlayer = minPlayer;         //set the minimum
        this.maxDiceValue = maxDiceValue;   //set the of the value  of dices
        this.startingDice = startingDice;
        this.isPublic = isPublic;           //ask if the lobby is public or private
        ID = String.valueOf(new Random().nextInt(10000, 100000));
        playerList.add(new Player(host.username, startingDice, maxDiceValue, this, host));  //when connected the server ask the username of the new client
        playerList.getFirst().myConnection.sendToClient("ID = " + ID);
        new Thread(this).start();
    }
    @Override
    public void run() {
        String message;
        try{
            playerList.getFirst().myConnection.sendToClient("Write start to begin the game (you have to reach the minimum player)");
            do{
                try{
                    message = playerList.getFirst().myConnection.receiveFromClient();
                    if(message.equals("start") && minPlayer <= playerList.size()){
                        hasStarted = true;
                    }
                }
                catch(SocketException e){
                    playerList.getFirst().myConnection.disconnect();
                    playerList.removeFirst();
                    if(!playerList.isEmpty())
                        playerList.getFirst().myConnection.sendToClient("You are the new host");
                    else
                        Connection.removeGame(this);
                }
            }while(!hasStarted);
        } catch(Exception e){
            try {
                playerList.getFirst().myConnection.disconnect();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        index = new Random().nextInt(0,playerList.size());
        try {
            reStart();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        numberOfRestingPlayer = playerList.size();
        do{
            try{
                startNewTurn();
            }
            catch (IOException e){
                System.out.println("MMMH");
            }
        } while(numberOfRestingPlayer > 1 && playerList.size() > 1);
        for(Player p : playerList)
        {
            if(!p.isEliminated) {
                try {
                    broadcast("the player " + p.username + " is the winner");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public void addPlayer(Player p) throws IOException {
        playerList.add(p);
        broadcast(p.username + " has connected, the player count is " + playerList.size() + "/" + maxPlayer);
    }

    private void broadcast(String message) throws IOException {
        for (Player p : playerList) {
            try{
                p.myConnection.sendToClient(message);
            }
            catch(SocketException e) {
                removePlayer(p);
            }
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
            try{
                p.myConnection.sendToClient(p.rToString());
            }
            catch (SocketException e){
                removePlayer(p);
            }

        }
    }
    private void printDiceAll() throws IOException {
        for(Player p : playerList){
            broadcast(p.rToString());   // Ravasio questa parte di codice non si modificherà da sola
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
        valueOfDice = -1;
        numberOfDice = -1;
        rollAll();
        addResults();
        printDicePrivate();
    }

    private void incrementIndex(){
        do{
            if(index >= playerList.size()-1) index = 0;
            else index++;
        } while(playerList.get(index).isEliminated);
    }

    private void startNewTurn() throws IOException {
        String tempMessage = "";
        String dudoOrNot = "";
        Player tempPlayer;
        Player previousPlayer = getPrevious();
        try{
            tempPlayer = playerList.get(index);
        }
        catch (IndexOutOfBoundsException e){
            incrementIndex();
            tempPlayer = playerList.get(index);
        }

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
                        dudo(tempPlayer, previousPlayer);
                    }
                    else if(dudoOrNot.equals("1")){
                        do{
                            c = true;
                            tempPlayer.myConnection.sendToClient("Ok, select if you want to increase the value or the number of the Dices \n" + "1. The value " + "(" + valueOfDice + ")" + "\n" + "2. The number " + "(" + numberOfDice + ")");
                            tempMessage = tempPlayer.myConnection.receiveFromClient();
                            if(tempMessage.equals("1")){
                                selectValue(tempPlayer);
                            }
                            else if(tempMessage.equals("2")){
                                selectNumber(tempPlayer);
                            }
                            else{
                                c = false;
                                tempPlayer.myConnection.sendToClient("Please insert a valid option");
                            }
                        } while(!c);
                    }
                    else{
                        c = false;
                        tempPlayer.myConnection.sendToClient("Please insert a valid option");
                    }
                } while(!c);
            }
            else{
                numberOfDice = -1;
                valueOfDice = -1;
                if(!tempPlayer.isEliminated){
                    selectValue(tempPlayer);
                }
                if(!tempPlayer.isEliminated){
                    selectNumber(tempPlayer);
                }

            }
            if(!tempPlayer.isEliminated && valueOfDice > 1)
                broadcast(tempPlayer.username + " claims that there are at least " + numberOfDice + " with the value " + valueOfDice);
            else if(valueOfDice == 1)
                broadcast(tempPlayer.username + " claims that there are at least " + numberOfDice + " with the value " + "j");
            incrementIndex();
        } catch (SocketException e){
            removePlayer(tempPlayer);
        }
    }

    private void removePlayer(Player p) throws IOException {
        playerList.remove(p);
        p.myConnection.disconnect();
        broadcast(p.username + " has disconnected");
        System.out.println(p.username + " has disconnected");
    }

    private void selectValue(Player tempPlayer) throws IOException {
        String tempMessage;
        boolean c = true;
        do{
            c = true;
            tempPlayer.myConnection.sendToClient("decide the value of the number you want to search (for the jolly write j) (MAX = " + maxDiceValue + ")");
            tempMessage = tempPlayer.myConnection.receiveFromClient();
            if(tempMessage.equals("j")){
                if(valueOfDice > 0){
                    numberOfDice = (int)(numberOfDice/2) + 1;
                    tempPlayer.myConnection.sendToClient("Since you use jolly as value for your statement you have to also decide the number of the dices, remember it has to be more than half of the previous one");
                    selectNumber(tempPlayer);
                }
                valueOfDice = 1;
            }
            else{
                try {
                    if (Integer.parseInt(tempMessage) <= maxDiceValue && Integer.parseInt(tempMessage) >= 2 && Integer.parseInt(tempMessage) > valueOfDice){
                        if(valueOfDice == 1){
                            numberOfDice = numberOfDice*2;
                            tempPlayer.myConnection.sendToClient("Since the previous statement use the jolly as the value you also have to insert the number of dice to search, remember it has to be at least one over the last number of dices multiplied by 2");
                            selectNumber(tempPlayer);
                        }
                        valueOfDice = Integer.parseInt(tempMessage);
                    }
                    else if(valueOfDice == maxDiceValue){
                        tempPlayer.myConnection.sendToClient("The value is already at his maximum, you have to use jolly");
                        c = false;
                    }
                    else{
                        tempPlayer.myConnection.sendToClient("invalid input");
                        c = false;
                    }
                }
                catch (NumberFormatException e){
                    valueOfDice = -1;
                    tempPlayer.myConnection.sendToClient("Error repeat");
                    c = false;
                }
            }
        } while(!c);
    }

    private void selectNumber(Player tempPlayer) throws IOException {
        boolean c = true;
        String tempMessage;
        do{
            c = true;
            try{
                tempPlayer.myConnection.sendToClient("decide what is the number of dice that has the value (it has to be less then how many are actually there do be correct)");
                tempMessage = tempPlayer.myConnection.receiveFromClient();
                if(Integer.parseInt(tempMessage) > numberOfDice){
                   numberOfDice = Integer.parseInt(tempMessage);
                }
                else{
                    tempPlayer.myConnection.sendToClient("it has to be higher than " + numberOfDice);
                    c = false;
                }
            }
            catch (NumberFormatException e){
                numberOfDice = -1;
                tempPlayer.myConnection.sendToClient("Error repeat");
                c = false;
            }
        } while(!c);
    }
    public void dudo(Player tempPlayer, Player previousPlayer) throws IOException {
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


}
