/* 
 clock
 Extending Processing Clock example for XYscope
 https://processing.org/examples/clock.html
 cc teddavis.org 2017
 */

// import and create instance of XYscope
import xyscope.*;
XYscope xy;

// minim is required to generate audio
import ddf.minim.*; 

int cx, cy;
float secondsRadius;
float minutesRadius;
float hoursRadius;
float clockDiameter;

void setup() {
  size(512, 512);
  stroke(255);

  // initialize XYscope with default sound out
  xy = new XYscope(this);

  // for clock
  int radius = min(width, height) / 2;
  secondsRadius = radius * 0.8;
  minutesRadius = radius * 0.60;
  hoursRadius = radius * 0.40;
  clockDiameter = radius * .8;

  cx = width / 2;
  cy = height / 2;
}

void draw() {
  background(0);

  // clear waves like refreshing background
  xy.clearWaves();

  // Angles for sin() and cos() start at 3 o'clock;
  // subtract HALF_PI to make them start at the top
  float s = map(second(), 0, 60, 0, TWO_PI) - HALF_PI;
  float m = map(minute() + norm(second(), 0, 60), 0, 60, 0, TWO_PI) - HALF_PI; 
  float h = map(hour() + norm(minute(), 0, 60), 0, 24, 0, TWO_PI * 2) - HALF_PI;

  // Draw the hands of the clock
  int dots = 10;
  float step = map(second(), 0, 59, 1, .1);
  for (int i=0; i<int(dots); i++) {
    xy.line(cx, cy, cx + cos(s) * secondsRadius*i/dots, cy + sin(s) * secondsRadius*i/dots);
  }
  for (int i=1; i<dots; i++) {
    xy.line(cx, cy, cx + cos(m) * minutesRadius*i/dots, cy + sin(m) * minutesRadius*i/dots);
  }
  for (int i=1; i<dots; i++) {
    xy.line(cx, cy, cx + cos(h) * hoursRadius*i/dots, cy + sin(h) * hoursRadius*i/dots);
  }

  // Draw the minute ticks
  for (int a = 0; a < 360; a+=6) {
    float angle = radians(a);
    float x = cx + cos(angle) * secondsRadius;
    float y = cy + sin(angle) * secondsRadius;
    xy.point(x, y);
  }

  // build audio from shapes
  xy.buildWaves();

  // draw all analytics
  xy.drawAll();
}

// adjust freq() with arrow keys
void keyPressed() {
  if (keyCode == 38) { // UP
    xy.freq(xy.freq().x+.5);
  } else if (keyCode == 40) { // DOWN
    xy.freq(xy.freq().x-.5);
  }
}