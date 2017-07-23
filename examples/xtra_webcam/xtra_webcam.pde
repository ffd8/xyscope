/* 
 xtra_type
 Let's draw type on the scope! 
 mouseX - threshold
 mouseY - threshold distance
 
 » Requires OpenCV for Processing + Video libraries
 
 cc teddavis.org 2017
 */

// import and create instance of XYscope
import xyscope.*;
XYscope xy;

// minim is required to generate audio
import ddf.minim.*; 

// video is required for webcam
import processing.video.*;
Capture video;

// libs required for point sorting (efficient drawing)
import java.util.Collections;
import java.util.Comparator;

//opencv
import gab.opencv.*;
import java.awt.*;
OpenCV opencv;
ArrayList<Contour> contours;
int cutoff = 91;
int threshold = 65;
float thresholdDist = 115;
PImage p;

void setup() {
  size(512, 512, P3D);

  // initialize XYscope with default/custom sound out
  xy = new XYscope(this, "");

  // initialize video capture
  video = new Capture(this, 640, 480);
  video.start();
  p = new PImage(video.width, video.height);

  // initialize OpenCV (used to convert webcam to single line)
  opencv = new OpenCV(this, p.width, p.height);
}

void draw() {
  background(0);

  // clear waves like refreshing background
  xy.clearWaves();

  // adjust threshold of image for selective lines
  if (mousePressed) {
    threshold = floor(map(mouseX, 0, width, 0, 255));
    thresholdDist = map(mouseY, 0, height, 0, 255-threshold);

    // replace variable defaults at top if you find better ones
    println(threshold +" / "+ thresholdDist);
  }

  // convert video to high contrast threshold
  video.loadPixels();
  for (int i=0; i<p.width*p.height; i++) {
    if (brightness(video.pixels[i]) > threshold && brightness(video.pixels[i]) < threshold+thresholdDist) {
      p.pixels[i]  = color(0); // White
    } else {
      p.pixels[i]  = color(255); // Black
    }
  }
  p.updatePixels();

  // process threshold to single line
  opencv.loadImage(p);
  opencv.flip(OpenCV.HORIZONTAL);
  opencv.dilate();
  contours = opencv.findContours(true, false);

  // sort group of lines for effeciant drawing
  Collections.sort(contours, new MyComparator());

  // draw shapes on scope
  for (Contour contour : contours) {
    if (contours.size() > 0) {
      contour.setPolygonApproximationFactor(1);
      if (contour.numPoints() > cutoff) {        
        xy.beginShape();
        for (PVector point : contour.getPolygonApproximation().getPoints()) {
          xy.vertex(point.x, point.y);
        }
        xy.endShape();
      }
    }
  }

  // build audio from shapes
  xy.buildWaves();

  // draw XY analytics
  xy.drawXY();
}

// used for sorting points
class MyComparator implements Comparator<Contour> {
  @Override
    public int compare(Contour o1, Contour o2) {
    if (o1.numPoints() > o2.numPoints()) {
      return -1;
    } else if (o1.numPoints() < o2.numPoints()) {
      return 1;
    }
    return 0;
  }
}