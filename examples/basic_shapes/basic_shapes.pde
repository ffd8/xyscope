/*
  basic_shapes
  Test this to make sure your setup is up and running.
  cc teddavis.org 2017
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

  // set detail of vertex ellipse
  xy.ellipseDetail(30);

  // use most primative shapes with class instance infront
  xy.ellipse(mouseX, mouseY, width/4, width/4);
  //xy.rect(mouseX, mouseY, width/4, width/4);
  //xy.line(mouseX, mouseY, width/4, width/4);
  //xy.point(mouseX, mouseY);

  
  // build audio from shapes
  xy.buildWaves();

  // draw all analytics
  xy.drawAll();

  // or specific ones
  // xy.drawPath();
  // xy.drawWaveform();
  // xy.drawWave();
  // xy.drawXY();
}
