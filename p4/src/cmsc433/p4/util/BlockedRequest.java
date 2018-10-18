package cmsc433.p4.util;

import cmsc433.p4.enums.*;
import cmsc433.p4.messages.*;
import akka.actor.ActorRef;

public class BlockedRequest {
	public ActorRef actor;
	public AccessRequestMsg msg;
	public AccessType type;
	
	public BlockedRequest(ActorRef newActor, AccessRequestMsg newmsg) {
		actor = newActor;
		msg = newmsg;
		type =  newmsg.getAccessRequest().getType().equals(AccessRequestType.CONCURRENT_READ_BLOCKING) ? AccessType.CONCURRENT_READ: AccessType.EXCLUSIVE_WRITE;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ActorRef) {
			return actor.equals(obj);
		} else if (obj instanceof BlockedRequest) {
			return actor.equals(((BlockedRequest) obj).actor) && msg.equals(((BlockedRequest) obj).msg) && type.equals(((BlockedRequest) obj).type);
		} else return false;
	}
	
}
