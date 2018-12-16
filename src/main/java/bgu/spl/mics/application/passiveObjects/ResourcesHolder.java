package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.Future;

import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * Passive object representing the resource manager.
 * You must not alter any of the given public methods of this class.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class ResourcesHolder {
	private static ResourcesHolder resourcesHolder=null;
	private Semaphore sem;
	private LinkedBlockingQueue<Future<DeliveryVehicle>> requestsQueue=new LinkedBlockingQueue<>();
	private LinkedBlockingQueue<DeliveryVehicle> vehiclesQueue=new LinkedBlockingQueue<>();
	
	/**
     * Retrieves the single instance of this class.
     */
	public static ResourcesHolder getInstance() {
		if(resourcesHolder==null){
			synchronized (ResourcesHolder.class){
				if(resourcesHolder==null){
					ResourcesHolder tmp= new ResourcesHolder();
					resourcesHolder=tmp;
				}

			}
		}
		return resourcesHolder;
	}
	
	/**
     * Tries to acquire a vehicle and gives a future object which will
     * resolve to a vehicle.
     * <p>
     * @return 	{@link Future<DeliveryVehicle>} object which will resolve to a 
     * 			{@link DeliveryVehicle} when completed.   
     */
	public Future<DeliveryVehicle> acquireVehicle() {
		Future<DeliveryVehicle> output=new Future<>();
		requestsQueue.offer(output);
		if (sem.tryAcquire())
			getVehicle();
		return output;
	}

	
	/**
     * Releases a specified vehicle, opening it again for the possibility of
     * acquisition.
     * <p>
     * @param vehicle	{@link DeliveryVehicle} to be released.
     */
	public void releaseVehicle(DeliveryVehicle vehicle) {
		vehiclesQueue.offer(vehicle);
		sem.release();
		try {
			sem.acquire();
			getVehicle();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is called only after a semaphore's permit
	 * is acquired. the only scenario for this method to fail is if there isn't
	 * any request waiting, in this case the permit will be release.
	 * @return true if managed to acquire vehicle and false otherwise.
	 */
	 private boolean getVehicle() {
		boolean acquired = false;
		Future<DeliveryVehicle> f=requestsQueue.poll();
		if (f!=null) {
			try {
				f.resolve(vehiclesQueue.take());
				acquired = true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(!acquired)
			sem.release();
		return acquired;
	 }

	/**
     * Receives a collection of vehicles and stores them.
     * <p>
     * @param vehicles	Array of {@link DeliveryVehicle} instances to store.
     */
	public void load(DeliveryVehicle[] vehicles) {

		for(int i=0;i<vehicles.length;i++){
			vehiclesQueue.offer(vehicles[i]);
		}
		sem=new Semaphore(vehicles.length,true);
	}

}
