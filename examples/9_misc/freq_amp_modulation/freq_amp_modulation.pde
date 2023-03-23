/* 
 freq_amp_modulation
 Basic modulation of amp + freq of waves
 Wild effects can be had by changing in unique ways
 cc teddavis.org 2017
 */

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

void setup() {
  size(512, 512);

  // initialize XYscope with default sound out
  xy = new XYscope(this);
}


void draw() {
  background(0);

  // clear waves like refreshing background
  xy.clearWaves();

  // modulate amp with mouseX left - right
  xy.amp(norm(mouseY, height, 0));

  // modulate just one value for fun scaling
  // xy.ampX(norm(mouseX, 0, width));
  // xy.ampY(norm(mouseX, 0, width));

  // modulate freq with mouseY up - down
  xy.freq(map(mouseX, 0, width, 0, 440));

  // draw house
  drawHouse();

  // build audio from shapes
  xy.buildWaves();

  // draw all analytics
  xy.drawAll();
}

void drawHouse() {
  pushMatrix();
  translate(width*.5, height*.75);
  float scl = 2;
  xy.beginShape();
  xy.vertex(-width*.1*scl, 0);
  xy.vertex(width*.1*scl, 0);
  xy.vertex(width*.1*scl, -height*.2*scl);
  xy.vertex(0, -height*.3*scl);
  xy.vertex(-width*.1*scl, -height*.2*scl);
  xy.vertex(-width*.1*scl, 0);
  xy.vertex(-width*.05*scl, 0);
  xy.vertex(-width*.05*scl, -height*.1*scl);
  xy.vertex(-width*.0*scl, -height*.1*scl);
  xy.vertex(-width*.0*scl, 0);
  xy.endShape();
  popMatrix();
}
