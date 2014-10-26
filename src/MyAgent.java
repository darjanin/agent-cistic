import java.awt.Point;
import java.util.HashSet;
import java.util.LinkedList;

public  class MyAgent extends Agent{	
	int[][] net, oldNet;
	LinkedList<State> open = new LinkedList<State>();
	HashSet<State> close = new HashSet<State>();
	LinkedList<String> path = new LinkedList<String>();
	LinkedList<String> lastPath = new LinkedList<String>();
	
	// skyNet is my map where would be done pathfinding
	int[][] skyNet;
	int height, width;
	State lastUsedState;
	
	boolean destroyThem = false;
	boolean newLife = false;
	boolean firstRun = false;
	
	static final int UNKNOWN = 3;
		
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
	}

	
	private void skyNetMap() {
		for (int i = 0; i < this.height; i++) {
			for (int j = 0; j < this.width; j++) {
				System.out.print(skyNet[i][j]);
			}
			System.out.println("");
		}
	}
	
	private void p(String msg) {
		System.out.println(msg);
	}
	
	public void act(){			
		
		
		State state, newState;
		if (!firstRun) {
			open.add(new State(height/2, width/2, getOrientation(), null));
			lastUsedState = open.get(0);
			destroyThem = true;
			firstRun = true;
		}
//		else
//			open.addFirst(lastUsedState);
//		if (newLife) {
//			open = new LinkedList<State>();
//			open.add(lastUsedState);
//		}
		
		if (skyNet[lastUsedState.x][lastUsedState.y] == DIRTY) {
			p("DIRTY OUT");
			destroyThem = true;
		}
		
		if (skyNet[lastUsedState.x-1][lastUsedState.y] == DIRTY)
			open.addFirst(new State(lastUsedState.x-1, lastUsedState.y, lastUsedState.orientation, lastUsedState));
		else if (skyNet[lastUsedState.x+1][lastUsedState.y] == DIRTY)
			open.addFirst(new State(lastUsedState.x+1, lastUsedState.y, lastUsedState.orientation, lastUsedState));
		else if (skyNet[lastUsedState.x][lastUsedState.y-1] == DIRTY)
			open.addFirst(new State(lastUsedState.x, lastUsedState.y-1, lastUsedState.orientation, lastUsedState));
		else if (skyNet[lastUsedState.x][lastUsedState.y] == DIRTY)
			open.addFirst(new State(lastUsedState.x, lastUsedState.y+1, lastUsedState.orientation, lastUsedState));
		
		while (!open.isEmpty() && !destroyThem) {
//			p("OPEN: " + open.size());
//			p("OPEN CONTENT: " + open);
//			p("CLOSE: " + close.size());
//			p("CLOSE CONTENT: " + close);
			
			state = open.pop();
//			p("POP: " + state);
			
//			System.out.println(state.x + "x" + state.y);
			
			if (!close.contains(state)) {
				if (skyNet[state.x][state.y] == DIRTY) {
					p("DIRTY");
					lastUsedState = state;
					destroyThem = true;
					path = state.path();
					break;
				}
				
//				if (skyNet[state.x-1][state.y] == DIRTY)
//					open.addFirst(new State(state.x-1, state.y, state.orientation, lastUsedState));
//				if (skyNet[state.x+1][state.y] == DIRTY)
//					open.addFirst(new State(state.x+1, state.y, state.orientation, lastUsedState));
//				if (skyNet[state.x][state.y-1] == DIRTY)
//					open.addFirst(new State(state.x, state.y-1, state.orientation, lastUsedState));
//				if (skyNet[state.x][state.y] == DIRTY)
//					open.addFirst(new State(state.x, state.y+1, state.orientation, lastUsedState));
				
				if (skyNet[state.x][state.y] == UNKNOWN) {
					if (skyNet[state.x][state.y] == UNKNOWN) p("UNKNOWN");
					lastUsedState = state;
					destroyThem = true;
					path = state.path();
					break;
				}
				
				close.add(state);
			
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
				
//				lastUsedState = state;
			}
		}
		
		if (path.size() > 0) {
			p("MOVING: " + path);
			switch (path.pollLast()) {
			case "f":
				moveFW();
				break;
			case "l":
				turnLEFT();
				break;
			case "r":
				turnRIGHT();
				break;
			default:
				break;
			}
		} else {
			p("SUCK");
			suck();
			destroyThem = false;
			
			net = percept();	// aktualny percept
			// zadefinovane su konstanty CLEAN=0, DIRTY=1, WALL=2)
				
			
			p("LAST USED STATE: " + lastUsedState);
			
			skyNet[lastUsedState.x-1][lastUsedState.y-1] = net[getPerceptSize() - 1][getPerceptSize() - 1];
			skyNet[lastUsedState.x-1][lastUsedState.y] 	 = net[getPerceptSize() - 1][getPerceptSize() + 0];
			skyNet[lastUsedState.x-1][lastUsedState.y+1] = net[getPerceptSize() - 1][getPerceptSize() + 1];
			skyNet[lastUsedState.x][lastUsedState.y-1]   = net[getPerceptSize() + 0][getPerceptSize() - 1];
			skyNet[lastUsedState.x][lastUsedState.y]     = net[getPerceptSize() + 0][getPerceptSize() + 0];
			skyNet[lastUsedState.x][lastUsedState.y+1] 	 = net[getPerceptSize() + 0][getPerceptSize() + 1];
			skyNet[lastUsedState.x+1][lastUsedState.y-1] = net[getPerceptSize() + 1][getPerceptSize() - 1];
			skyNet[lastUsedState.x+1][lastUsedState.y] 	 = net[getPerceptSize() + 1][getPerceptSize() + 0];
			skyNet[lastUsedState.x+1][lastUsedState.y+1] = net[getPerceptSize() + 1][getPerceptSize() + 1];
			
			skyNetMap();
			
//			halt();
		}
		
		
		
	}
	
	class State {
		int x, y, orientation;
		State prev;
		boolean ends;
		
		public State(int x, int y, int orientation, State prev) {
			this.x = x;
			this.y = y;
			this.orientation = orientation;
			this.prev = prev;
			this.ends = false;
		}
		
		public boolean checkPosition(int x, int y) {
			return (this.x == x && this.y == y) ? true : false;
		}
		
		public LinkedList<String> path() {
			p("Generate path");
			LinkedList<String> out = new LinkedList<String>();
			State tmpState;
			State state = prev;
			int oldOrientation = orientation;
			while (state != null) {
//				if (state == lastUsedState) break;
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
				tmpState = state.prev;
//				state.prev = null;
				state = tmpState;
			}
//			prev = null;
			ends = true;
//			for (int i = 0; i < lastPath.size(); i++)
//				out.remove(0);
			p("PATH: " + out);
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