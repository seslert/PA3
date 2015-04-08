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
	
	public BuildPeasant(int townhallId) {
		
		this.townhallId = townhallId;
	}
	
	
	/**
	 * 
	 * @return
	 */
	@Override
	public int getPeasantId() {
		
		return this.townhallId;
	}
	
	@Override
	public boolean preconditionsMet(GameState state) {
		return state.getGold() >= 400 && state.stateView.getSupplyCap(townhallId) > 0;
	}
	
	@Override
	public GameState apply(GameState state) {
		
		TemplateView peasantTemplate = state.stateView.getTemplate(state.getPlayernum(), "Peasant");
        int peasantTemplateID = peasantTemplate.getID();     // is this the actual peasants id or just the templates id?
		
		Peasant peasant = new Peasant(peasantTemplateID, new Position(state.townhall.getXPosition(), state.townhall.getYPosition()));
		//Peasant peasant = new Peasant(peasantTemplateID, new Position(state.townhall.getXPosition() + 2, state.townhall.getYPosition() + 2));
		this.peasantId = peasantTemplateID;
		state.peasants.put(peasantTemplateID, peasant);
		state.removeGold(400);
		
		return state;
	}
	
	@Override
	public String toString() {
		
		return "ACTION: BUILD PEASANT: " + peasantId + " BY: " + townhallId + "\n";
	}

}