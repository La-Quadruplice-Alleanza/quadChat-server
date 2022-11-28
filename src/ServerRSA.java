//SERVER
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.ArrayList;

public class ServerRSA extends Thread{
	ServerSocket server = null;
	Socket client = null;
	BufferedReader inDalClient;
	DataOutputStream outVersoClient;

	String stringaRicevuta;
	BigInteger publicKey[] = new BigInteger[2];
	BigInteger msg;
	BigInteger privateKey[], publicKey_server[], Keys[];
	String username = "";

	int flag;
	
	public static ArrayList<ServerRSA> listaClient = new ArrayList<>();
	RSA_Cripta crypt = new RSA_Cripta();

	public ServerRSA(Socket client, int valid){
		this.client = client;
		try{
			inDalClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			outVersoClient = new DataOutputStream(client.getOutputStream());
			if(valid == 0){
				client.close();
				return;
			}
			
			listaClient.add(this);
			//comunica();
		}catch(Exception e){

		}
	}

	public void run(){
		try{
			
			RSA_GenKey keyGenerator = new RSA_GenKey();
			
			System.out.println("Connessione al client riuscita, generazione chiavi...");
			
			Keys  = new BigInteger[3];
			publicKey_server = new BigInteger[2];
			privateKey = new BigInteger[2];

			Keys = keyGenerator.GenKeys();
			publicKey_server[0] = Keys[0];
			publicKey_server[1] = Keys[1];

			privateKey[0] = Keys[2];
			privateKey[1] = Keys[1];
			System.out.println("Chiavi generate, invio chiave pubblica al client...");
			
			stringaRicevuta = inDalClient.readLine();
			publicKey[0] = new BigInteger(stringaRicevuta);
			stringaRicevuta = inDalClient.readLine();
			publicKey[1] = new BigInteger(stringaRicevuta);
			//INIZIO VALIDITY HANDSHAKE
			do{
				flag = 0;
				username = inDalClient.readLine();
				//TODO: #2 Criptare anche gli username @Azel04 @Zuppa-hub @Reddddddddddddddd @leomob24
				for(ServerRSA client : listaClient){
					if(client != this && client.username.equals(username)){
						flag = 1;
						outVersoClient.writeBytes("1" + '\n');
						username = "";
						break;
					}
				}
			}while(flag == 1);
			if(username == null){
				username = "UNDEFINED_USER";
				throw new Exception();
			}
			outVersoClient.writeBytes("0" + '\n');
			//FINE VALIDITY HANDSHAKE
			System.out.println("Chiavi inviate e ricevute, in attesa del messaggio da decriptare...");
			annunciaClient();

			while(true){
				System.out.println("In attesa di un messaggio...");
				inDalClient.readLine();

				System.out.println("Richiesta di invio messaggio ricevuta, inizio routine...");
				outVersoClient.writeBytes("STPMSG" + '\n');
				sendMsg();

				outVersoClient.flush();
				System.out.println("Messaggio inviato con successo!");
			}
			//client.close();
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			try{
				rimuoviClient();
			}catch(Exception ex){

			}
			System.out.println("Il client è crashato o è stato chiuso forzatamente");
			return;
		}

	}
	public void sendMsg() throws Exception{
		BigInteger msg;
		BigInteger publicKeyServer[] = new BigInteger[2];
		outVersoClient.writeBytes(Integer.toString(listaClient.size()) + '\n');
		for(ServerRSA client : listaClient){
			if(!(client.username.equals(username))){
				outVersoClient.writeBytes(client.publicKey[0].toString() + '\n'); //Invio chiavi pubbliche al Client
				outVersoClient.writeBytes(client.publicKey[1].toString() + '\n');

				msg = new BigInteger(inDalClient.readLine()); //Ricezione messaggio e chiave pubblica
				publicKeyServer[0] = new BigInteger(inDalClient.readLine());
				publicKeyServer[1] = new BigInteger(inDalClient.readLine());

				client.outVersoClient.writeBytes(msg.toString() + '\n'); //Invio di messaggio/username/chiave pubblica
				client.outVersoClient.writeBytes(username + '\n');
				client.outVersoClient.writeBytes(publicKeyServer[0].toString() + '\n');
				client.outVersoClient.writeBytes(publicKeyServer[1].toString() + '\n');
			}
		}
	}
	public void sendComm(BigInteger msg, BigInteger publicKey[]) throws Exception{
		for(ServerRSA client : listaClient){
			if(!(client.username.equals(username))){
				client.outVersoClient.writeBytes(msg.toString() + '\n');
				client.outVersoClient.writeBytes("SERVER" + '\n');
				client.outVersoClient.writeBytes(publicKey[0].toString() + '\n');
				client.outVersoClient.writeBytes(publicKey[1].toString() + '\n');
			}
		}
	}
	public void rimuoviClient() throws Exception{
		String annuncio;
		BigInteger encr;
		if(username.equals("UNDEFINED_USER") == false){
			annuncio = "L'utente " + username + " si è disconnesso!";
			encr = crypt.crypt(privateKey[0], privateKey[1], annuncio);
			sendComm(encr, publicKey_server);
		}
		listaClient.remove(this);
	}
	public void annunciaClient() throws Exception{
		String annuncio;
		BigInteger encr;
		annuncio = "L'utente " + username + " si è connesso!";
		encr = crypt.crypt(privateKey[0], privateKey[1], annuncio);
		sendComm(encr, publicKey_server);
	}
}
