/*
 freq_keyboard_notes
 Use your keyboard (A, B, C, D, E, F, G + 1, 2, 3, 4, 5, 6, 7..)
 to play musical pitches of your graphics!
 cc teddavis.org 2023
 */

// import and create instance of XYscope
import xyscope.*;
XYscope xy;


// minim is required to generate audio
import ddf.minim.*;

// ugens to convert pitch to freq
import ddf.minim.ugens.*;
String keyLetter = "A", keyOctave = "3";

void setup() {
  size(512, 512);

  // initialize XYscope with default sound out
  xy = new XYscope(this);
}

void draw() {
  background(0);
  xy.clearWaves(); // clear waves

  keyboardNotes(); // use keyboard to set note pitch (freq)

  xy.ellipse(width/2, width/2, width/2, width/2);

  xy.buildWaves(); // build audio from shapes
  xy.drawAll(); // draw all analytics
}

void keyboardNotes() {
  if (keyPressed) {
    if (key == 'a' || key == 'b' || key == 'c' || key == 'd' || key == 'e' || key == 'f' || key == 'g') {
      keyLetter = (key+"").toUpperCase();
    }
    if (key == '1' || key == '2' || key == '3' || key == '4' || key == '5' || key == '6' || key == '7' || key == '8' || key == '9' || key == '0') {
      keyOctave = key + "";
    }
    xy.freq(Frequency.ofPitch(keyLetter+keyOctave).asHz());
  }
}
