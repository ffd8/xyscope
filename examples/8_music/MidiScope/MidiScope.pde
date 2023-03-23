/*
  MidiScope
  use a MIDI file to set the freq of any XYscope graphics!
  cc teddavis.org 2019-23
*/

String midiTrack = "teapot.MID"; // filename in /data folder
int midiTranspose = -3; // shift key of midi track
float midiBPM = 91;

import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance

import ddf.minim.ugens.*; // freq from pitch
// MIDI code at bottom

PShape obj;

void setup() {
  size(512, 512, P3D);

  xy = new XYscope(this);

  setupMidi(midiTrack);
  obj = loadShape("teapot.obj");
}

void draw() {
  background(0);

  // 3D teapot!
  xy.clearWaves();
  pushMatrix();
  translate(width/2, height/2);
  scale(2);
  rotateZ(PI);
  rotateY(radians(frameCount+ xy.freq().x));

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
  popMatrix();

  xy.buildWaves();
  xy.drawXY();
  xy.drawWaveform();
}

void mouseDragged() {
  xy.point(mouseX, mouseY);
}

void keyPressed() {
  if (keyCode == 8) { // DELETE
    xy.clearWaves();
  }
}

// MIDI details
import javax.sound.midi.*;
Sequencer sequencer;
Sequence sequence;

class MidiReceiver implements Receiver {
  void close() { }
  
  void send( MidiMessage msg, long timeStamp ) {
    if ( msg instanceof ShortMessage ) {
      ShortMessage sm = (ShortMessage)msg;
      if ( sm.getCommand() == ShortMessage.NOTE_ON ) {
        int note = sm.getData1();
        int vel  = sm.getData2();
        //xy.amp(map(vel, 0, 127, 0, 1));
        xy.freq(Frequency.ofMidiNote(note + (midiTranspose * 12)).asHz());
        xy.resetWaves();
      }
    }
  }
}

void setupMidi(String midiFile) {
  // load midi
  try {
    sequencer = MidiSystem.getSequencer( false );
    sequencer.open();
    sequence = MidiSystem.getSequence( createInput( midiFile ) );
    sequencer.setSequence( sequence );
    sequencer.setTempoInBPM( midiBPM );
    sequencer.getTransmitter().setReceiver( new MidiReceiver() );
    sequencer.setLoopCount( Sequencer.LOOP_CONTINUOUSLY );
    sequencer.start();
  } catch( MidiUnavailableException ex ) {
    // no sequencer
    println( "No default sequencer, sorry bud." );
  } catch( InvalidMidiDataException ex ) {
    // oops, the file was bad
    println( "The midi file was hosed or not a midi file, sorry bud." );
  } catch( IOException ex ) {
    println( "Had a problem accessing the midi file, sorry bud." );
  }
}
