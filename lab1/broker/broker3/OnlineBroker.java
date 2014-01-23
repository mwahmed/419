import java.net.*;
import java.io.*;

public class OnlineBroker {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        boolean listening = true;

        try {
        	if(args.length == 4) {
        		serverSocket = new ServerSocket(Integer.parseInt(args[2]));
        	} else {
        		System.err.println("ERROR: Invalid arguments!");
        		System.exit(-1);
        	}
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on port " + args[2]);
            System.exit(-1);
        }
        
        Socket lookupSocket = null;
		ObjectOutputStream toLookup = null;
		ObjectInputStream in = null;

		try {
			/* variables for hostname/port */
			String hostname = "localhost";
			int port = 4444;
			
				hostname = args[0];
				port = Integer.parseInt(args[1]);

			lookupSocket = new Socket(hostname, port);

			toLookup = new ObjectOutputStream(lookupSocket.getOutputStream());
			in = new ObjectInputStream(lookupSocket.getInputStream());

		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
		}


		
		BrokerPacket packetToLookup = new BrokerPacket();
		packetToLookup.type = BrokerPacket.LOOKUP_REGISTER;
		packetToLookup.symbol = args[3].toLowerCase() + ";" + args[0].toLowerCase() + ";" + Integer.parseInt(args[2]);

		try {
			toLookup.writeObject(packetToLookup);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			toLookup.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			lookupSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        
        
        
        
        

        while (listening) {
        	new BrokerServerHandlerThread(serverSocket.accept(), args[0], Integer.parseInt(args[1]), args[3], Integer.parseInt(args[2])).start();
        }

        serverSocket.close();
    }
}
