package main;

/** Pure-math utilities with no rendering or simulation dependencies. */
public final class SimulationMath {

    private SimulationMath() {}

    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public static double dirto(double x1, double y1, double x2, double y2) {
        double rise = y2 - y1;
        double run  = x2 - x1;
        double poss = (run < 0) ? Math.PI : 0;
        double result = Math.atan(rise / run) + poss;
        return (result > Math.PI) ? result - Math.PI * 2 : result;
    }
}
