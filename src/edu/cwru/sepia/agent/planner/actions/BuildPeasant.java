package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;
import edu.cwru.sepia.agent.planner.Peasant;
import edu.cwru.sepia.agent.planner.Position;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.Template.TemplateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

public class BuildPeasant implements StripsAction {
	
	private int townhallId;
	private int peasantId;
	private int peasantTemplateId;
	
	/**
	 * Creates a BuildPeasant action associated with the given townhall
	 * @param townhallId
	 */
	public BuildPeasant(int townhallId) {
		
		this.townhallId = townhallId;
	}
	
	/**
	 * Sets the unit ID of the peasant to be created
	 */
	@Override
	public void setUnitId(int id) {
		this.peasantId = id;
	}
	
	/**
	 * Gets the unit ID of the townhall creating the peasant
	 * @return
	 */
	@Override
	public int getUnitId() {
		
		return this.townhallId;
	}
	
	/**
	 * Gets the template ID of the peasant to be built
	 * @return
	 */
	public int getPeasantTemplateId() {
		
		return this.peasantTemplateId;
	}
	
	/**
	 * Determines if the action can be executed in the state
	 */
	@Override
	public boolean preconditionsMet(GameState state) {
		return state.getGold() >= 400 && state.stateView.getSupplyCap(townhallId) > 0;
	}
	
	/**
	 * Applies the effects of the action to the state
	 */
	@Override
	public GameState apply(GameState state) {
		
		TemplateView peasantTemplate = state.stateView.getTemplate(state.getPlayernum(), "Peasant");
		peasantTemplateId = peasantTemplate.getID();
		Peasant peasant = new Peasant(state.getLargestPeasantId() + 1, new Position(state.townhall.getXPosition() - 1, state.townhall.getYPosition()));
		this.peasantId = peasant.getID();
		state.peasants.put(peasant.getID(), peasant);
		state.removeGold(400);
		state.reduceFood(1);
		
		return state;
	}
	
	/**
	 * Returns the result of the action in a readable form
	 */
	@Override
	public String toString() {
		
		return "ACTION: BUILD PEASANT: " + peasantId + " BY: " + townhallId + "\n";
	}

}