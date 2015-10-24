package main;

import java.util.ArrayList;
import java.util.Arrays;

import com.sun.xml.internal.ws.util.StringUtils;

public class Node {
	public double x,y,dist;
	public void setX(double x) {
		this.x = x;
	}
	public void setY(double y) {
		this.y = y;
	}
	public boolean cp;
	public ArrayList<Node> parents,children,collisionpoints;
	public int ID;
	public ArrayList<Route> routes;
	
	public Node(double x1, double y1,int ID,boolean collisionpoint){
		this(x1,y1,ID);
		cp = collisionpoint;
	}
	public Node(double x1, double y1,int ID){
		this.ID = ID;
		cp = false;
		parents = new ArrayList<Node>();
		children = new ArrayList<Node>();
		collisionpoints = new ArrayList<Node>();
		x=x1;
		y=y1;
		routes = new ArrayList<Route>();
	}
	public String save(){
		String returnstr =  ID + ";" + x+ ";"+y+";";
		ArrayList<Integer> ps = new ArrayList<Integer>();
		ArrayList<Integer> cs = new ArrayList<Integer>();		
		for(Node p: parents){
			ps.add(p.ID);
		}
		for(Node c: children){
			cs.add(c.ID);
		}
		returnstr += (""+Arrays.asList(ps.toArray())).replaceAll("(^.|.$)", "").replace(", ", "," )+";";
		returnstr += (""+Arrays.asList(cs.toArray())).replaceAll("(^.|.$)", "").replace(", ", "," );
		return returnstr;
	}
	public Node clone(){
		Node returnnode = new Node(x,y,ID);
		returnnode.parents = parents;
		returnnode.children = children;
		return returnnode;
	}
	public void addParent(Node parent1){
		if(!parents.contains(parent1)){
			parents.add(parent1);
			parent1.addChild(this);
			if(!routes.contains(new Route(this,parent1))){
				routes.add(new Route(this,parent1));
			}
		}
	}
	public boolean hasCollisionPoints(){
		return (collisionpoints.isEmpty());
	}
	public void addChild(Node child1){
		if(!children.contains(child1)){
			children.add(child1);
			if(child1!=null){
				child1.addParent(this);
				if(!routes.contains(new Route(child1,this))){
					routes.add(new Route(child1,this));
				}
				// Add collision points along this path
				if(cp){
					if(children.size()==1){
						dist = this.distanceto(child1);
						if(dist<5){
							collisionpoints.add(new Node(Math.abs(x-child1.x/2),Math.abs(y-child1.y/2),0));
							return;
						}
						double directionto = this.dirto(child1);
						for(int i = 0; i<dist; i+=5){
							double tempx = Math.sin(directionto)*i+x;
							double tempy = Math.cos(directionto)*i+y;
							Node tempnode = new Node(tempx,tempy,0);
							collisionpoints.get(collisionpoints.size()).addChild(tempnode);
							collisionpoints.add(tempnode);
						}
						for(Node n: parents){
							if(n.hasCollisionPoints()){
								n.getLastCollisionPoint().addChild(collisionpoints.get(0));
							}
						}
					}
				}
			}else{
				System.out.println("somethings terribly wrong, we tried to add a null child ");
				Thread.dumpStack();
			}
		}
	}
	public Node getLastCollisionPoint(){
		return collisionpoints.get(collisionpoints.size());
	}
	public Node getChild(){
		return ((children.size()!=0)?children.get((int)Math.floor(Math.random()*children.size())):null);
	}
	public Route getChildRoute(){
		return ((routes.size()!=0)?routes.get((int)Math.floor(Math.random()*routes.size())):null);
	}
	public boolean hasParents(){
		return (parents.size()!=0);
	}
	public boolean hasChild(){
		return (children.size()!=0);
	}
	public String toString(){
		String returnstr = "";
		returnstr += "Node " + ID + " at " + x + "," + y + "\n";
		returnstr += "Children:";
		for(Node n: children){
			returnstr += " " + n.x + "," + n.y;
		}
		returnstr += "\n";
		returnstr += "Parents:";
		for(Node n: parents){
			returnstr += " " + n.x + "," + n.y;
		}
		returnstr += "\n";
		return returnstr;
	}
	public double distanceto(Node other){
		return (double)Math.sqrt((other.getX()-this.x)*(other.getX()-x)+(other.getY()-y)*(other.getY()-y));
	}
	public double getY() {
		return this.y;
	}
	public double getX() {
		return this.x;
	}
	public double dirto(Node other){
		double x1 = this.getX();
		double x2 = other.getX();
		double y1 = this.getY();
		double y2 = other.getY();
		double rise = y2-y1;
		double run = x2-x1;
		double poss = 0;
		if(run<0){
			poss = (double)Math.PI;
		}
		if(((double)Math.atan(rise/run)+poss)>Math.PI)
			return (double)(Math.atan(rise/run)+poss-Math.PI*2);
		return (double)Math.atan(rise/run)+poss;
	}
	public void removeChild(Node foundnode) {
		children.remove(foundnode);		
	}
}
