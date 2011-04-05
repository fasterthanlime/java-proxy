package webproxy;

/**
 * This class models an HTTP request.
 *
 * @author LSR
 */
public class HTTPRequest {
	private String method;
	private String url;
	private String httpVersion;
	private String headers;
	
	/**
	 * Sole constructor.
	 * 
	 * @param method the method attached to the request.
	 * @param url the url attached to the request.
	 * @param httpVersion the version of HTTP considered by the request.
	 * @param headers all the headers attached to the request.
	 */
	public HTTPRequest(String method, String url, String httpVersion, String headers) {
		this.method = method;
		this.url = url;
		this.httpVersion = httpVersion;
		this.headers = headers;
	}
		
	/**
	 * Return the method attached to the request.
	 * 
	 * @return the method attached to the request.
	 */
	public String getMethod() {
		return this.method;
	}
	
	/**
	 * Return the url attached to the request.
	 * 
	 * @return the url attached to the request.
	 */
	public String getURL() {
		return this.url;		
	}
	
	/**
	 * Return the version of HTTP considered by the request.
	 * 
	 * @return the version of HTTP considered by the request.
	 */
	public String getHTTPVersion() {
		return this.httpVersion;
	}

	/**
	 * Return the value of the header identified by the name passed in parameter.
	 * 
	 * @param headerName the name of the header.
	 * @return the value of the header.
	 */	
	public String getHeaderValue(String headerName) {
		int indexLineStart = 0;
		int indexSemiColumn = headers.indexOf(":", indexLineStart);
				
		// Go through each header (i.e., each line of this.headers)
		// and return the value corresponding to headerName
		while (indexSemiColumn != -1) {			
			int indexLineEnd = headers.indexOf("\r\n", indexSemiColumn);
			String lineHeader = headers.substring(indexLineStart, indexSemiColumn).toLowerCase();
			
			if (lineHeader.equals(headerName.toLowerCase()))
				return headers.substring(indexSemiColumn+1, indexLineEnd).trim();
			
			indexLineStart = indexLineEnd + 2;			
			indexSemiColumn = headers.indexOf(":", indexLineStart);
		}

		return null;
	}
	
	/**
	 * Return a byte array (which can be sent through the network) corresponding to the HTTP request.
	 * 
	 * @return the byte array corresponding to the HTTP request.
	 */
	public byte[] getBytes() {		
		return this.toString().getBytes();
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof HTTPRequest))
			return false;
		
		HTTPRequest hr = (HTTPRequest) o;
		
		return this.toString().equals(hr.toString());
	}
	
	public int hashCode() {
		return this.toString().hashCode();
	}
			
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append(method+" "+url+" "+httpVersion+"\r\n");
		result.append(headers);
		result.append("\r\n");
		return result.toString();
	}
}
