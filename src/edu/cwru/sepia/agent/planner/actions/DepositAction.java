package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.util.Direction;

public class DepositAction implements StripsAction {
	
private Direction resourceDirection;
	
	private int peasantId;
	
	public DepositAction(int peasantId, Direction resourceDirection) {
		
		this.peasantId = peasantId;
		this.resourceDirection = resourceDirection;
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
		
		return false;
	}
	
	@Override
	public GameState apply(GameState state) {
		
		return null;
	}

}