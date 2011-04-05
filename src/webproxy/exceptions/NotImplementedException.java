package webproxy.exceptions;

/**
 * This exception is thrown when the proxy receives a request 
 * (1) with a method that is not supported (i.e., a method different than GET or HEAD), or
 * (2) using a version of HTTP different than 1.1
 * 
 * @author LSR
 */

public class NotImplementedException extends ProxyException {
	private static final long serialVersionUID = 842556379388340254L;

	public NotImplementedException(String message) {
		super(message);
    }

    public NotImplementedException(String message, Throwable cause) {
    	super(message,cause);
    }
    
    public NotImplementedException(Throwable cause) {
    	super(cause);
    }

}
