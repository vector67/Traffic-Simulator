package main;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;

public class Start implements SimulationContext, VehicleRegistry {

    // --- FPS / timing ---
    double lastFPS, fps;
    double avgfps, secs;
    long lastFrame;
    int delta;

    // --- Display ---
    int screenwidth, screenheight;

    // --- Simulation state ---
    ArrayList<Vehicle> vehicles, removedvehicles;
    ArrayList<Node> alreadydrawn, startingnodes;
    Waypoints wp;
    Bucketset buckets;
    ArrayList<Factory> factories;

    // --- Input state ---
    boolean leftHeld = false, rightHeld = false;
    boolean upHeld = false, downHeld = false;
    float steering = 0;
    float throttle = 0;
    float brakes = 0;

    // -----------------------------------------------------------------------
    // SimulationContext
    // -----------------------------------------------------------------------

    @Override
    public int getDelta() {
        return this.delta * 2;
    }

    @Override
    public int getScreenWidth() {
        return screenwidth;
    }

    @Override
    public int getScreenHeight() {
        return screenheight;
    }

    @Override
    public Bucketset getBuckets() {
        return buckets;
    }

    @Override
    public void removeVehicle(Vehicle vehicle) {
        removedvehicles.add(vehicle);
    }

    // -----------------------------------------------------------------------
    // VehicleRegistry
    // -----------------------------------------------------------------------

    @Override
    public void addVehicle(Vehicle v) {
        vehicles.add(v);
    }

    @Override
    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    // -----------------------------------------------------------------------
    // Delegating static math utilities (callers that still use Start.distance)
    // -----------------------------------------------------------------------

    public static double distance(double x1, double y1, double x2, double y2) {
        return SimulationMath.distance(x1, y1, x2, y2);
    }

    public static double dirto(double x1, double y1, double x2, double y2) {
        return SimulationMath.dirto(x1, y1, x2, y2);
    }

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------

    public void start() {
        initDisplay();
        initSimulation();

        lastFPS = getTime();
        while (!Display.isCloseRequested()) {
            ProcessInput();
            updateFPS();
            tickLogic();
            tickRender();
        }
        Display.destroy();
    }

    // -----------------------------------------------------------------------
    // Initialisation
    // -----------------------------------------------------------------------

    private void initDisplay() {
        try {
            DisplayMode[] modes = Display.getAvailableDisplayModes();
            DisplayMode display = null;
            for (DisplayMode current : modes) {
                if (current.getWidth() == 1024 && current.getHeight() == 768) {
                    display = current;
                    screenwidth  = current.getWidth();
                    screenheight = current.getHeight();
                }
            }
            Display.setDisplayMode(display);
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, screenwidth, 0, screenheight, 1, -1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    private void initSimulation() {
        alreadydrawn   = new ArrayList<Node>();
        startingnodes  = new ArrayList<Node>();
        vehicles       = new ArrayList<Vehicle>();
        removedvehicles = new ArrayList<Vehicle>();
        factories      = new ArrayList<Factory>();

        int bucketsize = 50;
        int[] bucketsizes = {64};
        for (int i : bucketsizes) {
            if (screenheight % i == 0 && screenwidth % i == 0) {
                bucketsize = i;
            }
        }
        buckets = new Bucketset(screenwidth / bucketsize, screenheight / bucketsize, screenwidth, screenheight);
        System.out.println("screen height " + screenheight / bucketsize + " screen width " + screenwidth / bucketsize);
        for (int y = 0; y < screenheight; y += bucketsize) {
            for (int x = 0; x < screenwidth; x += bucketsize) {
                buckets.add(new Bucket(x + bucketsize / 2, y + bucketsize / 2, bucketsize, bucketsize));
            }
        }

        updatedelta();
        wp = new Waypoints("myfile.txt");

        for (Object a : wp) {
            Node startpoint = (Node) ((Entry) a).getValue();
            if (!startpoint.hasParents()) {
                startingnodes.add(startpoint);
                Factory f = new Factory(this, this, wp);
                f.direction = Math.PI / 2;
                f.x = startpoint.x;
                f.y = startpoint.y;
                f.frequency = 0.1;
                f.modelvehicles.add(new Car(0, 0, 10, 5, 0, 88, new Color(0.8f, 0.8f, 0.2f), wp, this, startpoint, 50));
                factories.add(f);
            }
        }

        if (startingnodes.isEmpty()) {
            Node startpoint = wp.get(1);
            startingnodes.add(startpoint);
            Factory f = new Factory(this, this, wp);
            f.direction = Math.PI / 2;
            f.x = startpoint.x;
            f.y = startpoint.y;
            f.frequency = 5;
            f.modelvehicles.add(new Car(0, 0, 10, 5, 0, 88, new Color(0.8f, 0.8f, 0.2f), wp, this, startpoint, 50));
            factories.add(f);
        }
    }

    // -----------------------------------------------------------------------
    // Game loop halves
    // -----------------------------------------------------------------------

    private void tickLogic() {
        updatedelta();
        for (Factory f : factories) {
            f.update();
        }
        pollInput();
        boolean check = true;
        while (check) {
            try {
                for (Vehicle v : vehicles) {
                    if (v != null) v.update();
                }
                check = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (Vehicle v : removedvehicles) {
            vehicles.remove(v);
        }
        removedvehicles.clear();
    }

    private void tickRender() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        drawLines();
        drawCars();
        Display.update();
    }

    // -----------------------------------------------------------------------
    // Input
    // -----------------------------------------------------------------------

    private void ProcessInput() {
        steering = leftHeld ? -1 : rightHeld ? 1 : 0;
        throttle = upHeld   ?  1 : 0;
        brakes   = downHeld ?  1 : 0;
    }

    public void pollInput() {
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) { /* reserved */ }

        while (Keyboard.next()) {
            boolean pressed = Keyboard.getEventKeyState();
            int key = Keyboard.getEventKey();
            if (key == Keyboard.KEY_A) leftHeld  = pressed;
            if (key == Keyboard.KEY_S) downHeld  = pressed;
            if (key == Keyboard.KEY_D) rightHeld = pressed;
            if (key == Keyboard.KEY_W) upHeld    = pressed;
        }
    }

    // -----------------------------------------------------------------------
    // Timing
    // -----------------------------------------------------------------------

    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    public void updatedelta() {
        long time = getTime();
        delta = (int) (time - lastFrame);
        lastFrame = time;
    }

    public void updateFPS() {
        if (getTime() - lastFPS > 1000) {
            Display.setTitle("FPS: " + Math.round(avgfps));
            secs++;
            avgfps = (avgfps * secs + fps) / (secs + 1);
            fps = 0;
            lastFPS += 1000;
        }
        fps++;
    }

    // -----------------------------------------------------------------------
    // Rendering
    // -----------------------------------------------------------------------

    void glCircle3i(double x, double y, double radius) {
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(5.0f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i < 100; i++) {
            double angle = i * 2 * Math.PI / 100;
            GL11.glVertex2d(x + Math.cos(angle) * radius, y + Math.sin(angle) * radius);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    public void drawBuckets() {
        for (Bucket b : buckets) {
            GL11.glColor3f(0.5f, 0.5f, 0.5f);
            GL11.glLineWidth(3f);
            int l = b.leftSide(), r = b.rightSide(), t = b.topSide(), bot = b.bottomSide();
            GL11.glBegin(GL11.GL_LINES); GL11.glVertex2d(l, t); GL11.glVertex2d(r, t); GL11.glEnd();
            GL11.glBegin(GL11.GL_LINES); GL11.glVertex2d(r, t); GL11.glVertex2d(r, bot); GL11.glEnd();
            GL11.glBegin(GL11.GL_LINES); GL11.glVertex2d(r, bot); GL11.glVertex2d(l, bot); GL11.glEnd();
            GL11.glBegin(GL11.GL_LINES); GL11.glVertex2d(l, bot); GL11.glVertex2d(l, t); GL11.glEnd();
            if (b.hasVehicles()) {
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2d(l, t); GL11.glVertex2d(r, t);
                GL11.glVertex2d(r, bot); GL11.glVertex2d(l, bot);
            }
        }
    }

    public void drawLines() {
        if (!startingnodes.isEmpty()) {
            for (Node startnode : startingnodes) {
                if (startnode != null) recDraw(startnode);
            }
            alreadydrawn.clear();
        }
    }

    public void recDraw(Node n) {
        if (!alreadydrawn.contains(n)) {
            alreadydrawn.add(n);
            for (Node a : n.children) {
                drawRoad(n.x, n.y, a.x, a.y);
                recDraw(a);
            }
        }
    }

    public void drawRoad(double x1, double y1, double x2, double y2) {
        GL11.glColor3f(0.5f, 0.5f, 0.5f);
        GL11.glLineWidth(100f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x1, y1); GL11.glVertex2d(x2, y2);
        GL11.glEnd();

        GL11.glColor3f(1f, 1f, 1f);
        GL11.glLineWidth(3f);
        double dist  = SimulationMath.distance(x1, y1, x2, y2);
        double angle = SimulationMath.dirto(x1, y1, x2, y2);
        double cx = x1, cy = y1;
        for (double i = 0; i < dist; i += 20) {
            double fx = cx + Math.cos(angle) * 5;
            double fy = cy + Math.sin(angle) * 5;
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2d(cx, cy); GL11.glVertex2d(fx, fy);
            GL11.glEnd();
            cx += Math.cos(angle) * 20;
            cy += Math.sin(angle) * 20;
        }
    }

    public void drawCars() {
        GL11.glColor3f(0.5f, 0.5f, 0.5f);
        GL11.glLineWidth(10f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glEnd();

        for (Vehicle v : vehicles) {
            if (v != null) {
                GL11.glColor4f((float) v.col.getRed() / 255,
                               (float) v.col.getGreen() / 255,
                               (float) v.col.getBlue() / 255,
                               v.col.getAlpha() / 255f);
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2f((float) (v.x + (v.w / 2) * Math.cos(v.angle) - (v.h / 2) * Math.sin(v.angle)),
                                (float) (v.y + (v.h / 2) * Math.cos(v.angle) + (v.w / 2) * Math.sin(v.angle)));
                GL11.glVertex2f((float) (v.x - (v.w / 2) * Math.cos(v.angle) - (v.h / 2) * Math.sin(v.angle)),
                                (float) (v.y + (v.h / 2) * Math.cos(v.angle) - (v.w / 2) * Math.sin(v.angle)));
                GL11.glVertex2f((float) (v.x - (v.w / 2) * Math.cos(v.angle) + (v.h / 2) * Math.sin(v.angle)),
                                (float) (v.y - (v.h / 2) * Math.cos(v.angle) - (v.w / 2) * Math.sin(v.angle)));
                GL11.glVertex2f((float) (v.x + (v.w / 2) * Math.cos(v.angle) + (v.h / 2) * Math.sin(v.angle)),
                                (float) (v.y - (v.h / 2) * Math.cos(v.angle) + (v.w / 2) * Math.sin(v.angle)));
                GL11.glEnd();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Main
    // -----------------------------------------------------------------------

    public static void main(String[] argv) {
        new Start().start();
    }
}
