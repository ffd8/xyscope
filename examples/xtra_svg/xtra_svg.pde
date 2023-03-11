/* 
 xtra_svg
 » Requires Geomerative library
 
 cc teddavis.org 2023
 */

// import and create instance of XYscope
import xyscope.*;
XYscope xy;

// minim is required to generate audio
import ddf.minim.*; 

// geomerative is required to generate svg points
import geomerative.*; 
RShape svg;
RPoint[][] pointPaths;

void setup() {
  size(512, 512, P3D);

  // initialize XYscope [with default/custom sound out]
  xy = new XYscope(this, "");

  // initialize Geomerative
  RG.init(this);
  svg = RG.loadShape("oscilloscope.svg");
  svg.centerIn(g, 30);
  pointPaths = svg.getPointsInPaths();
}

void draw() {
  background(0);
  // clear waves like refreshing background
  xy.clearWaves(); 

  pushMatrix();
  translate(width/2, height/2); 
  if (pointPaths != null) { // only draw if we have points 
    for (int i = 0; i < pointPaths.length; i++) { 
      xy.beginShape(); 
      for (int j=0; j < pointPaths[i].length; j++) { 
        xy.vertex(pointPaths[i][j].x, pointPaths[i][j].y);
      } 
      xy.endShape();
    }
  } 
  popMatrix();

  // build audio from shapes
  xy.buildWaves();

  // draw Wave + XY analytics
  xy.drawWave();
  xy.drawXY();
}
