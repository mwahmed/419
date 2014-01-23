import java.io.*;
import java.net.*;

public class BrokerClient {
	public static void main(String[] args) throws IOException,
			ClassNotFoundException {

		Socket echoSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		
		Socket brokerSocket = null;
		ObjectOutputStream broker_out = null;
		ObjectInputStream broker_in = null;
		
		String broker = null;
		String broker_host = null;
		String broker_port = null;

		boolean conn = false;
		
		try {
			/* variables for hostname/port */
			String hostname = "localhost";
			int port = 4444;
			
			if(args.length == 2 ) {
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

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;

		System.out.println("Enter command, symbol or x for exit:");
		System.out.print("> ");
		while ((userInput = stdIn.readLine()) != null
				&& !userInput.toLowerCase().equals("x")) {
			/* make a new request packet */
			
			/*String firstWord = null;
			if(userInput.contains(" ")){
			   firstWord= userInput.substring(0, userInput.indexOf(" ")); 
			}*/
			
			if (userInput.toLowerCase().contains("local"))
			{
				String[] tmp = userInput.split(" ");
				String tmp_name = broker;
				broker = tmp[1].toLowerCase();
				conn = false;
				BrokerPacket packetToLookup = new BrokerPacket();
				packetToLookup.type = BrokerPacket.LOOKUP_REQUEST;
				packetToLookup.symbol = broker;
				out.writeObject(packetToLookup);

				/* print server reply */
				BrokerPacket packetFromLookup;
				packetFromLookup = (BrokerPacket) in.readObject();
				if (packetFromLookup.type == BrokerPacket.LOOKUP_ERROR)
				{
					System.out.println(packetFromLookup.symbol);
					broker = tmp_name;
				}
				else
				{
						
					//System.out.println(packetFromLookup.symbol);
					String reply[] = packetFromLookup.symbol.split(";");
					
					broker_host = reply[1];
					broker_port = reply[2];
				}
			}
			
			/*
		Socket brokerSocket = null;
		ObjectOutputStream broker_out = null;
		ObjectInputStream broker_in = null;
		
		String broker_host = null;
		String broker_port = null;
*/
			if (conn == false && broker_host != null)
			{
				
				try {
					/* variables for hostname/port */
					String hostname = "localhost";
					int port = 4444;
					
					if(args.length == 2 ) {
						hostname = broker_host;
						port = Integer.parseInt(broker_port);
					} else {
						System.err.println("ERROR: Invalid arguments!");
						System.exit(-1);
					}
					if (brokerSocket != null)
					{
						broker_out.close();
						broker_in.close();
						brokerSocket.close();
					}
					
					brokerSocket = new Socket(hostname, port);
		
					broker_out = new ObjectOutputStream(brokerSocket.getOutputStream());
					broker_in = new ObjectInputStream(brokerSocket.getInputStream());
					conn = true;
					System.out.println(broker + " as local.");
				} catch (UnknownHostException e) {
					System.err.println("ERROR: Don't know where to connect!!");
					System.exit(1);
				} catch (IOException e) {
					System.err.println("ERROR: Couldn't get I/O for the connection.");
					System.exit(1);
				}
				
				
				
			}
			
			if(conn == false)
			{
				System.out.println("Please do a lookup first");
				System.out.print("> ");
			}
			else
			{
				if (!userInput.toLowerCase().contains("local"))
				{
					BrokerPacket packetToServer = new BrokerPacket();
					packetToServer.type = BrokerPacket.BROKER_REQUEST;
					packetToServer.symbol = userInput.toLowerCase();
					broker_out.writeObject(packetToServer);
		
					/* print server reply */
					BrokerPacket packetFromServer;
					packetFromServer = (BrokerPacket) broker_in.readObject();
		
					if (packetFromServer.type == BrokerPacket.BROKER_QUOTE)
					{
						System.out.println("Quote from broker: " + packetFromServer.quote);
					}
						
		
					if (packetFromServer.type == BrokerPacket.BROKER_ERROR)
					{
						System.out.println(packetFromServer.symbol);
					}
				}
				/* re-print console prompt */
				System.out.print("> ");
			}
		}
	

		/* tell server that i'm quitting */
		BrokerPacket packetToServer = new BrokerPacket();
		packetToServer.type = BrokerPacket.BROKER_BYE;
		packetToServer.symbol = "x";
		

		if (brokerSocket != null)
		{
			broker_out.writeObject(packetToServer);
			broker_out.close();
			broker_in.close();
			brokerSocket.close();
		}

		out.close();
		in.close();
		stdIn.close();
		echoSocket.close();
	}
}
