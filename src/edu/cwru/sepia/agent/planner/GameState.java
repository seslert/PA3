package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.DepositAction;
import edu.cwru.sepia.agent.planner.actions.HarvestAction;
import edu.cwru.sepia.agent.planner.actions.MoveAction;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
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
	
	private List<StripsAction> actionHistory;
	
	private GameState parent;
	
	public List<UnitView> units;
	public List<ResourceView> resources;
	public List<UnitView> peasants;
	public UnitView townhall;
	
	public double fCost, gCost, hCost;
	
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
     * nodes should be constructed from another constructor you create or by factory functions that you create.
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
    	
    	this.peasants = new ArrayList<UnitView>();
    	this.resources = new ArrayList<ResourceView>();
    	
    	// Get the peasant units and the townhall
    	for (UnitView unit : units) {
    		String unitType = unit.getTemplateView().getName().toLowerCase();
    		
    		if (unitType.equals("peasant")) {
    			this.peasants.add(unit);
    		}
    		else if (townhall == null && unitType.equals("townhall"))
    		{
    			this.townhall = unit;
    		}
    	}
    	
    	calculateFunctionalCost();
    }
    
    /**
     * 
     * @param parent
     */
    public GameState(GameState parent, List<StripsAction> actionHistory) {
    	this.stateView = parent.stateView;
    	this.requiredGold = parent.requiredGold;
    	this.requiredWood = parent.requiredWood;
    	this.buildPeasants = parent.buildPeasants;
    	
    	this.xExtent = this.stateView.getXExtent();
    	this.yExtent = this.stateView.getYExtent();
    	this.units = parent.units;
    	this.resources = parent.resources;
    	this.peasants = parent.peasants;
    	this.townhall = parent.townhall;
    	
    	calculateFunctionalCost();
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
        
    	List<GameState> children = new ArrayList<GameState>();
    	
    	// Create states for every action for every peasant.
    	for (UnitView peasant : peasants) {
    		List<StripsAction> generationActions = new ArrayList<StripsAction>();
			Position townhallPos = getUnitPosition(townhall);
			Position peasantPos = getUnitPosition(peasant);
    		
    		// If the peasant has cargo it should only be concerned with getting back to the townhall to deposit.
    		if (peasant.getCargoAmount() > 0) {
    			
    			// The peasant is next to the townhall and should deposit.
    			if (peasantPos.isAdjacent(townhallPos)) {
    				generationActions.add(new DepositAction(peasant.getID(), peasantPos.getDirection(townhallPos)));
    				children.add(new GameState(this, generationActions));
    			}
    			// The peasant has cargo and needs to get to the townhall to deposit.
    			else {
    				List<Position> openPositions = townhallPos.getValidAdjacentPositions(units, resources);
        			
        			for (Position position : openPositions) {    				
        				generationActions.add(new MoveAction(peasant.getID(), position));
        				children.add(new GameState(this, generationActions));
        			}
    			}    			
    		}
    		// The peasant does not have cargo and needs to either get to a resource or gather if it is next to one already.
    		else {
    			for (ResourceView resource : resources) {
    				Position resourcePos = getResourcePosition(resource);
    				
    				if (peasantPos.isAdjacent(resourcePos)) {
    					generationActions.add(new HarvestAction(peasant.getID(), peasantPos.getDirection(resourcePos)));
    					children.add(new GameState(this, generationActions));
    				}
    				// The peasant needs to move adjacent to the resource.
    				else {
    					List<Position> openPositions = resourcePos.getValidAdjacentPositions(units, resources);
            			
            			for (Position position : openPositions) {    				
            				generationActions.add(new MoveAction(peasant.getID(), position));
            				children.add(new GameState(this, generationActions));
            			}
    				}
    			}
    		}
    	}
    	
        return children;
    }
    
    /**
     * Get a Position abstraction of a unit's x and y coordinates.
     * @param unit
     * @return
     */
    private Position getUnitPosition(UnitView unit) {
    	
    	return new Position(unit.getXPosition(), unit.getYPosition());
    }
    
    /**
     * Get a Position abstraction of a resource's x and y coordinates.
     * @param resource
     * @return
     */
    private Position getResourcePosition(ResourceView resource) {
    	
    	return new Position(resource.getXPosition(), resource.getYPosition());
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

    	//double lowestDistance = Double.MAX_VALUE;
    	double overallDistance = 0.0;
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
    		//Position townhallPos = new Position(this.townhall.getXPosition(), this.townhall.getYPosition());
			//Position resourcePos = new Position(resource.getXPosition(), resource.getYPosition());
			//double currentDistance = townhallPos.euclideanDistance(resourcePos);
			
			
			for (UnitView peasant : peasants) {
				Position resourcePos = new Position(resource.getXPosition(), resource.getYPosition());
				Position peasantPos = new Position(peasant.getXPosition(), peasant.getYPosition());
				
				overallDistance += peasantPos.euclideanDistance(resourcePos);
			}			
			
//			if (currentDistance < lowestDistance) {
//				lowestDistance = currentDistance;
//    		}
    	}
    	
        return overallDistance;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
    	
    	// The GCost has already been set.
    	if (this.gCost > 0) {
    		
    		return this.gCost;
    	}

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
    	calculateFunctionalCost();
    	
    	return this.fCost;
    }
    
    public void calculateFunctionalCost() {
    	this.fCost = getCost() + heuristic();
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