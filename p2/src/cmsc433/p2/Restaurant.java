package cmsc433.p2;

import java.util.HashSet;
import java.util.LinkedList;

public class Restaurant {
	private int capacity;
	private Integer occupied;
	private LinkedList<Customer> orders;
	public Machine[] machines;
	public HashSet<Integer> finishedOrders;
	
	public Restaurant(int newcap) {
		capacity = newcap;
		occupied = 0;
		orders = new LinkedList<Customer>();
		finishedOrders = new HashSet<Integer>();
	}
	
	public synchronized void initMachines(Machine[] newmachines){
		if(machines == null) machines = newmachines;
	}
	
	public void addOrder(Customer c) {
		synchronized(orders) {
			orders.offer(c);	
		}
		
	}
	
	public void submitFinishedOrder(Integer ordernum) throws InterruptedException{
			finishedOrders.add(ordernum);
		
		
	}
	
	public boolean pickUpFinishedOrder(Integer ordernum) {
		return finishedOrders.contains(ordernum);
	}
	
	
	public Customer getOrder() throws InterruptedException{
		while(orders.size()  == 0 && !Thread.currentThread().isInterrupted()) {
		
		}
		
		if(Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}
		synchronized (this.orders) {
			return orders.poll();
		}
		
	}
	
	public boolean takeSeat() {
		synchronized(occupied) {
		if(occupied == capacity) return false;
		else {
			occupied++;
			
			return true;
		}
		}
	}
	
	public boolean leave() {
		synchronized(occupied) {
		if(occupied == 0) return false;
		else {
			occupied--;
			
			return true;
		}
		}
	}
	
}
