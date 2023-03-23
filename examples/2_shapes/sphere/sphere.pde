/*
  sphere
  mouseX/Y, adjust detailX, detailY
  SHIFT + mouseDragged, freq based on distance from initial click
*/

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

int pmx = 0, pmy = 0;

void setup() {
  size(512, 512, P3D); // declares size of output window

  xy = new XYscope(this);
  //xy.getMixerInfo(); // lists all audio devices
}

void draw() {
  background(0);
  xy.clearWaves();
  //xy.resetWaves();
  if (keyPressed && keyCode == 16) { // SHIFT
    float freq = dist(mouseX, mouseY, pmx, pmy);
    xy.freq(freq);
  }

  // draw shapes here
  pushMatrix();
  translate(width/2, height/2);
  rotateY(radians(frameCount/3));
  rotateX(radians(frameCount/2));
  xy.sphere(width*.4, mouseX/5, mouseY/5); // xy.sphere(size, xVert, yVert)
  popMatrix();

  xy.buildWaves();
  xy.drawXY();
}

void mousePressed() {
  pmx = mouseX;
  pmy = mouseY;
}
