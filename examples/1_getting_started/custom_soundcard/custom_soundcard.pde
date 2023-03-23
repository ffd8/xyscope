/*
 custom_soundcard
 Most AC-Coupled DACs will wiggle towards the center.
 For 'better' results, find a DC-coupled DAC.
 http://www.expert-sleepers.co.uk/siwacompatibility.html
 
 + cheapo $20 solution:
 https://www.delock.com/produkt/61645/merkmale.html
 https://www.youtube.com/live/VjRTLviVBxo?feature=share&t=2491
 
 cc teddavis.org 2017-23
 */

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

void setup() {
  size(512, 512);

  xy = new XYscope(this); // init XYscope with default sound out
  xy.getMixerInfo(); // list available sound cards

  //xy = new XYscope(this, 0); // select by port
  //xy = new XYscope(this, "BlackHole 2ch"); // select by name
  //xy = new XYscope(this, "BlackHole 2ch", 48000); // custom card + sampeRate
}

void draw() {
  background(0);
  xy.clearWaves(); // clear waves like refreshing background

  xy.rectMode(CENTER);
  xy.rect(mouseX, mouseY, width/4, width/4);

  xy.buildWaves(); // build audio from shapes
  xy.drawAll(); // draw all analytics
}
