package cmsc433.p2;

public class ValidateMachine {
	String type;
	String foodType;
	int capacityMax;
	int capacityCurr;
	boolean started;
	boolean ended;
	
	public ValidateMachine(String type, String foodType, int capacityMax) {
		this.type = type;
		this.foodType = foodType;
		this.capacityMax = capacityMax;
		this.capacityCurr = 0;
		this.started = true;
		this.ended = false;
		
	}
	
	public void machineStartingFood(Food food) throws ValidateMyException {
		if (!started || capacityCurr > capacityMax || capacityCurr < 0
			|| (food.name == null || !food.name.equals(foodType))
			|| capacityCurr == capacityMax
		   ) {
				throw new ValidateMyException();
		}		
		capacityMax++;
	}
	
	public void machineDoneFood(Food food) throws ValidateMyException {
		if (!started || capacityCurr > capacityMax || capacityCurr < 0
			|| (food.name == null || !food.name.equals(foodType))
		    || capacityCurr == 0
		   ) {
			throw new ValidateMyException();
		}		
		capacityCurr--;
	}
	
	public void machineEnding() throws ValidateMyException {
		if (!started || capacityCurr > capacityMax || capacityCurr < 0) {
			throw new ValidateMyException();
		}
		this.ended = true;
	}
}
