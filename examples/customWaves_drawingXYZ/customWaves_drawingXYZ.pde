/* 
 customWaves_drawing
 Test out our sound cards by manually drawing the wave form.
 Very useful for testing just the z-axis blanking parallel to another sketch
 DELETE - resets waveforms
 cc teddavis.org 2017
 */

// import and create instance of XYscope
import xyscope.*;
XYscope xy;

// minim is required to generate audio
import ddf.minim.*; 

// setup local waveforms
float[] x, y, z;
int dragDist = 14;
int dragRegion = 0;

void setup() {
  size(400, 600, P3D);
  background(0);

  // initialize XYscope with custom outXY, outZ
  // left blank so it runs, but fill in with your audio card
  xy = new XYscope(this, "", "");

  // initialize waves
  resetWaves();
}


void draw() {
  background(0);

  // display text and color boxes
  displayInfo();

  // build each wave custom from our local waveform
  xy.buildX(x);
  xy.buildY(y);
  xy.buildZ(z);

  // draw waveform analytics
  xy.drawWaveform();
}

void mousePressed() {
  // only draw in region clicked on
  if (mouseY > height*.125 && mouseY < height*.375) {
    dragRegion = 1;
  } else if (mouseY > height*.375 && mouseY < height*.625) {
    dragRegion = 2;
  } else if (mouseY > height*.625 && mouseY < height*.875) {
    dragRegion = 3;
  }
}

void mouseReleased() {
  // release custom region
  dragRegion = 0;
}

void mouseDragged() {
  // modify waveform based on drawing
  for (int i=0; i < x.length; i++) { 
    int mappedX = floor(map(mouseX, 0, width, 0, x.length));
    if (abs(i-mappedX) < dragDist) {
      if (mouseY > height*.125 && mouseY < height*.375 && dragRegion == 1) {
        float mappedY = map(mouseY, height*.375, height*.125, 0, 1);
        x[i] = mappedY;
      } else if (mouseY > height*.375 && mouseY < height*.625 && dragRegion == 2) {
        float mappedY = map(mouseY, height*.375, height*.625, 0, 1);
        z[i] = mappedY;
      } else if (mouseY > height*.625 && mouseY < height*.875 && dragRegion == 3) {
        float mappedY = map(mouseY, height*.625, height*.875, 0, 1);
        y[i] = mappedY;
      }
    }
  }
}

void keyPressed() {
  // reset waves on DELETE
  if (keyCode == 8) { // DELETE
    resetWaves();
  }
}

void resetWaves() {
  x = new float[xy.waveSize()];
  y = new float[xy.waveSize()];
  z = new float[xy.waveSize()];

  for (int i=0; i < x.length; i++) {
    x[i] = .5;
    y[i] = .5;
    z[i] = .5;
  }
}

void displayInfo() {
  noStroke();
  fill(255, 0, 0, 50);
  rect(0, height*.125, width, height*.25);
  fill(0, 255, 0, 50);
  rect(0, height*.375, width, height*.25);
  fill(0, 0, 255, 50);
  rect(0, height*.625, width, height*.25);
  fill(255, 0, 0);
  text("X - LEFT/HORIZONTAL", 5, height*.125, width, 100);
  fill(0, 255, 0);
  text("Z - BLANKING", 5, height*.375, width, 100);
  fill(0, 0, 255);
  text("Y - RIGHT/VERTICAL", 5, height*.625, width, 100);
}