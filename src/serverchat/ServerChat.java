/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverchat;
import Chat.ChatMessaggi;
import java.io.*;
import java.net.*;
import java.util.*;

public class ServerChat 
{	
    private static int Iduni; // ID univoco per ogni connessione
    private ArrayList<ClientThread> ListaClienti; // Array che mantiene la lista dei clienti connessi	
    private int port;// la porta dove aspetta le connessioni	
    private boolean keepGoing;// Per verificare se il server sta funzionando	
    private String notif = " [Benvenuto] ";// notifica
	/* costruttore con la porta su cui deve ascoltare le connessioni dei client*/
	public ServerChat(int port) 
        {
            this.port = port;
            ListaClienti = new ArrayList<ClientThread>();// Array List per ricordare i Client che sono connessi
	}
	public void start() 
        {
		keepGoing = true;
		try 
		{
                    ServerSocket serverSocket = new ServerSocket(port);// socket usato dal server con la porta
			while(keepGoing) // loop infitio per le connessioni
			{
                            display("Il Server sta aspettando la connessione sulla porta " + port );
                            Socket socket = serverSocket.accept();// accettare le connessioni se richieste dal Client
                            if(!keepGoing)
				break;
                            ClientThread t = new ClientThread(socket);// Thread che viene usato dal Client per chattare
                            ListaClienti.add(t);//aggiunge il Client alla lista dei clienti
				
                            t.start();
			}			
		}
		catch (IOException e) 
                {
            String msg =  " Eccezione sul nuovo server socket " + e + "\n";
			display(msg);
		}
	}
	private void display(String msg) 
        {
		String mes =  msg;
		System.out.println(mes);
	}

	private synchronized boolean broadcast(String message)// inviare un messaggio broadcast a tutti i Client 
        { 
                String[] w = message.split(" ",3);   
		boolean isPrivate = false;// se il messaggio è privato, ovvero da Client a Client
		if(w[1].charAt(0)=='@') 
			isPrivate=true;
		
		
		// se il messaggio è per un singolo cliente, lo invia solo a lui
		if(isPrivate==true)
		{
			String tocheck=w[1].substring(1, w[1].length());
			
			message=w[0]+w[2];
			String messageLf = message + "\n";
			boolean found=false;
			// ciclo inverso per cercare il nome utente a cui inviare il messaggio
			for(int y=ListaClienti.size(); --y>=0;)
			{
				ClientThread ct1=ListaClienti.get(y);
				String check=ct1.getUsername();
				if(check.equals(tocheck))
				{
					// scrive al cliente se non riesce a rimuoverlo dall'elenco
					if(!ct1.writeMsg(messageLf)) 
                                        {
						ListaClienti.remove(y);
						display("Il cliente " + ct1.username + " è disconnesso e rimosso dalla lista ");
					}
					// username trovato
					found=true;
					break;
				}
			}
			// username del cliente non trovato
			if(found!=true)
			{
				return false; 
			}
		}
                else // se il messaggio è broadcast, ovvero per tutta la chat
		{
			String messageLf =  message + "\n";
			System.out.print(messageLf);// visualizza il messaggio
			for(int i = ListaClienti.size(); --i >= 0;) // ciclo inverso per cercare il nome utente da rimuovere
                        {
				ClientThread ct = ListaClienti.get(i);
				if(!ct.writeMsg(messageLf)) 
                                {
					ListaClienti.remove(i);
					display("Cliente " + ct.username + " è disconnesso e rimosso dalla lista ");
				}
			}
		}
		return true;
	}
	synchronized void remove(int id) // se il cliente invia il messaggio LOGOUT
        {
		String disconnectedClient = "";
		
		for(int i = 0; i < ListaClienti.size(); ++i) //Ricerca nell'array l'ID del Client che ha scritto logout
                {
			ClientThread ct = ListaClienti.get(i);
			if(ct.id == id) //quando trova l'ID lo rimuove
                        {
				disconnectedClient = ct.getUsername();
				ListaClienti.remove(i);
				break;
			}
		}
		broadcast(notif + disconnectedClient + " ha abbandonato la chat " + notif);
	}
	
	/*
	 * Main del Server
	 * 
	 */ 
	public static void main(String[] args) 
        {
		int portNumber = 1500;
		switch(args.length) 
                {
			case 1:
				try 
                                {
                                    portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e) 
                                {
                                    System.out.println("Numero della porta errato");
                                    System.out.println("Corretta scrittura è: [numeroporta]");
                                    return;
				}
			case 0:
				break;
			default:
				System.out.println("Corretta scrittura è: [numeroporta]");
				return;
				
		}
		// crea l'oggetto server e lo avvia
		ServerChat server = new ServerChat(portNumber);
		server.start();
	}
	// ogni istanza della classe ClientThread è utilizzata per ogni cliente connesso
	class ClientThread extends Thread 
        {	
            Socket socket;// socket per ricevere i messaggi del cliente
            ObjectInputStream sInput;
            ObjectOutputStream sOutput;
            int id; // id unico per ogni cliente, che serve per la disconnessione
            String username;// username del cliente
            ChatMessaggi cm;// oggetto Messaggio per ricevere il tipo e il messaggio
            ClientThread(Socket socket) // costruttore
                {
                    id = ++Iduni;// id che cambia ogni volta
                    this.socket = socket;
                    System.out.println("Creazione dei flussi per i dati di Input-Output per la chat");
			try//Creazione dei flussi per i dati
                        {
                            sOutput = new ObjectOutputStream(socket.getOutputStream());
                            sInput  = new ObjectInputStream(socket.getInputStream());
                            username = (String) sInput.readObject();// legge l'username del cliente
                            broadcast(notif + username + " sta partecipando alla chat" );
			}
			catch (IOException e) 
                        {
                            display("Eccezione creata per problemi nei valori input o output: " + e);
                            return;
			}
			catch (ClassNotFoundException e) {}
		}
		public String getUsername() {
                    return username;
		}
		public void setUsername(String username) {
                    this.username = username;
		}
		public void run() // ciclo infinito per leggere e inviare il messaggio
                {                // questo ciclo rimane in vita finchè non appare il messaggio logout
			boolean keepGoing = true;
			while(keepGoing) 
                        {
				try // legge la stringa, che è un oggetto della classe ChatMessaggi
                                {      
                                    cm = (ChatMessaggi) sInput.readObject();
				}
				catch (IOException e) 
                                {
                                    display(username + " Errore durante la lettura del messaggio: " + e);
                                    break;				
				}
				catch(ClassNotFoundException e2) 
                                {
                                    break;
				}
				String message = cm.getMes();
				switch(cm.getType()) //tipi di messaggio
                                {
				case ChatMessaggi.message:
					boolean confirmation =  broadcast(username + ": " + message);
					if(confirmation==false)
                                        {
                                            String msg = notif + "Non esistono utenti di questo tipo" + notif;
                                            writeMsg(msg);
					}
					break;
				case ChatMessaggi.logout:
					display(username + " disconnesso con il messaggio LOGOUT");
					keepGoing = false;
					break;
				case ChatMessaggi.whoisin:
					writeMsg("Lista dei clienti connessi alla chat \n");
					for(int i = 0; i < ListaClienti.size(); ++i)// invia la lista dei clienti attivi 
                                        {
                                            ClientThread ct = ListaClienti.get(i);
                                            writeMsg((i+1) + ") " + ct.username );
					}
					break;
				}
			}
			close();
		}
		private void close() 
                {
			try 
                        {
                            if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try 
                        {
                            if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try 
                        {
                            if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}
		private boolean writeMsg(String msg) {			
			if(!socket.isConnected()) 
                        {
                            close();
                            return false;
			}
			try {
                            sOutput.writeObject(msg);// scrive il messaggio sulla chat
			}
			catch(IOException e) // se c'è un errore, informa il Client e non chiude il processo
                        {
                            display(notif + "Errore nell'invio del messaggio a " + username + notif);
                            display(e.toString());
			}
			return true;
		}
	}
}