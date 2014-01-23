import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.*;

public class LookupServerHandlerThread extends Thread {
	private Socket socket = null;
	
	public static String nasdaq = null;
	public static String tse = null;

	public LookupServerHandlerThread(Socket socket) {
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
			

			//String nasdaq1 = null;
			
			while (( packetFromClient = (BrokerPacket) fromClient.readObject()) != null) {
				/* create a packet to send reply back to client */
				BrokerPacket packetToClient = new BrokerPacket();
				/* process message */
				/* just echo in this example */


				if(packetFromClient.type == BrokerPacket.LOOKUP_REGISTER) {
					
					
					if (packetFromClient.symbol.contains("tse"))
					{
						tse = packetFromClient.symbol.toLowerCase();
						System.out.println("TSE Server " + tse);
					}
					else
					{
						nasdaq = packetFromClient.symbol.toLowerCase();
						System.out.println("NASDAQ Server " + nasdaq);
					}
					
					/* wait for next packet */
					continue;
				}
				
				if(packetFromClient.type == BrokerPacket.LOOKUP_REQUEST) {
					
					
					if (packetFromClient.symbol.contains("tse"))
					{
						if (tse == null)
						{

							packetToClient.type = BrokerPacket.LOOKUP_ERROR;
							packetToClient.symbol = "Invalid Lookup, no such server running.";
						}
						else
						{

							packetToClient.type = BrokerPacket.LOOKUP_REPLY;
							packetToClient.symbol = tse;
						}
					}
					else if (packetFromClient.symbol.contains("nasdaq"))
					{
						System.out.println("requested nasdaq broker");
						if (nasdaq == null)
						{
							packetToClient.type = BrokerPacket.LOOKUP_ERROR;
							System.out.println(nasdaq);
							packetToClient.symbol = "Invalid Lookup, no such server running.";
						}
						else
						{
							packetToClient.type = BrokerPacket.LOOKUP_REPLY;
							packetToClient.symbol = nasdaq;
						}
						
					}
					else
					{
						packetToClient.type = BrokerPacket.LOOKUP_ERROR;
						packetToClient.symbol = "Invalid Lookup, no such server running.";
					}
					
					toClient.writeObject(packetToClient);
					/* wait for next packet */
					continue;
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
