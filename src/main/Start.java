package main;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.lwjgl.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
//import org.lwjgl.opengl.GL


public class Start {
	double lastFPS,fps;
	double avgfps,secs;
	int screenwidth,screenheight;
	ArrayList<Vehicle> vehicles,removedvehicles;
	Vehicle v;
	Node startnode;
	ArrayList<Node> alreadydrawn,startingnodes;
	boolean adown,sdown,wdown,ddown;
	long lastFrame;
	int delta;
	Waypoints wp;
	Bucketset buckets;
	URL pngURL;
	boolean leftHeld = false, rightHeld = false;
    boolean upHeld = false, downHeld = false;

    //vehicle controls
    float steering = 0; //-1 is left, 0 is center, 1 is right
    float throttle = 0; //0 is coasting, 1 is full throttle
    float brakes = 0; //0 is no brakes, 1 is full brakes
	public static double distance(double x1,double y1,double x2,double y2){
		return (double)Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
	}
	public static double dirto(double x1,double y1,double x2,double y2){
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
	private void ProcessInput()
    {
        if (leftHeld)
            steering = -1;
        else if (rightHeld)
            steering = 1;
        else
            steering = 0;

        if (upHeld)
            throttle = 1;
        else
            throttle = 0;

        if (downHeld)
            brakes = 1;
        else
            brakes = 0;
    }
    public void start() {


		alreadydrawn = new ArrayList<Node>();
		startingnodes = new ArrayList<Node>();
        try {
        	DisplayMode[] modes = Display.getAvailableDisplayModes();
        	DisplayMode current = null;
        	DisplayMode display = null;
        	long maxpixels = 0;
        	for (int i=0;i<modes.length;i++) {
        	    current = modes[i];
        	    int pixels = current.getWidth()*current.getHeight();
        	    //if(pixels > maxpixels){
        	    if(current.getWidth()==1024&&current.getHeight()==768){
        	    	maxpixels = pixels;
        	    	display = current;
            	    //System.out.println(current.getWidth() + "x" + current.getHeight() + "y");
            	    screenwidth = current.getWidth();
            	    screenheight = current.getHeight();
        	    }
        	}
		    Display.setDisplayMode(display);
		    //Display.setFullscreen(true);
		    Display.create();
		} catch (LWJGLException e) {
		    e.printStackTrace();
		    System.exit(0);
		}
        //texturesetup();
    	int[] bucketsizes = {64};
    	int bucketsize = 50;
    	for(int i: bucketsizes){
    		if(screenheight%i==0&&screenwidth%i==0){
    			bucketsize = i;
    			//System.out.println(bucketsize);
    		}
    	}
    	buckets = new Bucketset(screenwidth/bucketsize,screenheight/bucketsize,screenwidth,screenheight);
    	int xcounter = 0, ycounter = 0;
    	System.out.println("screen height " + screenheight/bucketsize + " screen width " + screenwidth/bucketsize);
    	
		for(int y = 0;y<screenheight;y=y+bucketsize){
			for(int x = 0;x<screenwidth;x=x+bucketsize){
    			Bucket b = new Bucket(x+bucketsize/2,y+bucketsize/2,bucketsize,bucketsize);
    			buckets.add(b);
    		}
    	}
    	updatedelta();
    	//wp = new Waypoints("testfile.txt");
    	
    	//for(double r = 0;r < Math.PI*2; r+= Math.PI/45){
    		
    	//	wp.add(new Node(Math.cos(r)*350+370,Math.sin(r)*350+370,0));
    	//}
    	wp = new Waypoints("myfile.txt");
    	ArrayList<Factory> factories = new ArrayList<Factory>();
    	for(Object a : wp){
			//System.out.println(a);
			Node startpoint = (Node) ((Entry)a).getValue();
			if(!startpoint.hasParents()){
				startingnodes.add(startpoint);
				Factory f = new Factory(this,wp);
		    	f.direction = (double)Math.PI/2f;
		    	f.x = startpoint.x;
		    	f.y = startpoint.y;
		    	f.frequency = 0.1;
		    	f.modelvehicles.add(new Car(0,0,10,5,0,88,new Color(0.8f,0.8f,0.2f),wp,this,startpoint,50));
		        factories.add(f);
			}
		}

		if (startingnodes.size()==0){
			Node startpoint = wp.get(1);
			startingnodes.add(startpoint);
			Factory f = new Factory(this,wp);
	    	f.direction = (double)Math.PI/2f;
	    	f.x = startpoint.x;
	    	f.y = startpoint.y;
	    	f.frequency = 5;
	    	f.modelvehicles.add(new Car(0,0,10,5,0,88,new Color(0.8f,0.8f,0.2f),wp,this,startpoint,50));
	        factories.add(f);
		}
    	//Factory f = new Factory(this,wp);
    	//f.direction = (double)Math.PI/2f;
    	/*f.x = 292;
    	f.y = 652;
    	f.frequency = 0.5f;
    	f.modelvehicles.add(new Car(0,0,10,5,0,88,new Color(0.4f,0.8f,0.2f),wp,this,50));
        */
		vehicles = new ArrayList<Vehicle>();
		removedvehicles = new ArrayList<Vehicle>();
        
        lastFPS = getTime();
     // init OpenGL
    	GL11.glMatrixMode(GL11.GL_PROJECTION);
    	GL11.glLoadIdentity();
    	GL11.glOrtho(0, screenwidth, 0, screenheight, 1, -1);
    	GL11.glMatrixMode(GL11.GL_MODELVIEW);
    	double deltacounter = 0;
	    while (!Display.isCloseRequested()) {
	    // render OpenGL here
	    	// Clear the screen and depth buffer
		    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);	
		    ProcessInput();
		    //apply vehicle controls

		    updateFPS();
		    updatedelta();
		    deltacounter += getDelta()/1000f;
		    if(deltacounter>1){
		    	for(Bucket b: buckets){
		    		if(b.x>=0&&b.x<bucketsize){
		    			//System.out.println(b.vhcls.size());
		    		}else{
		    			//System.out.print(b.vhcls.size());
		    		}
		    	}
		    	deltacounter=0;
		    }
		    //drawBuckets();
		    drawLines();
		    drawCars();
		    for(Factory y: factories){
		    	y.update();
		    }
		    pollInput();
		    boolean check = true;
		    while(check){
		    	try{
				    for(Vehicle v: vehicles){
			        	if(v!=null){
			        		v.update();
			        	}
			        }
				    check = false;
		    	}catch(Exception e){
		    		e.printStackTrace();
		    	}
	    	}
		    //System.out.println(((double)getDelta())/1000f);
		    Display.update();
		    for(Vehicle v: removedvehicles){
		    	vehicles.remove(v);
		    }
		    removedvehicles.clear();
		   //Display.sync(240);
		}
	
		Display.destroy();
    }
    /**
     * Get the time in milliseconds
     * 
     * @return The system time in milliseconds
     */
    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
    public void updatedelta(){
    	long time = getTime();
        int d = (int) (time - lastFrame);
        lastFrame = time;
        this.delta = d;
    }
    public int getDelta() {
        
        return this.delta*2;
    }
    /**
     * Calculate the FPS and set it in the title bar
     */
    public void updateFPS() {
        if (getTime() - lastFPS > 1000) {
            Display.setTitle("FPS: " + Math.round(avgfps)); 
            secs++;
            avgfps = (avgfps*secs+fps)/(secs+1);
            //System.out.println(avgfps);
            //System.out.println(secs);
            //System.out.println(fps);
            fps = 0; //reset the FPS counter
            lastFPS += 1000; //add one second
        }
        fps++;
    }
    public void pollInput() {
		
        if (Mouse.isButtonDown(0)) {
		    int x = Mouse.getX();
		    int y = Mouse.getY();
				
		    //System.out.println("MOUSE DOWN @ X: " + x + " Y: " + y);
		}
			
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
		    //System.out.println("SPACE KEY IS DOWN");
		}
			
		while (Keyboard.next()) {
		    if (Keyboard.getEventKeyState()) {
		        if (Keyboard.getEventKey() == Keyboard.KEY_A) {
				    //System.out.println("A Key Pressed");
				    leftHeld = true;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_S) {
				    //System.out.println("S Key Pressed");
				    downHeld = true;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_D) {
				    //System.out.println("D Key Pressed");
				    rightHeld = true;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_W) {
				    //System.out.println("W Key Pressed");
				    upHeld = true;
				}
		    } else {
		        if (Keyboard.getEventKey() == Keyboard.KEY_A) {
		        	//System.out.println("A Key Released");
		        	leftHeld = false;
		        }
		    	if (Keyboard.getEventKey() == Keyboard.KEY_S) {
		    		//System.out.println("S Key Released");
		    		downHeld = false;
		    	}
				if (Keyboard.getEventKey() == Keyboard.KEY_D) {
				    //System.out.println("D Key Released");
				    rightHeld = false;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_W) {
				    //System.out.println("W Key Released");
				    upHeld = false;
				}
		    }
		}
    }
    void glCircle3i(double x, double y, double radius) { 
        double angle; 
        GL11.glPushMatrix(); 
        GL11.glLoadIdentity(); 
        GL11.glDisable(GL11.GL_TEXTURE_2D); 
        //GL11.glColor3f(0.0f, 1.0f, 0.0f); 
        GL11.glLineWidth(5.0f); 
        GL11.glBegin(GL11.GL_LINE_LOOP); 
        for(int i = 0; i < 100; i++) { 
            angle = i*2*Math.PI/100; 
            GL11.glVertex2d(x + (Math.cos(angle) * radius), y + (Math.sin(angle) * radius)); 
        } 
        GL11.glEnd(); 
        GL11.glEnable(GL11.GL_TEXTURE_2D); 
        GL11.glPopMatrix(); 
    }  
    public void drawBuckets(){
    	for(Bucket b:buckets){
    		GL11.glColor3f(0.5f,0.5f,0.5f);
    		GL11.glLineWidth(3f);
    	    GL11.glBegin(GL11.GL_LINES);
    	    int lside = b.leftSide();
    	    int rside = b.rightSide();
    	    int tside = b.topSide();
    	    int bside = b.bottomSide();
    	    
    	    GL11.glVertex2d(lside,tside);
    	    GL11.glVertex2d(rside,tside);
    	   
    	    GL11.glEnd();
    	    GL11.glBegin(GL11.GL_LINES);
    	    GL11.glVertex2d(rside,tside);
    	    GL11.glVertex2d(rside,bside);
    	    GL11.glEnd();
    	    GL11.glBegin(GL11.GL_LINES);
    	    GL11.glVertex2d(rside,bside);
    	    GL11.glVertex2d(lside,bside);
    	    GL11.glEnd();
    	    GL11.glBegin(GL11.GL_LINES);
    	    GL11.glVertex2d(lside,bside);
    	    GL11.glVertex2d(lside,tside);
    	    GL11.glEnd();
    	    if(b.hasVehicles()){
    	    	GL11.glBegin(GL11.GL_QUADS);
        	    GL11.glVertex2d(lside,tside);
        	    GL11.glVertex2d(rside,tside);
        	    GL11.glVertex2d(rside,bside);
        	    GL11.glVertex2d(lside,bside);
    	    }
    	}
    }
    public void drawLines(){
    	for(Object a1: wp){
    		Entry a = (Entry) a1;
    		Node n =  (Node)a.getValue();
    		//font2.drawString((float)a.x,(float) a.y,a.ID+"");
    		//System.out.println(a.ID);
    		//GL11.glDisable(GL11.GL_TEXTURE_2D);
    	}
    	if(startingnodes.size()!=0){
    		for(Node startnode: startingnodes)
    			if(startnode!=null)
    				recDraw(startnode);
	    	alreadydrawn.clear();
    	}
    }
    public void recDraw(Node n){
    	if(!alreadydrawn.contains(n)){
    		alreadydrawn.add(n);
    		//GL11.glColor3f(0f,1f,0f);
    		//glCircle3i(n.x,n.y,25);
	    	for(Node a:n.children){
				drawRoad(n.x,n.y,a.x,a.y);
				recDraw(a);
			}
    	}
    }
    public void drawRoad(double x1, double y1,double x2,double y2){
    	GL11.glColor3f(0.5f,0.5f,0.5f);
		GL11.glLineWidth(100f);
	    GL11.glBegin(GL11.GL_LINES);
	    GL11.glVertex2d(x1,y1);
		GL11.glVertex2d(x2,y2);
	    GL11.glEnd();
	    
	    GL11.glColor3f(1f,1f,1f);
		GL11.glLineWidth(3f);
	    double dist = distance(x1,y1,x2,y2);
	    double angle = dirto(x1,y1,x2,y2);
	    double currentx = x1;
	    double currenty = y1;
	    double finx,finy;
	    for(double i=0;i<dist;i+=20){
	    	finx = currentx+Math.cos(angle)*5;
	    	finy = currenty+Math.sin(angle)*5;
	    	GL11.glBegin(GL11.GL_LINES);
		    GL11.glVertex2d(currentx,currenty);
			GL11.glVertex2d(finx,finy);
		    GL11.glEnd();
		    currentx = currentx+Math.cos(angle)*20;
		    currenty = currenty+Math.sin(angle)*20;
	    }
    }
    public void drawCars(){
    	GL11.glColor3f(0.5f,0.5f,0.5f);
		GL11.glLineWidth(10f);
	    GL11.glBegin(GL11.GL_LINES);
    	for(Object n1:wp){
    		Node n = (Node) ((Entry)n1).getValue();
    		//System.out.println(n);
		    /*GL11.glVertex2d(n.x+2f,n.y+2f);
			GL11.glVertex2d(n.x-2,n.y+2);
			GL11.glVertex2d(n.x-2,n.y-2);
			GL11.glVertex2d(n.x+2,n.y-2);*/
    		//System.out.println(GL11.glGetFloat(GL11.GL_LINE_WIDTH_RANGE));
			//GL11.glVertex2d(n.x,n.y);
			//GL11.glVertex2d(wp.next(n).x,wp.next(n).y);
    	}
	    GL11.glEnd();
		
	    

    	for(Vehicle v:vehicles){
    		// draw quad
    		if(v!=null){
    			GL11.glColor4f((float)v.col.getRed()/255,(float)v.col.getGreen()/255,(float)v.col.getBlue()/255,((v.col.getAlpha())/255f));
    			GL11.glBegin(GL11.GL_QUADS);
			    GL11.glVertex2f((float)(v.x + ( v.w / 2 ) * Math.cos( v.angle )- ( v.h / 2 ) * Math.sin( v.angle )),(float)(  v.y + ( v.h / 2 ) * Math.cos (v.angle)  + ( v.w / 2 ) * Math.sin (v.angle)));
				GL11.glVertex2f((float)(v.x - ( v.w / 2 ) * Math.cos( v.angle )- ( v.h / 2 ) * Math.sin( v.angle )),(float)(  v.y + ( v.h / 2 ) * Math.cos (v.angle)  - ( v.w / 2 ) * Math.sin (v.angle)));
				GL11.glVertex2f((float)(v.x - ( v.w / 2 ) * Math.cos( v.angle )+ ( v.h / 2 ) * Math.sin( v.angle )),(float)(  v.y - ( v.h / 2 ) * Math.cos (v.angle)  - ( v.w / 2 ) * Math.sin (v.angle)));
				GL11.glVertex2f((float)(v.x + ( v.w / 2 ) * Math.cos( v.angle )+ ( v.h / 2 ) * Math.sin( v.angle )),(float)(  v.y - ( v.h / 2 ) * Math.cos (v.angle)  + ( v.w / 2 ) * Math.sin (v.angle)));
			    GL11.glEnd();
			    
    		}
    	}
    }
    public void removeVehicle(Vehicle vehicle){
    	removedvehicles.add(vehicle);
    }
    public static void main(String[] argv) {
        Start st = new Start();
        st.start();
    }
}