/* 
 basic_drawing
 You can draw by simply adding points directly or converted
 cc teddavis.org 2017/18
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

  // build audio from shapes
  xy.buildWaves();

  // draw all analytics
  xy.drawAll();
}

void mouseDragged() {
  // add point based on width/height
  xy.line(mouseX, mouseY, pmouseX, pmouseY);
}

void keyPressed() {
  if (keyCode == 8) { // DELETE
    xy.clearWaves();
  }
}
