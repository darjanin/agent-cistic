# Projekt č.1 cleaner
Author: Milan Darjanin <me@milandarjanin.com>

## Goal

The goal of the agent (vacuum) is to clean or dirty squares on the map. 

## Idea

There are few steps that are followed by the agent to achieve his goal.

### Initialization

The first step is to create own map (SKYNET) for the agent in which will be stored data for searching new targets.
At the beginning is the whole SKYNET filled with UNKNOWN squares. The agents initial position is in the center
of SKYNET, orientated to the World.NORTH.

### Act

Now starts agents work. In one act can be done only one action by agent, and he can get data using his
only sensor, percept. There are options that can happen described in list:

1. Agent is standing on the DIRTY square. For this is only one solution. Suck it!
2. Update the SKYNET based on the agents view distance that is defined by the percept size.
3. If there is at least one order for agent in `path`, then agent starts to execute them.
	a) If order is `f`, then agent moves forward with updating his position based on old position and orientation.
	b) If order is `l`, then agent rotates left and updates his orientation minus one.
	c) If order is `r`, then agent rotates right and updates his orientation plus one.
4. The Agent starts locating new targets. Target is DIRTY or UNKNOWN square in SKYNET.
   Targeting will be more specified in path finding algorithm.
5. If target locating algorithm cannot find any new target, then halt the agent.
6. If the agent is not halted, start again from point 1. 

## Path finding algorithm - BREADTH-FIRST SEARCH

For find target function is used breadth-first search algorithm. The idea of it is described in next steps.
Just few comments about used structures.

- OPEN is used to store states that will be tried to navigate through them. It's FIFO (first in, first out).
- CLOSE contains states that had been visited in lookup for target.
- ACTIVE_STATE is the agents position in SKYNET.

1. Start by adding the ACTIVE_STATE to the OPEN.
2. While the OPEN is not empty we are going to try find target.
3. Pop out first item from OPEN. (Pop out, returns it's value and removes the first item from OPEN).
4. Test if the popped stated is not in CLOSE. If it's than ignore it and go to 3rd step.
5. Add the popped state to CLOSE structure, so we are not going to use it again in this search.
6. The important part of our search, test if square on position from the popped state is DIRTY or UNKNOWN.
   If it is than call the popped state `path()` method that will generate list of orders for the agent,
   how to move from the position in real world to square that is DIRTY or UNKNOWN. Assign this list to
   `path` variable and return from the target locating function.
7. If state is not DIRTY or UNKNOWN in SKYNET, then we are going to add three new states. Or we are gonna try.
8. First new state that can possible go to OPEN is the state to which we will get if we move forward. If there
   is no WALL in SKYNET and we werent't on this square, then we will add it to OPEN.
9. This step is similar for left and right version, so it's connected step. If you are going move left, then
   new orientation will be smaller by one. To the right we add one to the new state. If new created state
   (turn left/right on the same position) is not in the CLOSE, then we add it to the OPEN.
10. If the OPEN is empty, it means that we don't have any new target to locate so we return false and
    the agents then knows that he can halt his attack. :-)

## Data structures

In this cleaning system are used few data structures.

### Linked List - STATE

State stores informations about agent actual position during target locating. His position [x,y] and orientation
described in the world coordinates (NORTH, EAST, SOUTH, WEST). It has one more information that does not specify
the current agents state, and its the `prev`. It stores previous state from which got agent to new state.
This information is used for recreating list of orders to navigate agent from it's position in real word to this
new position with target.

It has overrode three methods:

- toString(): used for debugging prints
- hashCode(): calculates hash value that is unique for State with specific position [x,y] and orientation.
- equals():   we specify what means for State to be equal to another; in this case they are equal if their
			  hash code is same
			  
The most important function is `path()` that returns list of orders for the agent, how to move to target.
It goes from actual state to previous one, if there is some. If the orientation changes from one state to
another, it means the agent needs to turn. If the orientation stays same in two different states, than
the agent goes forward. Returned list of orders for movement is with first order at the last place.

### HashSet

It is used for storing visited States. Reason for using set is that it has method contains that can return
if State is part of set in O(1).

