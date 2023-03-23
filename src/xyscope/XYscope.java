/*
 * cc ted davis 2017-23
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
	public Minim minim, minimZ;
	public AudioRecorder recorder;

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

	/**
	 * minim Wavetable, for customizing XYZ oscillators.
	 */
	public XYWavetable tableX, tableY, tableZ;
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

	int sampleRate = 44100; // def 44100
	int bufferSize = 512; // def 1024
	String mixerName = "";
	
	int waveSizeVal = bufferSize;
	int waveSizeValOG = waveSizeVal;
	int maxPoints = waveSizeValOG;
	public float[] shapeX = new float[waveSizeVal];
	public float[] shapeY = new float[waveSizeVal];
	public float[] shapeZ = new float[waveSizeVal];
	float[] shapePreX = new float[waveSizeVal];
	float[] shapePreY = new float[waveSizeVal];
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
	
	// TYPE VARS
	String hershey_font[];
	int hheight = 21;
	float hleading = 30f;
	float hfactor = 1;
	int textAlignX = 37;
	int textAlignY = 101;

	
	/**
	 * List of built-in Hershey Fonts available
	 */
	public String[] fonts = {"astrology", "cursive", "cyrilc_1", "cyrillic", "futural", "futuram", "gothgbt", "gothgrt", "gothiceng", "gothicger", "gothicita", "gothitt", "greek", "greekc", "greeks", "japanese", "markers", "mathlow", "mathupp", "meteorology", "music", "rowmand", "rowmans", "rowmant", "scriptc", "scripts", "symbolic", "timesg", "timesi", "timesib", "timesr", "timesrb"};
	
	// VECTREX VARS
	boolean useVectrex = false;
	float vectrexAmp = .82f;
	float vectrexAmpInit = .6f;
	int vectrexRotation = 0;
	int vectrexWidth = 310;
	int vectrexHeight = 410;

	// LASER VARS
	boolean useLaser = false;
	public Minim minimR, minimBG;

	/**
	 * minim Oscil, for customizing Laser RGB oscillators.
	 */
	public Oscil waveR, waveG, waveB; 

	public XYWavetable tableR, tableG, tableB;
	Pan panR = new Pan(1);
	Pan panG = new Pan(-1);
	Pan panB = new Pan(1);
	public AudioOutput outR, outGB;

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
	public XYscope(PApplet theParent) {
		myParent = theParent;
		welcome();
		initMinim();
		setOutput();
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
		setOutput();
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
		initMinim();
		sampleRate = sampleR;
		setOutput();
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
		setMixer(xyMixer);
		sampleRate = sampleR;
		setOutput();
	}
	
	/**
	 * Initialize library in setup(), custom soundcard by string for XY and set
	 * custom sample rate (44100, 192000).
	 * 
	 * @param theParent
	 *            PApplet to apply to, typically 'this'
	 * @param xyMixer
	 *            Name of custom sound mixer to use for XY.
	 * @param sampleRateVal
	 *            Sample rate for soundcard (44100, 48000, 96000, 192000).
	 * @param bufferSizeVal
	 *            Size of buffer/latency for cpu/soundcard (128, 256, 512, 1024, 2048).
	 */
	public XYscope(PApplet theParent, String xyMixer, int sampleRateVal, int bufferSizeVal) {
		myParent = theParent;
		welcome();
		getMixerInfo();
		initMinim();
		setMixer(xyMixer);
		sampleRate = sampleRateVal;
		bufferSize = bufferSizeVal;
		setOutput();
	}

	private void welcome() {
		System.out.println("XYscope 3.0.0 - https://teddavis.org/xyscope");
		xyWidth = myParent.width;
		xyHeight = myParent.height;
		initText();
	}
	
	private void initText() {
		textFont("meteorology");
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
	
	// *** add mixer() as getter and setter?? add bufferSize as initial option param

	private void setMixer(String xyMixer) {
		getMixerInfo();
		Mixer mixer = getMixerByName(xyMixer);
		minim.setOutputMixer(mixer);
	}
	
	private void setOutput() {
		outXY = minim.getLineOut(Minim.STEREO, bufferSize, sampleRate);
		setWaveTable();
	}
	
	public int sampleRate() {
		return sampleRate;
	}
	
	public void sampleRate(int sampleRateVal) {
		sampleRate = sampleRateVal;
		setOutput();
	}
	
	public int bufferSize() {
		return outXY.bufferSize();
	}
	
	public void bufferSize(int bufferSizeVal) {
		if(bufferSizeVal > 16) {
			bufferSize = bufferSizeVal;
		}
		setOutput();
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
	 * Reset time-step used by XYZ oscillators if they slip when changing
	 * frequencies.
	 * 
	 */
	public void resetWaves() {
		waveReset();
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
	 * Returns current value for border that limits rendering to edges of screen.
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
		outGB = minimBG.getLineOut(Minim.STEREO, waveSizeValOG);
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
		waveG.patch(panG).patch(outGB);

		tableB = new XYWavetable(2);
		waveB = new Oscil(freq.x, amp.x, tableB);
		tableB.setWaveform(shapeB);
		waveB.patch(panB).patch(outGB);
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
	 * Returns current minimum values set for RGB laser.
	 * 
	 * @return PVector
	 */
	public PVector strokeMin(){
		return lsMin;
	}

	/**
	 * Set new minimum values for RGB laser, as 3 floats.
	 * 
	 * @param minR
	 *            value between 0.0 - 255.0
	 * @param minG
	 *            value between 0.0 - 255.0
	 * @param minB
	 *            value between 0.0 - 255.0
	 */
	public void strokeMin(float minR, float minG, float minB){
		strokeMin(new PVector(minR, minG, minB));
	}

	/**
	 * Set new minimum values for RGB laser, as PVector.
	 * 
	 * @param minPV
	 *            PVector with 3 values between 0.0 - 255.0
	 */
	public void strokeMin(PVector minPV){
		lsMin = new PVector(minPV.x, minPV.y, minPV.z);
	}
	
	/**
	 * Returns current white balance (mixture for white) settings for RGB laser.
	 * 
	 * @return PVector
	 */
	public PVector strokeWB(){
		return lsWB;
	}

	/**
	 * Set white balance (mixture for white) for RGB laser, as 3 floats.
	 * 
	 * @param wbR
	 *            value between 0.0 - 255.0
	 * @param wbG
	 *            value between 0.0 - 255.0
	 * @param wbB
	 *            value between 0.0 - 255.0
	 */
	public void strokeWB(float wbR, float wbG, float wbB){
		strokeWB(new PVector(wbR, wbG, wbB));
	}

	/**
	 * Set white balance (mixture for white) for RGB laser, as PVector.
	 * 
	 * @param wbPV
	 *            PVector with 3 values between 0.0 - 255.0
	 */
	public void strokeWB(PVector wbPV){
		lsWB = new PVector(wbPV.x, wbPV.y, wbPV.z);
	}

	/**
	 * Returns current dashes used for RGB waves of laser.
	 * 
	 * @return PVector
	 */
	public PVector strokeDash(){
		return lsDash;
	}

	/**
	 * Set same number of dashes for RGB laser.
	 * 
	 * @param newDash
	 *            int
	 */
	public void strokeDash(int newDash){
		strokeDash(new PVector(newDash, newDash, newDash));
	}

	/**
	 * Set seperate number of dashes per color for RGB laser.
	 * 
	 * @param newDashR
	 *            int
	 * @param newDashG
	 *            int
	 * @param newDashB
	 *            int
	 */
	public void strokeDash(int newDashR, int newDashG, int newDashB){
		strokeDash(new PVector(newDashR, newDashG, newDashB));
	}

	/**
	 * Set dashes for RGB laser, as PVector.
	 * 
	 * @param newDash
	 *            PVector with 3 values
	 */
	public void strokeDash(PVector newDash){
		lsDash = new PVector(newDash.x, newDash.y, newDash.z);
	}

	/**
	 * Set stroke for RGB laser, as 3 floats.
	 * 
	 * @param r
	 *            float from 0.0 – 255.0
	 * @param g
	 *            float from 0.0 – 255.0
	 * @param b
	 *            float from 0.0 – 255.0
	 */
	public void stroke(float r, float g, float b){
		stroke(new PVector(r, g, b));
	}

	/**
	 * Set stroke for RGB laser, as PVector.
	 * 
	 * @param newDash
	 *            PVector with 3 values, from 0.0 – 255.0
	 */
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

	/**
	 * Get current frequency for R, G, B oscillators for laser as a PVector.
	 * 
	 * @return PVector
	 */
	public PVector strokeFreq() {
		return lsFreq;
	}

	/**
	 * Set new frequency for all RGB oscillators of laser together as single float.
	 * 
	 * @param newFreq
	 *            float
	 */
	public void strokeFreq(float newFreq) {
		lsFreq = new PVector(newFreq, newFreq, newFreq);
		strokeFreq(lsFreq);
	}

	/**
	 * Set new frequency for all R + G + B oscillators of laser.
	 * 
	 * @param newFreqR
	 *            float
	 * @param newFreqG
	 *            float
	 * @param newFreqB
	 *            float
	 */
	public void strokeFreq(float newFreqR, float newFreqG, float newFreqB) {
		lsFreq = new PVector(newFreqR, newFreqG, newFreqB);
		strokeFreq(lsFreq);
	}

	/**
	 * Set new frequency for each RGB oscillator of laser separately using a PVector.
	 * 
	 * @param newFreq
	 *            PVector
	 */
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
	 * Set new frequency for all X + Y + Z oscillators.
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
	 * Adjust the (x, y) panning, mainly useful if swapping cables digitally. Default (-1.0, 1.0)
	 * 
	 * @param panXVal
	 *            float - pan for x/left channel
	 * @param panYVal
	 *            float - pan for y/right channel	 */
	public void pan(float panXVal, float panYVal) {
		panX.setPan(panXVal);
		panY.setPan(panYVal);
	}

	/**
	 * Enable/Disable easing transitions from one set of buildWaves() to the
	 * next. Default is false. Deprecated in v2.0+
	 * 
	 * @param easeBool
	 *            true/false
	 */
	public void ease(boolean easeBool) {
		useEase = easeBool;
	}

	/**
	 * Check if easing between each frame of buildWaves() is enabled.
	 * Deprecated in v2.0+
	 * 
	 * @return boolean
	 */
	public boolean ease() {
		return useEase;
	}

	/**
	 * Returns current easeAmount, 0.0 - 1.0. Deprecated in v2.0+
	 * 
	 * @return float
	 */
	public float easeAmount() {
		return easeVal;
	}

	/**
	 * Set new easing value for speed between buildWave() transitions. Deprecated in v2.0+
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
	
	
	private double lerp(double start, double end, double amt) {
		  return start + (end-start)*amt;
		}
	
	public void buildWavesTest(ArrayList<PVector> wc) {
		if(wc.size() > 128) {
			waveSize(floor((wc.size())/2)*2);
		}
//		waveSize(1024);
		float[] mfx = new float[wc.size()];
		float[] mfy = new float[wc.size()];
		for(int i=0; i<wc.size();i++) {
			PVector tc = wc.get(i);
			mfx[i] = map(tc.x, 0f, 1f, -1f, 1f);
			mfy[i] = map(tc.y, 0f, 1f, 1f, -1f);
		}
		tableX.setWaveform(mfx);
		tableY.setWaveform(mfy);
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
		if(bwm == 0) { // waveform gen v4 mar 2020... 23
			if (shapes.size() > 0) {
				double wave_size = shapes.getPoints().size()*stepsSize;
				double total_dist = shapes.getDistance();
				double tot = 0.0;
				ArrayList<PVector> wave_col = new ArrayList<PVector>();
				for (int i=0; i < shapes.size(); i++) {
					double shapeDist = shapes.get(i).getDistance();
					ArrayList<PVector> pv = shapes.get(i);
					for (int j=0; j < pv.size()-1; j++) { // what about point?
						PVector p1 = pv.get(j);
						PVector p2 = pv.get(j+1);
						double line_dist = dist(p1.x, p1.y, p2.x, p2.y);
						tot += line_dist;
						double sec_per = Math.round(1+line_dist / total_dist * wave_size);
						double steps = 1.0 / sec_per;
						for (int k=0; k <= sec_per; k++) {
							PVector seg = PVector.lerp(p1, p2, (float)((float)k*steps));
							wave_col.add(seg);
						}
					}
				}
				
				float[] mfx = new float[waveSize()];
				float[] mfy = new float[waveSize()];
				float[] mfz = new float[waveSize()];
				for(int i=0; i<waveSize();i++) {
					int waveIndex = floor(map(i, 0, waveSize(), 0, wave_col.size()));
					PVector tc = wave_col.get(waveIndex);
					mfx[i] = tc.x * 2f - 1f;
					mfy[i] = tc.y * -2f + 1f;
					mfz[i] = zaxisMax;
					
					// *** double check if z works w/ shape on.off..
					if(tc.z == 1f)
						mfz[i] = zaxisMin;
					
					// *** test vectrex
					float tfxx = mfx[i];
					float tfyy = mfy[i];

					if(useVectrex){
						if(vectrexRotation == 90){
							mfx[i] = tfyy;
							mfy[i] = tfxx*-1;
						}else if(vectrexRotation == -90){
							mfx[i] = tfyy*-1;
							mfy[i] = tfxx;
						}else if(vectrexRotation == 0){
							mfx[i] = tfxx*-1;
							mfy[i] = tfyy*-1;
						}
					}
					
				}
				
				setWaveforms(mfx, mfy, mfz);
				
				if(useLaser){
					buildLaser();
				}
			}else {
				emptyWave();
			}
		} else if(bwm == -3){ // waveform gen v3 sep 2018
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
							tfz[i] = zaxisMin;

							if(tc.z == 1f)
								tfz[i] = zaxisMax;

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

				setWaveforms(mfx, mfy, mfz);
				
				if(useLaser){
					buildLaser();
				}
			}else{
				emptyWave();
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
	
	public void setWaveforms(float[] mfx, float[] mfy) {
		float[] mfz = new float[0];
		setWaveforms(mfx, mfy, mfz);
	}
	
	public void setWaveforms(float[] mfx, float[] mfy, float[] mfz) {
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
	}
	
	private void emptyWave() {
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
	
	// *** remove??
	public void warpWave(int mx, int pmx) {
		 float warpPoint = constrain( (float)pmx / myParent.width, 0, 1 );
		 float warpTarget = constrain( (float)mx / myParent.width, 0, 1 );
		 tableX.warp( warpPoint, warpTarget );
		 tableY.warp( warpPoint, warpTarget );
	}
	
	private void buildLaser() {
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
	 * Check if waveform smoothing is enabled/disabled. Deprecated since v3
	 * 
	 * @return boolean
	 */
	public boolean smoothWaves() {
		return useSmooth;
	}

	/**
	 * Enable/disable Smooth waveforms to reduce visibility of points in
	 * drawing. Default is false. Deprecated since v3
	 * 
	 * @param smoothWavesBool
	 *            true/false
	 */
	public void smoothWaves(boolean smoothWavesBool) {
		useSmooth = smoothWavesBool;
	}

	/**
	 * Get number of steps for smoothing waveforms. Deprecated since v3
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
	 * Set number of steps for smoothing waveforms. Default is 12. Deprecated since v3
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
	
	public void drawPath() {
		drawPath(255, 255, 255);
	}

	/**
	 * Draw path of points remapped from normalized values to width + height of
	 * sketch.
	 */
	public void drawPath(float r, float g, float b) {
		myParent.pushStyle();
		myParent.noFill();
		myParent.stroke(r, g, b);
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
	
	public void drawPoints() {
		drawPoints(0, 255, 0);
	}

	/**
	 * Draw points (as 3px ellipses) remapped from normalized values to width +
	 * height of sketch.
	 */
	public void drawPoints(float r, float g, float b) {
		myParent.pushStyle();
		myParent.fill(r, g, b);
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
	
	public void drawXY() {
		drawXY(50, 255, 50);
	}

	/**
	 * Simulate X-Y mode of oscilloscope output.
	 */
	public void drawXY(float r, float g, float b) {
		myParent.pushStyle();
		myParent.noFill();
		myParent.stroke(r, g, b);
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
			myParent.fill(r, g, b);
			myParent.noStroke();
			myParent.ellipse(mx, my, debugSize, debugSize);
			myParent.popStyle();
		}

		myParent.popMatrix();
		myParent.popStyle();
	}

	public void drawWaveform(){
		drawWaveform(50, 50, 255, 255, 50, 50);
	}
	
	/**
	 * Draw waveform of all XYZ oscillators.
	 * <ul>
	 * <li>Red: X</li>
	 * <li>Blue: Y</li>
	 * <li>Green: Z</li>
	 * </ul>
	 */
	public void drawWaveform(float lr, float lg, float lb, float rr, float rg, float rb) {
		myParent.pushStyle();
		myParent.noFill();
		myParent.pushMatrix();
		myParent.beginShape();

		// X -> L
		myParent.stroke(lr, lg, lb);
		for (int i = 0; i < xyWidth; i++) {
			myParent.vertex(i, (float) xyHeight * .25f
					- ((float) xyHeight * .125f) * tableX.value((float) i / (float) xyWidth));
		}
		myParent.endShape();

		// Y -> R
		myParent.stroke(rr, rg, rb);
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
			myParent.fill(lr, lg, lb);
			myParent.ellipse(lx - 2, ly, debugSize, debugSize);
			myParent.fill(rr, rg, rb);
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
		ellipseDetail = abs(newED);
	}
	
	/**
	 * Set detail (number of points) for drawing an ellipse.
	 * 
	 * @param newED
	 *            float for facets of ellipse (rounded)
	 */
	public void ellipseDetail(float newED) {
		ellipseDetail = abs(round(newED));
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
	
	int stepsSize = 24;
	/**
	 * Get steps multiplier (number of segments) between each point.
	 * 
	 * @return int
	 */
	public int steps() {
		return stepsSize;
	}

	/**
	 * Set steps multiplier (number of segments) between each point.
	 * 
	 * @param newSteps
	 *            float for segments multiplier (rounded)
	 */
	public void steps(float newSteps) {
		stepsSize = parseInt(newSteps);
	}
	
	// non-params drawing
	public void point() {
		line(xyWidth/2, xyHeight/2, xyWidth/2+1, xyHeight/2+1);
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
		line(x, y, x+1, y+1);
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
		line(x, y, z, x+1, y+1, z+1);
	}
	
	// non-params drawing
	public void line() {
		beginShape();
		vertex(0, 0);
		vertex(xyWidth, xyHeight);
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
	
	// non-params drawing
		public void square() {
			int x = xyWidth/2;
			int y = xyHeight/2;
			int w = xyHeight;
			if (rectM == 3) {
				x -= w / 2;
				y -= w / 2;
			}
			vertexRect(x, y, w, w);
		}
	
	/**
	 * Draw square, expects square(x, y, extent).
	 * 
	 * @param x float - x position of square
	 * @param y float - y position of square
	 * @param extent float - width + height of square
	 * 
	 * @see <a href="https://processing.org/reference/square_.html">Processing
	 *      Reference » rect()</a>
	 */
	public void square(float x, float y, float e) {
		if (rectM == 3) {
			x -= e / 2;
			y -= e / 2;
		}
		vertexRect(x, y, e, e);
	}
	
	// non-params drawing
	public void rect() {
		int x = xyWidth/2;
		int y = xyHeight/2;
		int w = xyHeight;
		if (rectM == 3) {
			x -= w / 2;
			y -= w / 2;
		}
		vertexRect(x, y, w, w);
	}
	
	/**
	 * Draw rectangle (square), expects rect(x, y, w).
	 * 
	 * @param x float - x position of rectangle
	 * @param y float - y position of rectangle
	 * @param w float - width of rectangle
	 * 
	 * @see <a href="https://processing.org/reference/rect_.html">Processing
	 *      Reference » rect()</a>
	 */
	public void rect(float x, float y, float w) {
		if (rectM == 3) {
			x -= w / 2;
			y -= w / 2;
		}
		vertexRect(x, y, w, w);
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
	
	// non-params drawing
	public void circle() {
		vertexEllipse(xyWidth/2, xyHeight/2, xyHeight, xyHeight);
	}
	
	public void ellipse() {
		vertexEllipse(xyWidth/2, xyHeight/2, xyHeight, xyHeight);
	}
	
	/**
	 * Draw circle (ellipse), expects circle(x, y, extent).
	 * 
	 * @param x float - x position of circle
	 * @param y float - y position of circle
	 * @param extent float - width + height of circle
	 * 
	 * @see <a href="https://processing.org/reference/circle_.html">Processing
	 *      Reference » ellipse()</a>
	 */
	public void circle(float x, float y, float e) {
		vertexEllipse(x, y, e, e);
	}
	
	/**
	 * Draw ellipse (circle), expects ellipse(x, y, w).
	 * 
	 * @param x float - x position of ellipse
	 * @param y float - y position of ellipse
	 * @param d float - diameter of ellipse
	 * 
	 * @see <a href="https://processing.org/reference/ellipse_.html">Processing
	 *      Reference » ellipse()</a>
	 */
	public void ellipse(float x, float y, float d) {
		vertexEllipse(x, y, d, d);
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
		float theta = TWO_PI / (float) ellipseDetail;
		float c = cos(theta);// precalculate the sine and cosine
		float s = sin(theta);
		float t;

		float x = .5f;// we start at angle = 0
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
	
	// non-param drawings
	public void lissajous(){
		lissajous(xyWidth/2, xyHeight/2, xyHeight/2, 1, 2, 0, 180);
	}
	
	/**
	 * Draw lissajous curve, expects lissajous(xPos, yPos, radius, ratioA, ratioB, phase, resolution).
	 * 
	 * @param xPos float - x position of lissajous
	 * @param yPos float - y position of lissajous
	 * @param radius float - size of lissajous
	 * @param ratioA float - lissajous ratio part A
	 * @param ratioB float - lissajous ratio part B
	 * @param phase float - phase of lissajous curve
	 * @param resolution float - number of points for lissajous curve
	 * 
	 */
	public void lissajous(float xPos, float yPos, float radius, float ratioA, float ratioB, float phase, float resolution){
		resolution = constrain(resolution, 1f, 360f);
		beginShape();
		for(int i=0; i < resolution+1; i++){
			float theta = TWO_PI/resolution;
			float x = sin(i*theta*ratioA)*radius;
			float y = sin(radians(phase)+i*theta*ratioB)*radius;
			vertex(xPos + x, yPos + y);
		}
		endShape();
	}
	
	public void box() {
		box(xyHeight/2, xyHeight/2, xyHeight/2);
	}

	/**
	 * Draw box, expects box(size).
	 * 
	 * @param size float - size of box
	 * 
	 * @see <a href="https://processing.org/reference/box_.html">Processing
	 *      Reference » box()</a>
	 */

	public void box(float size) {
		box(size, size, size);
	}

	/**
	 * Draw box, expects box(rx, ryz).
	 * 
	 * @param rx float - size of box in x-axis
	 * @param ryz float - size of box in y/z-axis
	 * 
	 * @see <a href="https://processing.org/reference/box_.html">Processing
	 *      Reference » box()</a>
	 */

	public void box(float rx, float ryz) {
		box(rx, ryz, ryz);
	}

	/**
	 * Draw box, expects box(rx, ry, rz).
	 * 
	 * @param rx float - size of box in x-axis
	 * @param ry float - size of box in y-axis
	 * @param rz float - size of box in z-axis
	 * 
	 * @see <a href="https://processing.org/reference/box_.html">Processing
	 *      Reference » box()</a>
	 */

	// extended from: https://stackoverflow.com/a/72277489/10885535
	public void box(float rxt, float ryt, float rzt) {
		// half size: keep the pivot at the center of the mesh
		float rx = rxt * .5f;
		float ry = ryt * .5f;
		float rz = rzt * .5f;
		beginShape();

		// back (-z)
		vertex(-rx, -ry, -rz);
		vertex(+rx, -ry, -rz);
		vertex(+rx, +ry, -rz);
		vertex(-rx, +ry, -rz);
		// slide to otherside
		vertex(-rx, -ry, -rz);
		// front (+z)
		vertex(-rx, -ry, +rz);
		vertex(+rx, -ry, +rz);
		vertex(+rx, +ry, +rz);
		vertex(-rx, +ry, +rz);
		// top (-y)
		vertex(-rx, -ry, +rz);
		vertex(-rx, -ry, -rz);
		vertex(+rx, -ry, -rz);
		vertex(+rx, -ry, +rz);
		// bottom (+y)
		vertex(+rx, +ry, +rz);
		vertex(+rx, +ry, -rz);
		vertex(-rx, +ry, -rz);
		vertex(-rx, +ry, +rz);
		// left (-x)
		vertex(-rx, -ry, +rz);
		vertex(-rx, -ry, -rz);
		vertex(-rx, +ry, -rz);
		vertex(-rx, +ry, +rz);
		// slide to otherside
		vertex(-rx, -ry, +rz);
		// right (+x)
		vertex(+rx, -ry, +rz);
		vertex(+rx, -ry, -rz);
		vertex(+rx, +ry, -rz);
		vertex(+rx, +ry, +rz);
		endShape();
	}

	// based on: Examples » Topics » Textures » Texture Sphere
	int dx = 24;
	int dy = 24;
	
	// non-param drawing
	public void sphere() {
		ellipsoid(xyHeight/3, xyHeight/3, xyHeight/3, dx, dy);
	}
	
	public void ellipsoid() {
		ellipsoid(xyHeight/3, xyHeight/3, xyHeight/3, dx, dy);
	}
	
	/**
	 * Draw sphere, expects sphere(size).
	 * 
	 * @param size float - size of sphere
	 * 
	 * @see <a href="https://processing.org/reference/sphere_.html">Processing
	 *      Reference » sphere()</a>
	 */
	
	public void sphere(float rs) {
		ellipsoid(rs, rs, rs, dx, dy);
	}

	/**
	 * Draw sphere, expects sphere(size, verticesCount).
	 * 
	 * @param size float - size of sphere
	 * @param verticesCount int - number of horzontal + vertical vertices
	 * 
	 * @see <a href="https://processing.org/reference/sphere_.html">Processing
	 *      Reference » sphere()</a>
	 */
	
	public void sphere(float rs, int dxy) {
		ellipsoid(rs, rs, rs, dxy, dxy);
	}
	
	/**
	 * Draw sphere, expects sphere(size, verticiesW, verticiesH).
	 * 
	 * @param size float - size of sphere
	 * @param verticiesW int - number of horzontal vertices
	 * @param verticiesH int - number of vertical vertices
	 * 
	 * @see <a href="https://processing.org/reference/sphere_.html">Processing
	 *      Reference » sphere()</a>
	 */

	public void sphere(float rs, int dx, int dy) {
		ellipsoid(rs, rs, rs, dx, dy);
	}
	
	/**
	 * Draw ellipsoid, expects ellipsoid(rx, ry, rz).
	 * 
	 * @param rx float - size in x-axis
	 * @param ry float - size in y-axis
	 * @param rz float - size in z-axis
	 * 
	 * @see <a href="https://processing.org/reference/sphere_.html">Processing
	 *      Reference » sphere()</a>
	 */

	public void ellipsoid(float rx, float ry, float rz) {
		ellipsoid(rx, ry, rz, dx, dy);
	}

	/**
	 * Draw ellipsoid, expects ellipsoid(rx, ry, rz, verticesCount).
	 * 
	 * @param rx float - size in x-axis
	 * @param ry float - size in y-axis
	 * @param rz float - size in z-axis
	 * @param verticesCount int - number of horzontal + vertical vertices
	 * 
	 * @see <a href="https://processing.org/reference/sphere_.html">Processing
	 *      Reference » sphere()</a>
	 */
	
	public void ellipsoid(float rx, float ry, float rz, int dxy) {
		ellipsoid(rx, ry, rz, dxy, dxy);
	}

	/**
	 * Draw ellipsoid, expects ellipsoid(rx, ry, rz, verticesCount).
	 * 
	 * @param rx float - size in x-axis
	 * @param ry float - size in y-axis
	 * @param rz float - size in z-axis
	 * @param verticiesW int - number of horzontal vertices
	 * @param verticiesH int - number of vertical vertices
	 * 
	 * @see <a href="https://processing.org/reference/sphere_.html">Processing
	 *      Reference » sphere()</a>
	 */
	
	public void ellipsoid(float rxt, float ryt, float rzt, int dx, int dy) {

		float rx = rxt;// * .5f;
		float ry = ryt;// * .5f;
		float rz = rzt;// * .5f;
				
		int numPointsW;
		int numPointsH_2pi;
		int numPointsH;

		float[] coorX;
		float[] coorY;
		float[] coorZ;
		float[] multXZ;

		int numvW = constrain(dx, 1, 50);
		int numvH_2pi = constrain(dy, 1, 50);

		// The number of points around the width and height
		numPointsW=numvW+1;
		numPointsH_2pi=numvH_2pi;  // How many actual pts around the sphere (not just from top to bottom)
		numPointsH=ceil((float)numPointsH_2pi/2f)+1;  // How many pts from top to bottom (abs(....) b/c of the possibility of an odd numPointsH_2pi)

		coorX=new float[numPointsW];   // All the x-coor in a horizontal circle radius 1
		coorY=new float[numPointsH];   // All the y-coor in a vertical circle radius 1
		coorZ=new float[numPointsW];   // All the z-coor in a horizontal circle radius 1
		multXZ=new float[numPointsH];  // The radius of each horizontal circle (that you will multiply with coorX and coorZ)

		for (int i=0; i<numPointsW; i++) {  // For all the points around the width
			float thetaW=i*2f*PI/(numPointsW-1);
			coorX[i]=sin(thetaW);
			coorZ[i]=cos(thetaW);
		}

		for (int i=0; i<numPointsH; i++) {  // For all points from top to bottom
			if (parseInt(numPointsH_2pi/2) != (float)numPointsH_2pi/2 && i==numPointsH-1) {  // If the numPointsH_2pi is odd and it is at the last pt
				float thetaH=(i-1f)*2f*PI/(numPointsH_2pi);
				coorY[i]=cos(PI+thetaH);
				multXZ[i]=0f;
			} else {
				//The numPointsH_2pi and 2 below allows there to be a flat bottom if the numPointsH is odd
				float thetaH=i*2f*PI/(numPointsH_2pi);

				//PI+ below makes the top always the point instead of the bottom.
				coorY[i]=cos(PI+thetaH);
				multXZ[i]=sin(thetaH);
			}
		}

		beginShape();
		for (int i=0; i<(numPointsH-1); i++) {  // For all the rings but top and bottom
			// Goes into the array here instead of loop to save time
			float coory=coorY[i];
			float cooryPlus=coorY[i+1];

			float multxz=multXZ[i];
			float multxzPlus=multXZ[i+1];

			for (int j=0; j<numPointsW; j++) { // For all the pts in the ring
				vertex(coorX[j]*multxz*rx, coory*ry, coorZ[j]*multxz*rz);
				vertex(coorX[j]*multxzPlus*rx, cooryPlus*ry, coorZ[j]*multxzPlus*rz);
			}
		}
		endShape();
	}
	
	// non-param drawing
	public void torus() {
		torus(xyHeight/4, xyHeight/6, dx, dy);
	}
	
	public void torus(float radius, float tubeRadius) {
		torus(radius, tubeRadius, dx, dy);
	}
	
	public void torus(float radius, float tubeRadius, int dxy) {
		torus(radius, tubeRadius, dxy, dxy);
	}
	
	/**
	 * Draw torus, expects torus(radius, tubeRadius, detailX, detailY).
	 * 
	 * @param radius float - radius of torus
	 * @param tubeRadius float - radius of torus tube
	 * @param detailX int - number of horzontal vertices
	 * @param detailY int - number of vertical vertices
	 * 
	 */
	
	// built upon: https://processing.org/examples/toroid.html
	public void torus(float radius, float tubeRadius, int dx, int dy) {
	  dx = constrain(dx, 1, 50);
	  dy = constrain(dy, 1, 50);
	  //tubeRadius = constrain(tubeRadius, 1, radius);

	  float angle = 0f;
	  float latheAngle = 0f;
	  PVector vertices[] = new PVector[dx+1];
	  PVector vertices2[] = new PVector[dx+1];

	  // fill arrays
	  for (int i=0; i<=dx; i++) {
	    vertices[i] = new PVector();
	    vertices2[i] = new PVector();
	    vertices[i].x = radius + sin(radians(angle))*tubeRadius;
	    vertices[i].z = cos(radians(angle))*tubeRadius;
	    angle+=360.0f/dx;
	  }

	  // draw toroid
	  latheAngle = 0f;
	  for (int i=0; i<=dy; i++) {
	    beginShape();

	    for (int j=0; j<=dx; j++) {
	      if (i > 0) {
	        vertex(vertices2[j].x, vertices2[j].y, vertices2[j].z);
	      }
	      vertices2[j].x = cos(radians(latheAngle))*vertices[j].x;
	      vertices2[j].y = sin(radians(latheAngle))*vertices[j].x;
	      vertices2[j].z = vertices[j].z;
	      vertex(vertices2[j].x, vertices2[j].y, vertices2[j].z);
	    }
	    latheAngle+=360.0f/dy;
	    endShape();
	  }
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
	
	public void vertex() {
		vertex(new PVector(myParent.random(xyWidth), myParent.random(xyHeight), 0), false);
	}
	
	public void curveVertex() {
		vertex(new PVector(myParent.random(xyWidth), myParent.random(xyHeight), 0), false);
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
			if ((sx >= limitVal && sx <= xyWidth - limitVal) && (sy >= limitVal && sy <= xyHeight - limitVal)) {
				currentShape.add(normP);
			}else {
				endShape();
				beginShape();
//				initShape = true;
			}
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
	public void endShape(int close) {
		if(close == 2) {
			if(currentShape.size() > 0) {
				PVector lastC = currentShape.get(0);
				currentShape.add(new PVector(lastC.x, lastC.y, 0));
			}
		}
		endShape();
//		// not necessary in current setup. maybe useful later for z-axis
//		if(currentShape.size() > 1) {
//			currentShape.get(currentShape.size()-1).z = 1f;
//		}else {
//			// Calculate index of last element
//	        int index = shapes.size() - 1;
//	  
//	        // Delete last element by passing index
//	        shapes.remove(index);
//		}
	}

	/**
	 * End complex shape.
	 * 
	 * @see <a href="https://processing.org/reference/endShape_.html">Processing
	 *      Reference » endShape()</a>
	 */
	public void endShape() {
		// not necessary in current setup. maybe useful later for z-axis
		if(currentShape.size() > 1) {
			currentShape.get(currentShape.size()-1).z = 1f;
		}else {
			// Calculate index of last element
	        int index = shapes.size() - 1;
	  
	        // Delete last element by passing index
	        shapes.remove(index);
		}
	}
	
	/*
	 * RECORDER OUT FUNCTIONS */
	
	public void recorderBegin() {
		recorderBegin("XYscope");
	}

	public void recorderBegin(String filename) {
		String date = new java.text.SimpleDateFormat("yyyy_MM_dd_kkmmssSSS").format(new java.util.Date()); 
	  	recorder = minim.createRecorder(outXY, filename + "_" + date + ".wav");
		recorder.beginRecord();
		println("XYscope - beginRecord");
	}

	public void recorderEnd() {
		recorder.endRecord();
		recorder.save();
		println("XYscope - endRecord + saved!");
	}

	
	
	/* 
	 * HERSHEY Font meets Processing text() functions 
	 * HUGE THX to this lib: https://github.com/ixd-hof/HersheyFont
	 * 
	 * */
	
	private boolean contains(String[] arr, String val) {
		for(int i=0; i<arr.length; i++) {
			if(arr[i].equals(val)) { 
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Set Hershey Font for built-in text rendering. Use println(xy.fonts) for complete list available.
	 * 
	 * @param fontName
	 *            String of Hershey Font name
	 */
	public void textFont(String fontName) {
		if(contains(fonts, fontName)) {
			String [] hershey_font_org;
			hershey_font_org = myParent.loadStrings("hershey_fonts/" + fontName + ".jhf");

			String hershey_font_string = "";

			for (int i=0; i<hershey_font_org.length; i++) {
				String line = hershey_font_org[i].trim();
				if (line.charAt(0) >= 48 && line.charAt(0) <= 57)
					hershey_font_string += line + "\n";
				else {
					hershey_font_string = hershey_font_string.substring(0, hershey_font_string.length()-1) + line + "\n";
				}
			}
			hershey_font = hershey_font_string.split("\n");
		}
	}
	
	/**
	 * Get size for built-in Hershey text rendering.
	 * 
	 * @return Float - size of text
	 */
	public float textSize(){
		return hfactor;
	}
	
	/**
	 * Set size for built-in Hershey text rendering.
	 * 
	 * @param fontSize
	 *            Float - size of text
	 */
	public void textSize(float fontSize){
		hfactor = fontSize/hheight;
	}
	
	/**
	 * Get the spacing between lines of text in units of pixels.
	 * 
	 * @return Float - the size in pixels for spacing between lines
	 */
	public float textLeading(){
		return hleading;
	}
	
	/**
	 * Sets the spacing between lines of text in units of pixels.
	 * 
	 * @param fontLeading
	 *            Float - 	the size in pixels for spacing between lines
	 */
	public void textLeading(float leading){
		hleading = leading;
	}
		
	/**
	 * Set horizontal alignment of built in Hershey text rending. LEFT (default), CENTER or RIGHT.
	 * 
	 * @param taX
	 *            LEFT, CENTER, RIGHT
	 */
	public void textAlign(int taX) {
		textAlign(taX, textAlignY);
	}
	
	/**
	 * Set horizontal and vertical alignment of built in Hershey text rending. Horizontal: LEFT (default), CENTER or RIGHT. Vertical: TOP, CENTER (default), BOTTOM.
	 * 
	 * @param taX
	 *            LEFT, CENTER, RIGHT
	 * @param taY
	 *            TOP, CENTER, BOTTOM
	 */
	public void textAlign(int taX, int taY) {
		if(taX == 37 || taX == 3 || taX == 39) {
			textAlignX = taX;
		}
		if(taY == 101 || taY == 3 || taY == 102) {
			textAlignY = taY;
		}
	}
	
	// non-param drawing
	public void text() {
		text("XYscope", xyWidth/2, xyHeight/2);
	}
	
	public void text(float s, float x, float y) {
		text(nf(s), x, y);
	}
	public void text(int s, float x, float y) {
		text(nf(s), x, y);
	}
	
	
	/**
	 * Render text using built in Hershey Fonts. 
	 * 
	 * @param s
	 *            String - text to display
	 * @param x
	 *            float - horizontal position of text
	 * @param y
	 *            float - vertical position of text
	 */
	public void text(String s, float x, float y) {
		String[] parts = splitTokens(s, "\n\r");
		
		switch(textAlignY) {
		case 101:
			y += hfactor * 12;
			break;
		
		case 102:
			y -= hfactor * 21 * parts.length;
			break;
		default:
			if(parts.length > 1) {
				y -= hfactor * 21 * parts.length/2;
			}
			break;
		}
		
		float yOffset = y;
		for(int i=0; i<parts.length; i++) {
			textParse(parts[i], x, yOffset);
			yOffset += hfactor * hheight + hleading;
		}
	}
	
	public void textParse(String s, float x, float y) {
//		PVector ta = textOffset(s, x, y);
		
		x += 5 * hfactor;
		
		switch(textAlignX) {
		case 3:
			x -= textWidth(s) / 2;
			break;
		case 39:
			x -= textWidth(s);
			break;
		}
		
		switch(textAlignY) {
		case 101:
			y += hfactor * 12;
			break;
		
		case 102:
			y -= hfactor * 12;
			break;
		}
		
		myParent.pushMatrix();		
		myParent.translate(x, y);
		
		for (int i=0; i<s.length(); i++){
			draw_character(s.charAt(i)); // custom drawChar for XYscope
		}
		myParent.popMatrix();
	}
	
	/**
	 * Calculate width of provided character of text, for built-in Hershey Font . 
	 * 
	 * @param s
	 *            char - character to measure width of
	 *            
	 * @return float
	 */	
	public float textWidth(int c) {
		String h = hershey_font[c - 32 ];

		int start_col = h.indexOf(" ");

		int h_left = hershey2coord(h.charAt(start_col+3));
		int h_right = hershey2coord(h.charAt(start_col+4));
		float h_width = h_right - h_left * hfactor;

		return h_width + 5 * hfactor;
	}
	
	/**
	 * Calculate width of provided string of text, for built-in Hershey Font . 
	 * 
	 * @param s
	 *            String - text to measure width of
	 *            
	 * @return float
	 */	
	public float textWidth(String s) {
		String[] parts = splitTokens(s, "\n\r");
		
		float offxMax = 0;
		for(int i=0; i<parts.length; i++) {
			float offxTemp = textWidthParse(parts[i]);
			if(offxTemp > offxMax) {
				offxMax = offxTemp;
			}
		}
		return offxMax;
	}
	
	float textWidthParse(String s) {
		float offx = 0;
		for (int k=0; k<s.length (); k++){
			int c = s.charAt(k);
			String h = hershey_font[c - 32 ];

			int start_col = h.indexOf(" ");

			int h_left = hershey2coord(h.charAt(start_col+3));
			int h_right = hershey2coord(h.charAt(start_col+4));
			float h_width = h_right - h_left * hfactor;
			offx += h_width + 5 * hfactor;
		}
		return offx;
	}
	
	/**
	 * Process and return 2D-array (points in paths) of PVector's from provided text, x, y coordinates using built in Hershey Fonts. 
	 * 
	 * @param s
	 *            String - text to display
	 * @param x
	 *            float - horizontal position of text
	 * @param y
	 *            float - vertical position of text
	 *            
	 * @return 2D-Array of <PVector>
	 */
	public PVector[][] textPaths(String s, float x, float y) {
		String[] parts = splitTokens(s, "\n\r");
		
		switch(textAlignY) {
		case 101:
			y += hfactor * 12;
			break;
		
		case 102:
			y -= hfactor * 21 * parts.length;
			break;
		default:
			y -= hfactor * 21 * parts.length/2;
			break;
		}
		
		ArrayList<ArrayList<PVector>> cooords = new ArrayList(s.length());
		float yOffset = y;
		for(int i=0; i<parts.length; i++) {
			textPathsParse(parts[i], x, yOffset, cooords);
			yOffset += hfactor * hheight + hleading;
		}
		
		PVector[][] coordsArray = new PVector[cooords.size()][];
		for(int i=0; i < cooords.size(); i++) {
			coordsArray[i] = new PVector[cooords.get(i).size()];
			for(int j=0; j < cooords.get(i).size(); j++) {
				coordsArray[i][j] = cooords.get(i).get(j);
			}
		}

		return coordsArray;	
	}
	
	void textPathsParse(String s, float x, float y, ArrayList<ArrayList<PVector>> coords) {
		
		x += 5 * hfactor;
		
		switch(textAlignX) {
		case 3:
			x -= textWidth(s) / 2;
			break;
		case 39:
			x -= textWidth(s);
			break;
		}
		
		float offx = x;
		float offy = y;
		for (int k=0; k<s.length (); k++){
			int c = s.charAt(k);
			String h = hershey_font[c - 32 ];

			int start_col = h.indexOf(" ");

			int h_left = hershey2coord(h.charAt(start_col+3));
			int h_right = hershey2coord(h.charAt(start_col+4));
			float h_width = h_right - h_left * hfactor;

			String[] h_vertices = h.substring(start_col+5, h.length()).replaceAll(" R", " ").split(" ");

			for (int i=0; i<h_vertices.length; i++) {
				ArrayList<PVector> coord = new ArrayList(h_vertices[i].length());
				for (int j=2; j<h_vertices[i].length (); j+=2) {
					float hx0 = hershey2coord(h_vertices[i].charAt(j-2)) * hfactor;
					float hy0 = hershey2coord(h_vertices[i].charAt(j-1)) * hfactor;
					coord.add(new PVector(offx + hx0, offy+hy0));
					float hx1 = hershey2coord(h_vertices[i].charAt(j)) * hfactor;
					float hy1 = hershey2coord(h_vertices[i].charAt(j+1)) * hfactor;
					coord.add(new PVector(offx+hx1, offy+hy1));
				}
				coords.add(coord);
			}
			offx += h_width + 5 * hfactor;
		}
	}

	private void draw_character(int c) {
		String h = hershey_font[c - 32 ];

		int start_col = h.indexOf(" ");

		int h_left = hershey2coord(h.charAt(start_col+3));
		int h_right = hershey2coord(h.charAt(start_col+4));
		float h_width = h_right - h_left * hfactor;
		String[] h_vertices = h.substring(start_col+5, h.length()).replaceAll(" R", " ").split(" ");
		for (int i=0; i<h_vertices.length; i++) {
			beginShape();
			for (int j=2; j<h_vertices[i].length (); j+=2) {
				float hx0 = hershey2coord(h_vertices[i].charAt(j-2)) * hfactor;
				float hy0 = hershey2coord(h_vertices[i].charAt(j-1)) * hfactor;
				vertex(hx0, hy0);
				float hx1 = hershey2coord(h_vertices[i].charAt(j)) * hfactor;
				float hy1 = hershey2coord(h_vertices[i].charAt(j+1)) * hfactor;
				vertex(hx1, hy1);
			}
			endShape();
		}
		myParent.translate(h_width + 5 * hfactor, 0);
	}

	private int hershey2coord(char c) {
		return c - 'R';
	}

	private XYShape currentShape = null;

	public class XYShapeList extends ArrayList<XYShape> {
		
		/**
		 * Returns float of total distance of drawn shapes, XYShapeList.
		 * 
		 * @return float
		 * 
		 */
		public double getDistance() {
			double sum = 0.0f;
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

		public double getDistance() {
			double sum = 0.0f;
			for (int i = 0; i < size() - 1; i++) {
				sum += dist(get(i).x, get(i).y, get(i+1).x, get(i+1).y);//get(i).dist(get(i + 1));
			}
			return sum;
		}
	}
}
