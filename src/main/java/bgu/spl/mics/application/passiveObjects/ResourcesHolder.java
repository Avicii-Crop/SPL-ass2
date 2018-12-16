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
	private Vector<DeliveryVehicle> vehicles;
	private boolean[] available;
	private final Object acquireLock=new Object();
	private LinkedBlockingQueue<Future<DeliveryVehicle>> requestsQueue=new LinkedBlockingQueue<>();
	
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
		acquire();
		return output;
	}

	
	/**
     * Releases a specified vehicle, opening it again for the possibility of
     * acquisition.
     * <p>
     * @param vehicle	{@link DeliveryVehicle} to be released.
     */
	public void releaseVehicle(DeliveryVehicle vehicle) {
		synchronized (acquireLock){
			int index=vehicles.indexOf(vehicle);
			available[index]=true;
			acquireLock.notifyAll();
		}

	}

	 private boolean acquire() {
		boolean found = false;
		synchronized (acquireLock) {
			for (int i = 0; i < available.length && !found; i++) {
				if (available[i]) {
					available[i] = false;
					found = true;
					try {
						requestsQueue.take().resolve(vehicles.elementAt(i));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		 return found;
	 }

	/**
     * Receives a collection of vehicles and stores them.
     * <p>
     * @param vehicles	Array of {@link DeliveryVehicle} instances to store.
     */
	public void load(DeliveryVehicle[] vehicles) {
		this.vehicles= new Vector<DeliveryVehicle>();
		available=new boolean[vehicles.length];
		for(int i=0;i<vehicles.length;i++){
			this.vehicles.add(new DeliveryVehicle(vehicles[i].getLicense(),vehicles[i].getSpeed()));
			available[i]=true;
		}
	}

}
