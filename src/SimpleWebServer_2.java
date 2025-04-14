import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class SimpleWebServer_2{
    private static volatile boolean running = true;
    public static void main(String[] args) throws Exception{
        ServerSocket server=new ServerSocket(8080);//wait for connection
        System.out.println("Server is running on http://localhost:8080 enter 'exit' to stop server");
        Thread consoleThread = new Thread(()->{
            Scanner scan=new Scanner(System.in);
            while (true){
                String input=scan.nextLine();
                scan.close();
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
        });
        consoleThread.start();
        while(running){
            try{
                Socket client = server.accept();
                OutputStream out = client.getOutputStream();
                String body = "<center><h1>Hello from Java Server 2!</h1></center>";
                String response = "HTTP/1.1 200 OK\r\n"+"Content-Type: text/html\r\n"+"Content-Length: "+body.length() + "\r\n" + 
                                    "Connection: close\r\n" + "\r\n" + body;                
                out.write(response.getBytes());
                out.flush();
                out.close();
                client.close();
            }
            catch(Exception e){
                if(running)
                System.out.println("Error handling client "+e.getMessage());
                else
                System.out.println("Server stopped");
            }
        }
    }
}
