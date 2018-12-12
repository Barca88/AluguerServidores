/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aluguerservidores;

import java.util.ArrayList;

/**
 *
 * @author quim
 */
public class emailList {

    private ArrayList<String> list;

    public emailList() {
        this.list = new ArrayList<>();
    }

    public synchronized boolean containsEmail(String email) {
        return this.list.contains(email);
    }

    public synchronized boolean addEmail(String email) {
        return this.list.add(email);
    }
    
    public synchronized boolean removeEmail(String email) {
        return this.list.remove(email);
    }
}
