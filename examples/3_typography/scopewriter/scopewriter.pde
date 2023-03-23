/*
 scopewriter
 use your oscilloscope as a word processor
 with additive synth (add-synth) drawing to animate + distort it
 
 DEL, clear last char
 SHIFT + DEL, clear whole text
 CTRL, generate new freq of add-synth
 mouseDragged, draw shape for add-synth (use + to center drawing)
 doubleCLick, clear add-synth drawing
 
 cc teddavis.org 2023
 */

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy, xy2;         // create XYscope instance

String txtString = "XYscope";
boolean shifted = false;
int doubleClicker = 0;

void setup() {
  size(512, 512);

  xy = new XYscope(this);
  //xy.getMixerInfo(); // lists all audio devices

  xy.textAlign(LEFT, TOP);
  xy.textSize(30);
  xy.freq(40);

  // add-synth instance of XYscope
  xy2 = new XYscope(this, xy.outXY);
  xy2.freq(39.7);
  xy2.line(width/2, height/2, width/2+50, height/2+50);
}

void draw() {
  background(0);
  xy.clearWaves();

  // draw shapes here
  xy.text(txtString, 10, 0);

  xy.buildWaves(); // build main waves
  xy2.buildWaves(); // build add-synth waves

  xy.drawXY(); // draw main XY visuals
  xy2.drawPath(); // draw add-synth path

  // grid to help center add-synth drawing
  noFill();
  pushMatrix();
  translate(width/2, height/2);
  stroke(0, 255, 255);
  line(0, -100, 0, 100);
  line(-100, 0, 100, 0);
  popMatrix();
}

void keyPressed() {
  //println(keyCode);

  // scopewriter logic
  if (keyCode == 8) {
    xy.clearWaves();
    if (shifted) {
      txtString = "";
    } else {
      if (txtString.length() > 0) {
        txtString = txtString.substring(0, txtString.length()-1);
      }
    }
  } else if (keyCode == 10) {
    txtString += "\r";
  } else if (keyCode != 27 && keyCode != 0 && keyCode != 65406 && keyCode != 9 && keyCode != 16 && keyCode != 20 && keyCode != 17 && keyCode != 18 && keyCode != 157 && keyCode != 37 && keyCode != 38 && keyCode != 39 && keyCode != 40) {
    if (xy.textWidth(txtString + key) > width) {
      String[] t = split(txtString, " ");
      if (key == ' ') {
        txtString += "\r";
        return;
      } else if (t.length == 1) {
        txtString += "\r";
      } else {
        txtString = txtString.replaceAll(" (?=[^ ]*$)", "\n");
      }
    }
    txtString += key+"";
  }

  // change freq of add-synth
  if (keyCode == 17) {
    xy2.freq(floor(random(25))*5+.5);
    println(xy2.freq().x);
  }

  if (keyCode == 16) {
    shifted = true;
  }
}


void keyReleased() {
  if (keyCode == 16) {
    shifted = false;
  }
}

void mouseDragged() {
  // draw add-synth shape
  xy2.line(mouseX, mouseY, pmouseX, pmouseY);
}

void mousePressed() {
  // double-clicker
  if (millis()-  doubleClicker < 500) {
    xy2.clearWaves();
  }
  doubleClicker = millis();
}
