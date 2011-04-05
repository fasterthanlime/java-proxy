package webproxy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import webproxy.exceptions.ProxyException;

/**
 * This class models an HTTP response.
 *
 * @author LSR
 */
public class HTTPResponse {
	private String httpVersion;
	private String status;
	private String reason;
	private String headers;

	private byte[] fullContent;

	/**
	 * The HTTP response returned by this method should be sent as an
	 * an answer to the browser when the request HTTP server cannot be reached.
	 * 
	 * @return HTTP response correspdonding to an unreachable HTTP server
	 * @throws ProxyException if an error occurs when constructing the HTTP response
	 */
	static HTTPResponse createGatewayTimeout() throws ProxyException {
		String content = new String("<html><head>\n" +
							  "<title>504 Gateway Timeout</title>\n" + 
							  "</head><body>\n" +
							  "<h1>504 Gateway Timeout</h1>\n" +
							  "<p>The requested host cannot be reached.</p>\n" +
							  "<hr>\n" +
							  "</body></html>\n");		
		String response = new String("HTTP/1.1 504 Gateway Timeout\r\n"+
							  "Content-Length: " + content.getBytes().length +"\r\n"+
							  "Connection: close\r\n" +
							  "Content-Type: text/html; charset=iso-8859-1\r\n\r\n"+
							  content);

		return new HTTPResponse(response.getBytes());
	}
	
	/**
	 * The HTTP response returned by this method should be sent as an
	 * an answer of a request accessing a forbidden URL.
	 * 
	 * @return HTTP response correspdonding to a request accessing
	 *  a forbidden URL.
	 * @throws ProxyException if an error occurs when constructing the HTTP response
	 */
	static HTTPResponse createForbiddenResponse() throws ProxyException {
		String content = new String("<html><head>\n" +
							  "<title>403 Forbidden</title>\n" + 
							  "</head><body>\n" +
							  "<h1>403 Forbidden</h1>\n" +
							  "<p>The requested URL cannot be accessed.</p>\n" +
							  "<hr>\n" +
							  "</body></html>\n");		
		String response = new String("HTTP/1.1 403 Forbidden\r\n"+
							  "Content-Length: " + content.getBytes().length +"\r\n"+
							  "Connection: close\r\n" +
							  "Content-Type: text/html; charset=iso-8859-1\r\n\r\n"+
							  content);

		return new HTTPResponse(response.getBytes());
	}

	/**
	 * The HTTP response returned by this method should be sent as an
	 * an answer of a request using a method different than GET or HEAD.
	 * 
	 * @return HTTP response correspdonding to a request (1) using
	 * 	 a method different than GET or HEAD, or (2) considering an HTTP version
	 *   different than 1.1 .
	 * @throws ProxyException if an error occurs when constructing the HTTP response
	 */
	static HTTPResponse createNotImplementedResponse() throws ProxyException {
		String content = new String("<html><head>\n" +
				  "<title>501 Not implemented</title>\n" + 
				  "</head><body>\n" +
				  "<h1>501 Not implemented</h1>\n" +
				  "<p>The request method or the http version is not implemented.</p>\n" +
				  "<hr>\n" +
				  "</body></html>\n");		
		String response = new String("HTTP/1.1 501 Not implemented\r\n"+
				  "Content-Length: " + content.getBytes().length +"\r\n"+
				  "Connection: close\r\n" +
				  "Content-Type: text/html; charset=iso-8859-1\r\n\r\n"+
				  content);
		return new HTTPResponse(response.getBytes());
	}
	
	/**
	 * Sole constructor.
	 * 
	 * @param content the content of the response.
	 * @throws ProxyException if the content of the response is not well-formed.
	 */
	public HTTPResponse(byte[] content) throws ProxyException {
		BufferedReader bin = new BufferedReader(new InputStreamReader(
				new ByteArrayInputStream(content)));

		// Parse the response from the result
		try {
			String line = bin.readLine();
			if ((line != null) && (line.length() != 0)) {
				// First line
				// Parse the command
				int index = line.indexOf(" ");
				this.httpVersion = line.substring(0, index).trim();

				// Parse the URL
				String remainder = line.substring(index).trim();
				index = remainder.indexOf(" ");
				this.status = remainder.substring(0, index).trim();

				// Parse the Version
				this.reason = remainder.substring(index).trim();

				// Parse the headers
				StringBuffer sBuf = new StringBuffer();
				while ((line = bin.readLine()) != null) {
					if (line.length() == 0)
						break;

					sBuf.append(line + "\r\n");
				}
				this.headers = sBuf.toString();
			}
		} catch (Exception ioe) {
			throw new ProxyException(ioe);
		} 

		this.fullContent = content;
	}

	/**
	 * Return the status code attached to the response.
	 * 
	 * @return the status code attached to the response.
	 */
	public String getStatus() {
		return this.status;
	}

	/**
	 * Return the reason explaining the status code attached to the response.
	 * 
	 * @return the reason explaining the status code attached to the response.
	 */
	public String getReason() {
		return this.reason;
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
	 * Return a byte array (which can be sent through the network) corresponding to the HTTP response.
	 * 
	 * @return the byte array corresponding to the HTTP response.
	 */
	public byte[] getBytes() throws IOException {
		return this.fullContent;
	}

	/**
	 * Return the string corresponding to the request without its content
	 *  (i.e., return only the status line and the headers). 
	 * 
	 * @return the string corresponding to the request without its content.
	 */	
	public String StatusLineAndHeaders() {
		StringBuffer result = new StringBuffer();

		result.append(httpVersion + " " + status + " " + reason + "\r\n");
		result.append(headers);
		result.append("\r\n");
		return result.toString();
	}
		
	public String toString() {
		return new String(fullContent);
	}
}
