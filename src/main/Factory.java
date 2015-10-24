package main;

import java.util.ArrayList;

import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Transform;

public class Factory {
	double x,y;
	double direction;
	double frequency; //this is the interval between cars being created
	ArrayList<Vehicle> modelvehicles; //vehicles to choose from
	int width=10,height=10;
	Start s;
	int amount;
	double timesincecreate;
	Waypoints wp;
	int curid;
	public Factory(Start s,Waypoints wp1){
		modelvehicles = new ArrayList<Vehicle>();
		this.s = s;
		wp = wp1;
		curid=0;
		amount =10000;
	}
	public void update(){
		int delta = s.getDelta();
		timesincecreate += ((double)delta)/1000.0f;
		int numcars = (int)(timesincecreate/frequency);
		timesincecreate -= (numcars)*frequency;
		if(timesincecreate<0){
			timesincecreate = 0;
		}
		
		if(numcars!=0&&!(amount<=0)){
			//System.out.println("we have a new car because numcars = " +numcars + " x = " + x + " and y = " + y);
			Vehicle v;
			if(!isColliding()){
				for(int i = 0; i<numcars;i++){
					v = modelvehicles.get((int)Math.random()*modelvehicles.size());
					if(v.getClass()==(new Car(v)).getClass()){
						Car newcar = new Car(x,y,v.w,v.h,direction,v.maxspeed,v.col,wp,s,v.steer,v.currentnode,curid);
						//System.out.println(newcar);
						s.vehicles.add(newcar);
					}else {
						s.vehicles.add(new Bus(x,y,v.w,v.h,direction,v.maxspeed,v.col,wp,s,v.steer,v.currentnode,curid));
					}
					curid++;
					amount--;
				}
			}
		}

	}
	
	public boolean isColliding(){
		Rectangle us = new Rectangle((float)x,(float)y,width,height);
		for(Vehicle b:s.vehicles){
			Rectangle them = new Rectangle((float)b.x, (float)b.y, (float)b.w, (float)b.h);
			them.transform(Transform.createRotateTransform(((float)b.angle)));
			if(them.intersects(us)){
				return true;
			}
		}
		return false;
	}
}
