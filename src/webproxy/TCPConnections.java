package webproxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

import webproxy.exceptions.HTTPServerException;
import webproxy.exceptions.NotImplementedException;
import webproxy.exceptions.ProxyException;

/**
 * This class models TCP Connections for a proxy.
 * It allows (1) to wait TCP connections from clients (i.e., browsers) on a specific port,
 *  (2) establish new connections to HTTP servers, and
 *  (3) send and receive HTTP requests and replies through TCP connections.
 *
 * @author LSR
 */
public class TCPConnections {
	
	/**
	 *  Open connections
	 */
	private final Hashtable<Integer,Connection> connections;
	
	/**
	 *  CID generator 
	 */
	private final AtomicInteger nextCID;
	
	/**
	 *  The TCP server 
	 */
	protected final ServerSocket client_server;

	/** 
	 * The default value (10 seconds) for the delay after which
	 *  a connection initialization is aborted. 
	 */
	final static private int DEFAULT_SOCKET_TIMEOUT = 10000;
	
	/**
	 * Sole constructor.  
	 * 
	 * @param port the port on which the proxy waits for browser connections.
	 * @throws ProxyException  if an error occurs while initializing the TCP connections servers.
	 */
	protected TCPConnections(int port) throws ProxyException {
				
		connections = new Hashtable<Integer,Connection>();
		nextCID = new AtomicInteger();
		try {
			client_server = new ServerSocket(port);
		} catch(IOException ioe) {
			throw new ProxyException(ioe);
		}
	}
	
	/**
	 * This method blocks until a new browser connects the proxy, and returns
	 * the CID of the corresponding connection.
	 * 
	 * @return the connection ID (CID) for the new connection.
	 * @throws ProxyException if an error occurs while accepting a new browser connection.
	 */
	protected int getNewClientConnection() throws ProxyException {
		try {
			Socket client = client_server.accept();
			Connection connection = new Connection(client);
			int cid = nextCID.getAndIncrement();
			connections.put(new Integer(cid), connection);
			return cid;
		} catch(IOException ioe) {
			throw new ProxyException(ioe);
		}
	}
	
	/**
	 * This method waits for an HTTP request sent through the connection specified
	 *  by the connection id (CID).
	 *
	 * @param cid the connection ID (CID).
	 * @return the HTTP request received from the browser.
	 * @throws ProxyException if an error occurs while receiving the HTTP request.
	 * @throws NotImplementedException if the request does not fullfills assumptions.
	 */
	protected HTTPRequest getHTTPRequest(int cid) throws ProxyException, NotImplementedException {
		try {
			Connection connection = (Connection) connections.get(new Integer(cid));
			if(connection == null)
				throw new ProxyException("Non-existent TID: "+cid);
			return connection.readRequest();
		} catch(IOException ioe) {
			throw new ProxyException(ioe);
		}
	}
	
	/**
	 * This method sends a HTTP request through the connection specified by the
	 * connection ID (CID).
	 *
	 * @param cid the connection ID (CID).
	 * @param request the HTTP request to be sent. 
	 * @throws ProxyException if an error occurs while sending the request.
	 */
	protected void sendHTTPRequest(int cid, HTTPRequest request) throws ProxyException {
		try {
			Connection connection = (Connection) connections.get(new Integer(cid));
			if(connection == null)
				throw new ProxyException("Non-existent TID: "+cid);
			connection.sendRequest(request);
		} catch(IOException ioe) {
			throw new ProxyException(ioe);
		}	
	}
	
	/**
	 * This method waits for a response sent hrough the connection specifed by the
	 * connection ID (CID).
	 *
	 * @param cid the connection ID (CID).
	 * @return the HTTP response received through the connection.
	 * @throws ProxyException if an error occurs while receiving the response.
	 */
	protected HTTPResponse getHTTPResponse(int cid) throws ProxyException {
		try {
			Connection connection = (Connection) connections.get(new Integer(cid));
			if(connection == null)
				throw new ProxyException("Non-existent TID: "+cid);
			return connection.readResponse();
		} catch(IOException ioe) {
			ioe.printStackTrace();
			System.out.println("ERROR: "+ioe.getMessage());
			throw new ProxyException(ioe);
		}
	}
	
	/**
	 * This method sends a response through the connection specifed by the
	 * connection ID (CID).
	 *
	 * @param cid the transaction ID (CID)
	 * @param response the HTTP response to be sent.
	 * @throws ProxyException if an error occurs while sending the response.
	 */
	protected void sendHTTPResponse(int cid, HTTPResponse response) throws ProxyException {
		try {
			Connection connection = (Connection) connections.get(new Integer(cid));
			if(connection == null)
				throw new ProxyException("Non-existent TID: "+cid);
			connection.sendResponse(response);
		} catch(IOException ioe) {
			throw new ProxyException(ioe);
		}	
	}
	
	/**
	 * This method establish a connection with a remote HTTP server, and returns the CID of
	 *  the corresponding connection.
	 * 
	 * @param hostName the name of the remote HTTP server (e.g., www.google.com).
	 * @param port the port to establish the connection with the remote HTTP server (usually, port = 80). 
	 * @return the connection ID (CID) for the new connection.
	 * @throws ProxyException if an error occurs while establishing a new connection to a server.
	 */
	protected int establishConnectionToHTTPServer(String hostName, int port) throws HTTPServerException {
		// Establish a connection with the server id
		Connection connection = null;
		
		try {			
			InetAddress to = InetAddress.getByName(hostName);
			
			Socket socket = new Socket(to, port);			
			connection = new Connection(socket);
		} catch (Exception ste) {
			// If a problem occurs during establishing a socket, an exception
			// is thrown but the socket is not closed. In this case, we 
			// must close the socket explicitly.
			try {
				if (connection != null)
					connection.close();
			} catch (IOException e) {
			}
			
			throw new HTTPServerException("Problem during the initialization of the connection: "+ste);
		}
		
		int cid = nextCID.getAndIncrement();
		connections.put(new Integer(cid), connection);
		return cid;
	}
			
	/**
	 * This method closes the connection specified by the connection ID (CID).
	 *
	 * @param cid the connection ID (CID)
	 * @throws ProxyException if an error occors while closing the connection.
	 */
	protected void closeConnection(int cid) throws ProxyException {
		try {
			Connection connection = connections.remove(new Integer(cid));
			if(connection == null)
				throw new ProxyException("Non-existent CID: "+cid);
			connection.close();
		} catch(IOException ioe) {
			throw new ProxyException(ioe);
		}
	}
	
	/**
	 * Inner class that represents a connection from a browser or to a HTTP server.
	 */
	private class Connection {
		protected final Socket socket;
		protected final DataInputStream inStream;
		protected final DataOutputStream outStream;
		
		// Sole constructor
		protected Connection(Socket socket) throws IOException {
			this.socket = socket;
			socket.setTcpNoDelay(false);
			socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);
			inStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			outStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		}

		// Read a request sent through the connection
		synchronized public HTTPRequest readRequest() throws IOException, ProxyException, NotImplementedException {
		    BufferedReader bin = new BufferedReader(new InputStreamReader(inStream));

		    // Parse the request
			String line = bin.readLine();
			if ((line != null) && (line.length() != 0)) {
				// First line
				// Parse the method
				int index = line.indexOf(" ");
				String method = line.substring(0, index).trim();
				if ((!method.toUpperCase().equals("GET")) &&
					(!method.toUpperCase().equals("HEAD")))
					throw new NotImplementedException(method);
				
				// Parse the URL
				String remainder = line.substring(index).trim();
				index = remainder.indexOf(" ");
				String url = remainder.substring(0, index).trim();

				// Parse the Version
				String httpVersion = remainder.substring(index).trim();
				if (!httpVersion.endsWith("1.1"))
					throw new NotImplementedException(httpVersion);

				// Parse the headers
				StringBuffer headers = new StringBuffer();
				while ((line = bin.readLine()) != null) {
					if (line.length() == 0)
						break;

					// Suppress headers to maintain connection or concerning Proxy-Connection!
					if ((!line.toUpperCase().contains("KEEP-ALIVE")) &&
						(!line.toUpperCase().contains("PROXY-CONNECTION")) &&
						(!line.toUpperCase().contains("CONNECTION: ")))	
						headers.append(line + "\r\n");
				}
				
				// Add an header specifying that connection must be closed after each interaction
				headers.append(new String("Connection: close \r\n"));
				
				return new HTTPRequest(method, url, httpVersion, headers.toString());
			}
			
			throw new ProxyException("Problem while reading a request!");
		}
		
		// Send a request through the connection
		synchronized public void sendRequest(HTTPRequest request) throws IOException {
		    outStream.write(request.getBytes());
			outStream.flush();
		}
		
		// Read a response sent through the connection
		synchronized public HTTPResponse readResponse() throws IOException, ProxyException {
		    ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte [] response = new byte[16384];
			int bytesRead = 0;
			
			while ((bytesRead = inStream.read(response)) != -1) {
				result.write(response, 0, bytesRead);
				result.flush();
			}
			
		    // Parse the response from the result
			return new HTTPResponse(result.toByteArray());
		}
		
		// Send a response through the connection
		synchronized public void sendResponse(HTTPResponse response) throws IOException {
		    outStream.write(response.getBytes());
			outStream.flush();
		}

		// Close the connection
		public void close() throws IOException {
			outStream.close();
			inStream.close();
			socket.close();
		}
	}
}
