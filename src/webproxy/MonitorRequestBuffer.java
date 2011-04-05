package webproxy;

import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of RequestBuffer using monitors
 * 
 * @author Amos Wenger
 */
public class MonitorRequestBuffer implements RequestBuffer {

	List<HTTPJob> httpJobs = new LinkedList<HTTPJob>();
	
	@Override
	public void queue(HTTPJob job) {
		synchronized(httpJobs) {
			httpJobs.add(job);
		}
	}

	@Override
	public HTTPJob pop() {
		HTTPJob job = null;
		synchronized(httpJobs) {
			if(!httpJobs.isEmpty()) {
				job = httpJobs.remove(0);
			}
		}
		return job;
	}
	
}
