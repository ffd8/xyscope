/*
 svg
 import any existing vector graphic as an SVG!
 » Requires Geomerative library
 
 cc teddavis.org 2023
 */

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

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
  xy.clearWaves(); // clear waves like refreshing background

  pushMatrix();
  translate(width/2, height/2);
  rotateY(radians(frameCount/2));
  if (pointPaths != null) { // only draw if we have points
    for (int i = 0; i < pointPaths.length; i++) {
      pushMatrix();
      translate(0, 0, sin(i*5 + frameCount*.02)*100);
      xy.beginShape();
      for (int j=0; j < pointPaths[i].length; j++) {
        xy.vertex(pointPaths[i][j].x, pointPaths[i][j].y);
      }
      xy.endShape();
      popMatrix();
    }
  }
  popMatrix();

  xy.buildWaves(); // build audio from shapes

  // draw Wave + XY analytics
  xy.drawWave();
  xy.drawXY();
}
