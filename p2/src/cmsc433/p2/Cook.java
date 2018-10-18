package cmsc433.p2;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;


/**
 * Cooks are simulation actors that have at least one field, a name.
 * When running, a cook attempts to retrieve outstanding orders placed
 * by Eaters and process them.
 */
public class Cook implements Runnable {
	private final String name;
	private HashMap<Thread,Food> slots;
	private HashSet<Thread> cookedFoods;
	private Restaurant r;
	
	/**
	 * You can feel free to modify this constructor.  It must
	 * take at least the name, but may take other parameters
	 * if you would find adding them useful. 
	 *
	 * @param: the name of the cook
	 */
	public Cook(String name, Restaurant newr) {
		this.name = name;
		r = newr;
		
	}
	
	public String toString() {
		return name;
	}
	
	private int findMachineID(String s) {
		int machineID = 0;
		switch(s) {
		case "wings":
			machineID= 0;
			break;
		case "pizza":
			machineID= 1;
			break;
		case "sub":
			machineID= 2;
			break;
		case "soda":
			machineID= 3;
			break;
		}
		return machineID;
	}
	
	private boolean addNextFood(Food f, int ordernum) throws InterruptedException {
		int machineID = findMachineID(f.name);
		
		Machine m = r.machines[machineID];
		synchronized(m) {
			if(m.full()) return false;
			else {
				Simulation.logEvent(SimulationEvent.cookStartedFood(this, f, ordernum));
				slots.put(m.makeFood(),f);
				return true;
			}
		}
			
		
		
	}
	
	private void removeDoneFoods(int order) throws InterruptedException {
		for(Thread t : slots.keySet()) {
			if(!cookedFoods.contains(t) && !t.isAlive()) {
				cookedFoods.add(t);
				Food f = slots.get(t);
				int ID = this.findMachineID(f.name);
				Machine m = r.machines[ID];
				synchronized(m) {
					m.retrieveFood();
					Simulation.logEvent(SimulationEvent.cookFinishedFood(this, f, order));
				}
			}
		}
		
	}

	/**
	 * This method executes as follows.  The cook tries to retrieve
	 * orders placed by Customers.  For each order, a List<Food>, the
	 * cook submits each Food item in the List to an appropriate
	 * Machine, by calling makeFood().  Once all machines have
	 * produced the desired Food, the order is complete, and the Customer
	 * is notified.  The cook can then go to process the next order.
	 * If during its execution the cook is interrupted (i.e., some
	 * other thread calls the interrupt() method on it, which could
	 * raise InterruptedException if the cook is blocking), then it
	 * terminates.
	 */
	public void run() {

		Simulation.logEvent(SimulationEvent.cookStarting(this));
		try {
			//Thread.sleep(10);
			while(!Thread.currentThread().isInterrupted()) {
				Customer customer = r.getOrder();
				
				while(customer == null && !Thread.currentThread().isInterrupted()) {
					customer = r.getOrder();
				}
				Simulation.logEvent(SimulationEvent.cookReceivedOrder(this, customer.order, customer.orderNum));
				
			
				slots = new HashMap<Thread,Food>();
				cookedFoods = new HashSet<Thread>();
				
				ArrayList<Food> temp = new ArrayList<Food>(customer.order);
				
				
				
				
				while(!temp.isEmpty()) {
					
					for(int i = 0; i < temp.size();) {
						Food f = temp.get(i);
						if(addNextFood(f, customer.orderNum)) {
							temp.remove(f);
						} else {
							i++;
						}
					removeDoneFoods(customer.orderNum);
					}
					
					
						
				}
				
				for(Thread t : slots.keySet()) {
					t.join();
				}
				
				removeDoneFoods(customer.orderNum);
				
				Simulation.logEvent(SimulationEvent.cookCompletedOrder(this, customer.orderNum));
				r.submitFinishedOrder(customer.orderNum);
			}
		}
		catch(InterruptedException e) {
			// This code assumes the provided code in the Simulation class
			// that interrupts each cook thread when all customers are done.
			// You might need to change this if you change how things are
			// done in the Simulation class.
			//e.printStackTrace();
			//e.printStackTrace();
			Simulation.logEvent(SimulationEvent.cookEnding(this));
			Thread.currentThread().interrupt();
			
		}
	}

	public String getName() {
		
		return this.name;
	}
}
