import java.util.ArrayList;

@SuppressWarnings("serial")
public class mineRecord implements java.io.Serializable {
	
	private int time;
	private String name;
	private ArrayList<Integer> mine;
	private ArrayList<Integer> visited;
	
	public mineRecord(String name, int time, ArrayList<Integer> mine, ArrayList<Integer> visited) {
		this.time = time;
		this.name = name;
		this.mine = mine;
		this.visited = visited;
	}
	
	public int getTime() {
		return time;
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<Integer> getMine(){
		return mine;
	}
	
	public ArrayList<Integer> getVisited(){
		return visited;
	}
	
	public String toString() {
		String s = name + ", " + time + ", " + mine + visited;
		return s;
	}
}