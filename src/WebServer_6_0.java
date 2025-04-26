import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.Scanner;

public class WebServer_6_0{
    private static volatile boolean running=true;
    public static void main(String[] args)throws Exception {
        ServerSocket server = new ServerSocket(8080);
        System.out.println("MIME type server running at http://localhost:8080/");
        
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
                new Thread(() -> {
                    try {
                        handleClient(client);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            catch(Exception e){
                if(running)
                System.out.println("Error handling client "+e.getMessage());
                else
                System.out.println("Server stopped");
            }
        }
    }

    private static void handleClient(Socket client)throws Exception{
        try(BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            OutputStream out = client.getOutputStream()) {
                String requestLine = in.readLine();
                if (requestLine == null || !requestLine.startsWith("GET")) {
                    client.close();
                    return;
                }
                String[] parts = requestLine.split(" ");
                String path = parts[1];
    
                System.out.println("Request: " + path);
    
                if (path.equals("/hello")) {
                    byte[] content = "<h1>Hello!</h1>".getBytes();
                    sendResponse(out, 200, "text/html", content);
                    return;
                }
    
                // Try serving file from "public"
                Path filePath = Paths.get("public", path.equals("/") ? "index.html" : path.substring(1));
    
                if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                    String contentType = getMimeType(filePath.toString());
                    byte[] content = Files.readAllBytes(filePath);
                    sendResponse(out, 200, contentType, content);
                } else {
                    byte[] content = "<h1>404 - Not Found</h1>".getBytes();
                    sendResponse(out, 404, "text/html", content);
                }    
        }catch(Exception e){
            System.out.println("Error: "+e.getMessage());
        }finally{
            try { client.close(); } catch (IOException ignored) {}
        }

    }

    private static String getMimeType(String filename) {
        if (filename.endsWith(".html")) return "text/html";
        if (filename.endsWith(".css")) return "text/css";
        if (filename.endsWith(".js")) return "application/javascript";
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        if (filename.endsWith(".txt")) return "text/plain";
        return "application/octet-stream"; // default binary
    }

    private static void sendResponse(OutputStream out, int status, String contentType, byte[] body) throws IOException {
        String statusText = (status == 200) ? "OK" : "Not Found";
        String header = "HTTP/1.1 " + status + " " + statusText + "\r\n" +
                        "Content-Type: " + contentType + "\r\n" +
                        "Content-Length: " + body.length + "\r\n" +
                        "Connection: close\r\n\r\n";
        out.write(header.getBytes());
        out.write(body);
    }
}
