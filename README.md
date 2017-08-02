# XYscope

XYScope is a library for Processing to render graphics on a vector display (oscilloscope, laser) by converting them to audio.

This includes most primitive shapes (point, line, rect, ellipse, vertex, ...) by converting those points to waveforms (oscillators with custom wavetables) and generating audio in real time using the Minim library. Vector graphics shine on a vector display and now you can view your generative works like never before! Only tested on MacOS 10.9.5+, but should work on Windows.

## Getting Started

Install as suggested below or 

*PENDING* add within Processing from Sketch -> Import Library... -> Add Library...

More details + reference at [teddavis.org/xyscope](http://www.teddavis.org/xyscope)

### Dependencies

This library relies on and is greatly thankful for [Minim](https://github.com/ddf/Minim)

xtra_ examples requires [Geomerative](https://github.com/rikrd/geomerative), [OpenCV for Processing](https://github.com/atduskgreg/opencv-processing), [openkinect](https://github.com/shiffman/OpenKinect-for-Processing), [video](https://github.com/processing/processing-video)

### Installing

Install the [latest release](https://github.com/ffd8/xyscope/releases) and extract the XYscope folder into your ~/Documents/Processing/libraries/ directory (Windows installation varies). Reference and examples are included in the XYscope folder.

## Authors

* [Ted Davis](http://teddavis.org)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Thanks to [Just Van Rossum](http://dailydrawbot.tumblr.com) for the enlightening conversation on my X-Y attempts.
* Thanks to [Stefanie Bräuer](https://mewi.unibas.ch/seminar/personen/profil/portrait/person/stefaniebraeuer/) for feeding the oscilloscope obsession

