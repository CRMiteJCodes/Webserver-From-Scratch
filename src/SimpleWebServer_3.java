import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class SimpleWebServer_3{
    private static volatile boolean running = true;
    public static void main(String[] args) throws Exception{
        ServerSocket server = new ServerSocket(8080);
        System.out.println("Multithreaded server running at http://localhost:8080 ");

        Thread consoleThread = new Thread(()->{
            Scanner scan=new Scanner(System.in);
            while (true){
                String input = scan.nextLine();
                if(input.equalsIgnoreCase("exit")){
                    System.out.println("Shutting down via console command...");
                    running = false;
                    try{
                        server.close();
                    }
                    catch(Exception e){
                        System.out.println("Error closing server!"+e.getMessage());
                    }
                    break;
                }
            }
            scan.close();
        });
        consoleThread.start();

        while(running){
            try{
                Socket client = server.accept();
                System.out.println("New client connected");
                new Thread(() -> handleClient(client)).start();
            }
            catch(Exception e){
                if(running)
                System.out.println("Error handling client "+e.getMessage());
                else
                System.out.println("Server stopped");
            }
        }
    }

    private static void handleClient(Socket client){
        try{
            OutputStream out = client.getOutputStream();
            String body = "<h1> Hello from Multithreaded Server </h1>";
            String response =  "HTTP/1.1 200 OK\r\n"+"Content-Type: text/html\r\n"+"Content-Length: "+body.length() + "\r\n" + 
            "Connection: close\r\n" + "\r\n" + body;                
            out.write(response.getBytes());
            out.flush();
            out.close();
            client.close();
        }
        catch(Exception e){
            System.out.println("Error handling client: "+e.getMessage());
        }
    }
}
