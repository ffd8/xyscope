/*

*/

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

void setup() {
  size(512, 512); // declares size of output window

  xy = new XYscope(this);
  xy.getMixerInfo(); // lists all audio devices

  genWaveforms();
}

void draw() {
  background(0);

  // build using custom waveforms
  genWaveforms();

  xy.drawWaveform();
  xy.drawXY();
}

void genWaveforms() {
  float[] waveX = new float[xy.bufferSize()];
  float[] waveY = new float[xy.bufferSize()];

  for (int i=0; i<waveX.length; i++) {
    waveX[i] = sin(i*TWO_PI/512);//map(i, 0, waveX.length, -1, 1);
    waveY[i] = -.5 + noise(frameCount*.015+i*TWO_PI/512);
  }
  
  // set waveform with arrays of -1.0 to 1.0 values
  xy.setWaveforms(waveX, waveY); // optionally set waveZ too
}
