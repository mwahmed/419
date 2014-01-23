import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.*;

public class BrokerServerHandlerThread extends Thread {
	private Socket socket = null;
	private String role = null;
	private String lookup_host = null;
	private int lookup_port = 0;
	private int port = 0;

	public BrokerServerHandlerThread(Socket socket, String lookup_hostname, int lookup_port, String role_type, int port_num) {
		super("EchoServerHandlerThread");
		this.socket = socket;
		this.role = role_type.toLowerCase();
		this.lookup_host = lookup_hostname.toLowerCase();
		this.lookup_port = lookup_port;
		this.port = port_num;



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
				/*System.out.println(role);
				System.out.println(lookup_host);
				System.out.println(lookup_port);
				*/
				/* create a packet to send reply back to client */
				BrokerPacket packetToClient = new BrokerPacket();
				packetToClient.type = BrokerPacket.BROKER_QUOTE;
				//System.out.println("TYPE IS :" + packetFromClient.type);
				
				String content = new Scanner(new File(role)).useDelimiter("\\Z").next();
				String[] parts = content.split("\n");
				ArrayList<String> list = new ArrayList<String>(Arrays.asList(parts));
				
				/* process message */
				/* just echo in this example */
				if(packetFromClient.type == BrokerPacket.BROKER_REQUEST || packetFromClient.type == BrokerPacket.BROKER_FORWARD) {
					
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
						// Try forwarding first
						//lookup_host
						int bool = 0;
						boolean conn = false;
						int result = 0;
						if (packetFromClient.type == BrokerPacket.BROKER_FORWARD)
						{
							bool=-1;
							result = -1;
						}
						if (bool == 0)
						{
							
							Socket brokerSocket = null;
							ObjectOutputStream broker_out = null;
							ObjectInputStream broker_in = null;
							String broker_host = null;
							String broker_port = null;
							
							
							Socket lookupSocket = null;
							ObjectOutputStream lookup_out = null;
							ObjectInputStream lookup_in = null;
							
							try {
								/* variables for hostname/port */
								String hostname = "localhost";
								int port = 4444;
							
									hostname = lookup_host;
									port = lookup_port;
								
								lookupSocket = new Socket(hostname, port);
	
								lookup_out = new ObjectOutputStream(lookupSocket.getOutputStream());
								lookup_in = new ObjectInputStream(lookupSocket.getInputStream());
	
							} catch (UnknownHostException e) {
								//System.err.println("ERROR: Don't know where to connect!!");
								//System.exit(1);
								result = -1;
							} catch (IOException e) {
								//System.err.println("ERROR: Couldn't get I/O for the connection.");
								//System.exit(1);
								result = -1;
							}
							
							if(result>=0)
							{
								String server = null;
								if (role.equals("tse") )
								{
									server = "nasdaq";
								}
								else
								{
									server = "tse";
								}
								//System.out.println("looking up: " + server);
								BrokerPacket packetToLookup = new BrokerPacket();
								packetToLookup.type = BrokerPacket.LOOKUP_REQUEST;
								packetToLookup.symbol = server;
								lookup_out.writeObject(packetToLookup);
								
								BrokerPacket packetFromLookup;
								packetFromLookup = (BrokerPacket) lookup_in.readObject();
		
								if (packetFromLookup.type == BrokerPacket.LOOKUP_ERROR)
								{
									//System.out.println(packetFromLookup.symbol);
									conn = false;
									//System.exit(-1);
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
									//System.out.println("Contacting the other server");
									try {
										/* variables for hostname/port */
										String hostname = "localhost";
										int port = 4444;
									
											hostname = broker_host;
											port = Integer.parseInt(broker_port);
										
										brokerSocket = new Socket(hostname, port);
		
										broker_out = new ObjectOutputStream(brokerSocket.getOutputStream());
										broker_in = new ObjectInputStream(brokerSocket.getInputStream());
		
									} catch (UnknownHostException e) {
										//System.err.println("ERROR: Don't know where to connect!!");
										//System.exit(1);
										result = -1;
									} catch (IOException e) {
										//System.err.println("ERROR: Couldn't get I/O for the connection.");
										//System.exit(1);
										result = -1;
									}
									if(result>=0)
									{
										//System.out.println("connected to broker");
										BrokerPacket packetToBroker = new BrokerPacket();
										packetToBroker.type = BrokerPacket.BROKER_FORWARD;
										packetToBroker.symbol = packetFromClient.symbol;
										broker_out.writeObject(packetToBroker);
										
										BrokerPacket packetFromBroker;
										packetFromBroker = (BrokerPacket) broker_in.readObject();
										//System.out.println("type returned by other server is " + packetFromBroker.type);
										if (packetFromBroker.type == BrokerPacket.BROKER_ERROR)
										{
											//System.out.println(packetFromLookup.symbol);
											conn = false;
											//System.exit(-1);
										}
										else
										{
											//System.out.println(packetFromLookup.symbol);
											packetToClient.type = BrokerPacket.BROKER_QUOTE;
											packetToClient.quote = packetFromBroker.quote;
											result = 1;
											conn = true;
											bool=0;
										}
									}
									
								}
							
							}
							if (brokerSocket != null)
							{

								broker_out.close();
								broker_in.close();
								brokerSocket.close();
							}
							
							if (lookupSocket != null)
							{
								lookup_out.close();
								lookup_in.close();
													
								lookupSocket.close();
							}
						}
						if (conn == false || result<=0 || bool==-1)
						{
							packetToClient.quote = (long) 0;
							packetToClient.type = BrokerPacket.BROKER_ERROR;
							packetToClient.symbol = company_name.toUpperCase() + " invalid.";
						}
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
								FileWriter writer = new FileWriter(role); 
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
							FileWriter writer = new FileWriter(role); 
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
							FileWriter writer = new FileWriter(role); 
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
							FileWriter writer = new FileWriter(role); 
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
