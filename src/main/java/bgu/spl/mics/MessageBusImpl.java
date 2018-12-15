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
	Object iteratorLock = new Object();
	Object robinLock=new Object();
	Object instanceLock=new Object();

	private MessageBusImpl() { }

	public static MessageBusImpl getInstance() {
		if(instance == null) {
			synchronized (MessageBusImpl.class){
				if(instance==null){
					MessageBusImpl tmp= new MessageBusImpl();
					instance = tmp;
				}
			}
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
		synchronized (iteratorLock) {
			LinkedBlockingQueue<Message> q = msMap.get(m);
			if (broadcastsMap.get(type) == null) {
				broadcastsMap.put(type, new Vector<LinkedBlockingQueue<Message>>());
				broadcastsMap.get(type).add(q);
			} else {
				if (!broadcastsMap.get(type).contains(q))
					broadcastsMap.get(type).add(q);
			}
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
			synchronized (iteratorLock) {

//			for(int i=0;i<v.size();i++)
					while (broadcastsMap.get(b.getClass())==null){
						try {
							iteratorLock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				}
				Vector<LinkedBlockingQueue<Message>> v = broadcastsMap.get(b.getClass());
				for (LinkedBlockingQueue<Message> q : v) {
					try {
						q.put(b);
//					v.get(i).put(b);
					} catch (NullPointerException | InterruptedException ex) {
					}

				}
			}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
			Future<T> output=null;
			if(eventsMap.get(e.getClass()) != null){
				output = new Future<T>();
				synchronized (robinLock) {
					LinkedBlockingQueue<Message> q = nextInRobin(e.getClass());

					try {
						q.put(e);
					} catch (NullPointerException | InterruptedException ex) {
					}
				}
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
				LinkedBlockingQueue<Message> q=msMap.get(m);
				if(q!= null) {
					removeFromEventsMap(q);
					removeFromBroadcastMap(q);
					while (!q.isEmpty()){
						Message msg=q.poll();
						if(Event.class.isAssignableFrom(msg.getClass())){
							complete((Event)msg,null);
						}
					}

				}

	}



	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		return msMap.get(m).take();
	}

	@Override
	public <T> void complete(Event<T> e, T result){
		synchronized (this){
		while (futureList.get(e)==null){
			try {
				wait();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}}
		futureList.get(e).resolve(result);
	}

	private LinkedBlockingQueue<Message> nextInRobin(Class<? extends Event> type){
		Integer p=robinPointer.get(type);
		if(eventsMap.get(type).size() >= (p+1))
			p=0;
		else
			p++;
		return eventsMap.get(type).elementAt(p);
	}

	private void removeFromEventsMap(LinkedBlockingQueue<Message> q){
		Set<Class<? extends Event>> keys= eventsMap.keySet();
		Iterator<Class<? extends Event>> it=keys.iterator();
		Class<? extends Event> key;
		int index;
		while(it.hasNext()){
			key=it.next();
			index=eventsMap.get(key).indexOf(q);
			synchronized (robinLock) {
				if (index != -1) {
					eventsMap.get(key).remove(index);
					if (robinPointer.get(key) == index) {
						if(index==0)
							robinPointer.replace(key, eventsMap.get(key).size()-1);
						else
							robinPointer.replace(key, (index - 1));
					}
				}
			}
		}
	}

	private void removeFromBroadcastMap(LinkedBlockingQueue<Message> q) {
		synchronized (iteratorLock) {
			Set<Class<? extends Broadcast>> keys = broadcastsMap.keySet();
			Iterator<Class<? extends Broadcast>> it = keys.iterator();
			Class<? extends Broadcast> key;
			int index;
			while (it.hasNext()) {
				key = it.next();
				broadcastsMap.get(key).remove(q);
			}
		}
	}
}
