/*
 calibration
 circle + square + lines to help center/adjust oscilloscpe display
 cc teddavis.org 2023
 */

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

void setup() {
  size(512, 512);

  xy = new XYscope(this); // define XYscope instance
  //xy.getMixerInfo(); // lists all audio devices
}

void draw() {
  background(0);
  xy.clearWaves(); // clear waves similar to background

  pushMatrix();
  translate(width/2, height/2);
  pushMatrix();
  rotate(radians(180));
  xy.circle(0, 0, width);
  popMatrix();
  xy.rectMode(CENTER);
  xy.rect(0, 0, width);
  xy.line(-width/2, 0, width/2, 0);
  xy.line(0, height/2, 0, -height/2);
  popMatrix();

  xy.buildWaves(); // build audio from shapes
  xy.drawAll(); // draw all analytics
}
