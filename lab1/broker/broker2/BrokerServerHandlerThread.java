import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
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
			

			
			
			while (( packetFromClient = (BrokerPacket) fromClient.readObject()) != null) {
				/* create a packet to send reply back to client */
				BrokerPacket packetToClient = new BrokerPacket();
				packetToClient.type = BrokerPacket.BROKER_QUOTE;
				//System.out.println("TYPE IS :" + packetFromClient.type);
				
				String content = new Scanner(new File("nasdaq")).useDelimiter("\\Z").next();
				String[] parts = content.split("\n");
				ArrayList<String> list = new ArrayList<String>(Arrays.asList(parts));
				
				/* process message */
				/* just echo in this example */
				if(packetFromClient.type == BrokerPacket.BROKER_REQUEST) {
					
					//System.out.println("From Client: " + packetFromClient.message);
					int check = 0;
					String company_name = packetFromClient.symbol;
					String company = null;
					
					
					for (String s: list)
				    {
				      String[] tmp = s.split(" ");
				      company = tmp[0];
				      String value = tmp[1];
/*				      System.out.println("Request: " + packetFromClient.symbol);
				      System.out.println("STRING: " + company);
				      System.out.println("value: " + Long.parseLong(value));*/
				      
				      if (packetFromClient.symbol.equals(company)){
				    	  packetToClient.quote = Long.parseLong(value); 
				    	  check = 1;
				    	  break;
				      }
				      
				    }
					if (check == 1)
					{
						packetToClient.type = BrokerPacket.BROKER_QUOTE;
						
					}
					else
					{
						packetToClient.quote = (long) 0;
						packetToClient.type = BrokerPacket.BROKER_ERROR;
						packetToClient.symbol = company_name.toUpperCase() + " invalid.";
					}
					
					//String part1 = parts[0]; // 004
					//String part2 = parts[1]; // 034556
				
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					/* wait for next packet */
					continue;
				}
				if(packetFromClient.type == BrokerPacket.EXCHANGE_ADD) {
					
					//System.out.println("From Client: " + packetFromClient.message);

					
					String[] input_parts = packetFromClient.symbol.split(" "); 
					
					//Check if already exists
					String company_name = input_parts[1];
										
					
					int check = 0;
					for (String s: parts)
					{
						String[] tmp = s.split(" ");
						if (tmp[0].equals(company_name))
						{
							check = -1;
							break;
						}
					}
					
			
					if (check == 0)
					{
						// company not present, can add
						if (input_parts.length == 3)
						{
							// ADD with  price provided
							int value = Integer.parseInt(input_parts[2]);
							// CHECK IF PRICE IN RANGE
							if (value >=1 && value <=300)
							{
							
							
								list.add(company_name + " " + input_parts[2]);
								packetToClient.type = BrokerPacket.EXCHANGE_REPLY;
								packetToClient.symbol = company_name.toUpperCase() + " added.";
								// Write to NASDAQ
								// System.out.println("Writing To NASDAQ");
								FileWriter writer = new FileWriter("nasdaq"); 
								for(String str: list) {
								  writer.write(str + "\n");
								}
								writer.close();
							}
							else
							{
								// Price out of range
								packetToClient.type = BrokerPacket.ERROR_OUT_OF_RANGE;
								packetToClient.symbol = company_name.toUpperCase() + " out of range.";
							}
							
						}
						else
						{
							// ADD using price of 0
							list.add(company_name + " 0\n");
							packetToClient.type = BrokerPacket.EXCHANGE_REPLY;
							packetToClient.symbol = company_name.toUpperCase() + " added.";
							// Write to NASDAQ
							// System.out.println("Writing To NASDAQ");
							FileWriter writer = new FileWriter("nasdaq"); 
							for(String str: list) {
							  writer.write(str + "\n");
							}
							writer.close();
						}
						
					}
					else
					{
						// company present, send error. Can not ADD
						packetToClient.type = BrokerPacket.ERROR_SYMBOL_EXISTS;
						packetToClient.symbol = company_name.toUpperCase() + " exists.";
					}
					
					//String part1 = parts[0]; // 004
					//String part2 = parts[1]; // 034556
				
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					/* wait for next packet */
					continue;
				}
				
				if(packetFromClient.type == BrokerPacket.EXCHANGE_REMOVE) {
					
					//System.out.println("From Client: " + packetFromClient.message);

					
					String[] input_parts = packetFromClient.symbol.split(" "); 
					
					//Check if exists
					String company_name = input_parts[1];
					
					
					int check = 0;
					for (String s: parts)
					{
						String[] tmp = s.split(" ");
						if (tmp[0].equals(company_name))
						{
							check = 1;
							list.remove(s);
							break;
						}
					}
					
			
					if (check == 1)
					{
							// Removed, Send update to packet						
							packetToClient.type = BrokerPacket.EXCHANGE_REPLY;
							packetToClient.symbol = company_name.toUpperCase() + " removed.";
							// Write to NASDAQ
							// System.out.println("Writing To NASDAQ");
							FileWriter writer = new FileWriter("nasdaq"); 
							for(String str: list) {
							  writer.write(str + "\n");
							}
							writer.close();
												
					}
					else
					{
						// company not present, send error. Can not REMOVE
						packetToClient.type = BrokerPacket.ERROR_INVALID_SYMBOL;
						packetToClient.symbol = company_name.toUpperCase() + " invalid.";
					}
					
					//String part1 = parts[0]; // 004
					//String part2 = parts[1]; // 034556
				
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					/* wait for next packet */
					continue;
				}
				
				if(packetFromClient.type == BrokerPacket.EXCHANGE_UPDATE) {
					
					//System.out.println("From Client: " + packetFromClient.message);

					
					String[] input_parts = packetFromClient.symbol.split(" "); 
					
					//Check if exists
					String company_name = input_parts[1];
					int value = Integer.parseInt(input_parts[2]);
					
					int check = 0;
					for (String s: parts)
					{
						String[] tmp = s.split(" ");
						if (tmp[0].equals(company_name))
						{
							check = 1;
							if (value >=1 && value <=300)
							{
								list.remove(s);
							}
							
							break;
						}
					}
					
			
					if (check == 1)
					{
						if (value >=1 && value <=300)
						{
							// Updated, Send update to packet
							list.add(company_name + " " + input_parts[2]);
							packetToClient.type = BrokerPacket.EXCHANGE_REPLY;
							packetToClient.symbol = company_name.toUpperCase() + " updated to " + value + ".";
							// Write to NASDAQ
							// System.out.println("Writing To NASDAQ");
							FileWriter writer = new FileWriter("nasdaq"); 
							for(String str: list) {
							  writer.write(str + "\n");
							}
							writer.close();
							
						}
						else
						{
							packetToClient.type = BrokerPacket.ERROR_OUT_OF_RANGE;
							packetToClient.symbol = company_name.toUpperCase() + " out of range.";
						}
							
							
					}
					else
					{
						// company not present, send error. Can not UPDATE
						packetToClient.type = BrokerPacket.ERROR_INVALID_SYMBOL;
						packetToClient.symbol = company_name.toUpperCase() + " invalid.";
					}
						
					/* send reply back to client */
					toClient.writeObject(packetToClient);
					
					/* wait for next packet */
					continue;
				}
				
				/* Sending an BROKER_NULL || BROKER_BYE means quit */
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
