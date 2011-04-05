package webproxy;

/**
 * Stores an HTTP job
 * 
 * @author Amos Wenger
 */
public class HTTPJob {

	HTTPRequest request;
	int clientCid;

	public HTTPJob(HTTPRequest request, int clientCid) {
		this.request = request;
		this.clientCid = clientCid;
	}

	public HTTPRequest getRequest() {
		return request;
	}
	
	public int getClientCid() {
		return clientCid;
	}
	
}
