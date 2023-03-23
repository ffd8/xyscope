/* 
 basic_drawing
 Draw to the scope by throwing more and more lines, clearing on demand
 cc teddavis.org 2017-23
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

  
  xy.buildWaves(); // build audio from shapes
  xy.drawAll(); // draw all analytics
}

void mouseDragged() {
  // add point based on width/height
  xy.line(mouseX, mouseY, pmouseX, pmouseY);
}

void keyPressed() {
  if (keyCode == 8) { // DELETE
    xy.clearWaves(); // clear waves similar to background
  }
}
