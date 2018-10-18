package cmsc433.p4.actors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import cmsc433.p4.enums.*;
import cmsc433.p4.messages.*;
import cmsc433.p4.util.*;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class ResourceManagerActor extends UntypedActor {
	
	private ActorRef logger;					// Actor to send logging messages to
	private ArrayList<Resource> resources = new ArrayList<Resource>();
	private ArrayList<ActorRef> users = new ArrayList<ActorRef>();
	private HashMap<ActorRef, HashSet<String>> managers = new HashMap<ActorRef, HashSet<String>>() ;
	private HashMap<Resource, BlockQueue> blockQueues = new HashMap<Resource, BlockQueue>();
	private HashSet<WhoHasRequest> currentWhoHasRequests= new HashSet<WhoHasRequest>();
	private HashSet<String> nonExistentResources = new HashSet<String>();
	/**
	 * Props structure-generator for this class.
	 * @return  Props structure
	 */
	static Props props (ActorRef logger) {
		return Props.create(ResourceManagerActor.class, logger);
	}
	
	/**
	 * Factory method for creating resource managers
	 * @param logger			Actor to send logging messages to
	 * @param system			Actor system in which manager will execute
	 * @return					Reference to new manager
	 */
	public static ActorRef makeResourceManager (ActorRef logger, ActorSystem system) {
		ActorRef newManager = system.actorOf(props(logger));
		return newManager;
	}
	
	/**
	 * Sends a message to the Logger Actor
	 * @param msg The message to be sent to the logger
	 */
	public void log (LogMsg msg) {
		logger.tell(msg, getSelf());
	}
	
	/**
	 * Constructor
	 * 
	 * @param logger			Actor to send logging messages to
	 */
	private ResourceManagerActor(ActorRef logger) {
		super();
		this.logger = logger;
		
	}

	// You may want to add data structures for managing local resources and users, storing
	// remote managers, etc.
	//
	// REMEMBER:  YOU ARE NOT ALLOWED TO CREATE MUTABLE DATA STRUCTURES THAT ARE SHARED BY
	// MULTIPLE ACTORS!
	
	/* (non-Javadoc)
	 * 
	 * You must provide an implementation of the onReceive() method below.
	 * 
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	
	private void myPrint(String str) {
		System.out.println(str);
	}
	
	
	private void processNonBlockingReadRequest(Resource resource, AccessRequest request, ActorRef replyTo, AccessRequestMsg msg) {
		ResourceStatus resourceStatus = resource.getStatus();
		if(resource.isWriter(replyTo) ||
				resourceStatus.equals(ResourceStatus.ENABLED) ||
				resourceStatus.equals(ResourceStatus.READING)) {
			
			AccessRequestGrantedMsg r = new AccessRequestGrantedMsg(msg);
			log(LogMsg.makeAccessRequestGrantedLogMsg(replyTo, getSelf(), request));
			resource.addReader(replyTo);
			replyTo.tell(r, getSelf());
			//myPrint(context().self() + ": " + request.getResourceName()+ " " + request.getType().toString() + " " + msg.getClass() + " ACCESS GRANTED");
		} else {
			AccessRequestDeniedMsg r  = new AccessRequestDeniedMsg(msg, AccessRequestDenialReason.RESOURCE_BUSY);
			log(LogMsg.makeAccessRequestDeniedLogMsg(replyTo, getSelf(), request, AccessRequestDenialReason.RESOURCE_BUSY));
			replyTo.tell(r, getSelf());
			//myPrint(context().self() + ": " + request.getResourceName()+ " " + request.getType().toString() + " " + msg.getClass() + " ACCESS DENIED, BUSY");
		}
		
	}
	
	private void processNonBlockingWriteRequest(Resource resource, AccessRequest request, ActorRef replyTo, AccessRequestMsg msg) {
		ResourceStatus resourceStatus = resource.getStatus();
		if(resource.isOnlyReader(replyTo) ||
				resourceStatus.equals(ResourceStatus.ENABLED)){
			AccessRequestGrantedMsg r = new AccessRequestGrantedMsg(msg);
			log(LogMsg.makeAccessRequestGrantedLogMsg(replyTo, getSelf(), request));
			resource.addWriter(replyTo);
			replyTo.tell(r, getSelf());
			//myPrint(context().self() + ": " + request.getResourceName()+ " " + request.getType().toString() + " " + msg.getClass() + " ACCESS GRANTED");
		} else {
			AccessRequestDeniedMsg r  = new AccessRequestDeniedMsg(msg, AccessRequestDenialReason.RESOURCE_BUSY);
			log(LogMsg.makeAccessRequestDeniedLogMsg(replyTo, getSelf(), request, AccessRequestDenialReason.RESOURCE_BUSY));
			//myPrint(context().self() + ": " + request.getResourceName()+ " " + request.getType().toString() + " " + msg.getClass() + " ACCESS DENIED, BUSY");
			replyTo.tell(r, getSelf());
			
		}
	}
	
	private void processNonBlockingRequest(Resource resource, AccessRequest request, ActorRef replyTo, AccessRequestMsg msg) {
		if(request.getType().equals(AccessRequestType.CONCURRENT_READ_NONBLOCKING)) {
			processNonBlockingReadRequest(resource, request, replyTo, msg);
		}
		else if(request.getType().equals(AccessRequestType.EXCLUSIVE_WRITE_NONBLOCKING)) {
			processNonBlockingWriteRequest(resource, request, replyTo, msg);
		}
	}
	
	private void processBlockingReadRequest(Resource resource, AccessRequest request, ActorRef replyTo, AccessRequestMsg msg){
		ResourceStatus resourceStatus = resource.getStatus();
		BlockQueue q = blockQueues.get(resource);
		
		if(resource.isWriter(replyTo) ||
				resourceStatus.equals(ResourceStatus.ENABLED) ||
				resourceStatus.equals(ResourceStatus.READING)) {
			
			AccessRequestGrantedMsg r = new AccessRequestGrantedMsg(msg);
			log(LogMsg.makeAccessRequestGrantedLogMsg(replyTo, getSelf(), request));
			resource.addReader(replyTo);
			replyTo.tell(r, getSelf());
			//myPrint(context().self() + ": " + request.getResourceName()+ " " + request.getType().toString() + " " + msg.getClass() + " ACCESS GRANTED");
		} else {
			q.submit(replyTo, msg);
			//myPrint(context().self() + ": " + request.getResourceName()+ " " + request.getType().toString() + " " + msg.getClass() + " ACCESS QUEUED");
		}
	}
	
	private void processBlockingWriteRequest(Resource resource, AccessRequest request, ActorRef replyTo, AccessRequestMsg msg){
		ResourceStatus resourceStatus = resource.getStatus();
		BlockQueue q = blockQueues.get(resource);
		if(resource.isOnlyReader(replyTo) ||
				resourceStatus.equals(ResourceStatus.ENABLED)){
			AccessRequestGrantedMsg r = new AccessRequestGrantedMsg(msg);
			log(LogMsg.makeAccessRequestGrantedLogMsg(replyTo, getSelf(), request));
			resource.addWriter(replyTo);
			replyTo.tell(r, getSelf());
			//myPrint(context().self() + ": " + request.getResourceName()+ " " + request.getType().toString() + " " + msg.getClass() + " ACCESS GRANTED");
		} else {
			q.submit(replyTo, msg);
			//myPrint(context().self() + ": " + request.getResourceName()+ " " + request.getType().toString() + " " + msg.getClass() + " ACCESS QUEUED");
		}
	}
	
	private void processBlockingRequest(Resource resource, AccessRequest request, ActorRef replyTo, AccessRequestMsg msg) {
		if(request.getType().equals(AccessRequestType.CONCURRENT_READ_BLOCKING)) {
			processBlockingReadRequest(resource, request, replyTo, msg);
		}
		else if(request.getType().equals(AccessRequestType.EXCLUSIVE_WRITE_BLOCKING)) {
			processBlockingWriteRequest(resource, request, replyTo, msg);
		}
	}
	
	private void processAccessRequest(Resource resource, AccessRequest request, ActorRef replyTo, AccessRequestMsg msg) {
		BlockQueue q = blockQueues.get(resource);
		if(resource.getStatus().equals(ResourceStatus.DISABLED) || q.isClosed()) {
			AccessRequestDeniedMsg r = new AccessRequestDeniedMsg(msg, AccessRequestDenialReason.RESOURCE_DISABLED);
			log(LogMsg.makeAccessRequestDeniedLogMsg(replyTo, getSelf(), request, AccessRequestDenialReason.RESOURCE_DISABLED));
			replyTo.tell(r, getSelf());
			//myPrint(context().self() + ": " + request.getResourceName()+ " " + request.getType().toString() + " " + msg.getClass() + " ACCESS DENIED, RESOURCE DOWN");
		}
		else if(request.getType().equals(AccessRequestType.CONCURRENT_READ_NONBLOCKING) || request.getType().equals(AccessRequestType.EXCLUSIVE_WRITE_NONBLOCKING)) {
			processNonBlockingRequest(resource, request,replyTo, msg);
		} 
		else if(request.getType().equals(AccessRequestType.CONCURRENT_READ_BLOCKING ) ||  request.getType().equals(AccessRequestType.EXCLUSIVE_WRITE_BLOCKING)) {
			processBlockingRequest(resource, request,replyTo, msg);
		}
	}
	
	private void processNextJobs(Resource resource, BlockQueue q) {
		
		if(!q.isEmpty()) {
			for(AccessRequestMsg m : q.poll()) {
				getSelf().tell(m, m.getReplyTo());
			}
		} else if(q.isClosed()) {
			resource.disable();
			log(LogMsg.makeResourceStatusChangedLogMsg(getSelf(), resource.name, ResourceStatus.DISABLED));
			//myPrint(context().self() + ": " +resource.getName() + " DISABLED");
			
		}
	}
	
	private void processReadReleaseRequest(Resource resource, AccessRelease release, ActorRef replyTo, AccessReleaseMsg msg) {
		BlockQueue q = blockQueues.get(resource);
		resource.removeReader(replyTo);
		log(LogMsg.makeAccessReleasedLogMsg(replyTo, getSelf(), release));
		//myPrint(context().self() + ": " + release.getResourceName()+ " " + release.getType().toString() + " " + msg.getClass() + " ACCESS RELEASED");
		if(resource.getStatus().equals(ResourceStatus.ENABLED) && !q.isEmpty()) {
			processNextJobs(resource, q);
		}
	}
	
	private void processWriteReleaseRequest(Resource resource, AccessRelease release, ActorRef replyTo, AccessReleaseMsg msg){
		BlockQueue q = blockQueues.get(resource);
		resource.removeWriter(replyTo);
		//myPrint(context().self() + ": " + release.getResourceName()+ " " + release.getType().toString() + " " + msg.getClass() + " ACCESS RELEASED");
		log(LogMsg.makeAccessReleasedLogMsg(replyTo, getSelf(), release));
		if(resource.getStatus().equals(ResourceStatus.ENABLED) && !q.isEmpty()) {
			processNextJobs(resource, q);
		}
		
	}
	
	private void processReleaseRequest(Resource resource, AccessRelease release, ActorRef replyTo, AccessReleaseMsg msg) {
		if(resource.getStatus().equals(ResourceStatus.ENABLED) ||
				( !resource.isWriter(replyTo)  && release.getType().equals(AccessType.EXCLUSIVE_WRITE)) ||
				( !resource.isReader(replyTo)  && release.getType().equals(AccessType.CONCURRENT_READ))) {
			log(LogMsg.makeAccessReleaseIgnoredLogMsg(replyTo, getSelf(), release));
			//myPrint(context().self() + ": " + release.getResourceName()+ " " + release.getType().toString() + " " + msg.getClass() + " REQUEST IGNORED");
		}
		else if(release.getType().equals(AccessType.CONCURRENT_READ)) {
			processReadReleaseRequest(resource, release, replyTo, msg);
		}
		else if(release.getType().equals(AccessType.EXCLUSIVE_WRITE)) {
			processWriteReleaseRequest(resource, release, replyTo, msg);
		} 
		
	}
	
	private void processEnableRequest(Resource resource, ManagementRequest request, ActorRef replyTo, ManagementRequest msg) {
		BlockQueue q = blockQueues.get(resource);
		if(!resource.getStatus().equals(ResourceStatus.DISABLED)){
	    		if(q.isClosed()) q.toggleDisable();
		} else {
			resource.enable();
			q.toggleDisable();
		}
		
		ManagementRequestGrantedMsg r = new ManagementRequestGrantedMsg(msg);
		log(LogMsg.makeManagementRequestGrantedLogMsg(replyTo, getSelf(), request));
		log(LogMsg.makeResourceStatusChangedLogMsg(getSelf(), resource.name, ResourceStatus.ENABLED));
		//myPrint(context().self() + ": " +resource.getName() + " " + request.getType().toString() + " " + msg.getClass() + " GRANTED");
		//myPrint(context().self() + ": " +resource.getName() + " ENABLED");
		replyTo.tell(r, getSelf());
		
	}
	
	private void processDisableRequest(Resource resource, ManagementRequest request, ActorRef replyTo, ManagementRequest msg) {
		if(resource.isReader(replyTo) || resource.isWriter(replyTo)) {
			ManagementRequestDeniedMsg r = new ManagementRequestDeniedMsg(msg, ManagementRequestDenialReason.ACCESS_HELD_BY_USER);
			
			log(LogMsg.makeManagementRequestDeniedLogMsg(replyTo, getSelf(), request, ManagementRequestDenialReason.ACCESS_HELD_BY_USER));
			//myPrint(context().self() + ": " +resource.getName() + " " + request.getType().toString() + " " + msg.getClass() + " DENIED, ACCESS HELD BY USER");
			replyTo.tell(r, getSelf());
		} else { 
			
			
			ManagementRequestGrantedMsg r = new ManagementRequestGrantedMsg(msg);
			log(LogMsg.makeManagementRequestGrantedLogMsg(replyTo, getSelf(), request));
			//myPrint(context().self() + ": " +resource.getName() + " " + request.getType().toString() + " " + msg.getClass() + " GRANTED");
			replyTo.tell(r, getSelf());
			
			if(!resource.getStatus().equals(ResourceStatus.DISABLED)) {
				BlockQueue q = blockQueues.get(resource);
				if(!q.isClosed()) q.toggleDisable();
				if(q.isEmpty()) { 
					resource.disable();
					log(LogMsg.makeResourceStatusChangedLogMsg(getSelf(), resource.name, ResourceStatus.DISABLED));
					//myPrint(context().self() + ": " +resource.getName() + " DISABLED");
				}
			}
			
			
		}
	}
	
	private void processManagementRequest(Resource resource, ManagementRequest request, ActorRef replyTo, ManagementRequest msg){
		if(request.getType().equals(ManagementRequestType.ENABLE)) {
			processEnableRequest(resource, request, replyTo, msg);
		}else {
			processDisableRequest(resource, request, replyTo, msg);
		}
	}
	
	private void sendWhoHas(ActorRef replyTo, Object msg, String resourceName) {
		
		WhoHasRequest w = new WhoHasRequest(resourceName, replyTo, managers.keySet().size(), msg);
		currentWhoHasRequests.add(w);
		for(ActorRef a : managers.keySet()) {
			WhoHasResourceRequestMsg r = new WhoHasResourceRequestMsg(resourceName);
			
			a.tell(r, getSelf());
		}
		
		
	}
	
	private void logForwardedRequest(ActorRef forwardedTo, Object msg) {
		
		if(msg instanceof AccessRequestMsg) {
			log(LogMsg.makeAccessRequestForwardedLogMsg(getSelf(), forwardedTo,((AccessRequestMsg) msg).getAccessRequest()));
			//myPrint(context().self() + ": " + ((AccessRequestMsg) msg).getAccessRequest().getResourceName()+ " " + ((AccessRequestMsg) msg).getAccessRequest().getType().toString() + " " + msg.getClass() + "ACCESS REQUEST FORWARDED");
		} 
		else if(msg instanceof AccessReleaseMsg) {
			log(LogMsg.makeAccessReleaseForwardedLogMsg(getSelf(), forwardedTo, ((AccessReleaseMsg) msg).getAccessRelease()));
			//myPrint(context().self() + ": " + ((AccessReleaseMsg) msg).getAccessRelease().getResourceName()+ " " + ((AccessReleaseMsg) msg).getAccessRelease().getType().toString() + " " + msg.getClass() + "ACCESS RELEASE FORWARDED");
		} 
		else if(msg instanceof ManagementRequestMsg) {
			log(LogMsg.makeManagementRequestForwardedLogMsg(getSelf(), forwardedTo, ((ManagementRequestMsg) msg).getRequest()));
			//myPrint(context().self() + ": " + ((ManagementRequestMsg) msg).getRequest().getResourceName()+ " " + ((ManagementRequestMsg) msg).getRequest().getType().toString() + " " + msg.getClass() + " MANAGEMENT REQUEST FORWARDED");
		}
		
		
		
		
	}
	
	private void forwardRequest(ActorRef replyTo, Object msg, String resourceName) {
		boolean resourceFound = false;
		
		
		for(Map.Entry<ActorRef, HashSet<String> > entry: managers.entrySet()) {
			if(entry.getValue().contains(resourceName)) {
				//forward request
				resourceFound = true;
				logForwardedRequest(entry.getKey(), msg);
				entry.getKey().tell(msg, replyTo);
			}
		}
		
		if(!resourceFound) {
			sendWhoHas(replyTo, msg, resourceName);
		}
	}
	
	private LinkedList<WhoHasRequest> updateTable(String resourceName, ActorRef sender ){
		managers.get(sender).add(resourceName);
		LinkedList<WhoHasRequest> requestsToForward = new LinkedList<WhoHasRequest>();
				
		for(WhoHasRequest w : currentWhoHasRequests) {
			if(w.resourceName().equals(resourceName)) {
				requestsToForward.add(w);
			}
		}
		
		currentWhoHasRequests.removeAll(requestsToForward);
		
		return requestsToForward;
	}
	
	private void denyBecauseNonExistent( ActorRef replyTo , Object msg) {
		if(msg instanceof AccessRequestMsg) {
			AccessRequestDeniedMsg r = new AccessRequestDeniedMsg(((AccessRequestMsg) msg).getAccessRequest(), AccessRequestDenialReason.RESOURCE_NOT_FOUND);
			log(LogMsg.makeAccessRequestDeniedLogMsg(replyTo, getSelf(), ((AccessRequestMsg) msg).getAccessRequest(), AccessRequestDenialReason.RESOURCE_NOT_FOUND));
			//myPrint(context().self() + ": " + ((AccessRequestMsg) msg).getAccessRequest().getResourceName()+ " " + ((AccessRequestMsg) msg).getAccessRequest().getType().toString() + " " + msg.getClass() + "ACCESS REQUEST DENIED, RESOURCE DOES NOT EXIST");
			replyTo.tell(r, getSelf());
		} 
		else if(msg instanceof AccessReleaseMsg) {
			log(LogMsg.makeAccessReleaseIgnoredLogMsg(replyTo, getSelf(), ((AccessReleaseMsg) msg).getAccessRelease()));
			//myPrint(context().self() + ": " + ((AccessReleaseMsg) msg).getAccessRelease().getResourceName() + " " + ((AccessReleaseMsg) msg).getAccessRelease().getType().toString() + " " + msg.getClass() + "ACCESS RELEASE IGNORED, RESOURCE DOES NOT EXIST");
		} 
		else if(msg instanceof ManagementRequestMsg) {
			ManagementRequestDeniedMsg r = new ManagementRequestDeniedMsg(((ManagementRequestMsg) msg).getRequest(), ManagementRequestDenialReason.RESOURCE_NOT_FOUND);
			log(LogMsg.makeManagementRequestDeniedLogMsg(replyTo, getSelf(), ((ManagementRequestMsg) msg).getRequest(), ManagementRequestDenialReason.RESOURCE_NOT_FOUND));
			//myPrint(context().self() + ": " + ((ManagementRequestMsg) msg).getRequest().getResourceName()+ " " + ((ManagementRequestMsg) msg).getRequest().getType().toString() + " " + msg.getClass() + "MANAGEMENT REQUEST DENIED, RESOURCE DOES NOT EXIST");
			replyTo.tell(r, getSelf());
		}
	}
	
	 private void updateWhoHasRequests(String resourceName, ActorRef respondant) {
		 boolean resourceConfirmedNonexistent = false;
		 for(WhoHasRequest w : currentWhoHasRequests) {
			 if(w.resourceName().equals(resourceName)) {
				 w.tallyResponse(respondant.toString());
				 if(w.allResponded()) {
					 resourceConfirmedNonexistent = true;
					 break;
				 }				
			 }
		 }
		 
		 if(resourceConfirmedNonexistent) {
			 nonExistentResources.add(resourceName);
			 LinkedList<WhoHasRequest> oldRequests = new LinkedList<WhoHasRequest>();
			 for(WhoHasRequest w : currentWhoHasRequests) {
				 if(w.resourceName().equals(resourceName)) {
					oldRequests.add(w);
					denyBecauseNonExistent(w.actor(), w.msg());
				 }
			 }
			 
			 currentWhoHasRequests.removeAll(oldRequests);
			 
			 
		 }
	 }
	
	@Override
	public void onReceive(Object msg) throws Exception {		
		//print(msg.getClass().toString());
		//LogMsg m = new LogMsg();
		//System.out.println(getSelf());
		
		//refreshWhoHas();
		
		if(!context().sender().equals(context().self())) {
			if(msg instanceof SimulationStartMsg){
				context().sender().tell(msg, getSelf());
			}
			else if(msg instanceof UserStartMsg) {
				////myPrint(context().self() + ": " + msg.getClass() );
			}
			else if(msg instanceof AddInitialLocalResourcesRequestMsg) {
				////myPrint(context().self() + ": " + msg.getClass() );
				resources.addAll( ((AddInitialLocalResourcesRequestMsg) msg).getLocalResources());
				AddInitialLocalResourcesResponseMsg r = new  AddInitialLocalResourcesResponseMsg(((AddInitialLocalResourcesRequestMsg) msg));
				//context().parent().tell(r, getSelf());
				for(Resource resource: resources) {
					log(LogMsg.makeLocalResourceCreatedLogMsg(getSelf(), resource.name));
					//myPrint(context().self() + ": "+ msg.getClass() + " RESOURCE CREATED");
					resource.enable();
					blockQueues.put(resource, new BlockQueue());
				}
				context().sender().tell(r, getSelf());
				
			
			}
			else if(msg instanceof AddLocalUsersRequestMsg) {
				////myPrint(context().self() + ": "+ msg.getClass() );
				users.addAll(((AddLocalUsersRequestMsg) msg).getLocalUsers());
				AddLocalUsersResponseMsg r = new AddLocalUsersResponseMsg((AddLocalUsersRequestMsg) msg);
				/*for(ActorRef actor: ((AddLocalUsersRequestMsg) msg).getLocalUsers()) {
					log(LogMsg.makeUserStartLogMsg(actor));
				}*/
			
				context().sender().tell(r, getSelf());
			
			}
			else if(msg instanceof AddRemoteManagersRequestMsg) {
				////myPrint(context().self() + ": "+ msg.getClass() );
				for( ActorRef manager : ((AddRemoteManagersRequestMsg) msg).getManagerList()){
					managers.put(manager, new HashSet<String>());
				}
				AddRemoteManagersResponseMsg r = new AddRemoteManagersResponseMsg((AddRemoteManagersRequestMsg) msg);
				
				context().sender().tell(r, getSelf());
			}
			else if(msg instanceof AccessRequestMsg) {
				AccessRequest req = ((AccessRequestMsg) msg).getAccessRequest();
				ActorRef replyto = ((AccessRequestMsg) msg).getReplyTo();
				boolean resourcefound = false;
				log(LogMsg.makeAccessRequestReceivedLogMsg(replyto, getSelf(), req));
				//myPrint(context().self() + ": " + req.getResourceName()+ " " + req.getType().toString() + " " + msg.getClass() + " RECIEVED");
				
				
				if(nonExistentResources.contains(req.getResourceName())) {
					denyBecauseNonExistent(replyto, msg);
				} else {
				
					
					for(Resource resource : resources) {
						if(resource.name.equals(req.getResourceName())) {
							resourcefound = true;	
							processAccessRequest(resource ,req, replyto, (AccessRequestMsg) msg);
						}	
					}
					
					if(!resourcefound) {
						
						forwardRequest(replyto, msg, ((AccessRequestMsg) msg).getAccessRequest().getResourceName());
					}
				}		
			}	
			else if(msg instanceof AccessReleaseMsg) {
				AccessRelease release = ((AccessReleaseMsg) msg).getAccessRelease();
				boolean resourcefound = false;
				log(LogMsg.makeAccessReleaseReceivedLogMsg(getSender(), getSelf(), release));
				//myPrint(context().self() + ": " + release.getResourceName()+ " " + release.getType().toString() + " " + msg.getClass() + " RECIEVED");
				
				if(nonExistentResources.contains(release.getResourceName())) {
					denyBecauseNonExistent(getSender(), msg);
				} else {
					
					for(Resource resource : resources) {
						if(resource.getName().equals(release.getResourceName())) {
							resourcefound = true;
							processReleaseRequest(resource, release, context().sender(), (AccessReleaseMsg)msg);
						}
					}
					
					if(!resourcefound) {
						forwardRequest(getSender(), msg, ((AccessReleaseMsg) msg).getAccessRelease().getResourceName());
					}
					
				}
				
				
			}
			else if(msg instanceof ManagementRequestMsg) {
				ManagementRequest request = ((ManagementRequestMsg) msg).getRequest();
				ActorRef replyTo = ((ManagementRequestMsg) msg).getReplyTo();
				log(LogMsg.makeManagementRequestReceivedLogMsg(replyTo, getSelf(), request));
				//myPrint(context().self() + ": " + request.getResourceName()+ " " + request.getType().toString() + " " + msg.getClass() + " RECIEVED");
				
				if(nonExistentResources.contains(request.getResourceName())) {
					denyBecauseNonExistent(replyTo, msg);
				} else {
					boolean resourcefound = false;
					for(Resource resource : resources) {
						if(resource.name.equals(request.getResourceName())) {
							resourcefound = true;
							processManagementRequest(resource, request, context().sender(), request);
							
						}
						
						if(!resourcefound) {
							forwardRequest(getSender(), msg, ((ManagementRequestMsg) msg).getRequest().getResourceName());
							
						}
					}
				}
				
						
			}
			else if(msg instanceof WhoHasResourceRequestMsg) {
				String resourceName = ((WhoHasResourceRequestMsg) msg).getResourceName();
				boolean found = false;
				////myPrint(context().self() + ": " + resourceName + " " + msg.getClass() );
				for(Resource resource : resources) {
					if(resource.getName().equals(resourceName)) {
						found = true;
					}
				}
				
				WhoHasResourceResponseMsg r = new WhoHasResourceResponseMsg(resourceName, found, getSelf());
				context().sender().tell(r, getSelf());
			}
			else if(msg instanceof WhoHasResourceResponseMsg) {
				boolean discovered = ((WhoHasResourceResponseMsg) msg).getResult();
				String resourceName = ((WhoHasResourceResponseMsg) msg).getResourceName();
				
				
				if (discovered) {
					log(LogMsg.makeRemoteResourceDiscoveredLogMsg(getSelf(), getSender(), resourceName));
					//myPrint(context().self() + ": " + resourceName + " " + msg.getClass() + " DISCOVERED");
					LinkedList<WhoHasRequest> requestsToForward = updateTable(resourceName, getSender() );
					
					for(WhoHasRequest w: requestsToForward) {
						forwardRequest(w.actor(),w.msg() ,w.resourceName());
					}
				} else {
					 updateWhoHasRequests(resourceName, getSender());
				}
			} 
			
			
					
		}
		
		
		
		
	}
	
}
	
	

