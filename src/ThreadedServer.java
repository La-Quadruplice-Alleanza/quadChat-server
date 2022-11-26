import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadedServer {
    static final int PORT = 6789;  
    public static void main(String args[]) {
        ServerSocket serverSocket = null;
        Socket socket = null;
        int cont = 2;
        //DataOutputStream outVersoClient;
        try{
            serverSocket = new ServerSocket(PORT);
            //outVersoClient = new DataOutputStream(socket.getOutputStream());
        }catch(IOException e){
            e.printStackTrace();
        }
        
        while(true){
            try{
                System.out.println("IN ATTESA DI CLIENT...");
                socket = serverSocket.accept();
                System.out.println("Client connesso (" + socket + "), apertura sessione....");
                if(cont >= 0){
                    //cont--;
                }
            }catch (IOException e){
                System.out.println("I/O error: " + e);
            }
            if(cont >= 0){
                new ServerRSA(socket, 1).start(); //Inizia thread
            }
            else{
                new ServerRSA(socket, 0).start(); //Inizia thread
            }
            
        }
    }
}