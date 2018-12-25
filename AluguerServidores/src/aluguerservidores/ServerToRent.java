/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aluguerservidores;

/**
 *
 * @author quim
 */
public class ServerToRent {

    private final String name;
    //número de servidores disponíveis
    private int number;
    //preço sem leilão
    private int price;

    public ServerToRent(String name, int number, int price) {
        this.name = name;
        this.number = number;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public int getPrice() {
        return price;
    }

    public synchronized void removeOne() {
        if (this.number > 0) {
            this.number--;
        }
    }

    public synchronized void remove(int n) {
        if (this.number < n) {
            this.number = 0;
        } else {
            this.number -= n;
        }
    }

    public synchronized void add(int n) {
        this.number += n;
    }

    public synchronized void setPrice(int n) {
        this.price = n;
    }
}
