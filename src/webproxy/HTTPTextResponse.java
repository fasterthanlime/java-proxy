package webproxy;

import webproxy.exceptions.ProxyException;

public class HTTPTextResponse extends HTTPResponse {

	public HTTPTextResponse(String content) throws ProxyException {
		super((
				"HTTP/1.1 200 OK\r\n" +
				"Content-Type: text/plain\r\n" +
				"\r\n" + content + "\r\n"
				).getBytes());
	}
	
}
