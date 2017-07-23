/*
 * cc teddavis.org 2017
 * */


package xyscope;
import processing.core.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;

// minim
import ddf.minim.*; 
import ddf.minim.ugens.*;
import javax.sound.sampled.*;



/**
 * Render graphics on a vector display by converting them to audio (oscilloscope X-Y mode, laser).
 *
 */

public class XYscope {

	// myParent is a reference to the parent sketch
	PApplet myParent;

	ArrayList<PVector> pts = new ArrayList<PVector>();
	ArrayList<PVector> ptsNew = new ArrayList<PVector>();
	ArrayList<PVector> ptsOld = new ArrayList<PVector>();

	// minim
	Minim minim, minimZ; 

	/**
	 * Access to minim XY + Z out, for any custom manipulation. 
	 */
	public AudioOutput outXY, outZ; 
	
	/**
	 * Access to minim XY + Z oscillators, for any custom manipulation. 
	 */
	public Oscil waveX, waveY, waveZ; 

	Wavetable tableX, tableY, tableZ; 
	Pan panX = new Pan(-1);
	Pan panY = new Pan(1);
	
	Mixer.Info[] mixerInfo;

	float initAmp = 1.0f;
	PVector amp = new PVector(initAmp, initAmp, initAmp);
	float initFreq = 50f; // 43.065
	PVector freq = new PVector(initFreq,initFreq,initFreq);
	float audioWaveAmp = 512f;

	int waveSize = 1024;
	float[] shapeY = new float[waveSize]; 
	float[] shapeX = new float[waveSize]; 
	float[] shapeZ = new float[waveSize]; 
	float[] shapePreY = new float[waveSize];
	float[] shapePreX = new float[waveSize];
	float[] shapePreZ = new float[waveSize];

	int ellipseDetail = 30;

	boolean useEase = false;
	float easeVal = .1f;
	boolean useZ = true;
	boolean useSmooth = false;
	int smoothVal = 12;

	boolean zaxis = false;
	float zaxisMin = 1f;
	float zaxisMax = -1f;
	int zoffset = 1;

	/**
	 * Initialize library in setup(), use default system audio out setting.
	 * 
	 * @param	theParent	PApplet to apply to, typically 'this'
	 */
	//	 * @example basic_shapes

	public XYscope(PApplet theParent) {
		myParent = theParent;
		welcome();
		initMinim();
		setMixer();
	}

	/**
	 * Initialize library in setup(), custom soundcard by String for XY.
	 * 
	 * @param	theParent	PApplet to apply to, typically 'this'
	 * @param	xyMixer	Name of custom sound mixer to use for XY.
	 */

	public XYscope(PApplet theParent, String xyMixer) {
		myParent = theParent;
		welcome();
		getMixerInfo();
		initMinim();
		setMixer(xyMixer);
	}

	/**
	 * Initialize library in setup(), custom soundcard by value for XY.
	 * 
	 * @param	theParent	PApplet to apply to, typically 'this'
	 * @param	xyMixer	ID from getMixerInfo() of custom sound mixer to use for XY.
	 */
	public XYscope(PApplet theParent, int xyMixer) {
		myParent = theParent;
		welcome();
		getMixerInfo();
		initMinim();
		setMixer(xyMixer);
	}


	/**
	 * Initialize library in setup(), custom soundcard by String for XY, custom soundcard by String for Z.
	 * <p>
	 * Minim only allows STEREO output, so we need to use two audio output devices. If you have a multi-channel DAC, be sure to setup Aggregates for each STEREO pair.
	 * 
	 * @param theParent	PApplet to apply to, typically 'this'
	 * @param xyMixer	Name of custom sound mixer to use for XY.
	 * @param zMixer	Name of custom sound mixer to use for Z.
	 */
	public XYscope(PApplet theParent, String xyMixer, String zMixer) {
		myParent = theParent;
		welcome();
		getMixerInfo();
		initMinim();
		setMixer(xyMixer, zMixer);
	}

	/**
	 * Initialize library in setup(), custom soundcard by int for XY, custom soundcard by int for Z.
	 * <p>
	 * Minim only allows STEREO output, so we need to use two audio output devices. If you have a multi-channel DAC, be sure to setup Aggregates for each STEREO pair.
	 * 
	 * @param	theParent	PApplet to apply to, typically 'this'
	 * @param	xyMixer	ID from getMixerInfo() of custom sound mixer to use for XY.
	 * @param zMixer	ID from getMixerInfo() of custom sound mixer to use for Z.
	 */
	public XYscope(PApplet theParent, int xyMixer, int zMixer) {
		myParent = theParent;
		welcome();
		getMixerInfo();
		initMinim();
		setMixer(xyMixer, zMixer);
	}

	// spits out version info
	private void welcome() {
		System.out.println("##library.name## ##library.prettyVersion## by ##author##");
	}

	/**
	 * Lists all audio input/output options available
	 */
	public void getMixerInfo(){
		mixerInfo = AudioSystem.getMixerInfo();
		for (int i = 0; i < mixerInfo.length; i++) {
			myParent.println(i + " = " + mixerInfo[i].getName());
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
		audioWaveAmp = myParent.height/2;
	}

	private void setMixer(){		
		outXY = minim.getLineOut(Minim.STEREO);
		setWaveTable();
	}

	private void setMixer(int xyMixer){
		getMixerInfo();
		
		Mixer mixer = AudioSystem.getMixer(mixerInfo[xyMixer]);
		minim.setOutputMixer(mixer);
		outXY = minim.getLineOut();
		setWaveTable();
	}

	private void setMixer(String xyMixer){
		getMixerInfo();
		
		Mixer mixer = getMixerByName(xyMixer);
		minim.setOutputMixer(mixer);
		outXY = minim.getLineOut();
		setWaveTable();
	}

	private void setMixer(int xyMixer, int zMixer){
		getMixerInfo();
		
		Mixer mixer = AudioSystem.getMixer(mixerInfo[xyMixer]);
		minim.setOutputMixer(mixer);
		outXY = minim.getLineOut();

		Mixer mixerZ = AudioSystem.getMixer(mixerInfo[zMixer]);
		minimZ.setOutputMixer(mixerZ);
		outZ = minimZ.getLineOut();

		zaxis = true;

		setWaveTable();
	}

	private void setMixer(String xyMixer, String zMixer){
		getMixerInfo();

		Mixer mixer = getMixerByName(xyMixer);
		minim.setOutputMixer(mixer);
		outXY = minim.getLineOut();

		Mixer mixerZ = getMixerByName(zMixer);
		minimZ.setOutputMixer(mixerZ);
		outZ = minimZ.getLineOut();

		zaxis = true;

		setWaveTable();
	}

	private void setWaveTable(){
		tableX = Waves.randomNHarms(0); 
		waveX  = new Oscil(freq.x, amp.x, tableX); 
		tableX.setWaveform(shapeX); 
		waveX.patch(panX).patch(outXY); 

		tableY = Waves.randomNHarms(0); 
		waveY  = new Oscil(freq.y, amp.y, tableY); 
		tableY.setWaveform(shapeY); 
		waveY.patch(panY).patch(outXY);

		waveX.reset();
		waveY.reset();

		if(zaxis){
			tableZ = Waves.randomNHarms(0); 
			waveZ  = new Oscil(freq.z, amp.z, tableZ); 
			tableZ.setWaveform(shapeZ); 
			waveZ.patch(outZ); // need pan?? or gets full amp to both channels?
			waveZ.reset();
		}
	}

	/**
	 * Check if z-axis waveform is being automatically drawn from added shapes.
	 */
	public boolean zAuto(){
		return useZ;
	}

	/**
	 * Enabled by default for automatic generation of z-axis waveform based on added shapes.
	 * Disable if creating your own waveform (used for blanking, dotted line, etc.
	 * Note: Only works if using a second audio output channel
	 * @param	zAutoBool	true/false for generating z waveform
	 */
	public void zAuto(boolean zAutoBool){
		useZ = zAutoBool;
	}	

	/**
	 * Add a point to the XY waveform.
	 * @param x	normalized x coordinate 0 - 1
	 * @param y	normalized y coordinate 0 - 1
	 */
	public void addPoint(float x, float y){
		pts.add(new PVector(x, y, zaxisMax));
	}

	/**
	 * Add a point to the XY waveform. Use custom z-axis (blanking).
	 * @param x	normalized x coordinate 0 - 1
	 * @param y	normalized y coordinate 0 - 1
	 * @param z	normalized z value 0 - 1
	 */
	public void addPoint(float x, float y, float z){
		pts.add(new PVector(x, y, z));
	}

	/**
	 * Add a point to the XY waveform using a PVector.
	 * @param addPointVector	PVector with three values for x, y, z.
	 */
	public void addPoint(PVector addPointVector){
		pts.add(addPointVector);
	}

	/**
	 * Get ArrayList of all coordinates used for vector drawing as an ArrayList of PVector's.
	 * @return ArrayList<PVector>
	 */
	public ArrayList<PVector> wavePoints(){
		return pts;
	}

	/**
	 * Set new ArrayList of normalized coordinates used for vector drawing. Run before buildWaves().
	 * @param tempPts	Collection of normalized coordinates 
	 */
	public void wavePoints(ArrayList<PVector> tempPts){
		pts = new ArrayList<>(tempPts);;
	}

	/**
	 * Get min and max values for z-axis output as two value array.
	 * @return array containing [zaxisMin, zaxisMax]
	 */
	public float[] zRange(){
		float[] zRangeFloat = {zaxisMin, zaxisMax};
		return zRangeFloat;
	}

	/**
	 * Set min and max values for z-axis output. Necessary for any inverted z-axis devices.
	 * <p>
	 * default is zMin: 1, zMax: -1
	 * @param zMin	float between -1 to 1
	 * @param zMax	float between -1 to 1
	 */
	public void zRange(float zMin, float zMax){
		zaxisMin = zMin;
		zaxisMax = zMax;
	}

	/**
	 * Get current amplitude setting of XY oscillators.
	 * @return float 
	 */
	public PVector amp(){
		return amp;
	}

	/**
	 * Set new amplitude for both XY oscillators as float.
	 * @param newAmp	value between 0.0 - 1.0
	 */
	public void amp(float newAmp){
		amp.x = myParent.constrain(newAmp, 0f, 1f);
		amp.y = myParent.constrain(newAmp, 0f, 1f);
		waveX.setAmplitude(amp.x);
		waveY.setAmplitude(amp.y);
	}

	/**
	 * Set new amplitude for only X oscillator as float.
	 * @param newAmp	value between 0.0 - 1.0
	 */
	public void ampX(float newAmp){
		amp.x = myParent.constrain(newAmp, 0f, 1f);
		waveX.setAmplitude(amp.x);
	}

	/**
	 * Set new amplitude for only Y oscillator as float.
	 * @param newAmp	value between 0.0 - 1.0
	 */
	public void ampY(float newAmp){
		amp.y = myParent.constrain(newAmp, 0f, 1f);
		waveY.setAmplitude(amp.y);
	}

	/**
	 * Set new amplitude for only Z oscillator as float.
	 * @param newAmp	value between 0.0 - 1.0
	 */
	public void ampZ(float newAmp){
		amp.z = myParent.constrain(newAmp, 0f, 1f);
		if(zaxis)
			waveZ.setAmplitude(amp.z);
	}

	/**
	 * Set new amplitude for each XYZ oscillator separately using a PVector for the values.
	 * @param newAmp	PVector of values between 0.0 - 1.0
	 */
	public void amp(PVector newAmp){
		float tempX = myParent.constrain(newAmp.x, 0f, 1f);
		float tempY = myParent.constrain(newAmp.y, 0f, 1f);
		float tempZ = myParent.constrain(newAmp.z, 0f, 1f);
		waveX.setAmplitude(tempX);
		waveY.setAmplitude(tempY);
		waveZ.setAmplitude(tempZ);
	}


	/**
	 * Get current frequency for X, Y, Z oscillators as a PVector.
	 * @return PVector
	 */
	public PVector freq(){
		return freq;
	}

	/**
	 * Set new frequency for all XYZ oscillators together as float.
	 * @param newFreq	float
	 */
	public void freq(float newFreq){
		freq = new PVector(newFreq, newFreq, newFreq);
		waveX.setFrequency(freq.x);
		waveY.setFrequency(freq.y);
		if(zaxis)
			waveZ.setFrequency(freq.z);
	}

	/**
	 * Set new frequency for only X oscillator as float.
	 * @param newFreq	new frequency value
	 */
	public void freqX(float newFreq){
		freq.x = newFreq;
		waveX.setFrequency(freq.x);
	}

	/**
	 * Set new frequency for only Y oscillator as float.
	 * @param newFreq	new frequency value
	 */
	public void freqY(float newFreq){
		freq.y = newFreq;
		waveY.setFrequency(freq.y);
	}

	/**
	 * Set new frequency for only Z oscillator as float.
	 * @param newFreq	new frequency value
	 */
	public void freqZ(float newFreq){
		freq.z = newFreq;
		if(zaxis)
			waveZ.setFrequency(freq.z);
	}

	/**
	 * Set new frequency for each XYZ oscillator separately using a PVector for the values.
	 * @param newFreq	PVector
	 */
	public void freq(PVector newFreq){
		waveX.setFrequency(newFreq.x);
		waveY.setFrequency(newFreq.y);
		if(zaxis)
			waveZ.setFrequency(newFreq.z);
	}

	/**
	 * Enable/Disable easing transitions from one set of buildWaves() to the next. Default is false.
	 * @param easeBool	true/false
	 */
	public void ease(boolean easeBool){
		useEase = easeBool;
	}

	/**
	 * Check if easing between each frame of buildWaves() is enabled.
	 * @return boolean
	 */
	public boolean ease(){
		return useEase;
	}

	/**
	 * Returns current easeAmount, 0.0 - 1.0.
	 * @return float
	 */
	public float easeAmount(){
		return easeVal;
	}

	/**
	 * Set new easing value for speed between buildWave() transitions.
	 * @param newEaseValue	float between 0.0 - 1.0
	 */
	public void easeAmount(float newEaseValue){
		easeVal = newEaseValue;
	}
	
	/**
	 * Get size of wavetables. By default, it's the same as the outXY.bufferSize()
	 * @param newSize	int 
	 */
	public int waveSize(){
		return waveSize;
	}
	
	/**
	 * Set custom size for wavetables. By default, it's the same as the outXY.bufferSize()
	 * @param newSize	int 
	 */
	public void waveSize(int newSize){
		waveSize = newSize;
		shapeY = new float[waveSize]; 
		shapeX = new float[waveSize]; 
		shapeZ = new float[waveSize]; 
		shapePreY = new float[waveSize];
		shapePreX = new float[waveSize];
		shapePreZ = new float[waveSize];
	}

	/**
	 * Clears the waveforms from previous buildWaves(). Useful to call at top of draw(), similar to using background() to clear the slate before building the waveforms at the bottom of your draw with buildWaves().
	 */
	public void clearWaves() {
		pts.clear();
		for (int i=0; i < shapeX.length; i++) {
			shapePreX[i] = 0;
			shapePreY[i] = 0;
			if(zaxis && useZ)
				shapePreZ[i] = zaxisMin;
		}
	}

	/**
	 * Generate the XY(Z) oscillator waveforms from all added points/shapes for sending audio to vector display. Call this after drawing any primitive shapes.
	 * @see points()
	 */
	public void buildWaves() {
		for (int i=0; i < shapeX.length; i++) { 
			if (pts.size() > 0) {
				int ptsSel = (int)Math.floor(myParent.map(i, 0f, shapeX.length, 0, pts.size()));

				shapePreX[i] = myParent.map(pts.get(ptsSel).x, 0f, 1f, -1f, 1f);
				shapePreY[i] = myParent.map(pts.get(ptsSel).y, 0f, 1f, 1f, -1f);

				if(zaxis && useZ){
					shapePreZ[i] = zaxisMin;
					int ptsZ = (int)Math.floor(myParent.map(i, 0f, shapeZ.length, 0, pts.size()));
					shapePreZ[i] = myParent.map(pts.get(ptsZ).z, 0f, 1f, zaxisMin, zaxisMax);
				}
			}
		}

		if(useEase){
			easeWaves();
		}else{
			for (int i=0; i < shapePreX.length; i++) { 
				shapeX[i] = shapePreX[i];
				shapeY[i] = shapePreY[i];
				if(zaxis && useZ)
					shapeZ[i] = shapePreZ[i];
			}
		}

		if(useSmooth){
			tableX.smooth(smoothVal);
			tableY.smooth(smoothVal);
		}
	}

	/**
	 * Build custom X oscillator waveform. Use waveSize() to ensure you send the right number of values.
	 * @param newWave	Array of normalized floats between 0.0 - 1.0
	 * @see waveSize();
	 */
	public void buildX(float[] newWave){
		for(int i=0; i< newWave.length; i++){
			int sel = (int)Math.floor(myParent.map(i, 0f, newWave.length, 0f, shapePreX.length));
			shapePreX[i] = myParent.map(newWave[sel], 0f, 1f, -1f, 1f);
		}

		if(useEase){
			easeWaves(shapePreX, shapeX);
		}else{
			for (int i=0; i < shapePreX.length; i++) { 
				shapeX[i] = shapePreX[i];
			}
		}
	}
	
	/**
	 * Build custom Y oscillator waveform. Use waveSize() to ensure you send the right number of values.
	 * @param newWave	Array of normalized floats between 0.0 - 1.0
	 * @see waveSize();
	 */
	public void buildY(float[] newWave){
		for(int i=0; i< newWave.length; i++){
			int sel = (int)Math.floor(myParent.map(i, 0f, newWave.length, 0f, shapePreY.length));
			shapePreY[i] = myParent.map(newWave[sel], 0f, 1f, 1f, -1f);
		}

		if(useEase){
			easeWaves(shapePreY, shapeY);
		}else{
			for (int i=0; i < shapePreY.length; i++) { 
				shapeY[i] = shapePreY[i];
			}
		}
	}
	
	/**
	 * Build custom Z oscillator waveform. Use waveSize() to ensure you send the right number of values.
	 * @param newWave	Array of normalized floats between 0.0 - 1.0
	 * @see waveSize();
	 */
	public void buildZ(float[] newWave){
		for(int i=0; i< newWave.length; i++){
			int sel = (int)Math.floor(myParent.map(i, 0f, newWave.length, 0f, shapePreZ.length));
			shapePreZ[i] = myParent.map(newWave[sel], 0f, 1f, zaxisMin, zaxisMax);
		}

		if(useEase){
			easeWaves(shapePreZ, shapeZ);
		}else{
			for (int i=0; i < shapePreZ.length; i++) { 
				shapeZ[i] = shapePreZ[i];
			}
		}
	}
	
	private void easeWaves(float[] sShape, float[] tShape) {
		for (int i=0; i < sShape.length; i++) {
			float targetX = sShape[i];
			float dx = targetX - tShape[i];
			tShape[i] += dx * easeVal;
		}
	}

	private void easeWaves() {
		easeWaves(shapePreX, shapeX);
		easeWaves(shapePreY, shapeY);
		
		if(zaxis && useZ){
			easeWaves(shapePreZ, shapeZ);

		}
	}


	/**
	 * Check if waveform smoothing is enabled/disabled.
	 * @param easeVal	true/false
	 */
	public boolean smoothWaves(){
		return useSmooth;
	}

	/**
	 * Enable/disable Smooth waveforms to reduce visibility of points in drawing. Default is false.
	 * @param smoothWavesBool	true/false
	 */
	public void smoothWaves(boolean smoothWavesBool){
		useSmooth = smoothWavesBool;
	}

	/**
	 * Get number of steps for smoothing waveforms.
	 * @see <a href="http://code.compartmental.net/minim/wavetable_method_smooth.html">Minim -> Wavetable -> smooth()</a>
	 */
	public int smoothWavesAmount(){
		return smoothVal;
	}

	/**
	 * Set number of steps for smoothing waveforms. Default is 12.
	 * @param swAmount	new int value for smoothening waveform
	 * @see <a href="http://code.compartmental.net/minim/wavetable_method_smooth.html">Minim -> Wavetable -> smooth()</a>
	 */
	public void smoothWavesAmount(int swAmount){
		smoothVal = swAmount;
	}



	/**
	 * Draw all information
	 * <ul>
	 * <li> drawPath()
	 * <li> drawWaveform()
	 * <li> drawWave()
	 * <li> drawXY()
	 * <li> drawPoints()
	 * </ul>
	 */
	public void drawAll(){
		drawPath();
		drawWaveform();
		drawWave();
		drawXY();
		drawPoints();
	}

	/**
	 * Draw path of points remapped from normalized values to width + height of sketch.
	 */
	public void drawPath() {
		myParent.pushStyle();
		myParent.noFill();
		myParent.stroke(255);
		myParent.pushMatrix();
		myParent.beginShape();
		for (int i=0; i< pts.size(); i++) {
			float x = myParent.map(pts.get(i).x, 0f, 1f, 0f, myParent.width);
			float y = myParent.map(pts.get(i).y, 0f, 1f, 0f, myParent.height);
			myParent.vertex(x, y);
		}
		myParent.endShape(myParent.OPEN);
		myParent.popMatrix();
		myParent.popStyle();
	}

	/**
	 * Draw points (as 3px ellipses) remapped from normalized values to width + height of sketch.
	 */
	public void drawPoints() {
		myParent.pushStyle();
		myParent.fill(0, 255, 0);
		myParent.noStroke();
		myParent.pushMatrix();
		for (int i=0; i< pts.size(); i++) {
			float x = myParent.map(pts.get(i).x, 0f, 1f, 0f, myParent.width);
			float y = myParent.map(pts.get(i).y, 0f, 1f, 0f, myParent.height);
			myParent.ellipse(x, y, 3, 3);
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
		myParent.translate(myParent.width/2, myParent.height/2);
		myParent.beginShape();
		for (int i = 0; i < outXY.bufferSize()-1; i++) {
			float lAudio = outXY.left.get(i)*audioWaveAmp;
			float rAudio = outXY.right.get(i)*audioWaveAmp;
			myParent.curveVertex(lAudio, rAudio*-1f);
		}
		myParent.endShape();
		myParent.popMatrix();
		myParent.popStyle();
	}

	/**
	 * Draw waveform of all XYZ oscillators.
	 * <ul>
	 * <li> Red: X
	 * <li> Blue: Y
	 * <li> Green: Z
	 */
	public void drawWaveform() {
		myParent.pushStyle();
		myParent.stroke(255, 50, 50 ); 
		myParent.noFill();
		myParent.pushMatrix();
		myParent.beginShape();

		// X -> L
		for (int i = 0; i < myParent.width; i++) { 
			myParent.vertex(i, (float)myParent.height*.25f - ((float)myParent.height*.125f) * tableX.value((float)i / (float)myParent.width));
		} 
		myParent.endShape();

		// Y -> R
		myParent.stroke( 50, 50, 255); 
		myParent.beginShape();
		for (int i = 0; i < myParent.width; i++) { 
			myParent.vertex(i, (float)myParent.height*.75f - ((float)myParent.height*0.125f) * tableY.value((float)i / (float)myParent.width));
		} 
		myParent.endShape();

		// Z
		if(zaxis){
			myParent.stroke( 50, 255, 50); 
			myParent.beginShape();
			for (int i = 0; i < myParent.width; i++) { 
				myParent.vertex(i, (float)myParent.height*.5f - ((float)myParent.height*0.125f) * tableZ.value((float)i / (float)myParent.width));
			} 
			myParent.endShape();
		}
		myParent.popMatrix();
		myParent.popStyle();
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
		for (int i = 0; i < outXY.bufferSize()-1; i++) {
			float xAudio = myParent.map(i, 0, outXY.bufferSize(), 0, myParent.width);
			float lAudio = outXY.left.get(i);
			//curveVertex(lAudio, rAudio*-1);
			myParent.vertex(xAudio, myParent.height*.25f - (myParent.height*.25f) * lAudio);
		}
		myParent.endShape();

		myParent.beginShape();
		for (int i = 0; i < outXY.bufferSize()-1; i++) {
			float xAudio = myParent.map(i, 0, outXY.bufferSize(), 0, myParent.width);
			float rAudio = outXY.right.get(i);
			//curveVertex(lAudio, rAudio*-1);
			myParent.vertex(xAudio, myParent.height*.75f + (myParent.height*.25f) * rAudio);
		}
		myParent.endShape();

		if(zaxis){
			myParent.beginShape();
			for (int i = 0; i < outZ.bufferSize()-1; i++) {
				float xAudio = myParent.map(i, 0, outZ.bufferSize(), 0, myParent.width);
				float rAudio = outZ.right.get(i);
				//curveVertex(lAudio, rAudio*-1);
				myParent.vertex(xAudio, myParent.height*.5f - (myParent.height*.25f) * rAudio);
			}
			myParent.endShape();

		}
		myParent.popMatrix();
		myParent.popStyle();
	}


	/* semi works, but slows way down */
	private void sortPoints() {
		ptsNew.clear();
		//ptsOld.clear();
		for (int i=0; i<pts.size(); i++) {
			ptsOld.add(new PVector(pts.get(i).x,pts.get(i).y));    
		}

		int dsel = 0;
		for (int i=0; i<ptsOld.size(); i++) {
			PVector p = ptsOld.get(dsel);
			if(i > 0){
				ptsOld.remove(dsel);
			}else{
				p = new PVector(.5f, .5f);
			}

			float d = 1;
			for (int j=0; j<ptsOld.size(); j++) {
				PVector pc = ptsOld.get(j);
				float dc = myParent.dist(p.x, p.y, pc.x, pc.y);
				if (dc < d && dc != 0) {
					d = dc;
					dsel = j;
				}
			}
			if (dsel < ptsOld.size()) {
				ptsNew.add(new PVector(ptsOld.get(dsel).x, ptsOld.get(dsel).y));
			}
		}
	}

	int rectM;
	boolean vertexInit = false;
	boolean vertexEnd = false;
	ArrayList<PVector> v1;


	/**
	 * Get detail (number of points) for drawing an ellipse.
	 * @return int
	 */
	public int ellipseDetail(){
		return ellipseDetail;
	}

	/**
	 * Set detail (number of points) for drawing an ellipse.
	 * @param newED int
	 */
	public void ellipseDetail(int newED){
		ellipseDetail = newED;
	}

	/**
	 * Set rectMode (similar to Processing function).
	 * @param rectModeVal CORNER or CENTER
	 * @see <a href="https://processing.org/reference/rectMode_.html">Processing Reference -> rectMode()</a>
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
	 * @see <a href="https://processing.org/reference/rect_.html">Processing Reference -> rect()</a>
	 */
	public void rect(float x1, float y1, float w1, float h1) {
		if (rectM == 3) {
			x1 -= w1/2;
			y1 -= h1/2;
		}
		vertexRect(x1, y1, w1, h1);
	}

	private void vertexRect(float x1, float y1, float w1, float h1) {
		beginShape();
		vertex(x1, y1);
		vertex(x1+w1, y1);
		vertex(x1+w1, y1+h1);
		vertex(x1, y1+h1);
		vertex(x1, y1);
		if(zaxis && useZ)
			vertex(x1, y1);
		endShape();
	}

	/**
	 * Draw ellipse, expects ellipse(x, y, w, h).
	 * @see <a href="https://processing.org/reference/ellipse_.html">Processing Reference -> ellipse()</a>
	 */
	public void ellipse(float x1, float y1, float w1, float h1) {
		vertexEllipse(x1, y1, w1, h1);
	}

	// vertexEllipse!
	// based on http://stackoverflow.com/questions/5886628/effecient-way-to-draw-ellipse-with-opengl-or-d3d
	private void vertexEllipse(float cx, float cy, float rx, float ry){ 
		float theta = 2f * myParent.PI / (float)ellipseDetail; 
		float c = myParent.cos(theta);//precalculate the sine and cosine
		float s = myParent.sin(theta);
		float t;

		float x = 1f;//we start at angle = 0 
		float y = 0f; 

		beginShape(); 
		for (int ii = 0; ii < ellipseDetail+1; ii++) 
		{ 
			//apply radius and offset
			vertex(x * rx + cx, y * ry + cy);//output vertex 
			//apply the rotation matrix
			t = x;
			x = c * x - s * y;
			y = s * t + c * y;
			

		} 
		endShape();
	}

	/**
	 * Draw point, expects point(x, y).
	 * @see <a href="https://processing.org/reference/point_.html">Processing Reference -> point()</a>
	 */
	public void point(float x1, float y1) {
		beginShape();
		vertex(x1, y1);
		if(zaxis && useZ)
			vertex(x1, y1);
		endShape();
	}

	/**
	 * Draw point, expects point(x, y, z).
	 * @see <a href="https://processing.org/reference/point_.html">Processing Reference -> point()</a>
	 */
	public void point(float x1, float y1, float z1) {
		beginShape();
		vertex(x1, y1, z1);
		if(zaxis && useZ)
			vertex(x1, y1, z1);
		endShape();
	}


	/**
	 * Draw line, expects line(x1, y1, x2, y2).
	 * @see <a href="https://processing.org/reference/line_.html">Processing Reference -> line()</a>
	 */
	public void line(float x1, float y1, float x2, float y2) {
		beginShape();
		vertex(x1, y1);
		vertex(x2, y2);
		if(zaxis && useZ)
			vertex(x1, y1);
		endShape();
	}

	/**
	 * Begin multi-vertex shape.
	 * @see <a href="https://processing.org/reference/beginShape_.html">Processing Reference -> beginShape()</a>
	 */
	public void beginShape() {
		vertexInit = true;
		v1 = new ArrayList<PVector>();
	}

	/**
	 * Currently sent as normal vertex (to be fixed). Simply here for code -> vectorcode compatibility.
	 * @see <a href="https://processing.org/reference/curveVertex_.html">Processing Reference -> curveVertex()</a>
	 */
	public void curveVertex(float x1, float y1) {
		vertex(x1, y1);
	}

	/**
	 * Currently sent as normal vertex (to be fixed). Simply here for code -> vectorcode compatibility.
	 * @see <a href="https://processing.org/reference/curveVertex_.html">Processing Reference -> curveVertex()</a>
	 */
	public void curveVertex(float x1, float y1, float z1) {
		vertex(x1, y1, z1);
	}

	/**
	 * Add vertex to complex shape. Expects vertex(x, y).
	 * @see <a href="https://processing.org/reference/vertex_.html">Processing Reference -> vertex()</a>
	 */
	public void vertex(float x1, float y1) {
		float x1out = myParent.norm(myParent.screenX(x1, y1), 0f, myParent.width);
		float y1out = myParent.norm(myParent.screenY(x1, y1), 0f, myParent.height); 
		v1.add(new PVector(x1out, y1out));
	}

	/**
	 * Add vertex to complex shape. Expects vertex(x, y, z).
	 * @see <a href="https://processing.org/reference/vertex_.html">Processing Reference -> vertex()</a>
	 */
	public void vertex(float x1, float y1, float z1) {
		float x1out = myParent.norm(myParent.screenX(x1, y1, z1), 0f, myParent.width);
		float y1out = myParent.norm(myParent.screenY(x1, y1, z1), 0f, myParent.height); 
		v1.add(new PVector(x1out, y1out));
	}

	/**
	 * End complex shape.
	 * @see <a href="https://processing.org/reference/endShape_.html">Processing Reference -> endShape()</a>
	 */
	public void endShape() {
		for (int i=0; i<v1.size(); i++) {
			if(i==0){
				addPoint(v1.get(i).x, v1.get(i).y, 0);
			}else{
				addPoint(v1.get(i).x, v1.get(i).y, 1);
			}	
		}
		vertexInit = false;
	}
}

