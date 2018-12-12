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
public class AluguerServidores {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
        System.out.println("Aluguer de Servidores");
        Client client = new Client();
        client.getInputsDoThings();
        }catch(Exception e){
            System.out.println("Erro!");
        }
    }

}
