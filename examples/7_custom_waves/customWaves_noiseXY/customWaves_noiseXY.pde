/*
 customWaves_buildXY
 You don't have to use primitive forms,
 you can also generate the waveforms by your own code and logic.
 cc teddavis.org 2017-23
 */

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

float[] yWave, xWave;

void setup() {
  size(512, 512);
  background(0);

  // initialize custom local minim
  xy = new XYscope(this);

  // function below to build waves
  genNoiseWave();
}


void draw() {
  background(0);

  // draw analytics
  xy.drawAll();
}

void keyPressed() {
  // refresh noise with spacebar
  if (keyCode == 32) {
    genNoiseWave();
  }
}

void genNoiseWave() {
  // set new noiseSeed
  noiseSeed(frameCount);

  // get bufferSize() of output
  println(xy.waveSize());

  // initialize array for storing values
  yWave = new float[xy.waveSize()];
  xWave = new float[xy.waveSize()];
  float nx = random(1);
  float ny = random(1);

  // add noise walker to waveform
  for (int i=0; i<yWave.length; i++) {
    xWave[i] = noise(nx)*1;
    yWave[i] = noise(ny)*1;
    nx+=.01;
    ny+=.011;
  }

  // build each XY waveform with array of 0.0 – 1.0 values
  xy.buildX(xWave);
  xy.buildY(yWave);
}
