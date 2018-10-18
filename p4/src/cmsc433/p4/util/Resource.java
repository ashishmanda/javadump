package cmsc433.p4.util;

import java.util.LinkedList;

import akka.actor.ActorRef;
import cmsc433.p4.enums.ResourceStatus;

/**
 * Class of resources.
 * 
 * @author Rance Cleaveland
 *
 */
public class Resource {
	public final String name;	// Resource name
	private volatile ResourceStatus status = ResourceStatus.DISABLED;
	private LinkedList<ActorRef> readers = new LinkedList<ActorRef>();
	private LinkedList<ActorRef> writers = new LinkedList<ActorRef>();
	/**
	 * Creates new resource with given name, and default status of DISABLED.
	 * @param name	Name of resource
	 */
	public Resource (String name) {
		this.name = name;
	}
	
	public boolean isOnlyReader(ActorRef a) {
		for (ActorRef reader: readers) {
			if(!reader.equals(a)) {
				return false;
			}
		}
		return true;
	}
	/**
	 * @return	Name of resource
	 */
	public String getName() {
		return name;
	}


	/**
	 * @return Status of resource
	 */
	public ResourceStatus getStatus() {
		return status;
	}
	
	/**
	 * Change resource status to ENABLED.
	 */
	public void enable() {
		status = ResourceStatus.ENABLED;
	}
	
	/**
	 * Change resource status to DISABLED.
	 */
	public void disable () {
		status = ResourceStatus.DISABLED;
	}
	
	/**
	 * Change resource status to WRITING.
	 */
	public void addWriter(ActorRef a) {
		writers.add(a);
		if(readers.isEmpty()) {
			status = ResourceStatus.WRITING;
		} else {
			status = ResourceStatus.BOTH;
		}
	}
	
	public boolean isReader(ActorRef a) {
		return readers.contains(a);
	}
	
	public boolean isWriter(ActorRef a) {
		if(writers.isEmpty()) return false;
		if(writers.contains(a)) {
			for(ActorRef user : writers) {
				if(!user.equals(a)) {
					System.out.println("Somehow there are 2 or more concurrent users with write permissions");
				}
			}
		}
		
		return writers.contains(a);
	}
	
	public boolean removeWriter(ActorRef a) {
		boolean existed = writers.remove(a);
		if(!existed )return false;
		if (writers.isEmpty() && readers.isEmpty()) {
			status = ResourceStatus.ENABLED;
		} else if(writers.isEmpty() ) {
			status = ResourceStatus.READING;
		}
		return true;
	}
	
	public boolean removeReader(ActorRef a){
		boolean existed = readers.remove(a);
		if(!existed) return false;
		
		if(readers.isEmpty() && writers.isEmpty()) {
			status = ResourceStatus.ENABLED;
		} else if(readers.isEmpty()) {
			status = ResourceStatus.WRITING;
		}
		return true;
	}
	
	
	
	/**
	 * Change resource status to READING.
	 */
	public void addReader(ActorRef a) {
		readers.add(a);
		if(writers.isEmpty()) {
			status = ResourceStatus.READING;
		} else {
			status = ResourceStatus.BOTH;
		}
	}
}
