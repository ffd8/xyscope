/*
 audio recorder
 Export your XYscope as .wav audio files!
 just use: xy.recorderBegin(filename) + xy.recorderEnd()
 cc teddavis.org 2023
 */

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

void setup() {
  size(512, 512, P3D); // declares size of output window

  xy = new XYscope(this, 48000); // use desired sampleRate
  //xy.getMixerInfo(); // lists all audio devices
}

void draw() {
  background(0);
  xy.clearWaves();

  push();
  translate(width/2, height/2);
  rotateY(radians(frameCount/2));
  rotateZ(radians(frameCount/2));
  xy.box(height/2);

  float off = width/5;
  translate(random(-off, off), random(-off, off), random(-off, off));
  xy.sphere(random(50));
  pop();

  xy.buildWaves();
  xy.drawAll();
}

void keyPressed() {
  if (key == 'r') {
    if (xy.recorder != null && xy.recorder.isRecording()) {
      xy.recorderEnd();
    } else {
      xy.recorderBegin("box-spheres"); // leave blank for "XYscope"
    }
  }
}
