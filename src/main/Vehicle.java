package main;

import java.awt.Color;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Transform;
//import org.newdawn.slick.geom.Vector2f;


public abstract class Vehicle{
	public boolean begin,colcirc1,colcirc2,colcirc3;
	public Bucket[] sbuckets;
	public Color colcirccol;
	public int colcircradius;
	public Node forwardpoint1,forwardpoint2,forwardpoint3;
	public ArrayList<Vehicle> checkedcollisions;
	public Bucket currentbucket;
	public int bleft,bright,btop,bbottom;
	public boolean calculate,constantspeed;
	public double x,y;
	public int w,h;
	double addninety;
	public double angle,speed,steer,maxspeed,braking;
	public double tcx,tcy,tcr; //turning circle vars
	double Mtocentercirc = 0,Ctocentercirc = 0,Mmiddleline = 0,Cmiddleline = 0,oldx,oldy;
	public double adddelta;
	double secs,circ,radius;
	public Color col;
	public Waypoints waypts;
	public Node currentnode;
	public Route currentroute;
	public int ID;
	public double avgspeedfortcirc,distancefortcirc,secstocompletefortcirc;
	public double distancetravelled,timetaken;
	public Node[] cache;
	public int cachepointer;
	Start st;
	public Vehicle(double x1,double y1,int w1,int h1,double a1,double s1,Color col2,Waypoints wp,Start st1,double steer,Node currentnode1,int id){
		this(x1,y1,w1,h1,a1,s1,col2,wp,st1,steer,currentnode1);
		ID = id;
	}
	public Vehicle(double x1,double y1,int w1,int h1,double a1,double s1,Color col2,Waypoints wp,Start st1,double steer1,Node currentnode1){
		//super();
		//super.Setup(new Vector2f(4,2), 1, new Color(1f,1f,1f),new Vector2f((float)x1,(float)y1));
		cache = new Node[10];
		
		forwardpoint1 = new Node((x1+Math.sin(angle)*(w1*3)),(y1+Math.cos(angle)*w1*3),0);
		forwardpoint2 = new Node((x1+Math.sin(angle)*(w1*2)),(y1+Math.cos(angle)*w1*2),0);
		forwardpoint3= new Node((x1+Math.sin(angle)*(w1)),(y1+Math.cos(angle)*w1),0);
		begin = true;
		x=x1;
		y=y1;
		w=w1;
		h=h1;
		angle=a1;
		maxspeed=s1;
		speed = 0.1;
		col = col2;
		waypts = wp;
		steer = steer1;
		st = st1;
		checkedcollisions = new ArrayList<Vehicle>();
		colcirccol = new Color((float)Math.random(),(float)Math.random(),(float)Math.random());
		colcircradius = (int)(Math.random()*1000); 
		
		if(!wp.isEmpty()){
			currentnode = wp.get(1);
			currentnode = currentnode1;
			currentroute = currentnode.getChildRoute();
			currentroute.addVehicle(this);
			if(currentnode.hasChild()){
				angle = Start.dirto(x, y, currentnode.getChild().getX(), currentnode.getChild().getY());
			} else {
				angle = Start.dirto(x, y, currentnode.getX(), currentnode.getY());
			}
				/*cache[0] = current; //assumes 1 child per node
		
			for(int i = 1; i <10; i++){
				cache[i] = cache[i-1].getChild();
			}
			cachepointer = 0;*/
			
		}
		
	}
	public Vehicle(Vehicle v){
		this(v.x,v.y,v.w,v.h,v.angle,v.maxspeed,v.col,v.waypts,v.st,v.steer,v.currentnode,v.ID);
	}
	public void update(){
		forwardpoint1.x = x + (double)Math.cos(angle)*(w*3);
		forwardpoint1.y = y + (double)Math.sin(angle)*(w*3);
		forwardpoint2.x = x + (double)Math.cos(angle)*(w*2);
		forwardpoint2.y = y + (double)Math.sin(angle)*(w*2);
		forwardpoint3.x = x + (double)Math.cos(angle)*(w);
		forwardpoint3.y = y + (double)Math.sin(angle)*(w);
		/*GL11.glColor3f((colcirc1)?1f:0, (colcirc1)?0f:1f,0);
		st.glCircle3i(forwardpoint1.x, forwardpoint1.y, 1);
		GL11.glColor3f((colcirc2)?1f:0, (colcirc2)?0f:1f,0);
		st.glCircle3i(forwardpoint2.x, forwardpoint2.y, 1);
		GL11.glColor3f((colcirc3)?1f:0, (colcirc3)?0f:1f,0);
		st.glCircle3i(forwardpoint3.x, forwardpoint3.y, 1);*/
		if(begin){
			currentbucket = st.buckets.getBucketIn((int)x,(int)y);
			currentbucket.addVehicle(this);
			begin = false;
			btop = currentbucket.topSide();
			bright = currentbucket.rightSide();
			bbottom = currentbucket.bottomSide();
			bleft = currentbucket.leftSide();
			checksides();
		}
		checkedcollisions.clear();
		updateBuckets();
		adddelta += st.getDelta()/1000f;
		if(adddelta>1){
			adddelta = 0;
		}
		double directionto = Start.dirto(x, y, currentnode.x, currentnode.y);
		if(currentnode!=null){
			
			if(Start.distance(x,y,currentnode.x,currentnode.y)<(Math.max(speed*((double)st.getDelta())/1000f,3))*1.1){
				constantspeed = false;
				calculate = false;
				oldx = x;
				oldy = y;
				Mtocentercirc = 0;
				Ctocentercirc = 0;
				Mmiddleline = 0;
				Cmiddleline = 0;
				braking = 0;
				if(currentnode.hasChild()){
					currentnode = currentnode.getChild();
					currentroute = currentnode.getChildRoute();
					currentroute.addVehicle(this);
				}else{
					st.removeVehicle(this);
					currentbucket.removeVehicle(this);
				}
				directionto = Start.dirto(x, y, currentnode.x, currentnode.y);
				if(ID==0){
					//System.out.println("Reached node, going towards node " + currentnode.x +","+currentnode.y);
				}
				recadjustangle(directionto,0);
				secs = 360/steer;
				circ = speed*secs;
				radius = (circ/Math.PI)/2;
				addninety = 0;
				double centerx,centery,centerx1,centery1;
				if(directionto>angle){
					addninety = Math.PI/2;
					centerx = Math.cos(angle+Math.PI/2)*radius+x;
					centery = Math.sin(angle+Math.PI/2)*radius+y;
				}else {
					addninety = -Math.PI/2;
					centerx = Math.cos(angle-Math.PI/2)*radius+x;
					centery = Math.sin(angle-Math.PI/2)*radius+y;
				}


				if(Start.distance(centerx, centery, currentnode.x, currentnode.y)<radius){
					calculatetcirc(directionto);
				}
				
			}else if((Start.distance(x,y,currentnode.x,currentnode.y)<(speed*((double)st.getDelta())/1000f)*50)&&(Math.abs(directionto-angle)>Math.PI/2)){
				calculatetcirc(directionto);
			}else{
			}
		}
		/*GL11.glColor3f(1f,1f,1f);
		GL11.glLineWidth(0.5f);
	    GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2d(0,Mtocentercirc*0+Ctocentercirc);
		GL11.glVertex2d(1280,Mtocentercirc*1280+Ctocentercirc);
	    GL11.glEnd();
	    GL11.glColor3f(1f,1f,1f);
		GL11.glLineWidth(0.5f);
	    GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2d(0,Mmiddleline*0+Cmiddleline);
		GL11.glVertex2d(1280,Mmiddleline*1280+Cmiddleline);
	    GL11.glEnd();
	    GL11.glColor3f(0f,0f,1f);
		GL11.glLineWidth(5f);
	    GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2d(oldx,oldy);
		GL11.glVertex2d(currentnode.x,currentnode.y);
	    GL11.glEnd();
	    GL11.glColor3f(0f,0f,1f);
		GL11.glLineWidth(5f);
	    GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2d(Math.cos(angle)+oldx,Math.sin(angle)+oldy);
		GL11.glVertex2d(Math.cos(angle)*50+oldx,Math.sin(angle)*50+oldy);
	    GL11.glEnd();
	    st.glCircle3i(tcx, tcy, tcr);//*/
	    double secs2 = 360/steer;
		double circ2 = speed*secs2;
		double radius2 = (circ2/Math.PI)/2;
		if(timetaken>secstocompletefortcirc){
			constantspeed = false;
		}
		if ((speed*secstocompletefortcirc)+((distancetravelled/timetaken)*timetaken)/(timetaken+secstocompletefortcirc)<(avgspeedfortcirc)&&calculate){
		/*if(((speed)<avgspeedfortcirc+avgspeedfortcirc/30)&&((speed)>avgspeedfortcirc-avgspeedfortcirc/30)&&calculate){
			avgspeedfortcirc = distancefortcirc/secstocompletefortcirc;
			braking = ((speed-(avgspeedfortcirc*2-speed))/secstocompletefortcirc)*3;
			if(((speed)<avgspeedfortcirc+avgspeedfortcirc/30)&&((speed)>avgspeedfortcirc-avgspeedfortcirc/30)){
				calculate = false;
			}*/
			
			
			constantspeed = true;
			braking = 0;
			calculate = false;
			
		}
		double centerx2,centery2,centerx3,centery3;
		if(directionto>angle){
			centerx2 = Math.cos(angle+Math.PI/2)*radius2+x;
			centery2 = Math.sin(angle+Math.PI/2)*radius2+y;
		}else {
			centerx2 = Math.cos(angle-Math.PI/2)*radius2+x;
			centery2 = Math.sin(angle-Math.PI/2)*radius2+y;
		}
		directionto = Start.dirto(x, y, currentnode.x, currentnode.y);
		
		if((directionto- angle)>Math.PI/180||(directionto-angle)<-Math.PI/180){
			recadjustangle(directionto,0);
			if(directionto>angle){
				angle += Math.min(Math.toRadians(steer*((double)st.getDelta())/1000f), Math.abs(directionto-angle));
			}else{
				angle -= Math.min(Math.toRadians(steer*((double)st.getDelta())/1000f), Math.abs(directionto-angle));
			}
		}
		if(braking!=0){
			speed = Math.max(speed-braking*((double)st.getDelta())/1000f,0);
			if(speed == 0){
				braking = 0;
			}
		} else{
			if(!constantspeed){
				speed = Math.min(maxspeed, speed+(((double)st.getDelta())*w/1000)*0.8);
			}
		}
		distancetravelled += speed*((double)st.getDelta())/1000f;
		timetaken += ((double)st.getDelta())/1000f;
		distancefortcirc -= speed*((double)st.getDelta())/1000f;
		secstocompletefortcirc -= ((double)st.getDelta())/1000f;
		x = x + (double)Math.cos(angle)*speed*((double)st.getDelta())/1000f;
		y = y + (double)Math.sin(angle)*speed*((double)st.getDelta())/1000f;
		if(speed > maxspeed){
			//System.out.println("Somethings funny");
			//System.out.println(toString());
			speed = maxspeed;
		}
		if(x>st.screenwidth) x=0;
		if(y>st.screenheight) y=0;
		if(x<0) x = st.screenwidth;
		if(y<0) y = st.screenheight;
		checkCollisions();
		/*Vector2f position = super.GetPosition();
		Vector2f screenSize = new Vector2f(768,1024);
		while (position.x > screenSize.x) 
        { position.x -= screenSize.x; }
        while (position.y > screenSize.y) 
        { position.y -= screenSize.y; }
        while (position.x < 0) 
        { position.x += screenSize.x; }
        while (position.y < 0) 
        { position.y += screenSize.y; }
        super.SetLocation(position, super.GetAngle());*/
	}
	public void updateBuckets(){

		if(x>bright){
			Bucket cb = st.buckets.next(1, currentbucket);
			if(cb!=null){
				currentbucket.removeVehicle(this);
				currentbucket = cb;
				currentbucket.addVehicle(this);
			}
			checksides();
		}
		if(x<bleft){
			Bucket cb = st.buckets.next(3, currentbucket);
			if(cb!=null){
				currentbucket.removeVehicle(this);
				currentbucket = cb;
				currentbucket.addVehicle(this);
			}
			checksides();
		}
		if(y>btop){
			Bucket cb = st.buckets.next(2, currentbucket);
			if(cb!=null){
				currentbucket.removeVehicle(this);
				currentbucket = cb;
				currentbucket.addVehicle(this);
			}
			checksides();
		}
		if(y<bbottom){
			//System.out.println("Moving bucket to the bottom :)");
			Bucket cb = st.buckets.next(0, currentbucket);
			if(cb!=null){
				currentbucket.removeVehicle(this);
				currentbucket = cb;
				currentbucket.addVehicle(this);
			}
			checksides();
		}
	}
	public void checksides(){
		btop = currentbucket.topSide();
		bright = currentbucket.rightSide();
		bbottom = currentbucket.bottomSide();
		bleft = currentbucket.leftSide();
		sbuckets = st.buckets.surroundBuckets(currentbucket);
	}
	public boolean checkCollisions(){
		boolean collided = false;
		colcirc1 = false;
		colcirc2 = false;
		colcirc3 = false;
		for(Bucket b: sbuckets){
			if(b!=null ){
				ArrayList<Vehicle> vhcls = b.getVehicles();
				for(Vehicle a: vhcls){
					
					if(this.ID!=a.ID&&this.hasCollided(a)){
						//System.out.println(this.shortString() + " collided with " + a.shortString());
						collided = true;
						//braking = 543;
						calculate = false;
						
						
					}
				}
			}
		}
		if(!collided){
			if(braking==543||braking==203||braking==103){
				braking = 0;
				colcirc1 = false;
				colcirc2 = false;
				colcirc3 = false;
			}
		}
		return true;
	}
	public boolean hasCollided(Vehicle b){
		if(checkedcollisions.contains(b))
			return false;
		//double tl = (float)(v.x + ( v.w / 2 ) * Math.cos( v.angle )- ( v.h / 2 ) * Math.sin( v.angle ))
		//if(st.distance(this.x,this.y, b.x, b.y)>this.w*10){
		//	return false;
		//}
		
		Rectangle them = new Rectangle((float)b.x, (float)b.y, (float)b.w, (float)b.h);
		them.transform(Transform.createRotateTransform(((float)b.angle)));
		if(them.contains((float)forwardpoint1.x,(float)forwardpoint1.y)){
			colcirc1 = true;
			braking = 103;
			return true;
		
		}
		if(them.contains((float)forwardpoint2.x,(float)forwardpoint2.y)){
			braking = 203;
			colcirc2 = true;
			return true;
		
		}
		if(them.contains((float)forwardpoint3.x,(float)forwardpoint3.y)){
			braking = 543;
			colcirc3 = true;
			return true;
		
		}
		Rectangle us = new Rectangle((float)x, (float)y, (float)w, (float)h);
		us.transform(new Transform().createRotateTransform(((float)angle)));
		if(us.intersects(them)){
			if(b.speed!=0){
				speed = 0;
			}else{
				braking = 0;
			}
			return true;
		}
		this.checkedcollisions.add(b);
		b.checkedcollisions.add(this);
		return false;
	}
	public void recadjustangle(double dirto, double ang){
		angle +=ang;
		if(Math.abs(dirto-angle)>Math.PI){
			if(dirto>angle){
				recadjustangle(dirto,(double)Math.PI*2);
			} else{
				recadjustangle(dirto,(double)-Math.PI*2);
			}
		}
	}
	public void calculatetcirc(double directionto){
		calculate = true;
		//System.out.println("We can't seem to be able to turn in time.");
		Mtocentercirc = Math.sin(angle+addninety)/Math.cos(angle+addninety); // Slope of the line from us to the center of the circle, our angle is a tangent of the circle, so adding 90 degrees, (Math.PI/4) makes it a line that intersects the circle
		Ctocentercirc = y-(Mtocentercirc*x); // y = mx + c therefore c = y-mx
		double hyp = Start.distance(x, y, currentnode.x, currentnode.y)/2; // this is the length from us to the line that runs between us and our destination along which any point is equidistant from us and the point we are going
		Mmiddleline = Math.sin(directionto+addninety)/Math.cos(directionto+addninety); // the slope of the line perp to the prev line ^^
			Cmiddleline = (Math.sin(directionto)*hyp+y)-(Mmiddleline*(Math.cos(directionto)*hyp+x));
		// Now that we have two lines that run through the center of our optimal circle, we need to find the intersection of these two lines to find the center.
		// m1x + c1 = m2x + c2;
		// x(m1 - m2) = c2 - c1;
		// x = (c2 - c1)/(m1-m2);
		double circx = (Ctocentercirc - Cmiddleline) /(Mmiddleline - Mtocentercirc);
		tcx = circx;
		// y = mx + c
		double circy = Mmiddleline*circx + Cmiddleline;
		tcy = circy;
		double circr = Start.distance(x, y, circx, circy);
		tcr = circr;
		// Now we need to figure out the angle between them
		// |\
		// |A\
		// |  \  r (from earlier that we calculated
	  //hyp|   \ 
		// |    \
		// ------
		// The point that A represents is the center of the circle, r is the radius
		//  angle A is half of the angle between our 2 points and the center of the circle
		double ourangle = Math.asin(hyp/circr)*2; //angle A*2
		double percentageofcirc = ourangle/(Math.PI*2); // percentage of the whole circle that ourangleis
		double circcirc = Math.PI*circr*2; // the circumfrence of this circle
		distancefortcirc = circcirc*percentageofcirc; // and the distance around the circle we need to travel
		secstocompletefortcirc = Math.toDegrees(ourangle)/steer; // time to get to the next point
		avgspeedfortcirc = distancefortcirc/secstocompletefortcirc; // therefore the speed we need to be going at
		braking = ((speed-(avgspeedfortcirc*2-speed))/secstocompletefortcirc)*2;
		//braking = w*50;
		distancetravelled = 0;
		timetaken = 0;
		if(braking<0){
			braking = 0;
			/*System.out.println("Mtocentercirc = " + Mtocentercirc);
			System.out.println("CtoCentercirc = " + Ctocentercirc);
			System.out.println("hyp = " + hyp);
			System.out.println("Mmiddleline = " + Mmiddleline);
			System.out.println("Cmiddleline = " + Cmiddleline);
			System.out.println("circx = " + circx);
			System.out.println("circy = " + circy);
			System.out.println("circr = " + circr);
			System.out.println("ourangle = " + ourangle);
			System.out.println("percentageofcirc = " + percentageofcirc);
			System.out.println("circcirc = " + circcirc);
			System.out.println("distance = " + distancefortcirc);
			System.out.println("secstocomplete = " + secstocompletefortcirc);
			System.out.println("avgspeed = " + avgspeedfortcirc);
			System.out.println("speed = " + speed);
			System.out.println("braking = " + braking);
			System.out.println("That means that we have " + secstocompletefortcirc + " seconds to go " + distancefortcirc + " pixels.\n With a currentnode speed of " + speed +"pixels/second that means we have to brake at a speed of " + braking + " pixels per second");
*/
		}
	}
	public String shortString(){
		String returnstr = "The car " + ID +" is at " + x+ ","+y+ "\n";
		returnstr += "Speed: " + ((speed/w)/0.122) + "mp/h\n";
		returnstr += "Heading: " + Math.toDegrees(angle) + " degrees\n";
		return returnstr;
	}
	public String toString(){
		String returnstr = "The car " + ID +" is at " + x+ ","+y+ "\n";
		returnstr += "Heading towards " + currentnode.x +"," +currentnode.y + "\n";
		returnstr += "Speed: " + ((speed/w)/0.122) + "mp/h\n";
		returnstr += "Heading: " + Math.toDegrees(angle) + " degrees\n";
		returnstr += "Turning circle:" + (Math.cos(angle+Math.PI/4)*radius+x) + "," + (Math.sin(angle+Math.PI/4)*radius+y)+ "\n";
		returnstr += "	radius " + radius + "\n";
		returnstr += "Top speed:"  + ((maxspeed/w)/0.122) + "mp/h \n";
		returnstr += ((braking!=0)?("Braking at a speed of "   + ((braking/w)/0.122) + "mp/h"):((!constantspeed)?("Accelerating at a speed of " + ((double)((1f/w)/0.122f)) + "Mp/h"):"Constant speed")) + "\n";
		return returnstr;
	}
	public double getAngle() {
		return angle;
	}
	public void setAngle(double angle) {
		this.angle = angle;
	}
	
}
