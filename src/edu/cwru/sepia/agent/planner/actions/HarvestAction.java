package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.util.Direction;

public class HarvestAction implements StripsAction {
	
	private Direction townhallDirection;
	
	private int peasantId;
	
	public HarvestAction(int peasantId, Direction townhallDirection) {
		
		this.peasantId = peasantId;
		this.townhallDirection = townhallDirection;
	}
	
	/**
	 * 
	 * @return
	 */
	public Direction getTownhallDirection() {
		
		return this.townhallDirection;			
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
		
		return false;
	}
	
	@Override
	public GameState apply(GameState state) {
		
		return null;
	}

}