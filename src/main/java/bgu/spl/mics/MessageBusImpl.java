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

	private Map<MicroService, Queue<Message,Future>>> msMap = new ConcurrentHashMap<>();
	private Map<Class<? extends Event>, LinkedList<Pair>> eventsMap = new ConcurrentHashMap<>();
	private Map<Class<? extends Event>, ListIterator<Pair>> robinPointer = new ConcurrentHashMap<>();
	private Map<Class<? extends Broadcast>, LinkedList<Pair>> broadcastsMap = new ConcurrentHashMap<>();
	private Object lock1 = new Object();
	private Object lock2 = new Object();

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
			robinPointer.put(type,eventsMap.get(type).listIterator());
		}
		else{
			if(!eventsMap.get(type).contains(p))
				eventsMap.get(type).addLast(p);
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		Pair p= msMap.get(m);
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
		synchronized (lock1){
			LinkedList<Pair> list = broadcastsMap.get(b);
			for(Pair<MicroService, Queue<Pair<Message,Future>>> pair: list) {
				pair.getValue().add(new Pair(b, null));
			}
		}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		synchronized (lock2){
			Future<T> output=null;
			if(eventsMap.get(e.getClass()) != null){
				output = new Future<T>();
				Pair<MicroService, Queue<Pair<Message,Future>>> p =nextInRobin(e.getClass());
				p.getValue().add(new Pair<>(e,output));
			}
			return output;
		}
	}

	@Override
	public void register(MicroService m) {
		if (msMap.get(m) == null){
			msMap.put(m, new Pair<>(m, new LinkedList<>()));
		}
	}

	@Override
	public void unregister(MicroService m) {
		synchronized ((lock1)){
			synchronized (lock2){
				Pair<MicroService, Queue<Pair<Message,Future>>> pair=msMap.get(m);
				if(pair!= null) {
					Queue<Pair<Message, Future>> q = pair.getValue();
					Collection<LinkedList<Pair>> lists = eventsMap.values();    //Removing the ms pair from event list
					Set<Class<? extends Event>> keys= eventsMap.keySet();
					Iterator<Class<? extends Event>> it=keys.iterator();
					Class<? extends Event> key;
					Pair p2;
					for (LinkedList<Pair> list : lists) {
						key= it.next();
						if(robinPointer.get(key).hasPrevious()) {
							robinPointer.get(key).previous();
							p2=robinPointer.get(key).next();
						}
						else
							p2=eventsMap.get(key).getFirst();
						if(list.remove(pair)){

						}

					}
					lists = broadcastsMap.values();                                //Removing the ms pair from broadcast list
					for (LinkedList<Pair> list : lists)
						list.remove(pair);

				}
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void complete(Event<T> e, T result){

	}

	private Pair<MicroService, Queue<Pair<Message,Future>>> nextInRobin(Class<? extends Event> type){
		if(!robinPointer.get(type).hasNext())
			robinPointer.replace(type,eventsMap.get(type).listIterator());
		return robinPointer.get(type).next();
	}
	

}
