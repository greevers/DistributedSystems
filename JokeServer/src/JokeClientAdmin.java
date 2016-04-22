
/*--------------------------------------------------------

1. Name / Date:

Sean Greevers / April 17, 2016

2. Java version used, if not the official version for the class:

Java 1.8

3. Precise command-line compilation examples / instructions:

> javac JokeClientAdmin.java


4. Precise examples / instructions to run this program:

> java JokeClientAdmin

OR

> java JokeClientAdmin 10.0.0.20

5. List of files needed for running the program.

 A. JokeServer.java
 B. JokeClient.java
 C. JokeClientAdmin.java

5. Notes:

----------------------------------------------------------*/

import java.io.*;
import java.net.*;

public class JokeClientAdmin {
	
	// Use port 5288
	private static final int port = 5288;
	
	public static void main(String[] args) {
		// If no arguments (IP address) is provided, connect to local host
		String serverName = (args.length < 1) ? "localhost" : args[0];
		
		// Print admin client string
		System.out.println("Using JokeServer Admin Client");
		
		// Read keyboard input to buffer "in"
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			String input;
			do {
				// Tell user to enter a key to see the server mode, or type quit to exit
				System.out.print("Press enter to view and change server mode, or 'quit' to exit: ");
				
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
			// Open connection to server on port 5288
			Socket sock = new Socket(serverName, port);
			
			// Get an input stream from the socket which will be read into buffer "fromServer"
			BufferedReader fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			// Get an output stream from the socket which will be printed to output stream "toServer"
			PrintStream toServer = new PrintStream(sock.getOutputStream());
			
			// Read keyboard input to buffer "in"
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

			// Print current mode and options for user to choose
			JokeClientAdmin.showCurrentServerModeAndOptions(fromServer.readLine());
			
			// Read in user's new desired mode
			String updatedMode = in.readLine().toUpperCase();
			if (updatedMode.equals("A") || updatedMode.equals("B") || updatedMode.equals("C")) {
				// Else write desired mode to server
				toServer.println(updatedMode);
				
				// Flush output stream
				toServer.flush();
				
				// Read new server mode from server
				System.out.println(fromServer.readLine());
			} else {
				// If entered option is not valid mode, try again
				System.out.println("Invalid option! New mode must be one of the given options. Try again!");
			}
			
			System.out.println("\n");
			
			// Close the socket
			sock.close();
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}
	
	private static void showCurrentServerModeAndOptions(String mode) {
		System.out.println("Current JokeServer mode: " + mode);
		System.out.println("---------------------------------------------");
		System.out.println("A: Joke Mode");
		System.out.println("B: Proverb Mode");
		System.out.println("C: Maintenance Mode");
		System.out.print("Choose option to change mode (or the current mode option to continue the current mode): ");
	}

}
