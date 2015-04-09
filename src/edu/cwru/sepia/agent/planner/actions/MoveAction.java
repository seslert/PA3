package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

public class MoveAction implements StripsAction {
	
	private int peasantId;
	private Position destination;
	private Position origin;
	
	/**
	 * Creates a MoveAction associated with the given unit
	 * @param destination
	 */
	public MoveAction(int peasantId, Position destination) {
		
		this.peasantId = peasantId;
		this.destination = destination;
	}
	
	/**
	 * Creates a MoveAction associated with the given unit
	 * @param x
	 * @param y
	 */
	public MoveAction(int peasantId, int x, int y) {
		
		this(peasantId, new Position(x, y));
	}
	
	/**
	 * Sets the ID of the peasant 
	 */
	@Override
	public void setUnitId(int id) {
		this.peasantId = id;
	}
	
	/**
	 * Gets the unit ID associated with this action
	 */
	@Override
	public int getUnitId() {
		
		return this.peasantId;
	}
	
	/**
	 * Returns the destination of this move action
	 * @return
	 */
	public Position getDestination() {
		
		return this.destination;
	}
	
	/**
	 * Returns the origin of the peasant associated with this move action
	 * @return
	 */
	public Position getOrigin() {
		
		return this.origin;
	}
	
	/**
	 * We check for this in the game state before we create the child so this method is unused. Sorry about that.
	 */
	@Override
	public boolean preconditionsMet(GameState state) {
		
		return false;
	}
	
	/**
	 * Applies the effects of this action to the state
	 */
	@Override
	public GameState apply(GameState state) {
		this.origin = state.peasants.get(peasantId).getPosition();
		
		if (state.peasants != null) {
			state.peasants.get(peasantId).setPosition(destination);
		}
		state.cost = getOrigin().euclideanDistance(destination);
		state.gCost += state.cost;
		
		return state;
	}
	
	/**
	 * Returns a readable format of this action for the plan.txt file
	 */
	@Override
	public String toString() {
		
		return new String("ACTION: MOVE( Peasant " + peasantId + " | " + destination.toString()) + " )" + "\n";
	}

}