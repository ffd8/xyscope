/* 
 fonts
 Let's draw type on the scope! 
 ANYKEY - type out
 DELETE - clear text

 » Requires Geomerative library
 cc teddavis.org 2017
 */

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

// geomerative is required to generate text points
import geomerative.*; 
RShape grp; 
RPoint[][] pointPaths;

// store our text to draw
String txtString = "XYscope";
String fontName = "FreeSans.ttf";

void setup() {
  size(512, 512);

  // initialize XYscope with default/custom sound out
  xy = new XYscope(this, "");

  // initialize Geomerative
  RG.init(this);
}

void draw() {
  background(0);

  // clear waves like refreshing background
  xy.clearWaves(); 

  // render type with Geomerative
  grp = RG.getText(txtString, fontName, width/2, CENTER); 
  grp.centerIn(g, 30);
  RG.setPolygonizer(RG.UNIFORMSTEP);
  RG.setPolygonizerStep(10);
  pointPaths = grp.getPointsInPaths();

  pushMatrix();
  translate(width/2, height/2); 
  if (pointPaths != null) { // only draw if we have points 
    for (int i = 0; i < pointPaths.length; i++) { 
      xy.beginShape(); 
      for (int j=0; j < pointPaths[i].length; j++) { 
        xy.vertex(pointPaths[i][j].x, pointPaths[i][j].y);
      } 
      xy.endShape();
    }
  } 
  popMatrix();

  // build audio from shapes
  xy.buildWaves();

  // draw Wave + XY analytics
  xy.drawWave();
  xy.drawXY();
}

void keyPressed() {
  if (keyCode == 8) {
    xy.clearWaves();
    txtString = "";
  } else if (keyCode != 16 && keyCode != 17 && keyCode != 18 && keyCode != 157 && keyCode != 37 && keyCode != 38 && keyCode != 39 && keyCode != 40) {
    txtString += key+"";
  }
}
