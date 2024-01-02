package giocoClient;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class Game {
    static LinkedList<Player> playerList = new LinkedList<>();
    static LinkedList<Integer> results = new LinkedList<>();
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        int nDice = -1;
        int nSearch;
        System.out.println("Inserisci il numero da cercare (per i jolly scrivere J)");
        try{
            nSearch = Integer.parseInt(s.nextLine());
        }
        catch (Exception e){
            nSearch = 1;
        }
        System.out.println("Inserisci il numero di dadi");
        nDice = Integer.parseInt(s.nextLine());
        playerList.add(new Player("eda27", 5));
        playerList.add(new Player("Samuele", 5));
        playerList.add(new Player("Jacopo", 5));
        playerList.add(new Player("Andrea", 5));
        rollAll();
        //printAll();
        int index = new Random().nextInt(1,playerList.size());
        while (true) {
            if(nDice != -1)
            {

            }
        }
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
            if(r == n || r == 1){
                c++;
            }
        }
        return c;
    }


}
