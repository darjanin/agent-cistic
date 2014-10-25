import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class World {
	public static final int CLEAN = 0;
	public static final int DIRTY = 1;
	public static final int WALL = 2;
	public static final int AGENT =3;
	
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;	
	
	public static final String FW = "FW";
	public static final String LEFT = "LEFT";
	public static final String RIGHT = "RIGHT";
	public static final String SUCK = "SUCK";
	
	
	
	public static Random r = new Random(new Date().getTime());
	
	private long WAIT = 1000;
	private int PERCEPTION = 0; 
		
	public Present presenter;
	
	public int net[][];
	public boolean v[][]; // visited?
	public int anet[][];
	public int ax, ay;
	private int h;
	private int w;
	
	private boolean randomMap;	
	private Map xcoor = new HashMap();
	private Map ycoor = new HashMap();
	private Map orient = new HashMap();
	private List agents= new ArrayList();
	
	boolean save = false;
	
	public World(String fileName, long wait, int perception) {
		load(fileName);
		this.WAIT=wait;
		this.PERCEPTION=perception;
	}
	
	private void load(String fileName) {
		//random xy;
		try {
		
			///////////////////////////////////////////
		URL url = GuiStarter.class.getResource(fileName);
		if (url == null) {
			throw new IllegalArgumentException("Subor " + fileName +
					" nebol najdeny.");
		}
		File f = new File(url.toURI());
			///////////////////////////////////////////
		
		
			BufferedReader r = new BufferedReader(new FileReader(f));
			String s=r.readLine();
			
			if (s.equals("random")){
				randomMap = true;
				s=r.readLine();
				h=Integer.parseInt(s);
				s=r.readLine();
				w=Integer.parseInt(s);				
				s=r.readLine();
				double di=Double.parseDouble(s);				
				s=r.readLine();
				double wa=Double.parseDouble(s);				

				v = new boolean[w][h];				
				net = new int[w][h];
				anet = new int[w][h];
				untidy(di,wa);
				
				createAgent();
			}else{
				randomMap = false;				
				s=r.readLine();
				h=Integer.parseInt(s);
				s=r.readLine();
				w=Integer.parseInt(s);
				net = new int[w][h];
				v = new boolean[w][h];
				anet = new int[w][h];
				for (int i = 0; i < w; i++) {
					s=r.readLine();
					for (int j = 0; j < s.length(); j++) {
						v[i][j] = false;
						switch (s.charAt(j)) {
						case '#':
							net[i][j]=WALL;	
							break;
						case '*':								
							net[i][j]=DIRTY;
							break;
						case '@':								
							ax = j;
							ay = i;
							break;
						case '0':								
							ax = j;
							ay = i;
							net[i][j]=DIRTY;							
							break;									
						default:
							net[i][j]=CLEAN;							
						}
						
					}
				}
			}
			r.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	
	private void createAgent() {
		int x = r.nextInt(getWidth()-1)+1;
		int y = r.nextInt(getHeight()-1)+1;
		while (!freePlace(x, y)){
			x = r.nextInt(getWidth()-1)+1;
			y = r.nextInt(getHeight()-1)+1;
		}
		
		ax = x;
		ay = y;
	}
	
	public void addAgent(MyAgent agent) {
		if (!agents.contains(agent)){
			agent.putInWorld(this);
			agents.add(agent);
			xcoor.put(agent,ax);
			ycoor.put(agent,ay);
			if (randomMap)
				orient.put(agent,r.nextInt(4));				
			else			
				orient.put(agent,NORTH);
		    anet[ay][ax]=AGENT;	
		}		
	}
	
	public boolean freePlace(int x,int y){
		if (x>=h || y>=w || x<0 || y<0) return false;
		if (net[y][x]==WALL || anet[y][x]==AGENT) return false;
		return true;
		
	}
	
	public void run(){
		Thread t = new Thread(){
			public void run(){
				
				if (presenter!=null)
					presenter.redraw();
				
				Agent element = (Agent) agents.get(0);
				
				do{
					
					try {

						if (presenter!=null){
							presenter.redraw();
							Thread.sleep(WAIT);
						}
													
							long s = element.getSteps();
							long p = element.getPercepted();			
							
							element.act();
							
							if (element.getSteps() - s > 1){
								throw new Exception("Agent performed more than one action in act.");								
							}else if (element.getPercepted() - p > 1){
								throw new Exception("Agent performed more than one perception in act.");
							}
					} catch (Exception e) {
						e.printStackTrace();
						element.halt();
						System.exit(1);
					}
				} while(element.isRunning());
				
				save = true; 

				if (presenter!=null){
					presenter.redraw();
				}

				System.out.println(element.getSteps() + " steps");								
			}
		};
		t.start();
	}

	private void untidy(double di, double wa){
		for (int i=0;i<w;i++)
			for (int j=0;j<h;j++){
				double d = r.nextDouble();
				if (d<di){
					net[i][j]=DIRTY;
					anet[i][j]=CLEAN;
				}else if (d<di+wa){					
					net[i][j]=WALL;
					anet[i][j]=CLEAN;
				}
				if (i==0 || j==0 || i==w-1 || j==h-1){
					net[i][j]=WALL;
				}
			}
	}
	
	private int getX(Agent agent){
		if (xcoor.containsKey(agent)){
			return ((Integer)xcoor.get(agent)).intValue();
		}
		return -1;
	}
	
	private int getY(Agent agent){
		if (ycoor.containsKey(agent)){
			return ((Integer)ycoor.get(agent)).intValue();
		}
		return -1;
	}
	
	public int getO(Agent agent){
		if (orient.containsKey(agent)){
			return ((Integer)orient.get(agent)).intValue();
		}
		return -1;
	}
	
	public void moveFW(Agent agent){
		int p = perceptFW(agent);
		//System.out.println("FW = "+p);
		int o=getO(agent);
		//System.out.println("O = "+o);
		if (p!=WALL && p!=AGENT){
			anet[getY(agent)][getX(agent)]=CLEAN;
			switch (o) {
			case NORTH:
				ycoor.put(agent,new Integer(getY(agent)-1));	
				break;
			case SOUTH:
				ycoor.put(agent,new Integer(getY(agent)+1));	
				break;
			case EAST:
				xcoor.put(agent,new Integer(getX(agent)+1));	
				break;
			case WEST:
				xcoor.put(agent,new Integer(getX(agent)-1));	
				break;

			default:
				break;
			}
			
			anet[getY(agent)][getX(agent)]=AGENT;
			
			ax = getX(agent);
			ay = getY(agent);			
			
		}
		
	}
	
	public void turnRIGHT(Agent agent){
		int o = getO(agent);
		orient.put(agent, new Integer((o+1)%4));
	}

	public void turnLEFT(Agent agent){
		int o = getO(agent);
		orient.put(agent, new Integer((o+3)%4));
	}

	public void suck(Agent agent){
		int x = getX(agent);
		int y = getY(agent);
			net[y][x] = CLEAN;
	}
	
	public int[][] percept(Agent agent){
		int[][] result = new int[1+2*PERCEPTION][1+2*PERCEPTION];		
		int x1=getX(agent);
		int y1=getY(agent);
		int x2=x1-PERCEPTION;
		int y2=y1-PERCEPTION;
		
		ax = x1;
		ay = y1;
		
		for (int i = 0; i < net.length; i++) {
			for (int j = 0; j < net[i].length; j++) {
				if ((y1 - PERCEPTION <= i) && (i <= y1 + PERCEPTION) && (x1 - PERCEPTION <= j) && (j <= x1 + PERCEPTION)) {
					v[i][j] = true;
				}
			}
		}
		
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[i].length; j++) {
				int x=x2+j;
				int y=y2+i;
				if (x<0||y<0||x>=h||y>=w) {
					result[i][j]=WALL;	
				}					
				else {
					result[i][j]=net[y][x];	
				}
			}	
		}
		return result;
	}

	
	public int perceptFW(Agent agent){
		int o = getO(agent);
		int x = getX(agent);
		int y = getY(agent);
		if (x>=0 && x<h && y>=0 && y<w){			
			if (o==NORTH){
				if (y>0){
					if (anet[y-1][x]==AGENT) return AGENT;
					else return net[y-1][x];
				}
				return WALL;
			}
			if (o==EAST){
				if (x<h-1){
					if (anet[y][x+1]==AGENT) return AGENT;
					else return net[y][x+1];
				}
				return WALL;
			}
			if (o==SOUTH){
				if (y<w-1){
					if (anet[y+1][x]==AGENT) return AGENT;
					else return net[y+1][x];
				}
				return WALL;
			}
			if (o==WEST){
				if (x>0){
					if (anet[y][x-1]==AGENT) return AGENT;
					else return net[y][x-1];
				}
				return WALL;
			}
		}
		return WALL;
	}
	
	
	public String print(){
		char s[][] = new char[w][h];
		for( int i=0;i<w;i++)
			for (int j=0;j<h;j++){
        if (anet[i][j]==AGENT){
          s[i][j]='0';
        }
        else{
  				switch (net[i][j]) {
  				case DIRTY:
  					s[i][j]='*';
  					break;
  				case CLEAN:
  					s[i][j]='_';
  					break;
  				case WALL:
  					s[i][j]='#';
  					break;
  				default:
  					break;
  				}
        }        
			}
		for (Iterator it = agents.iterator(); it.hasNext();) {
			Agent element = (Agent) it.next();
			int x = getX(element);
			int y = getY(element);
			s[y][x]='@';			
		}
		StringBuffer res = new StringBuffer(" ");
		for (int i=0;i<w;i++){
			res.append(s[i]).append("\n ");
		}
		//res.append(lastAction).append("\n");
		return res.toString();
	}
	
	public int getWidth(){
		return h;
	}
	
	public int getHeight(){
		return w;
	}
	
	public int getPerceptSize(){
		return PERCEPTION;
	}
	
}