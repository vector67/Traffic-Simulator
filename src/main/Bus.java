package main;

import java.awt.Color;

public class Bus extends Vehicle {

	public Bus(double x1, double y1, int w1, int h1, double a1, double s1,Color col,Waypoints wp,Start st1,Node currentnode1,double steer) {
		super(x1, y1, w1, h1, a1, s1,col,wp,st1,steer,currentnode1);
		// TODO Auto-generated constructor stub
	}
	public Bus(double x1, double y1, int w1, int h1, double a1, double s1,Color col,Waypoints wp,Start st1,double steer,Node currentnode1, int id) {
		super(x1, y1, w1, h1, a1, s1,col,wp,st1,steer,currentnode1,id);
		// TODO Auto-generated constructor stub
	}
	public Bus(Vehicle c){
		super(c);
	}
}
