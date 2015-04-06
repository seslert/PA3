package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to represent the state of the game after applying one of the available actions. It will also
 * track the A* specific information such as the parent pointer and the cost and heuristic function. Remember that
 * unlike the path planning A* from the first assignment the cost of an action may be more than 1. Specifically the cost
 * of executing a compound action such as move can be more than 1. You will need to account for this in your heuristic
 * and your cost function.
 *
 * The first instance is constructed from the StateView object (like in PA2). Implement the methods provided and
 * add any other methods and member variables you need.
 *
 * Some useful API calls for the state view are
 *
 * state.getXExtent() and state.getYExtent() to get the map size
 *
 * I recommend storing the actions that generated the instance of the GameState in this class using whatever
 * class/structure you use to represent actions.
 */
public class GameState implements Comparable<GameState> {
	
	private State.StateView stateView;
	
	private GameState parent;
	
	public List<UnitView> units;
	public List<ResourceView> resources;
	public List<UnitView> peasants;
	public UnitView townhall;
	
	private double fCost, gCost, hCost;
	
	private int playernum;
	private int requiredGold;
	private int currentGold;
	private int requiredWood;
	private int currentWood;
	private int xExtent;
	private int yExtent;
		
	
	private boolean buildPeasants;

    /**
     * Construct a GameState from a stateview object. This is used to construct the initial search node. All other
     * nodes should be constructed from the another constructor you create or by factory functions that you create.
     *
     * @param state The current stateview at the time the plan is being created
     * @param playernum The player number of agent that is planning
     * @param requiredGold The goal amount of gold (e.g. 200 for the small scenario)
     * @param requiredWood The goal amount of wood (e.g. 200 for the small scenario)
     * @param buildPeasants True if the BuildPeasant action should be considered
     */
    public GameState(State.StateView state, int playernum, int requiredGold, int requiredWood, boolean buildPeasants) {
        
    	this.stateView = state; 
    	this.requiredGold = requiredGold;
    	this.requiredWood = requiredWood;
    	this.buildPeasants = buildPeasants;
    	this.xExtent = stateView.getXExtent();
    	this.yExtent = stateView.getYExtent();
    	this.units = stateView.getAllUnits();
    	this.resources = stateView.getAllResourceNodes();
    	
    	// Get the peasant units and the townhall
    	for (UnitView unit : units) {
    		String unitType = unit.getTemplateView().getName().toLowerCase();
    		
    		if (unit.getTemplateView().getName().equals("Peasant")) {
    			this.peasants.add(unit);
    		}
    		else if (townhall == null && unit.getTemplateView().getName().equals("Townhall"))
    		{
    			this.townhall = unit;
    		}
    	}
    	
    	// Calculate the functional cost of the state.
    	this.fCost = getCost() + heuristic();
    }
    
    /**
     * Sets the parent pointer for this GameState.
     * @param parent
     */
    public void setParentState(GameState parent) {
    	this.parent = parent;
    }

    /**
     * Unlike in the first A* assignment there are many possible goal states. As long as the wood and gold requirements
     * are met the peasants can be at any location and the capacities of the resource locations can be anything. Use
     * this function to check if the goal conditions are met and return true if they are.
     *
     * @return true if the goal conditions are met in this instance of game state.
     */
    public boolean isGoal() {
        
    	return currentWood >= requiredWood && currentGold >= requiredGold;
    }

    /**
     * The branching factor of this search graph are much higher than the planning. Generate all of the possible
     * successor states and their associated actions in this method.
     *
     * @return A list of the possible successor states and their associated actions
     */
    public List<GameState> generateChildren() {
        // TODO: Implement me!
    	List<GameState> children = new ArrayList<GameState>();
    	
        return children;
    }

    /**
     * Write your heuristic function here. Remember this must be admissible for the properties of A* to hold. If you
     * can come up with an easy way of computing a consistent heuristic that is even better, but not strictly necessary.
     *
     * Add a description here in your submission explaining your heuristic.
     *
     * @return The value estimated remaining cost to reach a goal state from this state.
     */
    public double heuristic() {

    	double lowestDistance = Double.MAX_VALUE;
    	boolean goldGoalReached = currentGold >= requiredGold;
    	boolean woodGoalReached = currentWood >= requiredWood; 
    	
    	for (ResourceView resource : resources) {
    		String resourceType = resource.getType().name().toLowerCase();
    		
    		// We already have enough wood.  Move to the next resource.
    		if (woodGoalReached && resourceType.equals("wood")) {
    			break;
    		}
    		
    		// We already have enough gold.  Move to the next resource.
    		if (goldGoalReached && resourceType.equals("gold")) {
    			break;
    		}
    		
    		// Determine the distance to a resource that we actually need.
    		Position townhallPos = new Position(this.townhall.getXPosition(), this.townhall.getYPosition());
			Position resourcePos = new Position(resource.getXPosition(), resource.getYPosition());
			double currentDistance = townhallPos.euclideanDistance(resourcePos);
			
			if (currentDistance < lowestDistance) {
				lowestDistance = currentDistance;
    		}
    	}
    	
        return lowestDistance * 10;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {

    	double tentativeGCost = 0.0;
    	GameState current = this;
    	
    	while (current.parent != null) {
    		tentativeGCost++;
    		current = current.parent;
    	}
    	this.gCost = tentativeGCost;
    	
        return tentativeGCost;
    }
    
    public double getFunctionalCost() {
    	
    	if (fCost > 0) {
    		
    		return this.fCost;
    	}
    	else {
    		
    		return getCost() + heuristic();
    	}    	
    }
    
    /**
     * Helper method to quickly access all peasants in the state.
     * @return
     */
    public List<UnitView> getPeasants() {
    	
    	return stateView.getAllUnits();    	
    }

    /**
     * This is necessary to use your state in the Java priority queue. See the official priority queue and Comparable
     * interface documentation to learn how this function should work.
     *
     * @param o The other game state to compare
     * @return 1 if this state costs more than the other, 0 if equal, -1 otherwise
     */
    @Override
    public int compareTo(GameState o) {
        
    	if (this.getFunctionalCost() > o.getFunctionalCost()) {
    		
    		return 1;
    	}
    	
    	if (this.getFunctionalCost() == o.getFunctionalCost()) {
    		
    		return 0;
    	}
    	
        return -1;
    }

    /**
     * This will be necessary to use the GameState as a key in a Set or Map.
     *
     * @param o The game state to compare
     * @return True if this state equals the other state, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
    	GameState compare = (GameState) o;
    	
    	if (this.getCurrentGold() != compare.getCurrentGold() || this.getCurrentWood() != compare.getCurrentWood()) {
    		
    		return false;
    	}
    	
    	if (this.peasants.size() != compare.peasants.size() || this.resources.size() != compare.resources.size()) {
    		
    		return false;
    	}
    	
    	// Look at peasant locations and cargo and determine equality.
    	for (UnitView peasant1 : this.peasants) {
    		int numMatches = 0;
    		
    		for (UnitView peasant2 : compare.peasants) {
    			Position p1 = new Position(peasant1.getXPosition(), peasant1.getYPosition());
    			Position p2 = new Position(peasant2.getXPosition(), peasant2.getYPosition());
    			
    			// The peasant has an exact match.
    			if (p1.equals(p2) && peasant1.getCargoAmount() == peasant2.getCargoAmount() && peasant1.getCargoType().equals(peasant2.getCargoType())) {
    				
    				numMatches++;
    			}	
    		}
    		
    		// The peasant does not have exactly one match.
    		if (numMatches != 1) {
    			
    			return false;
    		}
    	}
    	
    	// Look at resource locations, resource types, and amounts remaining to determine equality. 
    	for (ResourceView resource1 : this.resources) {    		
    		int numMatches = 0;
    		
    		for (ResourceView resource2 : compare.resources) {
    			Position p1 = new Position(resource1.getXPosition(), resource1.getYPosition());
    			Position p2 = new Position(resource2.getXPosition(), resource2.getYPosition());
    			
    			// The resource has an exact match.
    			if (p1.equals(p2) && resource1.getAmountRemaining() == resource2.getAmountRemaining() && resource1.getType().equals(resource2.getType())) {
    				
    				numMatches++;
    			}	
    		}
    		
    		// The resource does not have exactly one match.
    		if (numMatches != 1) {
    			
    			return false;
    		}
    	}
    	
    	return true;
    }

    /**
     * This is necessary to use the GameState as a key in a HashSet or HashMap. Remember that if two objects are
     * equal they should hash to the same value.
     *
     * @return An integer hashcode that is equal for equal states.
     */
    @Override
    public int hashCode() {
        int hashCode = 0;
        
        if (buildPeasants) {
        	hashCode++;
        }
        hashCode += (currentGold * playernum);
        hashCode += (currentWood * playernum);
        hashCode *= peasants.size();
        hashCode *= resources.size();
        
        for (ResourceView resource : resources) {
        	
        	hashCode += resource.getAmountRemaining();
        }
    	
        return hashCode;
    }
    
    public int getCurrentGold() {
    	
    	return this.currentGold;
    }
    
    public int getCurrentWood() {
    	
    	return this.currentWood;
    }
    
}