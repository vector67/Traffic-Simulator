package main;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Map.Entry;

import main.Bucket;
import main.Bucketset;
import main.Button;
import main.Car;
import main.Factory;
import main.Node;
import main.Start;
import main.Vehicle;
import main.Waypoints;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;


public class mapmaker {
	boolean mousedown,mouseclicked;
	int screenwidth,screenheight;
	Node startpoint,mousepoint;
	Node startnode;
	int curID = 0;
	int mousex,mousey;
	long lastFPS,lastFrame;
	double avgfps,secs;
	Waypoints wp;
	ArrayList<Node> alreadydrawn,startingnodes;
	private UnicodeFont font2;
	int fps;
	int delta;
	private ArrayList<Button> buttons;
	public static double distance(double x1,double y1,double x2,double y2){
		return (double)Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
	}
	public void start() {
		startingnodes = new ArrayList<Node>();
		buttons = new ArrayList<Button>();
		buttons.add(new Button(50,50,50,50));
		//startpoint = new Node(0,0,curID);
		curID++;
		wp = new Waypoints("myfile.txt");
		for(Object a : wp){
			System.out.println(a);
			startpoint = (Node) ((Entry)a).getValue();
			if(!startpoint.hasParents()){
				startingnodes.add(startpoint);
			}
		}
		startingnodes.add(wp.get(1));
		//System.out.println(startpoint);
		alreadydrawn = new ArrayList<Node>();
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
		/*try{
			Font f = new Font("verdana.ttf", Font.PLAIN, 12);
			String fontPath = "verdana.ttf";
			font2 = new UnicodeFont(f);
			font2.addAsciiGlyphs();
			font2.addGlyphs("><1234567890X");
			font2.getEffects().add(new ColorEffect(java.awt.Color.black));
			font2.loadGlyphs();
			
		} catch (Exception e) {
			e.printStackTrace();
		}*/
    	updatedelta();
        
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
		    GL11.glColor3f(1,1,1);
			GL11.glBegin(GL11.GL_QUADS);
		    GL11.glVertex2f(0,0);
		    GL11.glVertex2f(0,screenheight);
		    GL11.glVertex2f(screenwidth,screenheight);
		    GL11.glVertex2f(screenwidth,0);		    
		    GL11.glEnd();
		    //apply vehicle controls
		    updateFPS();
		    updatedelta();
		    deltacounter += getDelta()/1000f;
		    drawLines();
		    pollInput();
		    drawButtons();
		    if(mouseclicked){
		    	//System.out.println("mouse clicked at " + mousex + "," + mousey);
		    }
		    //System.out.println(((double)getDelta())/1000f);
		    Display.update();
		    Display.sync(30);
		}
	    wp.save("myfile.txt");
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
            fps = 0; //reset the FPS counter
            lastFPS += 1000; //add one second
        }
        fps++;
    }
    public void drawButtons(){
    	for(Button b: buttons){
    		GL11.glColor3f(0.3f,0.2f, 0.1f);
			GL11.glBegin(GL11.GL_QUADS);
		    GL11.glVertex2f((float)(b.x + ( b.width / 2 )) ,(float)(  b.y + ( b.height / 2 ) ));
			GL11.glVertex2f((float)(b.x - ( b.width / 2 )) ,(float)(  b.y + ( b.height / 2 ) ));
			GL11.glVertex2f((float)(b.x - ( b.width / 2 )) ,(float)(  b.y - ( b.height / 2 ) ));
			GL11.glVertex2f((float)(b.x + ( b.width / 2 )) ,(float)(  b.y - ( b.height / 2 ) ));
		    GL11.glEnd();
    	}
    }
    public void mouseHeld(){
    	for(Button b: buttons){
    		if(b.isPointIn(mousex,mousey)){
    			return;
    		}
    	}
    	if(mousepoint!=null){
    		mousepoint.x = mousex;
    		mousepoint.y = mousey;
    	}else{
    		if(distance(mousex,mousey,startpoint.x,startpoint.y)>15){
	    		if(mousepoint==null){
	    			mousepoint = new Node(mousex,mousey,curID);
		    		curID++;
		          	startpoint.addChild(mousepoint);
	    			//System.out.println("addchild to:" +startpoint);
	    		} 
	          	//System.out.println("startpoint" + startpoint);
	          	//System.out.println("startnode" + startnode);
    		}
    	}
    }
    public void mouseReleased(){
    	for(Button b: buttons){
    		if(b.isPointIn(mousex,mousey)){
    			return;
    		}
    	}
    	boolean foundone = false;
    	Node foundnode = null;
    	for(Object n1: wp){
    		Entry n2 = (Entry) n1;
    		Node n = (Node) n2.getValue();
    		if(distance(mousex,mousey,n.x,n.y)<25){
    			if(distance(startpoint.x,startpoint.y,n.x,n.y)>25){
    				foundnode = n;
    				foundone = true;
    				startpoint.removeChild(mousepoint);
    				startpoint.addChild(foundnode);
    				startpoint = foundnode;
    				mousepoint = null;
    			}
    		}
    	}
    	if(mousepoint!= null){
    		startpoint = mousepoint.clone();
    		wp.add(startpoint.ID, startpoint);
    	}
    	mousepoint = null;
    }
    public void mousePressed(){
    	boolean foundone = false;
    	Node foundnode = null;
    	mousepoint = null;
    	for(Button b: buttons){
    		System.out.println(mousex + "," + mousey);
    		if(b.isPointIn(mousex,mousey)){
    			b.clicked(this);
    			return;
    		}
    	}
    	System.out.println("mousepressed");
    	for(Object n1: wp){
    		Entry n2 = (Entry) n1;
    		Node n = (Node) n2.getValue();
    		if(distance(mousex,mousey,n.x,n.y)<25){
    			foundnode = n;
    			foundone = true;
    		}
    	}
    	if(!foundone){
    		Node newstpt = new Node(mousex,mousey,curID);
    		curID++;
    		wp.add(newstpt.ID, newstpt);
    		if(startpoint==null){
    			startpoint = newstpt;
    			startingnodes.add(startpoint);
    		}else{
    			startpoint.addChild(newstpt);
    			//System.out.println("inside mouspressed addchild to:" +startpoint);
    		}
	    	startpoint = newstpt;
    	}else{
    		startpoint = foundnode;
    		mousepoint = new Node(mousex,mousey,curID);
    		curID++;
          	startpoint.addChild(mousepoint);
          	//System.out.println("found already node" + startpoint);
    	}

    }
    public void pollInput() {
		mouseclicked = false;
        if (Mouse.isButtonDown(0)) {
        	mousex = Mouse.getX();
		    mousey = Mouse.getY();
		    if(mousedown==false){
				mousePressed();
			    //System.out.println("MOUSE DOWN @ X: " + mousex + " Y: " + mousey);
		    }
		    mouseHeld();
		    mousedown=true;
		}else{
			if(mousedown==true){
				//System.out.println("Mouse released");
				mouseclicked = true;
				mousedown=false;
				mouseReleased();
			}
		}
			
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
		    //System.out.println("SPACE KEY IS DOWN");
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
    		glCircle3i(n.x,n.y,25);
	    	for(Node a:n.children){
				drawRoad(n.x,n.y,a.x,a.y);
				recDraw(a);
			}
    	}
    }
    void glCircle3i(double x, double y, double radius) { 
        double angle; 
        GL11.glPushMatrix(); 
        GL11.glLoadIdentity(); 
        GL11.glDisable(GL11.GL_TEXTURE_2D); 
        GL11.glColor3f(0.0f, 1.0f, 0.0f); 
        GL11.glLineWidth(1f); 
        GL11.glBegin(GL11.GL_LINE_LOOP); 
        for(int i = 0; i < 100; i++) { 
            angle = i*2*Math.PI/100; 
            GL11.glVertex2d(x + (Math.cos(angle) * radius), y + (Math.sin(angle) * radius)); 
        } 
        GL11.glEnd(); 
        GL11.glEnable(GL11.GL_TEXTURE_2D); 
        GL11.glPopMatrix(); 
    } 
    public void drawRoad(double x1, double y1,double x2,double y2){
    	GL11.glColor3f(0f,0f,0f);
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
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		mapmaker st = new mapmaker();
        st.start();
	}

}
