/* 
 multiscopes_class
 Control as many oscilloscopes as you have audio card outputs.
 Create Aggregate devices in Utilities » Audio/Midi 
 for pairs of stereo devices from multi-channel DAC
 cc teddavis.org 2017
 */

// import and create instance of XYscope
import xyscope.*;
XYscope xy;

// minim is required to generate audio
import ddf.minim.*; 

// how many scopes, audio channels to drive?
int scopeCount = 4;

// replace with your own aggregate device names
//String[] mixerName = {"MOTU828_A_12", "MOTU828_A_34", "MOTU828_A_56", "MOTU828_A_78"};
String[] mixerName = {"", "", "", ""};

// custom class below
ScopeDraw[] sd = new ScopeDraw[scopeCount];

void setup() {
  size(512, 512);
  background(0);

  // initiate class instances (
  // XYscope is within class
  for (int i=0; i<sd.length; i++) {
    sd[i] = new ScopeDraw(this, mixerName[i]);
  }
}

void draw() {
  background(0);

  // run each class in draw
  for (int i=0; i<sd.length; i++) {
    sd[i].display();
  }
}

void keyPressed() {
  // pass keyPressed into class
  for (int i=0; i<sd.length; i++) {
    sd[i].checkKey(keyCode);
  }
}

class ScopeDraw {
  // create instance of XYscope
  XYscope xy;

  float x, y, s, v;

  // tell class it's parent, needed for XYscope + mixer
  ScopeDraw(PApplet theParent, String scopeID) {

    // initialize XYscope with custom sound out
    xy = new XYscope(theParent, scopeID);

    v = random(1, 10);
    s = floor(random(10, 100));
    x = -s;
    y = height/2-s/2;
  }

  void display() {
    // clear waves like refreshing background
    xy.clearWaves();

    x += v;
    if (x > width) {
      x = -s;
      v = random(1, 10);
    }

    xy.rect(x, y, s, s);

    // build audio from shapes
    xy.buildWaves();

    // draw just two analytics
    xy.drawWave();
    xy.drawXY();
  }

  // process keyPressed
  void checkKey(int keyC) {
    if (keyC == 38) { // UP
      xy.freq(xy.freq().x+.5);
    } else if (keyC == 40) { // DOWN
      xy.freq(xy.freq().x-.5);
    }
  }
}