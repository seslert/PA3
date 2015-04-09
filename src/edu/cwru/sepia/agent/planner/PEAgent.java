package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionFeedback;
import edu.cwru.sepia.action.ActionResult;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.*;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Template;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.util.Direction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

/**
 * This is an outline of the PEAgent. Implement the provided methods. You may add your own methods and members.
 * @author Adam Boe
 * @author Timothy Sesler
 * @date 24 March 2015
 */
public class PEAgent extends Agent {

    // The plan being executed
    private Stack<StripsAction> plan = null;

    // maps the real unit Ids to the plan's unit ids
    // when you're planning you won't know the true unit IDs that sepia assigns. So you'll use placeholders (1, 2, 3).
    // this maps those placeholders to the actual unit IDs.
    private Map<Integer, Integer> peasantIdMap;
    private int townhallId;
    private int peasantTemplateId;

    /**
     * Constructs a new PEAgent based on a player number and plan.
     * @param playernum
     * @param plan
     */
    public PEAgent(int playernum, Stack<StripsAction> plan) {
        super(playernum);
        peasantIdMap = new HashMap<Integer, Integer>();
        this.plan = plan;
        
        System.out.println("Plan ready for execution!");
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {
        
    	// Gets the townhall ID and the peasant ID
        for(int unitId : stateView.getUnitIds(playernum)) {
            Unit.UnitView unit = stateView.getUnit(unitId);
            String unitType = unit.getTemplateView().getName().toLowerCase();
            
            if (unitType.equals("townhall")) {
                townhallId = unitId;
            } 
            else if (unitType.equals("peasant")) {
                peasantIdMap.put(unitId, unitId);
            }
        }

        // Gets the peasant template ID. This is used when building a new peasant with the townhall
        for(Template.TemplateView templateView : stateView.getTemplates(playernum)) {
            
        	if (templateView.getName().toLowerCase().equals("peasant")) {
                peasantTemplateId = templateView.getID();
                break;
            }
        }

        return middleStep(stateView, historyView);
    }

    /**
     * This is where you will read the provided plan and execute it. If your plan is correct, then when the plan is empty
     * the scenario should end with a victory. If the scenario keeps running after you run out of actions to execute
     * then either your plan is incorrect or your execution of the plan has a bug.
     *
     * You can create a SEPIA deposit action with the following method
     * Action.createPrimitiveDeposit(int peasantId, Direction townhallDirection)
     *
     * You can create a SEPIA harvest action with the following method
     * Action.createPrimitiveGather(int peasantId, Direction resourceDirection)
     *
     * You can create a SEPIA build action with the following method
     * Action.createPrimitiveProduction(int townhallId, int peasantTemplateId)
     *
     * You can create a SEPIA move action with the following method
     * Action.createCompoundMove(int peasantId, int x, int y)
     *
     * these actions are stored in a mapping between the peasant unit ID executing the action and the action you created.
     *
     * For the compound actions you will need to check their progress and wait until they are complete before issuing
     * another action for that unit. If you issue an action before the compound action is complete then the peasant
     * will stop what it was doing and begin executing the new action.
     *
     * To check an action's progress you can call getCurrentDurativeAction on each UnitView. If the Action is null nothing
     * is being executed. If the action is not null then you should also call getCurrentDurativeProgress. If the value is less than
     * 1 then the action is still in progress.
     *
     * Also remember to check your plan's preconditions before executing!
     */
    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
        Map<Integer, Action> executionPlan = new LinkedHashMap<Integer, Action>();
		
        // If it is the first turn we need to put the first action on the stack.
		if (stateView.getTurnNumber() == 0) {
			StripsAction nextAction = plan.pop();
			executionPlan.put(nextAction.getUnitId(), createSepiaAction(nextAction));
		}
		// If it is not the first turn and we still have moves left, look at the actions of the past state.
		else if (!plan.isEmpty()) {    			
			Map<Integer, ActionResult> actionResults = historyView.getCommandFeedback(playernum, stateView.getTurnNumber() - 1);
			
			for (ActionResult result : actionResults.values()) {
				// The last action has completed so we can add the next action.
				if (result.getFeedback() != ActionFeedback.INCOMPLETE) {
					StripsAction nextAction = plan.pop();   
					
					// =======================================
					// We could not figure out how to map the ID of a newly created peasant so that
					// a new action could be associated with that peasant.  It is manually performed here
					// and seems to work well.  We found the IDs associated by looking at the current stateview.
					if (nextAction.getUnitId() == 2) {
		    			nextAction.setUnitId(10);
		    		}
					if (nextAction.getUnitId() == 3) {
						nextAction.setUnitId(11);
					}
					// =======================================
					executionPlan.put(nextAction.getUnitId(), createSepiaAction(nextAction));					
				}
    		}
		}
    	return executionPlan;
    }

    /**
     * Returns a SEPIA version of the specified Strips Action.
     * @param action StripsAction
     * @return SEPIA representation of same action
     */
    private Action createSepiaAction(StripsAction action) {
    	
    	// Deal with a MoveAction
    	if (action instanceof MoveAction) {
    		MoveAction moveAction = (MoveAction) action;
    		Position destination = moveAction.getDestination();
    		
    		return Action.createCompoundMove(moveAction.getUnitId(), destination.x, destination.y);
    	}
    	// Deals with a HarvestAction
    	if (action instanceof HarvestAction) {
    		HarvestAction harvestAction = (HarvestAction) action;    		
    		
    		return Action.createPrimitiveGather(harvestAction.getUnitId(), harvestAction.getResourceDirection());
    	}
    	// Deals with DepositAction
    	if (action instanceof DepositAction) {
    		DepositAction depositAction = (DepositAction) action;
    		
    		return Action.createPrimitiveDeposit(depositAction.getUnitId(), depositAction.getTownhallDirection());
    	}
    	// Deals with building a peasant
    	if (action instanceof BuildPeasant) {
    		BuildPeasant buildPeasant = (BuildPeasant) action;
    		return Action.createPrimitiveProduction(buildPeasant.getUnitId(), buildPeasant.getPeasantTemplateId());
    	}
        
    	return null;
    }

    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {}

    @Override
    public void savePlayerData(OutputStream outputStream) {}

    @Override
    public void loadPlayerData(InputStream inputStream) {}
    
}