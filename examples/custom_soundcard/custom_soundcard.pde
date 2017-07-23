/* 
  custom_soundcard
  Enjoy wiggle/movement of most DAC's.
  For 'better' results, find a DC-coupled DAC.
  http://www.expert-sleepers.co.uk/siwacompatibility.html
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
  xy.getMixerInfo();
  
  // set soundcard by String or ID (if not found, will default system out)
  // xy = new XYscope(this, "MOTU828_12");
  // xy = new XYscope(this, 4);
}

void draw() {
  background(0);

  // clear waves like refreshing background
  xy.clearWaves();

  // set detail of vertex ellipse
  xy.rectMode(CENTER);

  // use all primative shapes with class instance infront
  xy.rect(mouseX, mouseY, width/4, width/4);

  // build audio from shapes
  xy.buildWaves();

  // draw all analytics
  xy.drawAll();
}