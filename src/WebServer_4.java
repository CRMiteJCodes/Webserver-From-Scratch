import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.Scanner;

public class WebServer_4{
    private static volatile boolean running=true;
    public static void main(String[] args)throws Exception{
        ServerSocket server = new ServerSocket(8080);
        System.out.println("Static file Server running at http://localhost:8080/index.html");

        Thread consoleThread = new Thread(()-> {
            Scanner scan=new Scanner(System.in);
            while(true){
                String input = scan.nextLine();
                if(input.equalsIgnoreCase("exit")){
                    System.out.println("Shutting down via console command...");
                    running = false;
                    try{
                        server.close();
                    }
                    catch(Exception e){
                        System.out.println("Error closing server: "+e.getMessage());
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
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            OutputStream out = client.getOutputStream();

            //Read the first Line: GET /file HTTP/1.1
            String requestLine = in.readLine();
            if(requestLine == null || !requestLine.startsWith("GET")){
                client.close();
                return;
            }
            System.out.println("Request: "+requestLine);
            //parse with path default to "/index.html"
            String[] parts = requestLine.split(" ");

            //Even if the client just requests only the root(localhost:8080/) then the default index.html will be returned by default else whtever the path is
            String path = parts[1].equals("/")?"/index.html":parts[1];

            //remove leading / and join with public folder
            Path filePath = Paths.get("public", path.substring(1));

            if(Files.exists(filePath) && !Files.isDirectory(filePath)){
                byte[] content = Files.readAllBytes(filePath);
                String header="HTTP/1.1 200 OK\r\n"+"Content-Type: text/html\r\n"+"Content-Length: "+content.length+ 
                              "\r\n" + "Connection: close\r\n\r\n";
                              out.write(header.getBytes());
                              out.write(content);
            }
            else{
                String notFound="<h1>404 - File Not Found";
                String header="HTTP/1.1 404 Not Found\r\n"+"Content-Type: text/html\r\n"+"Content-Length: "
                               +notFound.length()+ "\r\n" + "Connection: close\r\n\r\n";
                              out.write(header.getBytes());
                              out.write(notFound.getBytes());
            }
            out.close();
            in.close();
            client.close();
        }
        catch(Exception e){
        System.out.println("Error :"+e.getMessage());
        }
    }
}
