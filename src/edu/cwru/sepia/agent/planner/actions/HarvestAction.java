package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

public class HarvestAction implements StripsAction {
	
	private Direction resourceDirection;	
	private enum ResourceType { GOLD, WOOD };
	private ResourceType resourceType;
	private int peasantId;
	
	public HarvestAction(int peasantId, Direction resourceDirection, String resourceType) {
		
		this.peasantId = peasantId;
		this.resourceDirection = resourceDirection;
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
		
//		switch (resourceType) {
//		case GOLD:
//			
//			UnitView peasant = state.stateView.getUnit(peasantId);
//			Position peasantPos = state.getUnitPosition(peasant);
//			
//			for (ResourceView resource : state.resources) {
//				
//				String resourceType = resource.getType().name().toLowerCase();
//				
//				if (resourceType.equals("wood")) {
//					return false;
//				}
//				else {
//					Position resourcePos = state.getResourcePosition(resource);
//				
//					if (peasantPos.isAdjacent(resourcePos)) {
//						return true;
//					}
//				}
//			}
//			break;
//			
//		case WOOD:
//			break;
//		}
		
		return false;
	}
	
	@Override
	public GameState apply(GameState state) {
		UnitView peasant = state.stateView.getUnit(peasantId);
		//peasant.getTemplateView()
		
		return null;
	}
	
	@Override
	public String toString() {
		
		return "ACTION: HARVEST PEASANT: " + peasantId + " RESOURCE: " + resourceType.toString() + "\n";
	}

}