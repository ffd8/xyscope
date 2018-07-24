/*
 vectrex
 Basics settings for using a modded Vectrex monitor.
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
  xy = new XYscope(this, "");

  // set XYscope into Vectrex mode, using 90° rotation (landscape)
  xy.vectrex(90);

  /*
   If the anti-blanking mod was applied (z/brightness is always on),
   this auto sets the brightness (from way turned down) when the sketch runs.
   */
  //xy.z("*set to 3rd audio channel out*");
  //xy.zRange(.5, 0);
}

void draw() {
  background(0);

  // clear waves like refreshing background
  xy.clearWaves();

  // set detail of vertex ellipse
  xy.ellipseDetail(30);

  // draw two primative shapes, testing boundry of screen
  xy.rect(0, 0, width, height);
  float s = map(mouseX, 0, width, -height/2, height/2);
  xy.ellipse(width/2, height/2, s, s);

  // build audio from shapes
  xy.buildWaves();

  // draw all analytics
  xy.drawAll();
}
