import java.awt.Point;
import java.rmi.activation.Activatable;
import java.util.HashSet;
import java.util.LinkedList;

public  class MyAgent extends Agent{	
	int[][] net;
	LinkedList<String> path = new LinkedList<String>();
	
	// skyNet is my map where would be done path-finding
	int[][] skyNet;
	int height, width;
	State activeState;
	
	static final int UNKNOWN = 7;
		
	public MyAgent(int height, int width) {
		// initialize the skyNet with dimension 4 times larger than input map with UNKNOWN squares
		this.height = height*4+1;
		this.width = width*4+1;
		skyNet = new int[this.height][this.width];
		for (int i = 0; i < this.height; i++) {
			for (int j = 0; j < this.width; j++) {
				skyNet[i][j] = UNKNOWN;
			}
		}
		
		activeState = new State(this.height/2, this.width/2, World.NORTH, null); 
	}

	
	private void skyNetMap() {
		for (int i = 0; i < this.height; i++) {
			for (int j = 0; j < this.width; j++) {
				if (skyNet[i][j] == UNKNOWN)
					System.out.print(" ");
				else
					System.out.print(skyNet[i][j]);
			}
			System.out.println("");
		}
	}
	
	private void netMap(int[][] net) {
		p(">>> NET");
		System.out.print(net[getPerceptSize() - 1][getPerceptSize() - 1]);
		System.out.print(net[getPerceptSize() - 1][getPerceptSize() - 0]);
		System.out.print(net[getPerceptSize() - 1][getPerceptSize() + 1]);
		p("");
		System.out.print(net[getPerceptSize() - 0][getPerceptSize() - 1]);
		System.out.print(net[getPerceptSize() - 0][getPerceptSize() - 0]);
		System.out.print(net[getPerceptSize() - 0][getPerceptSize() + 1]);
		p("");
		System.out.print(net[getPerceptSize() + 1][getPerceptSize() - 1]);
		System.out.print(net[getPerceptSize() + 1][getPerceptSize() - 0]);
		System.out.print(net[getPerceptSize() + 1][getPerceptSize() + 1]);
		p("");
	}
	
	private void p(String msg) {
		System.out.println(msg);
	}
	
	private boolean findTarge() {
		p("::: FINDING TARGET");
		
		LinkedList<State> open = new LinkedList<State>();
		HashSet<State> close = new HashSet<State>();
		State state, newState;
		
		open.add(activeState);
		
		while(!open.isEmpty()) {
			p("======================================");
			p("OPEN " + open);
			p("CLOSE " + close);
			state = open.pop();
			
			if (!close.contains(state)) {
				close.add(state);
				
				// if agent stands on DIRTY or UNKNOWN square, generate path to it from active state
				if (skyNet[state.x][state.y] == DIRTY || skyNet[state.x][state.y] == UNKNOWN) {
					p("FOUND DIRTY OR UNKNOWN");
					path = state.path();
					return true;
				}
				
				// base on orientation add new state that will move forward
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
				
				// if newState is not in closed states and is not wall then add it to the open
				if (!close.contains(newState) && skyNet[newState.x][newState.y] != WALL) {
					open.add(newState);
				}
				
				// rotate to left
				if (state.orientation == World.NORTH)
					newState = new State(state.x, state.y, World.WEST , state);
				else
					newState = new State(state.x, state.y, state.orientation - 1, state);
				if (!close.contains(newState))
					open.add(newState);
				
				// rotate to the right
				if (state.orientation == World.WEST)
					newState = new State(state.x, state.y, World.NORTH, state);
				else
					newState = new State(state.x, state.y, state.orientation + 1, state);
				if (!close.contains(newState))
					open.add(newState);
				
			}
			
			
			p("OPEN " + open);
			p("CLOSE " + close);
			p("======================================");
		}
		
		// halt if no target has been found
		p("HALTED");
		halt();
		return false;
		
	}
	
	public void updateSkyNet() {
		p(">>> UPDATE SKYNET");
		
		net = percept();	// aktualny percept
		// zadefinovane su konstanty CLEAN=0, DIRTY=1, WALL=2)
		
		netMap(net);
			
		p("LAST USED STATE: " + activeState);
		
		skyNet[activeState.x-1][activeState.y-1] = net[getPerceptSize() - 1][getPerceptSize() - 1];
		skyNet[activeState.x-1][activeState.y] 	 = net[getPerceptSize() - 1][getPerceptSize() + 0];
		skyNet[activeState.x-1][activeState.y+1] = net[getPerceptSize() - 1][getPerceptSize() + 1];
		skyNet[activeState.x][activeState.y-1]   = net[getPerceptSize() + 0][getPerceptSize() - 1];
		skyNet[activeState.x][activeState.y]     = net[getPerceptSize() + 0][getPerceptSize() + 0];
		skyNet[activeState.x][activeState.y+1] 	 = net[getPerceptSize() + 0][getPerceptSize() + 1];
		skyNet[activeState.x+1][activeState.y-1] = net[getPerceptSize() + 1][getPerceptSize() - 1];
		skyNet[activeState.x+1][activeState.y] 	 = net[getPerceptSize() + 1][getPerceptSize() + 0];
		skyNet[activeState.x+1][activeState.y+1] = net[getPerceptSize() + 1][getPerceptSize() + 1];
		
		skyNetMap();
		p("");
	}
	
	public void act(){			
		
		
		// if standing on the DIRTY square, than suck
		if (skyNet[activeState.x][activeState.y] == DIRTY) {
			p(">>> SUCK");
			suck();
		}
		
		updateSkyNet();
		
		// if there is some order in path than poll last and execute it
		// return at the end and execute next order in new act call
		if (path.size() > 0) {
			p("||| MOVING: " + path);
			switch (path.pollLast()) {
			case "f":
				boolean allowMove = false;
				if (activeState.orientation == World.NORTH && skyNet[activeState.x-1][activeState.y] != WALL)
					allowMove = true;
				if (activeState.orientation == World.SOUTH && skyNet[activeState.x+1][activeState.y] != WALL)
					allowMove = true;
				if (activeState.orientation == World.WEST && skyNet[activeState.x][activeState.y-1] != WALL)
					allowMove = true;
				if (activeState.orientation == World.EAST && skyNet[activeState.x][activeState.y+1] != WALL)
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
				turnLEFT();
				// update orientation based on the agent's turn
				if (activeState.orientation == World.NORTH)
					activeState.orientation = World.WEST;
				else
					activeState.orientation -= 1;
				break;
			case "r":
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
		
		if (!findTarge()) {
			p("HALTED");
			halt();
		}
		
	}
	
	class State {
		int x, y, orientation;
		State prev;
		
		public State(int x, int y, int orientation, State prev) {
			this.x = x;
			this.y = y;
			this.orientation = orientation;
			this.prev = prev;
		}
		
		public boolean checkPosition(int x, int y) {
			return (this.x == x && this.y == y) ? true : false;
		}
		
		public LinkedList<String> path() {
			p("=== GENERATE PATH");
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
			p("=== PATH: " + out);
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