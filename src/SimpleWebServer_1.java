import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleWebServer_1{
    public static void main(String[] args) throws Exception{
        ServerSocket server=new ServerSocket(8080);//wait for connection
        System.out.println("Server is running on http://localhost:8080");
        Socket client = server.accept();//to accept the first browser that connects
        OutputStream out = client.getOutputStream();
        String body = "<center><h1>Hello from Simple Java Server!</h1></center>";
        String response = "HTTP/1.1 200 OK\r\n"+"Content-Type: text/html\r\n"+"Content-Length: "+body.length() + "\r\n" + 
                             "Connection: close\r\n" + "\r\n" + body;                
        out.write(response.getBytes());
        System.out.println("Closing...");
        out.close();
        client.close();
        server.close();
    }
}
