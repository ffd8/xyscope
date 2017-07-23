/* 
 P3D_toroid
 Minimal version of Ira Greenbergs Toroid example
 for demonstrating P3D points and transformations can be used.
 - mouseX » adjust pts
 - mouseY » adjust segments
 - keyboard » look below for details
 https://processing.org/examples/toroid.html
 cc teddavis.org 2017
 */

// import and create instance of XYscope
import xyscope.*;
XYscope xy;

// minim is required to generate audio
import ddf.minim.*; 


int pts = 40; 
float angle = 0;
float radius = 100.0;

// lathe segments
int segments = 20;
float latheAngle = 0;
float latheRadius = 100.0;

//vertices
PVector vertices[], vertices2[];

// for optional helix
boolean isHelix = false;
float helixOffset = 5.0;

void setup() {
  size(512, 512, P3D);

  // initialize XYscope with default sound out
  xy = new XYscope(this);
}

void draw() {
  background(0);

  // clear waves like refreshing background
  xy.clearWaves();
  
  // change shape with mouse
  pts = floor(map(mouseX, 0, width, 1, 50));
  segments = floor(map(mouseY, 0, height, 1, 50));

  //center and spin toroid
  pushMatrix();

  translate(width/2, height/2, -100);

  rotateX(frameCount*PI/150);
  rotateY(frameCount*PI/170);
  rotateZ(frameCount*PI/90);

  // initialize point arrays
  vertices = new PVector[pts+1];
  vertices2 = new PVector[pts+1];

  // fill arrays
  for (int i=0; i<=pts; i++) {
    vertices[i] = new PVector();
    vertices2[i] = new PVector();
    vertices[i].x = latheRadius + sin(radians(angle))*radius;
    if (isHelix) {
      vertices[i].z = cos(radians(angle))*radius-(helixOffset* 
        segments)/2;
    } else {
      vertices[i].z = cos(radians(angle))*radius;
    }
    angle+=360.0/pts;
  }

  // draw toroid
  latheAngle = 0;
  for (int i=0; i<=segments; i++) {
    xy.beginShape();

    for (int j=0; j<=pts; j++) {
      if (i>0) {
        xy.vertex(vertices2[j].x, vertices2[j].y, vertices2[j].z);
      }
      vertices2[j].x = cos(radians(latheAngle))*vertices[j].x;
      vertices2[j].y = sin(radians(latheAngle))*vertices[j].x;
      vertices2[j].z = vertices[j].z;
      // optional helix offset
      if (isHelix) {
        vertices[j].z+=helixOffset;
      } 
      xy.vertex(vertices2[j].x, vertices2[j].y, vertices2[j].z);
    }
    // create extra rotation for helix
    if (isHelix) {
      latheAngle+=720.0/segments;
    } else {
      latheAngle+=360.0/segments;
    }
    xy.endShape();
  }
  popMatrix();

  // build audio from shapes
  xy.buildWaves();

  // draw all analytics
  xy.drawAll();
}

/*
 left/right arrow keys control ellipse detail
 up/down arrow keys control segment detail.
 'a','s' keys control lathe radius
 'z','x' keys control ellipse radius
 'w' key toggles between wireframe and solid
 'h' key toggles between toroid and helix
 */
void keyPressed() {
  if (key == CODED) { 
    // pts
    if (keyCode == UP) { 
      if (pts<40) {
        pts++;
      }
    } else if (keyCode == DOWN) { 
      if (pts>3) {
        pts--;
      }
    } 
    // extrusion length
    if (keyCode == LEFT) { 
      if (segments>3) {
        segments--;
      }
    } else if (keyCode == RIGHT) { 
      if (segments<80) {
        segments++;
      }
    }
  }
  // lathe radius
  if (key =='a') {
    if (latheRadius>0) {
      latheRadius--;
    }
  } else if (key == 's') {
    latheRadius++;
  }
  // ellipse radius
  if (key =='z') {
    if (radius>10) {
      radius--;
    }
  } else if (key == 'x') {
    radius++;
  }
  // helix
  if (key =='h') {
    if (isHelix) {
      isHelix=false;
    } else {
      isHelix=true;
    }
  }
}