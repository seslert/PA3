package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

public class HarvestAction implements StripsAction {
	
	private Direction resourceDirection;
	private ResourceType resourceType;
	private int resourceId;
	private int peasantId;
	
	/**
	 * Creates a harvest action associated with a unit and a resource
	 * @param peasantId
	 * @param resourceDirection
	 * @param resourceType
	 * @param resourceId
	 */
	public HarvestAction(int peasantId, Direction resourceDirection, String resourceType, int resourceId) {
		
		this.peasantId = peasantId;
		this.resourceDirection = resourceDirection;
		this.resourceId = resourceId;
		this.resourceType = getEnum(resourceType);
	}
	
	/**
	 * Returns the resource type to be harvested based off of the string passed to the constructor
	 * @param value
	 * @return
	 */
	public ResourceType getEnum(String value) {
		
		if (value.toLowerCase().equals("tree")) {
			
			return ResourceType.WOOD;
		}
		return ResourceType.GOLD;
	}
	
	/**
	 * Returns the direction of the resource to be harvested.  Used for Sepia primitiveGather action
	 * @return
	 */
	public Direction getResourceDirection() {
		
		return this.resourceDirection;			
	}
	
	/**
	 * Sets the ID of the unit associated with this action
	 */
	@Override
	public void setUnitId(int id) {
		this.peasantId = id;
	}
	
	/**
	 * Returns the ID of the unit associated with this action
	 * @return
	 */
	@Override
	public int getUnitId() {
		
		return this.peasantId;
	}
	
	/**
	 * Determines if this action can be performed in the given state
	 */
	@Override
	public boolean preconditionsMet(GameState state) {
		
		switch (resourceType) {
		case GOLD:
			
			Position peasantPos = state.peasants.get(peasantId).getPosition();
			
			for (ResourceNode resource : state.resources) {
				
				String resourceType = resource.getResourceType().name().toLowerCase();
				
				if (resourceType.equals("wood")) {
					return false;
				}
				else {
					Position resourcePos = state.getResourcePosition(resource);
				
					if (peasantPos.isAdjacent(resourcePos)) {
						return true;
					}
				}
			}
			break;
			
		case WOOD:
			peasantPos = state.peasants.get(peasantId).getPosition();
			
			for (ResourceNode resource : state.resources) {
				
				String resourceType = resource.getResourceType().name().toLowerCase();
				
				if (resourceType.equals("gold")) {
					return false;
				}
				else {
					Position resourcePos = state.getResourcePosition(resource);
				
					if (peasantPos.isAdjacent(resourcePos)) {
						return true;
					}
				}
			}
			break;
		}
		
		return false;
	}
	
	/**
	 * Applies the effects of this action to the state
	 */
	@Override
	public GameState apply(GameState state) {
		
		switch (resourceType) {
		
		case GOLD:
			state.peasants.get(peasantId).setCargoType(ResourceType.GOLD);
			break;
		case WOOD:
			state.peasants.get(peasantId).setCargoType(ResourceType.WOOD);
			break;
		}
		ResourceNode newResource = null;
		for (ResourceNode resource : state.resources) {
			if (resource.getID() == resourceId) {
				newResource = resource;
			}
		}
		state.peasants.get(peasantId).addCargo(newResource.reduceAmountRemaining(100));
		if (newResource.getAmountRemaining() <= 0) {
			state.resources.remove(newResource);
		}
		state.cost = 2;
		state.gCost += state.cost;	
		
		return state;
	}
	
	/**
	 * Returns a purty formatted string representation of this action
	 */
	@Override
	public String toString() {
		
		return "ACTION: HARVEST( Peasant " + peasantId + " | " + resourceType.toString() + " )" + "\n";
	}

}