package cmsc433.p4.util;

import java.util.LinkedList;
import cmsc433.p4.enums.*;
import cmsc433.p4.messages.*;
import akka.actor.ActorRef;

public class BlockQueue {

	private LinkedList<BlockedRequest> blockedRequests= new LinkedList<BlockedRequest>();
	private boolean disabled = false;
	
	public boolean submit(ActorRef a, AccessRequestMsg msg) {
		return blockedRequests.add(new BlockedRequest(a, msg));
	}
	
	public boolean isEmpty() {
		return blockedRequests.isEmpty();
	}
	
	public LinkedList<AccessRequestMsg> poll(){
		ActorRef nextActor  = blockedRequests.peek().actor;
		LinkedList<BlockedRequest> submittableRequests = new LinkedList<BlockedRequest>();
		LinkedList<AccessRequestMsg> submittableMsgs = new LinkedList<AccessRequestMsg>();
		
		if(blockedRequests.isEmpty()) return new LinkedList<AccessRequestMsg>();
		
		for(BlockedRequest b : blockedRequests) {
			if(b.actor.equals(nextActor)) {
				submittableRequests.add(b);
				submittableMsgs.add(b.msg);
			}
		}
		
		blockedRequests.removeAll(submittableRequests);
		
		return submittableMsgs;
	}

	public boolean isClosed() {
		return disabled;
	}

	public void toggleDisable() {
		disabled = !disabled;
		
	}
}

