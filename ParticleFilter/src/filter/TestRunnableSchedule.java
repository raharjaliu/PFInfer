package filter;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class TestRunnableSchedule implements Runnable {

	private int id;
	private double time;
	private ReentrantLock lock;

	public TestRunnableSchedule(int _id, double _time, ReentrantLock _lock) {

		this.id = _id;
		this.time = _time;
		this.lock = _lock;
	}

	@Override
	public void run() {
		
		this.lock.lock();
		
		try {
			
			System.out.println("Thread (" + this.id + ") -- running. Initial time: " + this.time);
			
			while (this.time > 0) {
				
				this.time -= Math.random();
				
				System.out.println("Thread (" + this.id + ") -- time remaining :" + this.time);
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			
			System.out.println("Thread (" + this.id + "): done.");
			
		} finally {
			this.lock.unlock();
		}
		
		
	}

	public static void main(String[] args) {

		ArrayList<TestRunnableSchedule> list = new ArrayList<TestRunnableSchedule>();

		ReentrantLock lock = new ReentrantLock();

		for (int i = 0; i < 1000; i++) {
			TestRunnableSchedule thisShit = new TestRunnableSchedule(i,
					Math.random() * 1000, lock);
			list.add(thisShit);
		}

		for (TestRunnableSchedule thisShit : list) {
			new Thread(thisShit).start();
		}
	}

}
