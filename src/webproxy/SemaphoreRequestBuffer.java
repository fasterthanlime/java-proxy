package webproxy;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Implementation of RequestBuffer using semaphores
 * 
 * @author Amos Wenger
 */
public class SemaphoreRequestBuffer implements RequestBuffer {

	List<HTTPJob> httpJobs = new LinkedList<HTTPJob>();
	
	Semaphore semaphore = new Semaphore(1);
	
	@Override
	public void queue(HTTPJob job) {
		try {
			semaphore.acquire();
		} catch (InterruptedException e) { }
		httpJobs.add(job);
		semaphore.release();
	}

	@Override
	public HTTPJob pop() {
		try {
			semaphore.acquire();
		} catch (InterruptedException e) { }
		HTTPJob job = httpJobs.isEmpty() ? null : httpJobs.remove(0);
		semaphore.release();
		return job;
	}

}
