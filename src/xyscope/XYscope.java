/*
 * cc ted davis 2017-18
 */

package xyscope;

import processing.core.*;
import static processing.core.PApplet.*;

import java.util.ArrayList;

// minim
import ddf.minim.*;
import ddf.minim.ugens.*;
import javax.sound.sampled.*;

/**
 * Render vector graphics on a vector display (oscilloscope
 * X-Y mode, laser) by converting them to audio .
 *
 */

public class XYscope {

	// myParent is a reference to the parent sketch
	PApplet myParent;

	/**
	 * Collection of current shapes rendered by buildWaves().
	 */

	public XYShapeList shapes = new XYShapeList();

	// minim
	Minim minim, minimZ;

	/**
	 * minim AudioOutput, for customizing audio out.
	 */

	public AudioOutput outXY, outZ;

	/**
	 * minim Summer, for customizing patching filters.
	 */
	public Summer sumXY, sumZ;

	/**
	 * minim Oscil, for customizing XYZ oscillators.
	 */
	public Oscil waveX, waveY, waveZ;

	XYWavetable tableX, tableY, tableZ;
	Pan panX = new Pan(-1);
	Pan panY = new Pan(1);

	Mixer.Info[] mixerInfo;

	float initAmp = 1.0f;
	PVector amp = new PVector(initAmp, initAmp, initAmp);
	float initFreq = 50f; // 43.065
	PVector freq = new PVector(initFreq, initFreq, initFreq);

	// Mixing audio channels
	AudioOutput mixXY;
	boolean useMix = false;

	int waveSizeVal = 512;
	int waveSizeValOG = waveSizeVal;
	int maxPoints = waveSizeValOG;
	public float[] shapeY = new float[waveSizeVal];
	public float[] shapeX = new float[waveSizeVal];
	public float[] shapeZ = new float[waveSizeVal];
	float[] shapePreY = new float[waveSizeVal];
	float[] shapePreX = new float[waveSizeVal];
	float[] shapePreZ = new float[waveSizeVal];
	boolean debugWave = false;
	int debugSize = 10;
	boolean busy = false;

	boolean useLimitPoints = false;
	int limitPointsVal = waveSizeValOG;

	int ellipseDetail = 30;

	boolean useEase = false;
	float easeVal = .1f;
	boolean useZ = false;
	boolean useSmooth = false;
	int smoothVal = 12;
	boolean useLimitPath = false;
	float limitVal = 1;

	float zaxisMax = 1f;
	float zaxisMin = -1f;
	int zoffset = 1;

	boolean useVectrex = false;
	float vectrexAmp = .82f;
	float vectrexAmpInit = .6f;
	int vectrexRotation = 0;
	int vectrexWidth = 310;
	int vectrexHeight = 410;

	// LASER VARS
	boolean useLaser = false;
	Minim minimR, minimBG;

	/**
	 * minim Oscil, for customizing Laser RGB oscillators.
	 */
	public Oscil waveR, waveG, waveB; 

	XYWavetable tableR, tableG, tableB;
	Pan panR = new Pan(1);
	Pan panG = new Pan(-1);
	Pan panB = new Pan(1);
	AudioOutput outR, outBG;

	public float[] shapeR = new float[waveSizeVal];
	public float[] shapeG = new float[waveSizeVal];
	public float[] shapeB = new float[waveSizeVal];
	private XYShape RGBshape = new XYShape();
	PVector lsFreq = new PVector(initFreq, initFreq, initFreq);
	PVector lsWB = new PVector(250, 220, 90);
	PVector lsMin = new PVector(0, 0, 0);
	PVector lsDash = new PVector(1, 1, 1);
	MoogFilter moog;
	float laserLPFVal = 10000.0f;
	float laserCutoffVal = 20f;

	int xyWidth, xyHeight;

	/**
	 * Initialize library in setup(), use default system audio out setting.
	 * 
	 * @param theParent
	 *            PApplet to apply to, typically 'this'
	 */
	// * @example basic_shapes
	public XYscope(PApplet theParent) {
		myParent = theParent;
		welcome();
		initMinim();
		setMixer();
	}

	/**
	 * Initialize instance of XYscope and patch to an already existing signal.
	 * 
	 * @param theParent
	 *            PApplet to apply to, typically 'this'
	 * @param outMix
	 *            AudioOutput to merge instance and of XYscope to
	 */
	public XYscope(PApplet theParent, AudioOutput outMix) {
		myParent = theParent;
		welcome();
		initMinim();
		setWaveTable(outMix);
		useMix = true;
	}

	/**
	 * Initialize library in setup(), custom soundcard by String for XY.
	 * 
	 * @param theParent
	 *            PApplet to apply to, typically 'this'
	 * @param xyMixer
	 *            Name of custom sound mixer to use for XY.
	 */
	public XYscope(PApplet theParent, String xyMixer) {
		myParent = theParent;
		welcome();
		getMixerInfo();
		initMinim();
		setMixer(xyMixer);
	}

	/**
	 * Initialize library in setup(), using default soundcard and set custom
	 * sample rate (44100, 192000).
	 * 
	 * @param theParent
	 *            PApplet to apply to, typically 'this'
	 * @param sampleR
	 *            Sample rate for soundcard (44100, 48000, 96000, 192000).
	 */
	public XYscope(PApplet theParent, int sampleR) {
		myParent = theParent;
		welcome();
		getMixerInfo();
		initMinim();
		setMixer(sampleR);
	}

	/**
	 * Initialize library in setup(), custom soundcard by string for XY and set
	 * custom sample rate (44100, 192000).
	 * 
	 * @param theParent
	 *            PApplet to apply to, typically 'this'
	 * @param xyMixer
	 *            Name of custom sound mixer to use for XY.
	 * @param sampleR
	 *            Sample rate for soundcard (44100, 48000, 96000, 192000).
	 */
	public XYscope(PApplet theParent, String xyMixer, int sampleR) {
		myParent = theParent;
		welcome();
		getMixerInfo();
		initMinim();
		setMixer(xyMixer, sampleR);
	}

	private void welcome() {
		System.out.println("XYscope 2.2.0 by Ted Davis http://teddavis.org");
		xyWidth = myParent.width;
		xyHeight = myParent.height;
	}

	/**
	 * Lists all audio input/output options available
	 */
	public void getMixerInfo() {
		mixerInfo = AudioSystem.getMixerInfo();
		for (int i = 0; i < mixerInfo.length; i++) {
			println(i + " = " + mixerInfo[i].getName());
		}
	}

	private static Mixer getMixerByName(String toFind) {
		for (Mixer.Info info : AudioSystem.getMixerInfo()) {
			if (toFind.equals(info.getName())) {
				return AudioSystem.getMixer(info);
			}
		}
		return null;
	}

	private void initMinim() {
		minim = new Minim(myParent);
		minimZ = new Minim(myParent);
		minimR = new Minim(myParent);
		minimBG = new Minim(myParent);
	}

	private void setMixer() {
		outXY = minim.getLineOut(Minim.STEREO, waveSizeValOG);
		setWaveTable();
	}

	private void setMixer(int sampleR) {
		outXY = minim.getLineOut(Minim.STEREO, waveSizeValOG, sampleR);
		setWaveTable();
	}

	private void setMixer(String xyMixer) {
		getMixerInfo();
		Mixer mixer = getMixerByName(xyMixer);
		minim.setOutputMixer(mixer);
		outXY = minim.getLineOut(Minim.STEREO, waveSizeValOG);
		setWaveTable();
	}

	private void setMixer(String xyMixer, int sampleR) {
		getMixerInfo();
		Mixer mixer = getMixerByName(xyMixer);
		minim.setOutputMixer(mixer);
		outXY = minim.getLineOut(Minim.STEREO, waveSizeValOG, sampleR);
		setWaveTable();
	}

	private void setWaveTable() {
		sumXY = new Summer();
		sumXY.setChannelCount(2);

		tableX = new XYWavetable(2);
		waveX = new Oscil(freq.x, amp.x, tableX);
		tableX.setWaveform(shapeX);
		waveX.patch(panX).patch(sumXY);

		tableY = new XYWavetable(2);
		waveY = new Oscil(freq.y, amp.y, tableY);
		tableY.setWaveform(shapeY);
		waveY.patch(panY).patch(sumXY);

		waveReset();
		sumXY.patch(outXY);

	}

	private void setWaveTable(AudioOutput outMix) {
		tableX = new XYWavetable(2);
		waveX = new Oscil(freq.x, amp.x, tableX);
		tableX.setWaveform(shapeX);
		waveX.patch(panX).patch(outMix);

		tableY = new XYWavetable(2);
		waveY = new Oscil(freq.y, amp.y, tableY);
		tableY.setWaveform(shapeY);
		waveY.patch(panY).patch(outMix);

		mixXY = outMix;

		waveReset();
	}

	private void setWaveTableZ() {
		if (useZ) {
			tableZ = new XYWavetable(2);
			waveZ = new Oscil(freq.z, amp.z, tableZ);
			tableZ.setWaveform(shapeZ);
			waveZ.patch(outZ); // need pan?? or gets full amp to both channels?
			waveReset();
			sumXY.unpatch(outXY);
			sumXY.patch(outXY);

		}
	}

	/**
	 * Reset time-step used by XYZ oscillators if they slip when changing
	 * frequencies.
	 * 
	 */
	public void waveReset() {
		waveX.reset();
		waveY.reset();
		if (useZ)
			waveZ.reset();
	}

	/**
	 * Patch z-axis to custom soundcard by String. Note: Auto z-axis has been
	 * disabled until solved, until then, one can manually buildZ()
	 * 
	 * @param zMixer
	 *            Name of custom sound mixer to use for Z.
	 */
	public void z(String zMixer) {
		Mixer mixerZ = getMixerByName(zMixer);
		minimZ.setOutputMixer(mixerZ);
		outZ = minimZ.getLineOut(Minim.STEREO, waveSizeValOG);
		useZ = true;
		setWaveTableZ();
	}

	/**
	 * Patch z-axis to custom soundcard by String and set custom sample rate
	 * (44100, 192000). Note: Auto z-axis has been disabled until solved, until
	 * then, one can manually buildZ()
	 * 
	 * @param zMixer
	 *            Name of custom sound mixer to use for Z.
	 * 
	 * @param sampleR
	 *            Sample rate for soundcard (44100, 48000, 192000).
	 * 
	 */
	public void z(String zMixer, int sampleR) {
		Mixer mixerZ = getMixerByName(zMixer);
		minimZ.setOutputMixer(mixerZ);
		outZ = minimZ.getLineOut(Minim.STEREO, waveSizeValOG, sampleR);
		useZ = true;
		setWaveTableZ();
	}

	/**
	 * Check if z-axis waveform is being automatically drawn from added shapes.
	 * 
	 * @return boolean
	 */
	public boolean zAuto() {
		return useZ;
	}

	/**
	 * Enabled by default for automatic generation of z-axis waveform based on
	 * added shapes. Disable if creating your own waveform (used for blanking,
	 * dotted line, etc. Note: Only works if using a second audio output channel
	 * 
	 * @param zAutoBool
	 *            true/false for generating z waveform
	 */
	public void zAuto(boolean zAutoBool) {
		useZ = zAutoBool;
	}

	/**
	 * Get ArrayList of all coordinates used for vector drawing as an ArrayList
	 * of PVector's.
	 * 
	 * @return ArrayList of PVector
	 */
	public ArrayList<PVector> wavePoints() {
		return shapes.getPoints();
	}

	/**
	 * Get min and max values for z-axis output as two value array.
	 * 
	 * @return array containing [zaxisMin, zaxisMax]
	 */
	public float[] zRange() {
		float[] zRangeFloat = { zaxisMin, zaxisMax };
		return zRangeFloat;
	}

	/**
	 * Set min and max values for z-axis output. Necessary for any inverted
	 * z-axis devices.
	 * <p>
	 * default is zMin: 1, zMax: -1
	 * 
	 * @param zMin
	 *            float between -1 to 1
	 * @param zMax
	 *            float between -1 to 1
	 */
	public void zRange(float zMin, float zMax) {
		zaxisMin = zMin;
		zaxisMax = zMax;
	}

	/**
	 * Returns current value for limiting drawing by number of points.
	 * 
	 * @return limitVal
	 */
	public float limitPoints(){
		return limitPointsVal;
	}

	/**
	 * Limit drawing by number of points
	 * 
	 * @param newLimitPointsVal int for limiting number of points
	 */
	public void limitPoints(int newLimitPointsVal){
		if(newLimitPointsVal == 0){
			useLimitPoints = false;
		}else{
			limitPointsVal = abs(newLimitPointsVal);
			useLimitPoints = true;
		}
	}


	/**
	 * Returns current value for border that limtis rendering to edges of screen.
	 * 
	 * @return limitVal
	 */
	public float limitPath(){
		return limitVal;
	}

	/**
	 * Only render points within specified border from the edge
	 * 
	 * @param newLimitVal float for border limit from edges
	 */
	public void limitPath(float newLimitVal){
		limitVal = newLimitVal;
		useLimitPath = true;
	}

	/**
	 * Use XYscope on a modded Vectrex monitor for XYZ input. This will automatically adjust the canvas and amplitude settings to match the ratio of the Vectrex.
	 * 
	 */
	public void vectrex(){
		vectrex(vectrexWidth, vectrexHeight, vectrexAmpInit, vectrexRotation);
	}

	/**
	 * Use XYscope on a modded Vectrex monitor for XYZ input. This will automatically adjust the canvas and amplitude settings to match the ratio of the Vectrex. You can customize the rotation of the monitor +/- 90° if landscape oriented.
	 * 
	 * @param vrot
	 *            int for degrees of rotation, 90 or -90
	 */
	public void vectrex(int vrot){
		if(vrot == 90){
			vectrexRotation = vrot;
			vectrex(vectrexHeight, vectrexWidth, vectrexAmpInit, vectrexRotation);
		}else if(vrot == -90){
			vectrexRotation = vrot;
			vectrex(vectrexHeight, vectrexWidth, vectrexAmpInit, vectrexRotation);
		}else {
			vectrexRotation = 0;
			vectrex(vectrexWidth, vectrexHeight, vectrexAmpInit, vectrexRotation);
		}
	}

	/**
	 * Use XYscope on a modded Vectrex monitor for XYZ input. Set custom width, height, initial amplitude scaling and rotation/orientation. 
	 * 
	 * @param vw
	 *            int for width of canvas, default is 330
	 * @param vh
	 *            int for height of canvas, default is 410
	 * @param vamp
	 *            float for initial amplitude adjustment of signal to screen (0.0 - 1.0), default is .6
	 * @param vrot
	 *            int for degrees of rotation, 90 or -90, default is 0
	 */
	public void vectrex(int vw, int vh, float vamp, int vrot){
		useVectrex = true;
		vectrexRotation = vrot;
		myParent.getSurface().setResizable(true);
		myParent.getSurface().setSize(vw, vh);
		xyWidth = vw;
		xyHeight = vh;
		vectrexAmpInit = vamp;
		amp(vectrexAmpInit);
	}


	/**
	 * Get current amplitude difference used for ratio of Vectrex.
	 * 
	 * @return float
	 */
	public float vectrexRatio(){
		return vectrexAmp;
	}


	/**
	 * Set current amplitude difference used for ratio of Vectrex.
	 * 
	 * @param vectrexAmpVal
	 *            float for amplitude difference (0.0 - 1.0), default is .82
	 */
	public void vectrexRatio(float vectrexAmpVal){
		vectrexAmp = constrain(vectrexAmpVal, 0f, 1f);
		amp(vectrexAmpInit);
	}

	/**
	 * Activate use of Laser's RGB by assigning 3 additional (2x stereo pairs) audio channels for controlling the RGB modulation.
	 * 
	 * @param inR
	 *            String for name of audio channel for Red
	 * @param inBG
	 *            String for name of audio channel for Blue/Green
	 */
	public void laser(String inR, String inBG){
		Mixer mixerR = getMixerByName(inR);
		Mixer mixerBG = getMixerByName(inBG);
		minimR.setOutputMixer(mixerR);
		minimBG.setOutputMixer(mixerBG);
		outR = minimR.getLineOut(Minim.STEREO, waveSizeValOG);
		outBG = minimBG.getLineOut(Minim.STEREO, waveSizeValOG);
		setWaveTableRGB();
		useLaser = true;

		//LPF
		moog = new MoogFilter( laserLPFVal, 0f );
		moog.setChannelCount(2);
		sumXY.unpatch(outXY);
		sumXY.patch(moog).patch(outXY);
	}

	private void setWaveTableRGB() {
		tableR = new XYWavetable(2);
		waveR = new Oscil(freq.x, amp.x, tableR);
		tableR.setWaveform(shapeR);
		waveR.patch(panR).patch(outR);

		tableG = new XYWavetable(2);
		waveG = new Oscil(freq.x, amp.x, tableG);
		tableG.setWaveform(shapeG);
		waveG.patch(panG).patch(outBG);

		tableB = new XYWavetable(2);
		waveB = new Oscil(freq.x, amp.x, tableB);
		tableB.setWaveform(shapeB);
		waveB.patch(panB).patch(outBG);
	}

	/**
	 * Returns current frequency of Laser's low-pass-filter (LPF).
	 * 
	 * @return float
	 */
	public float laserLPF(){
		return laserLPFVal;
	}

	/**
	 * Set new frequency for Laser's low-pass-filter (LPF) as float. Be careful to stay within range that's safe for your Galvos.
	 * 
	 * @param newLaserLPFVal
	 *            float between 0.1 - 20000.0
	 */
	public void laserLPF(float newLaserLPFVal){
		laserLPFVal = constrain(newLaserLPFVal, .1f, 20000f);
		moog.frequency.setLastValue(laserLPFVal);
	}

	/**
	 * Returns current value spot-killer (minimum size of drawing for laser).
	 * 
	 * @return float
	 */
	public float spotKiller(){
		return laserCutoffVal;
	}

	/**
	 * Set new value for spotKiller (won't draw if XY shape is smaller than provided value).
	 * 
	 * @param newLaserCutoffVal
	 *            float 
	 */
	public void spotKiller(float newLaserCutoffVal){
		laserCutoffVal = abs(newLaserCutoffVal);
	}

	/**
	 * Returns current values for setting minimum values from RGB mixture in laser.
	 * 
	 * @return PVector
	 */
	public PVector strokeMin(){
		return lsMin;
	}

	public void strokeMin(float minR, float minG, float minB){
		strokeMin(new PVector(minR, minG, minB));
	}

	public void strokeMin(PVector minPV){
		lsMin = new PVector(minPV.x, minPV.y, minPV.z);
	}
	
	/**
	 * Returns current values for setting white balance from RGB mixture in laser.
	 * 
	 * @return PVector
	 */
	public PVector strokeWB(){
		return lsWB;
	}

	public void strokeWB(float wbR, float wbG, float wbB){
		strokeWB(new PVector(wbR, wbG, wbB));
	}

	public void strokeWB(PVector wbPV){
		lsWB = new PVector(wbPV.x, wbPV.y, wbPV.z);
	}

	public PVector strokeDash(){
		return lsDash;
	}

	public void strokeDash(int newDash){
		strokeDash(new PVector(newDash, newDash, newDash));
	}

	public void strokeDash(int newDashR, int newDashG, int newDashB){
		strokeDash(new PVector(newDashR, newDashG, newDashB));
	}

	public void strokeDash(PVector newDash){
		lsDash = new PVector(newDash.x, newDash.y, newDash.z);
	}

	public void stroke(float r, float g, float b){
		stroke(new PVector(r, g, b));
	}

	public void stroke(PVector rgb){
		float mr = 0f;
		float mg = 0f;
		float mb = 0f;
		if(rgb.x > 0f)
			mr = map(rgb.x, 0f, 255f, (lsMin.x/255f), 1f);
		if(rgb.y > 0f)
			mg = map(rgb.y, 0f, 255f, (lsMin.y/255f), 1f);
		if(rgb.z > 0f)
			mb = map(rgb.z, 0f, 255f, (lsMin.z/255f), 1f);
		
		if(rgb.x == 255f && rgb.y == 255f && rgb.z == 255f){
			mr = lsWB.x / 255f;
			mg = lsWB.y / 255f;
			mb = lsWB.z / 255f;
		}
		RGBshape.add(new PVector(mr, mg, mb));
	}

	public PVector strokeFreq() {
		return lsFreq;
	}

	public void strokeFreq(float newFreq) {
		lsFreq = new PVector(newFreq, newFreq, newFreq);
		strokeFreq(lsFreq);
	}

	public void strokeFreq(float newFreqR, float newFreqG, float newFreqB) {
		lsFreq = new PVector(newFreqR, newFreqG, newFreqB);
		strokeFreq(lsFreq);
	}

	public void strokeFreq(PVector newFreq) {
		lsFreq = newFreq;
		waveR.setFrequency(lsFreq.x);
		waveG.setFrequency(lsFreq.y);
		waveB.setFrequency(lsFreq.z);
	}

	/**
	 * Get current amplitude setting of XY oscillators.
	 * 
	 * @return float
	 */
	public PVector amp() {
		return amp;
	}

	/**
	 * Set new amplitude for both XYZ oscillators as float.
	 * 
	 * @param newAmp
	 *            value between 0.0 - 1.0
	 */
	public void amp(float newAmp) {
		amp.x = constrain(newAmp, 0f, 1f);
		amp.y = constrain(newAmp, 0f, 1f);
		if(useVectrex){
			amp.x *= vectrexAmp;
		}

		waveX.setAmplitude(amp.x);
		waveY.setAmplitude(amp.y);
		if (useZ) {
			amp.z = constrain(newAmp, 0f, 1f);
			waveZ.setAmplitude(amp.z);
		}
	}

	/**
	 * Set new amplitude for both X + Y oscillators as float.
	 * 
	 * @param newAmpX
	 *            value between 0.0 - 1.0
	 * @param newAmpY
	 *            value between 0.0 - 1.0
	 */
	public void amp(float newAmpX, float newAmpY) {
		amp.x = constrain(newAmpX, 0f, 1f);
		if(useVectrex)
			amp.x *= vectrexAmp;
		amp.y = constrain(newAmpY, 0f, 1f);
		waveX.setAmplitude(amp.x);
		waveY.setAmplitude(amp.y);
	}

	/**
	 * Set new amplitude for both X + Y + Z oscillators as float.
	 * 
	 * @param newAmpX
	 *            value between 0.0 - 1.0
	 * @param newAmpY
	 *            value between 0.0 - 1.0
	 * @param newAmpZ
	 *            value between 0.0 - 1.0
	 */
	public void amp(float newAmpX, float newAmpY, float newAmpZ) {
		amp.x = constrain(newAmpX, 0f, 1f);
		if(useVectrex)
			amp.x *= vectrexAmp;
		amp.y = constrain(newAmpY, 0f, 1f);
		waveX.setAmplitude(amp.x);
		waveY.setAmplitude(amp.y);
		if (useZ) {
			amp.z = constrain(newAmpZ, 0f, 1f);
			waveZ.setAmplitude(amp.z);
		}
	}

	/**
	 * Set new amplitude for each XYZ oscillator separately using a PVector for
	 * the values.
	 * 
	 * @param newAmp
	 *            PVector of values between 0.0 - 1.0
	 */
	public void amp(PVector newAmp) {
		float tempX = constrain(newAmp.x, 0f, 1f);
		if(useVectrex)
			tempX *= vectrexAmp;
		float tempY = constrain(newAmp.y, 0f, 1f);
		waveX.setAmplitude(tempX);
		waveY.setAmplitude(tempY);
		if (useZ) {
			float tempZ = constrain(newAmp.z, 0f, 1f);
			waveZ.setAmplitude(tempZ);
		}
	}

	/**
	 * Get current frequency for X, Y, Z oscillators as a PVector.
	 * 
	 * @return PVector
	 */
	public PVector freq() {
		return freq;
	}

	/**
	 * Set new frequency for all XYZ oscillators together as single float.
	 * 
	 * @param newFreq
	 *            float
	 */
	public void freq(float newFreq) {
		freq = new PVector(newFreq, newFreq, newFreq);
		waveX.setFrequency(freq.x);
		waveY.setFrequency(freq.y);
		if (useZ)
			waveZ.setFrequency(freq.z);
	}

	/**
	 * Set new frequency for all X + Y oscillators.
	 * 
	 * @param newFreqX
	 *            float
	 * @param newFreqY
	 *            float
	 */
	public void freq(float newFreqX, float newFreqY) {
		freq.x = newFreqX;
		freq.y = newFreqY;
		waveX.setFrequency(freq.x);
		waveY.setFrequency(freq.y);
	}

	/**
	 * Set new frequency for all X + Y oscillators.
	 * 
	 * @param newFreqX
	 *            float
	 * @param newFreqY
	 *            float
	 * @param newFreqZ
	 *            float
	 */
	public void freq(float newFreqX, float newFreqY, float newFreqZ) {
		freq.x = newFreqX;
		freq.y = newFreqY;
		waveX.setFrequency(freq.x);
		waveY.setFrequency(freq.y);
		if (useZ) {
			freq.z = newFreqZ;
			waveZ.setFrequency(freq.z);
		}
	}

	/**
	 * Set new frequency for each XYZ oscillator separately using a PVector for
	 * the values.
	 * 
	 * @param newFreq
	 *            PVector
	 */
	public void freq(PVector newFreq) {
		freq = newFreq;
		waveX.setFrequency(freq.x);
		waveY.setFrequency(freq.y);
		if (useZ)
			waveZ.setFrequency(freq.z);
	}

	/**
	 * Enable/Disable easing transitions from one set of buildWaves() to the
	 * next. Default is false.
	 * 
	 * @param easeBool
	 *            true/false
	 */
	public void ease(boolean easeBool) {
		useEase = easeBool;
	}

	/**
	 * Check if easing between each frame of buildWaves() is enabled.
	 * 
	 * @return boolean
	 */
	public boolean ease() {
		return useEase;
	}

	/**
	 * Returns current easeAmount, 0.0 - 1.0.
	 * 
	 * @return float
	 */
	public float easeAmount() {
		return easeVal;
	}

	/**
	 * Set new easing value for speed between buildWave() transitions.
	 * 
	 * @param newEaseValue
	 *            float between 0.0 - 1.0
	 */
	public void easeAmount(float newEaseValue) {
		easeVal = newEaseValue;
	}

	/**
	 * Enable/Disable debug view for comparing waveform to shape.
	 * 
	 * @param debugBool
	 *            true/false
	 */
	public void debugView(boolean debugBool) {
		debugWave = debugBool;
	}

	/**
	 * Check if debugView is active.
	 * 
	 * @return boolean
	 */
	public boolean debugView() {
		return debugWave;
	}

	/**
	 * Get size of wavetables. By default, it's the same as the
	 * outXY.bufferSize()
	 * 
	 * @return newSize
	 *            int
	 */
	public int waveSize() {
		return waveSizeVal;
	}

	/**
	 * Set custom size for wavetables. By default, it's the same as the
	 * outXY.bufferSize()
	 * 
	 * @param newSize
	 *            int
	 */
	public void waveSize(int newSize) {
		waveSizeVal = newSize;
		shapeY = new float[waveSizeVal];
		shapeX = new float[waveSizeVal];
		// shapeZ = new float[waveSizeVal];
		shapePreY = new float[waveSizeVal];
		shapePreX = new float[waveSizeVal];
		// shapePreZ = new float[waveSizeVal];
		tableX.setWaveform(shapeX);
		tableY.setWaveform(shapeY);
		// if (useZ)
		// tableZ.setWaveform(shapeZ);
	}

	/**
	 * Clears the waveforms from previous buildWaves(). Useful to call at top of
	 * draw(), similar to using background() to clear the slate before building
	 * the waveforms at the bottom of your draw with buildWaves().
	 */
	public void clearWaves() {
		if(!busy){
			for (int i = 0; i < shapeX.length; i++) {
				shapePreX[i] = 0;
				shapePreY[i] = 0;
			}

			if (useZ) {
				for (int i = 0; i < shapeZ.length; i++) {
					shapePreZ[i] = zaxisMin;
				}
			}

			shapes = new XYShapeList();
			currentShape = null;

			if(useLaser)
				RGBshape = new XYShape();
		}
	}

	/**
	 * Generate the XY oscillator waveforms from all added shapes for sending
	 * audio to vector display. Call this after drawing any primitive shapes.
	 * New Rendering mode in place, if old dots style is preferred, use
	 * buildWaves(-1).
	 * 
	 * @param bwm int for buildWaves mode
	 */
	public void buildWaves(int bwm) {
		if(bwm == 0){ // waveform gen v3 sep 2018
			if (shapes.size() > 0) {
				XYWavetable mx = new XYWavetable(2);
				XYWavetable my = new XYWavetable(2);
				XYWavetable mz = new XYWavetable(2);
				float[] mfx = new float[0];
				float[] mfy = new float[0];
				float[] mfz = new float[0];

				for (XYShape shape : shapes) {
					XYWavetable tx = new XYWavetable(2);
					XYWavetable ty = new XYWavetable(2);
					XYWavetable tz = new XYWavetable(2);
					float[] tfx = new float[shape.size()];
					float[] tfy = new float[shape.size()];
					float[] tfz = new float[shape.size()];

					for (int i = 0; i < shape.size(); i++) {
						if(i < tfx.length){
							PVector tc = shape.get(i);
							tfx[i] = map(tc.x, 0f, 1f, -1f, 1f);
							tfy[i] = map(tc.y, 0f, 1f, 1f, -1f);
							tfz[i] = zaxisMax;

							if(tc.z == 1f)
								tfz[i] = zaxisMin;

							float tfxx = tfx[i];
							float tfyy = tfy[i];

							if(useVectrex){
								if(vectrexRotation == 90){
									tfx[i] = tfyy;
									tfy[i] = tfxx*-1;
								}else if(vectrexRotation == -90){
									tfx[i] = tfyy*-1;
									tfy[i] = tfxx;
								}else if(vectrexRotation == 0){
									tfx[i] = tfxx*-1;
									tfy[i] = tfyy*-1;
								}
							}
						}
					}

					tx.setWaveform(tfx);
					ty.setWaveform(tfy);
					tz.setWaveform(tfz);
					mfx = concat(mfx, tx.getWaveform());
					mfy = concat(mfy, ty.getWaveform());
					mfz = concat(mfz, tz.getWaveform());

				}

				// limit points
				if(useLimitPoints && (mfx.length > limitPointsVal || mfy.length > limitPointsVal)){
					float[] lx = new float[limitPointsVal];
					float[] ly = new float[limitPointsVal];
					float[] lz = new float[limitPointsVal];
					for(int i=0; i < limitPointsVal; i++){
						int mfxSel = floor(map(i, 0, limitPointsVal, 0, mfx.length));
						int mfySel = floor(map(i, 0, limitPointsVal, 0, mfy.length));
						int mfzSel = floor(map(i, 0, limitPointsVal, 0, mfz.length));
						lx[i] = mfx[mfxSel];
						ly[i] = mfy[mfySel];
						lz[i] = mfz[mfzSel];
					}
					tableX.setWaveform(lx);
					tableY.setWaveform(ly);
					if(useZ)
						tableZ.setWaveform(lz);
				}else{
					tableX.setWaveform(mfx);
					tableY.setWaveform(mfy);
					if(useZ)
						tableZ.setWaveform(mfz);
				}


				if(useLaser){
					// spotkiller checkSize
					boolean checkSize = false;
					AudioOutput tempXY = outXY;

					for (int i = 0; i < tempXY.bufferSize() - 1; i++) {
						float lAudio = abs(tempXY.left.get(i) * (float) xyWidth / 2);
						float rAudio = abs(tempXY.right.get(i) * (float) xyHeight / 2);
						if(lAudio > laserCutoffVal || rAudio > laserCutoffVal)
							checkSize = true;
					}
					if(checkSize){
						if(RGBshape.size() > 0){
							buildColorWave(tableR, "x", floor(lsDash.x));
							buildColorWave(tableG, "y", floor(lsDash.y));
							buildColorWave(tableB, "z", floor(lsDash.z));
						}else{
							float[] tfr = {lsWB.x/255f};
							float[] tfg = {lsWB.y/255f};
							float[] tfb = {lsWB.z/255f};
							tableR.setWaveform(tfr);
							tableG.setWaveform(tfg);
							tableB.setWaveform(tfb);
						}
					}else{
						tableR.setWaveform(new float[0]);
						tableG.setWaveform(new float[0]);
						tableB.setWaveform(new float[0]);
					}

				}
			}else{
				tableX.setWaveform(new float[0]);
				tableY.setWaveform(new float[0]);
				if(useZ)
					tableZ.setWaveform(new float[0]);

				if(useLaser){
					tableR.setWaveform(new float[0]);
					tableG.setWaveform(new float[0]);
					tableB.setWaveform(new float[0]);
				}
			}
		} else if (bwm == -2) { // waveform gen v2 may 2018
			if (shapes.size() > 0) {
				if (shapes.totalSize() < waveSizeValOG) {
					if (waveSize() != shapes.totalSize()) {
						waveSize(shapes.totalSize());
					}

					int SID = 0;
					if (waveSize() == shapes.totalSize()) {
						for (XYShape shape : shapes) {
							for (int i = 0; i < shape.size(); i++) {
								shapePreX[SID] = map(shape.get(i).x, 0f, 1f, -1f, 1f);
								shapePreY[SID] = map(shape.get(i).y, 0f, 1f, 1f, -1f);
								SID++;
							}
						}
					}
				} else {
					if (waveSize() != waveSizeValOG)
						waveSize(waveSizeValOG);

					ArrayList<PVector> ts = shapes.getPoints();
					for (int i = 0; i < shapeX.length; i++) {
						int ptsSel = (int) Math.floor(map(i, 0f, shapeX.length, 0, ts.size()));
						shapePreX[i] = map(ts.get(ptsSel).x, 0f, 1f, -1f, 1f);
						shapePreY[i] = map(ts.get(ptsSel).y, 0f, 1f, 1f, -1f);
					}
				}
			} else {
				waveSize(1);
				shapePreX[0] = 0f;
				shapePreY[0] = 0f;
			}
		} else if (bwm == -1) { // waveform gen v1 jul 2017
			if (waveSize() != waveSizeValOG)
				waveSize(waveSizeValOG);
			if (shapes.size() > 0) {
				ArrayList<PVector> ts = shapes.getPoints();
				for (int i = 0; i < shapeX.length; i++) {
					int ptsSel = (int) Math.floor(map(i, 0f, shapeX.length, 0, ts.size()));
					shapePreX[i] = map(ts.get(ptsSel).x, 0f, 1f, -1f, 1f);
					shapePreY[i] = map(ts.get(ptsSel).y, 0f, 1f, 1f, -1f);
				}
			}
		}

		if(bwm == -1 || bwm == -2){
			// easing
			if (useEase) {
				easeWaves();
			} else {
				for (int i = 0; i < shapePreX.length; i++) {
					shapeX[i] = shapePreX[i];
					shapeY[i] = shapePreY[i];

					if(useVectrex){
						if(vectrexRotation == 90){
							shapeX[i] = shapePreY[i];
							shapeY[i] = shapePreX[i]*-1;
						}else if(vectrexRotation == -90){
							shapeX[i] = shapePreY[i]*-1;
							shapeY[i] = shapePreX[i];
						}else if(vectrexRotation == 0){
							shapeX[i] = shapePreX[i]*-1;
							shapeY[i] = shapePreY[i]*-1;
						}
					}

					if (useZ)
						shapeZ[i] = shapePreZ[i];
				}

				if (useZ) {
					for (int i = 0; i < shapePreZ.length; i++) {
						shapeZ[i] = shapePreZ[i];
					}
				}
			}

			// smooth
			if (useSmooth) {
				tableX.smooth(smoothVal);
				tableY.smooth(smoothVal);
			}
		}
	}

	private void buildColorWave(XYWavetable tableTemp, String RGBval, int dashTemp){
		XYShape shapeTemp = new XYShape();						
		for (int i = 0; i < RGBshape.size()*dashTemp; i++) {
			int RGBshapeIndex = floor(map(i, 0, RGBshape.size()*dashTemp, 0, RGBshape.size()));
			PVector tc = RGBshape.get(RGBshapeIndex);
			if(i%2==0 && dashTemp > 1) {
				tc = new PVector(-1, -1, -1);
			}
			shapeTemp.add(tc);
		}
		println(shapeTemp.size());
		float[] tfr = new float[shapeTemp.size()];
		for (int i = 0; i < shapeTemp.size(); i++) {
			PVector tc = shapeTemp.get(i);
			if(RGBval == "x") {
				tfr[i] = tc.x;
			}else if(RGBval == "y") {
				tfr[i] = tc.y;
			}else if(RGBval == "z") {
				tfr[i] = tc.z;
			}
			
		}
		tableTemp.setWaveform(tfr);
	}

	/**
	 * Generate the XY(Z) oscillator waveforms from all added points/shapes for
	 * sending audio to vector display. Call this after drawing any primitive
	 * shapes.
	 */
	public void buildWaves() {
		buildWaves(0);
	}

	/**
	 * Build custom X oscillator waveform. Use waveSize() to ensure you send the
	 * right number of values.
	 * 
	 * @param newWave
	 *            Array of normalized floats between 0.0 - 1.0
	 *            
	 * @see #waveSize()
	 * 
	 */
	public void buildX(float[] newWave) {
		for (int i = 0; i < newWave.length; i++) {
			int sel = (int) Math.floor(map(i, 0f, newWave.length, 0f, shapePreX.length));
			shapePreX[i] = map(newWave[sel], 0f, 1f, -1f, 1f);
		}

		if (useEase) {
			easeWaves(shapePreX, shapeX);
		} else {
			for (int i = 0; i < shapePreX.length; i++) {
				shapeX[i] = shapePreX[i];
			}
		}
	}

	/**
	 * Build custom Y oscillator waveform. Use waveSize() to ensure you send the
	 * right number of values.
	 * 
	 * @param newWave
	 *            Array of normalized floats between 0.0 - 1.0
	 * @see #waveSize()
	 */
	public void buildY(float[] newWave) {
		for (int i = 0; i < newWave.length; i++) {
			int sel = (int) Math.floor(map(i, 0f, newWave.length, 0f, shapePreY.length));
			shapePreY[i] = map(newWave[sel], 0f, 1f, 1f, -1f);
		}

		if (useEase) {
			easeWaves(shapePreY, shapeY);
		} else {
			for (int i = 0; i < shapePreY.length; i++) {
				shapeY[i] = shapePreY[i];
			}
		}
	}

	/**
	 * Build custom Z oscillator waveform. Use waveSize() to ensure you send the
	 * right number of values.
	 * 
	 * @param newWave
	 *            Array of normalized floats between 0.0 - 1.0
	 * @see #waveSize()
	 */
	public void buildZ(float[] newWave) {
		for (int i = 0; i < newWave.length; i++) {
			int sel = (int) Math.floor(map(i, 0f, newWave.length, 0f, shapePreZ.length));
			shapePreZ[i] = map(newWave[sel], 0f, 1f, zaxisMin, zaxisMax);
		}

		if (useEase) {
			easeWaves(shapePreZ, shapeZ);
		} else {
			for (int i = 0; i < shapePreZ.length; i++) {
				shapeZ[i] = shapePreZ[i];
			}
		}
	}

	private void easeWaves(float[] sShape, float[] tShape) {
		for (int i = 0; i < sShape.length; i++) {
			float targetX = sShape[i];
			float dx = targetX - tShape[i];
			tShape[i] += dx * easeVal;
		}
	}

	private void easeWaves() {
		easeWaves(shapePreX, shapeX);
		easeWaves(shapePreY, shapeY);

		if (useZ) {
			easeWaves(shapePreZ, shapeZ);

		}
	}

	/**
	 * Check if waveform smoothing is enabled/disabled.
	 * 
	 * @return boolean
	 */
	public boolean smoothWaves() {
		return useSmooth;
	}

	/**
	 * Enable/disable Smooth waveforms to reduce visibility of points in
	 * drawing. Default is false (new rendering doesn't need it).
	 * 
	 * @param smoothWavesBool
	 *            true/false
	 */
	public void smoothWaves(boolean smoothWavesBool) {
		useSmooth = smoothWavesBool;
	}

	/**
	 * Get number of steps for smoothing waveforms.
	 * 
	 * @return int
	 * 
	 * @see <a href=
	 *      "http://code.compartmental.net/minim/wavetable_method_smooth.html">Minim
	 *      » Wavetable » smooth()</a>
	 */
	public int smoothWavesAmount() {
		return smoothVal;
	}

	/**
	 * Set number of steps for smoothing waveforms. Default is 12.
	 * 
	 * @param swAmount
	 *            new int value for smoothening waveform
	 * @see <a href=
	 *      "http://code.compartmental.net/minim/wavetable_method_smooth.html">Minim
	 *      » Wavetable » smooth()</a>
	 */
	public void smoothWavesAmount(int swAmount) {
		smoothVal = swAmount;
	}

	/**
	 * Draw all information
	 * <ul>
	 * <li>drawPath()
	 * <li>drawWaveform()
	 * <li>drawWave()
	 * <li>drawXY()
	 * <li>drawPoints()
	 * </ul>
	 */
	public void drawAll() {
		drawPath();
		drawWaveform();
		drawWave();
		drawXY();
		drawPoints();
	}

	/**
	 * Draw path of points remapped from normalized values to width + height of
	 * sketch.
	 */
	public void drawPath() {
		myParent.pushStyle();
		myParent.noFill();
		myParent.stroke(255);
		myParent.pushMatrix();
		myParent.beginShape();
		for (XYShape shape : shapes) {
			for (int i = 0; i < shape.size(); i++) {
				float x = map(shape.get(i).x, 0f, 1f, 0f, xyWidth);
				float y = map(shape.get(i).y, 0f, 1f, 0f, xyHeight);
				myParent.vertex(x, y);
			}
		}
		myParent.endShape(OPEN);
		myParent.popMatrix();
		myParent.popStyle();
	}

	/**
	 * Draw points (as 3px ellipses) remapped from normalized values to width +
	 * height of sketch.
	 */
	public void drawPoints() {
		myParent.pushStyle();
		myParent.fill(0, 255, 0);
		myParent.noStroke();
		myParent.pushMatrix();
		for (XYShape shape : shapes) {
			for (int i = 0; i < shape.size(); i++) {
				float x = map(shape.get(i).x, 0f, 1f, 0f, xyWidth);
				float y = map(shape.get(i).y, 0f, 1f, 0f, xyHeight);
				myParent.ellipse(x, y, 3, 3);
			}
		}
		myParent.popMatrix();
		myParent.noFill();
		myParent.popStyle();
	}

	/**
	 * Simulate X-Y mode of oscilloscope output.
	 */
	public void drawXY() {
		myParent.pushStyle();
		myParent.noFill();
		myParent.stroke(50, 255, 50);
		myParent.pushMatrix();
		myParent.translate(xyWidth / 2, xyHeight / 2);
		myParent.beginShape();
		AudioOutput tempXY;
		if (useMix) {
			tempXY = mixXY;
		} else {
			tempXY = outXY;
		}

		for (int i = 0; i < tempXY.bufferSize() - 1; i++) {
			float lAudio = tempXY.left.get(i) * (float) xyWidth / 2;
			float rAudio = tempXY.right.get(i) * (float) xyHeight / 2;

			if(useVectrex){
				if(vectrexRotation == 90){
					rAudio = tempXY.left.get(i) * (float) xyHeight / 2;
					lAudio = tempXY.right.get(i) * (float) xyWidth / 2 * -1f * vectrexAmp;
				}else if(vectrexRotation == -90){
					rAudio = tempXY.left.get(i) * (float) xyHeight / 2 * -1f;
					lAudio = tempXY.right.get(i) * (float) xyWidth / 2 * vectrexAmp;
				}else if(vectrexRotation == 0){
					lAudio = tempXY.left.get(i) * (float) xyWidth / 2 * -1f;
					rAudio = tempXY.right.get(i) * (float) xyHeight / 2 * -1f * vectrexAmp;
				}
			}

			myParent.curveVertex(lAudio, rAudio * -1f);
		}

		myParent.endShape();

		if (debugWave) {
			float mouseT = (myParent.mouseX / (float) xyWidth);
			float mx = tableX.value(mouseT) * (float) xyWidth / 2 * amp.x;
			float my = -tableY.value(mouseT) * (float) xyHeight / 2 * amp.y;
			myParent.pushStyle();
			myParent.fill(50, 255, 50);
			myParent.noStroke();
			myParent.ellipse(mx, my, debugSize, debugSize);
			myParent.popStyle();
		}

		myParent.popMatrix();
		myParent.popStyle();
	}

	/**
	 * Draw waveform of all XYZ oscillators.
	 * <ul>
	 * <li>Red: X</li>
	 * <li>Blue: Y</li>
	 * <li>Green: Z</li>
	 * </ul>
	 */
	public void drawWaveform() {
		myParent.pushStyle();
		myParent.noFill();
		myParent.pushMatrix();
		myParent.beginShape();

		// X -> L
		myParent.stroke(50, 50, 255);
		for (int i = 0; i < xyWidth; i++) {
			myParent.vertex(i, (float) xyHeight * .25f
					- ((float) xyHeight * .125f) * tableX.value((float) i / (float) xyWidth));
		}
		myParent.endShape();

		// Y -> R
		myParent.stroke(255, 50, 50);
		myParent.beginShape();
		for (int i = 0; i < xyWidth; i++) {
			myParent.vertex(i, (float) xyHeight * .75f
					- ((float) xyHeight * 0.125f) * tableY.value((float) i / (float) xyWidth));
		}
		myParent.endShape();


		if (debugWave) {
			float mouseT = (myParent.mouseX / (float) xyWidth);
			float lx = myParent.mouseX;
			float ly = (float) xyHeight * .25f - ((float) xyHeight * .125f)
					* tableX.value((float) myParent.mouseX / (float) xyWidth);
			float ry = (float) xyHeight * .75f - ((float) xyHeight * .125f)
					* tableY.value((float) myParent.mouseX / (float) xyWidth);
			myParent.pushStyle();
			myParent.noStroke();
			myParent.fill(50, 50, 255);
			myParent.ellipse(lx - 2, ly, debugSize, debugSize);
			myParent.fill(255, 50, 50);
			myParent.ellipse(lx - 2, ry, debugSize, debugSize);
			myParent.popStyle();
		}

		// Z
		if (useZ) {
			myParent.stroke(50, 255, 50);
			myParent.beginShape();
			for (int i = 0; i < xyWidth; i++) {
				myParent.vertex(i, (float) xyHeight * .5f
						- ((float) xyHeight * 0.125f) * tableZ.value((float) i / (float) xyWidth));
			}

			myParent.endShape();
		}
		myParent.popMatrix();
		myParent.popStyle();
	}

	/**
	 * Draw waveform of all laser RGB oscillators.
	 * <ul>
	 * <li>Red: R</li>
	 * <li>Green: G</li>
	 * <li>Blue: B</li>
	 * </ul>
	 */
	public void drawRGB(){
		if(useLaser){
			myParent.pushStyle();
			myParent.noFill();
			myParent.pushMatrix();

			//R
			myParent.stroke(255, 50, 50);
			myParent.beginShape();
			for (int i = 0; i < xyWidth; i++) {
				myParent.vertex(i, (float) xyHeight * .25f
						- ((float) xyHeight * 0.125f) * tableR.value((float) i / (float) xyWidth));
			}
			myParent.endShape();

			//G
			myParent.stroke(50, 255, 50);
			myParent.beginShape();
			for (int i = 0; i < xyWidth; i++) {
				myParent.vertex(i, (float) xyHeight * .5f
						- ((float) xyHeight * 0.125f) * tableG.value((float) i / (float) xyWidth));
			}
			myParent.endShape();

			//B
			myParent.stroke(50, 50, 255);
			myParent.beginShape();
			for (int i = 0; i < xyWidth; i++) {
				myParent.vertex(i, (float) xyHeight * .75f
						- ((float) xyHeight * 0.125f) * tableB.value((float) i / (float) xyWidth));
			}
			myParent.endShape();

			myParent.popMatrix();
			myParent.popStyle();
		}
	}

	/**
	 * Draw wave of all XYZ oscillators.
	 */
	public void drawWave() {
		myParent.pushStyle();
		myParent.stroke(255);
		myParent.noFill();
		myParent.pushMatrix();
		myParent.beginShape();

		AudioOutput tempXY;
		if (useMix) {
			tempXY = mixXY;
		} else {
			tempXY = outXY;
		}

		for (int i = 0; i < tempXY.bufferSize() - 1; i++) {
			float xAudio = map(i, 0, tempXY.bufferSize(), 0, xyWidth);
			float lAudio = tempXY.left.get(i);
			// curveVertex(lAudio, rAudio*-1);
			myParent.vertex(xAudio, xyHeight * .25f - (xyHeight * .25f) * lAudio);
		}
		myParent.endShape();

		myParent.beginShape();
		for (int i = 0; i < tempXY.bufferSize() - 1; i++) {
			float xAudio = map(i, 0, tempXY.bufferSize(), 0, xyWidth);
			float rAudio = tempXY.right.get(i);
			// curveVertex(lAudio, rAudio*-1);
			myParent.vertex(xAudio, xyHeight * .75f + (xyHeight * .25f) * rAudio);
		}
		myParent.endShape();

		if (useZ) {
			myParent.beginShape();
			for (int i = 0; i < outZ.bufferSize() - 1; i++) {
				float xAudio = map(i, 0, outZ.bufferSize(), 0, xyWidth);
				float lAudio = outZ.left.get(i);
				// curveVertex(lAudio, rAudio*-1);
				myParent.vertex(xAudio, xyHeight * .5f - (xyHeight * .25f) * lAudio);
			}
			myParent.endShape();

		}
		myParent.popMatrix();
		myParent.popStyle();
	}

	int rectM;

	/**
	 * Get detail (number of points) for drawing an ellipse.
	 * 
	 * @return int
	 */
	public int ellipseDetail() {
		return ellipseDetail;
	}

	/**
	 * Set detail (number of points) for drawing an ellipse.
	 * 
	 * @param newED
	 *            int for facets of ellipse
	 */
	public void ellipseDetail(int newED) {
		ellipseDetail = newED;
	}

	/**
	 * Set rectMode (similar to Processing function).
	 * 
	 * @param rectModeVal
	 *            CORNER or CENTER
	 * @see <a href="https://processing.org/reference/rectMode_.html">Processing
	 *      Reference » rectMode()</a>
	 */
	public void rectMode(int rectModeVal) {
		if (rectModeVal == 0) {
			rectM = 0;
		} else if (rectModeVal == 3) {
			rectM = 3;
		}
	}

	/**
	 * Draw rectangle, expects rect(x, y, w, h).
	 * 
	 * @param x float - x position of rectangle
	 * @param y float - y position of rectangle
	 * @param w float - width of rectangle
	 * @param h float - height of rectangle
	 * 
	 * @see <a href="https://processing.org/reference/rect_.html">Processing
	 *      Reference » rect()</a>
	 */
	public void rect(float x, float y, float w, float h) {
		if (rectM == 3) {
			x -= w / 2;
			y -= h / 2;
		}
		vertexRect(x, y, w, h);
	}

	private void vertexRect(float x1, float y1, float w1, float h1) {
		beginShape();
		vertex(x1, y1);
		vertex(x1 + w1, y1);
		vertex(x1 + w1, y1 + h1);
		vertex(x1, y1 + h1);
		vertex(x1, y1);
		//		if (useZ)
		//			vertex(x1, y1);
		endShape();
	}

	/**
	 * Draw ellipse, expects ellipse(x, y, w, h).
	 * 
	 * @param x float - x position of ellipse
	 * @param y float - y position of ellipse
	 * @param w float - width of ellipse
	 * @param h float - height of ellipse
	 * 
	 * @see <a href="https://processing.org/reference/ellipse_.html">Processing
	 *      Reference » ellipse()</a>
	 */
	public void ellipse(float x, float y, float w, float h) {
		vertexEllipse(x, y, w, h);
	}

	// vertexEllipse!
	// based on
	// http://stackoverflow.com/questions/5886628/effecient-way-to-draw-ellipse-with-opengl-or-d3d
	private void vertexEllipse(float cx, float cy, float rx, float ry) {
		float theta = 2f * PI / (float) ellipseDetail;
		float c = cos(theta);// precalculate the sine and cosine
		float s = sin(theta);
		float t;

		float x = 1f;// we start at angle = 0
		float y = 0f;

		beginShape();
		for (int ii = 0; ii < ellipseDetail + 1; ii++) {
			// apply radius and offset
			vertex(x * rx + cx, y * ry + cy);// output vertex
			// apply the rotation matrix
			t = x;
			x = c * x - s * y;
			y = s * t + c * y;

		}
		endShape();
	}

	/**
	 * Draw point, expects point(x, y).
	 * 
	 * @param x float - x position of point
	 * @param y float - y position of point
	 * 
	 * @see <a href="https://processing.org/reference/point_.html">Processing
	 *      Reference » point()</a>
	 */
	public void point(float x, float y) {
		beginShape();
		vertex(x, y);
		vertex(x, y);
		endShape();
	}

	/**
	 * Draw point, expects point(x, y, z).
	 * 
	 * @param x float - x position of point
	 * @param y float - y position of point
	 * @param z float - z position of point
	 * 
	 * @see <a href="https://processing.org/reference/point_.html">Processing
	 *      Reference » point()</a>
	 */
	public void point(float x, float y, float z) {
		beginShape();
		vertex(x, y, z);
		vertex(x, y, z);
		endShape();
	}

	/**
	 * Draw line, expects line(x1, y1, x2, y2).
	 * 
	 * @param x1 float - first x position of point
	 * @param y1 float - first y position of point
	 * @param x2 float - second x position of point
	 * @param y2 float - second y position of point
	 * 
	 * @see <a href="https://processing.org/reference/line_.html">Processing
	 *      Reference » line()</a>
	 */
	public void line(float x1, float y1, float x2, float y2) {
		beginShape();
		vertex(x1, y1);
		vertex(x2, y2);
		endShape();
	}

	/**
	 * Draw line, expects line(x1, y1, z1, x2, y2, z2).
	 * 
	 * @param x1 float - first x position of point
	 * @param y1 float - first y position of point
	 * @param z1 float - first z position of point
	 * @param x2 float - second x position of point
	 * @param y2 float - second y position of point
	 * @param z2 float - second z position of point
	 * 
	 * @see <a href="https://processing.org/reference/line_.html">Processing
	 *      Reference » line()</a>
	 */
	public void line(float x1, float y1, float z1, float x2, float y2, float z2) {
		beginShape();
		vertex(x1, y1, z1);
		vertex(x2, y2, z2);
		endShape();
	}

	/**
	 * Begin multi-vertex shape.
	 * 
	 * @see <a href=
	 *      "https://processing.org/reference/beginShape_.html">Processing
	 *      Reference » beginShape()</a>
	 */
	public void beginShape() {
		currentShape = new XYShape();
		shapes.add(currentShape);
	}

	/**
	 * Currently sent as normal vertex (to be fixed). Simply here for code »
	 * vectorcode compatibility.
	 * 
	 * @param x float - x position of vertex point
	 * @param y float - y position of vertex point
	 * 
	 * @see <a href=
	 *      "https://processing.org/reference/curveVertex_.html">Processing
	 *      Reference » curveVertex()</a>
	 */
	public void curveVertex(float x, float y) {
		vertex(new PVector(x, y, 0), false);
	}

	/**
	 * Currently sent as normal vertex (to be fixed). Simply here for code »
	 * vectorcode compatibility.
	 * 
	 * @param x float - x position of vertex point
	 * @param y float - y position of vertex point
	 * @param z float - z position of vertex point
	 * 
	 * @see <a href=
	 *      "https://processing.org/reference/curveVertex_.html">Processing
	 *      Reference » curveVertex()</a>
	 */
	public void curveVertex(float x, float y, float z) {
		vertex(new PVector(x, y, z), true);
	}

	/**
	 * Add vertex to complex shape. Expects vertex(x, y).
	 * 
	 * @param x float - x position of vertex point
	 * @param y float - y position of vertex point
	 * 
	 * @see <a href="https://processing.org/reference/vertex_.html">Processing
	 *      Reference » vertex()</a>
	 */
	public void vertex(float x, float y) {
		vertex(new PVector(x, y, 0), false);
	}

	/**
	 * Add vertex to complex shape. Expects vertex(x, y, z).
	 * 
	 * @param x float - x position of vertex point
	 * @param y float - y position of vertex point
	 * @param z float - z position of vertex point
	 * 
	 * @see <a href="https://processing.org/reference/vertex_.html">Processing
	 *      Reference » vertex()</a>
	 */
	public void vertex(float x, float y, float z) {
		vertex(new PVector(x, y, z), true);
	}
	
	/**
	 * Add vertex to complex shape. Expects vertex(PVector()).
	 * 
	 * @param p PVector – pass xy[z] position as PVector.
	 * 
	 * @see <a href="https://processing.org/reference/vertex_.html">Processing
	 *      Reference » vertex()</a>
	 */
	public void vertex(PVector p) {
		if(p.z == 0f) {
			vertexAdd(p, false);
		}else {
			vertexAdd(p, true);
		}
	}

	private void vertex(PVector p, boolean mode3D) {
		vertexAdd(p, mode3D);
	}

	private void vertexAdd(PVector p, boolean mode3D) {
		float x, y;
		if(mode3D){
			x = norm(myParent.screenX(p.x, p.y, p.z), 0f, xyWidth + 0f);
			y = norm(myParent.screenY(p.x, p.y, p.z), 0f, xyHeight + 0f);
		}else{
			x = norm(myParent.screenX(p.x, p.y), 0f, xyWidth + 0f);
			y = norm(myParent.screenY(p.x, p.y), 0f, xyHeight + 0f);

		}
		PVector normP = new PVector(x, y, 0);
		if(useLimitPath){
			float sx, sy;
			if(mode3D){
				sx = myParent.screenX(p.x, p.y, p.z);
				sy = myParent.screenY(p.x, p.y, p.z);
			}else{
				sx = myParent.screenX(p.x, p.y);
				sy = myParent.screenY(p.x, p.y);
			}
			if ((sx >= limitVal && sx <= xyWidth - limitVal) && (sy >= limitVal && sy <= xyHeight - limitVal))
				currentShape.add(normP);
		}else{
			currentShape.add(normP);
		}
	}

	/**
	 * End complex shape.
	 * 
	 * @see <a href="https://processing.org/reference/endShape_.html">Processing
	 *      Reference » endShape()</a>
	 */
	public void endShape() {
		// not necessary in current setup. maybe useful later for z-axis
		if(currentShape.size() > 1)
			currentShape.get(currentShape.size()-1).z = 1f;
	}

	private XYShape currentShape = null;

	public class XYShapeList extends ArrayList<XYShape> {
		
		/**
		 * Returns float of total distance of drawn shapes, XYShapeList.
		 * 
		 * @return float
		 * 
		 */
		public float getDistance() {
			float sum = 0;
			for (XYShape shape : this) {
				sum += shape.getDistance();
			}
			return sum;
		}

		/**
		 * Returns int of total number of points in drawn shapes, XYShapeList.
		 * 
		 * @return float
		 * 
		 */
		public int totalSize() {
			int tsCounter = 0;
			for (XYShape shape : this) {
				for (int i = 0; i < shape.size(); i++) {
					tsCounter++;
				}
			}
			return tsCounter;
		}

		/**
		 * Returns ArrayList<PVector> of coordinates for all drawn shapes, XYShapeList.
		 * 
		 * @return float
		 * 
		 */
		public ArrayList<PVector> getPoints() {
			ArrayList<PVector> gp = new ArrayList<PVector>();
			for (XYShape shape : this) {
				for (int i = 0; i < shape.size(); i++) {
					gp.add(shape.get(i));
				}
			}
			return gp;
		}
	}

	public class XYShape extends ArrayList<PVector> {

		public float getDistance() {
			float sum = 0;
			for (int i = 0; i < size() - 1; i++) {
				sum += get(i).dist(get(i + 1));
			}
			return sum;
		}
	}
}
