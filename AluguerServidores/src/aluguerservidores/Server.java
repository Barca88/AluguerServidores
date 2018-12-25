/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author quim
 */
public class Server {

    private final ServerSocket serverSocket;
    private ArrayList<ServerThread> clients;
    private AccountsMap accounts;
    private EmailList loggedIn;
    private Catalogue catalogue;
    private Lock accountsLock;

    public Server() throws IOException, NoSuchAlgorithmException {
        this.serverSocket = new ServerSocket(12345);
        this.clients = new ArrayList<>();
        this.accounts = new AccountsMap();
        this.accountsLock = new ReentrantLock();
        this.loggedIn = new EmailList();
        this.catalogue = new Catalogue();
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        Server servidor = new Server();
        servidor.getInput();
    }

    private void getInput() throws IOException {
        while (true) {
            Socket clSocket = serverSocket.accept();
            ServerThread st = new ServerThread(clSocket);
            this.clients.add(st);
            st.start();
        }
    }

    //Thread para cada cliente
    private class ServerThread extends Thread {

        private BufferedWriter output;
        private BufferedReader input;

        private ServerThread(Socket clSocket) {
            try {
                input = new BufferedReader(new InputStreamReader(clSocket.getInputStream()));
                output = new BufferedWriter(new OutputStreamWriter(clSocket.getOutputStream()));
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
            this.sendMessage("1 - Login \n2 - Registar");
            String answer = input.readLine();
            if (answer.equals("2")) {
                int singupResult = signupPrompt();
                return singupResult;
            } else if (answer.equals("1")) {
                int loginResult = loginPrompt();
                return loginResult;
            } else {
                return 0;
            }
        }

        private int mainPage() throws IOException {
            int status=0;
            String answer= "";
            while(status == 0) {
                this.sendMessage("1 - Listar Catalogo \n2 - Pedir servidor \n3 - Libertar servidor \n4 - Listar Leilões \n5 - Ir para Leilão \n6 - Para Sair");
                answer = input.readLine();
                switch (answer) {
                    case "1":
                        this.sendMessage(this.list_catalogue());
                        break;
                    case "2":
                        this.sendMessage(this.request_Server());
                        break;
                    case "3":
                        this.sendMessage(this.liberate_Server());
                        break;
                    case "4": break;
                    case "5": break;
                    default:
                        status=1;
                        break;
                }

            }
            return 0;
        }

        private String list_catalogue(){
            String response = "";
            ArrayList <Servers>  catalogue_list =  new ArrayList<>(catalogue.server_catalogue.values());
            for (Servers server: catalogue_list) {
                response = response + "Id do Servidor: " + server.get_id() + " \n\t-- Tipo: " + server.get_type() + " \n\t-- Preço nominal:" + (new String (String.valueOf(server.getNominal_price()))) + " \n\t-- Preço indicado:" + (new String (String.valueOf(server.getIndic_price()))) + " \n\t-- Servidor Ocupado:" + (new String (String.valueOf(server.get_ocupied()))) + " \n\t-- Servidor Leiloado:" + (new String (String.valueOf(server.get_auctioned()))) + "\n\n";
            }
            return response;
        }

        private String request_Server() throws IOException {
            this.sendMessage("Por favor indique o id do servidor que deseja requisitar!\n");
            String answer = input.readLine();
            this.sendMessage("Por favor indique o seu email!\n");
            String u_email = input.readLine();
            if (catalogue.server_catalogue.containsKey(answer)){
                Servers  s_requested= catalogue.server_catalogue.get(answer);
                if (s_requested.get_ocupied()== false){
                    s_requested.set_ocupied(true);
                    s_requested.setUser_email(u_email);
                    s_requested.start();
                    return "Este é o identificador da reserva: " +answer;
                }
                else {
                    if (s_requested.get_auctioned() == true) {
                        s_requested.set_minutes(0);
                        s_requested.setUser_email(u_email);
                        return "Este é o identificador da reserva: " + answer;
                    } else {
                        return "O servidor mencionado está ocupado!\n";
                    }
                }
            } else{ return "O servidor mencionado não existe!\n";     }
        }

        private String liberate_Server() throws IOException {
            float total_pay=0;
            this.sendMessage("Por favor indique o identificador da reserva!\n");
            String answer = input.readLine();
            this.sendMessage("Por favor indique o seu email!\n");
            String u_email = input.readLine();
            if (catalogue.server_catalogue.containsKey(answer)){
                Servers  s_requested= catalogue.server_catalogue.get(answer);

                if (s_requested.getUser_email().equals(u_email)){
                    s_requested.set_ocupied(false);
                    if (s_requested.get_auctioned()== true){
                        total_pay = s_requested.getIndic_price()*s_requested.get_minutes();
                        s_requested.setUser_email("");
                        s_requested.set_minutes(0);
                        return "O servidor foi libertado com sucesso! Teria de pagar " + Float.toString(total_pay) + " mas desta vez fica por conta da casa ;D \n";
                    }else{
                        total_pay = s_requested.getNominal_price()*s_requested.get_minutes();
                        s_requested.setUser_email("");
                        s_requested.set_minutes(0);
                        return "O servidor foi libertado com sucesso! Teria de pagar " + Float.toString(total_pay) + " mas desta vez fica por conta da casa ;D \n";
                    }
                }
                else{ return "O servidor mencionado já não está associado a si!\n"; }
            } else{ return "A transação mencionada não existe!\n";     }
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
                    if (!accounts.isAccountEmail(email)) {
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
                while (phase == 0) {
                    phase = this.startMenu();
                }
                while(phase == 1){
                    sendMessage("Está dentro do sistema!");
                    phase = this.mainPage();
                   break;
                }
                sendMessage("end");
            } catch (Exception e) {
                System.out.println("ups... ocorreu um erro, sei lá qual");
            }
        }
    }
}
