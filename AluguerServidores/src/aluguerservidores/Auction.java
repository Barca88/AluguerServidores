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
public class Auction extends Thread {

    private HashMap<String, BufferedWriter> writers;
    ArrayList<String> participants;
    private Catalogue catalogue;
    private String type;
    private float highestBid;
    private final float serverPrice;
    private String currentHighestBidder;
    //1 - acabou o tempo; 2 - preço >= do que o nominal; 3 - falta de servidores
    private int finished;
    AuctionManager manager;

    public Auction(Catalogue c, String type, AuctionManager manager) {
        this.writers = new HashMap<>();
        this.participants = new ArrayList<>();
        this.catalogue = c;
        this.serverPrice = this.catalogue.getNominalPrice(type);
        this.finished = 0;
        this.type = type;
        this.highestBid = 0;
        this.manager = manager;
    }

    public synchronized void addParticipant(String s, BufferedWriter b) {
        this.writers.put(s, b);
        this.participants.add(s);
    }

    public synchronized void removeParticipant(String s) {
        this.writers.remove(s);
        this.participants.remove(s);
    }

    public synchronized void sendMessage(String s, String user) {
        BufferedWriter output = this.writers.get(user);
        try {
            output.write(s);
            output.newLine();
            output.flush();
        } catch (IOException ex) {
        }
    }

    public synchronized void sendGeneralMessage(String s) {
        for (String user : participants) {
            sendMessage(s, user);
        }
    }

    public synchronized void sendBidMessage() {
        sendGeneralMessage("Oferta mais alta: " + this.highestBid + "\nFaça uma proposta: ");
    }
    
    public synchronized float getHighestBid(){
        return highestBid;
    }

    public synchronized void terminate(int cause) {
        this.finished = cause;
        notify();
    }
    
    public synchronized boolean isFinished(){
        return finished > 0;
    }

    public synchronized void endAuction() {
        Servers server;
        switch (this.finished) {
            case 1:
                server = catalogue.findAvailableServerOfType(type);
                if (server != null) {
                    server.set_occupied(true);
                    server.setUser_email(currentHighestBidder);
                    server.setBoughtInAuction(true);
                    server.setIndic_price(highestBid);
                    server.startServer();
                    this.sendGeneralMessage("Acabou o tempo. O vencedor é " + currentHighestBidder + "\n");
                } else {
                    this.sendGeneralMessage("Deu-se o caso improvável de o último servidor disponível este tipo ter sido requisitado no último minuto...\n");
                }
                break;
            case 2:
                server = catalogue.findAvailableServerOfType(type);
                if (server != null) {
                    server.set_occupied(true);
                    server.setUser_email(currentHighestBidder);
                    server.setBoughtInAuction(true);
                    server.setIndic_price(serverPrice);
                    server.startServer();
                    this.sendGeneralMessage("O leilão acabou devido a uma oferta igual ou superior ao valor nominal do servidor. O vencedor é " + currentHighestBidder + "\n");
                } else {
                    this.sendGeneralMessage("Lamentamos, mas o leilão foi cancelado dado que todos os servidores disponíveis deste tipo foram requisitados.\n");
                }
                break;
            case 3:
                this.sendGeneralMessage("Lamentamos, mas o leilão foi cancelado dado que todos os servidores disponíveis deste tipo foram requisitados.\n");
                break;
            default:
                break;
        }
            this.manager.removeAuction(type);
            try{
                this.join();
            } catch(InterruptedException e){
                e.printStackTrace();
            }
    }

    public synchronized void bid(String user, float price) {
        if (price >= this.serverPrice) {
            this.currentHighestBidder = user;
            this.terminate(2);
        }
        if (price > this.highestBid) {
            this.highestBid = price;
            this.currentHighestBidder = user;
            this.notify();
        }
    }

    public synchronized void standBy() throws InterruptedException {
        wait();
    }

    public void run() {
        while (!isFinished()) {
            try {
                sendBidMessage();
                standBy();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        endAuction();
    }
}
