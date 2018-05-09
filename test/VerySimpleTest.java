import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;
import xyscope.*;

public class VerySimpleTest extends PApplet {

	XYscope xy;
	int mode = 4;
	ArrayList<PVector> p = new ArrayList<PVector>();
	
	@Override
	public void settings() {
		size(512, 512);
	}

	public void setup() {
		xy = new XYscope(this, "");
		//xy.z("mk3_34");
		xy.amp(.5f);
	}

	@Override
	public void draw() {
		background(0);

		// clear waves like refreshing background
		xy.clearWaves();

		// set detail of vertex ellipse
		xy.ellipseDetail(15);
		mode = 6;
		if (mode == 0) {
			xy.ellipse(width / 2, height / 2, width / 4, width / 4);
			xy.ellipse(width / 2, height / 2, width / 3, width / 3);
		} else if (mode == 1) {
			xy.ellipse(mouseX, mouseY, width / 4, width / 4);
		} else if (mode == 2) {
			xy.rect(mouseX, mouseY, width / 4, width / 4);
		} else if (mode == 3) {
			xy.line(0, height, width / 4, width / 4);
		} else if (mode == 4) {
			if (mousePressed) {
				p.add(new PVector(mouseX, mouseY));
			}

			for (int i = 0; i < p.size() - 1; i++) {
				xy.line(p.get(i).x, p.get(i).y, p.get(i + 1).x, p.get(i + 1).y);
				// xy.line(p.get(i).x, p.get(i).y, width/2, height/2);
				// xy.addPoint(map(p.get(i).x, 0, width, 0, 1), map(p.get(i).y,
				// 0, height, 0, 1));
			}
		} else if (mode == 5) {
			xy.line(width * .05f, height * .25f, width * .35f, height * .75f);
			xy.line(width * .75f, height * .25f, width * .95f, height * .75f);

		}else if(mode == 6){
			xy.rect(100, 100, width-200, height-200);
			xy.rect(250, 250, width-200, height-200);
		}
		// xy.line(mouseX, mouseY, width/4, width/4);
		// xy.point(mouseX, mouseY);

		// build audio from shapes
		xy.buildWaves();

		// draw all analytics
		xy.drawAll();
		println(xy.wavePoints());

		// or specific ones
		// xy.drawPath();
		// xy.drawWaveform();
		// xy.drawWaves();
		// xy.drawXY();
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { VerySimpleTest.class.getName() });
	}
}