package edu.cwru.sepia.agent.planner;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.agent.planner.actions.StripsAction;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.*;
import java.util.*;

/**
 * Created by Devin on 3/15/15.
 * 
 * @author Adam Boe
 * @author Tim Sesler
 * @date 27 March 2015
 */
public class PlannerAgent extends Agent {

    final int requiredWood;
    final int requiredGold;
    final boolean buildPeasants;

    // Your PEAgent implementation. This prevents you from having to parse the text file representation of your plan.
    PEAgent peAgent;

    public PlannerAgent(int playernum, String[] params) {
        super(playernum);

        if(params.length < 3) {
            System.err.println("You must specify the required wood and gold amounts and whether peasants should be built");
        }

        requiredWood = Integer.parseInt(params[0]);
        requiredGold = Integer.parseInt(params[1]);
        buildPeasants = Boolean.parseBoolean(params[2]);


        System.out.println("required wood: " + requiredWood + " required gold: " + requiredGold + " build Peasants: " + buildPeasants);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView stateView, History.HistoryView historyView) {

        Stack<StripsAction> plan = AstarSearch(new GameState(stateView, playernum, requiredGold, requiredWood, buildPeasants));

        if (plan == null) {
            System.err.println("No plan was found");
            System.exit(1);
            return null;
        }

        // write the plan to a text file
        System.out.println("Writing...");
        savePlan(plan);


        // Instantiates the PEAgent with the specified plan.
        peAgent = new PEAgent(playernum, plan);

        return peAgent.initialStep(stateView, historyView);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView stateView, History.HistoryView historyView) {
        if(peAgent == null) {
            System.err.println("Planning failed. No PEAgent initialized.");
            return null;
        }

        return peAgent.middleStep(stateView, historyView);
    }

    @Override
    public void terminalStep(State.StateView stateView, History.HistoryView historyView) {}

    @Override
    public void savePlayerData(OutputStream outputStream) {}

    @Override
    public void loadPlayerData(InputStream inputStream) {}

    /**
     * Perform an A* search of the game graph. This should return your plan as a stack of actions. This is essentially
     * the same as your first assignment. The implementations should be very similar. The difference being that your
     * nodes are now GameState objects not MapLocation objects.
     *
     * @param startState The state which is being planned from
     * @return The plan or null if no plan is found.
     */
    private Stack<StripsAction> AstarSearch(GameState startState) {
        
    	Stack<StripsAction> actionPlan = new Stack<StripsAction>();
    	PriorityQueue<GameState> openSet = new PriorityQueue<GameState>();
    	Set<GameState> closedSet = new HashSet<GameState>();
    	GameState current = startState;
    	GameState previous = null;
    	
    	openSet.add(current);
    	while (!openSet.isEmpty()) {
//    		if (current.depth == 11) {
//    			try {
//    				Thread.sleep(5000);
//    			} catch (InterruptedException e) {
//    				// TODO Auto-generated catch block
//    				e.printStackTrace();
//    			}
//    		}
//    		try {
//				Thread.sleep(200);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
    		
    		current = GetLowestFcost(openSet);
    		openSet.remove(current);
    		
			// TODO: remove
        	System.out.println("\n\nCURRENT State: " + current.toString());
    		
    		if (current.isGoal()) {
    			System.out.println("WE FOUND A PATH!!!");    			
    			//current.setAstarParent(previous);
    			return reconstructActionPlan(current);
    		}
    		openSet.remove(current);
    		closedSet.add(current);    		
    		List<GameState> children = current.generateChildren();
    		Iterator<GameState> i = children.iterator();
    		
    		//System.out.println(current.hashCode() + " not a goal state, expanding " + children.size() + " children");
    		int pos = 0;
			while (i.hasNext()) {
				pos++;
				GameState child = i.next();			
				
				// TODO: remove
	        	//System.out.println("CHILD " + pos + "/" + children.size() + " " + child.actionHistory.toString());
				
				if (!closedSet.contains(child)) {
					double newGCost = current.getGCost() + child.cost;
					//System.out.println("newGCost: " + newGCost + " vs child.gCost: " + child.getGCost());
					if (newGCost < child.getGCost()) {
						
						//System.out.println("Added parent in Astar path.");
						
						child.gCost = newGCost;
						child.calculateFunctionalCost();
						//System.out.println("newFCost for child " + child.hashCode() + " " + child.fCost);
						child.setAstarParent(current);
					}
					
					if (!openSet.contains(child)) {
						//System.out.println("child " + pos + "/" + children.size() + " fCost " + child.fCost);
						openSet.add(child);
						//System.out.println("testing hash after an add: " + child.hashCode());
					}
				}
			}
    	}
    	System.out.println("FAILURE: No available path found.");
    	
        return new Stack<StripsAction>();
    }
    
    /**
     * Rebuilds the action plan from the final GameState chosen through A*.
     * @param finalState
     * @return
     */
    private Stack<StripsAction> reconstructActionPlan(GameState finalState) {
    	Stack<StripsAction> reverseActionPlan = new Stack<StripsAction>();
    	Stack<StripsAction> actionPlan = new Stack<StripsAction>();
    	
    	GameState current = finalState;    	
    	
    	while (current.astarParent != null) {
    		
    		reverseActionPlan.push(current.actionHistory);    		
    		current = current.astarParent;
    	}
    	
//    	while (!reverseActionPlan.isEmpty()) {
//    		actionPlan.push(reverseActionPlan.pop());
//    	}
    	
    	return reverseActionPlan;
    }

    /**
     * This has been provided for you. Each strips action is converted to a string with the toString method. This means
     * each class implementing the StripsAction interface should override toString. Your strips actions should have a
     * form matching your included Strips definition writeup. That is <action name>(<param1>, ...). So for instance the
     * move action might have the form of Move(peasantID, X, Y) and when grounded and written to the file
     * Move(1, 10, 15).
     *
     * @param plan Stack of Strips Actions that are written to the text file.
     */
    private void savePlan(Stack<StripsAction> plan) {
        if (plan == null) {
            System.err.println("Cannot save null plan");
            return;
        }

        File outputDir = new File("saves");
        outputDir.mkdirs();

        File outputFile = new File(outputDir, "plan.txt");

        PrintWriter outputWriter = null;
        try {
            outputFile.createNewFile();

            outputWriter = new PrintWriter(outputFile.getAbsolutePath());

            Stack<StripsAction> tempPlan = (Stack<StripsAction>) plan.clone();
            while(!tempPlan.isEmpty()) {
                outputWriter.println(tempPlan.pop().toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputWriter != null)
                outputWriter.close();
        }
    }
    
    private GameState GetLowestFcost(PriorityQueue<GameState> gameStates) {
     
    	GameState lowestFcost = null;
    	if (gameStates != null) {
    		
	    	lowestFcost = gameStates.element();
	    	
	    	for (GameState state : gameStates) {
	    		if (state.fCost < lowestFcost.fCost) {
	    			lowestFcost = state;
	    		}
	    	}
    	}
    	return lowestFcost;
    }
}
