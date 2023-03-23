# XYscope
v 3.0.0  
cc [teddavis.org](https://teddavis.org) 2017 – 2023

Processing library to render vector graphics on vector displays.   

XYscope converts the coordinates of primative shapes (point, line, rect, ellipse, vertex, box, sphere, torus...)to audio waveforms (oscillators with custom wavetables) which are sent to an analog display, revealing their graphics. This process of generating real-time audio from vector drawings is built upon the amazing Minim library. Vector graphics shine on a vector display and now we can view our generative works like never before! Tested on MacOS, Windows, Linux (RPi!).

[teddavis.org/xyscope](http://www.teddavis.org/xyscope)  
[github.com/ffd8/xyscope](https://github.com/ffd8/xyscope)

## Table of Contents
- [Installation](#installation)
- [Audio Interfaces](#audio-interfaces)
- [Vector Displays](#vector-displays)
- [Getting Started](#getting-started)
- [Additive Synthesis](#additive-synthesis)
- [References](#references)
- [Extras ](#extras)

## Installation

Add via Processing's Library Manager:  

- Sketch menu » Import Library... » Add Library... (v4 Manage Libraries...)  
- Search for 'XYscope' and click `Install`.  
- Search for 'Minim' and click `Install`.

This library relies on and is infinitely thankful for [Minim](https://github.com/ddf/Minim)!  
Search for and add the following libraries for various examples:  
[Geomerative](https://github.com/rikrd/geomerative), [OpenCV for Processing](https://github.com/atduskgreg/opencv-processing), [openkinect](https://github.com/shiffman/OpenKinect-for-Processing), [video](https://github.com/processing/processing-video), [syphon](https://github.com/Syphon/Processing)

Add manually, [download latest release](https://github.com/ffd8/xyscope/releases/latest) and expand into `~/Processing/libraries`:

## Audio Interfaces

*< $5*  
At the very least you can use your computer's headphone jack with an [`1/8" to RCA`](https://duckduckgo.com/?q=1%2F8%22+to+RCA&t=h_&iax=images&ia=images) cable. However you'll soon want to get a [DC-Coupled](https://www.expert-sleepers.co.uk/siwacompatibility.html) audio interface for a cleaner and more stable visual (not wobbling/centering constantly). 

*< $20*  
For workshops I like to use some variant of the 48Khz [Delock 61645](https://www.delock.com/produkt/61645/merkmale.html) or 96Khz [Delock 63926](https://www.delock.com/produkt/63926/merkmale.html) – you'll find the same chip under many different brands and casings. For modern laptops, there's also a very compact 96Khz [Delock USB-C](https://www.delock.com/produkt/66304/merkmale.html) version.

*> $150+*  
Many of us in the community found the [MOTU Ultralite Mk3 Hybrid](https://motu.com/products/motuaudio/ultralite-mk3) (or newer versions) to be an ideal audio interface. Price varies from used to new. It offers, 10-channels of DC-Coupled 196Khz output, which is useful to drive multiple oscilloscopes or RGB lasers (X, Y, R, G, B).

### List
Check which interface is plugged in and how to reference it.

```
xy.getMixerInfo(); // lists available audio devices
```

### Select
By default XYscope uses the system settings audio-output, however we can specify a custom sound card (Digital Analog Converter – DAC) and sample rate.

```
xy = new XYscope(this); // system settings soundcard 
xy = new XYscope(this, "MOTU 1_2"); // specify a soundcard/channel to use 
xy = new XYscope(this, 96000); // custom sample rate ( > finer detail if card supports) 
xy = new XYscope(this, "MOTU 1_2", 96000); // custom soundcard, custom sample rate 
```

Or select a previous XYscope instance as the output for additive-synthesis:
```
xy2 = new XYscope(this, xy.outXY); // send 2nd instance to 1st
```

### Aggregate Device
If you have a multi-channel DAC, we need to create multiple 2ch stereo pairs, since Processing/Minim can't handel multi-channel devices. Especially useful with a great DAC like the MOTU Ultralite Mk3 Hybrid (or newer), offering 10ch of DC-Coupled outputs, so one could control 5 oscillscopes at once! Have only tested using MacOS:

- Press `CMD + Spacebar` (opens Spotlight Search)
- Type `Audio Midi Setup`, press `ENTER`
- Click `+` in lower left, select `Create Aggregate Device`
- Tick checkbox of your multi-channel audio device
- Click `Configure Speakers...` in lower right
- Set configuration to `Stereo`, select channels desired
- Rename to something like: `MOTU 1_2`
- Repeat above for each stereo pair, ie, `MOTU 3_4`, `MOTU 5_6`...
- They can now be selected by name when initializing XYscope!

### Multi-Channel Output Device
Within `Audio Midi Setup`, you can `Create Multi-Output Device` to send your output to multiple devices, ie. [Blackhole](https://existential.audio/blackhole/) + DAC + Speakers, so you can view it [virtually](https://www.oscilloscopemusic.com/software/oscilloscope/), on analog device, and hear it. 

- Press `CMD + Spacebar` (opens Spotlight Search)
- Type `Audio Midi Setup`, press `ENTER`
- Click `+` in lower left, select `Create Multi-Channel Device`
- Tick checkbox of best device first, then select additional
- Set sample rate to highest available, 48000 or 96000+
- Rename for clarity, ie `DAC + SPEAKERS` or `BLACKHOLE + SPEAKERS`.

## Vector Displays
Now we need a vector display to see our glowing output!

### Virtual
[Oscilloscope](https://www.oscilloscopemusic.com/software/oscilloscope/) by Hansi Raber (adopting [m1el's woscope](http://m1el.github.io/woscope-how/) 'physical rendering') is a fantastic way to see your XYscope drawings while on the go without a physical device. You'll want to install [Blackhole](https://existential.audio/blackhole/) (MacOS) or [VB-CABLE](https://vb-audio.com/Cable/index.htm) (Windows) to re-route your system audio to a virtual source for Oscilloscope to render it.

### Analog Oscilloscope
This is what we really want! They have a Cathode-ray Tube (CRT) that is the magic behind this obsession. You'll find them for ~$50 used on auction websites – be sure it has 2-channels (z-axis input is a bonus) and that they show images of a sharp working beam. You'll need a few [`RCA to BNC`](https://duckduckgo.com/?q=RCA+to+BNC&t=h_&iar=images&iax=images&ia=images) adaptors to interface with it. Have fun playing with all the knobs to put it into `XY Mode` so that the 2-channels drive the beam X/Y (Horizontal/Vertical).

### X-Y Monitor
Similar to an analog oscilloscope, but usually has a larger display and reduced controls for X-Y (+Z) input, leaving away many of the features on an oscilloscope we won't use. They're more rare, expensive, but great if you stumble upon one. Don't confuse these with a 'vector monitor' which is used for TV broadcast and won't draw X-Y coordinates.

### Vectrex
A vector-graphics video game system of the 1980s, these amazing 9" displays can be [very carefully modified](http://users.sussex.ac.uk/~ad207/adweb/assets/vectrexminijackinputmod2014.pdf) (**CAREFUL - at own risk**) to override (on-demand) the videogame control of the monitor's XYZ inputs. It's ideal to use [switching jacks](https://www.youtube.com/watch?v=q3sKZA2r7qk) so videogames still works when cables are unplugged. You'll also want to apply the [SPOT KILLER MOD](https://www.facebook.com/groups/vectorsynthesis/posts/832237263652516/), but BE SURE to apply an appropriately high-voltage rated switch, so it can be toggled on and off.

XYscope has a special mode when using a Vectrex for the aspect ratio. See example `vextrex` within `5_displays`, but the main notes are:

```
/* within setup() */
xy.vectrex(90); // -90/90 for landscape, 0 for portrait

// optionally z-axis for blanking
xy.z("MOTU 3-4"); // use custom 3rd channel for z-axis
//xy.zRange(.5, 0); // set min/max values for beam on/off
```

### Laser
Once you want something bigger than most screens, you'll want to move to an RGB Laser. They're BIG and BRIGHT, but also much slower and more dangerous! It's slower because it mechanically moves galvos/mirrors for the X-Y and dangerous, because, LASERS! Nevertheless, they can be controlled via the ILDA analog input, for which I've developed an easy to build [dac_ilda adaptor](https://github.com/ffd8/dac_ilda). To control a laser, you'll need a DAC (sound card) with a minimum of 5-channels, for sending `X, Y, R, G, B` signals. See example `laser` within `5_displays`, but the main notes are:

```
/* within setup() */
// only stereo pairs in processing, so it's broken to R, GB
// incase 2nd channel of R pair is useful for blanking/etc.
xy.laser("MOTU 3-4", "MOTU 5-6"); //xy.laser(mixerR, mixerGB);

/* within setup() or draw() */
// RGB waveforms have own freq, so we can them out of sync
xy.strokeFreq(50.05, 50.1, 50.2);
  
xy.strokeDash(8); // optional dashes in the RGB stroke

xy.stroke(0, 255, 255); // set RGB stroke before shapes (0-255)

xy.limitPath(0); // avoid drawing any forms beyond view
```

## Getting Started
Our basic template for an XYscope sketch involves:

```
import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy;         // create XYscope instance
 
void setup(){ 
  size(512, 512); // window size, optionally add P3D
 
  xy = new XYscope(this, ""); // define XYscope instance, "custom_dac" optional
  xy.getMixerInfo(); // lists all audio devices
} 
 
void draw(){ 
  background(0); 
  xy.clearWaves();  // clear waves from previous drawing
  
  // draw shapes here
  xy.circle(width/2, height/2, width); // it all began with a circle....
  
  xy.buildWaves(); // build shapes to audio waves
  xy.drawAll();
} 
```

## Additive Synthesis
Something very unique to this workflow is sending multiple audio signals to the oscilloscope, which interfer and modulate one another. As these waves combine, their amp and freq determine its influence on other waves. Key is having different frequencies and amplitudes to modulate off one another. The lower the frequency, the more it will push other waves around. The lower the amp, the less influence it has on the additive waveform. We can simply open two sketches, both being sent to the same sound card to see this in action, however we can also achieve it within a single sketch. 

```
import ddf.minim.*; // minim req to gen audio
import xyscope.*;   // import XYscope
XYscope xy, xy2;    // create 2x XYscope instances

void setup() {
  size(512, 512); // declares size of output window
  xy = new XYscope(this);
  xy2 = new XYscope(this, xy.outXY); // patch 2nd instance to 1st output
}

void draw() {
  background(0);
  // set main shapes
  xy.clearWaves();
  xy.circle(width/2, height/2, width/2);
  xy.buildWaves();
  xy.drawPath(0, 255, 255); // custom stroke color
  xy.drawXY(); // displays combined output

  // additive synth on 2nd instance of XYscope
  xy2.clearWaves();
  xy2.freq(mouseX); // without speakers, test really high freqs!
  //xy2.amp(.2); // experiment with high freq, low amp modulation
  xy2.circle(width/2, height/2, mouseY);
  xy2.buildWaves();
  xy2.drawPath(255, 255, 0); // custom stroke color
}
```
Additional tips:

- No need to stop at just 2, add as many as needed!  
- Ratio between freqs is crucial, diff of +/- .1 animates things. 
- Really low frequencies animate shapes over that path.  
- Really high frequencies display shape made of 2nd shape.
- Play with position of 2nd shape, from center to corners.

## References
XYscope is a class, so after an instance has been defined to a variable, we'll use that prefix in front of every function listed below, ie: `xy.ellipse()`. This enables us to have multiple XYscope instances running parallel, which will reveal wild and crazy audio/visuals. All examples below use `xy` as the instance prefix.

- [Initialize XYscope](#initialize-xyscope)
- [Z-axis](#z-axis)
  * [z()](#z())
  * [zAuto()](#zauto())
- [Audio Interface Settings](#audio-interface-settings)
  * [getMixerInfo()](#getmixerinfo())
  * [sampleRate()](#samplerate())
  * [bufferSize()](#buffersize())
- [Waves](#waves)
  * [resetWaves()](#resetwaves())
  * [clearWaves()](#clearwaves())
  * [buildWaves()](#buildwaves())
  * [buildX()](#buildx())
  * [buildY()](#buildy())
  * [buildZ()](#buildz())
  * [waveSize()](#wavesize())
  * [setWaveforms()](#setwaveforms())
- [Points](#points)
  * [wavePoints()](#wavepoints())
  * [steps()](#steps())
  * [limitPoints()](#limitpoints())
  * [limitPath()](#limitpath())
- [Record Audio](#record-audio)
  * [recorderBegin()](#recorderbegin())
  * [recorderEnd()](#recorderend())
- [Primitive Shapes](#primitive-shapes)
  * [point()](#point())
  * [line()](#line())
  * [square()](#square())
  * [rect()](#rect())
  * [circle()](#circle())
  * [ellipse()](#ellipse())
  * [complex shape](#complex-shape)
  * [lissajous()](#lissajous())
  * [box()](#box())
  * [sphere()](#sphere())
  * [ellipsoid()](#ellipsoid())
  * [torus()](#torus())
- [Text](#text)
  * [text()](#text())
  * [textPaths()](#textpaths())
  * [textFont()](#textfont())
  * [textSize()](#textsize())
  * [textLeading()](#textleading())
  * [textAlign()](#textalign())
  * [textWidth()](#textwidth())
- [Modulation](#modulation)
  * [freq()](#freq())
  * [amp()](#amp())
  * [pan()](#pan())
- [Vectrex](#vectrex)
- [Laser](#laser)
- [Rendering](#rendering)
- [Vars](#vars)

---
### Initialize XYscope
```
xy = new XYscope(this); // system audio settings
xy = new XYscope(this, "custom_dac"); // custom audio card
xy = new XYscope(this, xy_instance.outXY); // another XYscope out
xy = new XYscope(this, sampleRate); // default sampleRate is 41000
xy = new XYscope(this, "custom_dac", sampleRate); // default sampleRate is 41000
xy = new XYscope(this, "custom_dac", sampleRate, bufferSize); // default bufferSize is 512
```
---
### Z-axis
#### z()
```
xy.z("custom_dac"); // set audio out for z-axis (blanking)
xy.z("custom_dac", sampleRate); // add custom sampleRate
```
#### zAuto()
```
// somewhat deprecated.. may return
xy.zAuto(); // get setting for using z-axis
xy.zAuto(boolean); // set auto use of z-axis 
```
---
### Audio Interface Settings
#### getMixerInfo()
```
xy.getMixerInfo(); // list connected audio interfaces
```
#### sampleRate()
```
xy.sampleRate(); // get current sampleRate, default 41000
xy.sampleRate(newRate); // set new sampleRate (int)
```
#### bufferSize()
```
xy.bufferSize(); // get current bufferSize, default 512
xy.bufferSize(newSize); // set  new bufferSize (int)
```
---
### Waves
#### resetWaves()
```
xy.resetWaves(); // fix any sync issues with phase of waves
```
#### clearWaves()
```
xy.clearWaves(); // clear wavetables from any previous drawing
```
#### buildWaves()
```
xy.buildWaves(); // compile drawn shapes into new wavetables/audio
xy.buildWaves(-1); // draw in v1 style
xy.buildWaves(-2); // draw in v2 style
xy.buildWaves(-3); // draw in v3 style
```
#### buildX()
```
xy.buildX(newWave); // build wavetableX with float[] array
```
#### buildY()
```
xy.buildY(newWave); // build wavetableY with float[] array
```
#### buildZ()
```
xy.buildZ(newWave); // build wavetableZ with float[] array
```
#### waveSize()
```
xy.waveSize(); // get current size of wavetables
xy.waveSize(newSize); // set new wavetable size, default is 512 
```
#### setWaveforms()
```
xy.setWaveforms(wfX, wfY, wfZ); // set wavetables with float[] arrays 
```
---
### Points
#### wavePoints()
```
xy.wavePoints(); // get PVector ArrayList of all drawn coordinates (0.0 – 1.0)
```
#### steps()
```
xy.steps(); // get current steps between points, default 24
xy.steps(newVal); // set step multiplier, segments, between points
```
#### limitPoints()
```
xy.limitPoints(); // get number of limited drawing points
xy.limitPoints(newLimit); // set new limit of points for drawing
```
#### limitPath()
```
xy.limitPath(); // get current limit of drawn path
xy.limitPath(newLimit); // avoid drawn vectors beyond border edges (px)
```
---
### Record Audio
Easily save your drawings as .wav audio files! Just use a keyPress to initiate starting and stopping the recording (see demo). Recording uses our set sampleRate.
#### recorderBegin()
```
xy.recorderBegin(); // will name file "XYscope_timestamp.wav"
xy.recorderBegin("customName"); // set name, timestamp added
```
#### recorderEnd()
```
xy.recorderEnd(); // complete recording
```
---
### Primitive Shapes
Most primitives from Processing have been ported, so you only need to add `xy.` in front of them! They can also be used without parameters, for quickly testing.  

#### point()
```
xy.point();
xy.point(x, y);
xy.point(x, y, z);
```
#### line()
```
xy.line();
xy.line(x1, y1, x2, y2);
xy.line(x1, y1, z1, x2, y2, z2);
```
#### square()
```
xy.rectMode(); // default CORNER, CENTER to draw center out

xy.square();
xy.square(x, y, w);
```
#### rect()
```
xy.rectMode(); // default CORNER, CENTER to draw center out

xy.rect();
xy.rect(x, y, w);
xy.rect(x, y, w, h);
```
#### circle()
```
xy.ellipseDetail(); // get current facets of ellipse
xy.ellipseDetail(newVal); // set new count of facets, default 30

xy.circle();
xy.circle(x, y, w);
```
#### ellipse()
```
xy.ellipseDetail(); // get current facets of ellipse
xy.ellipseDetail(newVal); // set new count of facets, default 30

xy.ellipse();
xy.ellipse(x, y, w);
xy.ellipse(x, y, w, h);
```
#### complex shape
```
xy.beginShape();

xy.vertex();
xy.vertex(x, y);
xy.vertex(x, y, z); // if in P3D mode
// ...

xy.endShape();
xy.endShape(CLOSE); // closes form
```
#### lissajous()
```
xy.lissajous();
xy.lissajous(x, y, radius, ratioA, ratioB, phase, resolution);
```
#### box()
```
xy.box();
xy.box(size);
xy.box(rx, ryz);
xy.box(rx, ry, rz);
```
#### sphere()
```
xy.sphere();
xy.sphere(size);
xy.sphere(size, detailXY); // default 24
xy.sphere(size, detailX, detailY); // default 24, 24
```
#### ellipsoid()
```
xy.ellipsoid();
xy.ellipsoid(rx, ry, rz);
xy.ellipsoid(rx, ry, rz, detailXY); // default 24
xy.ellipsoid(rx, ry, rz, detailX, detailY); // default 24, 24
```
#### torus()
```
xy.torus();
xy.torus(radius, tubeRadius);
xy.torus(radius, tubeRadius, detailXY); // default 24
xy.torus(radius, tubeRadius, detailX, detailY); // default 24, 24
```
---
### Text
#### text()
```
xy.text(); // paramless text drawing
xy.text("string", x, y); // use '\n' for multi-line text
```
#### textPaths()
Use coordinates of Hershey text for manipulating type!

```
xy.textPaths("string", x, y); // return 2D Array of PVector coords
```
#### textFont()
```
println(xy.fonts); // print list of avilable Hershey fonts
xy.textFont("fontname"); // set active Hershey font
```
#### textSize()
```
xy.textSize(); // get current textSize
xy.textSize(newSize); // set new textSize
```
#### textLeading()
```
xy.textLeading(); // get current textLeading
xy.textLeading(newSize); // set new textLeading
```
#### textAlign()
```
xy.textAlign(hAlign); // Horz: LEFT (default) / CENTER / RIGHT
xy.textAlign(hAlign, vAlign); // Vert options: TOP / CENTER / BOTTOM
```
#### textWidth()
```
xy.textWidth(int); // from 32 characters, get width of char
xy.textWidth("string"); // get width of text for positioning
```
---
### Modulation
#### freq()
Frequency of oscillators

```
// get
xy.freq(); // get PVector (.x, .y, .z) of freqs
xy.freq().x;   // get frequency of x oscillator 

// set
xy.freq(freqXY);  // default is 50.0 
xy.freq(freqX, freqY); // set x, y frequencies 
xy.freq(freqX, freqY, freqZ); // set x, y, z frequencies 
xy.freq(PVector freqXYZ); // set as PVector 
 
xy.resetWaves(); // occasionally needed if they slip out of phase 
```
#### amp()
Amplitude of oscillators

```
// get
xy.amp();   // get PVector (.x, .y, .z) of amps
xy.amp().x; // get amplitude of x oscillator

// set
xy.amp(ampXY); // default is 1.0 
xy.amp(ampX, ampY); // set x, y to specific amplitudes 
xy.amp(ampX, ampY, ampZ); // set x, y, z to specific amplitudes 
xy.amp(PVector ampXYZ); // set as PVector
```
#### pan()
Optionally swap the hard pan of XY rather than physical cables

```
xy.pan(leftPan, rightPan); // default is -1.0, 1.0
```
---
### Vectrex
```
xy.vectrex(0); // 0 for portrait, -90/90 for landscape orientation
xy.vectrex(vw, vh, vamp, vrot); // custom width, height, amp, rotation

xy.vectrexRatio(); // get current ratio
xy.vectrexRatio(newRatio); // set new ratio, default is .82
```
---
### Laser
```
xy.laser("dac_Red", "dac_GreenBlue"); // custom 2ch pairs for R, GB

// be cautious of laser galvos, can help smooth graphics
xy.laserLPF(); // get value of laser's LowPassFilter
xy.laserLPF(newLPF); // set mew value of LowPassFilter (0.1 – 20000.0)

// avoid dangerous hotspot, only draw laser if shape is big enough
xy.spotKiller(); // get current min size of drawing for laser
xy.spotKiller(newVal); // set min drawing size to prevent laser hotspot

xy.stroke(r, g, b); // set RGB laser stroke color
xy.stroke(PVector rgb); // set as PVector

xy.strokeFreq(); // get laser RGB channel frequencies
xy.strokeFreq(freqRGB); // set laser RGB freq as group
xy.strokeFreq(freqR, freqG, freqB); // set laser R, G, B separately
xy.strokeFreq(PVector freqRGB); // set as PVector

// some lasers ignore values below __ when setting colors, 
// use this to set lowest point when color mixing
xy.strokeMin(); // get curret min RGB values for laser stroke
xy.strokeMin(minR, minG, minB); // set new min values (0.0 - 255.0)
xy.strokeMin(PVector minPV); // set new min as PVector

xy.strokeWB(wbR, wbG, wbB); // set values needed for white light
xy.strokeWB(PVector wbRGB); // set values as PVector

xy.strokeDash(); // get current dash setting
xy.strokeDash(sdRGB); // set laser dash count for RGB strokes
xy.strokeDash(sdR, sdG, sdB); // set dashes per R, G, B
xy.strokeDash(PVector sdRGB); // set as PVector
```
---
### Rendering
```
xy.drawAll(); // all views below 
xy.drawPath(); // shapes being drawn 
xy.drawPoints(); // points along paths of drawing 
xy.drawWaveform(); // drawing as oscillator's waveform 
xy.drawWave(); // waveform over time at frequency 
xy.drawXY(); // simulated xy-mode oscilloscope 

xy.debugView(); // check if debugView active
xy.debugView(boolean); // compare drawXY() to drawWaveform()
```
### Vars
Lastly, many variables within XYscope are public for your vector hacking needs.

```
shapes; // XYShapeList, processed by buildWaves()
minim, minimZ; // instances of Minim
recorder; // instance for audio recording
outXY, outZ; // AudioOutput (used to patch for additive-synth)
sumXY, sumZ; // Summer for patching filters
waveX, waveY, waveZ; // Oscil for each XYZ oscillators
tableX, tableY, tableZ; // XYWavetable, applied to oscillator
shapeX, shapeY, shapeZ; // float[] used for wavetables
fonts; // list of Hershey fonts

minimR, minimBG; // instances of Minim
waveR, waveG, waveB; // Oscil for each RGB oscillators
tableR, tableG, tableB; // XYWavetable, applied to oscillator
shapeR, shapeG, shapeB; // float[] used for wavetables

```

## Extras 
### Contributing
Found a bug, missing feature, and/or created a project with XYscope? Let me know!  
Create an [issue on GitHub](https://github.com/ffd8/xyscope/issues).

### License

This project is licensed under the LGPL License - see [LICENSE.md](https://github.com/ffd8/xyscope/blob/master/license.txt) for details.

### Shoutouts

* [Just Van Rossum](http://dailydrawbot.tumblr.com), the enlightening conversation on my X-Y attempts.
* [Stefanie Bräuer](https://stefaniebraeuer.ch/), feeding the obsession with crucial theory + context.
* [Hansi Raber](https://asdfg.me), java meta insights + finding external WaveTable bug!
* [Processing Library Template](https://github.com/processing/processing-library-template)