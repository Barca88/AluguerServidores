package aluguerservidores;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

public class Servers  extends Thread{
    String id;
    private String type;
    private float nominal_price;
    private float indic_price;
    private float minutes;
    private boolean ocupied;
    private boolean auctioned;
    private String user_email;

    public Servers (String type, float n_price, float i_price, boolean auct, String user ) throws NoSuchAlgorithmException {
        this.id=this.create_id();
        this.type=type;
        this.nominal_price= n_price;
        this.indic_price = i_price;
        this.minutes=0;
        this.user_email =user;
        this.ocupied=true;
        this.auctioned=auct;
    }

    public Servers(String type, float n_price) throws NoSuchAlgorithmException {
        this.id = this.create_id();
        this.type=type;
        this.nominal_price=n_price;
        this.indic_price =0;
        this.minutes=0;
        this.ocupied=false;
        this.auctioned=false;
        this.user_email="";
    }

    public synchronized void set_minutes(float m){
        this.minutes=m;
    }
    public synchronized float get_minutes(){
        return this.minutes;
    }

    public synchronized void setUser_email(String x){this.user_email=x;}
    public synchronized String getUser_email(){ return this.user_email;}

    public synchronized void inc_minutes(){ this.minutes++;}
    public synchronized void set_type(String type){
        this.type=type;
    }
    public synchronized String get_type(){
        return this.type;
    }

    public synchronized String get_id(){
        return this.id;
    }
    public synchronized void set_id(String id){
        this.id=id;
    }

    public  synchronized String create_id() throws NoSuchAlgorithmException {
        Random r = new Random();
        String pk = "SLKNGLRKJBHTGLJRTHEBLE" + Integer.toString(r.nextInt(50000));
        String myHash = Base64.getEncoder().encodeToString(pk.getBytes());
        return myHash;
    }

    public synchronized float getNominal_price(){
        return this.nominal_price;
    }
    public synchronized void setNominal_price(float x){
        this.nominal_price=x;
    }

    public synchronized float getIndic_price(){
        return this.indic_price;
    }
    public synchronized void setIndic_price(float x){
        this.indic_price =x;
    }

    public synchronized void set_ocupied(boolean x){
        this.ocupied=x;
    }
    public synchronized boolean get_ocupied(){
        return this.ocupied;
    }

    public synchronized void set_auctioned(boolean x){ this.auctioned =x; }
    public synchronized boolean get_auctioned(){
        return this.auctioned;
    }

    public synchronized Servers clone(){return this.clone();}

    public void run() {
        while(get_ocupied()==true){
            try {
                sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (get_ocupied()== true){
                inc_minutes();
            }
        }
    }
}
