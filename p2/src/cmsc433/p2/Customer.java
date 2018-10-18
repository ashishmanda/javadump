package cmsc433.p2;

import java.util.List;

/**
 * Customers are simulation actors that have two fields: a name, and a list
 * of Food items that constitute the Customer's order.  When running, an
 * customer attempts to enter the Ratsie's (only successful if the
 * Ratsie's has a free table), place its order, and then leave the
 * Ratsie's when the order is complete.
 */
public class Customer implements Runnable {
	//JUST ONE SET OF IDEAS ON HOW TO SET THINGS UP...
	private final String name;
	public final List<Food> order;
	public final Integer orderNum;
	private Restaurant r;
	
	
	private static int runningCounter = 0;

	/**
	 * You can feel free modify this constructor.  It must take at
	 * least the name and order but may take other parameters if you
	 * would find adding them useful.
	 */
	public Customer(String name, List<Food> order, Restaurant newr) {
		this.name = name;
		this.order = order;
		this.orderNum = ++runningCounter;
		r =newr;
		
	}

	public String toString() {
		return name;
	}

	/** 
	 * This method defines what an Customer does: The customer attempts to
	 * enter the Ratsie's (only successful when the Ratsie's has a
	 * free table), place its order, and then leave the Ratsie's
	 * when the order is complete.
	 */
	public void run() {
		Simulation.logEvent(SimulationEvent.customerStarting(this));
		
			while(!r.takeSeat()) {
				
			}
		
			Simulation.logEvent(SimulationEvent.customerEnteredRatsies(this));
		
			Simulation.logEvent(SimulationEvent.customerPlacedOrder(this,this.order,this.orderNum));
			//wait on the chef
			r.addOrder(this);
		
			while(!r.pickUpFinishedOrder(orderNum)) {
				
			}
			
			Simulation.logEvent(SimulationEvent.customerReceivedOrder(this, this.order, this.orderNum));
			Simulation.logEvent(SimulationEvent.customerLeavingRatsies(this));
			r.leave();
		
		
	}

	public String getName() {
		return this.name;
	}
}
