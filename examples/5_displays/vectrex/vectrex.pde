/*
 vectrex
 Basics settings for using a modded Vectrex monitor.
 cc teddavis.org 2018
 */

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

void setup() {
  size(512, 512);

  // initialize XYscope with default sound out
  xy = new XYscope(this, "");

  // set XYscope into Vectrex aspect-ratio mode
  xy.vectrex(90); // 90 for landscape, 0 for portrait

  /*
   If the SPOT-KILLER MOD was applied (z/brightness is always on),
   this auto sets the brightness (from way turned down) when the sketch runs.
   */
  //xy.z("MOTU 3-4"); // use custom 3rd channel audio device
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
