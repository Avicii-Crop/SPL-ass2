package bgu.spl.mics;

import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	private static MessageBusImpl instance = null;

	private Map<MicroService, Pair<MicroService, Queue<Pair<Message,Future>>>> msMap = new ConcurrentHashMap<>();
	private Map<Class<? extends Event>, LinkedList<Pair>> eventsMap = new ConcurrentHashMap<>();
	private Map<Class<? extends Event>, Iterator<Pair>> robinPointer = new ConcurrentHashMap<>();
	private Map<Class<? extends Broadcast>, LinkedList<Pair>> broadcastsMap = new ConcurrentHashMap<>();

	private MessageBusImpl() { }

	public static MessageBusImpl getInstance() {
		if(instance == null) {
			instance = new MessageBusImpl();
		}
		return instance;
	}
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		Pair p= msMap.get(m);
		if (eventsMap.get(type) == null){
			eventsMap.put(type,new LinkedList<>());
			eventsMap.get(type).addLast(p);
			robinPointer.put(type,eventsMap.get(type).iterator());
		}
		else{
			eventsMap.get(type).addLast(p);
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if (broadcastsMap.get(type) == null){
			broadcastsMap.put(type,new LinkedList<>());
			broadcastsMap.get(type).add(p);
		}
		else {
			if(!broadcastsMap.get(type).contains(p))
				broadcastsMap.get(type).add(p);
		}

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		LinkedList<MicroService> relevantMsList = broadcastsMap.get(b);


	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void register(MicroService m) {
		if (msMap.get(m) == null){
			msMap.put(m, new Pair<>(m, new LinkedList<>()));
		}
	}

	@Override
	public void unregister(MicroService m) {

		Pair<MicroService, Queue<Pair<Message,Future>>> pair=msMap.get(m);
		if(pair!= null) {
			Queue<Pair<Message,Future>> q = pair.getValue();
			Collection<LinkedList<Pair>> lists = eventsMap.values();	//Removing the ms pair from event list
			for (LinkedList<Pair> list : lists)
				list.remove(pair);
			lists = broadcastsMap.values();								//Removing the ms pair from broadcast list
			for (LinkedList<Pair> list : lists)
				list.remove(pair);


		}


	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}
	<T> void complete(Event<T> e, T result)
	{}

	private void nextInRobin(Class<? extends Event> type){

	}
	

}
