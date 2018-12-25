package aluguerservidores;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Catalogue {
    HashMap<String, Servers> server_catalogue;

    //there are 2 types:
    //type "large5k" --- 5000 € in 24hours
    //type "small1k" --- 1000 € in 24hours

    public Catalogue() throws NoSuchAlgorithmException {
        Random r = new Random();
        this.server_catalogue =  new HashMap<>();
        for (int i=0; i< 100; i++){
            if ((r.nextInt(2)%2)== 0){
                this.add_Server(new Servers("small1k", (float) 41.67));
            }
            else{this.add_Server(new Servers("large5k", (float) 208.33));}
        }
    }


    public synchronized void add_Server(Servers s){
        this.server_catalogue.put(s.id, s);
    }

    public synchronized Servers get_Server(String id){
        return this.server_catalogue.get(id);
    }
}
