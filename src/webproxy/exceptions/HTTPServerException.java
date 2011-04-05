package webproxy.exceptions;

/**
 * This exception is thrown when the proxy cannot connect to
 * the distant HTTP server
 * 
 * @author LSR
 */

public class HTTPServerException extends ProxyException {
	private static final long serialVersionUID = 6529293225225152414L;

	public HTTPServerException(String message) {
		super(message);
    }

    public HTTPServerException(String message, Throwable cause) {
    	super(message,cause);
    }
    
    public HTTPServerException(Throwable cause) {
    	super(cause);
    }

}
