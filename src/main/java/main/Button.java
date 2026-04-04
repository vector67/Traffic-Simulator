package main;

public class Button extends Bucket {

	public Button(double x1, double y1, int w, int h) {
		super(x1, y1, w, h);
		// TODO Auto-generated constructor stub
	}
	public void clicked(mapmaker mm){
		mm.startpoint = null;
	}

}
