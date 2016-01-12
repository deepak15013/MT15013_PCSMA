import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class serverApp {

    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static InputStream inputStream;
    private static FileOutputStream fileOutputStream;
    private static BufferedOutputStream bufferedOutputStream;
    private static int filesize = 10000000; // filesize temporary hardcoded 
    private static int bytesRead;
    private static int current = 0;

    public static void main(String[] args) throws IOException {


        serverSocket = new ServerSocket(4444);  //Server socket

        System.out.println("Server started. Listening to the port 4444");


        clientSocket = serverSocket.accept();


        byte[] mybytearray = new byte[filesize];    //create byte array to buffer the file

        inputStream = clientSocket.getInputStream();
        fileOutputStream = new FileOutputStream("/home/deepakpc/Desktop/values.csv"); //if using windows change the location to store the file
        bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

        System.out.println("Receiving...");

        //following lines read the input slide file byte by byte
        bytesRead = inputStream.read(mybytearray, 0, mybytearray.length);
        current = bytesRead;

        do {
            bytesRead = inputStream.read(mybytearray, current, (mybytearray.length - current));
            if (bytesRead >= 0) {
                current += bytesRead;
            }
        } while (bytesRead > -1);


        bufferedOutputStream.write(mybytearray, 0, current);
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        inputStream.close();
        clientSocket.close();
        serverSocket.close();

        System.out.println("Sever recieved the file");
	System.out.println("File Saved");

    }
}
