package chatserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;


public class Session {
	private String username;
	private Socket socket;
	private PrintWriter outputWriter;
	private BufferedReader inputBuffer;
	
	/** Constructor establishes a Connection for a given connected socket.
	 * 
	 * @param socket A connected socket for communication.
	 */
	Session(Socket socket) {
            this.socket = socket;
            try {
                inputBuffer = new BufferedReader(new InputStreamReader(this.socket.getInputStream())); 
                outputWriter = new PrintWriter(this.socket.getOutputStream(), true);
            } catch(IOException e) {
                System.err.println(e);
                e.printStackTrace();
            }
	}
	
	/** Write to the connection socket */
	public void write(String msg) {
            outputWriter.println(msg);
            outputWriter.flush();
	}
	
	/** Attempt to read from the connection socket. */
	public String read() {
            String line = null;
            try {
                line = inputBuffer.readLine();
            }
            catch(SocketException e) {
            	System.out.println("Log: Client disconnected, session ended");
            }
            catch(IOException e) {
                System.err.println(e);
                e.printStackTrace();
            }
            return line;
	}
	
	/** Attempt to close the connection, including input/output streams. */
	public boolean disconnect() {
            try {
                socket.close();
                inputBuffer.close();
            } catch(IOException e) {
                System.err.println(e);
                e.printStackTrace();
                return false;
            }
            outputWriter.close();
            return true;
	}
	
	/** Set the username associated with the given connection */
	public void setUsername(String username) {
            this.username = username;
	}
	
	public Socket getSocket() { return socket; }
	
	public String getUsername() { return username; }
}