package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;

public class MoveAction implements StripsAction {
	
	private int peasantId;
	private Position destination;	
	
	/**
	 * 
	 * @param destination
	 */
	public MoveAction(int peasantId, Position destination) {
		this.peasantId = peasantId;
		this.destination = destination;
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 */
	public MoveAction(int peasantId, int x, int y) {
		this(peasantId, new Position(x, y));
	}
	
	public int getPeasantId() {
		
		return this.peasantId;
	}
	
	public Position getDestination() {
		
		return this.destination;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		
		return false;
	}
	
	@Override
	public GameState apply(GameState state) {
		
		return null;
	}
	
	@Override
	public String toString() {
		
		return new String("MOVEACTION " + destination.toString());
	}

}