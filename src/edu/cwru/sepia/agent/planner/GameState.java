package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.agent.planner.actions.BuildPeasant;
import edu.cwru.sepia.agent.planner.actions.DepositAction;
import edu.cwru.sepia.agent.planner.actions.HarvestAction;
import edu.cwru.sepia.agent.planner.actions.MoveAction;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public int depth;
	
	public State.StateView stateView;
	public StripsAction actionHistory;
	public GameState parent;
	public GameState astarParent;
	public List<UnitView> units;
	public List<ResourceNode> resources;
	public Map<Integer, Peasant> peasants;
	public UnitView townhall;
	public double fCost, gCost, hCost, cost;
	
	private int playernum;
	private int requiredGold;
	private int currentGold;
	private int grossGold;
	private int requiredWood;
	private int grossWood;
	private int food;
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

    	this.playernum = playernum;
    	this.stateView = state; 
    	this.requiredGold = requiredGold;
    	this.requiredWood = requiredWood;
    	this.buildPeasants = buildPeasants;
    	this.xExtent = stateView.getXExtent();
    	this.yExtent = stateView.getYExtent();
    	this.units = stateView.getAllUnits();
    	this.gCost = 0.0;
    	this.cost = 0.0;
    	this.fCost = Double.MAX_VALUE;    	
    	this.peasants = new HashMap<Integer, Peasant>();
    	this.resources = new ArrayList<ResourceNode>();
    	
    	// Find and bind all units and resources in the state.
    	discoverUnits(state);
    	discoverResources(state);
    	
    	this.food = stateView.getSupplyCap(townhall.getID()) - 1;
    	this.grossGold = 0;
    	this.grossWood = 0;
    }
    
    /**
     * 
     * @param parent
     */
    public GameState(GameState parent, StripsAction actionHistory) {
    	
    	this.depth = parent.depth + 1;
    	
    	this.playernum = parent.playernum;
    	this.actionHistory = actionHistory;
    	this.parent = parent;
    	this.stateView = parent.stateView;
    	this.requiredGold = parent.requiredGold;
    	this.requiredWood = parent.requiredWood;
    	this.currentGold = parent.currentGold;
    	this.currentWood = parent.currentWood;
    	this.buildPeasants = parent.buildPeasants;
    	this.xExtent = this.stateView.getXExtent();
    	this.yExtent = this.stateView.getYExtent();
    	this.units = parent.units;
    	this.resources = new ArrayList<ResourceNode>();
    	this.peasants = new HashMap<Integer, Peasant>();
    	this.gCost = Double.MAX_VALUE;
    	this.cost = 0.0;
    	this.fCost = Double.MAX_VALUE;
    	
    	for (Peasant peasant : parent.peasants.values()) {
    		this.peasants.put(peasant.getID(), peasant.Clone(peasant));
    	}
    	
    	for (ResourceNode resource : parent.resources) {
    		this.resources.add(cloneResource(resource));
    	}
    	
    	this.townhall = parent.townhall;	
    	
    	this.food = parent.food;
    	this.grossGold = parent.grossGold;
    	this.grossWood = parent.grossWood;
    }
    
    public ResourceNode cloneResource(ResourceNode resource) {
    	return new ResourceNode(resource.getType(), resource.getxPosition(), resource.getyPosition(), resource.getAmountRemaining(), resource.ID);
    }
    
    private void discoverUnits(State.StateView s) {
    	
    	for (UnitView unit : s.getAllUnits()) {
    		String unitType = unit.getTemplateView().getName().toLowerCase();
    		
    		if (unitType.equals("peasant")) {
    			Peasant peasant = new Peasant(unit);
    			peasant.setPosition(new Position(unit.getXPosition(), unit.getYPosition()));
    			this.peasants.put(unit.getID(), peasant);
    		}
    		else if (townhall == null && unitType.equals("townhall"))
    		{
    			this.townhall = unit;
    		}
    	}
    }
    
    /**
     * Determines all remaining resources in the state.
     * @param s
     */
    private void discoverResources(State.StateView s) {
    	
    	for (ResourceView r : s.getAllResourceNodes()) {    		
    		this.resources.add(new ResourceNode(r.getType(), r.getXPosition(), r.getYPosition(), r.getAmountRemaining(), r.getID()));
    	}
    }
    
    /**
     * Sets the parent pointer for this GameState.
     * @param parent
     */
    public void setAstarParent(GameState parent) {
    	this.astarParent = parent;
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
    	
    	// Generate BuildPeasant state if preconditions met
    	if (buildPeasants && currentGold >= 400 && food > 0) {
    		BuildPeasant buildPeasant = new BuildPeasant(townhall.getID());
    		GameState st = new GameState(this, buildPeasant);
			children.add(buildPeasant.apply(st));
    	}
    	
    	// Create states for every action for every peasant.
    	for (Peasant peasant : peasants.values()) {    		
			Position townhallPos = getUnitPosition(townhall);
			Position peasantPos = peasant.getPosition();
    		
    		// If the peasant has cargo it should only be concerned with getting back to the townhall to deposit.
    		if (peasant.getCargoAmount() > 0) {    			
    			
    			// The peasant is next to the townhall and should deposit.
    			if (peasantPos.isAdjacent(townhallPos)) {
    				DepositAction depositAction = new DepositAction(peasant.getID(), peasantPos.getDirection(townhallPos), peasant.getCargoType().name().toUpperCase());
    				GameState st = new GameState(this, depositAction);
    				children.add(depositAction.apply(st));
    			}
    			// The peasant has cargo and needs to get to the townhall to deposit.
    			else {
    				List<Position> openPositions = townhallPos.getValidAdjacentPositions(stateView, townhall, peasants, resources);
        			
        			for (Position position : openPositions) {    				
        				MoveAction moveAction = new MoveAction(peasant.getID(), position);
        				GameState st = new GameState(this, moveAction);
        				children.add(moveAction.apply(st));
        			}
    			}    			
    		}
    		// The peasant does not have cargo and needs to either get to a resource or harvest if it is next to one already.
    		else {
    			for (ResourceNode resource : resources) {
    				Position resourcePos = getResourcePosition(resource);
    				
    				if (peasant.getCargoAmount() == 0 && peasantPos.isAdjacent(resourcePos)) {
    					System.out.println("before harvest: " + resource.getAmountRemaining());
    					HarvestAction harvestAction = new HarvestAction(peasant.getID(), peasantPos.getDirection(resourcePos), resource.getType().name().toUpperCase(), resource.ID);
    					GameState st = new GameState(this, harvestAction);
    					children.add(harvestAction.apply(st));
    					System.out.println("after harvest: " + resource.getAmountRemaining());
    				}
    				// The peasant needs to move adjacent to the resource we still need.
    				else if (needResource(resource)) {
    					List<Position> openPositions = resourcePos.getValidAdjacentPositions(stateView, townhall, peasants, resources);
            			for (Position position : openPositions) { 
            				MoveAction moveAction = new MoveAction(peasant.getID(), position);
            				GameState st = new GameState(this, moveAction);
            				children.add(moveAction.apply(st));
            			}
    				}
    			}
    		}
    	}
    	
        return children;
    }
    
    /**
     * Determines if this state still needs a given resource.
     * @param resourceNode
     * @return
     */
    public boolean needResource(ResourceNode resourceNode) {
    	
    	switch (resourceNode.getResourceType()) {
    	case GOLD:
    		return currentGold < requiredGold;    		
    	case WOOD:
    		return currentWood < requiredWood;
    	}
    	return true;
    }
    
    /**
     * Get a Position abstraction of a unit's x and y coordinates.
     * @param unit
     * @return
     */
    public Position getUnitPosition(UnitView unit) {
    	    	
    	return new Position(unit.getXPosition(), unit.getYPosition());
    }
    
    /**
     * Get a Position abstraction of a resource's x and y coordinates.
     * @param resource
     * @return
     */
    public Position getResourcePosition(ResourceNode resource) {
    	
    	return new Position(resource.getxPosition(), resource.getyPosition());
    }
    
    public void addGold(int amount) {
    	currentGold += amount;
    	grossGold += amount;
    }
    
    public void removeGold(int amount) {
    	currentGold -= amount;
    }
    
    public void addWood(int amount) {
    	currentWood += amount;
    	grossWood += amount;
    }
    
    public int getGold() {
    	return this.currentGold;
    }
    
    public int getPlayernum() {
    	return this.playernum;
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
    	double overallH = 0.0;
    	
    	for (Peasant peasant : peasants.values()) {
    		
    		// peasant needs to deposit resource in townHall
    		if (peasant.getCargoAmount() > 0 && actionHistory instanceof DepositAction) {
    			overallH += 0;
    		}
    		
    		// peasant needs to move to townHall to be able to deposit resources
    		if (peasant.getCargoAmount() > 0 && actionHistory instanceof MoveAction) {
    			overallH += 25;
    		}
    		
    		// peasant needs to harvest resource
    		if (peasant.getCargoAmount() == 0 && actionHistory instanceof HarvestAction) {
    			overallH += 50;
    		}
    		
    		// Peasant needs to move to a resource to be able to harvest it
    		if (peasant.getCargoAmount() == 0 && actionHistory instanceof MoveAction) {
    			overallH += 300;
    		}
    	}
    	
    	// Encourage states that build peasants
    	if (!(this.actionHistory instanceof BuildPeasant)) {
    		overallH *= 10;
    	}
    	
    	// Encourage states that have gathered more resources overall.
    	// Works with arbitrary resource requirements.
    	overallH += 2.5 * (requiredWood + requiredGold + 400 * (2 - food)) - 2.5 * (grossWood + grossGold);
    	
    	// Encourage states that have multiple peasants (assuming that the maximum number of peasants is 3)
    	overallH *= Math.pow(2, 3 - peasants.size()) ;

        return overallH / peasants.size();
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getGCost() {
    	
    	return this.gCost;
    }
     
    public double getFunctionalCost() {
    	calculateFunctionalCost();
    	
    	return this.fCost;
    }
    
    public void calculateFunctionalCost() {
    	this.fCost = getGCost() + heuristic();
    }
    
    public void reduceFood(int reduce) {
    	this.food -= reduce;
    }
    
    public int getLargestPeasantId() {
    	
    	int largest = 0;
    	for (Peasant peasant : peasants.values()) {
    		if (peasant.getID() > largest) {
    			largest = peasant.getID();
    		}
    	}
    	
    	return largest;
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
    	
    	if (this.getFunctionalCost() < o.getFunctionalCost()) {
    		
    		return -1;
    	}
    	
        return 0;
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
    	for (Peasant peasant1 : this.peasants.values()) {
    		int numMatches = 0;
    		
    		for (Peasant peasant2 : compare.peasants.values()) {
    			Position p1 = peasant1.getPosition();
    			Position p2 = peasant2.getPosition();
    			
    			// The peasant has an exact match.
    			if (p1.equals(p2) && peasant1.getCargoAmount() == peasant2.getCargoAmount()) {
    				
    				if (peasant1.getCargoType() != null && peasant2.getCargoType() != null)
    				{
    					if (peasant1.getCargoType().equals(peasant2.getCargoType())) {    				
        					numMatches++;
        				}
    				}
    				else if (peasant1.getCargoType() == null && peasant2.getCargoType() == null) {
    					numMatches++;
    				}
    			}	
    		}
    		// The peasant does not have exactly one match.
    		if (numMatches != 1) {
    			
    			return false;
    		}
    	}
    	// Look at resource locations, resource types, and amounts remaining to determine equality. 
    	for (ResourceNode resource1 : this.resources) {    		
    		int numMatches = 0;
    		
    		for (ResourceNode resource2 : compare.resources) {
    			Position p1 = getResourcePosition(resource1);
    			Position p2 = getResourcePosition(resource2);
    			
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
        
        for (Peasant p : peasants.values()) {
        	hashCode += p.getXPosition();
        	hashCode += p.getYPosition();
        }
        
        for (ResourceNode resource : resources) {
        	hashCode += resource.getAmountRemaining();
        }
        hashCode += ((int)getFunctionalCost() + 1);
    	
        return hashCode;
    }
    
    public int getCurrentGold() {
    	
    	return this.currentGold;
    }
    
    public int getCurrentWood() {
    	
    	return this.currentWood;
    }
    
    @Override
    public String toString() {
    	StringBuilder builder = new StringBuilder();
    	builder.append("GameState " + hashCode() + ", depth " + depth + "\n");
    	builder.append("Cost to this state: " + cost + "\n");
    	builder.append("GCost: " + gCost + "\n");
    	builder.append("FCost: " + getFunctionalCost() + "\n");
    	builder.append("Current Gold: " + getCurrentGold() + " | Current Wood: " + getCurrentWood() + "\n");
    	builder.append("Gross Gold: " + grossGold + " | Gross Wood: " + grossWood + "\n");
    	builder.append("Active Peasants: " + peasants.size() + " | Remaining Resources: " + resources.size() + "\n");
    	
    	if (actionHistory != null) {
    		builder.append("Action History: " + actionHistory.toString() + "\n");
    	}    	
    	
    	return builder.toString();
    }
    
}