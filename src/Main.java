import java.net.*;
import java.io.*;
import java.util.Arrays;


class Server{
    private int port;
    private ServerSocket serverSocket;
    private Socket socket;

    Server(int port){
        this.port = port;
        this.accept();
    }

    public void accept(){
        try {
            this.serverSocket = new ServerSocket(this.port);
            this.socket = this.serverSocket.accept();
            System.out.println("Connected");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String read(){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void close(){
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


class Command{

    public void processCommand(String command){
        String[] commandArray = command.split(" ");
        String commandString = "";
        for(int x=1; x < commandArray.length; x++){
           commandString = commandString + " " + commandArray[x];
        }

        if(commandArray[0].equals("linux")){
            linuxCommand(commandString);
        }
    }

    public void linuxCommand(String command){

        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (reader.readLine() != null){
                System.out.println(reader.readLine());
            }
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}


public class Main {

    public static void main(String[] args) {
        Server server = new Server(5000);
        Command command = new Command();
        while(true){
            String input = "";

            while(true){
                input = server.read();
                if(input == null){
                    break;
                }
                System.out.println(input);
                if(input == "quit"){
                    server.close();
                    break;
                }
                command.processCommand(input);
            }
            server.close();
            server.accept();
        }
    }
}
