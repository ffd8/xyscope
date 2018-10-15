/*
  xtra_filters
 Use minim ugens audio filters.
 
 Click and drag mouse to adjust moog filter
 
 cc teddavis.org 2018
 */

// import and create instance of XYscope
import xyscope.*;
XYscope xy;

// minim is required to generate audio
import ddf.minim.*; 

// extra libs are required for filtering
import ddf.minim.ugens.*;
MoogFilter moog;
Flanger flange;
Delay myDelay;
ADSR adsr;


void setup() {
  size(512, 512);


  // initialize XYscope with default sound out
  xy = new XYscope(this, "", 96000);

  moog = new MoogFilter( 1200, 0.75 );
  moog.setChannelCount(2);

  myDelay = new Delay( 0.4, 0.5, false, false );
  myDelay.setChannelCount(2);

  flange = new Flanger( 0, // delay length in milliseconds ( clamped to [0,100] )
    0.2f, // lfo rate in Hz ( clamped at low end to 0.001 )
    1.0f, // delay depth in milliseconds ( minimum of 0 )
    0.5f, // amount of feedback ( clamped to [0,1] )
    0.0f, // amount of dry signal ( clamped to [0,1] )
    0.5f // amount of wet signal ( clamped to [0,1] )
    );
  flange.setChannelCount(2);

  //patch/unpatch moog
  xy.sumXY.unpatch(xy.outXY);
  xy.sumXY.patch(moog).patch(xy.outXY);

  //patch/unpatch delay
  //xy.sumXY.unpatch(xy.outXY);
  //xy.sumXY.patch(myDelay).patch(xy.outXY);

  //patch/unpatch flange
  //xy.sumXY.unpatch(xy.outXY);
  //xy.sumXY.patch(flange).patch(xy.outXY);
}

void draw() {
  background(0);

  // clear waves like refreshing background
  xy.clearWaves();

  // grid of squares
  int grid = 4; 
  for (int i=0; i < grid; i++) { 
    for (int j=0; j < grid; j++) { 
      xy.rect(i*width/grid, j*height/grid, width/grid, height/grid);
    }
  }

  // enable for delay demo
  //xy.rect(mouseX, mouseY, 200, 200);

  // adjust moog filter
  if (mousePressed) {
    float freq = constrain( map(mouseX, 0, width, 20, 12000), 20, 12000);
    float rez  = constrain( map(mouseY, height, 0, 0, 1), 0, 1);

    moog.frequency.setLastValue(freq);
    moog.resonance.setLastValue(rez);
  }


  // build audio from shapes
  xy.buildWaves();

  // draw all analytics
  xy.drawAll();
}

void keyPressed() {
  if ( key == '1' ) moog.type = MoogFilter.Type.LP;
  if ( key == '2' ) moog.type = MoogFilter.Type.HP;
  if ( key == '3' ) moog.type = MoogFilter.Type.BP;
}
