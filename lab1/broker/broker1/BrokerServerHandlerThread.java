import java.net.*;
import java.util.Scanner;
import java.io.*;

public class BrokerServerHandlerThread extends Thread {
	private Socket socket = null;

	public BrokerServerHandlerThread(Socket socket) {
		super("EchoServerHandlerThread");
		this.socket = socket;
		System.out.println("Created new Thread to handle client");
	}

	public void run() {

		boolean gotByePacket = false;
		
		try {
			/* stream to read from client */
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			BrokerPacket packetFromClient;
			
			/* stream to write back to client */
			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
			
			String content = new Scanner(new File("nasdaq")).useDelimiter("\\Z").next();
			//System.out.println(content);
			while (( packetFromClient = (BrokerPacket) fromClient.readObject()) != null) {
				/* create a packet to send reply back to client */
				BrokerPacket packetToClient = new BrokerPacket();
				packetToClient.type = BrokerPacket.BROKER_QUOTE;
				
				/* process message */
				/* just echo in this example */
				if(packetFromClient.type == BrokerPacket.BROKER_REQUEST) {
					
					//System.out.println("From Client: " + packetFromClient.message);
					packetToClient.quote = (long) 0;
					String[] parts = content.split("\n");
					
					for (String s: parts)
				    {
				      String[] tmp = s.split(" ");
				      String company = tmp[0];
				      String value = tmp[1];
				      //System.out.println("Request: " + packetFromClient.symbol);
				      //System.out.println("STRING: " + company);
				      //System.out.println("value: " + Long.parseLong(value));
				      if (packetFromClient.symbol.equals(company)){
				    	  packetToClient.quote = Long.parseLong(value); //packetFromClient.message;
				    	  break;
				      }
				      
				    }
			
					
					//String part1 = parts[0]; // 004
					//String part2 = parts[1]; // 034556
				
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					/* wait for next packet */
					continue;
				}
				
				/* Sending an ECHO_NULL || ECHO_BYE means quit */
				if (packetFromClient.type == BrokerPacket.BROKER_NULL || packetFromClient.type == BrokerPacket.BROKER_BYE) {
					gotByePacket = true;
					packetToClient = new BrokerPacket();
					packetToClient.type = BrokerPacket.BROKER_BYE;
					packetToClient.quote = (long) 0;
					toClient.writeObject(packetToClient);
					break;
				}
				
				/* if code comes here, there is an error in the packet */
				System.err.println("ERROR: Unknown ECHO_* packet!!");
				System.exit(-1);
			}
			
			/* cleanup when client exits */
			fromClient.close();
			toClient.close();
			socket.close();

		} catch (IOException e) {
			if(!gotByePacket)
				e.printStackTrace();
		} catch (ClassNotFoundException e) {
			if(!gotByePacket)
				e.printStackTrace();
		}
	}
}
