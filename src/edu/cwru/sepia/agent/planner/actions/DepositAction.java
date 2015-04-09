package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

public class DepositAction implements StripsAction {
	
	private Direction townhallDirection;
	private ResourceType resourceType;
	private int peasantId;
	
	/**
	 * Creates a new deposit action associated with a peasant and resource type
	 * @param peasantId
	 * @param townhallDirection
	 * @param resourceType
	 */
	public DepositAction(int peasantId, Direction townhallDirection, String resourceType) {
		
		this.peasantId = peasantId;
		this.townhallDirection = townhallDirection;
		this.resourceType = getEnum(resourceType);
	}
	
	/**
	 * Determines the resource type from the string passed to the constructor
	 * @param value
	 * @return
	 */
	public ResourceType getEnum(String value) {
		
		if (value.toLowerCase().equals("wood")) {
			
			return ResourceType.WOOD;
		}
		return ResourceType.GOLD;
	}
	
	/**
	 * Returns the direction to the townhall
	 * @return
	 */
	public Direction getTownhallDirection() {
		
		return this.townhallDirection;			
	}
	
	/**
	 * Sets the unit ID to be associated with this action
	 */
	@Override
	public void setUnitId(int id) {
		this.peasantId = id;
	}
	
	/**
	 * Returns the unit ID associated with this action
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
		Position peasantPos = state.peasants.get(peasantId).getPosition();
		Position townhallPos = state.getUnitPosition(state.townhall);
		
		return peasantPos.isAdjacent(townhallPos);
	}
	
	/**
	 * Applies the effects of the action to a given state
	 */
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
		state.peasants.get(peasantId).removeCargo();
		state.cost = 2;
		state.gCost += state.cost;
		
		return state;
	}
	
	/**
	 * Returns a string of the action 
	 */
	@Override
	public String toString() {
		
		return "ACTION: DEPOSIT( Peasant " + peasantId + " | " + resourceType.toString() + " )" + "\n";
	}

}