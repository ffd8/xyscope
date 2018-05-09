import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;
import xyscope.*;

public class squareTest extends PApplet {

	XYscope xy;
	int mode = 4;
	ArrayList<PVector> p = new ArrayList<PVector>();

	@Override
	public void settings() {
		size(512, 512);
	}

	public void setup() {
		xy = new XYscope(this, "", 192000);
		//xy.z("mk3_34", 192000);
		xy.rectMode(CENTER);
		xy.amp(.5f);
	}

	@Override
	public void draw() {
		background(0);

		// clear waves like refreshing background
		xy.clearWaves();

		// set detail of vertex ellipse
		xy.ellipseDetail(40);

		//translate(mouseX, mouseY);

		// use most primative shapes with class instance infront
		// xy.ellipse(mouseX, mouseY, width/4, width/4);
		 xy.ellipse(mouseX, mouseY, width/4, width/4);
		 xy.ellipse((float)(width*.75), height/6, width/4, width/4);
		 xy.ellipse(width/4, height/4, width/4, width/4);
		// xy.line(mouseX, mouseY, mouseX+50, mouseY+50);
		// xy.line(mouseX-50, mouseY, mouseX-0, mouseY+50);
		// xy.line(mouseX-150, mouseY, mouseX-100, mouseY+50);
		// xy.line(mouseX-250, mouseY, mouseX-200, mouseY+50);

		/*
		 * // GRID for(int i=0;i<width; i+= 50){ xy.line(i, 0, i, mouseY);
		 * xy.line(0, i, mouseX, i); xy.line(i, height, 0, i); xy.line(i, 0,
		 * width, i); }
		 */
		// xy.point(mouseX, mouseY);

		// build audio from shapes
		xy.buildWaves();

		// draw all analytics
		xy.drawAll();

		// or specific ones
		// xy.drawPath();
		// xy.drawWaveform();
		// xy.drawWaves();
		// xy.drawXY();
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { squareTest.class.getName() });
	}
}