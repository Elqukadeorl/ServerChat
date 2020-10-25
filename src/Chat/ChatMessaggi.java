/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chat;

import java.io.*;
/*
 * Questa classe definisce i diversi tipi di messaggio che possono
 * essere scambiati tra Client e Server
 */
public class ChatMessaggi implements Serializable 
{

    // I diversi tipi di messaggio sono:
    // whoisin per far vedere agli altri clienti chi è connesso 
    // message che inidica il messaggio normale da inviare sulla chat   
    // logout per disconnettersi dal server
    public static final int whoisin = 0, message = 1, logout = 2;
    private int type;
    private String mes;
    
	public ChatMessaggi(int type, String message) /*costruttore della ChatMessaggi*/         
        {
            this.type = type;   //tipo del messaggio
            this.mes = message;//messaggio
	}
	
	public int getType() 
        {return type;}

	public String getMes() 
        { return mes;}
}