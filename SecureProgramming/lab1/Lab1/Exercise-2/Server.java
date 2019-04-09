import java.net.*;
import java.io.*;

public class Server {
	final int port;
	final String noticeboard;
	final int buffersize = 140;

	public Server(String noticeboard, int port) {
		this.port = port;
		this.noticeboard = noticeboard;
	}

	public void run() {
		ServerSocket ssocket = null;
		Socket socket = null;
		BufferedReader input = null;
		PrintWriter output = null;
		char[] buffer = new char[140];
		int len;

		// Open the noticeboard file for appending
		System.out.println("Opening noticeboard");
		try {
			output = new PrintWriter(
					new BufferedWriter(
							new FileWriter(this.noticeboard, true)));
		} catch (IOException e) {
			System.err.println("Failed to open noticeboard "+this.noticeboard);
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		// Main loop
		try {
			ssocket = new ServerSocket(this.port);
			while(true) {
				System.out.println("Listening...");
				try {
					socket = ssocket.accept();
					input = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));
					try {	
						len = Integer.parseInt(input.readLine());

						if (len < this.buffersize) {
							System.out.println("Message of length "+len+" receieved");
							input.read(buffer, 0, len);
							output.println();
							output.print(buffer);
							output.println();
							output.flush();
						} else {
							throw new NumberFormatException("length too big: "+len);
						}
					} catch (NumberFormatException e) {
						System.err.println("Bad message length");
						System.err.println(e.getMessage());
					} catch (IOException e) {
						System.err.println("Failed to read input");
						System.err.println(e.getMessage());
					}
				} finally {
					socket.close();
					input.close();
				}
			}

		} catch (IOException e) {
			System.err.println("Failed to setup port "+this.port+" for listening");
			System.err.println(e.getMessage());
		}

		output.close();
	}

	public static void main(String[] args) {
		int port = -1;
		try {
			port = Integer.parseInt(args[0]);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Please provide a port number as the first argument");
			System.exit(-1);
		}

		Server s = new Server("/tmp/noticeboard.txt", port);
		s.run();
	}
}
