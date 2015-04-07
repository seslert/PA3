package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

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
	
	public Position getOrigin() {
		
		return null;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		
		return false;
	}
	
	@Override
	public GameState apply(GameState state) {
		
		System.out.println("Applying move for state: " + state.hashCode());
		
		if (state.peasants != null) {
			state.peasants.get(peasantId).setPosition(destination);
		}
		
		return state;
	}
	
	@Override
	public String toString() {
		
		return new String("ACTION: MOVE PEASANT: " + peasantId + " DESTINATION: " + destination.toString()) + "\n";
	}

}