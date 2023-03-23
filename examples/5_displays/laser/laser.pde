/*
 laser
 Guide to driving RGB laser with XYscope.
 cc teddavis.org 2018 – 23
 */

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

void setup() {
  size(640, 480, P3D);
  background(0);

  // initialize XYscope with default/custom sound out
  xy = new XYscope(this, "MOTU 1-2");

  // only stereo pairs allowed in processing, so it's broken to R, GB
  // incase 2nd channel of R pair is useful for blanking/etc.
  xy.laser("MOTU 3-4", "MOTU 5-6"); //xy.laser(mixerR, mixerGB);

  // RGB waveforms have own freq, so we can them out of sync
  xy.strokeFreq(50.05, 50.1, 50.2);
}

void draw() {
  background(0);

  // clear waves like refreshing background
  xy.clearWaves();

  xy.limitPath(0); // avoid drawing any forms beyond view

  xy.strokeDash(8); // optional dashes in the RGB stroke

  if (mousePressed) {
    // set low-pass filter of laser
    float newLPF = map(mouseX, 0, width, 1, 10000);
    xy.laserLPF(newLPF);

    // limit number of points it can draw with
    xy.limitPoints(floor(map(mouseY, 0, height, 0, 100)));
  }

  pushMatrix();
  translate(width/2, height/2);
  rotateY(radians(frameCount*.2));
  float s = height*.25;
  xy.stroke(0, 255, 255); // set RGB stroke before shapes (0-255)
  xy.ellipse(-s, 0, s, s);

  // rotating for infinity movement
  rotateY(radians(frameCount*.2));
  xy.stroke(255, 0, 255);
  rotate(radians(180)); // match ellipse startings points
  rotateX(radians(180)); // match ellipse startings points
  xy.ellipse(-s, 0, s, s);

  popMatrix();

  // build audio from shapes
  xy.buildWaves();

  // draw RGB Waveforms + XY simulation
  xy.drawRGB();
  //xy.drawWaveform();
  xy.drawXY();
}
