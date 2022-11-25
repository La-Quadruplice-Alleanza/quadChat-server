//SERVER
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.ArrayList;

public class ServerRSA extends Thread{
	ServerSocket server = null;
	Socket client = null;
	String stringaRicevuta;
	String stringaModificata;
	BufferedReader inDalClient;
	DataOutputStream outVersoClient;
	int lunghezza = 0;
	BigInteger publicKey[] = new BigInteger[2];
	BigInteger msg;
	BigInteger privateKey[], publicKey_server[], Keys[];
	String username;
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
			RSA_Decripta decrypt = new RSA_Decripta();
			
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
			//System.out.println(publicKey_server[0]);
			//System.out.println(publicKey_server[1]);

			//outVersoClient.writeBytes(publicKey_server[0].toString() + "\n");
			//outVersoClient.writeBytes(publicKey_server[1].toString() + "\n");
			int flag;
			stringaRicevuta = inDalClient.readLine();
			publicKey[0] = new BigInteger(stringaRicevuta);
			stringaRicevuta = inDalClient.readLine();
			publicKey[1] = new BigInteger(stringaRicevuta);
			do{
				flag = 0;
				username = inDalClient.readLine();
				for(ServerRSA client : listaClient){
					if(client != this && client.username.equals(username)){
						flag = 1;
						outVersoClient.writeBytes("1" + '\n');
						username = "";
						break;
					}
				}
			}while(flag == 1);
			outVersoClient.writeBytes("0" + '\n');
			System.out.println("Chiavi inviate e ricevute, in attesa del messaggio da decriptare...");
			annunciaClient();

			do{
				System.out.println("In attesa di un messaggio...");
				inDalClient.readLine();
				System.out.println("Richiesta di invio messaggio ricevuta, inizio routine...");
				outVersoClient.writeBytes("STPMSG" + '\n');
				sendMsg();
				//msg = decrypt.decrypt_bi(privateKey[0], privateKey[1], msg);
				//stringaModificata = decrypt.decrypt(publicKey[0], publicKey[1], msg);
				
				//System.out.println("Messaggio decriptato! Invio al client..." + stringaModificata);
				//stringaModificata = Base64.getEncoder().encodeToString(stringaModificata.getBytes());
				outVersoClient.flush();
				System.out.println("Messaggio inviato con successo!");
			}while(lunghezza != -999);
			client.close();
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
				outVersoClient.writeBytes(client.publicKey[0].toString() + '\n');
				outVersoClient.writeBytes(client.publicKey[1].toString() + '\n');
				msg = new BigInteger(inDalClient.readLine());
				publicKeyServer[0] = new BigInteger(inDalClient.readLine());
				publicKeyServer[1] = new BigInteger(inDalClient.readLine());
				client.outVersoClient.writeBytes(msg.toString() + '\n');
				client.outVersoClient.writeBytes(username + '\n');
				client.outVersoClient.writeBytes(publicKeyServer[0].toString() + '\n');
				client.outVersoClient.writeBytes(publicKeyServer[1].toString() + '\n');
				//System.out.println(username);
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
		annuncio = "L'utente " + username + " si è disconnesso!";
		encr = crypt.crypt(privateKey[0], privateKey[1], annuncio);
		sendComm(encr, publicKey_server);
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