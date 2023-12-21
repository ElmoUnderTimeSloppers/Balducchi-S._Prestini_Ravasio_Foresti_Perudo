package giocoClient;

import java.util.LinkedList;
import java.util.Scanner;

public class Game {
    static LinkedList<Player> playerList = new LinkedList<>();
    static LinkedList<Integer> results = new LinkedList<>();
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        playerList.add(new Player("eda27", 5));
        playerList.add(new Player("Samuele", 5));
        playerList.add(new Player("Jacopo", 5));
        playerList.add(new Player("Andrea", 5));
        rollAll();
        printAll();
    }

    private static void rollAll(){
        for(Player p : playerList){
            p.Throw();
        }
        addResults();
    }

    private static void printAll(){
        for(Player p : playerList){
            System.out.println(p.rToString());
        }
    }

    private static void addResults(){
        results.clear();
        for(Player p : playerList){
            results.addAll(p.r.results);
        }
    }

    private static int count(int n){
        int c = 0;
        for(int r : results){
            if(r == n){
                c++;
            }
        }
        return c;
    }
}
