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
	
	public BuildPeasant(int townhallId, int playernum) {
		
		this.townhallId = townhallId;
	}
	
	
	/**
	 * 
	 * @return
	 */
	@Override
	public int getPeasantId() {
		
		return this.peasantId;
	}
	
	public int getPeasantTempalteId() {
		
		return this.peasantTemplateId;
	}
	
	public int getTownhallId() {
		
		return this.townhallId;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		return state.getGold() >= 400 && state.stateView.getSupplyCap(townhallId) > 0;
	}
	
	@Override
	public GameState apply(GameState state) {
		
		TemplateView peasantTemplate = state.stateView.getTemplate(state.getPlayernum(), "Peasant");
		peasantTemplateId = peasantTemplate.getID();
		
		Peasant peasant = new Peasant(state.getLargestPeasantId() + 1, new Position(state.townhall.getXPosition(), state.townhall.getYPosition()));
		//Peasant peasant = new Peasant(peasantTemplateID, new Position(state.townhall.getXPosition() + 2, state.townhall.getYPosition() + 2));
		
		this.peasantId = peasant.getID();
		state.peasants.put(peasant.getID(), peasant);
		state.removeGold(400);
		state.reduceFood(1);
		
		return state;
	}
	
	@Override
	public String toString() {
		
		return "ACTION: BUILD PEASANT: " + peasantId + " BY: " + townhallId + "\n";
	}

}