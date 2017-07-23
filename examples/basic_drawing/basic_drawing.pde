/* 
 basic_drawing
 You can draw by simply adding points directly or converted
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

  // smooth waves to reduce visible points
  xy.smoothWaves(true);
  
  // test best smoothAmount, default 12
  xy.smoothWavesAmount(12);
}


void draw() {
  background(0);

  // build audio from shapes
  xy.buildWaves();

  // draw all analytics
  xy.drawAll();
}

void mouseDragged() {
  // add point based on width/height
  xy.point(mouseX, mouseY);

  // add point normalized to 0 – 1
  //xy.addPoint(norm(mouseX, 0, width), norm(mouseY, 0, height));
}

void keyPressed() {
  if (keyCode == 8) { // DELETE
    xy.clearWaves();
  }
}