package aluguerservidores;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Leiloes extends Thread{
    private Queue<Leilao> leiloes;

    public Leiloes(){
        this.leiloes = new LinkedList<>();
    }

    public synchronized void add_leilao(Leilao k){
        this.leiloes.add(k);
    }

    public synchronized void remove_Leilao(String leilao_type){
        this.leiloes.remove(leilao_type);
    }

    public synchronized Leilao getLeilao(String type){
        for(Leilao k: this.leiloes){
            if (k.getServer_type().equals(type)){
                return k;
            }
        }
        return null;
    }

    public synchronized ArrayList<Leilao> getLeiloes(){
        return (new ArrayList<Leilao>(this.leiloes));
    }

    public synchronized Boolean containsLeilao(Leilao k){
        return this.leiloes.contains(k);
    }

    public synchronized void endLeilao(Leilao k){
        for (Leilao i: this.leiloes) {
            if (i == k){
                i.endLeilao();
            }
        }
    }
    public void run(){
        try {
            Leilao k;
            while(this.leiloes.isEmpty() == false) {
                sleep(30000);
                if(this.leiloes.isEmpty() == false){
                    k = this.leiloes.peek();
                    if (k != null) {
                        this.endLeilao(k);
                        this.leiloes.remove(k);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
