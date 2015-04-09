package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

/**
 * This class abstracts the peasants in a state so that we can modify their cargo and positions in a state
 * without affecting the actual peasant unit.
 * 
 * @author Adam Boe
 * @author Tim Sesler
 *
 */
public class Peasant {
	
	private int id;
	private int cargoAmount;
	private Position rootPosition;
	private Position statePosition;
	private ResourceType cargoType;
	private int carryLimit;
	
	/**
	 * Constructor
	 */
	public Peasant() {}
	
	/**
	 * Creates a Peasant from a UnitView peasant
	 * @param peasant
	 */
	public Peasant(UnitView peasant) {
		
		this.id = peasant.getID();
		this.carryLimit = 100;
		this.cargoAmount = 0;
		this.rootPosition = new Position(peasant.getXPosition(), peasant.getYPosition());
		this.statePosition = new Position(this.rootPosition);		
	}
	
	/**
	 * Creates a peasant at a given position
	 * @param peasantId
	 * @param position
	 */
	public Peasant(int peasantId, Position position) {
		
		this.id = peasantId;
		this.carryLimit = 100;
		this.cargoAmount = 0;
		this.rootPosition = position;
		this.statePosition = new Position(this.rootPosition);
	}
	
	/**
	 * Creates a clone of a peasant to be given to child states so that the peasants of parent states
	 * are not affected by subsequent actions on child states
	 * @param parent
	 * @return
	 */
	public Peasant clone(Peasant parent) {
		Peasant child = new Peasant();
		child.id = parent.id;
		child.carryLimit = parent.carryLimit;
		child.cargoAmount = parent.cargoAmount;
		child.rootPosition = parent.rootPosition;
		child.statePosition = parent.statePosition;
		if (parent.cargoType != null) {
			
			if (parent.cargoType == ResourceType.WOOD) {
				child.cargoType =  ResourceType.WOOD;
			}
			else if (parent.cargoType == ResourceType.GOLD) {
				child.cargoType =  ResourceType.GOLD;
			}	
		}	
		
		return child;
	}
	
	/**
	 * Returns the ID of the peasant
	 * @return
	 */
	public int getID() {
		
		return this.id;
	}
	
	/**
	 * Returns the amount of cargo that the peasant is holding
	 * @return
	 */
	public int getCargoAmount() {
		return this.cargoAmount;
	}
	
	/**
	 * Adds a specified amount of cargo to the peasant
	 * @param amount
	 */
	public void addCargo(int amount) {
		
		if (cargoAmount + amount <= carryLimit) {
			cargoAmount += amount;
		}
		else {
			System.err.println("Peasant already carrying maximum load.");
		}
	}
	
	/**
	 * Removes all the cargo from the peasant 
	 */
	public void removeCargo() {
		this.cargoAmount = 0;
	}
	
	/**
	 * Returns the type of cargo the peasant is carrying
	 * @return
	 */
	public ResourceType getCargoType() {
		
		if (this.cargoType != null) {
			
			if (cargoType.toString().toLowerCase().equals("wood")) {
				return ResourceType.WOOD;
			}
			else if (cargoType.toString().toLowerCase().equals("gold")) {
				return ResourceType.GOLD;
			}	
		}		
		return null;
	}
	
	/**
	 * Sets the cargo type of the peasant
	 * @param type
	 */
	public void setCargoType(ResourceType type) {
		this.cargoType = type;
	}
	
	/**
	 * Returns the current position of the peasant
	 * @return
	 */
	public Position getPosition() {
		
		return this.statePosition;
	}
	
	/**
	 * Returns the x position of the peasant
	 * @return
	 */
	public int getXPosition() {
		
		return this.statePosition.x;
	}
	
	/**
	 * Returns the y position of the peasant
	 * @return
	 */
	public int getYPosition() {
		
		return this.statePosition.y;
	}
	
	/**
	 * Sets the position of the peasant
	 * @param position
	 */
	public void setPosition(Position position) {
		this.statePosition = new Position(position);
	}
}