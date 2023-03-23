/*
 clock
 cc teddavis.org 2018-23
 */

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

float sec = 0;

void setup() {
  size(512, 512);

  xy = new XYscope(this); // define XYscope instance
  //xy.getMixerInfo(); // lists all audio devices
  
  sec = map(second(), 0, 60, 0, 360);
}

void draw() {
  background(0);
  xy.freq(second());
  //xy.freq(sec%360/6); // smooth ramp
  xy.ellipseDetail(24);

  // clear waves like refreshing background
  xy.clearWaves();

  // draw clock face
  xy.ellipse(width/2, height/2, width, width);
  pushMatrix();
  translate(width/2, height/2);
  for (int i=0; i < 12; i++) {
    rotate(radians(360/12));
    xy.line(0, height*.49, 0, height*.4);
  }
  popMatrix();

  //second
  float s = map(second(), 0, 60, 0, 360);
  sec += .1;//24/millis()/1000;
  pushMatrix();
  translate(width/2, height/2);
  rotate(radians(s)); // sec for smooth
  xy.line(0, -height*.35, 0, 0);
  popMatrix();

  //minute
  float m = map(minute(), 0, 60, 0, 360);
  pushMatrix();
  translate(width/2, height/2);
  rotate(radians(m));
  xy.line(0, -height*.25, 0, 0);
  popMatrix();

  //hour
  float h = map(hour(), 0, 24, 0, 360);
  pushMatrix();
  translate(width/2, height/2);
  rotate(radians(h));
  xy.line(0, -height*.15, 0, 0);
  popMatrix();

  // build audio from shapes
  xy.buildWaves();

  // draw all analytics
  xy.drawAll();
}
