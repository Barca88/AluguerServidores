package aluguerservidores;

import java.util.ArrayList;

public class EmailList {

    private ArrayList<String> list;

    public EmailList() {
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
