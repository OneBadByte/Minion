import java.net.*;
import java.io.*;
import java.util.ArrayList;

class MinionConnection {
    private Socket socket;

    // used to connect to other minions
    MinionConnection(String ipAddress, int port){
        while(true){
            try {
                this.socket = new Socket(ipAddress, port);
                break;
            } catch (IOException e) {
            }
        }
//        System.out.println("Connected to: " + ipAddress + ":" + port);
    }

    // sends a command to other minions
    public void send(String message){
        try {
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            output.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // receives input from another minion
    public void receive(){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            String message = "";
            message = reader.readLine();
            message = message.replace("|", "\n");
//            System.out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

// Server is the brains of a minion
class Server{
    private boolean keepAlive = true;
    private int port;
    private ServerSocket serverSocket;
    private Socket socket;

    // holds all of the minions that this minion is connected to
    ArrayList<MinionConnection> minionConnections = new ArrayList<>();

    Server(int port){
        this.port = port;
        this.accept();
    }

    // waits and accepts a connection
    public void accept(){
        try {
            this.serverSocket = new ServerSocket(this.port);
            this.socket = this.serverSocket.accept();
//            System.out.println("Connected");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // gets commands from a minion or gru
    public String read(){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void respond(String response){
        try {
            PrintWriter writer = new PrintWriter(this.socket.getOutputStream(), true);
            writer.print(response + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // closes a socket
    public void close() {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void killServer(){
        this.keepAlive = false;
    }

    public boolean isServerAlive(){
        return this.keepAlive;
    }

    // processes the input received and executes appropriate function
    public void processCommand(String command) {
        // splits command by whitespace and stores it in an array
        String[] commandArray = command.split(" ");

        // TODO fix this section
        String commandString = "";
        for (int x = 1; x < commandArray.length; x++) {
            commandString = commandString + " " + commandArray[x];
        }

        switch (commandArray[0]) {
            case "linux":
                linuxCommand(commandString);
                break;
            case "connect":
                this.respond("in progress");
                break;
            case "kill":
                this.respond("in progress");
                break;
            case "die":
                this.killServer();
                break;
            default:
                this.respond(command);
                break;
        }
        //___________________
    }

    // runs a linux command
    public void linuxCommand(String command){
        try {
            // process runs the command and waits for it to finish
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            process.waitFor();
            // stores everything in one line separated by | instead of new lines
            String message = "";
            //ghetto hack so that the reader wont keep running through lines
            String storage = "";
            while ((storage = reader.readLine()) != null){
                message = message + storage + "|";
            }
            // sends the response message
            this.respond(message);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class Main {

    public static void main(String[] args) {
        // starts the server on the port provided
        Server server = new Server(Integer.parseInt(args[0]));
        while(true){
            String input = "";

            // checks if the server is alive and will end when changed
            while(server.isServerAlive()){
                // gets user input sent over tcp
                input = server.read();
                if(input == null){
                    break;
                }
                server.processCommand(input);
            }
            // closes the socket
            server.close();
            if(server.isServerAlive()) {
                // will start a new connection
                server.accept();
            }else{
                break;
            }
        }
        System.out.println("GoodBye Cruel World!");
    }
}
