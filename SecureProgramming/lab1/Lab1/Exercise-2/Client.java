import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	final String server;
	final int port;
	final int MAX_LENGTH = 140;

	public Client(String server, int port) {
		this.server = server;
		this.port = port;
	}

	public void send_message() {
		PrintWriter noticeboard = null;

		BufferedReader in = new BufferedReader( 
				new InputStreamReader(System.in));
		String msg;

		try {
			noticeboard = 
			  new PrintWriter(
					  (new Socket(this.server, this.port)).getOutputStream(), true);
	
		} catch (UnknownHostException e) {
			System.err.println("Unknown host");
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println("Unable to open socket");
			System.err.println(e.getMessage());
		}
		
		System.out.println("Enter your message, followed by a newline:");
		try {
			msg = in.readLine();

			if (msg.length() > this.MAX_LENGTH) {
				System.err.println("Message is too long ("+msg.length()+" characters).");
				System.err.println("Please keep all messages under "+this.MAX_LENGTH+" characters");
			} else if (msg.length() == 0) {
				System.err.println("The message is empty");
			} else {
				noticeboard.println(msg.length());
				noticeboard.println(msg);
				noticeboard.flush();
			}		
		} catch (IOException e) {
			System.err.println("Failed to read message");
			System.err.println(e.getMessage());
		}

	}

	public static void main(String[] args) {
		String server = null;
		int port = -1;
		
		try {
			server = args[0];
			port = Integer.parseInt(args[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Please provide a server as the first argument and a port number as the second argument");
			System.exit(-1);
		}
		

		Client client = new Client(server, port);
		client.send_message();
	}
}
