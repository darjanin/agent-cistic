import java.awt.Point;
import java.util.HashSet;
import java.util.LinkedList;

public  class MyAgent extends Agent{	
	int[][] net;
	LinkedList<State> open = new LinkedList<State>();
	HashSet<State> close = new HashSet<State>();
	LinkedList<String> path;
	
	int[] start = {20,20};
	int[] finish = {20,17};
	
	int statesCount = 0;
		
	public MyAgent(int height, int width) {
		
	}

	public void act(){			
		/* ZACIATOK MIESTA PRE VAS KOD */
		
		net = percept();	// aktualny percept
					// zadefinovane su konstanty CLEAN=0, DIRTY=1, WALL=2)

		State tmpState, newState;
		open.add(new State(start[0], start[1], getOrientation(), null));
		while (!open.isEmpty()) {
			tmpState = open.pop();

			statesCount++;
			if (tmpState.checkPosition(finish[0], finish[1])) {
				path = tmpState.path();
			}
			if (!close.contains(tmpState)) {
				close.add(tmpState);
			
				switch (tmpState.orientation) {
				case World.NORTH:
					newState = new State(tmpState.x - 1, tmpState.y, tmpState.orientation, tmpState);
					break;
				case World.SOUTH:
					newState = new State(tmpState.x + 1, tmpState.y, tmpState.orientation, tmpState);
					break;
				case World.WEST:
					newState = new State(tmpState.x, tmpState.y - 1, tmpState.orientation, tmpState);
					break;
				case World.EAST:
					newState = new State(tmpState.x, tmpState.y + 1, tmpState.orientation, tmpState);
					break;
				default:
					newState = null;
				}
				
				if (!close.contains(newState) && net[newState.x][newState.y] != WALL && net[newState.x][newState.y] != net[tmpState.x][tmpState.y]) {
					open.add(newState);
				}
				
				if (tmpState.orientation == World.NORTH)
					newState = new State(tmpState.x, tmpState.y, World.WEST , tmpState);
				else
					newState = new State(tmpState.x, tmpState.y, tmpState.orientation - 1, tmpState);
				if (!close.contains(newState))
					open.add(newState);
				
				if (tmpState.orientation == World.WEST)
					newState = new State(tmpState.x, tmpState.y, World.NORTH, tmpState);
				else
					newState = new State(tmpState.x, tmpState.y, tmpState.orientation + 1, tmpState);
				if (!close.contains(newState))
					open.add(newState);
				
			}
		}
		
		if (path.size() > 0) {
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
			halt();
		}
		
		
		/* KONIEC MIESTA PRE VAS KOD */
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
			LinkedList<String> out = new LinkedList<String>();
			State s = prev;
			int oldOrientation = orientation;
			while (s != null) {
				if (oldOrientation != s.orientation) {
					if (oldOrientation == World.NORTH && s.orientation == World.WEST) {
						out.add("r");
					} else if (oldOrientation == World.WEST && s.orientation == World.NORTH) {
						out.add("l");
					} else if (oldOrientation > s.orientation) {
						out.add("r");
					} else {
						out.add("l");
					}
				} else {
					out.add("f");
				}
				oldOrientation = s.orientation;
				s = s.prev;
			}
			return out;
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