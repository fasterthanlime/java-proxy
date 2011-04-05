package webproxy;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import webproxy.exceptions.ProxyException;

public class WebProxy {

	/** 8080 is usually the port used by proxies. */
	public static final int DEFAULT_PORT = 8080;
	
	/** 'cause System.out.println is ugly */
	Logger logger = Logger.getLogger(getClass().getSimpleName());

	/** Used to dispatch jobs between worker threads */
	RequestBuffer buffer;
	
	/** Handles all TCP connection establishing/closing and HTTP response/request reading/sending */
	TCPConnections connections;
	
	/** Filtered out domains */
	ArrayList<String> blockedDomains = new ArrayList<String>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO: make number of worker threads specifiable by command-line option
		// TODO: make port specifiable by command-line option
		// TODO: display help when -h / --help
		
		// TODO: make semaphore/monitor selectable by command-line option
		//RequestBuffer buffer = new MonitorRequestBuffer();
		RequestBuffer buffer = new SemaphoreRequestBuffer();
		
		new WebProxy(DEFAULT_PORT, 20, buffer);
	}
	
	public WebProxy(int port, int numThreads, RequestBuffer buffer) {
		logger.setLevel(Level.FINEST);
		this.buffer = buffer;
		
		try {
			connections = new TCPConnections(port);
		} catch (ProxyException e) {
			logger.log(Level.SEVERE, "Couldn't start proxy listening on port " + port + ", giving up...");
			System.exit(1);
		}
		
		for(int i = 0; i < numThreads; i++) {
			new HTTPJobWorker(connections, buffer);
		}
		
		logger.log(Level.INFO, "Listening on port " + DEFAULT_PORT);
		
		while (true) {
			acceptConnection();
		}
		
	}

	public void acceptConnection() {
		try {
			int clientCid = connections.getNewClientConnection();
			HTTPRequest request = connections.getHTTPRequest(clientCid);
			
			logger.log(Level.FINEST, "Got request: ");
			logger.log(Level.FINEST, new String(request.getBytes()));
			
			buffer.queue(new HTTPJob(request, clientCid));
		} catch (ProxyException e) {
			// FIXME: handle that better
			e.printStackTrace();
		}
		
	}

}
