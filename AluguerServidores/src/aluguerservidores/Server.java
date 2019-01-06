package aluguerservidores;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private final ServerSocket serverSocket;
    private ArrayList<ServerThread> clients;
    private AccountsMap accounts;
    private EmailList loggedIn;
    private Catalogue catalogue;
    private MyQueue queue;
    private WriterMap writers;
    private AuctionManager auctionManager;
    private Lock accountsLock;

    public Server() throws IOException, NoSuchAlgorithmException {
        this.serverSocket = new ServerSocket(12345);
        this.clients = new ArrayList<>();
        this.accounts = new AccountsMap();
        this.accountsLock = new ReentrantLock();
        this.loggedIn = new EmailList();
        this.catalogue = new Catalogue();
        this.writers = new WriterMap();
        this.auctionManager = new AuctionManager(catalogue);
        this.queue = new MyQueue(catalogue.getTypes());
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        Server servidor = new Server();
        servidor.getInput();
    }

    private void getInput() throws IOException {
        auctionManager.start();
        while (true) {
            try {
                Socket clSocket = serverSocket.accept();
                ServerThread st = new ServerThread(clSocket);
                this.clients.add(st);
                st.start();
                /*System.out.println("ClientThread " + st.getId() + "started\n");
                st.join();
                this.clients.remove(st);
                System.out.println("ClientThread " + st.getId() + "finished\n");*/
            } catch (Exception e) {
            }
        }
    }

    private class ServerThread extends Thread {

        private BufferedWriter output;
        private BufferedReader input;
        public String myEmail;

        private ServerThread(Socket clSocket) {
            try {
                input = new BufferedReader(new InputStreamReader(clSocket.getInputStream()));
                output = new BufferedWriter(new OutputStreamWriter(clSocket.getOutputStream()));
                myEmail = "";
            } catch (IOException ex) {
            }
        }

        private void sendMessage(String textInput) {
            try {
                output.write(textInput);
                output.newLine();
                output.flush();
            } catch (IOException ex) {
            }
        }

        private int startMenu() throws IOException {
            this.sendMessage("\n1 - Login \n2 - Registar \nquit para sair");
            String answer = input.readLine();
            if (answer.equalsIgnoreCase("quit")) {
                return -1;
            } else if (answer.equals("2")) {
                int singupResult = signupPrompt();
                return singupResult;
            } else if (answer.equals("1")) {
                int loginResult = loginPrompt();
                return loginResult;
            } else {
                return 0;
            }
        }

        private int mainPage() throws IOException, InterruptedException {
            ArrayList<Servers> catalogue_list = catalogue.makeServerList();
            int status = 0;
            String answer = "";
            while (status == 0) {
                this.sendMessage("\n1 - Listar Catálogo \n2 - Reservar servidor \n3 - Meus Servidores \n4 - Libertar servidor \n5 - Log Out");
                answer = input.readLine();
                switch (answer) {
                    case "1":
                        this.sendMessage(this.listCatalogue());
                        break;
                    case "2":
                        this.sendMessage(this.request_Server());
                        break;
                    case "3":
                        this.sendMessage(this.listMyServers());
                        break;
                    case "4":
                        this.sendMessage(this.liberateServer());
                        break;
                    case "5":
                        loggedIn.removeEmail(myEmail);
                        writers.remove(myEmail);
                        status = 1;
                        break;
                    default:
                        break;
                }
            }
            return 0;
        }

        private String listCatalogue() throws IOException {
            String response = listFreeServers() + listOccupiedServers();
            return response;
        }

        /*private String list_catalogue() throws IOException {
            this.sendMessage("\n1 - Listar servidores livres \n2 - Listar servidores ocupados \n3 - Listar servidores para leilão \n4 - Listar servidores alugados por mim \n5 - Listar todos os servidores do tipo \"large.5k\" \n6 - Listar todos os servidores do tipo \"small.1k\"\n7- \"Para sair\"\n ");
            String answer = input.readLine();
            String response = "";
            ArrayList<Servers> catalogue_list = catalogue.makeServerList();
            switch (answer) {
                case "1":
                    response = listFreeServers();
                    break;
                case "2":
                    for (Servers server : catalogue_list) {
                        if (server.isOccupied()) {
                            response = "Id do Servidor: " + server.getIdServers() + " \n\t-- Tipo: " + server.getType() + " \n\t-- Preço nominal:" + (String.valueOf(server.getNominalPrice())) + " \n\t-- Preço indicado:" + (String.valueOf(server.getIndicPrice())) + " \n\t-- Servidor Ocupado:" + (String.valueOf(server.isOccupied())) + " \n\t-- Servidor Leiloado:" + (String.valueOf(server.isInAuction())) + "\n\n";
                        }
                    }
                    break;
                case "3":
                    for (Servers server : catalogue_list) {
                        if (server.isInAuction() && !server.isOccupied()) {
                            response = "Id do Servidor: " + server.getIdServers() + " \n\t-- Tipo: " + server.getType() + " \n\t-- Preço nominal:" + (String.valueOf(server.getNominalPrice())) + " \n\t-- Última oferta:" + (String.valueOf(server.getIndicPrice())) + " \n\t-- Servidor Leiloado:" + (String.valueOf(server.isInAuction())) + "\n\n";
                        }
                    }
                    break;
                case "4":
                    for (Servers server : catalogue_list) {
                        if (server.getUserEmail().equals(myEmail)) {
                            response = "Id da Reserva: " + server.getIdServers() + " \n\t-- Tipo: " + server.getType() + " \n\t-- Preço nominal:" + (String.valueOf(server.getNominalPrice())) + " \n\t-- Preço indicado:" + (String.valueOf(server.getIndicPrice())) + "\nMinutos ativo: " + (String.valueOf(server.getMinutes())) + "\nTotal a pagar: " + (String.valueOf(server.getCurrentTotal())) + "\n\n";
                        }
                    }
                    break;
                case "5":
                    for (Servers server : catalogue_list) {
                        if (server.getType().equals("large.5k")) {
                            response = "Id do Servidor: " + server.getIdServers() + " \n\t-- Tipo: " + server.getType() + " \n\t-- Preço nominal:" + (String.valueOf(server.getNominalPrice())) + " \n\t-- Preço indicado:" + (String.valueOf(server.getIndicPrice())) + " \n\t-- Servidor Ocupado:" + (String.valueOf(server.isOccupied())) + " \n\t-- Servidor Leiloado:" + (String.valueOf(server.isInAuction())) + "\n\n";
                        }
                    }
                    break;
                case "6":
                    for (Servers server : catalogue_list) {
                        if (server.getType().equals("small.1k")) {
                            response = "Id do Servidor: " + server.getIdServers() + " \n\t-- Tipo: " + server.getType() + " \n\t-- Preço nominal:" + (String.valueOf(server.getNominalPrice())) + " \n\t-- Preço indicado:" + (String.valueOf(server.getIndicPrice())) + " \n\t-- Servidor Ocupado:" + (String.valueOf(server.isOccupied())) + " \n\t-- Servidor Leiloado:" + (String.valueOf(server.isInAuction())) + "\n\n";
                        }
                    }
                    break;
                default:
                    break;
            }

            return response;
        }*/
        private String request_Server() throws IOException, InterruptedException {
            this.sendMessage(listFreeServers());
            this.sendMessage("\n1 - Reservar servidor pelo preço nominal \n2 - Propor oferta de preço em leilão");
            String answer = input.readLine();
            String response = "";
            switch (answer) {
                case "1":
                    response = this.rentServer();
                    break;
                case "2":
                    response = this.serverAuction();
                    break;
                default:
                    break;
            }
            return response;
        }

        private String rentServer() throws IOException, InterruptedException {
            ArrayList<String> typeList = catalogue.getTypes();
            String message = "";
            int i = 1;
            for (String type : typeList) {
                message += i + " - Alugar servidor to tipo " + type + "\n";
                i++;
            }
            this.sendMessage(message);

            String answer = input.readLine();
            int n;

            try {
                n = Integer.parseInt(answer);
            } catch (Exception e) {
                return "Comando inválido";
            }

            if (n > typeList.size() || n < 0) {
                return "Comando inválido";
            }
            String tipo = typeList.get(n - 1);
            Servers requested = catalogue.findAvailableServerOfType(tipo);
            if (requested == null) {
                requested = catalogue.findOccupiedAuctionedServerOfType(tipo);
                if (requested != null) {
                    String oldUserMail = requested.getUserEmail();
                    Account oldUser = accounts.getAccount(oldUserMail);
                    requested.reset();
                    requested.setOccupied(true);
                    requested.setUserEmail(myEmail);
                    writers.writeMessage(oldUserMail, "Lamentamos, mas a reserva que obteve em leilão, cuja identificação é: " + requested.getIdServers()
                            + " teve de ser cedida para uma reserva pelo preço nominal, por falta de disponibilidade de servidores.");
                    return " Este é o identificador da reserva: " + requested.getIdServers() + "\n";
                } else {
                    return "Não há servidores disponiveis";
                 /*   this.sendMessage( "Neste momento não há servidores disponíveis do tipo pretendido\nPretende eentrar para a Lista de Espera?\n1 - Sim\n2 - Não");
                    answer = input.readLine();
                    try {
                        n = Integer.parseInt(answer);
                    } catch (Exception e) {
                        return "Comando inválido";
                    }
                    switch(n){
                        case 1:
                            queue.addQueue(tipo,Gajo);// <-- o que ponho ali? //TODO
                            return "Estas na Fila de Espera!\n";
                        case 2:
                            return "Não quer Esperar!\n"
                        default:
                            return "Default\n"
                    }

                    //TODO Tem que se preguntar se ele quer entrar para a lista de espera?
                    */
                }
            } else {
                requested.set_occupied(true);
                requested.setUser_email(myEmail);
                requested.startServer();
                return "Este é o identificador da reserva: " + requested.get_id() + "\n";
            }
        }

        private String serverAuction() throws IOException {
            ArrayList<String> typeList = catalogue.getTypes();
            String serverType;
            String message = "";
            int i = 1;
            for (String type : typeList) {
                message += i + " - Propor oferta para servidor to tipo " + type + "\n";
                i++;
            }
            this.sendMessage(message);
            String answer = input.readLine();
            int n;

            try {
                n = Integer.parseInt(answer);
            } catch (Exception e) {
                return "Comando inválido\n";
            }

            if (n > typeList.size() || n < 0) {
                return "Comando inválido\n";
            }

            serverType = typeList.get(n - 1);

            Auction auction = auctionManager.joinAuction(myEmail, serverType, output);

            while (!auction.isFinished()) {
                answer = input.readLine();
                if (answer.equals("quit")) {
                    if (auction.getCurrentHighestBidder().equals(myEmail)) {
                        sendMessage("Não pode sair do leilão dado que é o atual maior licitador");
                    } else {
                        auction.removeParticipant(myEmail);
                        return "Saiu do leilão";
                    }
                } else if (!auction.isFinished()) {
                    try {
                        n = Integer.parseInt(answer);
                        auction.bid(myEmail, n);
                    } catch (Exception e) {
                        sendMessage("Comando inválido\n");
                    }
                }
            }
            return ("\n");
        }

        private int nFreeServers(String type) {
            ArrayList<Servers> catalogue_list = catalogue.makeServerList();
            ArrayList<String> typeList = catalogue.getTypes();
            if (!typeList.contains(type)) {
                return -1;
            } else {
                int i = 0;
                for (Servers server : catalogue_list) {
                    if (!server.isOccupied() && server.getType() == type) {
                        i++;
                    }
                }
                return i;
            }
        }

        private String listMyServers() {
            ArrayList<Servers> catalogue_list = catalogue.makeServerList();
            String response = "";
            for (Servers server : catalogue_list) {
                if (server.getUser_email().equals(myEmail)) {
                    response = response + "Id da Reserva: " + server.get_id() + " \n\t-- Tipo: " + server.get_type() + " \n\t-- Preço nominal:" + (new String(String.valueOf(server.getNominal_price()))) + " \n\t-- Preço indicado:" + (new String(String.valueOf(server.getIndic_price()))) + "\nMinutos ativo: " + (new String(String.valueOf(server.getMinutes()))) + "\nTotal a pagar: " + (new String(String.valueOf(server.getCurrentTotal()))) + "\n\n";
                }
            }
            return response;
        }

        private String listFreeServers() {
            String response = "Servidores livres:\n";
            ArrayList<Servers> catalogue_list = catalogue.makeServerList();
            ArrayList<String> typeList = catalogue.getTypes();
            int[] ntype = new int[typeList.size()];
            int i;
            for (Servers server : catalogue_list) {
                if (!server.isOccupied()) {
                    i = 0;
                    for (String type : typeList) {
                        if (server.getType().equals(type)) {
                            ntype[i]++;
                        }
                        i++;
                    }
                }
            }
            i = 0;
            for (String type : typeList) {
                response += "\t" + type + ": " + ntype[i] + "\n\t-- Preço nominal: " + catalogue.getNominalPrice(type) + "\n";
                i++;
            }
            return response;
        }

        private String listOccupiedServers() {
            String response = "Servidores Ocupados:\n";
            ArrayList<Servers> catalogue_list = catalogue.makeServerList();
            ArrayList<String> typeList = catalogue.getTypes();
            int[] ntype = new int[typeList.size()];
            int i;
            for (Servers server : catalogue_list) {
                if (server.isOccupied()) {
                    i = 0;
                    for (String type : typeList) {
                        if (server.get_type().equals(type)) {
                            ntype[i]++;
                        }
                        i++;
                    }
                }
            }
            i = 0;
            for (String type : typeList) {
                response += "\t" + type + ": " + ntype[i] + "\n";
                i++;
            }
            return response;
        }

        private String liberateServer() throws IOException, InterruptedException {
            float total_pay = 0;
            this.sendMessage("Por favor indique o identificador da reserva!\n");
            String answer = input.readLine();
            String u_email = myEmail;
            if (catalogue.containsKey(answer)) {
                Servers s_requested = catalogue.getServer(answer);
                if (s_requested.getUserEmail().equals(u_email)) {
                    total_pay = s_requested.getCurrentTotal();
                    s_requested.reset();
                    return "O servidor foi libertado com sucesso! Teria de pagar " + Float.toString(total_pay) + " mas desta vez fica por conta da casa ;D \n";
                } else {
                    return "O servidor mencionado já não está associado a si!\n";
                }
            } else {
                return "A referência é inválida\n";
            }
        }

        private int loginPrompt() {
            try {
                boolean set = false;
                boolean valid = false;
                int tries = 3;
                String email;
                String password;
                while (!set) {
                    this.sendMessage("E-mail: ");
                    email = input.readLine();
                    if (email.equalsIgnoreCase("quit")) {
                        return 0;
                    } else if (!accounts.isAccountEmail(email)) {
                        this.sendMessage("E-mail inválido\n");
                    } else if (loggedIn.containsEmail(email)) {
                        this.sendMessage("Um utilizador com esse e-mail já efetuou log-in\n");
                    } else {
                        while (!valid) {
                            this.sendMessage("Password: ");
                            password = input.readLine();
                            if (!accounts.isValidPassword(email, password)) {
                                tries--;
                                if (tries == 0) {
                                    this.sendMessage("Terceira tentativa falhada\n");
                                    return 0;
                                } else {
                                    this.sendMessage("Password Inválida. Tem mais " + tries + " tentativas\n");
                                }
                            } else {
                                loggedIn.addEmail(email);
                                writers.add(this.myEmail, this.output);
                                set = true;
                                valid = true;
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            return 1;
        }

        private int signupPrompt() {
            try {
                boolean set = false;
                String email;
                String password;
                String answer = "";
                while (!set) {
                    this.sendMessage("E-mail: ");
                    email = input.readLine();
                    if (accounts.isAccountEmail(email)) {
                        this.sendMessage("Já existe uma conta com o e-mail indicado.\nPretende introduzir novo e-mail? s/n");
                        while (!answer.equals("s") && !answer.equals("n")) {
                            answer = input.readLine();
                            if (answer.equals("n")) {
                                return 0;
                            }
                        }
                    } else if (email == null || email.equals(""))
                    ; else {
                        this.sendMessage("Password: ");
                        password = input.readLine();
                        Account conta = new Account(email, password);
                        myEmail = email;
                        accounts.addAccount(conta);
                        set = true;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            return 0;
        }

        @Override
        public void run() {
            try {
                int phase = 0;
                while (phase != -1) {
                    if (phase == 0) {
                        phase = this.startMenu();
                    }
                    if (phase == 1) {
                        phase = this.mainPage();
                    }
                }
                sendMessage("exit");
            } catch (Exception e) {
                System.out.println("ups... ocorreu um erro, sei lá qual");
                System.out.println(e);
            }
        }
    }
}
