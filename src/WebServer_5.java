//Routing based on the url /hello returns hello message, /time for the current time, and / for index.html from the disk
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Scanner;

public class WebServer_5{
    private static volatile boolean running=true;
    public static void main(String[] args)throws Exception{
        ServerSocket server = new ServerSocket(8080);
        System.out.println("Router Server running at http://localhost:8080");

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
            
            //parse with path default to "/index.html"
            String[] parts = requestLine.split(" ");
            String path = parts[1];
            System.out.println("Request for: "+path);

            String body;
            String contentType = "text/html";

            //Routing Logic
            if(path.equals("/hello")){
                body = "<h1>Hello World! Your request has successfully reached the backend</h1>";
                sendResponse(out, 200, contentType, body.getBytes());
            }
            else if(path.equals("/time")){
                body = "<h1>The Time is: "+ LocalDateTime.now() + "</h1>";
                sendResponse(out, 200, contentType, body.getBytes());
            }
            else if(path.equals("/")){
                Path filePath = Paths.get("public", "index.html");
                if(Files.exists(filePath)){
                    byte[] content = Files.readAllBytes(filePath);
                    sendResponse(out, 200, contentType, content);
                    return;
                }
                else{
                    body = "<h1>index.html not found!</h1>";
                    sendResponse(out, 404, contentType, body.getBytes());
                }
            }
            else{
                body = "<h1>404 - Page NOT Found</h1>";
                sendResponse(out, 404, contentType, body.getBytes());
            }
        }
        catch(Exception e){
        System.out.println("Error :"+e.getMessage());
        }
        finally{
            try{
                client.close();
            }catch(IOException ignored) {}
        }
    }

    private static void sendResponse(OutputStream out, int status, String contentType, byte[] body) throws IOException{
        String statusText = (status == 200) ? "OK" : "Not Found";
        String header="HTTP/1.1 " + status + " " + statusText +"\r\n"
                      +"Content-Type: "+ contentType +"\r\n"+"Content-Length: "+ body.length+ 
                              "\r\n" + "Connection: close\r\n\r\n";
        out.write(header.getBytes());
        out.write(body);

    }
}
