import java.io.*;
import java.net.*;

public class BrokerExchange {
	public static void main(String[] args) throws IOException,
			ClassNotFoundException {

		Socket echoSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;


		Socket brokerSocket = null;
		ObjectOutputStream broker_out = null;
		ObjectInputStream broker_in = null;
		
		boolean conn = false;
		String broker_host = null;
		String broker_port = null;
		try {
			/* variables for hostname/port */
			String hostname = "localhost";
			int port = 4444;
			
			if(args.length == 3 ) {
				hostname = args[0];
				port = Integer.parseInt(args[1]);
			} else {
				System.err.println("ERROR: Invalid arguments!");
				System.exit(-1);
			}
			echoSocket = new Socket(hostname, port);

			out = new ObjectOutputStream(echoSocket.getOutputStream());
			in = new ObjectInputStream(echoSocket.getInputStream());

		} catch (UnknownHostException e) {
			System.err.println("ERROR: Don't know where to connect!!");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get I/O for the connection.");
			System.exit(1);
		}
		
		BrokerPacket packetToLookup = new BrokerPacket();
		packetToLookup.type = BrokerPacket.LOOKUP_REQUEST;
		packetToLookup.symbol = args[2];
		out.writeObject(packetToLookup);
		
		BrokerPacket packetFromLookup;
		packetFromLookup = (BrokerPacket) in.readObject();

		if (packetFromLookup.type == BrokerPacket.LOOKUP_ERROR)
		{
			System.out.println(packetFromLookup.symbol);
			conn = false;
			System.exit(-1);
		}
		else
		{
			//System.out.println(packetFromLookup.symbol);
			String reply[] = packetFromLookup.symbol.split(";");
			
			broker_host = reply[1];
			broker_port = reply[2];
			conn = true;
		}
		
		if (conn == true)
		{
			try {
				/* variables for hostname/port */
				String hostname = "localhost";
				int port = 4444;
				
				hostname = broker_host;
				port = Integer.parseInt(broker_port);
				//System.out.println("creating connection");
				brokerSocket = new Socket(hostname, port);
				//System.out.println("done connection");

				broker_out = new ObjectOutputStream(brokerSocket.getOutputStream());
				broker_in = new ObjectInputStream(brokerSocket.getInputStream());

			} catch (UnknownHostException e) {
				System.err.println("ERROR: Don't know where to connect!!");
				System.exit(1);
			} catch (IOException e) {
				System.err.println("ERROR: Couldn't get I/O for the connection.");
				System.exit(1);
			}

		
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			String userInput;
	
			System.out.println("Enter Commands or x for exit:");
			System.out.print("> ");
			while ((userInput = stdIn.readLine()) != null
					&& !userInput.toLowerCase().equals("x")) {
				
				
	
	
				String firstWord = null;
				if(userInput.contains(" ")){
				   firstWord= userInput.substring(0, userInput.indexOf(" ")); 
				}
				
				if (firstWord.toLowerCase().equals("add"))
				{
					/* make a new request packet */
					BrokerPacket packetToServer = new BrokerPacket();
					packetToServer.type = BrokerPacket.EXCHANGE_ADD;
					packetToServer.symbol = userInput.toLowerCase();
					broker_out.writeObject(packetToServer);
					
				}
				else if (firstWord.toLowerCase().equals("remove"))
				{
					/* make a new request packet */
					BrokerPacket packetToServer = new BrokerPacket();
					packetToServer.type = BrokerPacket.EXCHANGE_REMOVE;
					packetToServer.symbol = userInput.toLowerCase();
					broker_out.writeObject(packetToServer);
					
				}
				else if (firstWord.toLowerCase().equals("update"))
				{
					/* make a new request packet */
					BrokerPacket packetToServer = new BrokerPacket();
					packetToServer.type = BrokerPacket.EXCHANGE_UPDATE;
					packetToServer.symbol = userInput.toLowerCase();
					broker_out.writeObject(packetToServer);
					
				}
				else
				{
					System.out.println("Unknown Command");
				}
				
				
	
				/* print server reply */
				BrokerPacket packetFromServer;
				packetFromServer = (BrokerPacket) broker_in.readObject();
	
				if (packetFromServer.type == BrokerPacket.EXCHANGE_REPLY || packetFromServer.type<0);
					System.out.println(packetFromServer.symbol);
	
				/* re-print console prompt */
				System.out.print("> ");
			}
	
			/* tell server that i'm quitting */
			BrokerPacket packetToServer = new BrokerPacket();
			packetToServer.type = BrokerPacket.BROKER_BYE;
			packetToServer.symbol = "x";
			broker_out.writeObject(packetToServer);

			broker_out.close();
			broker_in.close();
			out.close();
			in.close();
			stdIn.close();
			brokerSocket.close();
			echoSocket.close();
		}
		
	}
}
