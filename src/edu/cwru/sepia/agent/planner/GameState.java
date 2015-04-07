package edu.cwru.sepia.agent.planner;

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
    	this.gCost = 0.0;
    	this.cost = 0.0;
    	//this.resources = stateView.getAllResourceNodes();
    	
    	this.peasants = new HashMap<Integer, Peasant>();
    	this.resources = new ArrayList<ResourceNode>();
    	
    	// Get the peasant units and the townhall
    	for (UnitView unit : units) {
    		String unitType = unit.getTemplateView().getName().toLowerCase();
    		
    		if (unitType.equals("peasant")) {
    			Peasant peasant = new Peasant(unit, this);
    			peasant.setPosition(new Position(unit.getXPosition(), unit.getYPosition()));
    			this.peasants.put(unit.getID(), peasant);
    			//this.peasantPositions.put(unit.getID(), new Position(unit.getXPosition(), unit.getYPosition()));
    		}
    		else if (townhall == null && unitType.equals("townhall"))
    		{
    			this.townhall = unit;
    		}
    	}
    	
    	for (ResourceView r : stateView.getAllResourceNodes()) {    		
    		this.resources.add(new ResourceNode(r.getType(), r.getXPosition(), r.getYPosition(), r.getAmountRemaining(), r.getID()));
    	}
    	
    	//applyAction();
    	//calculateGCost();
    	calculateFunctionalCost();
    }
    
    /**
     * 
     * @param parent
     */
    public GameState(GameState parent, StripsAction actionHistory) {
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
    	this.resources = new ArrayList<ResourceNode>(parent.resources);
    	this.peasants = new HashMap<Integer, Peasant>();
    	this.gCost = Double.MAX_VALUE; //parent.gCost;
    	this.cost = 0.0;
    	
    	for (Peasant peasant : parent.peasants.values()) {
    		this.peasants.put(peasant.getID(), peasant.Clone(peasant));
    	}
    	
    	this.townhall = parent.townhall;
    	    	
    	//System.out.println("Peasant " + peasants.get(1).getID() + " position before: " + peasants.get(1).getPosition().toString());
    	
    	//applyAction();
    	
    	//System.out.println("Peasant " + peasants.get(1).getID() + " position after: " + peasants.get(1).getPosition().toString());
    	
    	//calculateGCost();
    	calculateFunctionalCost();    	
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
    	
    	// Create states for every action for every peasant.
    	for (Peasant peasant : peasants.values()) {    		
			Position townhallPos = getUnitPosition(townhall);
			Position peasantPos = peasant.getPosition();
    		
    		// If the peasant has cargo it should only be concerned with getting back to the townhall to deposit.
    		if (peasant.getCargoAmount() > 0) {    			
    			
    			// The peasant is next to the townhall and should deposit.
    			if (peasantPos.isAdjacent(townhallPos)) {
    				DepositAction depositAction = new DepositAction(peasant.getID(), peasantPos.getDirection(townhallPos), peasant.getCargoType().name().toUpperCase());
    				//children.add(new GameState(this, depositAction));
    				GameState st = new GameState(this, depositAction);
    				children.add(depositAction.apply(st));
    			}
    			// The peasant has cargo and needs to get to the townhall to deposit.
    			else {
    				List<Position> openPositions = townhallPos.getValidAdjacentPositions(stateView, townhall, peasants, resources);
        			
        			for (Position position : openPositions) {    				
        				MoveAction moveAction = new MoveAction(peasant.getID(), position);
        				//children.add(new GameState(this, moveAction));
        				GameState st = new GameState(this, moveAction);
        				children.add(moveAction.apply(st));
        			}
    			}    			
    		}
    		// The peasant does not have cargo and needs to either get to a resource or harvest if it is next to one already.
    		else {
    			for (ResourceNode resource : resources) {
    				System.out.println("Finding options for resource " + getResourcePosition(resource));
    				Position resourcePos = getResourcePosition(resource);
    				
    				if (peasant.getCargoAmount() == 0 && peasantPos.isAdjacent(resourcePos)) {
    					HarvestAction harvestAction = new HarvestAction(peasant.getID(), peasantPos.getDirection(resourcePos), resource.getType().name().toUpperCase(), resource);
//    					children.add(new GameState(this, harvestAction));
    					GameState st = new GameState(this, harvestAction);
    					children.add(harvestAction.apply(st));
    				}
    				// The peasant needs to move adjacent to the resource we still need.
    				else if (needResource(resource)) {
    					List<Position> openPositions = resourcePos.getValidAdjacentPositions(stateView, townhall, peasants, resources);
            			for (Position position : openPositions) { 
            				System.out.println("Position: " + position.toString() + ", parent cost: " + this.cost);
            				MoveAction moveAction = new MoveAction(peasant.getID(), position);
            				//children.add(new GameState(this, moveAction));
            				GameState st = new GameState(this, moveAction);
            				children.add(moveAction.apply(st));
            			}
    				}
    			}
    		}
    	}
    	
        return children;
    }
    
    public boolean needResource(ResourceNode resourceNode) {
    	
    	switch (resourceNode.getResourceType()) {
    	case GOLD:
    		return currentGold < requiredGold;    		
    	case WOOD:
    		return currentWood < requiredWood;
    	}
    	return false;
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
    }
    
    public void addWood(int amount) {
    	currentWood += amount;
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
    	double overallDistance = 0.0;
    	boolean goldGoalReached = currentGold >= requiredGold;
    	boolean woodGoalReached = currentWood >= requiredWood; 
    	
    	for (ResourceNode resource : resources) {
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
			for (Peasant peasant : peasants.values()) {
				Position resourcePos = getResourcePosition(resource);//new Position(resource.getXPosition(), resource.getYPosition());
				Position peasantPos = peasant.getPosition();
				overallDistance += peasantPos.euclideanDistance(resourcePos);
			}
    	}
    	
        return overallDistance / 20;
    }

    /**
     *
     * Write the function that computes the current cost to get to this node. This is combined with your heuristic to
     * determine which actions/states are better to explore.
     *
     * @return The current cost to reach this goal
     */
    public double getCost() {
    	
    	// The GCost hasn't been set.
    	if (this.gCost <= 0) {
    		calculateGCost();
    	}
    	
    	return this.gCost;
    }
    
    /**
     * 
     */
    public void calculateGCost() {
    	double tentativeGCost = 0.0;
    	GameState current = this;
    	
    	while (current.parent != null) {
    		tentativeGCost++;
    		current = current.parent;
    	}
    	this.gCost = tentativeGCost;
    }
    
    public double getFunctionalCost() {
    	calculateFunctionalCost();
    	
    	return this.fCost;
    }
    
    public void calculateFunctionalCost() {
    	this.fCost = getCost() + heuristic();
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
    	//System.out.println("test1");
    	if (this.getCurrentGold() != compare.getCurrentGold() || this.getCurrentWood() != compare.getCurrentWood()) {
    		
    		return false;
    	}
    	//System.out.println("test2");
    	if (this.peasants.size() != compare.peasants.size() || this.resources.size() != compare.resources.size()) {
    		
    		return false;
    	}
    	//System.out.println("test3");
    	// Look at peasant locations and cargo and determine equality.
    	for (Peasant peasant1 : this.peasants.values()) {
    		int numMatches = 0;
    		
    		for (Peasant peasant2 : compare.peasants.values()) {
    			Position p1 = peasant1.getPosition();
    			Position p2 = peasant2.getPosition();
    			
    			// The peasant has an exact match.
    			//System.out.println("p1: " + p1.toString());
    			//System.out.println("p2: " + p2.toString());
    			//System.out.println("p1.equals(p2) = " + p1.equals(p2));
    			//System.out.println("peasant1.getCargoAmount() == peasant2.getCargoAmount() = " + (peasant1.getCargoAmount() == peasant2.getCargoAmount()));
    			
    			//System.out.println("peasant1.getCargoType() != null && peasant2.getCargoType() != null = " + (peasant1.getCargoType() != null && peasant2.getCargoType() != null));
    			
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
    		//System.out.println("numMatches: " + numMatches);
    		// The peasant does not have exactly one match.
    		if (numMatches != 1) {
    			
    			return false;
    		}
    	}
    	//System.out.println("test4");
    	// Look at resource locations, resource types, and amounts remaining to determine equality. 
    	for (ResourceNode resource1 : this.resources) {    		
    		int numMatches = 0;
    		
    		for (ResourceNode resource2 : compare.resources) {
    			Position p1 = getResourcePosition(resource1); //new Position(resource1.getXPosition(), resource1.getYPosition());
    			Position p2 = getResourcePosition(resource2); //new Position(resource2.getXPosition(), resource2.getYPosition());
    			
    			// The resource has an exact match.
    			if (p1.equals(p2) && resource1.getAmountRemaining() == resource2.getAmountRemaining() && resource1.getType().equals(resource2.getType())) {
    				numMatches++;
    			}	
    		}
    		
    		// The resource does not have exactly one match.
    		if (numMatches != 1) {
    			
    			return false;
    		}
    		//System.out.println("test5");
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
        	
//        	// TODO: Remove
//        	System.out.println("\nHashCode peasant position: " + p.getPosition().toString());
        }
        
        for (ResourceNode resource : resources) {
        	hashCode += resource.getAmountRemaining();
        }
        hashCode /= (int)getFunctionalCost();
    	
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
    	builder.append("GameState " + hashCode() + "\n");
    	builder.append("Cost to this state: " + cost + "\n");
    	builder.append("GCost: " + gCost + "\n");
    	builder.append("FCost: " + getFunctionalCost() + "\n");
    	builder.append("Num Peasants: " + peasants.size() + "\n");
    	builder.append("Num Resources: " + resources.size() + "\n");
    	builder.append("Current Gold: " + getCurrentGold() + "\n");
    	builder.append("Current Wood: " + getCurrentWood() + "\n");
    	
    	if (actionHistory != null) {
    		builder.append("Action History: " + actionHistory.toString() + "\n");
    	}    	
    	
    	return builder.toString();
    }
    
}