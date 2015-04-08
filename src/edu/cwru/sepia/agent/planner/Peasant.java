package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class Peasant {
	
	private int id;
	private int cargoAmount;
	private Position rootPosition;
	private Position statePosition;
	private ResourceType cargoType;
	
	private int carryLimit;
	
	public Peasant() {
		
	}
	
	public Peasant(UnitView peasant) {
		
		this.id = peasant.getID();
		this.carryLimit = 100;
		this.cargoAmount = 0;
		this.rootPosition = new Position(peasant.getXPosition(), peasant.getYPosition());
		this.statePosition = new Position(this.rootPosition);		
	}
	
	public Peasant(int peasantId, Position position) {
		
		this.id = peasantId;
		this.carryLimit = 100;
		this.cargoAmount = 0;
		this.rootPosition = position;
		this.statePosition = new Position(this.rootPosition);
	}
	
	public Peasant Clone(Peasant parent) {
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
	
	public int getID() {
		
		return this.id;
	}
	
	public int getCargoAmount() {
		return this.cargoAmount;
	}
	
	public void addCargo(int amount) {
		
		if (cargoAmount + amount <= carryLimit) {
			cargoAmount += amount;
		}
		else {
			System.err.println("Peasant already carrying maximum load.");
		}
	}
	
	public void removeCargo() {
		this.cargoAmount = 0;
	}
	
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
	
	public void setCargoType(ResourceType type) {
		this.cargoType = type;
	}
	
	public Position getPosition() {
		
		return this.statePosition;
	}
	
	public int getXPosition() {
		
		return this.statePosition.x;
	}
	
	public int getYPosition() {
		
		return this.statePosition.y;
	}
	
	public void setPosition(Position position) {
		this.statePosition = new Position(position);
	}
}
