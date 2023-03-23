/*
 osc wavetables
 transport your XYscope custom wavetables via OSC to other tools
 to visual forms drawn, add effects, pass into custom oscillators.
 
 format: [x1, y1, z1], [x2, y2, z2], ...];
 
 + import /data/P5L_xyscope-osc_wavetables.json to offline P5LIVE for demo
 * not working for your tool of choice? join discussion:
   https://github.com/ffd8/xyscope/issues/5
 
 cc teddavis.org 2023
 */

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

import java.util.Arrays; // needed for 2D Array to string

// OSC code
import oscP5.*;
import netP5.*;
OscP5 oscP5;
NetAddress myRemoteLocation;

float fc = 0;

void setup() {
  size(512, 512, P3D); // declares size of output window

  xy = new XYscope(this); // define XYscope instance
  //xy.getMixerInfo(); // lists all audio devices

  /* start oscP5, sending outgoing messages */
  oscP5 = new OscP5(this, 12001); // receiving port
  myRemoteLocation = new NetAddress("127.0.0.1", 12000); // sending port
}

void draw() {
  background(0);
  xy.clearWaves();
  xy.limitPath(0);
  xy.steps(20);

  pushMatrix();
  translate(width/2, height/2);
  rotateX(radians(-frameCount));
  rotateY(radians(-frameCount/2));
  xy.textSize(72);
  xy.textAlign(CENTER, CENTER);
  xy.text("XYscope", 0, 0);

  xy.textSize(32);
  float tw = xy.textWidth("XYscope");
  fc += mouseX;
  for (int i=0; i<3; i++) {
    pushMatrix();
    rotateX(radians(-fc+i*50));
    translate(map(i, 0, 3, -tw, tw), -height*.1, height*.1);
    xy.text("OSC", 0, 0);
    popMatrix();
  }
  popMatrix();

  xy.buildWaves();
  xy.drawAll();

  sendWaves(); // send OSC message of waves [x1, y1, z1], [x2, y2, z2], ...];
}

void sendWaves() {
  float[][] getWaves = new float[xy.tableX.size()][3];
  for (int i=0; i < xy.tableX.size(); i++) {
    getWaves[i][0] = xy.tableX.get(i);
    getWaves[i][1] = xy.tableY.get(i);
  }

  //System.out.println(Arrays.deepToString(getWaves));
  String xyWavetables = Arrays.deepToString(getWaves);

  OscMessage myMessage = new OscMessage("/XYscope");
  myMessage.add(xyWavetables); /* add an int to the osc message */

  //    /* send the message */
  oscP5.send(myMessage, myRemoteLocation);
}

void keyPressed() {
  // optionally only send on demand, disable within draw()
  if (keyCode == 32) {
    sendWaves();
  }
}
