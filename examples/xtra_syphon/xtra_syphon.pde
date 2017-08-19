/* 
 xtra_syphon
 Images from any syphon source on the scope! 
 mouseX - threshold
 mouseY - threshold distance
 
 » Requires OpenCV for Processing + Syphon libraries
 
 cc teddavis.org + jankenpopp.com 2017
 */

//PREFS
int threshold = 138;
float thresholdDist = 43;
int cutoff = 91; // limit min size contour

// import and create instance of XYscope
import xyscope.*;
XYscope xy;

// minim is required to generate audio
import ddf.minim.*; 

// syphon is required for passing imagery
import codeanticode.syphon.*;
SyphonClient client;
PImage imgs; // syphon client
PImage imgo; // syphon resize
PImage imgf; // syphon threshold

// libs required for point sorting (efficient drawing)
import java.util.Collections;
import java.util.Comparator;

//opencv
import gab.opencv.*;
import java.awt.*;
OpenCV opencv;
ArrayList<Contour> contours;

void setup() {
  size(512, 512, P3D);

  // initialize XYscope with default/custom sound out
  xy = new XYscope(this, "");

  // initialize syphon client
  client = new SyphonClient(this);

  // initialize OpenCV (used to convert syphon to single line)
  opencv = new OpenCV(this, width, height);
}

void draw() {
  background(0);

  // only up date when sypon sends new image
  if (client.newFrame()) {
    imgs = client.getImage(imgs);
  }
  if (imgs != null) {
    imgf = imgs.get();
    if (imgf.width > imgf.height) {
      imgf.resize(width, 0);
    } else {
      imgf.resize(0, height);
    }
    imgo = imgf.get();
    // clear waves like refreshing background
    xy.clearWaves();

    // adjust threshold of image for selective lines
    if (mousePressed) {
      threshold = floor(map(mouseX, 0, width, 0, 255));
      thresholdDist = map(mouseY, 0, height, 0, 255-threshold);

      // replace variable defaults at top if you find better ones
      println("threshold: "+threshold +" / thresholdDist: "+ thresholdDist);
    }

    // convert syphon to high contrast threshold
    for (int i=0; i<imgf.width*imgf.height; i++) {
      if (brightness(imgf.pixels[i]) > threshold && brightness(imgf.pixels[i]) < threshold+thresholdDist) {
        imgf.pixels[i]  = color(200); // White
      } else {
        imgf.pixels[i]  = color(0); // Black
      }
    }

    // process threshold to single line
    opencv.loadImage(imgf);
    opencv.dilate();
    opencv.erode();

    // display syphon original, threshold, opencv images – w/ keys 1, 2, 3
    if (keyPressed) {
      if (key == '1')
        image(imgo, 0, 0);
      if (key == '2')
        image(imgf, 0, 0);
      if (key == '3') {
        PImage otemp = opencv.getSnapshot();
        image(otemp, 0, 0);
      }
    }

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