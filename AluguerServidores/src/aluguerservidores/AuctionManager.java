/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aluguerservidores;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author quim
 */
public class AuctionManager extends Thread {
    
    private HashMap<String, Auction> auctions;
    private HashMap<String, Integer> timers;
    private ArrayList<String> types;
    private Catalogue catalogue;
    private boolean hasActiveAuctions;
    private final int maxTime;
    
    public AuctionManager(Catalogue c) {
        this.auctions = new HashMap<>();
        this.timers = new HashMap<>();
        this.types = new ArrayList<>();
        this.catalogue = c;
        this.hasActiveAuctions = false;
        this.maxTime = 30;
    }
    
    public synchronized void createAuction(String type) {
        Auction auction = new Auction(catalogue, type, this);
        auction.start();
        this.auctions.put(type, auction);
        if (!this.types.contains(type)) {
            this.types.add(type);
        }
        timers.put(type, 0);
        hasActiveAuctions = true;
        notify();
    }
    
    public synchronized void removeAuction(String type) {
        this.auctions.remove(type);
        this.timers.remove(type);
        if (this.auctions.isEmpty()) {
            this.hasActiveAuctions = false;
        }
    }
    
    public boolean hasActiveAuction(String type) {
        return auctions.get(type) != null;
    }
    
    public synchronized void sendMessage(BufferedWriter output, String s) {
        try {
            output.write(s);
            output.newLine();
            output.flush();
        } catch (IOException ex) {
        }
    }
    
    
    public synchronized Auction joinAuction(String user, String type, BufferedWriter b) {
        if (catalogue.containsType(type)) {
            if (auctions.containsKey(type)) {
                auctions.get(type).addParticipant(user, b);
            } else {
                createAuction(type);
                auctions.get(type).addParticipant(user, b);
            }
            sendMessage(b, "Entrou no Leilão. \n Faltam " + (maxTime - timers.get(type)) + " segundos para terminar");
            return auctions.get(type);
        } else {
            sendMessage(b, "Não existem servidores do tipo pretendido\n");
            return null;
        }
    }
    
    public synchronized void standBy() throws InterruptedException {
        wait();
    }
    
    public void run() {
        int i;
        while (true) {
            while (hasActiveAuctions) {
                try {
                    sleep(1000);
                    for (String type : this.types) {
                        if (timers.get(type) != null) {
                            if (timers.get(type) >= maxTime) {
                                auctions.get(type).terminate(1);
                            } else {
                                i = timers.get(type);
                                i++;
                                timers.put(type, i);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                standBy();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
