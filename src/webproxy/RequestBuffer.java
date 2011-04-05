package webproxy;


/**
 * Synchronized request buffer 
 * 
 * @author Amos Wenger
 */
public interface RequestBuffer {

	/**
	 * Add an HTTP job that needs to be processed to the buffer.
	 * @param job
	 */
	public void queue(HTTPJob job);
	
	/**
	 * @return an HTTPJob that needs to be processed, or null if there are no pending jobs
	 */
	public HTTPJob pop();
	
}
