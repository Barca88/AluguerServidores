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
public class ServerList {

    private final ArrayList<ServerToRent> servers;

    public ServerList() {
        this.servers = new ArrayList<>();
    }

    public synchronized void addServers(ServerToRent server) {
            this.servers.add(server);
    }

    public synchronized void removeServer(ServerToRent server) {
        this.servers.remove(server);
    }
    
    
}
