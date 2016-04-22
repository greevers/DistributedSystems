
/*--------------------------------------------------------

1. Name / Date:

Sean Greevers / April 17, 2016

2. Java version used, if not the official version for the class:

Java 1.8

3. Precise command-line compilation examples / instructions:

> javac JokeServer.java


4. Precise examples / instructions to run this program:

> java JokeServer

5. List of files needed for running the program.

 A. JokeServer.java
 B. JokeClient.java
 C. JokeClientAdmin.java

5. Notes:

----------------------------------------------------------*/

import java.io.*;
import java.net.*;
import java.util.*;

class Worker extends Thread {
	
	// ClientType enum to perform normal or admin operations
	public enum ClientType {
		JOKE_REQUEST,
		JOKE_ADMIN
	}
	
	// Class member, socket, local to Worker
	Socket sock;
	
	// Client type
	ClientType client;
	
	// Constructor, assign arg s to local sock
	Worker (Socket s, ClientType c) {
		sock = s;
		client = c;
	}
	
	public void run() {
		// Cet I/O streams in/out from the socket
		PrintStream out = null;
		BufferedReader in = null;
		
		switch (client) {
		case JOKE_REQUEST:
			try {
				// Get an input stream from the socket which will be read into buffer "in"
				in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				
				// Get an output stream from the socket which will be printed to output stream "out"
				out = new PrintStream(sock.getOutputStream());
				
				try {
					if ((JokeServer.currentMode() == JokeServer.Mode.JOKE)) {
						// Print mode (Joke) to client
						out.println("Currently in JOKE mode.");
					} else if (JokeServer.currentMode() == JokeServer.Mode.PROVERB) {
						// Print mode (Proverb) to client
						out.println("Currently in PROVERB mode.");
					} else if (JokeServer.currentMode() == JokeServer.Mode.MAINTENANCE) {
						out.println("The server is temporarily unavailable -- check-back shortly.");
						// Close this connection, but not the server
						sock.close();
						return;
					}
					
					// Read in userName and clientID from client
					String userName = in.readLine();
					Integer clientID = in.read();
					
					// Get new joke or proverb for specified clientID & replace Rando with userName
					String nextJokeOrProverb = JokeServer.getNewJokeOrProverb(clientID).replaceAll("Rando", userName);
					
					// Print joke or proverb to client
					out.println(nextJokeOrProverb);
				} catch (IOException x) {
					System.out.println("Server read error");
					x.printStackTrace();
				}
				
				// Close this connection, but not the server
				sock.close();
			} catch (IOException ioe) {
				System.out.println(ioe);
			}
			break;
		case JOKE_ADMIN:
			try {
				// Get an input stream from the socket which will be read into buffer "in"
				in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				
				// Get an output stream from the socket which will be printed to output stream "out"
				out = new PrintStream(sock.getOutputStream());
				
				// Print current mode to admin client
				out.println(JokeServer.currentMode());
				
				// Read in new mode from admin client and update
				JokeServer.changeServerModeTo(in.readLine());
				
				// Print new mode to admin client
				out.println("Server mode updated: " + JokeServer.currentMode());
				
				// Close this connection, but not the server
				sock.close();
			} catch (IOException ioe) {
				System.out.println(ioe);
			}
			break;
		}
	}
	
}

public class JokeServer {
	
	// Mode enum to keep track of server state
	public enum Mode {
		JOKE,
		PROVERB,
		MAINTENANCE
	}
	
	private static Mode currentMode = Mode.JOKE; // Default mode is joke mode
	private static final int port = 5287; // Use port 5287
	private static Integer[] indices = new Integer[]{0, 1, 2, 3, 4}; // Joke, proverb indices (will be randomized)
	private static String jokesKey = "jokes"; // Jokes map key
	private static String proverbsKey = "proverbs"; // Proverbs map key
	
	// All my jokes!
	private static String[] jokes = {
		"A. Hey Rando, the moon is a planet just like the Earth, only deader.",
		"B. Rando...show me a man who always has two feet on the ground, and I'll show you a man who can't take his pants off.",
		"C. Hey Rando, why doesn't Tarzan have a beard?",
		"D. Just remember Rando...if the world didn't suck, we'd all fall off.",
		"E. You’ll never be as lazy as whoever named the fireplace."
		};
	
	// All my proverbs!
	private static String[] proverbs = {
		"A. Rando...When the going gets tough, the tough get going.",
		"B. Rando...No man is an island.",
		"C. Better late than never.",
		"D. Rando...Actions speak louder than words.",
		"E. Absence makes the heart grow fonder."
		};
	
	// Client map w/ clientID at key, map of randomized Integer list for jokes & proverbs each
	public static Map<Integer, Map<String, List<Integer>>> clientMap = new HashMap<Integer, Map<String, List<Integer>>>();
	
	public static void main(String args[]) throws IOException {
		// Number of requests for OpSys to queue
		int qLen = 6;
		Socket sock;
		
		// Create AdminLooper thread for admin client
	    AdminLooper AL = new AdminLooper();
	    Thread t = new Thread(AL);
	    
	    // Start AdminLooper thread, waiting for admin client
	    t.start();
		
		// Create new server socket bound to port 5287 with backlog of 6
		ServerSocket servsock = new ServerSocket(port, qLen);
		
		// Print server port # to console
		System.out.println("Sean Greevers's JokeServer starting up, listening at port " + port + ".\n");
		while(true) {
			// Wait for the next client connection
			sock = servsock.accept();
			
			// Spawn worker to handle it
			new Worker(sock, Worker.ClientType.JOKE_REQUEST).start();
		}
	}
	
	// Returns JokeServer's current mode
	public static Mode currentMode() {
		return JokeServer.currentMode;
	}
	
	// Change server mode
	public static void changeServerModeTo(String mode) {
		// Set updatedMode to current mode if entered mode is not valid
		JokeServer.Mode updatedMode = JokeServer.currentMode;
		
		if (mode.equals("A")) {
			// Joke mode if user entered A
			updatedMode = JokeServer.Mode.JOKE;
		} else if (mode.equals("B")) {
			// Proverb mode if user entered B
			updatedMode = JokeServer.Mode.PROVERB;
		} else if (mode.equals("C")) {
			// Maintenance mode if user entered C
			updatedMode = JokeServer.Mode.MAINTENANCE;
		}
		
		// Update current mode
		JokeServer.currentMode = updatedMode;
	}
	
	public static void addNewClient(int clientID) {
		// Create new map with list of jokes and list of proverbs
		Map<String, List<Integer>> jokeProverbMap = new HashMap<String, List<Integer>>();
		
		// Add randomly ordered jokes to map
		jokeProverbMap.put(jokesKey, initJokes());
		
		// Add randomly ordered proverbs to map
		jokeProverbMap.put(proverbsKey, initProverbs());
		
		// Add map to client map. Get this map by client id, then get
		// randomized list of jokes or proverbs saved for that client
		clientMap.put(clientID, jokeProverbMap);
	}
	
	public static String getNewJokeOrProverb(Integer clientID) {
		String nextJokeOrProverb;
		if (!clientMap.containsKey(clientID)) {
			// If we have a new client, add it to he client map
			// and add new joke/proverb lists for the client
			addNewClient(clientID);
		}
		
		// Get the joke/proverb list map for the current client
		Map<String, List<Integer>> jokeProverbMap = clientMap.get(clientID);
		
		if (JokeServer.currentMode == JokeServer.Mode.JOKE) {
			// We're in joke mode, get joke list for client
			List<Integer> clientJokes = jokeProverbMap.get(jokesKey);
			
			// Get the next random joke in the client's joke list
			nextJokeOrProverb = jokes[clientJokes.get(0)];
			
			// Remove the first random joke index from the list
			clientJokes.remove(0);
			
			if (clientJokes.isEmpty()) {
				// If we've used up all jokes in random fashion,
				// initialize a new random joke index list and
				// replace the map with the new list
				clientJokes = initJokes();
				jokeProverbMap.replace(jokesKey, clientJokes);
			}
		} else {
			// We're in proverb mode, get proverb list for client
			List<Integer> clientProverbs = jokeProverbMap.get(proverbsKey);
			
			// Get the next random proverb in the client's proverb list
			nextJokeOrProverb = proverbs[clientProverbs.get(0)];
			
			// Remove the first random proverb index from the list
			clientProverbs.remove(0);
			
			if (clientProverbs.isEmpty()) {
				// If we've used up all proverbs in random fashion,
				// initialize a new random proverb index list and
				// replace the map with the new list
				clientProverbs = initProverbs();
				jokeProverbMap.replace(proverbsKey, clientProverbs);
			}
		}
		return nextJokeOrProverb;
	}
	
	private static List<Integer> initJokes() {
		// Create a new list of joke indices (0-4) and randomize their order
		List<Integer> jokes = new ArrayList<>(Arrays.asList(indices));
		Collections.shuffle(jokes);
		return jokes;
	}
	
	private static List<Integer> initProverbs() {
		// Create a new list of proverb indices (0-4) and randomize their order
		List<Integer> proverbs = new ArrayList<>(Arrays.asList(indices));
		Collections.shuffle(proverbs);
		return proverbs;
	}
	
}

class AdminLooper implements Runnable {
	
	// Running the Admin listen loop
	public void run() {
		// Number of requests for OpSys to queue
	    int qLen = 6;
	    
	    // Listening on port 5288 for Admin clients
	    int port = 5288;
	    Socket sock;

	    try {
	    	ServerSocket servsock = new ServerSocket(port, qLen);
	    	
	    	while (true) {
	    		// Wait for the next Admin client connection:
	    		sock = servsock.accept();
	    		
	    		// Spawn worker to handle it
	    		new Worker(sock, Worker.ClientType.JOKE_ADMIN).start(); 
	    	}
	    } catch (IOException ioe) {
	    	System.out.println(ioe);
	    }
	}
	
}
