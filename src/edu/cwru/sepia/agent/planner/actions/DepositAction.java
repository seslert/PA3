package edu.cwru.sepia.agent.planner.actions;

import edu.cwru.sepia.agent.planner.GameState;

public class DepositAction implements StripsAction {
	
	@Override
	public boolean preconditionsMet(GameState state) {
		
		return false;
	}
	
	@Override
	public GameState apply(GameState state) {
		
		return null;
	}

}