package bgu.spl.mics;

import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	private static MessageBusImpl instance = null;

	private Map<MicroService, LinkedBlockingQueue<Message>> msMap = new ConcurrentHashMap<>();
	private Map<Class<? extends Event>, Vector<LinkedBlockingQueue<Message>>> eventsMap = new ConcurrentHashMap<>();
	private Map<Class<? extends Event>, Integer> robinPointer = new ConcurrentHashMap<>();
	private Map<Class<? extends Broadcast>, Vector<LinkedBlockingQueue<Message>>> broadcastsMap = new ConcurrentHashMap<>();
	private Map<Event, Future> futureList = new ConcurrentHashMap<>();


	private MessageBusImpl() { }

	public static MessageBusImpl getInstance() {
		if(instance == null) {
			instance = new MessageBusImpl();
		}
		return instance;
	}
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		LinkedBlockingQueue<Message> q= msMap.get(m);
		if (eventsMap.get(type) == null){
			eventsMap.put(type,new Vector<LinkedBlockingQueue<Message>>());
			eventsMap.get(type).add(q);
			robinPointer.put(type,new Integer(0));
		}
		else{
			if(!eventsMap.get(type).contains(q))
				eventsMap.get(type).add(q);
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		LinkedBlockingQueue<Message> q= msMap.get(m);
		if (broadcastsMap.get(type) == null){
			broadcastsMap.put(type,new Vector<LinkedBlockingQueue<Message>>());
			broadcastsMap.get(type).add(q);
		}
		else {
			if(!broadcastsMap.get(type).contains(q))
				broadcastsMap.get(type).add(q);
		}

	}

	@Override
	public void sendBroadcast(Broadcast b) {

			Vector<LinkedBlockingQueue<Message>> v = broadcastsMap.get(b.getClass());
//			for(int i=0;i<v.size();i++)
			for(LinkedBlockingQueue<Message> q: v) {
				try {
					q.put(b);
//					v.get(i).put(b);
				}
				catch(NullPointerException |InterruptedException ex){}

			}

	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
			Future<T> output=null;
			if(eventsMap.get(e.getClass()) != null){
				output = new Future<T>();
				LinkedBlockingQueue<Message> q =nextInRobin(e.getClass());
				try {
					q.put(e);
				}
				catch(NullPointerException |InterruptedException ex){}

				futureList.put(e,output);

			}
			return output;
	}

	@Override
	public void register(MicroService m) {
		if (msMap.get(m) == null){
			msMap.put(m, new LinkedBlockingQueue<Message>() );
		}
	}

	@Override
	public void unregister(MicroService m) {
				Queue<Message> q=msMap.get(m);
				if(q!= null) {
					//Collection<Vector<LinkedBlockingQueue<Message>>> lists = eventsMap.values();    //Removing the ms pair from event list
					Set<Class<? extends Event>> keys= eventsMap.keySet();
					Iterator<Class<? extends Event>> it=keys.iterator();
					Class<? extends Event> key;
					int index;
					while(it.hasNext()){
						key=it.next();
						 index=eventsMap.get(key).indexOf(q);
						if(index!=-1){
							eventsMap.get(key).remove(index);
							if(robinPointer.get(key)==index){
								robinPointer.replace(key,(index-1));
							}
						}
					}

//					Queue<Message> tmpQ;
//					Class<? extends Event> tmpEvent;
//
//					for (Message msg : q) {
//						if(msg.getClass().isA(<? extends Event>.class)){
//							tmpQ =nextInRobin(msg.getClass());
//						}
//
//						key= it.next();
//						if(robinPointer.get(key).hasPrevious()) {
//							robinPointer.get(key).previous();
//							p2=robinPointer.).next();
//						}
//						else
//							p2=eventsMap.get(key).getFirst();
//						if(list.remove(pair)){
//
//						}
//
//					}
//					lists = broadcastsMap.values();                                //Removing the ms pair from broadcast list
//					for (LinkedList<Pair> list : lists)
//						list.remove(pair);

				}

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		return msMap.get(m).take();
	}

	@Override
	public <T> void complete(Event<T> e, T result){
		futureList.get(e).resolve(result);
		futureList.get(e).isDone();
	}

	private LinkedBlockingQueue<Message> nextInRobin(Class<? extends Event> type){
		Integer p=robinPointer.get(type);
		if(eventsMap.get(type).size() >= (p+1))
			p=0;
		else
			p++;
		return eventsMap.get(type).elementAt(p);
	}
	

}
