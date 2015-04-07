package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

public class DepositAction implements StripsAction {
	
	private Direction resourceDirection;
	private enum ResourceType { GOLD, WOOD };
	private ResourceType resourceType;
	private int peasantId;
	
	public DepositAction(int peasantId, Direction resourceDirection, String resourceType) {
		
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
		UnitView peasant = state.stateView.getUnit(peasantId);
		Position peasantPos = state.getUnitPosition(peasant);
		Position townhallPos = state.getUnitPosition(state.townhall);
		
		return peasantPos.isAdjacent(townhallPos);
	}
	
	@Override
	public GameState apply(GameState state) {
		
		switch (resourceType) {
		
		case GOLD:
			state.addGold(100);
			break;
		case WOOD:
			state.addWood(100);
			break;
	
		}
		
		return state;
	}
	
	@Override
	public String toString() {
		
		return "ACTION: DEPOSIT PEASANT: " + peasantId + "RESOURCE: " + resourceType.toString() + "\n";
	}

}