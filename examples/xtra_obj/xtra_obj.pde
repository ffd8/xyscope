/* 
 xtra_obj
 Import and render 3D obj files!
  
 cc teddavis.org 2018
 */
 
import xyscope.*;
XYscope xy;

// minim is required to generate audio
import ddf.minim.*; 

// obj file instance
PShape obj;

public void setup() {
  size(512, 512, P3D);

  // load 3D obj file
  obj = loadShape("teapot.obj");

  // initialize XYscope
  xy = new XYscope(this);
}

public void draw() {
  background(0);

  // modulate freq to make beats out of shapes!?
  if(mousePressed){
    xy.freq(map(mouseX, 0, width, 0, 50));
  }

  // clear XYscope waves
  xy.clearWaves();

  pushMatrix();
  translate(width/2, height/2);
  scale(2);
  rotateZ(PI);
  rotateY(radians(frameCount));

  // break 3D obj into its pieces
  int children = obj.getChildCount();

  // render vertices in XYscope
  xy.beginShape();
  for (int i = 0; i < children; i++) {
    PShape child = obj.getChild(i);
    int total = child.getVertexCount();

    for (int j = 0; j < total; j++) {
      PVector v = child.getVertex(j);
      xy.vertex(v.x, v.y, v.z);
    }
  }
  xy.endShape();

  // generate waveform
  xy.buildWaves();
  popMatrix();

  // draw waveform + xy simulation
  xy.drawWaveform();
  xy.drawXY();
}
