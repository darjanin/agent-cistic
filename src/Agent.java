public abstract class Agent {
	public static final int CLEAN = 0;
	public static final int DIRTY = 1;
	public static final int WALL = 2;
	public static final int AGENT = 3;

	private long step=0;
	
	private long percepted=0;
	
	private World world;
	
	private boolean running;
	
	public Agent(){
		running = true;		
	}
	
	/**
	*zaradi agenta do sveta
	**/
	public void putInWorld(World w){
		world= w;
	}
	
	public abstract void act();
			
		
	/* =========================================================================================== */
	/* ================================= DO NOT MODIFY ============================================ */
	/* =========================================================================================== */
	
	public void halt(){
		running = false;
	}
	
	public boolean isRunning(){
		return running;
	}
	
	/**
	  * Pohne agentom dopredu, ak nenastane kolizia
	  **/
	public void moveFW(){
		if (running){
			incStep();
			world.moveFW(this);
		}
	}	
	

	/**
	  * Otoci agenta vpravo
	  **/
	public void turnRIGHT(){
		if (running){
			incStep();
			world.turnRIGHT(this);
		}
	}
	
	/**
	  * Otoci agenta vlavo
	  **/
	public void turnLEFT(){	
		if (running){
			incStep();
			world.turnLEFT(this);
		}
	}
	
	/**
	* Vysaje spinu
	**/
	public void suck(){
		if (running){
			incStep();
			world.suck(this);
		}
	}
	
	/**
	 * vrati dvojroznemrne pole s rozmermy 2*getPerception()+1
	 * agent sa nachadza v strede
	 **/
	public int[][] percept(){
		incPercepted();		
		return world.percept(this);
	}
	
	/**
	  * vrati dosah sensora
	  */
	public int getPerceptSize(){
		return world.getPerceptSize();
	}

	/**
	* Pocet krokov
	**/
	public long getSteps(){
		return step;
	}
	
	public long getPercepted(){
		return percepted;
	}
	
	/**
	* Orientacia
	**/
	public int getOrientation(){
		return world.getO(this);
	}
	

	/* =========================================================================================== */
	/* ================================= DO NOT USE ============================================== */
	/* =========================================================================================== */
	
    private void incStep(){
    	step++;
    }
    
    private void incPercepted(){
    	percepted++;
    }
	
    
}
