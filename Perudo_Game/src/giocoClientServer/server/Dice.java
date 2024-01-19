package giocoClientServer.server;

import java.util.Random;

public class Dice {
    private final int max;      // maximum value of diec

    public Dice(int max) {
        this.max = max;
    }

    /**
     * throws the dice
     * @return the dice value
     */
    public int Throw() {
        return new Random().nextInt(1, max+1);
    }
}