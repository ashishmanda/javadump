package cmsc433.p4.util;

import java.util.LinkedList;
import akka.actor.ActorRef;

public class WhoHasRequest {
	private String resource_name;
	private ActorRef a;
	private int responses;
	private int respondant_pool_size;
	private final long ttl = 1000;
	private long start_time;
	private Object msg;
	private LinkedList <String> respondants;
	
	public WhoHasRequest(String name, ActorRef newA, int pool_size, Object msg){
		resource_name = name;
		a = newA;
		respondants = new LinkedList<String>();
		respondant_pool_size = pool_size-1;
		start_time = System.nanoTime();
		this.msg = msg;
	}
	
	public Object msg() {
		return msg;
	}
	
	public String resourceName() {
		return resource_name;
	}
	
	public ActorRef actor() {
		return a;
	}
	
	public int tally() {
		return responses;
	}
	
	
	public int pool() {
		return respondant_pool_size;
	}

	public boolean tallyResponse(String respondant) {
		if(!respondants.contains(respondant)) {
			responses++;
			respondants.add(respondant);
		}
		return responses >= respondant_pool_size;
	}
	
	public boolean allResponded() {
		return responses >= respondant_pool_size;
	}
	
	public boolean isExpired() {
		return System.nanoTime() - start_time >= ttl;
	}
	
	public int hashCode() {
		int result = 17;
		result = 31*result+ resource_name.hashCode();
		result = 31*result+ a.hashCode();
		result = 31*result+ responses;
		result = 31*result+ respondant_pool_size;
		result = (int) (31*result+ ttl);
		result = (int) (31*result+ start_time);
		result= 31*result+ msg.hashCode();
		result = 31*result+ respondants.hashCode();
		
		return result;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof String) {
			return resource_name.equals(obj);
		} else if(obj instanceof WhoHasRequest) {
			return (resource_name.equals(((WhoHasRequest) obj).resourceName())) && a.equals(((WhoHasRequest) obj).actor()) && responses == ((WhoHasRequest) obj).tally() && respondant_pool_size == ((WhoHasRequest)obj).pool() && msg.equals(((WhoHasRequest) obj).msg());
		} else return false;
	}
	
	
}
