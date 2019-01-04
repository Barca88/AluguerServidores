package aluguerservidores;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MyQueue {
    private HashMap<String,Queue<Account>> mapQueue;

    public MyQueue(List<String> l){
        this.mapQueue = new HashMap<>();
        for(String tipo : l) {
            Queue<Account> q = new LinkedList<>();
            mapQueue.put(tipo, q);
        }
    }

    public synchronized void addQueue(String tipo,Account a){
        if(mapQueue.containsKey(tipo)) {
            mapQueue.get(tipo).add(a);
        }
    }

    public synchronized Account getNext(String tipo){
        return mapQueue.get(tipo).remove();
    }

    public synchronized int size(String tipo){
        return mapQueue.get(tipo).size();
    }

    public synchronized void addNewTipo(String tipo){
        if(!this.mapQueue.containsKey(tipo)){
            Queue<Account> q = new LinkedList<>();
            mapQueue.put(tipo,q);
        }
    }
}