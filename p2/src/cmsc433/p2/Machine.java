package cmsc433.p2;

import java.util.LinkedList;

/**
 * A Machine is used to make a particular Food.  Each Machine makes
 * just one kind of Food.  Each machine has a capacity: it can make
 * that many food items in parallel; if the machine is asked to
 * produce a food item beyond its capacity, the requester blocks.
 * Each food item takes at least item.cookTimeS seconds to produce.
 */

public class Machine {
	
	// Types of machines used in Ratsie's.  Recall that enum types are
	// effectively "static" and "final", so each instance of Machine
	// will use the same MachineType.
	
	public enum MachineType { fountain, fryer, grillPress, oven };
	
	// Converts Machine instances into strings based on MachineType.
	
	public String toString() {
		switch (machineType) {
		case fountain: 		return "Fountain";
		case fryer:			return "Fryer";
		case grillPress:	return "Grill Press";
		case oven:			return "Oven";
		default:			return "INVALID MACHINE";
		}
	}
	
	public final MachineType machineType;
	public final Food machineFoodType;
	public final int capacity;
	private int load = 0;
	private LinkedList<Thread> slots;
	//YOUR CODE GOES HERE...

	
	/**
	 * The constructor takes at least the type of the machine,
	 * the Food item it makes, and its capacity.  You may extend
	 * it with other arguments, if you wish.  Notice that the
	 * constructor currently does nothing with the capacity; you
	 * must add code to make use of this field (and do whatever
	 * initialization etc. you need).
	 */
	public Machine(MachineType machineType, Food food, int capacityIn) {
		this.machineType = machineType;
		this.machineFoodType = food;
		this.capacity = capacityIn;
		this.slots = new LinkedList<Thread>();
		Simulation.logEvent(SimulationEvent.machineStarting(this, this.machineFoodType, this.capacity));
		//YOUR CODE GOES HERE...

	}
	
	public boolean full() {
		
			return load == capacity;
		
	}

	/**
	 * This method is called by a Cook in order to make the Machine's
	 * food item.  You can extend this method however you like, e.g.,
	 * you can have it take extra parameters or return something other
	 * than Object.  It should block if the machine is currently at full
	 * capacity.  If not, the method should return, so the Cook making
	 * the call can proceed.  You will need to implement some means to
	 * notify the calling Cook when the food item is finished.
	 */
	public Thread makeFood() throws InterruptedException {
		
		synchronized(slots) {
		load++;
		Thread newslot = new Thread(new Slot(this.machineFoodType.cookTimeS));
		slots.offer(newslot);
		newslot.run();
		Simulation.logEvent(SimulationEvent.machineCookingFood(this, this.machineFoodType));
		return newslot;
		}
		
	}
	
	public boolean retrieveFood() throws InterruptedException {
		
		synchronized(slots) {
			if(load == 0 || slots.peek().isAlive()) return false;
			load--;
			Simulation.logEvent(SimulationEvent.machineDoneFood(this, machineFoodType));
			slots.poll();
			return true;	
		}
		
	}

	
	public void shutdown() throws InterruptedException {
		for(Thread t : slots) {
			t.join();
		}
		Simulation.logEvent(SimulationEvent.machineEnding(this));
	}
	//THIS MIGHT BE A USEFUL METHOD TO HAVE AND USE BUT IS JUST ONE IDEA
	private class Slot implements Runnable {
		
		private final int cooktime;
		
		public Slot(int time) {
			cooktime = time;
		}
		
		public void run() {
			try {
				Thread.sleep(cooktime);
				
				//this.notifyAll();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
