package model;

import java.util.LinkedList;

public class Results {
    public LinkedList<Integer> results = new LinkedList<>();    // list of integer representing the results of the dice
    private final Dice d;                                       // generic dice class

    /**
     *
     * @param max Maximum value for the dice
     */
    Results(int max){
        d = new Dice(max);
    }

    /**
     * Rolls all the dices
     * @param nDice how many dice it should roll
     */
    public void Roll(int nDice){
        results.clear();
        for(int i = 0; i < nDice; i++) results.add(d.Throw());
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for(int r : results){
            if(r != 1)  // if the value is 1 it should print j representing the jolly
                s.append("| ").append(r).append(" | ");
            else
                s.append("| J | ");
        }
        return s.toString();
    }
}
