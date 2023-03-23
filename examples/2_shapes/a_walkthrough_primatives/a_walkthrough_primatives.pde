/*
 a_walkthrough_primatives
 demo-run of available shapes
 cc teddavis.org 2023
 */

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

int shapeSel = 0;

void setup() {
  size(512, 512, P3D);

  // initialize XYscope with default sound out
  xy = new XYscope(this);
}

void draw() {
  background(0);
  xy.limitPath(0); // limit drawing any points outside of view
  xy.steps(100); // interpolated points between points

  // clear waves like refreshing background
  xy.clearWaves();

  if (frameCount%240==0) {
    shapeSel++;
  }

  pushMatrix();
  translate(width/2, height/2);
 
  // use most primative shapes with class instance infront
  switch(shapeSel%10) {
  case 0:
    xy.freq(1);
    xy.point(random(-width/2, width/2), random(-height/2, height/2));
    break;
  case 1:
    xy.freq(12);
    for (int i=0; i<4; i++) {
      pushMatrix();
      rotate(radians(frameCount*i*.2));
      float lineLen = sin(frameCount*.01 + i*1)*width/2;
      xy.line(0, -lineLen, 0, lineLen);
      popMatrix();
    }
    break;
  case 2:
    float efreq = frameCount*.1%30;//sin(frameCount*.005)*30;
    xy.freq(25);
    for (int i=0; i < 3; i++) {
      pushMatrix();
      rotate(radians(i*120+frameCount*.1));
      translate(width/4, 0);
      xy.ellipseDetail(noise(i*2+frameCount*.01)*12);
      xy.ellipse(0, 0, width/4);
      popMatrix();
    }
  break;
case 3:
  xy.freq(50);
  xy.rectMode(CENTER);
  pushMatrix();
  rotateY(radians(frameCount));
  xy.rect(0, 0, width/2, height/2);
  rotateX(radians(frameCount));
  xy.rect(0, 0, width/3, height/3);
  popMatrix();
  break;
case 4:
  xy.freq(75);
  //randomSeed(1);
  pushMatrix();
  rotateY(radians(frameCount/3));
  xy.beginShape();
  for (int i=0; i < 10; i++) {
    float d = width/2;
    xy.vertex(noise(i*5+frameCount*.001)*d*2 - d, noise(i*20+frameCount*.003)*d*2 - d, noise(i*10+frameCount*.002)*d*2 - d);
  }
  xy.endShape(CLOSE);
  popMatrix();
  break;
case 5:
  xy.freq(50);
  xy.lissajous(0, 0, height/2, 1, 2, frameCount, 180);
  break;
case 6:
  xy.freq(50);
  pushMatrix();
  rotateY(radians(frameCount));
  rotateX(radians(frameCount/2));
  float bsize = width/2;
  xy.box(sin(frameCount*.01)*bsize, bsize, cos(frameCount*.01)*bsize);
  popMatrix();
  break;
case 7:
  pushMatrix();
  rotateY(radians(frameCount));
  xy.sphere(width/2.5, floor(noise(frameCount*.01)*24), floor(noise(10+frameCount*.011)*24));
  popMatrix();
  break;
case 8:
  pushMatrix();
  rotateY(radians(frameCount));
  xy.torus(width/4, width/6, floor(noise(frameCount*.01)*12), floor(noise(10+frameCount*.011)*24));
  popMatrix();
  break;
case 9:
  xy.freq(8);
  xy.textAlign(CENTER, CENTER);
  xy.textSize(50 + sin(frameCount*.025)*25);
  pushMatrix();
  xy.text("XYscope", 0, sin(frameCount*.05)*height/3);
  popMatrix();
  break;
}
popMatrix();

// build audio from shapes
xy.buildWaves();

// draw analytics
xy.drawWaveform();
xy.drawXY();
}

void keyPressed() {
  if (key == ' ') {
    shapeSel++;
  }
}
