/* 
 zAxis_auto
 Draw isolated forms by using a 2nd audio out for Z-axis modulation.
 When 2nd audio card is specified, z-axis mode is enabled.
 Note: temporarily broken.. new better rendering mode, needs better z-axis
 cc teddavis.org 2017
 */

// import and create instance of XYscope
import xyscope.*;
XYscope xy;

// minim is required to generate audio
import ddf.minim.*; 

void setup() {
  size(512, 512, P3D);

  // initialize XYscope with custom outXY, outZ
  // left blank so it runs, but fill in with your audio card
  xy = new XYscope(this, "", "");
  //xy.getMixerList();

  // enable/disable auto z-axis modulation
  //xy.zAuto(false);
  
  // customize range for z-axis based on oscilloscope preference -1.0 » 1.0
  //xy.zRange(1,-1);
}


void draw() {
  background(0);

  // clear waves like refreshing background
  xy.clearWaves();

  // draw multiple rectangles in P3D space
  multiRect();

  // build audio from shapes
  xy.buildWaves();

  // draw all analytics
  xy.drawAll();

}

void multiRect() {
  pushMatrix();
  translate(width/2, height/2);
  rotateY(radians(frameCount));
  xy.rectMode(CENTER);
  int loopc = 10;
  float d = height/loopc;
  for (int i=0; i<loopc; i++) {
    pushMatrix();
    translate(0, 0, map(i, 0, loopc-1, -200, 200));
    rotateZ(radians(i*90/loopc+frameCount));
    translate(map(i, 0, loopc-1, -50, 50), 0, 0);
    xy.rect(0,0,100, 100);
    popMatrix();
  }
  popMatrix();
}
