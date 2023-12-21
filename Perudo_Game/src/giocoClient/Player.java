package giocoClient;

public class Player {
    public Results r;
    public String username;
    public int nDice;

    Player(String username, int nDice){
        r = new Results(6);
        this.username = username;
        this.nDice = nDice;
    }

    public void Throw(){
        r.Roll(nDice);
    }

    public String rToString(){
        return r.toString();
    }
}
