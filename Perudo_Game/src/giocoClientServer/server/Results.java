package giocoClientServer.server;

import java.util.LinkedList;

public class Results {
    public LinkedList<Integer> results = new LinkedList<>();
    private final Dice d;

    Results(int max){
        d = new Dice(max);
    }
    public void Roll(int nDice){
        results.clear();
        for(int i = 0; i < nDice; i++) results.add(d.Throw());
    }

    @Override
    public String toString() {
        String s = "";
        for(int r : results){
            if(r != 1)
                s += "| " + r + " | ";
            else
                s += "| J | ";
        }
        return s;
    }
}
