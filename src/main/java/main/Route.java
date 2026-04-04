package main;

import java.util.ArrayList;

public class Route {

	private Node start,end;
	ArrayList<Vehicle> vehicles;
	public Route(Node start, Node end){
		this.vehicles = new ArrayList<Vehicle>();
		this.start = start;
		this.end = end;
	}
	public void addVehicle(Vehicle v){
		vehicles.add(v);
	}
	public Vehicle nextVehicle(Vehicle v){
		if(vehicles.size()==0){
			return null;
		}
		if(vehicles.contains(v)){
			int index = vehicles.indexOf(v);
			if(index!=0){
				return vehicles.get(index-1);
			} else {
				return null;
			}
		} else {
			return null;
			// TODO: put proper forward looking code
		}
	}
	public boolean equals(Object other){
		if(other instanceof Route){
			Route rother = (Route) other;
			return (rother.start.equals(this.start))&&(rother.end.equals(this.end));
		}
		return false;
	}
}
