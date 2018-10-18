package cmsc433.p2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ValidateCook {
	String cookName;
	boolean cookStarted; //tracks if cook thread has started or not. This is NOT set to false when the thread ends!
	boolean cookFinished; //tracks if cook thread has ended or not.
	boolean hasOrder;
	int currOrderNum;
	int orderListSize;
	HashMap<Food,Integer>[] orderList = new HashMap[4];			//These are its components;
	//HashMap<Food, Integer> order; 			//index0
	//HashMap<Food, Integer> orderNotStarted;	//index1
	//HashMap<Food, Integer> orderCooking;		//index2
	//HashMap<Food, Integer> orderCooked;		//index3

	public ValidateCook(String cookName) {
		this.cookName = cookName;
		cookStarted = true;
		cookFinished = false;
		orderList[0] = new HashMap<Food,Integer>();
		orderList[1] = new HashMap<Food,Integer>();
		orderList[2] = new HashMap<Food,Integer>();
		orderList[3] = new HashMap<Food,Integer>();
	}
			
	public void cookNewOrder(int orderNum, List<Food> orderRaw) throws ValidateMyException {
		
		if(!cookStarted
		   || cookFinished
		   || hasOrder
		  ) {
			//Can't begin a new order while executing an existing order.
			throw new ValidateMyException();
		}
		
		Iterator<Food> foodItr = orderRaw.iterator();
		Food curr;
		orderListSize = 0;
		while(foodItr.hasNext()) {
			curr = foodItr.next();
			orderList[0].putIfAbsent(curr, 0);
			orderList[0].put(curr, 1 + orderList[0].get(curr));
			orderListSize++;
		}
		
		this.currOrderNum = orderNum;
		hasOrder = true;	  //dont accept new orders!
		//orderList[0] = order; //order				//order, which doesn't change.
		orderList[1] = new HashMap<Food,Integer>(orderList[0]); //orderNotStarted, which is originally the same as order.
		orderList[2] = new HashMap<Food,Integer>(); //orderCooking, which starts empty.
		orderList[3] = new HashMap<Food,Integer>(); //orderCooked, which starts empty.
	}
	
	public void cookStartCooking(Food food) throws ValidateMyException {
		// 1: orderNotStarted
		// ->
		// 2: orderCooking
		cookSimulationUpdate(food, 1, 2);
	}
	
	public void cookDoneCooking(Food food) throws ValidateMyException {
		// 2: orderCooking
		// ->
		// 3: orderCooked 
		cookSimulationUpdate(food, 2, 3);
	}
	
	public void cookSimulationUpdate(Food food, int startingMapIndex, int endingMapIndex) throws ValidateMyException {
		HashMap<Food,Integer> startingMap = orderList[startingMapIndex];
		HashMap<Food,Integer> endingMap = orderList[endingMapIndex];
		
		if (!cookStarted
			|| cookFinished
			|| !(startingMap.containsKey(food) && startingMap.get(food) > 0) //not going over capacity of the order
		   ) {
			throw new ValidateMyException();
		}
				
		//This food is in line to be cooked, it can begin being cooked!
		startingMap.put(food, startingMap.get(food) - 1);
		
		//This food is now cooking.
		endingMap.putIfAbsent(food, 0);
		endingMap.put(food, endingMap.get(food) + 1);
	}
		
	public void cookCompletedOrder(int orderNum) throws ValidateMyException {
		//Validate params
		if(!cookStarted
		   ||cookFinished
		   ||currOrderNum != orderNum
		   ||!hasOrder) {
			//Can't end an order if you aren't executing an order!
			throw new ValidateMyException();
		}
		
		//Validate that the cookedList has every food that is in the original order, with the same value.
		// Note: This won't check for food in the cookedList that ISNT in the original order, we need another check for that.
		for(Map.Entry<Food, Integer> entry : orderList[0].entrySet()) {
			Food currFood = entry.getKey();
			Integer currFoodQuantity = orderList[3].get(currFood);
			
			if (currFoodQuantity == null || entry.getValue() != currFoodQuantity) {
				throw new ValidateMyException();
			}
		}
		
		//Make sure there isnt any Food in the cookedList that ISNT in the original order
		int sumCooked = 0;
		for(Map.Entry<Food, Integer> entry : orderList[3].entrySet()) {
			if(!orderList[0].containsKey(entry.getKey())) { //if the cooked food has a food element not in the original order, thats an error!
				throw new ValidateMyException();
			}
			sumCooked += entry.getValue();
		}
		if(sumCooked != orderListSize) {
			throw new ValidateMyException();
		}
		
		hasOrder = false;
		currOrderNum = -1;
	}
	
	public void cookTerminate() throws ValidateMyException {
		//Can't kill itself if it's already dead. (or if it never started!)
		if(!cookStarted
		   || cookFinished) {
			throw new ValidateMyException();
		}
		cookFinished = true;
	}
	
}
