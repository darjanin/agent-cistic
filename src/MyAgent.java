import java.util.HashSet;
import java.util.LinkedList;

public  class MyAgent extends Agent{	
	int[][] net;
	LinkedList<String> path = new LinkedList<String>();
	
	// skyNet is my map where would be done path-finding to DIRTY or UNKNOWN squares
	int[][] skyNet;
	// height and width of the skyNet
	int height, width;
	// activeState is for handling position of the agent (terminator)
	State activeState;
	// constant for UNKOWN squares in skyNet
	static final int UNKNOWN = 7;
		
	public MyAgent(int height, int width) {
		// initialize the skyNet with dimension 4 times larger than input map with UNKNOWN squares
		// save this values into height and width
		this.height = height*4+1;
		this.width = width*4+1;
		skyNet = new int[this.height][this.width];
		for (int i = 0; i < this.height; i++) {
			for (int j = 0; j < this.width; j++) {
				skyNet[i][j] = UNKNOWN;
			}
		}
		// set the position and orientation of the agent in centre of the skyNet looking at the north
		activeState = new State(this.height/2, this.width/2, World.NORTH, null); 
	}
	
	/**
	 * it locates prey and gives orders how to move to it's position
	 * returns true and sets the path variable if finds DIRTY or UNKNOWN square
	 * return false if there are no accessible DIRTY or UNKNOWN squares 
	 **/
	private boolean findTarget() {
		// open is list where are saved states for visit
		LinkedList<State> open = new LinkedList<State>();
		// close is set containing visited states
		HashSet<State> close = new HashSet<State>();
		State state, newState;
		
		// add to open the active state information from which the movement starts
		open.add(activeState);
		
		while(!open.isEmpty()) {
			
			state = open.pop();
			
			if (!close.contains(state)) {
				close.add(state);
				
				// if agent stands on DIRTY or UNKNOWN square, generate path to it from activeState
				if (skyNet[state.x][state.y] == DIRTY || skyNet[state.x][state.y] == UNKNOWN) {
					path = state.path();
					return true;
				}
				
				// base on orientation add new state that will move the agent forward
				switch (state.orientation) {
				case World.NORTH:
					newState = new State(state.x - 1, state.y, state.orientation, state);
					break;
				case World.SOUTH:
					newState = new State(state.x + 1, state.y, state.orientation, state);
					break;
				case World.WEST:
					newState = new State(state.x, state.y - 1, state.orientation, state);
					break;
				case World.EAST:
					newState = new State(state.x, state.y + 1, state.orientation, state);
					break;
				default:
					newState = null;
				}
				
				// if newState is not in closed states and it's not wall then add it to the open
				if (!close.contains(newState) && skyNet[newState.x][newState.y] != WALL) {
					open.add(newState);
				}
				
				// rotate to left on the same position
				if (state.orientation == World.NORTH)
					newState = new State(state.x, state.y, World.WEST , state);
				else
					newState = new State(state.x, state.y, state.orientation - 1, state);
				if (!close.contains(newState))
					open.add(newState);
				
				// rotate to the right on the same position
				if (state.orientation == World.WEST)
					newState = new State(state.x, state.y, World.NORTH, state);
				else
					newState = new State(state.x, state.y, state.orientation + 1, state);
				if (!close.contains(newState))
					open.add(newState);
				
			}
			
		}
		
		// if open is empty and the locator of DIRTY or UNKNOWN square doesn't return true return false
		// and the agent movement will be halted
		return false;
		
	}

	/**
	 * update squares in skyNet with values that were percepted by the agent
	 */
	private void updateSkyNet() {
		
		// perceptSize says the distance in which the agent can see
		int viewDistance = getPerceptSize();
		// load information about the map around the agent 
		net = percept();
		
		// save loaded information into skyNet
		for (int x = -viewDistance; x <= viewDistance; x++) {
			for (int y = -viewDistance; y <= viewDistance; y++) {
				skyNet[activeState.x + x][activeState.y + y] = net[viewDistance + x][viewDistance + y];
			}
		}
	}
	
	public void act(){			
		
		// if standing on the DIRTY square, than suck
		if (skyNet[activeState.x][activeState.y] == DIRTY) {
			suck();
		}
		
		updateSkyNet();
		
		// if there is some order in path than poll last and execute it
		// return at the end and execute next order in new act call
		if (path.size() > 0) {
			switch (path.pollLast()) {
			case "f":
				// if action is forward
				boolean allowMove = false;
				if (activeState.orientation == World.NORTH && skyNet[activeState.x-1][activeState.y] != WALL)
					allowMove = true;
				else if (activeState.orientation == World.SOUTH && skyNet[activeState.x+1][activeState.y] != WALL)
					allowMove = true;
				else if (activeState.orientation == World.WEST && skyNet[activeState.x][activeState.y-1] != WALL)
					allowMove = true;
				else if (activeState.orientation == World.EAST && skyNet[activeState.x][activeState.y+1] != WALL)
					allowMove = true; 
				
				if (allowMove) {
					moveFW();
					// update position based on the agent's orientation
					if (activeState.orientation == World.NORTH) activeState.x -= 1;
					if (activeState.orientation == World.SOUTH) activeState.x += 1;
					if (activeState.orientation == World.WEST) activeState.y -= 1;
					if (activeState.orientation == World.EAST) activeState.y += 1;
				}
				break;
			case "l":
				// if action is left
				turnLEFT();
				// update orientation based on the agent's turn
				if (activeState.orientation == World.NORTH)
					activeState.orientation = World.WEST;
				else
					activeState.orientation -= 1;
				break;
			case "r":
				// if action is right
				turnRIGHT();
				// update orientation based on the agent's turn
				if (activeState.orientation == World.WEST)
					activeState.orientation = World.NORTH;
				else
					activeState.orientation += 1;
				break;
			default:
				break;
			}
			return;
		}
		
		// call findTarget function, if it returns false then it means that there are no more DIRTY
		// to clean, so halt
		if (!findTarget()) {
			halt();
		}
		
	}
	
	/**
	 * State class with information about position and orientation
	 * It saves previous state so the path can be reproduced
	 *
	 */
	private class State {
		int x, y, orientation;
		State prev;
		
		public State(int x, int y, int orientation, State prev) {
			this.x = x;
			this.y = y;
			this.orientation = orientation;
			this.prev = prev;
		}
		
		/**
		 * Returns actions that will be used to navigate the agent
		 * @return path with first action on last position
		 */
		public LinkedList<String> path() {
			LinkedList<String> out = new LinkedList<String>();
			State state = prev;
			int oldOrientation = orientation;
			while (state != null) {
				if (oldOrientation != state.orientation) {
					if (oldOrientation == World.NORTH && state.orientation == World.WEST) {
						out.add("r");
					} else if (oldOrientation == World.WEST && state.orientation == World.NORTH) {
						out.add("l");
					} else if (oldOrientation > state.orientation) {
						out.add("r");
					} else {
						out.add("l");
					}
				} else {
					out.add("f");
				}
				
				oldOrientation = state.orientation;
				state = state.prev;
			}
			return out;
		}
		
		@Override
		public String toString() {
			return "[" + x + "," + y + "] <" + orientation + ">";
		}
		
		@Override
		public int hashCode() {
			return x*10000+y*10+orientation;
		}
		
		@Override
		public boolean equals(Object obj) {
			return this.hashCode() == obj.hashCode();
		}
		
	}
	
		
}