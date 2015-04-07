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
	private ResourceNode resourceNode;
	private int peasantId;
	
	public HarvestAction(int peasantId, Direction resourceDirection, String resourceType, ResourceNode resourceNode) {
		
		this.peasantId = peasantId;
		this.resourceDirection = resourceDirection;
		this.resourceNode = resourceNode;
		this.resourceType = getEnum(resourceType);
	}
	
	public ResourceType getEnum(String value) {
		
		if (value.toLowerCase().equals("tree")) {
			
			return ResourceType.WOOD;
		}
		return ResourceType.GOLD;
	}
	
	/**
	 * 
	 * @return
	 */
	public Direction getResourceDirection() {
		
		return this.resourceDirection;			
	}
	
	/**
	 * 
	 * @return
	 */
	public int getPeasantId() {
		
		return this.peasantId;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		
		switch (resourceType) {
		case GOLD:
			
			Position peasantPos = state.peasants.get(peasantId).getPosition();
			
			for (ResourceNode resource : state.resources) {
				
				String resourceType = resource.getType().name().toLowerCase();
				
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
				
				String resourceType = resource.getType().name().toLowerCase();
				
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
		state.peasants.get(peasantId).addCargo(resourceNode.reduceAmountRemaining(100));
		
//		if (resourceNode.getAmountRemaining() >= 100) {
//			resourceNode.reduceAmountRemaining(100);
//		}		
		
		return state;
	}
	
	@Override
	public String toString() {
		
		return "ACTION: HARVEST PEASANT: " + peasantId + " RESOURCE: " + resourceType.toString() + "\n";
	}

}