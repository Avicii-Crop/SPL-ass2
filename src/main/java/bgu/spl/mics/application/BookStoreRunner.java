package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.BookInventoryInfo;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import com.google.gson.Gson;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;


/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner {
    public static void main(String[] args) {
        InputJson input =getJson(args[0]);
        try{
            if(input==null) throw new NullPointerException();
        }catch(NullPointerException e){
            e.printStackTrace();
            int x =5;
        }

    }

    private static InputJson getJson(String arg0){
        Gson gson=new Gson();
        InputJson output=null;
        try {
            BufferedReader json=new BufferedReader(new FileReader(arg0));
            output=gson.fromJson(json,InputJson.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return output;
    }

    /**
     * Ths class represent  the JSON object which we are getting in the input to initialize the store.
     */
    public class InputJson{
        private InitBook[] initialInventory;
        private InitVehicle[] initialResources;
        private InitService[] services;

        public class InitBook{
            private String bookTitle;
            private int amount;
            private int price;

            /**
             * Initialize BookInventoryInfo object
             * @return BookInventoryInfo object
             */
            public BookInventoryInfo getBookInventoryInfo(){
                return new BookInventoryInfo(bookTitle,amount,price);
            }

            public String getBookTitle() {
                return bookTitle;
            }

            public int getAmount() {
                return amount;
            }

            public int getPrice() {
                return price;
            }
        }

        public class InitVehicle{
            private DeliveryVehicle[] vehicles;

            public DeliveryVehicle[] getVehicles() {
                return vehicles;
            }
        }

        public class  InitService{
            private InitTime time;
            private int selling;
            private int inventoryService;
            private int resourcesService;
            private InitCustomer[] customers;

            public InitCustomer[] getCustomers() {
                return customers;
            }

            public InitTime getTime() {
                return time;
            }

            public int getInventoryService() {
                return inventoryService;
            }

            public int getResourcesService() {
                return resourcesService;
            }

            public int getSelling() {
                return selling;
            }


        }
        public class InitCustomer{
            private int id;
            private String name;
            private String address;
            private int distance;
            private CreditCard creditCard;
            private OrderSchedule[] orderSchedule;

            /**
             * Initialize Customer object.
             * @return Customer object
             */
            public Customer getCustomer(){
                return new Customer(name,id, address,distance,creditCard.getNumber(),creditCard.getAmount());
            }

            public int getDistance() {
                return distance;
            }

            public int getId() {
                return id;
            }

            public CreditCard getCreditCard() {
                return creditCard;
            }

            public OrderSchedule[] getOrderSchedule() {
                return orderSchedule;
            }

            public String getAddress() {
                return address;
            }

            public String getName() {
                return name;
            }



        }

        public class CreditCard{
            private int number;
            private int amount;

            public CreditCard(int number,int amount){
                this.number=number;
                this.amount=amount;
            }

            public int getAmount() {
                return amount;
            }

            public int getNumber() {
                return number;
            }
        }

        public class OrderSchedule implements Comparable<OrderSchedule>{
            private String bookTitle;
            private int tick;

            public OrderSchedule(String bookTitle,int tick){
                this.bookTitle=bookTitle;
                this.tick=tick;
            }

            public int getTick() {
                return tick;
            }

            public String getBookTitle() {
                return bookTitle;
            }

            public void setBookTitle(String bookTitle) {
                this.bookTitle = bookTitle;
            }

            @Override
            public int compareTo(OrderSchedule o) {
                return this.tick-o.getTick();
            }
        }

        public class InitTime{
            private int speed;
            private int duration;

            public InitTime(int speed,int duration){
                this.speed=speed;
                this.duration=duration;
            }

            public int getSpeed(){return speed;}

            public int getDuration(){return duration;}


        }
    }



}
