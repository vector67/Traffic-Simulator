package main;

import java.awt.Color;
import java.util.ArrayList;

import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Transform;

public abstract class Vehicle {
    public boolean begin;
    public Bucket[] sbuckets;
    public Node forwardpoint1, forwardpoint2, forwardpoint3;
    public ArrayList<Vehicle> checkedcollisions;
    public Bucket currentbucket;
    public int bleft, bright, btop, bbottom;
    public boolean calculate, constantspeed;
    public double x, y;
    public int w, h;
    double addninety;
    public double angle, speed, steer, maxspeed, braking;
    public double tcx, tcy, tcr;
    double Mtocentercirc = 0, Ctocentercirc = 0, Mmiddleline = 0, Cmiddleline = 0, oldx, oldy;
    public double adddelta;
    double secs, circ, radius;
    public Color col;
    public Waypoints waypts;
    public Node currentnode;
    public Route currentroute;
    public int ID;
    public double avgspeedfortcirc, distancefortcirc, secstocompletefortcirc;
    public double distancetravelled, timetaken;
    public Node[] cache;
    public int cachepointer;
    SimulationContext sim;

    public Vehicle(double x1, double y1, int w1, int h1, double a1, double s1,
                   Color col2, Waypoints wp, SimulationContext sim1,
                   double steer, Node currentnode1, int id) {
        this(x1, y1, w1, h1, a1, s1, col2, wp, sim1, steer, currentnode1);
        ID = id;
    }

    public Vehicle(double x1, double y1, int w1, int h1, double a1, double s1,
                   Color col2, Waypoints wp, SimulationContext sim1,
                   double steer1, Node currentnode1) {
        cache = new Node[10];
        forwardpoint1 = new Node((x1 + Math.sin(angle) * (w1 * 3)), (y1 + Math.cos(angle) * w1 * 3), 0);
        forwardpoint2 = new Node((x1 + Math.sin(angle) * (w1 * 2)), (y1 + Math.cos(angle) * w1 * 2), 0);
        forwardpoint3 = new Node((x1 + Math.sin(angle) * (w1)),     (y1 + Math.cos(angle) * w1), 0);
        begin = true;
        x = x1;
        y = y1;
        w = w1;
        h = h1;
        angle = a1;
        maxspeed = s1;
        speed = 0.1;
        col = col2;
        waypts = wp;
        steer = steer1;
        sim = sim1;
        checkedcollisions = new ArrayList<Vehicle>();

        if (!wp.isEmpty()) {
            currentnode = wp.get(1);
            currentnode = currentnode1;
            currentroute = currentnode.getChildRoute();
            currentroute.addVehicle(this);
            if (currentnode.hasChild()) {
                angle = SimulationMath.dirto(x, y, currentnode.getChild().getX(), currentnode.getChild().getY());
            } else {
                angle = SimulationMath.dirto(x, y, currentnode.getX(), currentnode.getY());
            }
        }
    }

    public Vehicle(Vehicle v) {
        this(v.x, v.y, v.w, v.h, v.angle, v.maxspeed, v.col, v.waypts, v.sim, v.steer, v.currentnode, v.ID);
    }

    public void update() {
        forwardpoint1.x = x + Math.cos(angle) * (w * 3);
        forwardpoint1.y = y + Math.sin(angle) * (w * 3);
        forwardpoint2.x = x + Math.cos(angle) * (w * 2);
        forwardpoint2.y = y + Math.sin(angle) * (w * 2);
        forwardpoint3.x = x + Math.cos(angle) * (w);
        forwardpoint3.y = y + Math.sin(angle) * (w);

        if (begin) {
            currentbucket = sim.getBuckets().getBucketIn((int) x, (int) y);
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
        adddelta += sim.getDelta() / 1000f;
        if (adddelta > 1) {
            adddelta = 0;
        }
        double directionto = SimulationMath.dirto(x, y, currentnode.x, currentnode.y);
        if (currentnode != null) {
            if (SimulationMath.distance(x, y, currentnode.x, currentnode.y) < (Math.max(speed * ((double) sim.getDelta()) / 1000f, 3)) * 1.1) {
                constantspeed = false;
                calculate = false;
                oldx = x;
                oldy = y;
                Mtocentercirc = 0;
                Ctocentercirc = 0;
                Mmiddleline = 0;
                Cmiddleline = 0;
                braking = 0;
                if (currentnode.hasChild()) {
                    currentnode = currentnode.getChild();
                    currentroute = currentnode.getChildRoute();
                    currentroute.addVehicle(this);
                } else {
                    sim.removeVehicle(this);
                    currentbucket.removeVehicle(this);
                }
                directionto = SimulationMath.dirto(x, y, currentnode.x, currentnode.y);
                recadjustangle(directionto, 0);
                secs = 360 / steer;
                circ = speed * secs;
                radius = (circ / Math.PI) / 2;
                addninety = 0;
                double centerx, centery;
                if (directionto > angle) {
                    addninety = Math.PI / 2;
                    centerx = Math.cos(angle + Math.PI / 2) * radius + x;
                    centery = Math.sin(angle + Math.PI / 2) * radius + y;
                } else {
                    addninety = -Math.PI / 2;
                    centerx = Math.cos(angle - Math.PI / 2) * radius + x;
                    centery = Math.sin(angle - Math.PI / 2) * radius + y;
                }
                if (SimulationMath.distance(centerx, centery, currentnode.x, currentnode.y) < radius) {
                    calculatetcirc(directionto);
                }
            } else if ((SimulationMath.distance(x, y, currentnode.x, currentnode.y) < (speed * ((double) sim.getDelta()) / 1000f) * 50)
                    && (Math.abs(directionto - angle) > Math.PI / 2)) {
                calculatetcirc(directionto);
            }
        }

        if (timetaken > secstocompletefortcirc) {
            constantspeed = false;
        }
        if ((speed * secstocompletefortcirc) + ((distancetravelled / timetaken) * timetaken) / (timetaken + secstocompletefortcirc) < (avgspeedfortcirc) && calculate) {
            constantspeed = true;
            braking = 0;
            calculate = false;
        }

        double radius2 = (speed * 360 / steer / Math.PI) / 2;
        if (directionto > angle) {
            // centerx2 / centery2 unused but preserved for symmetry with original
            double cx = Math.cos(angle + Math.PI / 2) * radius2 + x;
            double cy = Math.sin(angle + Math.PI / 2) * radius2 + y;
        } else {
            double cx = Math.cos(angle - Math.PI / 2) * radius2 + x;
            double cy = Math.sin(angle - Math.PI / 2) * radius2 + y;
        }
        directionto = SimulationMath.dirto(x, y, currentnode.x, currentnode.y);

        if ((directionto - angle) > Math.PI / 180 || (directionto - angle) < -Math.PI / 180) {
            recadjustangle(directionto, 0);
            if (directionto > angle) {
                angle += Math.min(Math.toRadians(steer * ((double) sim.getDelta()) / 1000f), Math.abs(directionto - angle));
            } else {
                angle -= Math.min(Math.toRadians(steer * ((double) sim.getDelta()) / 1000f), Math.abs(directionto - angle));
            }
        }
        if (braking != 0) {
            speed = Math.max(speed - braking * ((double) sim.getDelta()) / 1000f, 0);
            if (speed == 0) braking = 0;
        } else {
            if (!constantspeed) {
                speed = Math.min(maxspeed, speed + (((double) sim.getDelta()) * w / 1000) * 0.8);
            }
        }
        distancetravelled += speed * ((double) sim.getDelta()) / 1000f;
        timetaken        += ((double) sim.getDelta()) / 1000f;
        distancefortcirc -= speed * ((double) sim.getDelta()) / 1000f;
        secstocompletefortcirc -= ((double) sim.getDelta()) / 1000f;
        x = x + Math.cos(angle) * speed * ((double) sim.getDelta()) / 1000f;
        y = y + Math.sin(angle) * speed * ((double) sim.getDelta()) / 1000f;
        if (speed > maxspeed) speed = maxspeed;
        int sw = sim.getScreenWidth();
        int sh = sim.getScreenHeight();
        if (x > sw) x = 0;
        if (y > sh) y = 0;
        if (x < 0) x = sw;
        if (y < 0) y = sh;
        checkCollisions();
    }

    public void updateBuckets() {
        if (x > bright) {
            Bucket cb = sim.getBuckets().next(1, currentbucket);
            if (cb != null) { currentbucket.removeVehicle(this); currentbucket = cb; currentbucket.addVehicle(this); }
            checksides();
        }
        if (x < bleft) {
            Bucket cb = sim.getBuckets().next(3, currentbucket);
            if (cb != null) { currentbucket.removeVehicle(this); currentbucket = cb; currentbucket.addVehicle(this); }
            checksides();
        }
        if (y > btop) {
            Bucket cb = sim.getBuckets().next(2, currentbucket);
            if (cb != null) { currentbucket.removeVehicle(this); currentbucket = cb; currentbucket.addVehicle(this); }
            checksides();
        }
        if (y < bbottom) {
            Bucket cb = sim.getBuckets().next(0, currentbucket);
            if (cb != null) { currentbucket.removeVehicle(this); currentbucket = cb; currentbucket.addVehicle(this); }
            checksides();
        }
    }

    public void checksides() {
        btop    = currentbucket.topSide();
        bright  = currentbucket.rightSide();
        bbottom = currentbucket.bottomSide();
        bleft   = currentbucket.leftSide();
        sbuckets = sim.getBuckets().surroundBuckets(currentbucket);
    }

    public boolean checkCollisions() {
        boolean collided = false;
        for (Bucket b : sbuckets) {
            if (b != null) {
                ArrayList<Vehicle> vhcls = b.getVehicles();
                for (Vehicle a : vhcls) {
                    if (this.ID != a.ID && this.hasCollided(a)) {
                        collided = true;
                        calculate = false;
                    }
                }
            }
        }
        if (!collided && (braking == 543 || braking == 203 || braking == 103)) {
            braking = 0;
        }
        return true;
    }

    public boolean hasCollided(Vehicle b) {
        if (checkedcollisions.contains(b)) return false;
        Rectangle them = new Rectangle((float) b.x, (float) b.y, (float) b.w, (float) b.h);
        them.transform(Transform.createRotateTransform(((float) b.angle)));
        if (them.contains((float) forwardpoint1.x, (float) forwardpoint1.y)) { braking = 103; return true; }
        if (them.contains((float) forwardpoint2.x, (float) forwardpoint2.y)) { braking = 203; return true; }
        if (them.contains((float) forwardpoint3.x, (float) forwardpoint3.y)) { braking = 543; return true; }
        Rectangle us = new Rectangle((float) x, (float) y, (float) w, (float) h);
        us.transform(new Transform().createRotateTransform(((float) angle)));
        if (us.intersects(them)) {
            if (b.speed != 0) { speed = 0; } else { braking = 0; }
            return true;
        }
        this.checkedcollisions.add(b);
        b.checkedcollisions.add(this);
        return false;
    }

    public void recadjustangle(double dirto, double ang) {
        angle += ang;
        if (Math.abs(dirto - angle) > Math.PI) {
            if (dirto > angle) {
                recadjustangle(dirto, Math.PI * 2);
            } else {
                recadjustangle(dirto, -Math.PI * 2);
            }
        }
    }

    public void calculatetcirc(double directionto) {
        calculate = true;
        Mtocentercirc = Math.sin(angle + addninety) / Math.cos(angle + addninety);
        Ctocentercirc = y - (Mtocentercirc * x);
        double hyp = SimulationMath.distance(x, y, currentnode.x, currentnode.y) / 2;
        Mmiddleline = Math.sin(directionto + addninety) / Math.cos(directionto + addninety);
        Cmiddleline = (Math.sin(directionto) * hyp + y) - (Mmiddleline * (Math.cos(directionto) * hyp + x));
        double circx = (Ctocentercirc - Cmiddleline) / (Mmiddleline - Mtocentercirc);
        tcx = circx;
        double circy = Mmiddleline * circx + Cmiddleline;
        tcy = circy;
        double circr = SimulationMath.distance(x, y, circx, circy);
        tcr = circr;
        double ourangle = Math.asin(hyp / circr) * 2;
        double percentageofcirc = ourangle / (Math.PI * 2);
        double circcirc = Math.PI * circr * 2;
        distancefortcirc = circcirc * percentageofcirc;
        secstocompletefortcirc = Math.toDegrees(ourangle) / steer;
        avgspeedfortcirc = distancefortcirc / secstocompletefortcirc;
        braking = ((speed - (avgspeedfortcirc * 2 - speed)) / secstocompletefortcirc) * 2;
        distancetravelled = 0;
        timetaken = 0;
        if (braking < 0) braking = 0;
    }

    public String shortString() {
        return "The car " + ID + " is at " + x + "," + y + "\n"
             + "Speed: " + ((speed / w) / 0.122) + "mp/h\n"
             + "Heading: " + Math.toDegrees(angle) + " degrees\n";
    }

    public String toString() {
        return "The car " + ID + " is at " + x + "," + y + "\n"
             + "Heading towards " + currentnode.x + "," + currentnode.y + "\n"
             + "Speed: " + ((speed / w) / 0.122) + "mp/h\n"
             + "Heading: " + Math.toDegrees(angle) + " degrees\n"
             + "Top speed: " + ((maxspeed / w) / 0.122) + "mp/h\n";
    }

    public double getAngle() { return angle; }
    public void setAngle(double angle) { this.angle = angle; }
}
