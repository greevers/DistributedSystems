
/*--------------------------------------------------------

1. Name / Date:

Sean Greevers / April 17, 2016

2. Java version used, if not the official version for the class:

Java 1.8

3. Precise command-line compilation examples / instructions:

> javac JokeClient.java


4. Precise examples / instructions to run this program:

> java JokeClient

OR

> java JokeClient 10.0.0.20

5. List of files needed for running the program.

 A. JokeServer.java
 B. JokeClient.java
 C. JokeClientAdmin.java

5. Notes:

----------------------------------------------------------*/

import java.io.*; // Get the input output libraries
import java.net.*; // Get the Java networking libraries
import java.util.UUID; // Use this library to generate unique client id

public class JokeClient {
	
	// Use port 5287
	private static final int port = 5287;
	
	// Initialized to Rando in case read fails
	private static String userName = "Rando";
	
	// Generate random client UUID for server to keep state for client
	private static UUID clientID = UUID.randomUUID();
	
	public static void main(String[] args) {
		// If no arguments (IP address) is provided, connect to local host
		String serverName = (args.length < 1) ? "localhost" : args[0];
		
		// Read keyboard input to buffer "in"
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		// Ask for user's name
		System.out.println("Enter your name: ");
		try {
			// Read in user's name
			userName = in.readLine();
		} catch (IOException x) {
			x.printStackTrace();
		}
		
		// Print user's joke client
		System.out.println("Using " + userName + "'s JokeServer Joke Client");
		
		// Print server name and port # to console
		System.out.println("Using server " + serverName + ", Port: " + port);
		
		try {
			String input;
			do {
				// Tell user to enter a key to ask the server for something, or type quit to exit
				System.out.print("Press enter for something cool! Or be boring and type 'quit' to exit: ");
				
				// Flush output stream
				System.out.flush();
				
				// Read line from buffer "in" to ask server for something
				input = in.readLine();
				
				// As long as name string does not contain string "quit", get remote address
				if (input.indexOf("quit") < 0) {
					// Send request to server
					talkToServer(serverName);
				}
			} while (input.indexOf("quit") < 0); // Continue while input string does not contain string "quit"
			System.out.println("Cancelled by user request.");
		} catch (IOException x) {
			x.printStackTrace();
		}
	}
	
	static void talkToServer(String serverName) {
		try {
			// Open connection to server on port 5287
			Socket sock = new Socket(serverName, port);
			
			// Get an input stream from the socket which will be read into buffer "fromServer"
			BufferedReader fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			// Get an output stream from the socket which will be printed to output stream "toServer"
			PrintStream toServer = new PrintStream(sock.getOutputStream());
			
			// Print userName to server
			toServer.println(userName);
			
			// Print clientID to server
			toServer.println(clientID);
			
			// Flush output stream
			toServer.flush();
			
			String textFromServer;
			// Read two or three lines of response from the server, and block while synchronously waiting
			for (int i = 1; i <= 3; i++) {
				// Read line from socket to buffer "fromServer"
				textFromServer = fromServer.readLine();
				if (textFromServer != null) {
					// Print text read from server to console
					System.out.println(textFromServer + "\n");
				}
			}
			
			// Close the socket
			sock.close();
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}
	
}
