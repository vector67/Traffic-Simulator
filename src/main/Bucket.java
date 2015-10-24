package main;

import java.util.ArrayList;

public class Bucket extends Node{
	ArrayList<Vehicle> vhcls;
	public int width,height;
	public Bucket(double x1, double y1,int w,int h) {
		super(x1, y1,0);
		vhcls = new ArrayList<Vehicle>();
		width = w;
		height = h;
	}
	public boolean addVehicle(Vehicle arg0){
		return vhcls.add(arg0);
	}
	public boolean removeVehicle(Vehicle arg0){
		return vhcls.remove(arg0);
	}
	public ArrayList<Vehicle> getVehicles(){
		return vhcls;
	}
	public boolean isPointIn(int x1,int y1){
		return (x1<(x+width/2)&&x1>(x-width/2)&&y1<(y+height/2)&&y1>(y-height/2));
	}
	public int topSide(){
		return (int) y+height/2;
	}
	public int rightSide(){
		return (int) x+width/2;
	}
	public int bottomSide(){
		return (int) y-height/2;
	}
	public int leftSide(){
		return (int) x-width/2;
	}
	public String toString(){
		
		return "Bucket at " + x + "," + y + " with a width of " + width + " and a height of " + height + "containing" + vhcls.size() + " vehicles";
		
	}
	public boolean hasVehicles() {
		
		return (vhcls.size()!=0);
	}
}
