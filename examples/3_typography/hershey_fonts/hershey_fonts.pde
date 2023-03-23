/*
 text_hershey_fonts
 draw single-line text with hershey fonts!
 lots of functions to learn from below
 big thanks for hershey font lib: https://github.com/ixd-hof/HersheyFont
 cc teddavis.org 2023
 */

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

String txt = "XYscope";

void setup() {
  size(512, 512, P3D);
  xy = new XYscope(this);

  // list all available fonts
  println("Available Hershey Fonts:\n" + join(xy.fonts, ", ")); // list available fonts

  // set random font
  //xy.textFont(xy.fonts[floor(random(xy.fonts.length))]);
}

void draw() {
  background(0);
  xy.clearWaves();
  xy.steps(50); // set segment multiplier

  // draw text
  xy.textSize(50);
  xy.textAlign(CENTER, CENTER);
  xy.text(txt, width/2, height*.25);

  // draw text per character
  float xoff = -xy.textWidth(txt)/2.5;
  for (int i=0; i<txt.length(); i++) {
    push();
    translate(width/2+xoff, height*.5);
    rotateX(radians(i*15+frameCount));
    xy.text(txt.charAt(i)+"", 0, 0);
    pop();
    xoff += xy.textWidth(txt.charAt(i));
  }


  // get and draw paths of text
  PVector[][] textPaths = xy.textPaths(txt, width/2, floor(height*.75));
  for (int i = 0; i < textPaths.length; i++) {
    xy.beginShape();
    for (int j = 0; j < textPaths[i].length; j++) {
      float offset = 10;
      xy.vertex(textPaths[i][j].x+sin(j*.2+frameCount*.05)*offset, textPaths[i][j].y);
    }
    xy.endShape();
  }

  xy.buildWaves();

  xy.drawWaveform(); // wavetable
  xy.drawXY(); // scope viewer
}
