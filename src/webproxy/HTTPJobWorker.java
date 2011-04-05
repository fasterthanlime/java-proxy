package webproxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import webproxy.exceptions.HTTPServerException;
import webproxy.exceptions.ProxyException;

/**
 * Pulls HTTP jobs from the request buffer and processes them.
 * 
 * @author Amos Wenger
 */
public class HTTPJobWorker implements Runnable {

	TCPConnections connections;
	
	RequestBuffer buffer;
	
	Logger logger = Logger.getLogger(getClass().getSimpleName());
	
	public HTTPJobWorker(TCPConnections connections, RequestBuffer buffer) {
		this.connections = connections;
		this.buffer = buffer;
		new Thread(this).start();
	}

	@Override
	public void run() {
		while(true) {
			try {
				HTTPJob job = buffer.pop();
				if(job == null) {
					// sleep a bit and try again
					try {
						Thread.sleep(100L + new Random().nextLong() % 100L);
					} catch (InterruptedException e) { }
					continue;
				}
				
				HTTPRequest request = job.getRequest();
				int clientCid = job.getClientCid();
				
				try {
					URL url = new URL(request.getURL());
					logger.log(Level.FINEST, "request URL = " + url.getPath() + " on host " + url.getHost()
							+ ":" + url.getPort() + " (raw = " + request.getURL()+ ")");
					
					int requestPort = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
					try {
						int serverCid = connections.establishConnectionToHTTPServer(url.getHost(), requestPort);
						
						connections.sendHTTPRequest(serverCid, new HTTPRequest(request.getMethod(), url.getFile(), request.getHTTPVersion(), request.getHeaders()));
						connections.sendHTTPRequest(serverCid, request);
						HTTPResponse response = connections.getHTTPResponse(serverCid);
						logger.log(Level.FINEST, "response = " + response.StatusLineAndHeaders());
						connections.sendHTTPResponse(clientCid, response);
					} catch (HTTPServerException e) {
						connections.sendHTTPResponse(clientCid, new HTTPTextResponse("Unknown host: " + url.getHost()));
					}
				} catch (MalformedURLException e) {
					connections.sendHTTPResponse(clientCid, new HTTPTextResponse("Malformed url: " + request.getURL()));
				}
				connections.closeConnection(clientCid);
			} catch (ProxyException e) {
				e.printStackTrace();
			}
		}
	}

}
