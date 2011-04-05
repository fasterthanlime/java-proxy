package webproxy.exceptions;

/**
 * This class models any exception thrown by the proxy. 
 * 
 * @author LSR
 */

public class ProxyException extends Exception {
	private static final long serialVersionUID = 7369670814390540511L;

	public ProxyException(String message) {
		super(message);
    }

    public ProxyException(String message, Throwable cause) {
    	super(message,cause);
    }
    
    public ProxyException(Throwable cause) {
    	super(cause);
    }

}
