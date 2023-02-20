# XYscope

XYScope is a library for Processing to render graphics on a vector display (oscilloscope, laser) by converting them to audio.

This includes most primitive shapes (point, line, rect, ellipse, vertex, ...) by converting those points to waveforms (oscillators with custom wavetables) and generating audio in real time using the Minim library. Vector graphics shine on a vector display and now you can view your generative works like never before! Tested on MacOS 10.9+, Windows, Linux (RPi!).

## Getting Started

### Installing

Simply add within Processing's Contribution Manager:  
Sketch -> Import Library... -> Add Library...

OR manually install the [latest release](https://github.com/ffd8/xyscope/releases) and extract the XYscope folder into your ~/Documents/Processing/libraries/ directory (Windows installation varies). Reference and examples are included in the XYscope folder.

More details + reference at [teddavis.org/xyscope](http://www.teddavis.org/xyscope)

### Dependencies

This library relies on and is infinitely thankful for [Minim](https://github.com/ddf/Minim)

xtra_ examples requires:  
[Geomerative](https://github.com/rikrd/geomerative), [OpenCV for Processing](https://github.com/atduskgreg/opencv-processing), [openkinect](https://github.com/shiffman/OpenKinect-for-Processing), [video](https://github.com/processing/processing-video), [syphon](https://github.com/Syphon/Processing)





## Authors

* [Ted Davis](http://teddavis.org)

## License

This project is licensed under the LGPL License - see [LICENSE.md](LICENSE.md) for details.

## Acknowledgments

* [Just Van Rossum](http://dailydrawbot.tumblr.com), the enlightening conversation on my X-Y attempts.
* [Stefanie Bräuer](https://stefaniebraeuer.ch/), feeding the obsession with crucial theory + context.
* [Hansi Raber](https://asdfg.me), java meta insights + finding external WaveTable bug!
* [Processing Library Template](https://github.com/processing/processing-library-template)