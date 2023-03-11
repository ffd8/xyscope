/*
 shape_torus
 Based upon Ira Greenbergs Toroid example
 https://processing.org/examples/toroid.html
 - mouseX » adjust dx
 - mouseY » adjust dy
 cc teddavis.org 2023
 */

// import and create instance of XYscope
import xyscope.*;
XYscope xy;

// minim is required to generate audio
import ddf.minim.*;

// initial detailX, detailY values
int dx = 12, dy = 12;

void setup() {
  size(512, 512, P3D);

  // initialize XYscope with default sound out
  xy = new XYscope(this);
}

void draw() {
  background(0);
  xy.clearWaves(); // clear waves

  //center and spin toroid
  pushMatrix();
  translate(width/2, height/2, -100);
  rotateX(radians(frameCount/2));
  rotateY(radians(frameCount));
  rotateZ(radians(frameCount/3));

  if (mousePressed) {
    dx = (int)map(mouseX, 0, width, 1, 50);
    dy = (int)map(mouseY, 0, height, 1, 50);
  }

  xy.torus(height/3, height/6, dx, dy);
  popMatrix();

  xy.buildWaves(); // build audio from shapes
  xy.drawAll(); // draw all analytics
}
