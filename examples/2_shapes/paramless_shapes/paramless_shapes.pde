/*
 paramless shapes
 incase you're quickly testing/sketching,
 you can create primatives without values
 cc teddavis.org 2023
 */

import xyscope.*;
XYscope xy;
import ddf.minim.*;

void setup() {
  size(512, 512, P3D); // declares size of output window

  xy = new XYscope(this);
  //xy.getMixerInfo(); // lists all audio devices
}

void draw() {
  background(0);
  if (frameCount%60==0) {
    xy.freq(random(-100, 100));
    xy.resetWaves(); // helps if slipping out of phase from freq changes
  }
  xy.clearWaves();

  switch(floor(frameCount*.1)%10) {
  case 0:
    xy.line();
    break;
  case 1:
    xy.rectMode(CENTER);
    xy.rect();
    break;
  case 2:
    xy.square();
    break;
  case 3:
    xy.circle();
    break;
  case 4:
    xy.ellipse();
    break;
  case 5:
    xy.lissajous();
    break;
  case 6:
    xy.textAlign(CENTER, CENTER);
    xy.textSize(80);
    xy.text();
    break;
  case 7:
    pushMatrix();
    translate(width/2, height/2);
    xy.box();
    popMatrix();
    break;
  case 8:
    pushMatrix();
    translate(width/2, height/2);
    xy.sphere();
    popMatrix();
    break;
  case 9:
    pushMatrix();
    translate(width/2, height/2);
    xy.torus();
    popMatrix();
    break;
  }

  xy.buildWaves();
  xy.drawXY();
}
