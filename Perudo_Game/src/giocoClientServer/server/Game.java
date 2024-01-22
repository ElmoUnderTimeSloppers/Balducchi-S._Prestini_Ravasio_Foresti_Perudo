package giocoClientServer.server;

import java.io.IOException;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Random;

public class Game implements Runnable{

    int numberOfRestingPlayer = 0;      // Number of player which hasn't been eliminated
    int numberOfDice = -1;              // number of dice for the statement
    int valueOfDice = -1;               // value of dice for the statement
    int index = 0;                      // the index for the turn
    public boolean isPublic;            // says if the game is public
    public boolean hasStarted = false;  // says if the game has started
    private LinkedList<Integer> results = new LinkedList<>();   // list of results
    public int maxPlayer;               // maximum number of player
    public int minPlayer;               // minimum number of player
    public int maxDiceValue;            // max value of the dice
    public int startingDice;            // number of starting dice
    public String ID;                   // game ID
    boolean calza = false;
    private Player calzaPlayer;
    LinkedList<Player> playerList = new LinkedList<>();         // list of player

    public Game(int maxPlayer, int minPlayer, int maxDiceValue, int startingDice, Connection host, boolean isPublic) throws IOException {
        this.maxPlayer = maxPlayer;         //set the max of player in the lobby
        this.minPlayer = minPlayer;         //set the minimum
        this.maxDiceValue = maxDiceValue;   //set the of the value  of dices
        this.startingDice = startingDice;   //set the number of starting dice
        this.isPublic = isPublic;           //ask if the lobby is public or private
        ID = String.valueOf(new Random().nextInt(10000, 100000));   // randomize the game ID
        playerList.add(new Player(host.username, startingDice, maxDiceValue, host, this));  //when connected the server ask the username of the new client
        playerList.getFirst().myConnection.sendToClient("ID = " + ID);
        new Thread(this).start();
    }
    @Override
    public void run() {
        String message;
        try{
            //
            // Before the game starts
            //
            playerList.getFirst().myConnection.sendToClient("Write start to begin the game (you have to reach the minimum player)");
            do{
                try{
                    message = playerList.getFirst().myConnection.receiveFromClient();
                    if(message.equals("start") && minPlayer <= playerList.size()){  // if the host says "start" the game starts
                        hasStarted = true;
                    }
                }
                catch(SocketException e){
                    // if the host disconnect it changes to the next player if there are no player it removes the game from the list
                    playerList.getFirst().myConnection.disconnect();
                    playerList.removeFirst();
                    if(!playerList.isEmpty())
                        playerList.getFirst().myConnection.sendToClient("You are the new host");
                    else
                        Connection.removeGame(this);
                }
            }while(!hasStarted);
            //
            // Game starts here
            //
        } catch(Exception e){
            try {
                playerList.getFirst().myConnection.disconnect();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        index = new Random().nextInt(0,playerList.size()); // randomize the starting player
        try {
            reStart();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        numberOfRestingPlayer = playerList.size();
        // START

        for(Player p : playerList){
            p.startReceiving();
        }

        do{
            try{
                startNewTurn();
            }
            catch (IOException e){
                System.out.println("error");
            }
        } while(numberOfRestingPlayer > 1 && playerList.size() > 1);    // if the number of player is one then it finishes
        //
        //  THE END CHECKS FOR THE WINNING PLAYER
        //
        for(Player p : playerList)
        {
            if(!p.isEliminated) {
                try {
                    broadcast("the player " + p.username + " is the winner");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
    }

    /**
     * adds a player to the game
     * @param p the player to add
     * @throws IOException can happen
     */
    public void addPlayer(Player p) throws IOException {
        playerList.add(p);
        broadcast(p.username + " has connected, the player count is " + playerList.size() + "/" + maxPlayer);
    }

    /**
     * sends a message to every connected player
     * @param message the message to send
     * @throws IOException can happen
     */
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

    /**
     * rolls every players dices
     */
    private void rollAll(){
        for(Player p : playerList){
            p.Throw();
        }
        addResults();
    }

    /**
     * prints the results of the dices to every player privately (every player will see his dices)
     * @throws IOException can happen
     */
    private void printDicePrivate() throws IOException {
        for(Player p : playerList){
            try{
                p.myConnection.sendToClient(p.rToString());
            }
            catch (SocketException e){
                // if it can't send the message it means the client disconnected
                removePlayer(p);
            }

        }
    }

    /**
     * prints the results of the dices to every player (every player will see everyone's dices)
     * @throws IOException can happen
     */
    private void printDiceAll() throws IOException {
        for(Player p : playerList){
            broadcast(p.username + ": " + p.rToString());
        }
    }

    /**
     * adds the results of the dices to a list, used later for dudo
     */
    private void addResults(){
        results.clear();
        for(Player p : playerList){
            results.addAll(p.results.results);
        }
    }

    /**
     * counts how many number N dices are in the results
     * @param n the number to search
     * @return how many dices there are
     */
    private int count(int n){
        int c = 0;
        for(int r : results){
            if(r == n || r == 1){   // also checks for 1 since there is the jolly (or Paco)
                c++;
            }
        }
        return c;
    }

    /**
     * checks for Dudo
     * @param diceValue the value to search
     * @param numberOfDice the number of dices the player said
     * @return if true it means there were more dices with that value then what it was said, if false it means there were less
     */
    private boolean dudo(int diceValue, int numberOfDice){
        return count(diceValue) >= numberOfDice;
    }

    /**
     * return the previous player, used to remove dices if the dudo is false
     * @return the previous player
     */
    private Player getPrevious(){
        int i = index;
        do{
            if(i == 0) i = playerList.size()-1;
            else i--;
        } while(playerList.get(i).isEliminated);
        return playerList.get(i);
    }

    /**
     * restart, used at the start of the turn, it rolls everyone dices, adds the results, print the dices value privately
     * @throws IOException can happen
     */
    private void reStart() throws IOException {
        valueOfDice = -1;
        numberOfDice = -1;
        rollAll();
        addResults();
        printDicePrivate();
    }

    /**
     * increments the index
     */
    private void incrementIndex(){
        do{
            if(index >= playerList.size()-1) index = 0;
            else index++;
        } while(playerList.get(index).isEliminated);
    }

    /**
     * main function to the game, it starts a new turn
     * @throws IOException can happen
     */
    private void startNewTurn() throws IOException {
        calza = false;
        calzaPlayer = null;
        String tempMessage;         // used as a temporary string to get the message from the client
        String dudoOrNot;           // used to check if a player wants to dudo or not
        Player tempPlayer;          // the player whose turn it is
        Player previousPlayer = getPrevious();  // the previous player
        // Try to get the player whose turn it is
        try{
            tempPlayer = playerList.get(index);
        }
        catch (IndexOutOfBoundsException e){
            // just in case
            incrementIndex();
            tempPlayer = playerList.get(index);
        }
        tempPlayer.stopReceiving();//

        boolean c;          // used to see if the player can continue
        try{
            broadcast("it's the turn of: " + tempPlayer.username);
            tempPlayer.myConnection.sendToClient("It's your turn");
            if(valueOfDice > 0){        // if it's more than 0, it means it's at least the second turn (which is different from the first)
                //
                // turn that isn't the first
                //
                do{
                    c = true;
                    tempPlayer.myConnection.sendToClient("do you think the statement is correct?\n" + "1. Yes, it's correct\n" + "2. Dudo, it's not correct");
                    dudoOrNot = tempPlayer.myConnection.receiveFromClient();
                    if(calza){
                        Calza();
                        break;
                    }
                    else if(dudoOrNot.equals("2")){      // the player checks for dudo
                        dudo(tempPlayer, previousPlayer);
                        resetCalza();
                    }
                    else if(dudoOrNot.equals("1")){     // the player doesn't check for dudo
                        do{
                            c = true;
                            tempPlayer.myConnection.sendToClient("Ok, select if you want to increase the value or the number of the Dices \n" + "1. The value " + "(" + getValueCorrect(valueOfDice) + ")" + "\n" + "2. The number " + "(" + numberOfDice + ")");
                            tempMessage = tempPlayer.myConnection.receiveFromClient();
                            if(tempMessage.equals("1")){        // change the value
                                selectValue(tempPlayer);
                            }
                            else if(tempMessage.equals("2")){   // change the number
                                selectNumber(tempPlayer);
                            }
                            else{
                                // error
                                c = false;
                                tempPlayer.myConnection.sendToClient("Please insert a valid option");
                            }
                        } while(!c);
                        resetCalza();
                    }
                    else{
                        // error
                        c = false;
                        tempPlayer.myConnection.sendToClient("Please insert a valid option");
                    }
                } while(!c);
            }
            else{
                //
                // FIRST TURN
                //
                numberOfDice = -1;
                valueOfDice = -1;
                if(!tempPlayer.isEliminated){
                    selectValue(tempPlayer);
                }
                if(!tempPlayer.isEliminated){
                    selectNumber(tempPlayer);
                }
                resetCalza();

            }
            if(!tempPlayer.isEliminated && (valueOfDice > 0 && numberOfDice > 0) && !calza)
                broadcast(tempPlayer.username + " claims that there are at least " + numberOfDice + " with the value " + getValueCorrect(valueOfDice));

            tempPlayer.startReceiving();
            incrementIndex();
        } catch (SocketException e){
            // catch disconnection
            removePlayer(tempPlayer);
        }
    }

    /**
     * removes the player from the game
     * @param p the player to remove
     * @throws IOException can happen
     */
    public void removePlayer(Player p) throws IOException {
        playerList.remove(p);
        p.myConnection.disconnect();
        broadcast(p.username + " has disconnected");
        System.out.println(p.username + " has disconnected");
    }

    /**
     * used to select the value of the dice during the game
     * @param tempPlayer the player whose turn it is
     * @throws IOException can happen
     */
    private void selectValue(Player tempPlayer) throws IOException {
        String tempMessage;
        boolean c;
        do{
            c = true;
            tempPlayer.myConnection.sendToClient("decide the value of the number you want to search (for the jolly write j) (MAX = " + maxDiceValue + ")");
            tempMessage = tempPlayer.myConnection.receiveFromClient();
            if(tempMessage.equals("j") && numberOfDice > 0 && valueOfDice != 1){ // if the value is j it needs to set it has 1
                if(valueOfDice > 0){    // when you use jolly you have to reinsert the number of dice
                    numberOfDice = (numberOfDice/2) + (numberOfDice%2);
                    tempPlayer.myConnection.sendToClient("Since you use jolly as value for your statement you have to also decide the number of the dices, remember it has to be more than half of the previous one");
                    selectNumber(tempPlayer);
                }
                valueOfDice = 1;
            }
            else if(tempMessage.equals("j") && numberOfDice < 0){   // you cannot use jolly in the first turn
                tempPlayer.myConnection.sendToClient("You can't use the jolly at the start");
                c = false;
            }
            else if(tempMessage.equals("j") && valueOfDice == 1){
                tempPlayer.myConnection.sendToClient("You can't use the same value");
                c = false;
            }
            else{
                try {
                    if (Integer.parseInt(tempMessage) <= maxDiceValue && Integer.parseInt(tempMessage) >= 2 && Integer.parseInt(tempMessage) > valueOfDice){
                        if(valueOfDice == 1){   // if the value was a jolly you have to insert the number again
                            numberOfDice = numberOfDice*2;
                            tempPlayer.myConnection.sendToClient("Since the previous statement use the jolly as the value you also have to insert the number of dice to search, remember it has to be at least one over the last number of dices multiplied by 2");
                            selectNumber(tempPlayer);
                        }
                        valueOfDice = Integer.parseInt(tempMessage);
                    }
                    else if(valueOfDice == maxDiceValue){   // if the value is at his maximum you have to use jolly
                        tempPlayer.myConnection.sendToClient("The value is already at his maximum, you have to use jolly");
                        c = false;
                    }
                    else{
                        // error
                        tempPlayer.myConnection.sendToClient("invalid input");
                        c = false;
                    }
                }
                catch (NumberFormatException e){
                    // error
                    valueOfDice = -1;
                    tempPlayer.myConnection.sendToClient("Error repeat");
                    c = false;
                }
            }
        } while(!c);
    }

    /**
     * selects the number of dices for the statement
     * @param tempPlayer the player whose turn it is
     * @throws IOException can happen
     */
    private void selectNumber(Player tempPlayer) throws IOException {
        boolean c;
        String tempMessage;
        do{
            c = true;
            try{
                tempPlayer.myConnection.sendToClient("decide what is the number of dice that has the value (it has to be less then how many are actually there do be correct)");
                tempMessage = tempPlayer.myConnection.receiveFromClient();
                if(Integer.parseInt(tempMessage) > numberOfDice){   // if it's more then the last number of dices it's ok
                    numberOfDice = Integer.parseInt(tempMessage);
                }
                else{
                    // error
                    tempPlayer.myConnection.sendToClient("it has to be higher than " + numberOfDice);
                    c = false;
                }
            }
            catch (NumberFormatException e){
                // error
                numberOfDice = -1;
                tempPlayer.myConnection.sendToClient("Error repeat");
                c = false;
            }
        } while(!c);
    }

    /**
     * Used when a player claims dudo
     * @param tempPlayer the player whose turn it is
     * @param previousPlayer the last player
     * @throws IOException can happen
     */
    public void dudo(Player tempPlayer, Player previousPlayer) throws IOException {
        broadcast(tempPlayer.username + " claims dudo, lets check");
        printDiceAll();
        if(dudo(valueOfDice, numberOfDice)){    // if it's false
            broadcast("The dudo is false, " + tempPlayer.username + " gets one of his dice taken away");
            RemoveDice(tempPlayer);
        }
        else{                                   // if it's true
            broadcast("The dudo is true, " + previousPlayer.username + " gets one of his dice taken away");
            RemoveDice(previousPlayer);
        }
        reStart();
    }

    /**
     * removes a dice to the player
     * @param tempPlayer the player
     * @throws IOException can happen
     */
    public void RemoveDice(Player tempPlayer) throws IOException {
        tempPlayer.numberOfDice--;
        if(tempPlayer.numberOfDice<1){ // checks if eliminated
            broadcast(tempPlayer.username + " has finished his dices, he is eliminated");
            numberOfRestingPlayer--;
            tempPlayer.isEliminated = true;
        }
    }

    /**
     * used to print the correct value, for 1 is j for the other number it's just the number
     * @param i the value to check
     * @return the char that is correct
     */
    private String getValueCorrect(int i){
        if(i == 1) return "j";
        else return "" + i;
    }
    public void callCalza(Player calzaPlayer){
        calza = true;
        this.calzaPlayer = calzaPlayer;
    }

    public void resetCalza(){
        calza = false;
        this.calzaPlayer = null;
    }

    public void Calza() throws IOException {
        broadcast(calzaPlayer.username + " calls calza, let's check");
        printDiceAll();
        if(count(valueOfDice) == numberOfDice){
            broadcast("It's correct, " + calzaPlayer.username + " obtains a dice");
            calzaPlayer.numberOfDice++;
        }
        else{
            broadcast("It isn't correct, " + calzaPlayer.username + " loses a dice");
            calzaPlayer.numberOfDice--;
        }
        reStart();
    }
}
