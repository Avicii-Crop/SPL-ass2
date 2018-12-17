package bgu.spl.mics.application.services;
//Ofek
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;


/**
 * TimeSethervice is  global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link Tick Broadcast}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService {
	
	private int speed;
	private int duration;

	public TimeService(String name, int speed, int duration) {
		super(name);
		this.speed = speed;
		this.duration = duration;
	}

	@Override
	protected void initialize() {
		TickBroadcast tickBroadcast = new TickBroadcast(0);
		for (int tick = 1; tick <= duration; tick++) {
			tickBroadcast.increasTick();
			sendBroadcast((Broadcast) tickBroadcast);
		try {
			Thread.sleep(speed);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		}
	terminate();
	}
}
