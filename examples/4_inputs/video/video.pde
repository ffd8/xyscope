/* 
 video
 Import and render videos in XYscope
 
 mouseX - threshold
 mouseY - threshold distance
 keys 1, 2, 3 - view original, threshold, opencv image
 
 ** some strange behavior with Processing 4+...
 
 » Requires OpenCV for Processing + Video libraries
 cc teddavis.org 2018
 */

//PREFS
String moviePath = "BigBuckBunny_320x180_trim.mp4";
int threshold = 108;
float thresholdDist = 80;
int cutoff = 91; // limit min size contour

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

// video library and instance
import processing.video.*;
Movie movie;

// OpenCV imagesteps
PImage imgResize; // movie resize
PImage imgThres; // movie threshold

// libs required for point sorting (efficient drawing)
import java.util.Collections;
import java.util.Comparator;

//opencv
import gab.opencv.*;
import java.awt.*;
OpenCV opencv;
ArrayList<Contour> contours;

void setup() {
  size(512, 512);

  // initialize XYscope with default/custom sound out
  xy = new XYscope(this, "");

  // initialize video
  movie = new Movie(this, moviePath);
  movie.play();
  movie.loop();
  movie.volume(0);

  // initialize OpenCV (used to convert syphon to single line)
  opencv = new OpenCV(this, width, height);
}

void draw() {
  background(0);

  if (movie.available() == true) {
    movie.read();
  }

  if (movie != null) {
    // clear waves like refreshing background
    xy.clearWaves();

    // resize movie to fit canvas
    imgResize = movie.get();
    if (imgResize.width > imgResize.height) {
      imgResize.resize(width, 0);
    } else {
      imgResize.resize(0, height);
    }
    imgThres = imgResize.get();

    // adjust threshold of image for selective lines
    if (mousePressed) {
      threshold = floor(map(mouseX, 0, width, 0, 255));
      thresholdDist = map(mouseY, 0, height, 0, 255-threshold);

      // replace variable defaults at top if video needs specific ones
      println("threshold: "+threshold +" / thresholdDist: "+ thresholdDist);
    }

    // convert video to high contrast threshold
    for (int i=0; i<imgThres.width*imgThres.height; i++) {
      if (brightness(imgThres.pixels[i]) > threshold && brightness(imgThres.pixels[i]) < threshold+thresholdDist) {
        imgThres.pixels[i]  = color(255); // White
      } else {
        imgThres.pixels[i]  = color(0); // Black
      }
    }

    // process threshold to single line
    opencv.loadImage(imgThres);
    opencv.dilate();
    opencv.erode();
    contours = opencv.findContours(true, false);

    // display video original, threshold, opencv images – w/ keys 1, 2, 3
    if (keyPressed) {
      if (key == '1')
        image(imgResize, 0, 0);
      if (key == '2')
        image(imgThres, 0, 0);
      if (key == '3') {
        PImage otemp = opencv.getSnapshot();
        image(otemp, 0, 0);
      }
    }


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
  }

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
