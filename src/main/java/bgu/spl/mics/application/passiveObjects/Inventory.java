package bgu.spl.mics.application.passiveObjects;


import org.omg.PortableInterceptor.SUCCESSFUL;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive data-object representing the store inventory.
 * It holds a collection of {@link BookInventoryInfo} for all the
 * books in the store.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class Inventory {
	private static Inventory instance=null;
	private Map<String,BookInventoryInfo> stoke=new ConcurrentHashMap<>();

	/**
     * Retrieves the single instance of this class.
     */
	public static Inventory getInstance() {
		if(instance==null){
			synchronized (Inventory.class){
				if(instance==null){
					Inventory tmp=new Inventory();
					instance=tmp;
				}
			}
		}
		return instance;
	}
	
	/**
     * Initializes the store inventory. This method adds all the items given to the store
     * inventory.
     * <p>
     * @param inventory 	Data structure containing all data necessary for initialization
     * 						of the inventory.
     */
	public void load (BookInventoryInfo[ ] inventory ) {
			for (BookInventoryInfo b : inventory)
				stoke.putIfAbsent(b.getBookTitle(), b);

	}
	
	/**
     * Attempts to take one book from the store.
     * <p>
     * @param book 		Name of the book to take from the store
     * @return 	an {@link Enum} with options NOT_IN_STOCK and SUCCESSFULLY_TAKEN.
     * 			The first should not change the state of the inventory while the 
     * 			second should reduce by one the number of books of the desired type.
     */
	public OrderResult take (String book) {
		BookInventoryInfo b=stoke.get(book);
		if (b != null){
			synchronized (b) {
				if (b.takeBook())
					return OrderResult.SUCCESSFULLY_TAKEN;
			}

		}
		return OrderResult.NOT_IN_STOCK;
	}
	
	
	
	/**
     * Checks if a certain book is available in the inventory.
     * <p>
     * @param book 		Name of the book.
     * @return the price of the book if it is available, -1 otherwise.
     */
	public int checkAvailabiltyAndGetPrice(String book) {
		BookInventoryInfo b=stoke.get(book);
		if(b!=null){
			if(b.getAmountInInventory()>0)
				return b.getPrice();
		}
		return -1;
	}
	
	/**
     * 
     * <p>
     * Prints to a file name @filename a serialized object HashMap<String,Integer> which is a Map of all the books in the inventory. The keys of the Map (type {@link String})
     * should be the titles of the books while the values (type {@link Integer}) should be
     * their respective available amount in the inventory. 
     * This method is called by the main method in order to generate the output.
     */
	public void printInventoryToFile(String filename){
		Map<String,Integer> output=new HashMap<>();
		Set<String> keys=stoke.keySet();
		for(String book:keys){
			output.put(book,stoke.get(book).getAmountInInventory());
		}
		try
		{
			FileOutputStream fos =new FileOutputStream(filename);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(output);
			oos.close();
			fos.close();
		}catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
}
