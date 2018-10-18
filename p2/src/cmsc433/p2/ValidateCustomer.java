package cmsc433.p2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ValidateCustomer {
	String customerName;
	boolean customerStarted;
	boolean customerFinished;
	boolean seated;
	int orderNumber;
	boolean placedOrder;
	boolean recievedOrder;
	boolean leftRatsies;
	HashMap<Food, Integer> order;
	int orderSize = 0;
	
	//createCustomer
	public ValidateCustomer(String name, List<Food> orderRaw) {
		customerName = name;
		customerStarted = true;
		customerFinished = false;
		seated = false;
		orderNumber = -1;
		placedOrder = false;
		recievedOrder = false;
		leftRatsies = false;
		Iterator<Food> foodItr = orderRaw.iterator();
		Food curr;
		while(foodItr.hasNext()) {
			curr = foodItr.next();
			order.putIfAbsent(curr, 0);
			order.put(curr, 1 + order.get(curr));
			orderSize++;
		}
	}
	
	//customerEntersRatsies
	public void customerEntersRatsies() throws ValidateMyException {
		if (!customerStarted || customerFinished || seated
			|| placedOrder || recievedOrder || leftRatsies
		    ) {
			throw new ValidateMyException();
		}
		seated = true;
	}
	
	//customerPlacedOrder
	public void customerPlacedOrder()  throws ValidateMyException {
		if (!customerStarted || customerFinished || !seated
			|| placedOrder || recievedOrder || leftRatsies
			) {
				throw new ValidateMyException();
		}
		placedOrder = true;
	}
	
	//customerRecievedOrder
	public void customerRecievedOrder(List<Food> orderGivenIN)  throws ValidateMyException {
		if (!customerStarted || customerFinished || seated
			|| !placedOrder || recievedOrder || leftRatsies
			) {
				throw new ValidateMyException();
		}
		
		//check that the order matches.
		HashMap<Food,Integer> orderGiven = new HashMap<Food,Integer>();
		Iterator<Food> foodItr = orderGivenIN.iterator();
		int orderGivenSize = 0;
		Food curr;
		while(foodItr.hasNext()) {
			curr = foodItr.next();
			orderGiven.putIfAbsent(curr, 0);
			orderGiven.put(curr, 1 + orderGiven.get(curr));
			orderGivenSize++;
		}
		
		//Validate that the orderGiven has every food that is in the original order, with the same value.
		// Note: This won't check for food in the orderGiven that ISNT in the original order, we need another check for that.
		for(Map.Entry<Food, Integer> entry : order.entrySet()) {
			Food currFood = entry.getKey();
			Integer currFoodQuantity = orderGiven.get(currFood);
			
			if (currFoodQuantity == null || entry.getValue() != currFoodQuantity) {
				throw new ValidateMyException();
			}
		}
		
		//Make sure there isnt any Food in the cookedList that ISNT in the original order
		int sumCooked = 0;
		for(Map.Entry<Food, Integer> entry : orderGiven.entrySet()) {
			if(!order.containsKey(entry.getKey())) { //if the cooked food has a food element not in the original order, thats an error!
				throw new ValidateMyException();
			}
			sumCooked += entry.getValue();
		}
		if(sumCooked != orderSize) {
			throw new ValidateMyException();
		}
	}
	
	//customerLeavesRatises = self.terminate
	public void customerLeavesRatsies() throws ValidateMyException {
		if (!customerStarted || customerFinished || seated
			|| !placedOrder || !recievedOrder || leftRatsies
			) {
				throw new ValidateMyException();
		}
		
		//leaving
		
	}
}
