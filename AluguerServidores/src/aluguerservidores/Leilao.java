package aluguerservidores;

import java.io.BufferedWriter;
import java.util.ArrayList;

public class Leilao {
    private WriterMap writers;
    private String maxbid_user;
    private float max_bid;
    private float bid_limit;
    private String server_type;
    private ArrayList<String> users_leilao;

    public Leilao(String stype){
        if (stype.equals("small.1k")){
            this.bid_limit= 125.01f;
        } else{
            this.bid_limit = 624.99f;
        }
        this.writers = new WriterMap();
        this.max_bid=0;
        this.maxbid_user="";
        this.server_type= stype;
        this.users_leilao = new ArrayList<>();
    }

    public synchronized void add_write(String user_email, BufferedWriter output){
        this.users_leilao.add(user_email);
        this.writers.add(user_email, output);
    }

    public synchronized float get_bidLimit(){
        return this.bid_limit;
    }

    public synchronized ArrayList<String> getUsers_leilao(){
        return this.users_leilao;
    }

    public synchronized void set_bid(float k){
        this.max_bid=k;
    }

    public synchronized float get_bid(){
        return this.max_bid;
    }

    public synchronized String getServer_type(){
        return this.server_type;
    }

    public synchronized void setMaxbid_user(String user_email){
        this.maxbid_user=user_email;
    }
    public synchronized String getMaxbid_user(){
        return this.maxbid_user;
    }

    public synchronized void endLeilao(){
        this.writers.writeWinner(this.maxbid_user, "O leilão acabou! Obrigado por participar.");
    }

    public synchronized void new_bid(float price, String user_email){
        this.max_bid=price;
        this.maxbid_user=user_email;
        this.writers.writeBid(user_email,"Última oferta: " + price);
    }
}
