/*
  template
  Recommended template for Processing when working with XYscope often.
  Save into ~/Documents/Processing/templates/Java/ (as `sketch.pde`)
  cc teddavis.org 2023
*/

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance
 
void setup(){ 
  size(512, 512); // window size, optionally add P3D
 
  xy = new XYscope(this, ""); // define XYscope instance, "custom_dac" optional
  xy.getMixerInfo(); // lists all audio devices
} 
 
void draw(){ 
  background(0); 
  xy.clearWaves();  // clear waves from previous drawing
  
  // draw shapes here
  xy.circle(width/2, height/2, width); // it all began with a circle....
  
  xy.buildWaves(); // build shapes to audio waves
  xy.drawWaveform();
  xy.drawXY();
} 
