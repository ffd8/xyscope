/* 
 clock
 cc teddavis.org 2018
 */

// import and create instance of XYscope
import xyscope.*;
XYscope xy;

// minim is required to generate audio
import ddf.minim.*; 

void setup() {
  size(512, 512);

  // initialize XYscope with default sound out
  xy = new XYscope(this);
}

void draw() {
  background(0);

  // clear waves like refreshing background
  xy.clearWaves();

  // draw clock face
  xy.ellipse(width/2, height/2, width*.4, width*.4);

  //second
  float s = map(second(), 0, 60, 0, 360);
  pushMatrix();
  translate(width/2, height/2);
  rotate(radians(s));
  xy.line(0, -height*.38, 0, 0);
  popMatrix();

  //minute
  float m = map(minute(), 0, 60, 0, 360); 
  pushMatrix();
  translate(width/2, height/2);
  rotate(radians(m));
  xy.line(0, -height*.28, 0, 0);
  popMatrix();

  //hour
  float h = map(hour(), 0, 24, 0, 360); 
  pushMatrix();
  translate(width/2, height/2);
  rotate(radians(h));
  xy.line(0, -height*.18, 0, 0);
  popMatrix();

  // build audio from shapes
  xy.buildWaves();

  // draw all analytics
  xy.drawAll();
}
