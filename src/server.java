import java.io.*;
import java.net.*;

public class server {
	 private ObjectOutputStream outputToFile;
	 private ObjectInputStream inputFromClient;
	
	 public static void main(String[] args) {
		 new server();
	 }
	
	 @SuppressWarnings({ "resource", "unused" })
	public server() {
		 try {
			 // Create a server socket
			 ServerSocket serverSocket = new ServerSocket(8000);
			 System.out.println("Server started ");
	
			 // Create an object output stream
			 outputToFile = new ObjectOutputStream(new FileOutputStream("student.dat", true));
	
			 while (true) {
				 // Listen for a new connection request
				 Socket socket = serverSocket.accept();
	
				 // Create an input stream from the socket
				 inputFromClient = new ObjectInputStream(socket.getInputStream());
	
				 // Read from input
				 Object object = inputFromClient.readObject();
	
				 // Write to the file
				 mineRecord s = (mineRecord)object;
				 System.out.println("got object " + object.toString());
				 outputToFile.writeObject(object);
				 outputToFile.flush();
				 System.out.println("A new object is stored");
			 }
		}
		 catch(ClassNotFoundException ex) {
			 ex.printStackTrace();
		 }
		 catch(IOException ex) {
			 ex.printStackTrace();
		 }
		 finally {
			 try {
				 inputFromClient.close();
				 outputToFile.close();
			 }
			 catch (Exception ex) {
				 ex.printStackTrace();
			 }
		 }
	 }
}
