package giocoClientServer.server;

import java.util.Random;

public class Dice {
    private int max;

    public Dice(int max) {
        this.max = max;
    }

    public int Throw() {
        return new Random().nextInt(1, max+1);
    }
}