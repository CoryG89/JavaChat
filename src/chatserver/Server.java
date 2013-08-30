package chatserver;

import java.net.*;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Server.java
 * 
 * Objects may be instantiated from the Server class by giving a port number.
 * The object will create a ServerSocket instance which will allow the server
 * to listen to incoming connections. Server.java is a multithreaded server
 * implementation. The server spins off a new thread to communicate with each
 * new client connection, while continuing to listen for new incoming connections
 * on the ServerSocket instance on the main thread.
 * 
 * @author Cory Gross
 * @version October 25, 2012
 */
public class Server {
	
	/** The number of current connections (not logged-in sessions) */
	private int connections;
	
	/** Server's socket to listen for incoming connections on. ServerSocket objects
	 *  provide an accept() method that blocks until a client connects at which
	 *  point it returns a regular Socket object for that connection. */
	private ServerSocket listener;
	
	/** A list of active logged-in client sessions. */
	private ArrayList<Session> clientList;
	
	/** The server's database manager provides an interface to the MySQL data
	 *  store supporting the server. */
	private DBManager db;

	/** Flag to limit the maximum number of connections */
	private final int MAX_CONNECTIONS = 0;
	

	/**
	 * Constructor creates a new server given a specified port number.
	 * 
	 * @param port Port number to listen for incoming connections on.
	 */
	Server(short port) {
		connections = 0;
		try {
			listener = new ServerSocket(port);
			clientList = new ArrayList<Session>();
		} catch(IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * Main method for the server to begin running. A Server instance must be
	 * created and run() must be called before any client connections can be
	 * attempted. First the server's DBManager instance is created, connecting
	 * the server to its supporting MySQL data-store. Immediately after a
	 * database connection has been made, the server forever loops, calling
	 * accept on the listener socket. This method blocks until a connection has
	 * been made. A new thread using the ClientHandler runnable instance is
	 * created to communicate with the client.
	 * 
	 * @param args Command line arguments, passed in from the main method.
	 */
	public void run(String[] args) throws IOException {
		System.out.println("Log: Server started.. listening for connections");
	
		BufferedReader in = new BufferedReader(new FileReader("dbauth.dat")); 
		String dbUser = in.readLine();
		String dbPass = in.readLine();
		in.close();
		
		/** Create a DBManager instance for communicating with our DB */
		String dbAddress = "jdbc:mysql://localhost:3306/chatdb";
		db = new DBManager(dbAddress, dbUser, dbPass);		
		
		/** Continually loop and listen for connections while running */
		while (true) {
		
			/** Only accept connections up to the limit specified by the
			    MAX_CONNECTIONS constant, 0 means no new connection limit */
			if (++connections < MAX_CONNECTIONS || MAX_CONNECTIONS == 0) {
				
				/** Block here listening for a client connection */
				Socket client = listener.accept();
				
				/** Create a new thread to communicate with the socket on,
				 *  so that we can continue listening for connections in
				 *  the main thread.  */
				new Thread(new ClientHandler(client, clientList, db)).start();
							
			}
		}
	}
	
	/** Main method to start up the server on a port */
	public static void main(String args[]) {
		
		final short PORT = 1337;
		Server server = new Server(PORT);
		
		try {
			server.run(args);
		} catch (IOException e) {
			System.err.print(e);
			e.printStackTrace();
		}
	}
	
}

/** The ClientHandler class, which is private to Server.java is the
 *  backbone of our server. It implements the Runnable interface allowing
 *  it to be used to create a new Thread which calls run() in a new thread.
 *  The constructor of the class expects a client connection in the form of
 *  a live socket passed to it from the server. It is then responsible for
 *  communicating with the client over that socket on behalf of the server.
 *  
 * @author Cory Gross
 * @version October 25, 2012
 */
class ClientHandler implements Runnable {
	private Session client;
	
	/** List containing all active client sessions, each Session instance
	 *  provides an interface to each client. This list is shared among
	 *  all ClientHandler threads, so updates to the list must be synchronized. */
	private ArrayList<Session> clientList;
	
	/** Reference to the database manager provided by the server. A single instance
	 *  created by the server is shared among all ClientHandler instances. */
	private DBManager db;
	
	/**
	 * Creates a ClientHandler instance to manage each client session and to communicate
	 * with said client on behalf of the server.
	 * 
	 * @param socket Live socket passed in by the server to establish a new client session.
	 * @param cliList Reference to the list of active client sessions provided by the server.
	 * @param database Reference to the database manager provided by the server.
	 */
	ClientHandler(Socket socket, ArrayList<Session> cliList, DBManager database) {
		client = new Session(socket);
		this.clientList = cliList;
		System.out.println("Log: Client connected, new thread created.");
		db = database;
	}
	
	/**
	 * ClientHandler implements the Runnable interface, this is the main method
	 * ran in it's own thread. ClientHandler is a monitor, run() can call several
	 * synchronized methods which can only be entered by one ClientHandler thread
	 * at a given time.
	 */
	public void run() {
		System.out.println("Log: Got input/output streams for connected client.");
		
		/** Get the first message from the client, attempt communication */
		String clientMsg = null;
		boolean accepted = false;
		
		/** Allow client to create an account, login, or quit */
		do {
			clientMsg = client.read();
			if (clientMsg.equals("QUIT")) {
				System.out.println("Log: Client disconnected without signing in.");
				client.disconnect();
				return;
			}
			else if (clientMsg.startsWith("NEWUSER: ")) {
				createUser(clientMsg);
			}
			else if (clientMsg.startsWith("LOGIN: ")) {
				accepted = authenticate(clientMsg);
			}
			else
			{
				System.out.println("Log: Unexpected client message -> " + clientMsg);
				client.disconnect();
				return;
			}
		} while(!accepted);
		
		/** Run main chat loop. Will read from the client, and broadcast each read
		 *  until the client disconnects. */
		while (true) {
			String line = client.read();
			if (line == null) break;
			else {
				broadcast(line);
			}
		}
		
		/** The only way for the client to exit the above loop is to disconnect.
		 *  Therefore, call the handler's exit routine */
		exit();
	}
	
	/**
	 * Given a message from the client containing a desired username and a
	 * password, extract the data and create the user with the database
	 * if it does not exist there already.
	 * 
	 * @param clientMsg Message from client requesting a new account.
	 */
	private synchronized void createUser(String clientMsg) {
		
		/** Extract the username and password from the client message */
		clientMsg = clientMsg.split(" ")[1];
		String username = clientMsg.split(",")[0];
		String password = clientMsg.split(",")[1];
		
		/** Attempt to create the account in the database, send
		 *  appropriate message if the username is taken.
		 */
		try {
			if (db.userExists(username)) {
				client.write("TAKEN");
			}
			else {
				db.createUser(username, password);			
				client.write("USERCREATED");
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Attempts to log the client in given a message from the client containing
	 * login data
	 * 
	 * @param client Message sent from client containing login data.
	 * @return Success (true) or failure (false) of authentication.
	 */
	private synchronized boolean authenticate(String clientMsg) {
		boolean accepted = false;
		
		/** Extract the username and password from the client message */
		clientMsg = clientMsg.split(" ")[1];
		String username = clientMsg.split(",")[0];
		String password = clientMsg.split(",")[1];
		
		/** Attempt to authenticate with the database, send appropriate reply */
		try {
			if (db.authenticate(username, password)) {
				accepted = true;
				
	            client.setUsername(username);
	            client.write("ACCEPTED");
	            clientList.add(client);

	    		updateClientUserList();
	    		
	            System.out.println("Log: Client logged in with username -> " + client.getUsername());
	            broadcast("ChatServer: User " + client.getUsername() + " has joined the chat.");
			}
			else client.write("DENIED");
			
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		
		/** Return success or failure */
		return accepted;
	}
	
	
	/** Exit routine broadcasts the disconnected user event to all clients
	 *  on behalf of the server. disconnects the client socket, and removes
	 *  the client from the server's list of active client sessions.
	 */
	private synchronized void exit() {
		String exitMsg = "ChatServer: User " + client.getUsername();
		exitMsg += " has left the chat.";
		
		/** Broadcast the exit message to each client, and log it */
		broadcast(exitMsg);

		/** Disconnect the client and remove from the clientList */
		client.disconnect();
		clientList.remove(client);
		
		/** Update each of the client's user lists */
		updateClientUserList();
		
		System.out.println("Log: Client socket closed, removed from client list");
	}


	/** Synchronized method which writes to all connections in the
	 *  client list.
	 *  
	 * @param msg Message to be broadcast.
	 */
	private synchronized void broadcast(String msg) {
        for (int i = 0; i < clientList.size(); i++) {
        	clientList.get(i).write(msg);
        }
        System.out.println("Log: Message broadcast --> " + msg);
	}
	
	/** Compiles a list of usernames from the client list and
	 *  broadcasts it to all clients in the list. */
	private synchronized void updateClientUserList() {
            String userList = "USERLIST:";
            for (int i = 0; i < clientList.size(); i++) {
            	userList += " " + clientList.get(i).getUsername();
            }
            broadcast(userList);
	}
}
