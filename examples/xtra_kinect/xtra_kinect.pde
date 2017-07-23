/* 
 xtra_kinect
 Turn the kinect depth image into a nice silhouette! 
 
 » Requires OpenCV for Processing + openkinect libraries
 
 cc teddavis.org 2017
 */

/* PREFS */
int kinectVer = 1; //(1 = xbox 360 vs 2 = xbox one)

// import and create instance of XYscope
import xyscope.*;
XYscope xy;

// minim is required to generate audio
import ddf.minim.*; 

// libs required for point sorting (efficient drawing)
import java.util.Collections;
import java.util.Comparator;

// libs + settings for kinect
import org.openkinect.freenect.*;
import org.openkinect.processing.*;
Kinect kinect;
Kinect2 kinect2;

// Depth image
PImage depthImg;

// Which pixels do we care about?
int minDepth =  60;
int maxDepth = 960;

// What is the kinect's angle
float angle;

//opencv + settings
import gab.opencv.*;
import java.awt.*;
OpenCV opencv;
ArrayList<Contour> contours;
int thres = 30;
int cutoff = 100;

void setup() {
  size(512, 512);

  // initialize XYscope with default/custom sound out
  xy = new XYscope(this, "");

  // pick kinect version (1 works great)
  if (kinectVer == 1) {
    kinect = new Kinect(this);
    kinect.initDepth();
    angle = kinect.getTilt();
    depthImg = new PImage(kinect.width, kinect.height);
  } else if (kinectVer == 2) {
    kinect2 = new Kinect2(this);
    kinect2.initDepth();
    kinect2.initDevice();
    depthImg = new PImage(kinect2.depthWidth, kinect2.depthHeight);
  }

  // initialize OpenCV (used to convert webcam to single line)
  opencv = new OpenCV(this, depthImg.width, depthImg.height);
}


void draw() {
  background(0);

  // clear waves like refreshing background
  xy.clearWaves();

  // kinect depth data
  int[] rawDepth = {};
  if (kinectVer == 1) {
    rawDepth = kinect.getRawDepth();
  } else if (kinectVer == 2) {
    rawDepth = kinect2.getRawDepth();
  }

  // create b/w threshold image from depth data
  for (int i=0; i < rawDepth.length; i++) {
    if (rawDepth[i] >= minDepth && rawDepth[i] <= maxDepth) {
      depthImg.pixels[i] = color(255);
    } else {
      depthImg.pixels[i] = color(0);
    }
  }

  // draw the thresholded image
  depthImg.updatePixels();
  //image(depthImg, 0, 0);

  // process threshold to single line
  opencv.loadImage(depthImg);
  opencv.dilate();
  opencv.flip(OpenCV.HORIZONTAL);
  contours = opencv.findContours(true, false);

  // sort group of lines for effeciant drawing
  Collections.sort(contours, new MyComparator());

  // draw shapes on scope
  for (Contour contour : contours) {
    if (contours.size() > 0) {
      contour.setPolygonApproximationFactor(1);
      xy.beginShape();
      for (PVector point : contour.getPolygonApproximation().getPoints()) {
        xy.vertex(point.x, point.y);
      }
      xy.endShape();
    }
  }

  // build audio from shapes
  xy.buildWaves();

  // draw Path + XY analytics
  xy.drawXY();
  xy.drawPath();
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