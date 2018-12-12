/*
 xtra_laser
 Guide to special functions for driving RGB laser with XYscope.
 cc teddavis.org 2018
 */

// import and create instance of XYscope
import xyscope.*;
XYscope xy;

// minim is required to generate audio
import ddf.minim.*; 


void setup() {
  size(800, 500, P3D);
  background(0);

  // initialize XYscope with default/custom sound out
  xy = new XYscope(this, "sf64_12");

  // can only use stereo pairs in processing, so i figured R, GB
  // trying to recall why.. gotta look at MOTU later, 
  // maybe due to using stereo of R for something else? or testing blanking
  xy.laser("sf64_34", "sf64_56");

  // it's really fun to tweak the freq of each wave for RGB, fall outta sync
  xy.strokeFreq(50.025, 50.05, 50.1);
}

void draw() {
  background(0);

  // clear waves like refreshing background
  xy.clearWaves(); 

  // optional dashes in the RGB stroke
  xy.strokeDash(2);


  if (mousePressed) {
    // set low-pass filter
    float newLPF = map(mouseX, 0, width, 1, 10000);
    xy.laserLPF(newLPF);

    // limit number of points it can draw with
    xy.limitPoints(floor(map(mouseY, 0, height, 0, 1000)));
  }


  float s = height*.25;
  xy.stroke(0, 255, 255);
  xy.ellipse(width/2-s, height/2, s, s);

  xy.stroke(255, 0, 255);
  xy.ellipse(width/2+s, height/2, s, s);


  // build audio from shapes
  xy.buildWaves();


  // draw Wave + XY analytics
  xy.drawRGB();
  //xy.drawWaveform();
  xy.drawXY();
}
