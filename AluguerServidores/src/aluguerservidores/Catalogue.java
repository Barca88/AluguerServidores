package aluguerservidores;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Catalogue {

    HashMap<String, Servers> server_catalogue;
    HashMap<String, Float> nominalPrices;
    HashMap<String, Float> bidPrices;
    ArrayList<String> types;

    //there are 2 types:
    //type "large.5k" --- 5000 € in 24hours
    //type "small.1k" --- 1000 € in 24hours
    public Catalogue() throws NoSuchAlgorithmException {
        Random r = new Random();
        this.server_catalogue = new HashMap<>();
        this.nominalPrices = new HashMap<>();
        this.bidPrices = new HashMap<>();
        this.types = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            if ((r.nextInt(2) % 2) == 0) {
                this.addServer(new Servers("small.1k", (float) 41.67));
            } else {
                this.addServer(new Servers("large.5k", (float) 208.33));
            }
        }
    }

    public synchronized void addServer(Servers s) {
        String id = s.getIdServers();
        String type = s.getType();
        float price = s.getNominalPrice();
        this.server_catalogue.put(id, s);
        if (!this.types.contains(type)) {
            this.types.add(type);
        }
        if (this.nominalPrices.get(type) == null) {
            this.nominalPrices.put(type, price);
        }
        if (this.bidPrices.get(type) == null) {
            this.bidPrices.put(type, (float) 0);
        }
    }

    public synchronized Servers findAvailableServerOfType(String type) {
        ArrayList<Servers> list = makeServerList();
        for (Servers server : list) {
            if (server.getType().equals(type) && !server.isOccupied()) {
                return server;
            }
        }
        return null;
    }

    public synchronized Servers findOccupiedAuctionedServerOfType(String type) {
        ArrayList<Servers> list = makeServerList();
        for (Servers server : list) {
            if (server.getType().equals(type) && server.isOccupied() && server.wasBoughtInAuction()) {
                return server;
            }
        }
        return null;
    }

    public synchronized ArrayList<String> getTypes() {
        return types;
    }

    public synchronized boolean containsKey(String s) {
        return server_catalogue.containsKey(s);
    }

    public synchronized Servers getServer(String s) {
        return server_catalogue.get(s);
    }

    public synchronized ArrayList<Servers> makeServerList() {
        ArrayList<Servers> list = new ArrayList<>(server_catalogue.values());
        return list;
    }

    public synchronized float getNominalPrice(String s) {
        return this.nominalPrices.get(s);
    }
    
    public synchronized float getBidPrice(String s) {
        return this.bidPrices.get(s);
    }

    public synchronized Servers get_Server(String id) {
        return this.server_catalogue.get(id);
    }
}
