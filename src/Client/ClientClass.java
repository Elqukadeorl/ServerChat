/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;
import Chat.ChatMessaggi;
import java.net.*;
import java.io.*;
import java.util.*;


public class ClientClass  
{	
    private String notif = " [Benvenuto] ";// notifica
    
    private ObjectInputStream sInput;//Input	
    private ObjectOutputStream sOutput;//Output		
    private Socket socket;// socket 
    private String server, username;// nome server e username del cliente
    private int port;//porta
	
    /*
    *  Costruttore per settare diverse variabili
    *  server: inserisce l'indirizzo del server
    *  port: inserisce il numero di porta
    *  username: username del cliente
    */
    ClientClass(String server, int port, String username) 
    {
        this.server = server;
        this.port = port;
        this.username = username;
    }
        public String getUsername() 
        {
            return username;
        }

	public void setUsername(String username) 
        {
            this.username = username;
	}
	/*
	 * Metodo che da inizio alla chat
	 */
        public boolean start() {
            try { // tenta la connessione con il server
                    socket = new Socket(server, port);
		} 
		catch(Exception ec) 
                {// eccezione che si attiva quando fallisce la connessione con il server
                    display(" Errore nella connessione con il Server: " + ec);
                    return false;
		}
		String msg = " Connessione effettuata al Server. Nome e indirizzo: " +socket.getInetAddress()+ " Porta: " + socket.getPort();
		display(msg);
		try// Crea gli Input e Output per il flusso dei dati 
		{
                    sInput  = new ObjectInputStream(socket.getInputStream());
                    sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) 
                {
                    display(" Creata un eccezione con i dati Output / Input : " + eIO);
                    return false;
		}
		new ListenFromServer().start(); //Thread che il server utilizza per ascoltare il client
		try  // Invia l'username al server sottoforma di stringa
		{
                    sOutput.writeObject(username);
		}
		catch (IOException eIO) 
                {
                    display("Exception doing login : " + eIO);
                    disconnect();
                    return false;
		}
		return true;
	}
	/*
	 *  Metodo per inviare un messaggio alla console
	 */
	private void display(String msg) 
        {
		System.out.println(msg);	
	}	
	/*
	 * Metodo per inviare un messaggio al server
	 */
	void sendMessage(ChatMessaggi msg) {
            try {
                    sOutput.writeObject(msg);
		}
            catch(IOException e) {
                    display("Exception writing to server: " + e);
		}
	}
	
	private void disconnect() /* In caso di errori chiude i canali di Input e Output */
        {
		try 
                { 
                    if(sInput != null) sInput.close();
		}
		catch(Exception e) {}
		try 
                {
                    if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {}
                try
                {
                    if(socket != null) socket.close();
		}
		catch(Exception e) {}	
	}
        
	public static void main(String[] args) 
        {
            // Valori di default per entrare
            int portNumber = 1500;
            String serverAddress = "localhost";
            String userName = "Anonimo";
            Scanner scan = new Scanner(System.in);
            System.out.println("Inserisci l'username: ");
            userName = scan.nextLine();
            switch(args.length) {
                case 3:
        // se c'è username del client, numero di porta e nome/indirizzo del serverù
                    serverAddress = args[2];
                case 2:
             // se c'è username del cliente e numero della porta
                    try {
                        portNumber = Integer.parseInt(args[1]);
                    }
                    catch(Exception e) {
                        System.out.println("Numero della porta sbagliato");
			System.out.println("La corretta scrittura è: [username] [numeroPorta] [IndirizzoServer]");
			return;
                    }
                case 1: 
		// se c'è solo il nome del cliente
                    userName = args[0];
                case 0:
                    // se non c'è nulla, quindi il nome è Anonimo
                    break;
		}
		// oggetto cliente
		ClientClass client = new ClientClass(serverAddress, portNumber, userName);
		// Il Client avvia la connessione con il server.
		if(!client.start())
                    return;
		System.out.println("\nCiao! Benvenuto alla chat");
		System.out.println("Ecco le istruzioni che puoi eseguire:");
		System.out.println("1. Puoi scrivere un messaggio per inviarlo in broadcast");
		System.out.println("2. Puoi scrivere @username per inviare un messaggio al client specificato");
		System.out.println("3. Puoi scrivere whoisin per vedere chi è connesso al canale");
		System.out.println("4. Puoi scrivere logout per disconnettersi dal server ");
                
		while(true) {  // loop infinito che permette di ricevere i messaggi dei clienti 
                    System.out.print(" ");
	     // legge il messaggio del cliente
                    String msg = scan.nextLine();
                        if(msg.equalsIgnoreCase("logout")) // logout fa disconettere il cliente dalla chat 
                        {
                            client.sendMessage(new ChatMessaggi(ChatMessaggi.logout, ""));
                            break;
			}	
			else if(msg.equalsIgnoreCase("whoisin")) // whoisin fa vedere i partecipanti alla chat
                        {
                            client.sendMessage(new ChatMessaggi(ChatMessaggi.whoisin, ""));				
			}
			
			else // messaggio per comunicare
                        {
                            client.sendMessage(new ChatMessaggi(ChatMessaggi.message, msg));
			}
		}
		scan.close();// chiude la possibilità di leggere i messaggi scritti	
		client.disconnect();// disconnette il cliente dalla chat	
	}
	
        /* classe che aspetta i messaggi dal server*/
	class ListenFromServer extends Thread 
        {
            public void run() 
            {
                while(true) 
                {
                    try 
                    {
		// legge i messaggi da input
                        String msg = (String) sInput.readObject();
		// fa vedere il messaggio
                System.out.println(msg);
                    }
                    catch(IOException e) 
                    {
                        display("Il server ha chiuso la connessione: ");
                        break;
                    }
                    catch(ClassNotFoundException e2) {}
                }
            }
        }
}