package bgu.spl.mics.application.services;


import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BookOrderEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.*;

//Avishai

/**
 * Selling service in charge of taking orders from customers.
 * Holds a reference to the {@link MoneyRegister} singleton of the store.
 * Handles {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class SellingService extends MicroService{
	protected int currentTick = 0;

	public SellingService(String name) {
		super(name);
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, br -> currentTick=br.getTick());
		subscribeEvent(BookOrderEvent.class, ev -> {

		});
		
	}

}
