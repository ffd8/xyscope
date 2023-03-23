/*
  additive-synth shapes
  an amazing aspect of vector graphics, 
  is sending multiple shapes on the same audio = additive synth!
  create wild forms, animation, effects, through various freq/amp play.
  cc teddavis.org 2023
 */

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy, xy2;         // create 2x XYscope instances

void setup() {
  size(512, 512); // declares size of output window

  xy = new XYscope(this);
  xy2 = new XYscope(this, xy.outXY); // patch 2nd instance to 1st output
  //xy.getMixerInfo(); // lists all audio devices
}

void draw() {
  background(0);
  xy.clearWaves();
  xy.circle(width/2, height/2, width/2);
  xy.buildWaves();
  xy.drawPath(0, 255, 255); // custom stroke color
  xy.drawXY();

  // additive synth on 2nd instance of XYscope
  xy2.clearWaves();
  xy2.freq(mouseX); // without speakers, test really high freqs!
  //xy2.amp(.2); // experiment with high freq, low amp modulation
  //xy2.circle(width/2, height/2, mouseY);
  xy2.circle(mouseX, mouseY, 50);
  //xy2.line(width/2, height/2, mouseX, mouseY);
  //xy2.lissajous(width/2, height/2, mouseY/2, 1, 2, frameCount, 180);
  xy2.buildWaves();
  xy2.drawPath(255, 255, 0); // custom stroke color
}
