/*
 shapes_lissajous
 Sometimes you just want some lissajous curves!
 cc teddavis.org 2023
 */

// import and create instance of XYscope
import xyscope.*;
XYscope xy;

// minim is required to generate audio
import ddf.minim.*;

void setup() {
  size(512, 512);
  background(0);

  // initialize custom local minim
  xy = new XYscope(this);
}


void draw() {
  background(0);
  xy.clearWaves(); // clear waves
  
  int res = 180;
  if(mousePressed){
    res = mouseX;
    println("resolution: " + res);
  }

  xy.lissajous(width*.25, height*.25, height/4.5, 1, 1, frameCount, res);
  xy.lissajous(width*.75, height*.25, height/4.5, 1, 2, frameCount, res);
  xy.lissajous(width*.25, height*.75, height/4.5, 1, 3, frameCount, res);
  xy.lissajous(width*.75, height*.75, height/4.5, 1, 4, frameCount, res);
 
  xy.buildWaves(); // build waves
  xy.drawAll(); // draw analytics

}
