
/*************************************************************************
 * Orrery
 *  Implements the core of Kepler's Orrery.
 *
 *  Portions of this code were derived from code supplied with
 *        Introduction to Programming in Java:  An Interdisciplinary Approach,
 *        Robert Sedgewick and Kevin Wayne, Addison-Wesley, 2007. 
 *        ISBN 0-321-49805-4
 *        http://www.cs.princeton.edu/IntroProgramming
 *
 *    Specifically, in this class:
 *        Orrery.increaseTime() and Orrery.draw(), from Universe.java
 *    Additionally:
 *        Body.forceTo(), from Body.java
 *        util.StdDraw.java, from StdDraw.java
 *        util.TokenReader, from StdIn.java
 *        parts of Vect, from Vector.java
 *
 *************************************************************************/

/***************************************************
 * Copyright 2007 by Simran Gleason,               *
 *                   Robert Sedgewick, Kevin Wayne *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;

import util.StdDraw;
import util.TokenReader;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Orrery implements MouseListener, MouseMotionListener {
    public static final int PLAYING_MODE            = 0;
    public static final int INITIAL_CONDITIONS_MODE = 1;
    private int mode = PLAYING_MODE;
    private WorldBuilder worldBuilder = null;
    private Kepler kepler = null;

    ///////////////////////
    // World metadata   ///
    ///////////////////////

    // TODO: general mechanism for the metadata, and a way to tell if the
    //       metadata was changed by the world file or simply defaulted.
    
    public  double radius;                    // radius of universe
    public  double bodyBaseRadius  = 512;     // base radius of bodies (i.e. min)
    public  int    maxBodies       = 25;      // max number of bodies
    public  double maxMass         = 0.0;
    public  double minMass         = -1.;
    private String title           = null;
    private int    nbodies;
    private int    nbodies_initial = 0;
    public  int    maxRocks        = 10;      // max number of rocks
    private int    nrocks;
    private int    nrocks_initial  = 0;
    private double friction        = .99;
    private double minDistance;
    private List   backgrounds;

    private double gravFactorBody  = 1.0;
    private double repelFactorBody = 1.0;
    private double gravFactorRock  = 1.0;
    private double repelFactorRock = 1.0;

    private int    gridCircles     = 0;
    private int    gridRadii       = 0;

    private boolean  winkingOut = false;
    private String[][] doomsayersLaments = null;
    private String[]   doomsayersLament;

    //
    // Beat locking and quantization
    //
    public static final long BEAT_TIME_NOW = -1;
    private boolean beatLocked = false;
    private int bpm = 120;
    private int quantize = 8;
    private int signatureBeatNote = 4;
    private int signatureBeatsPerMeasure = 4;
    private int measureQuants; // derived
    private int halfMeasureQuants; // derived
    private int beatQuants; // derived;
    private long quantMs; // derived
    private long beatMs; // derived
    private long measureMs; // derived
    private long t0_songStart;
    private float measureBump = 1.4f;
    private float halfMeasureBump = 1.2f;
    private float beatBump = 1.1f;
    

    //////////////////////////////////////////////////////////////////
    ///  Arrays that hold the bodies and rocks.                    ///
    ///   They are split into the initialBodies and initialRocks   ///
    ///   arrays and the ones used for running the simulation.     ///
    ///   The initial arrays contain the values that were read in, ///
    ///   and that are changed by the WorldBuilder.                ///
    //////////////////////////////////////////////////////////////////

    private Body[] initialBodies;   // array of bodies that holds the initial set
                                    // of bodies, as they are read in, and before any
                                    // changes are made. This is what the world builder.
                                    // operates on. 
    private Body[] bodies;          // array of bodies that's used for the simulation.
    private Rock[] initialRocks;    // array of rocks that holds the initial set
                                    // of rocks, as they are read in, and before any
                                    // changes are made. This is what the world builder.
    private Rock[] rocks;           // array of rocks that's used for the simulation.

    private HashMap  scales;
    private HashMap  melodies;
    private HashMap  sequences;
    private HashMap  infos;
    private String[] infosArray;

    Vect[] forces;

    private int cycleTime       =  60; // ms;
    public  int creationDelay   =  200; // ms;
    private int playTimeLo      = 0;
    private int playTimeHi      = 0;
    private int playCycles      = -1;
    private int playCycles_keep = 0;
    public  int numCycles       = 0;

    public  int     debugLevel          = 0;
    private boolean paused              = false;
    private int     displayInfo         = 0;
    private float   initialInfoDisplaySeconds = 0.f;
    private boolean showHelpInfo        = false;
    private List    helpInfo            = null;
    private boolean showBodyForces      = false;
    private boolean showBodyForces_keep = false;
    private boolean showForceField      = false;
    private boolean showForceField_keep = false;
    private int     forceFieldGridVectors = 30;
    private boolean showVelocities      = false;
    private boolean showVelocities_keep = false;
    public  double  velocityFactor      = 3.141592;
    public  double  ctlVelocityFactor   = 10.;

    private MidiStuff midiStuff          = null;
    public  int       defaultMidiVelocity_min  = 25;
    public  int       defaultMidiVelocity_max  = 100;
    public  int       numChannels      = 16;
    public  int[]     midiVelocity_min = new int[numChannels];
    public  int[]     midiVelocity_max = new int[numChannels];
    public  HashMap   channelMap;
    public  ChannelQueue[] channelQueues;

    public Color bgColor, circleColor, circleBorderColor, gridColor, gridColor2;
    public Color dbColor, titleColor, infoColor, countdownColor, forceColor;
    public Color squeegeeTabBG, squeegeeTabFG;
    public Color controlPanelTitle;
    public Color controlPanelBG, controlPanelBG2, controlPanelBG3, controlPanelFG;
    public Color buildBG, playBG, physicsBG, musicBG;
    public Color controlPanelBorder, controlPanelSubBorder, controlPanelBorderHighlight;
    public Color controlPanelValueFG, controlPanelValueBG, controlPanelSelectedBG;
    public Color velocityColor, highlightVelocityColor;
    protected TokenReader reader;
    public StdDraw drawer;

    protected double  dt       = 75.;
    protected double  local_dt = -1.;   // < 0 means not set. 
    protected int     trails   = 0; // approx # of cycles to run trails
    protected int     trails_keep = 0; // approx # of cycles to run trails
    protected boolean muted    = false;

    protected Component repaintComponent;
    private   String worldFileName;

    private ArrayList noteListeners = null;

    // read orrery from standard input
    public Orrery(TokenReader reader, StdDraw drawer) {
        init();
        this.reader = reader;
        this.drawer = drawer;
        Body.drawer = drawer;
        Body.orrery = this;

        drawer.addMouseListener(this);
        drawer.addMouseMotionListener(this);
    }

    public Orrery() {}

    private void init() {
        initColors();
        setupMidiStuff();
        setupNumberFormats();
        setupDoomsayersLaments();
        scales = new HashMap();
        clearMelodies();
        clearInfos();
        channelQueues = new ChannelQueue[numChannels];
        channelMap = new HashMap();
        for(int i=0; i < channelQueues.length; i++) {
            channelQueues[i] = new ChannelQueue(3); // get capacity from reader?
        }
        setForceFieldGridVectors(forceFieldGridVectors);
    }

    public void initColors() {
        bgColor                  = new Color(20, 18, 22);// Color.getHSBColor(.8f, .2f, .1f);
        circleBorderColor        = Color.getHSBColor(.7f, .35f, .2f);
        circleColor              = Color.getHSBColor(.01234f, .2f, .08f);
        gridColor                = Color.getHSBColor(.74f, 0.4f, .5f);
        gridColor2               = Color.getHSBColor(.95f, 0.5f, .3f);
        titleColor               = new Color(72, 72, 159); // Color.getHSBColor(.74f, 0.4f, .6f);
        infoColor                = new Color(72, 72, 99); 
        countdownColor           = new Color(255, 200, 30);
        forceColor               = Color.getHSBColor(.74f, 0.5f, .3f);
        velocityColor            = new Color(50, 50, 250);
        highlightVelocityColor   = new Color(250, 50, 50);
        dbColor                  = Color.getHSBColor(0.f, 0.8f, 1.0f);
        controlPanelTitle        = new Color(102, 0, 0);
        controlPanelBG           = bgColor;
        controlPanelBG2          = Color.getHSBColor(.8f, .3f, .1f);
        controlPanelBG3          = new Color(26, 20, 19); // Color.getHSBColor(.01234f, .25f, .10f); //new Color(32, 33, 28);
        playBG                   = new Color(15, 15, 35);
        physicsBG                = playBG;
        buildBG                  = new Color(15, 25, 12);
        musicBG                  = buildBG;
        controlPanelFG           = new Color(72, 72, 109);
        controlPanelBorder       = new Color(51, 51, 102);
        controlPanelSubBorder    = new Color(35, 35,  72);
        controlPanelBorderHighlight = new Color(71, 71, 112);
        squeegeeTabFG            = bgColor;
        squeegeeTabBG            = controlPanelBorder;

        //controlPanelValueFG = Color.getHSBColor(0.f, 1.f, .5f);
        controlPanelValueFG = new Color(155, 60, 65);
        controlPanelValueBG = Color.getHSBColor(.8f, .27f, .14f);
        controlPanelSelectedBG = Color.getHSBColor(.8f, .27f, .25f);
        //initColors_tshirt();
    }

    public void initColors_tshirt() {
        bgColor                  = new Color(220, 220, 220); //Color.WHITE; // new Color(20, 18, 22);// Color.getHSBColor(.8f, .2f, .1f);
        circleBorderColor        = Color.getHSBColor(.7f, .35f, .2f);
        circleColor              = new Color(200, 200, 190); // Color.getHSBColor(.01234f, .2f, .08f);
        gridColor                = Color.getHSBColor(.74f, 0.4f, .5f);
        gridColor2               = Color.getHSBColor(.95f, 0.5f, .3f);
        titleColor               = new Color(72, 72, 159); // Color.getHSBColor(.74f, 0.4f, .6f);
        infoColor                = new Color(72, 72, 99); 
        countdownColor           = new Color(255, 200, 30);
        forceColor               = Color.getHSBColor(.74f, 0.5f, .3f);
        velocityColor            = new Color(50, 50, 250);
        highlightVelocityColor   = new Color(250, 50, 50);
        dbColor                  = Color.getHSBColor(0.f, 0.8f, 1.0f);
        controlPanelTitle        = new Color(102, 0, 0);
        controlPanelBG           = bgColor;
        controlPanelBG2          = circleColor; // Color.getHSBColor(.8f, .3f, .1f);
        controlPanelBG3          = circleColor; // new Color(26, 20, 19); // Color.getHSBColor(.01234f, .25f, .10f); //new Color(32, 33, 28);
        playBG                   = circleColor; // new Color(15, 15, 35);
        physicsBG                = playBG;
        buildBG                  = circleColor; // new Color(15, 25, 12);
        musicBG                  = buildBG;
        controlPanelFG           = new Color(72, 72, 109);
        controlPanelBorder       = new Color(51, 51, 102);
        controlPanelSubBorder    = new Color(35, 35,  72);
        controlPanelBorderHighlight = new Color(71, 71, 112);
        squeegeeTabFG            = bgColor;
        squeegeeTabBG            = controlPanelBorder;

        //controlPanelValueFG = Color.getHSBColor(0.f, 1.f, .5f);
        controlPanelValueFG = new Color(155, 60, 65);
        controlPanelValueBG = circleColor; // Color.getHSBColor(.8f, .27f, .14f);
        controlPanelSelectedBG = circleColor; // Color.getHSBColor(.8f, .27f, .25f);
        initColors_bizcard();
    }

    public void initColors_bizcard() {
        Color bizBg = new Color(20, 18, 22);
        Color bizCircle = new Color(55, 45, 62);
        Color bizPanel = new Color(250, 240, 255);
        
        bgColor                  = bizBg;  //Color.WHITE; // new Color(20, 18, 22);// Color.getHSBColor(.8f, .2f, .1f);
        circleBorderColor        = Color.getHSBColor(.7f, .35f, .2f);
        circleColor              = bizCircle; // Color.getHSBColor(.01234f, .2f, .08f);
        gridColor                = Color.getHSBColor(.74f, 0.4f, .5f);
        gridColor2               = Color.getHSBColor(.95f, 0.5f, .3f);
        titleColor               = new Color(72, 72, 159); // Color.getHSBColor(.74f, 0.4f, .6f);
        infoColor                = new Color(72, 72, 99); 
        countdownColor           = new Color(255, 200, 30);
        forceColor               = Color.getHSBColor(.74f, 0.5f, .3f);
        velocityColor            = new Color(50, 50, 250);
        highlightVelocityColor   = new Color(250, 50, 50);
        dbColor                  = Color.getHSBColor(0.f, 0.8f, 1.0f);
        controlPanelTitle        = new Color(102, 0, 0);
        controlPanelBG           = bgColor;
        controlPanelBG2          = bizPanel; // Color.getHSBColor(.8f, .3f, .1f);
        controlPanelBG3          = bizPanel; // new Color(26, 20, 19); // Color.getHSBColor(.01234f, .25f, .10f); //new Color(32, 33, 28);
        playBG                   = bizPanel; // new Color(15, 15, 35);
        physicsBG                = playBG;
        buildBG                  = bizPanel; // new Color(15, 25, 12);
        musicBG                  = buildBG;
        controlPanelFG           = new Color(72, 72, 109);
        controlPanelBorder       = new Color(51, 51, 102);
        controlPanelSubBorder    = new Color(35, 35,  72);
        controlPanelBorderHighlight = new Color(71, 71, 112);
        squeegeeTabFG            = bgColor;
        squeegeeTabBG            = controlPanelBorder;

        //controlPanelValueFG = Color.getHSBColor(0.f, 1.f, .5f);
        controlPanelValueFG = new Color(155, 60, 65);
        controlPanelValueBG = bizPanel; // Color.getHSBColor(.8f, .27f, .14f);
        controlPanelSelectedBG = bizPanel; // Color.getHSBColor(.8f, .27f, .25f);
    }

    public boolean isInitialConditionsMode() {
        return mode == INITIAL_CONDITIONS_MODE;
    }
    
    public void playingMode() {
        if (mode == PLAYING_MODE) {
            return;
        }
        playCycles = playCycles_keep;

        System.out.println("########################");
        System.out.println("  playing mode");
        mode = PLAYING_MODE;
        selectedBody = null;
        setTrails(trails_keep);
        setShowForceField(showForceField_keep);
        setShowBodyForces(showBodyForces_keep);
        setShowVelocities(showVelocities_keep);
        // Note: the restarting and resuming seem to happen outside here.
    }

    // TODO: this is for restart initial bodies. 
    public void initialConditionsMode() {
        System.out.println("Orrery: entering initial conditions mode. mode: " + mode);
        if (mode == INITIAL_CONDITIONS_MODE) {
            return;
        }
        mode = INITIAL_CONDITIONS_MODE;
        selectedBody = null;
        playCycles_keep = playCycles;
        playCycles = 0;
        pause();
        trails_keep = trails;
        showBodyForces_keep = showBodyForces;
        showForceField_keep = showForceField;
        showVelocities_keep = showVelocities;
        setTrails(0);
        setShowBodyForces(false);
        setShowForceField(false);
        setShowVelocities(false);
        System.out.println("==============================");
        System.out.println("Entering initial conditions mode. maxMass: " + maxMass + " Body.maxMass: " + Body.maxMass + " minMass: " + minMass + " Body.minMass: " + Body.minMass);
        resetInitialMasses();
        redrawBackground();
        worldBuilderDraw();
        //worldBuilderDraw();
    }

    public void resetInitialMasses() {
        Body.setMaxMass(maxMass);
        Body.setMinMass(minMass);
        System.out.println("resetInitialMasses. nbodies_initial: " + nbodies_initial + " len(ib): " + initialBodies.length);
        
        for(int i=0; i < nbodies_initial; i++) {
            initialBodies[i].setMinMaxMasses(initialBodies[i].mass);
        }
        for(int i=0; i < nrocks_initial; i++) {
            initialRocks[i].setMinMaxMasses(initialRocks[i].mass);
        }
        for(int r=0; r < nrocks_initial; r++) {
            initialRocks[r].massToSize(drawer);
        }
        for(int b=0; b < nbodies_initial; b++) {
            initialBodies[b].massToSize(drawer);
        }
    }

    ///////
    /////// beat locking
    ///////
    private void setupQuantization() {
        float measureWholeNotes = signatureBeatsPerMeasure / signatureBeatNote;
        beatQuants = (int)(quantize / signatureBeatNote); // e.g. 4/4 time, quantize@16th => 4Q/beat
        measureQuants = beatQuants * signatureBeatsPerMeasure;
        // now we're dealing in microseconds..
        beatMs = 1000000 * bpm / 60;
        quantMs = beatMs * beatQuants;
        measureMs = beatMs * signatureBeatsPerMeasure;
    }
    
    public void worldBuilderDraw() {
        //System.out.println("WBdraw(" + (mode == INITIAL_CONDITIONS_MODE? "[INIT]" : "") + ")");
        drawer.clearbg(bgColor);
        if (mode == INITIAL_CONDITIONS_MODE) {
            draw(initialBodies, initialRocks, nbodies_initial, nrocks_initial);
        } else {
            draw(bodies, rocks, nbodies, nrocks);
        }
        drawer.show(true);
        repaint();
    }

    public WorldBuilder getWorldBuilder() {
        return this.worldBuilder;
    }
    
    public void setWorldBuilder(WorldBuilder val) {
        this.worldBuilder = val;
    }

    public void setKepler(Kepler val) {
        this.kepler = val;
    }
    
    public void setupDrawing() {
        setDisplayInfoSeconds(initialInfoDisplaySeconds);
        drawer.show(true);
        repaint();
        if (debugLevel > 0) {
            System.out.println("Orrery.setUpDrawing -- complete. ");
        }
    }

    public void setupMidiStuff() {
        midiStuff = new MidiStuff(this);
    }

    ///////////////////
    ///  Accessors  ///
    ///////////////////
    
    public void setReboundMethod(int r) {
        Body.setReboundMethod(r);
    }

    public void setDt(double val) {
        this.dt = val;
    }
    public double getDt() {
        return this.dt;
    }

    public void setCycleTime(int val) {
        this.cycleTime = val;
    }
    public int getCycleTime() {
        return this.cycleTime;
    }

    public void setPlayTimes(int valLo, int valHi) {
        this.playTimeLo = valLo; // seconds.
        this.playTimeHi = valHi; // seconds.
    }


    
    public double getFriction() {
        return friction;
    }

    public void setFriction(double friction) {
        this.friction = friction;
        System.out.println("ORRERY.setFriction: " + friction);
        Body.setFriction(friction);
    }

    public double getGravityFactorBody() {
        return gravFactorBody;
    }

    public void setGravityFactorBody(double gravFactorBody) {
        System.out.println("Orrery.setGRAV BODY: " + gravFactorBody);
        this.gravFactorBody = gravFactorBody;
    }

    public double getGravityFactorRock() {
        return gravFactorRock;
    }

    public void setGravityFactorRock(double gravFactorRock) {
        System.out.println("Orrery.setGRAV ROCK: " + gravFactorRock);
        this.gravFactorRock = gravFactorRock;
    }

    public double getRepelFactorBody() {
        return repelFactorBody;
    }

    public void setRepelFactorBody(double repelFactorBody) {
        System.out.println("Orrery.setREPEL BODY: " + repelFactorBody);
        this.repelFactorBody = repelFactorBody;
    }

    public double getRepelFactorRock() {
        return repelFactorRock;
    }

    public void setRepelFactorRock(double repelFactorRock) {
        System.out.println("Orrery.setREPEL ROCK: " + repelFactorRock);
        this.repelFactorRock = repelFactorRock;
    }

    // TODO: might need to pass in number in list. 
    public double findAverageMass(Body[] bodyList) {
        double sum = 0.;
        int count = 0;
        for(int i=0; i < bodyList.length; i++) {
            if (bodyList[i] != null) {
                sum += bodyList[i].mass;
                count++;
            }
        }
        if (count == 0) {
            return 0;
        }
        return sum / count;
    }
                
    ///////////////////
    ///             ///
    ///////////////////

    public void possiblyResetPlayCycles() {
        if (playCycles_keep > 0) {
            playCycles = playCycles_keep;
        }
    }

    public void randomizePlayCycles()  {
        if (playCycles != 0) {
            int playTime = randomRange(this.playTimeLo, this.playTimeHi);
            this.playCycles = secondsToCycles(playTime);
            this.playCycles_keep = this.playCycles;
        }
    }

    public int secondsToCycles(float secs) {
        if (this.cycleTime > 0)  {
            return (int) ((1000. / this.cycleTime) * secs);
        } else  {
            return (int)(100.f * secs);
        }
    }

    public double cyclesToSeconds(int cycles) {
        if (cycleTime > 0)  {
            return (cycleTime / 1000.) * cycles;
        } else  {
            return (cycles / 100.);
        }
    }

    public int getDebugLevel() {
        return  this.debugLevel;
    }
    public void setDebugLevel(int val) {
        this.debugLevel = val;
    }

    public int getTrails() {
        return this.trails;
    }
    public void setTrails(int val) {
        if (isInitialConditionsMode()) {
            this.trails_keep = val;
        } else {
            this.trails = val;
            drawer.setTrails(val);
        }
    }


    public void resetDefaults() {
        friction             = .99;
        gravFactorBody       = 1.0;
        repelFactorBody      = 1.0;
        gravFactorRock       = 1.0;
        repelFactorRock      = 1.0;
        Body.resetDefaults();
        Rock.resetDefaults();
        title                = "";
        backgrounds          = new ArrayList();
        channelMap           = new HashMap();
        local_dt             = -1.;
        minForce             = 20;
        maxForce             = 27;
        nrocks               = 0;
        nrocks_initial       = 0;
        nbodies              = 0;
        nbodies_initial      = 0;
        for(int i=0; i < numChannels; i++) {
            midiVelocity_min[i] = defaultMidiVelocity_min;
            midiVelocity_max[i] = defaultMidiVelocity_max;
        }
        setupQuantization();
    }

    public String getWorldFile() {
        return this.worldFileName;
    }
    
    public void setWorldFile(String fileName) {
        this.worldFileName = fileName;
    }

    
    public void clearWorld() {
        resetDefaults();
        Body.setFriction(friction);
    }
    
    public void readWorld() {
        readWorld(reader);
    }

    public Body[] getBodies() {
        return bodies;
    }

    public int getBodyCount() {
        return nbodies;
    }

    public Rock[] getRocks() {
        return rocks;
    }

    public int getRockCount() {
        return nrocks;
    }

    public Body[] getInitialBodies() {
        return initialBodies;
    }

    public int getInitialBodyCount() {
        return nbodies_initial;
    }

    public Rock[] getInitialRocks() {
        return initialRocks;
    }

    public int getInitialRockCount() {
        return nrocks_initial;
    }

    public void copyInitialBodies() {
        Body.resetNth();
        Rock.resetNth();
        if (debugLevel > 0) {
            System.out.println("copyInitialBodies. nbodies_initial: " + nbodies_initial + " len(ib): " + initialBodies.length);
        }
        for(int i=0; i < nbodies_initial; i++) {
            bodies[i] = initialBodies[i].duplicate();
            bodies[i].resetPlayable();
            bodies[i].setMinMaxMasses(bodies[i].mass);
        }
        nbodies = nbodies_initial;
                        
        if (debugLevel > 0) {
            System.out.println("copying Initial rocks. nrocks_initial: " + nrocks_initial + " len(ir): " + initialRocks.length);
        }
        for(int i=0; i < nrocks_initial; i++) {
            rocks[i] = (Rock)initialRocks[i].duplicate();
            rocks[i].resetPlayable();
        }
        nrocks = nrocks_initial;
        for(int r=0; r < nrocks; r++) {
            rocks[r].massToSize(drawer);
        }
        for(int b=0; b < nbodies; b++) {
            bodies[b].massToSize(drawer);
        }
    }

    public void setPlayable(Body body, String type, String name, boolean shared) {
        if (type.equalsIgnoreCase("note")) {
            body.setPlayable(new Note(name));    
            
        } else if (type.equalsIgnoreCase("scale")) {
            body.setPlayable(getScale(name));

        } else if (type.equals("melody")) {
            Melody m;
            if (shared) {
                m = getOrCreateMelody(name);
                m.setShared(true);
            } else {
                m = cloneMelody(name);
            }
            body.setPlayable(m);

        } else if (type.equalsIgnoreCase("sequence")) {
            Playable s;
            if (shared) {
                s = getSequence(name);
                s.setShared(true); 
            } else {
                s = getSequence(name).duplicate();
            }
            if (debugLevel > 0) {
                System.out.println("Setting sequence: "  +name);
            }
            body.setPlayable(s);
        }
    }

    public void readWorld(TokenReader reader) {
        clearMelodies();
        clearInfos();
        try {
            readThings(reader);
            if (debugLevel > 0) {
                System.out.println("MaxBodies:" + maxBodies);
            }
            forces = new Vect[maxBodies];
            for (int i = 0; i < maxBodies; i++) {
                forces[i] = new Vect();
            }
            for(int r=0; r < nrocks; r++) {
                initialRocks[r].massToSize(drawer);
            }
            for(int b=0; b < nbodies; b++) {
                initialBodies[b].massToSize(drawer);
            }
        } catch (Exception ex) {
            System.out.println("Caught exception: " + ex);
            ex.printStackTrace();
        }
    }

    public void readThings(TokenReader reader) throws IOException {
        boolean done = false;
        while (!done) {
            String token = reader.readToken();
            if (token == null) {
                done = true;
            } else if (token.startsWith("@")) {
                String key = token.substring(1);
                String line = reader.readLine();
                addInfo(key, line);
            } else if (token.equalsIgnoreCase("stop")) {
                done = true;
            } else if (token.equalsIgnoreCase("background")) { 
                String background = reader.readToken();
                backgrounds.add(background);
                drawBG(background);
                drawer.initbg(bgColor);
                drawer.show(true);
                repaint();
            } else if (token.equalsIgnoreCase("drawbg")) {
                drawer.initbg(bgColor);
                drawer.show(true);
                repaint();
            } else if (token.equalsIgnoreCase("title")) {
                title = reader.readLine().trim();

            } else if (token.equalsIgnoreCase("dt")) {
                local_dt = reader.readDouble();

            } else if (token.equalsIgnoreCase("maxbodies")) {
                maxBodies = reader.readInt();
                nbodies = 0;
                initialBodies = new Body[maxBodies + 1];
                bodies = new Body[maxBodies + 1];
                Body.setupCollisions(maxBodies * 4);

            } else if (token.equalsIgnoreCase("maxmass")) {
                double mm = reader.readDouble();
                if (debugLevel > 0) {
                    System.out.println("maxMass: " + mm);
                }
                maxMass = mm;
                Body.setMaxMass(mm);

            } else if (token.equalsIgnoreCase("minmass")) {
                double mm = reader.readDouble();
                if (debugLevel > 0) {
                    System.out.println("minMass: " + mm);
                }
                minMass = mm;
                Body.setMinMass(mm);

            } else if (token.equalsIgnoreCase("midi_velocity_min")) {
                int mvm = reader.readInt();
                if (debugLevel > 0) {
                    System.out.println("midi velocity min: " + mvm);
                }
                for(int i=0; i < numChannels; i++) {
                    midiVelocity_min[i] = mvm;
                }
            } else if (token.equalsIgnoreCase("midi_velocity_max")) {
                int mvm = reader.readInt();
                if (debugLevel > 0) {
                    System.out.println("midi velocity max: " + mvm);
                }
                for(int i=0; i < numChannels; i++) {
                    midiVelocity_max[i] = mvm;
                }

            } else if (token.equalsIgnoreCase("maxrocks")) {
                maxRocks = reader.readInt();
                nrocks = 0;
                initialRocks = new Rock[maxRocks + 1];
                rocks = new Rock[maxRocks + 1];

            } else if (token.equalsIgnoreCase("radius")) {
                radius = reader.readDouble();
                Body.setOrreryRadius(radius);
                drawer.setXscale(-radius, +radius); 
                drawer.setYscale(-radius, +radius);
                drawer.postDisplayInit();
                minDistance = radius * .01;
                Body.setMinDistance(minDistance);
                resetForceFieldGridVectors();

            } else if (token.equalsIgnoreCase("baseradius")) {
                bodyBaseRadius = reader.readDouble();
                Body.setBaseRadius(bodyBaseRadius);

            } else if (token.equalsIgnoreCase("friction")) {
                friction = reader.readDouble();
                Body.setFriction(friction);

            } else if (token.equalsIgnoreCase("gridcircles")) {
                gridCircles = reader.readInt();
                if (debugLevel > 0) {
                    System.out.println("  gridCircles: " + gridCircles);
                }


            } else if (token.equalsIgnoreCase("gridradii")) {
                gridRadii = reader.readInt();
                if (debugLevel > 0) {
                    System.out.println("  gridRadii: " + gridRadii);
                }

            } else if (token.equalsIgnoreCase("channel") ) {
                readChannel(reader);

            } else if (token.equalsIgnoreCase("channel_queue") ) {
                int ch  = reader.readInt();
                int chq  = reader.readInt();
                if (debugLevel > 0) {
                    System.out.println("Setting channel q("+ ch+ ").size() to " +chq);
                }
                channelQueues[ch]  = new ChannelQueue(chq);

            } else if (token.equalsIgnoreCase("end_channels")) {
                midiStuff.reloadPatchSet();

            } else if (token.equalsIgnoreCase("reboundmethod")) {
                String next = reader.readToken();
                if (next.equalsIgnoreCase("square")) {
                    setReboundMethod(Body.REBOUND_SQUARE); 
                } else if (next.equalsIgnoreCase("none")) {
                    setReboundMethod(Body.REBOUND_NONE);
                } else if (next.equalsIgnoreCase("circle")) {
                    setReboundMethod(Body.REBOUND_CIRCLE);
                } else if (next.equalsIgnoreCase("circlefake")) {
                    setReboundMethod(Body.REBOUND_CIRCLE_FAKE);
                } else if (next.equalsIgnoreCase("circlenotrap")) {
                    setReboundMethod(Body.REBOUND_CIRCLE_NOTRAP);
                } else if (next.equalsIgnoreCase("torus") || next.equalsIgnoreCase("wrap")) {
                    setReboundMethod(Body.REBOUND_TORUS);
                }
                if (debugLevel > 0) {
                    System.out.println("  Rebound Method: " + next);
                }

            } else if (token.equalsIgnoreCase("gravFactor") || token.equalsIgnoreCase("gravityFactor")) {
                String next = reader.readToken();
                if (next.equalsIgnoreCase("body")) {
                    gravFactorBody = reader.readDouble();
                } else if (next.equalsIgnoreCase("rock")) {
                    gravFactorRock = reader.readDouble();
                } else {
                    try {
                        double bothFactors = Double.parseDouble(next);
                        gravFactorBody = bothFactors;
                        gravFactorRock = bothFactors;
                    } catch (Exception ex) {
                    }
                } 

            } else if (token.equalsIgnoreCase("repelFactor")) {
                String next = reader.readToken();
                if (next.equalsIgnoreCase("body")) {
                    repelFactorBody = reader.readDouble();
                } else if (next.equalsIgnoreCase("rock")) {
                    repelFactorRock = reader.readDouble();
                    if (debugLevel > 0) {
                        System.out.println(" Repel Factor ROCK " + repelFactorRock);
                    }
                } else {
                    try {
                        double bothFactors = Double.parseDouble(next);
                        repelFactorBody = bothFactors;
                        repelFactorRock = bothFactors;
                    } catch (Exception ex) {
                    }
                }

            } else if (token.equalsIgnoreCase("body")) {
                readBody(reader);

            } else if (token.equalsIgnoreCase("rock")) {
                readRock(reader);

            } else if (token.equalsIgnoreCase("scale")) {
                Scale scale = readScale(reader);
                addScale(scale.getName(), scale);

                if (debugLevel > 0) {
                    System.out.println("Scale: " + scale);
                }

            } else if (token.equalsIgnoreCase("melody")) {
                Melody melody = readMelody(reader);
                addMelody(melody.getName(), melody);
                if (debugLevel > 0) {
                    System.out.println("Melody: " + melody);
                }

            } else if (token.equalsIgnoreCase("sequence")) {
                Sequence sequence = readSequence(reader);
                addSequence(sequence.getName(), sequence);
                if (debugLevel > 0) {
                    System.out.println("sequence: " + sequence);
                }


            } else {
                reader.readLine();
            }
            //System.out.println(" ReadLine. AtEol: " + reader.atEol());
        }
    }

    private void readChannel(TokenReader reader)  {
        try {
            int ch = reader.readInt();
            String channelName = reader.readToken();
            channelMap.put(channelName, new Integer(ch));
            channelMap.put("" + ch, channelName);
            int bank = reader.readInt();
            int program = reader.readInt();
            midiStuff.setPatch(ch, bank, program);
            String maybeSetMidiRange = reader.readToken();
            if (debugLevel > 0) {
                System.out.println("channel " + ch + ": " + channelName + " b: " + bank + " p: " + program);
            }
            if (!"midi".equals(maybeSetMidiRange)) {
                reader.pushToken();
            } else {
                int midiVelLow = reader.readInt();
                int midiVelHigh = reader.readInt();
                if (midiVelLow > 0) {
                    midiVelocity_min[ch] = midiVelLow;
                }
                if (midiVelHigh > 0) {
                    midiVelocity_max[ch] = midiVelHigh;
                }
            }

        } catch (Exception ex) {
            System.out.println(" Caught exception reading Channel. " + ex);
            ex.printStackTrace();
            try {
                reader.readLine();
            } catch (Exception exx) {
                System.out.println(" Caught exception reading rest of line. " + ex);
                exx.printStackTrace();
            }
        }
    }

    private Body readBody(TokenReader reader)  {
        try {
            Body body = new Body();
            readCoords_pos(reader, body);
            readCoords_v(reader, body);
            double mass = reader.readDouble();
            body.mass = mass;
            if (debugLevel > 0) {
                System.out.println("BODY ");
            }
            body.init(maxBodies);
            addInitialBody(body);
            readBodyMods(reader, body);
            return body;
        } catch (Exception ex) {
            System.out.println(" Caught exception reading Body. " + ex);
            ex.printStackTrace();
            try {
                reader.readLine();
            } catch (Exception exx) {
                System.out.println(" Caught exception reading rest of line. " + ex);
                exx.printStackTrace();
            }
            return null;
        }
    }

    private Rock readRock(TokenReader reader) {
        try {
            double mass;
            Rock rock = new Rock();
            readCoords_pos(reader, rock);
            mass = reader.readDouble();
            rock.mass = mass;
            rock.init(maxRocks);
            if (debugLevel > 0) {
                System.out.println("ROCK ");
            }
            addInitialRock(rock);
            readBodyMods(reader, rock);
            return rock;
        } catch (Exception ex) {
            System.out.println(" Caught exception reading Rock. " + ex);
            ex.printStackTrace();
            try {
                reader.readLine();
            } catch (Exception exx) {
                System.out.println(" Caught exception reading rest of line. " + ex);
                exx.printStackTrace();
            }
            return null;
        }
    }

    public void writeWorld(String filePath) {
        writeWorld(new File(filePath));
    }

    public void writeWorldXML(String filePath, boolean flipY) {
        writeWorldXML(new File(filePath), flipY);
    }
                   
    public void writeWorld(File file) {
        try {
            StringBuffer buf = new StringBuffer(2048);
            writeWorld(buf);
            PrintStream fileOut = new PrintStream(new FileOutputStream(file));
            fileOut.println(buf);
            fileOut.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Orrery.writeWorld caught File not found exception. " + ex);
            ex.printStackTrace();
            // TODO: realy should throw here...
        }
    }

    public void writeWorldXML(File file, boolean flipY) {
        try {
            StringBuffer buf = new StringBuffer(2048);
            writeWorldXML(buf, flipY);
            PrintStream fileOut = new PrintStream(new FileOutputStream(file));
            fileOut.println(buf);
            fileOut.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Orrery.writeWorld caught File not found exception. " + ex);
            ex.printStackTrace();
            // TODO: realy should throw here...
        }
    }

    
    public void writeWorld(StringBuffer buf) {
        writeWorldMetaData(buf);
        buf.append("drawbg");
        buf.append("\n");
        writeInstruments(buf);
        writeMelodies(buf);
        buf.append("\n");
        writeBodies(buf, initialBodies);
        buf.append("\n");
        writeBodies(buf, initialRocks);
    }


    public void writeAttribute(StringBuffer buf, String key, String line) {
        if (line != null && key != null) {
            buf.append(key);
            buf.append(" ");
            buf.append(line);
            buf.append("\n");
        }
    }

    public void writeAttribute(StringBuffer buf, String key, String val, String defaultVal ) {
        if (val != null && key != null && !val.equals(defaultVal)) {
            writeAttribute(buf, key, val);
        }
    }

    public void writeAttribute(StringBuffer buf, String key, double val, double defaultVal ) {
        if (key != null && val != defaultVal) {
            writeAttribute(buf, key, "" + val);
        }
    }
    
    public void writeAttribute(StringBuffer buf, String key, int val, int defaultVal ) {
        if (key != null && val != defaultVal) {
            writeAttribute(buf, key, "" + val);
        }
    }
    
    public void writeWorldMetaData(StringBuffer buf) {
        writeInfos(buf);
        buf.append("# \n");
        buf.append("# Metadata\n");
        buf.append("# \n");
        writeAttribute(buf, "title", title);
        writeAttribute(buf, "maxbodies", maxBodies, -1);
        writeAttribute(buf, "maxrocks", maxRocks, -1);
        writeAttribute(buf, "radius", radius, 0.);
        writeAttribute(buf, "friction", friction, .99);
        writeAttribute(buf, "maxmass", maxMass, 0.);
        writeAttribute(buf, "minmass", minMass, -1.);
        writeAttribute(buf, "baseradius", bodyBaseRadius, 512);
        writeAttribute(buf, "dt", local_dt, -1.);
        writeAttribute(buf, "trails", trails, 0);
        writeAttribute(buf, "gridcircles", gridCircles, 0);
        writeAttribute(buf, "gridradii", gridRadii, 0);
        writeAttribute(buf, "reboundmethod",
                       Body.reboundMethodString(Body.getReboundMethod()),
                       Body.reboundMethodString(Body.DEFAULT_REBOUND_METHOD));
        
        // TODO: need better defaulting system.
        // TODO: write the midivelocities as channel midi ranges if not == default.
        writeAttribute(buf, "midi_velocity_min", midiVelocity_min[0], defaultMidiVelocity_min);
        writeAttribute(buf, "midi_velocity_max", midiVelocity_max[0], defaultMidiVelocity_max);
        writeAttribute(buf, "gravityfactor body", gravFactorBody, 1.);
        writeAttribute(buf, "repelfactor   body", repelFactorBody, 1.);
        writeAttribute(buf, "gravityfactor rock", gravFactorRock, 1.);
        writeAttribute(buf, "repelfactor   rock", repelFactorRock, 1.);
        for(Iterator it = backgrounds.iterator(); it.hasNext(); ) {
            String background = (String)it.next();
            writeAttribute(buf, "background", background);
        }
        buf.append("\n");
    }

    public void writeInfos(StringBuffer buf) {
        if (infos.size() > 0) {
            buf.append("# \n");
            buf.append("# Infos\n");
            buf.append("# \n");
            for(Iterator kit = infos.keySet().iterator(); kit.hasNext(); ) {
                String key = (String)kit.next();
                List info = (List)infos.get(key);
                for(Iterator it = info.iterator(); it.hasNext(); ) {
                    buf.append("@");
                    buf.append(key);
                    buf.append(" ");
                    buf.append(it.next());
                    buf.append("\n");
                }
            }
            buf.append("\n");
        }
    }

    public void writeInstruments(StringBuffer buf) {
        buf.append("# \n");
        buf.append("# Instruments\n");
        buf.append("# \n");
        // TODO: get the channels in order.
        int numChannels = channelMap.size() / 2;
        for(int ch = 0; ch < numChannels; ch++) {
            String channelName = (String)channelMap.get("" + ch);
            int bank = midiStuff.getPatchBank(ch);
            int program = midiStuff.getPatchProgram(ch);
            buf.append("channel ");
            buf.append(ch);
            buf.append(" ");
            buf.append(channelName);
            buf.append(" ");
            buf.append(bank);
            buf.append(" ");
            buf.append(program);
            buf.append("\n");
        }
        buf.append("end_channels\n");
        buf.append("\n");
    }

    ///////////////////////////////////////////
    ///   Write world as XML                ///
    ///////////////////////////////////////////
    public void writeWorldXML(StringBuffer buf, boolean flipY) {
        buf.append("<world>\n");
        String indent = "  ";
        writeWorldMetaDataXML(indent, buf);
        buf.append("\n");
        writeInstrumentsXML(indent, buf);
        writeMelodiesXML(indent, buf);
        buf.append("\n");
        writeBodiesXML(indent, buf, initialBodies, flipY);
        buf.append("\n");
        writeBodiesXML(indent, buf, initialRocks, flipY);
        buf.append("</world>\n");
    }


    public void writeAttributeXML(String indent, StringBuffer buf,
                                  String key, String line) {
        if (line != null && key != null) {
            buf.append(indent);
            buf.append("<");
            buf.append(key);
            buf.append(">");
            buf.append(line);
            buf.append("</");
            buf.append(key);
            buf.append(">");
            buf.append("\n");
        }
    }

    public void writeAttributeXML(String indent, StringBuffer buf,
                                  String key, String val, String defaultVal ) {
        if (val != null && key != null && !val.equals(defaultVal)) {
            writeAttributeXML(indent, buf, key, val);
        }
    }

    public void writeAttributeXML(String indent, StringBuffer buf,
                                  String key, double val, double defaultVal ) {
        if (key != null && val != defaultVal) {
            writeAttributeXML(indent, buf, key, "" + val);
        }
    }
    
    public void writeAttributeXML(String indent, StringBuffer buf,
                                  String key, int val, int defaultVal ) {
        if (key != null && val != defaultVal) {
            writeAttributeXML(indent, buf, key, "" + val);
        }
    }
    
    public void writeCoordsXML(String indent, StringBuffer buf,
                               String type, Vect coords,
                               boolean specifiedPolar, boolean flipY) {
        buf.append(indent);
        buf.append("<" + type + ">");
        writeCoords(buf, coords, specifiedPolar, flipY);
        buf.append("</" + type + ">\n");
    }

    public void writeWorldMetaDataXML(String indent, StringBuffer buf) {
        writeInfosXML(indent, buf);
        buf.append("\n");
        buf.append(indent + "<!-- World Metadata -->\n");
        writeAttributeXML(indent, buf, "title", title);
        writeAttributeXML(indent, buf, "maxbodies", maxBodies, -1);
        writeAttributeXML(indent, buf, "maxrocks", maxRocks, -1);
        writeAttributeXML(indent, buf, "radius", radius, 0.);
        writeAttributeXML(indent, buf, "friction", friction, .99);
        writeAttributeXML(indent, buf, "maxmass", maxMass, 0.);
        writeAttributeXML(indent, buf, "minmass", minMass, -1.);
        writeAttributeXML(indent, buf, "baseradius", bodyBaseRadius, 512);
        writeAttributeXML(indent, buf, "dt", local_dt, -1.);
        writeAttributeXML(indent, buf, "trails", trails, 0);
        writeAttributeXML(indent, buf, "gridcircles", gridCircles, 0);
        writeAttributeXML(indent, buf, "gridradii", gridRadii, 0);
        writeAttributeXML(indent, buf, "reboundmethod",
                       Body.reboundMethodString(Body.getReboundMethod()),
                       Body.reboundMethodString(Body.DEFAULT_REBOUND_METHOD));
        
        // TODO: need better defaulting system.
        // TODO: write the midivelocities as channel midi ranges if not == default.
        writeAttributeXML(indent, buf, "midi_velocity_min", midiVelocity_min[0], defaultMidiVelocity_min);
        writeAttributeXML(indent, buf, "midi_velocity_max", midiVelocity_max[0], defaultMidiVelocity_max);
        writeAttributeXML(indent, buf, "gravityfactor body", gravFactorBody, 1.);
        writeAttributeXML(indent, buf, "repelfactor   body", repelFactorBody, 1.);
        writeAttributeXML(indent, buf, "gravityfactor rock", gravFactorRock, 1.);
        writeAttributeXML(indent, buf, "repelfactor   rock", repelFactorRock, 1.);
        for(Iterator it = backgrounds.iterator(); it.hasNext(); ) {
            String background = (String)it.next();
            writeAttributeXML(indent, buf, "background", background);
        }
        buf.append("\n");
    }

    public void writeInfosXML(String indent, StringBuffer buf) {
        if (infos.size() > 0) {
            buf.append("\n");
            buf.append(indent + "<!-- Infos -->\n");
            for(Iterator kit = infos.keySet().iterator(); kit.hasNext(); ) {
                String key = (String)kit.next();
                List info = (List)infos.get(key);
                for(Iterator it = info.iterator(); it.hasNext(); ) {
                    buf.append(indent);
                    buf.append("<info key=\"");
                    buf.append(key);
                    buf.append("\">");
                    buf.append(it.next());
                    buf.append("</info>\n");
                }
            }
            buf.append("\n");
        }
    }

    public void writeInstrumentsXML(String indent, StringBuffer buf) {
        buf.append("\n");
        buf.append(indent + "<instruments>\n");
        String indent1 = indent + "  ";
        String indent2 = indent + "    ";
        // TODO: get the channels in order.
        int numChannels = channelMap.size() / 2;
        for(int ch = 0; ch < numChannels; ch++) {
            String channelName = (String)channelMap.get("" + ch);
            int bank = midiStuff.getPatchBank(ch);
            int program = midiStuff.getPatchProgram(ch);
            buf.append(indent1);
            buf.append("<instrument type=\"midi\">\n");
            writeAttributeXML(indent2, buf, "channel", ch, -1);
            writeAttributeXML(indent2, buf, "name", channelName, "");
            writeAttributeXML(indent2, buf, "bank", bank, -1);
            writeAttributeXML(indent2, buf, "program", program, -1);
            buf.append(indent1 + "</instrument>\n");
        }
        buf.append(indent + "</instruments>\n");
        buf.append("\n");
    }

    //////////////////////////////////////////

    public int getNumChannels() {
        int numChannels = channelMap.size() / 2;
        return numChannels;
    }

    public String getChannelName(int ch) {
        return (String)channelMap.get("" + ch);
    }

    public int getChannelBank(int ch) {
        int bank = midiStuff.getPatchBank(ch);
        return bank;
    }

    public int getChannelProgram(int ch) {
        int program = midiStuff.getPatchProgram(ch);
        return program;
    }

    public void setChannelInfo(int ch, String name, int bank, int program) {
        channelMap.put(name, new Integer(ch));
        channelMap.put("" + ch, name);
        midiStuff.setPatch(ch, bank, program);
    }

    public void reloadPatches() {
        midiStuff.reloadPatchSet();
    }


    ////////////////////////////////////////////////////////////////

    public void writeMelodies(StringBuffer buf) {
        buf.append("# \n");
        buf.append("# Melodies\n");
        buf.append("# \n");
        if (debugLevel > 0) {
            System.out.println("writeMelodies. ");
            System.out.println(" scales: " + (scales == null ? "<null>" : ("" + scales.size())));
        }
        if (scales != null) {
            for(Iterator it=scales.keySet().iterator(); it.hasNext(); ) {
                String name = (String)it.next();
                Scale scale = (Scale)scales.get(name);
                scale.write(buf);
                buf.append("\n");
            }
            buf.append("\n");
        }
        if (debugLevel > 0) {
            System.out.println(" melodies: " + (melodies == null ? "<null>" : ("" + melodies.size())));
        }
        if (melodies != null) {
            for(Iterator it=melodies.keySet().iterator(); it.hasNext(); ) {
                String name = (String)it.next();
                Melody melody = (Melody)melodies.get(name);
                melody.write(buf);
                buf.append("\n");
            }
            buf.append("\n");
        }
        if (debugLevel > 0) {
            System.out.println(" sequences: " + (sequences == null ? "<null>" : ("" + sequences.size())));
        }
        if (sequences != null) {
            for(Iterator it=sequences.keySet().iterator(); it.hasNext(); ) {
                String name = (String)it.next();
                Sequence sequence = (Sequence)sequences.get(name);
                sequence.write(buf);
                buf.append("\n");
            }
            buf.append("\n");
        }
    }

    public void writeBodies(StringBuffer buf, Body[] bodies) {
        for(int i=0; i < bodies.length; i++) {
            Body body = bodies[i];
            if (body != null) {
                if (body instanceof Rock) {
                    writeRock(buf, (Rock)body);
                } else {
                    writeBody(buf, body);
                }
            }
        }
    }

    public void writeRock(StringBuffer buf, Rock rock) {
        buf.append("rock ");
        Vect pos = rock.pos;
        if (rock.posOffset != null) {
            pos = pos.minus(rock.posOffset);
        }
        writeCoords(buf, pos, rock.posSpecifiedPolar);
        buf.append(rock.getMass());
        writeBodyMods(buf, rock);
        buf.append("\n");
    }
    
    public void writeBody(StringBuffer buf, Body body) {
        buf.append("body ");
        // if there's an offset, need to subtract first
        Vect pos = body.pos;
        if (body.posOffset != null) {
            pos = pos.minus(body.posOffset);
        }
        writeCoords(buf, pos, body.posSpecifiedPolar);
        writeCoords(buf, body.v, body.vSpecifiedPolar);
        buf.append(body.getMass());
        writeBodyMods(buf, body);
        buf.append("\n");
    }

    public void writeBodyMods(StringBuffer buf, Body body) {
        if (body.posOffset != null) {
            buf.append(" offset ");
            writeCoords(buf, body.posOffset, body.offsetSpecifiedPolar);
        }
        if (body.constrainMovesToRadius()) {
            buf.append(" constrain");
        }
        if (body.getRepelFactor() != 1.0) {
            buf.append(" repelfactor");
            buf.append(body.getRepelFactor());
        }
        if (body.getGravityFactor() != 1.0) {
            buf.append(" gravityfactor");
            buf.append(body.getGravityFactor());
        }
        Playable playable = body.getPlayable();
        if (playable != null) {
            buf.append(" ");
            buf.append(playable.getType());
            buf.append(" ");
            if (playable.isShared()) {
                buf.append("shared ");
            }
            buf.append(playable.getName());
        }
        int channel = body.getChannel();
        String instrumentName = (String)channelMap.get("" + channel);
        if (instrumentName != null) {
            buf.append(" instrument");
            buf.append(" ");
            buf.append(instrumentName);
        } else if (body.getChannel() != 0) {
            buf.append(" channel");
            buf.append(body.getChannel());
        }
        
        // write actions here, if we ever get around to defining them.

        writeMutations(buf, body);

        if (body instanceof Rock && ((Rock)body).move_theta > 0.) {
            buf.append(" move_theta ");
            buf.append(((Rock)body).move_theta);
        }
    }
    
    private void writeMutations(StringBuffer buf, Body body) {
        if (body.is_mutator) {
            buf.append(" mutate");
            if (body.mutate_after > 0) {
                buf.append(" after ");
                buf.append(body.mutate_after);
            }

            if (body.mutate_clone) {
                buf.append(" clone ");
                buf.append(body.mutate_clone_chance);
                buf.append(" ");
                buf.append(body.mutate_clone_lo);
                buf.append(" ");
                buf.append(body.mutate_clone_hi);
            }

            if (body.mutate_killa) {
                buf.append(" killa ");
                buf.append(body.mutate_killa_chance);
            }

            if (body.mutate_blackhole) {
                buf.append(" blackhole ");
                buf.append(body.mutate_blackhole_chance);
            }


            if (body.mutate_mass_type == Body.MUTATE_MASS) {
                buf.append(" mass ");
                buf.append(body.mutate_mass_chance);
                buf.append(" ");
                buf.append(body.mutate_mass_lo);
                buf.append(" ");
                buf.append(body.mutate_mass_hi);
            }

            if (body.mutate_mass_type == Body.MUTATE_MASS_PERCENT) {
                buf.append(" mass% ");
                buf.append(body.mutate_mass_chance);
                buf.append(" ");
                buf.append(body.mutate_mass_lo);
                buf.append(" ");
                buf.append(body.mutate_mass_hi);
            }

            if (body.mutate_velocity_type == Body.MUTATE_VELOCITY_PERCENT) {
                buf.append(" velocity% ");
                buf.append(body.mutate_velocity_chance);
                buf.append(" ");
                buf.append(body.mutate_velocity_lo);
                buf.append(" ");
                buf.append(body.mutate_velocity_hi);
            }
            buf.append(" end");
        }
    }

    //////////////////////////////////////////////////////////////
    ///   write bodies & stuffs as XML
    //////////////////////////////////////////////////////////////
    public void writeMelodiesXML(String indent, StringBuffer buf) {
        buf.append("\n");
        buf.append(indent + "<!-- Melodies -->\n");
        if (debugLevel > 0) {
            System.out.println("writeMelodiesXML. ");
            System.out.println(" scales: " + (scales == null ? "<null>" : ("" + scales.size())));
        }
        if (scales != null) {
            for(Iterator it=scales.keySet().iterator(); it.hasNext(); ) {
                String name = (String)it.next();
                Scale scale = (Scale)scales.get(name);
                scale.writeXML(indent, buf);
                buf.append("\n");
            }
            buf.append("\n");
        }
        if (debugLevel > 0) {
            System.out.println(" melodies: " + (melodies == null ? "<null>" : ("" + melodies.size())));
        }
        if (melodies != null) {
            for(Iterator it=melodies.keySet().iterator(); it.hasNext(); ) {
                String name = (String)it.next();
                Melody melody = (Melody)melodies.get(name);
                melody.writeXML(indent, buf);
                buf.append("\n");
            }
            buf.append("\n");
        }
        if (debugLevel > 0) {
            System.out.println(" sequences: " + (sequences == null ? "<null>" : ("" + sequences.size())));
        }
        if (sequences != null) {
            for(Iterator it=sequences.keySet().iterator(); it.hasNext(); ) {
                String name = (String)it.next();
                Sequence sequence = (Sequence)sequences.get(name);
                sequence.writeXML(indent, buf);
                buf.append("\n");
            }
            buf.append("\n");
        }
    }

    public void writeBodiesXML(String indent, StringBuffer buf,
                               Body[] bodies, boolean flipY) {
        for(int i=0; i < bodies.length; i++) {
            Body body = bodies[i];
            if (body != null) {
                if (body instanceof Rock) {
                    writeRockXML(indent, buf, (Rock)body, flipY);
                } else {
                    writeBodyXML(indent, buf, body, flipY);
                }
            }
        }
    }

    public void writeRockXML(String indent, StringBuffer buf, Rock rock,
                             boolean flipY) {
        buf.append(indent);
        buf.append("<rock>\n");
        String indent1 = indent + "  ";
        Vect pos = rock.pos;
        if (rock.posOffset != null) {
            pos = pos.minus(rock.posOffset);
        }
        writeCoordsXML(indent1, buf, "pos", pos, rock.posSpecifiedPolar, flipY);
        writeAttributeXML(indent1, buf, "mass", rock.getMass(), -1);
        writeBodyModsXML(indent1, buf, rock, flipY);
        buf.append(indent + "</rock>\n");
    }
    
    public void writeBodyXML(String indent, StringBuffer buf, Body body,
                             boolean flipY) {
        buf.append(indent);
        buf.append("<body>\n");
        String indent1 = indent + "  ";
        // if there's an offset, need to subtract first
        Vect pos = body.pos;
        if (body.posOffset != null) {
            pos = pos.minus(body.posOffset);
        }
        writeCoordsXML(indent1, buf, "pos", pos, body.posSpecifiedPolar, flipY);
        writeCoordsXML(indent1, buf, "velocity", body.v, body.vSpecifiedPolar, flipY);
        writeAttributeXML(indent1, buf, "mass", body.getMass(), -1);
        writeBodyModsXML(indent1, buf, body, flipY);
        buf.append(indent + "</body>\n");
    }

    public void writeBodyModsXML(String indent, StringBuffer buf, Body body,
                                 boolean flipY) {
        if (body.posOffset != null) {
            writeCoordsXML(indent, buf, "offset", body.posOffset,
                           body.offsetSpecifiedPolar, flipY);
        }
        if (body.constrainMovesToRadius()) {
            writeAttributeXML(indent, buf, "constrain", "true", "");
        }
        writeAttributeXML(indent, buf, "repelFactor", body.getRepelFactor(), 1.0);
        writeAttributeXML(indent, buf, "gravityFactor", body.getGravityFactor(), 1.0);
        Playable playable = body.getPlayable();
        if (playable != null) {
            buf.append(indent);
            buf.append("<" + playable.getType());
            if (playable.isShared()) {
                buf.append(" shared=\"true\"");
            }
            buf.append(">");
            buf.append(playable.getName());
            buf.append("</" + playable.getType() + ">\n");
        }
        int channel = body.getChannel();
        String instrumentName = (String)channelMap.get("" + channel);
        if (instrumentName != null) {
            writeAttributeXML(indent, buf, "instrument", instrumentName, "");
        } else if (body.getChannel() != 0) {
            writeAttributeXML(indent, buf, "channel", body.getChannel(), 0);
        }
        
        // write actions here, if we ever get around to defining them.

        writeMutationsXML(indent, buf, body);

        if (body instanceof Rock && ((Rock)body).move_theta > 0.) {
            writeAttributeXML(indent, buf, "move_theta", ((Rock)body).move_theta, 0.);
        }
    }
    
    private void writeMutationsXML(String indent, StringBuffer buf, Body body) {
        if (body.is_mutator) {
            if (body.mutate_clone) {
                writeMutationXML(indent, buf, "clone", body.mutate_after,
                                 body.mutate_clone_chance,
                                 body.mutate_clone_lo, body.mutate_clone_hi);
            }
            if (body.mutate_killa) {
                writeMutationXML(indent, buf, "killa", body.mutate_after,
                                 body.mutate_killa_chance, -1, -1);
            }
            if (body.mutate_blackhole) {
                writeMutationXML(indent, buf, "blackhole", body.mutate_after,
                                 body.mutate_blackhole_chance, -1, -1);
            }
            if (body.mutate_mass_type == Body.MUTATE_MASS) {
                writeMutationXML(indent, buf, "mass", body.mutate_after,
                                 body.mutate_mass_chance,
                                 body.mutate_mass_lo, body.mutate_mass_hi);
            }
            if (body.mutate_mass_type == Body.MUTATE_MASS_PERCENT) {
                writeMutationXML(indent, buf, "mass%", body.mutate_after,
                                 body.mutate_mass_chance,
                                 body.mutate_mass_lo, body.mutate_mass_hi);
            }
            if (body.mutate_velocity_type == Body.MUTATE_VELOCITY_PERCENT) {
                writeMutationXML(indent, buf, "velocity%", body.mutate_after,
                                 body.mutate_velocity_chance,
                                 body.mutate_velocity_lo, body.mutate_velocity_hi);
            }
        }
    }

    private void writeMutationXML(String indent, StringBuffer buf,
                                  String mutationType,
                                  int after, int chance,
                                  double range_low, double range_hi) {
        buf.append(indent);
        buf.append("<mutate type=\"" + mutationType+ "\"");
        if (after > 0) {
            buf.append(" after=\"" + after + "\"");
        }
        buf.append(" chance=\"" + chance + "\"");
        if (range_low >= 0.) {
            buf.append(" low=\"" + range_low + "\"");
        }
        if (range_hi >= 0.) {
            buf.append(" hi=\"" + range_hi + "\"");
        }
        buf.append("/>\n");
    }

    
    /////////////////////////////////////////////////////////////////

    public void addInitialBody(Body body) {
        if (nbodies_initial < maxBodies) {
            initialBodies[nbodies_initial] = body;
            nbodies_initial++;
            body.massToSize(drawer);
        }
    }

    public void addBody(Body body) {
        if (nbodies < maxBodies) {
            bodies[nbodies] = body;
            nbodies++;
            body.massToSize(drawer);
        }
    }

    public void addInitialRock(Rock rock) {
        rock.orrery = this;
        if (nrocks_initial < maxRocks) {
            initialRocks[nrocks_initial] = rock;
            nrocks_initial++;
            rock.massToSize(drawer);
        }
    }

    public void addRock(Rock rock) {
        rock.orrery = this;
        if (nrocks < maxRocks) {
            rocks[nrocks] = rock;
            nrocks++;
            rock.massToSize(drawer);
        }
    }

    public void deleteBody(Body body) {
        int found = deleteBody(body, bodies);
        if (found != -1) {
            nbodies--;
        }
    }

    public void deleteInitialBody(Body body) {
        int found = deleteBody(body, initialBodies);
        if (found != -1) {
            nbodies_initial--;
        }
    }

    public void deleteRock(Rock rock) {
        int found = deleteBody(rock, rocks);
        if (found != -1) {
            nrocks--;
        }
    }

    public void deleteInitialRock(Rock rock) {
        int found = deleteBody(rock, initialRocks);
        if (found != -1) {
            nrocks_initial--;
        }
    }

    public int deleteBody(Body body, Body[] bodyList) {
        int found = -1;
        for(int i=0; i < bodyList.length; i++) {
            Body candidate = bodyList[i];
            if (candidate != null && candidate == body) {
                found = i;
                break;
            }
        }
        if (found != -1) {
            // move the ones above over.
            for(int j=found; j < bodyList.length - 1; j++) {
                bodyList[j] = bodyList[j+1];
            }
            bodyList[bodyList.length - 1] = null;
        }
        return found;
    }

    public void readCoords_pos(TokenReader reader, Body body) throws IOException {
        String next = reader.readToken();
        Vect coords;
        if (next.equalsIgnoreCase("polar")) {
            double r = reader.readDouble();
            double theta = reader.readDouble();
            coords = Vect.createPolarDegrees(r, theta);
            body.posSpecifiedPolar = true;
        } else {
            double rx = Double.parseDouble(next); 
            double ry = reader.readDouble(); 
            coords = new Vect(rx, ry);
            body.posSpecifiedPolar = false;
        }
        body.pos = coords;
    }

    public void readCoords_v(TokenReader reader, Body body) throws IOException {
        String next = reader.readToken();
        Vect coords;
        if (next.equalsIgnoreCase("polar")) {
            double r = reader.readDouble();
            double theta = reader.readDouble();
            coords = Vect.createPolarDegrees(r, theta);
            body.vSpecifiedPolar = true;
        } else {
            double rx = Double.parseDouble(next); 
            double ry = reader.readDouble(); 
            coords = new Vect(rx, ry);
            body.vSpecifiedPolar = false;
        }
        body.v = coords;
    }

    public void readCoords_offset(TokenReader reader, Body body) throws IOException {
        String next = reader.readToken();
        Vect coords;
        if (next.equalsIgnoreCase("polar")) {
            double r = reader.readDouble();
            double theta = reader.readDouble();
            coords = Vect.createPolarDegrees(r, theta);
            body.offsetSpecifiedPolar = true;
        } else {
            double rx = Double.parseDouble(next); 
            double ry = reader.readDouble(); 
            coords = new Vect(rx, ry);
        }
        body.posOffset = coords;
    }


    public void readBodyMods(TokenReader reader, Body body) throws IOException {
        while (!reader.atEol()) {
            String next = reader.readToken();
            if (debugLevel > 0) {
                System.out.println("      read mod: " + next + " atEol: " + reader.atEol());
            }
            if (next == null) {
                reader.readLine();
                // ignore..
            } else if (next.equalsIgnoreCase("offset")) {
                readCoords_offset(reader, body);
                body.pos.plusEquals(body.posOffset);
            } else if (next.equals("constrain")) {
                body.setConstrainMovesToRadius(true);
                if (debugLevel > 0) {
                    System.out.println("    Constrain");
                }
            } else if (next.equalsIgnoreCase("repelfactor")) {
                double rf = reader.readDouble();
                body.setRepelFactor(rf);
                if (debugLevel > 0) {
                    System.out.println("    repelFactor: " + rf);
                }
            } else if (next.equalsIgnoreCase("gravityfactor")) {
                double f = reader.readDouble();
                body.setGravityFactor(f);
                if (debugLevel > 0) {
                    System.out.println("    gravityFactor: " + f);
                }
            } else if (next.equalsIgnoreCase("note")) {
                String notestr = reader.readToken();
                setPlayable(body, "note", notestr, false);

            } else if (next.equalsIgnoreCase("scale")) {
                String name = reader.readToken();
                setPlayable(body, "scale", name, false);

            } else if (next.equalsIgnoreCase("melody")) {
                String name = reader.readToken();
                boolean shared = false;
                if (name.equalsIgnoreCase("shared")) {
                    name = reader.readToken();
                    shared = true;
                }
                setPlayable(body, "melody", name, shared);

            } else if (next.equalsIgnoreCase("sequence")) {
                String name = reader.readToken();
                boolean shared = false;
                if (name.equalsIgnoreCase("shared")) {
                    name = reader.readToken();
                    shared = true;
                }
                setPlayable(body, "sequence", name, shared);

            } else if (next.equals("channel") || next.equals("ch") || next.equalsIgnoreCase("inst") || next.equalsIgnoreCase("instrument")) {
                String channelNameOrNumber = reader.readToken();
                int ch = 0;
                try {
                    ch = Integer.parseInt(channelNameOrNumber);
                } catch (NumberFormatException ex) {
                    body.instrument = channelNameOrNumber;
                    ch = getInstrumentChannel(body.instrument);
                    if (ch == -1) {
                        ch = 0;
                    }
                }
                body.setChannel(ch);

            } else if (next.equalsIgnoreCase("action")) {
                Action action = readAction();
                addAction(action);

            } else if (next.equals("mutate")) {
                readMutations(reader, body);
            } else if (next.equals("move_theta") && body instanceof Rock) {
                double mt = reader.readDouble();
                ((Rock)body).move_theta  = mt;

            } else {
                if (debugLevel > 0) {
                    System.out.println("    other mod: " + next);
                }
            }
        }
    }

    public void writeCoords(StringBuffer buf, Vect coords, boolean specifiedPolar) {
        writeCoords(buf, coords, specifiedPolar, false);
    }
    
    public void writeCoords(StringBuffer buf, Vect coords,
                            boolean specifiedPolar, boolean flipY) {
        if (specifiedPolar) {
            buf.append("polar ");
            buf.append(coords.r());
            buf.append(" ");
            // TODO: what to do about flipY?
            buf.append(coords.thetaDegrees());
            buf.append(" ");
        } else {
            buf.append(coords.x());
            buf.append(" ");
            if (flipY) {
                buf.append(-1.0 * coords.y());
            } else {
                buf.append(coords.y());
            }
            buf.append(" ");
        }
    }
    

    public int getInstrumentChannel(String instrument) {
        Integer chI = (Integer)channelMap.get(instrument);
        int ch = -1;
        if (chI != null) {
            ch = chI.intValue();
        }
        return ch;
    }

    // mutate [|: <item> <chance> <lo> <hi> :|] end
    //   where <item> in [
    //        melody (<lo> <hi> == nth melody)
    //        mass (<lo> <hi> == absolute masses)
    //        mass% (<lo> <hi> == percent factors to multiply mass by)
    //        velocity% (<lo> <hi> ==  percent factors to multiply mass by)
    //        warp (<lo> <hi> == absolute positions to warp to)
    //        split (<lo> <hi> == number of bodies to split into)
    //        eat (<lo> <hi> == ignored)
    //       ]
    private void readMutations(TokenReader reader, Body body) throws IOException {
        String token = reader.readToken();
        while (token  != null && !token.equals("end")) {
            if (debugLevel > 0) System.out.println("ReadMutations. " + token);
            if (token.equals("after")) {
                int seconds = reader.readInt();
                body.mutate_after = secondsToCycles(seconds);
            } else if (token.equals("clone")) {
                int chance = reader.readInt();
                double lo = reader.readDouble();
                double hi = reader.readDouble();
                body.is_mutator = true;
                body.mutate_clone = true;
                body.mutate_clone_chance = chance;
                body.mutate_clone_lo = lo;
                body.mutate_clone_hi = hi;
            } else if (token.equals("killa")) {
                int chance = reader.readInt();
                body.is_mutator = true;
                body.mutate_killa = true;
                body.mutate_killa_chance = chance;

            } else if (token.equals("blackhole")) {
                int chance = reader.readInt();
                body.is_mutator = true;
                body.mutate_blackhole = true;
                body.mutate_blackhole_chance = chance;

            } else {
                int chance = reader.readInt();
                double lo = reader.readDouble();
                double hi = reader.readDouble();
                if (token.equals("mass")) {
                    body.is_mutator = true;
                    body.mutate_mass_type = Body.MUTATE_MASS;
                    body.mutate_mass_chance = chance;
                    body.mutate_mass_lo = lo;
                    body.mutate_mass_hi = hi;
                } else if (token.equals("mass%")) {
                    body.is_mutator = true;
                    body.mutate_mass_type = Body.MUTATE_MASS_PERCENT;
                    body.mutate_mass_chance = chance;
                    body.mutate_mass_lo = lo;
                    body.mutate_mass_hi = hi;
                } else if (token.equals("velocity") || token.equals("velocity%")) {
                    body.is_mutator = true;
                    body.mutate_velocity_type = Body.MUTATE_VELOCITY_PERCENT;
                    body.mutate_velocity_chance = chance;
                    body.mutate_velocity_lo = lo;
                    body.mutate_velocity_hi = hi;
                } 

            }
            token= reader.readToken();
        }
    }

    private Action  readAction() {
        return null;
    }

    public HashMap actions = new HashMap();
    public void addAction(Action action) {
        if (action != null) {
            actions.put(action.name, action);
        }
    }

    public Playable parsePlayable(String type, String name, String body) {
        Playable result = null;
        try {
            TokenReader reader = TokenReader.makeStringTokenReader(name + " " + body);
            if (type.equals("scale")) {
                result = readScale(reader);
            } else if (type.equals("melody")) {
                result = readMelody(reader);
            } else if (type.equals("sequence")) {
                result = readSequence(reader);
            }
            reader.close();
        } catch (Exception ex) {
            System.out.println("Orrery.parsePlayable got exception parsing playable(" + type + "): " + ex);
            // TODO: percolate this out for error reporting. 
        }
        return result;
    }

    public void storePlayable(Playable pl) {
        String type = pl.getType();
        String name = pl.getName();
        if (type.equals("scale")) {
            addScale(name, (Scale)pl);
        } else if (type.equals("melody")) {
            addMelody(name, (Melody)pl);
        } else if (type.equals("sequence")) {
            addSequence(name, (Sequence)pl);
        }
    }

    public void removePlayable(Playable pl) {
        String type = pl.getType();
        String name = pl.getName();
        removePlayable(type, name);
    }
    
    public void removePlayable(String type, String name) {
        if (type.equals("scale")) {
            removeScale(name);
        } else if (type.equals("melody")) {
            removeMelody(name);
        } else if (type.equals("sequence")) {
            removeSequence(name);
        }
    }

    private Scale readScale(TokenReader reader) throws IOException {
        String name = reader.readToken();
        Scale scale = new Scale(name);
        //System.out.print("S:" + name + " ");
        boolean done = false;
        while (!done) {
            String noteName = reader.readToken();
            if (noteName == null || noteName.equalsIgnoreCase("end")) {
                done = true;
            } else {
                //System.out.print("-" + noteName + "-");
                scale.addNote(new Note(noteName));
            }
        }
        //System.out.println();
        return scale;
    }

    private Melody readMelody(TokenReader reader) throws IOException {
        Scale scale = readScale(reader);
        Melody melody = new Melody(scale);
        return melody;
    }

    public void addScale(String name, Scale scale) {
        scales.put(name, scale);
    }

    public void removeScale(String name) {
        scales.put(name, null);
    }

    public void addMelody(String name, Melody melody) {
        melodies.put(name, melody);
    }

    public void removeMelody(String name) {
        melodies.put(name, null);
    }

    public Scale getScale(String name) {
        return (Scale)scales.get(name);
    }

    public Iterator getScaleNames() {
        return scales.keySet().iterator();
    }

    public Melody getOrCreateMelody(String name) {
        Melody m = getMelody(name);
        if (m != null) {
            return m;
        }
        Scale s = getScale(name);
        if (s != null) {
            m = new Melody(s);
            melodies.put(name, m);
        }
        return m;
    }

    public Melody cloneMelody(String name) {
        Melody m = getMelody(name);
        if (m != null) {
            Melody mc = new Melody(m.getScale());
            return mc;
        }
        return null;
    }

    public Iterator getMelodyNames() {
        return melodies.keySet().iterator();
    }
    
    public Melody getMelody(String name) {
        return (Melody)melodies.get(name);
    }

    public void clearMelodies() {
        melodies = new HashMap();
        sequences = new HashMap();
    } 

    public void clearInfos() {
        infos = new HashMap();
    }

    public void addInfo(String key, String line) {
        List info = (List)infos.get(key);
        if (info == null) {
            info = new ArrayList();
        }
        info.add(line);
        infos.put(key, info);
        resetInfosArray();
    }

    public List getInfo(String key) {
        return (List)infos.get(key);
    }
    public Iterator getInfoKeysIterator() {
        // later: sort the keys?
        return infos.keySet().iterator();
    }

    private void resetInfosArray() {
        int numInfoLines = 0;
        for(Iterator it = getInfoKeysIterator(); it.hasNext(); ) {
            String key = (String)it.next();
            numInfoLines++;
            List lines = getInfo(key);
            numInfoLines += lines.size();
        }
        int i=0;
        if (title != null) {
            numInfoLines++;
        }
        infosArray = new String[numInfoLines];
        if (title != null) {
            infosArray[i] = title;
            i++;
        }
        for(Iterator it = getInfoKeysIterator(); it.hasNext(); ) {
            String key = (String)it.next();
            List lines = getInfo(key);
            infosArray[i] = key;
            i++;
            for(Iterator lit = lines.iterator(); lit.hasNext(); ) {
                String line = (String)lit.next();
                infosArray[i] = "    " + line;
                i++;
            }
        }
    }
    
    /*
     * Sample sequence:
     *   sequence fourth_movement 
     *      timetype seconds
     *       ...(other sequence mods)
     *      start
     *         melody opening 20
     *         melody build 40
     *         sequence meander 120
     *         melody denouement 10
     *      end
     *   sequence ramper
     *      timetype rounds
     *      start
     *         melody build 20
     *         melody sustain continue
     *      end
     */
    private Sequence readSequence(TokenReader reader) throws IOException {
        String name = reader.readToken();
        Sequence sequence = new Sequence(name);
        //System.out.println("SEQUENCE " + name);
        readSequenceMods(reader, sequence);     // until "start"
        int localTimeType = sequence.timeType;
        boolean done = false;
        while (!done) {
            Playable pl = null;
            String token = reader.readToken();
            if (token.equalsIgnoreCase("end"))  {
                done = true;
            } else {
                String plname = reader.readToken();
                String timeStr = reader.readToken();
                if (token.equalsIgnoreCase("melody"))  {
                    pl = getOrCreateMelody(plname);
                } else if (token.equalsIgnoreCase("sequence"))  {
                    pl = getSequence(plname).duplicate();
                } else if (token.equalsIgnoreCase("scale"))  {
                    pl = getScale(plname);
                } else if (token.equalsIgnoreCase("note"))  {
                    pl = new Note(plname);
                }

                //System.out.println("  "  +token+  " " +plname+ " " + timeStr);
                int time;
                if (timeStr.equalsIgnoreCase("continue"))   {
                    time = Sequence.TIME_CONTINUE;
                } else  {
                    time = Integer.parseInt(timeStr);
                }
                if (pl != null) {
                    sequence.add(pl, time);
                }
            }
        }
        return sequence;
    } 

    private void readSequenceMods(TokenReader reader, Sequence sequence) throws IOException {
        String token = reader.readToken();
        while (token != null && !token.equalsIgnoreCase("start")) {
            if (token.equalsIgnoreCase("timetype")) {
                String timeTypeStr = reader.readToken();
                if (debugLevel > 0) {
                    System.out.println("  timetype: "  +timeTypeStr);
                }
                sequence.setTimeType(timeTypeStr);
            }
            token = reader.readToken();
        }
    }

    public Iterator getSequenceNames() {
        return sequences.keySet().iterator();
    }
    
    public Sequence getSequence(String name) {
        return (Sequence)sequences.get(name);
    }

    public void addSequence(String name, Sequence sequence) {
        sequences.put(name, sequence);
    }

    public void removeSequence(String name) {
        sequences.put(name, null);
    }

    public void clearSequences() {
        sequences = new HashMap();
    }     
    // increment time by dt units, assume forces are constant in given interval
    public void increaseTime(double dt) {
        for (int i = 0; i < nbodies; i++) {
            forces[i].zero();
        }

        Body.resetCollisions();
        for (int i = 0; i < nbodies; i++) {
            Body bodyi = bodies[i];
            if (bodyi != null && bodyi.alive)  {
                for (int j = 0; j < nbodies; j++) {
                    Body bodyj = bodies[j];
                    if (bodyj != null && bodyj.alive) {
                        if (i != j) {
                            forces[i].plusEquals(bodyj.forceTo(bodyi, gravFactorBody, repelFactorBody, true));
                            // note: this is symmetric, so we can cut the calc time in half by
                            //       cleverly applying the (negative) result to bodyj and
                            //       getting our loops right. 
                        }
                    }
                }
                for (int r = 0; r < nrocks; r++) {
                    Rock rock = rocks[r];
                    if (rock != null && rock.alive) {
                        forces[i].plusEquals(rock.forceTo(bodyi, gravFactorRock, repelFactorRock, true));
                    }
                }
            }
        }

        int nc = Body.getNumCollisions();
        if (nc > 0) {
            for(int i=0; i < nc; i++) {
                Body[] coll = Body.getCollision(i);
                Body b1 = coll[0];
                Body b2 = coll[1];
                b1.highlight(5);
                b2.highlight(5);
                if (!b1.isPlaying()) {
                    Note played = playNote(b1);
                    if (played != null) {
                        if (!played.isRest()) {
                            b1.setPlaying(10); //
                        }
                        //notifyNotePlayed(b1, b1.getPlayable(), played);
                    }
                }
                if (!b2.isPlaying()) {
                    Note played = playNote(b2);
                    if (played != null) {
                        if (!played.isRest()) {
                            b2.setPlaying(10); //
                        }
                        //notifyNotePlayed(b1, b1.getPlayable(), played);
                    }
                }
                b1.maybeMutate(b2, numCycles);
                b2.maybeMutate(b1, numCycles);

                // some sort of collision callback?
            }
        }

        for(int i = 0; i < nbodies; i++) {
            Body body = bodies[i];
            if (body != null && body.alive) {
                body.move(forces[i], dt, radius, radius);
            }
        }

        for(int j=0; j < nrocks; j++) {
            Rock rock = rocks[j];
            if (rock != null && rock.alive) {
                rock.maybeMove();
            }
        }
    }


    // draw the N bodies

    public void draw(Body[] bodyList, Rock[] rockList, int nbodies, int nrocks) {
        //System.out.println("O:draw nb: " + nbodies + " nr: " + nrocks + " @ " + System.currentTimeMillis());
        if (displayInfo != 0) {
            drawDisplayInfo();
            if (displayInfo > 0) {
                displayInfo--;
            }
        }

        if (showHelpInfo && helpInfo != null) {
            drawHelpInfo();
        }

        if (showForceField) {
            drawForceField(bodyList, rockList);
        }

        for (int r = 0; r < nrocks; r++) {
            Rock rock = rockList[r];
            if (rock != null && rock.alive) {
                rock.draw(drawer);
            }
        }
        for (int i = 0; i < nbodies; i++) {
            Body body = bodyList[i];
            if (body != null && body.alive) {
                body.draw(drawer);
                if (showBodyForces) {
                    Vect bodyForce = forces[i];
                    double mag = bodyForce.magnitude();
                    //if (i < 2) {
                        //System.out.print(i + ":: " + body.pos + " mag: " + mag);
                    //}
                    if (mag > 1. ) { //mag != 0.
                        mag = Math.log(mag);
                    }
                    //if (i < 2) {
                        //System.out.print(" after log: " + mag);
                    //}
                    mag = mag - minForce;
                    if (mag < 0) {
                        mag = 0;
                    }
                    //if (i < 2) {
                        //System.out.println(" after sub: " + mag);
                        //System.out.println(i + ":: F_b4: " + bodyForce);
                    //}
                    bodyForce.unitEquals();  
                    bodyForce.timesEquals(mag);
                    double range = maxForce - minForce;
                    //System.out.println("CYCLEMinForce: " + cycleMinForce + " CYCLEMaxForce: " + cycleMaxForce);
                    //System.out.println("MinForce: " + minForce + " MaxForce: " + maxForce + " range: " + range);
                    double factor = normalizedVectorLength / range;
                    //if (i < 2) {
                        //System.out.println(i + ":: min: " + minForce + " max: " + maxForce + " range: " + range);                        //System.out.println(i + ":: mag: " + mag + " F_aft: " + bodyForce + " fact[" + bodyForce.x*factor + ", " + bodyForce.y*factor + "]");
                    //}
                    drawVect(drawer, body.pos.x, body.pos.y, bodyForce, factor);

                }

                // perhaps this shold go with the bodies themselves?
                if (showVelocities) {
                    Vect v = body.v;
                    Vect vdt = v.times(dt);
                    drawer.setPenColor(velocityColor);
                    drawVect(drawer, body.pos.x, body.pos.y, vdt, velocityFactor);
                }
            }
        }
        
        int tminus = secondsLeft();
        if (tminus >= 0 && !winkingOut && tminus <= 5) {
            drawCountdown();
        }

        if (winkingOut) {
            drawDoomsayersLament();
        }

    }

    double minForce = 0;
    double maxForce = 27;
    double normalizedVectorLength;
    double gridSize;
    Vect[][] forceVs;
    public void drawForceField(Body[] bodyList, Rock[] rockList) {
        //System.out.println("DFF: nbodies " + nbodies + " nrocks " + nrocks);
        //System.out.println("   min: " + minForce + " max: " + maxForce);
        int numVectors = forceFieldGridVectors;
        double m = 1.0e14;

        //System.out.println("DFF: create force matrix.");
        Vect zeroV = new Vect();
        //System.out.println("Force vectors. norm: " + normalizedVectorLength);
        drawer.setPenColor(forceColor);
        for(int i=0; i < numVectors; i++) {
            double y = i * gridSize - radius;
            for(int j=0; j < numVectors; j++) {
                double x = j * gridSize - radius;
                forceVs[i][j].zero();
                Vect pos = new Vect(x, y);
                Body pointBody = new Body(pos, zeroV, m, 1);
                //System.out.print(" pos: " + pos);
                for(int bs = 0; bs < nbodies; bs++) {
                    Body body = bodyList[bs];
                    if (body != null && body.alive) {
                        forceVs[i][j].plusEquals(body.forceTo(pointBody, gravFactorBody, repelFactorBody, false)); // don't check for collisions when drawing force field
                    }
                }
                for(int rs = 0; rs < nrocks; rs++) {
                    Rock rock = rockList[rs];
                    if (rock != null && rock.alive) {
                        forceVs[i][j].plusEquals(rock.forceTo(pointBody, gravFactorRock, repelFactorRock, false)); // don't check for collisions when drawing force field
                    }
                }
            }
            //System.out.println();
        }
        //normalize the force vectors
        //System.out.println("DFF: normalize the force vectors.");
        double cycleMaxForce = 0.;
        double cycleMinForce = 100.e100;
        for(int i=0; i<numVectors; i++) {
            for(int j=0; j<numVectors; j++) {
                double mag = forceVs[i][j].magnitude();
                //System.out.println(" M: " + mag);
                if (mag > 1.) { //mag != 0.
                    mag = Math.log(mag);
                }
                //System.out.print("(" + i + ", " + j + ")=> " + forceVs[i][j] + " = " + mag);
                if (mag > cycleMaxForce) {
                    cycleMaxForce = mag;
                }
                if (mag < cycleMinForce) {
                    cycleMinForce = mag;
                }
            }
            //System.out.println();
        }
        double cyclesToAverageForceMaxima = 30;
        maxForce = maxForce * (cyclesToAverageForceMaxima - 1) / cyclesToAverageForceMaxima;
        maxForce += cycleMaxForce / cyclesToAverageForceMaxima;
        minForce = minForce * (cyclesToAverageForceMaxima - 1) / cyclesToAverageForceMaxima;
        minForce += cycleMinForce / cyclesToAverageForceMaxima;
        double range = maxForce - minForce;
        //System.out.println("CYCLEMinForce: " + cycleMinForce + " CYCLEMaxForce: " + cycleMaxForce);
        //System.out.println("MinForce: " + minForce + " MaxForce: " + maxForce + " range: " + range);
        double factor = normalizedVectorLength / range;

        //System.out.println("DFF: drawing.");
        for(int i=0; i<numVectors; i++) {
            double y = i * gridSize - radius;
            for(int j=0; j<numVectors; j++) {
                double x = j * gridSize - radius;
                Vect force = forceVs[i][j];
                double mag = force.magnitude();
                if (mag != 0.) {
                    mag = Math.log(mag);
                }
                mag = mag - minForce;
                //System.out.print(" F_b4: " + force);
                force.unitEquals();
                force.timesEquals(mag);
                //System.out.println(" mag: " + mag + " F_aft: " + force + " fact[" + force.x*factor + ", " + force.y*factor + "]");
                //System.out.print("<");
                drawVect(drawer, x, y, force, factor);
                //System.out.print(">");
            }
            //System.out.println("#");
        }
        //System.out.println("DFF: done.");
    }

    public void drawVect(StdDraw drawer,
                         double x, double y,
                         Vect vect,
                         double factor) {
        double xend = x + (vect.x) * factor;
        double yend = y + (vect.y) * factor;
        drawer.line(x, y, xend, yend);
        drawer.circle(xend, yend, 150.);
    }

    public void drawVectWithHandle(StdDraw drawer,
                                   double x, double y,
                                   Vect vect,
                                   double factor,
                                   double handleSize) {
        double xend = x + (vect.x) * factor;
        double yend = y + (vect.y) * factor;
        drawer.line(x, y, xend, yend);
        drawer.filledSquare(xend, yend, handleSize);
    }

    public void drawDisplayInfo() {
        if (infosArray == null) {
            return;
        }
        double line_height = drawer.getLineHeight();
        double textY = -.995 * radius + infosArray.length * line_height;
        drawer.set_text_point(-.98 * radius, textY);
        drawer.setPenColor(infoColor);
        for(int i=0; i < infosArray.length; i++) {
            drawer.text_line(infosArray[i]);
            drawer.next_line();
        }
    }

    public void drawCountdown() {
        drawer.set_text_point(.45 * radius, .97*radius);
        drawer.next_line(); // go down one from the title. 
        drawer.setPenColor(countdownColor);
        int tminus = secondsLeft();
        drawer.text_line("World ends in " + tminus + " seconds.");
    }

    public void drawDoomsayersLament() {
        drawer.set_text_point(.45 * radius, .97*radius);
        if (doomsayersLament != null) {
            drawer.setPenColor(countdownColor);
            for(int i=0; i < doomsayersLament.length; i++) {
                drawer.next_line(); // go down one from the title. 
                drawer.text_line(doomsayersLament[i]);
            }
        }
    }

    public void chooseRandomDoomsayersLament() {
        if (doomsayersLaments == null) {
            setupDoomsayersLaments();
        }
        doomsayersLament = doomsayersLaments[(int)(doomsayersLaments.length * Math.random())];
    }

    private void setupDoomsayersLaments() {
        doomsayersLaments = new String[][] {
            { "...and one by one, ", "   the stars started to wink out."},
            {"It's the end of the world", "as we know it."},
            {"...and I feel fine."},
            {"The end is nigh!"}
        };
    }
    
    public void drawBodyStats(Body body) {
        drawer.set_text_point(-.97 * radius, -.90*radius);
        drawer.text_line("Mass: " + numFormat(body.mass));
        drawer.next_line(); 
        String loc;
        if (body.posSpecifiedPolar) {
            loc = "Pos:  {r: " + numFormat(body.pos.r()) +
                ", theta: " + numFormat(body.pos.thetaDegrees()) + "}";
        } else {
            loc = "Pos:  {X: " + numFormat(body.pos.x) +
                ", Y: " + numFormat(body.pos.y) + "}";
        }
        drawer.text_line(loc);
        drawer.next_line();
        if (! (body instanceof Rock)) {
            String vel;
            if (body.vSpecifiedPolar) {
                vel = "Vel:  {r: " + numFormat(body.v.r()) +
                    ", theta: " + numFormat(body.v.thetaDegrees()) + "}";
            } else {
                vel = "Vel:  {X: " + numFormat(body.v.x) +
                    ", Y: " + numFormat(body.v.y) + "}";
            }
            drawer.text_line(vel);
        }
    }

    protected NumberFormat doublesFormat;
    protected NumberFormat eFormat;
    private void setupNumberFormats() {
        doublesFormat = NumberFormat.getInstance();
        doublesFormat.setMaximumFractionDigits(4);
        doublesFormat.setGroupingUsed(false);
        
        eFormat = (DecimalFormat)NumberFormat.getInstance();
        ((DecimalFormat)eFormat).applyPattern("0.####E0");

    }

    
    public String numFormat(double num) {
        if (num > 1.E6) {
            return eFormat.format(num);
        } else {
            return doublesFormat.format(num);
        }
    }

    public int secondsLeft() {
        if (playCycles <= 0) {
            return -1;
        }
        int cyclesLeft = playCycles - numCycles;
        return (int)cyclesToSeconds(cyclesLeft);
    }
    
    public void drawHelpInfo() {
        drawer.set_text_point(-.98 * radius, 0.);
        drawer.setPenColor(titleColor);
        for(Iterator it = helpInfo.iterator(); it.hasNext(); ) {
            String line = (String)it.next();
            drawer.text_line("   " + line);
            drawer.next_line();
        }
    }

    public void redrawBackground() {
        drawer.clearbg(bgColor);
        drawer.initbg(bgColor);
        drawer.show(true);
        repaint();
    }


    public void draw_slowly(int delay) {
        if (displayInfo != 0) {
            drawDisplayInfo();
        }
        if (showForceField) {
            drawForceField(bodies, rocks);
        }
        for (int r = 0; r < nrocks; r++) {
            Rock rock = rocks[r];
            if (rock != null) {
                rock.draw(drawer);
                drawer.show((int)(delay/4));
                repaint();
            }
        }
        for (int i = 0; i < nbodies; i++) {
            Body body = bodies[i];
            if (body != null) {
                body.draw(drawer);
                drawer.show(delay);
                repaint();
            }
        }
    } 



    public void clearBG() {
        drawBG_blank();
    }
    public void drawBG(String background) {
        if (background.equals("radar")) {
            drawBG_radar();
        } else if (background.equals("square")) {
            drawBG_square();
        } else if (background.equals("circle")) {
            drawBG_circle();

        } else if (background.equals("stars")) {
            drawBG_stars();

        } else if (background.equals("starclusters") || background.equals("starclusters_circle")) {
            drawStarClusters(50, 55, 35000.);

        } else if (background.equals("starclusters_square")) {
            drawStarClusters_square(45, 55, 35000.);

        } else if (background.equals("none") || background.equals("blank") || background.equals("clear")) {
            drawBG_blank();
        } else {
            drawBG_radar();
        }
        drawTitle_bg();
    }
    
    private void drawTitle_bg() {
        if (title != null) {
            drawer.setPenColor_bg(titleColor);
            drawer.text_bg(-.98 * radius, .97*radius, title);
        }
    }

    private void drawTitle_fg() {
        if (title != null) {
            drawer.setPenColor(titleColor);
            drawer.text(-.98 * radius, .97*radius, title);
        }
    }

    
    public void drawBG_radar() {
        drawBG_circle();
        drawgrid();
    }

    public void drawBG_stars() {
        drawStars();
    }

    public void drawBG_blank() {
        drawer.setPenColor_bg(bgColor);
        drawer.filledSquare_bg(0.0, 0.0, radius * 1.15);
    }

    public void drawBG_circle() {
        drawer.setPenColor_bg(circleColor);
        drawer.filledCircle_bg(0.0, 0.0, radius * 1.02);
        drawer.setPenColor_bg(circleBorderColor);
        drawer.setPenRadius_bg(.005);
        drawer.circle_bg(0.0, 0.0, radius * 1.02);
    }

    public void drawBG_square() {
        drawer.setPenColor_bg(circleColor);
        drawer.filledSquare_bg(0.0, 0.0, radius * 1.02);
        drawer.setPenRadius_bg(.015);
        drawer.setPenColor_bg(circleBorderColor);
        drawer.square_bg(0.0, 0.0, radius * 1.02);
    }

    public void drawStars() {
        int numStars = (int)randomRange(175., 550.);
        for(int i=0; i < numStars; i++) {
            double x = randomRange(-1. * radius, radius);
            double y = randomRange(-1. * radius, radius);
            drawRandomStar(x, y, 40., 250.);
        }
    }

    public void drawStarClusters(int clusters, int starsPer, double clusterSizeRange) {
        int numClusters = randomRange(5, clusters);
        for(int i=0; i < numClusters; i++) {
            int perCluster = randomRange(starsPer / 4, starsPer);
            double clusterSize = randomRange(0., clusterSizeRange);
            double r = randomRange(clusterSize, radius);
            double theta = randomRange(0., 360.);
            Vect offset = Vect.createPolarDegrees(r, theta);

            drawStarCluster(offset, clusterSize, perCluster);
        }
    }

    public void drawStarClusters_square(int clusters, int starsPer, double clusterSizeRange) {
        int numClusters = randomRange(5, clusters);
        for(int i=0; i < numClusters; i++) {
            int perCluster = randomRange(starsPer / 4, starsPer);
            double clusterSize = randomRange(0., clusterSizeRange);
            double x = randomRange(-1. * radius, radius);
            double y = randomRange(-1. * radius, radius);

            Vect offset = new Vect(x, y);
            drawStarCluster(offset, clusterSize, perCluster);
        }
    }

    public void drawStarCluster(Vect offset, double clusterSize, int stars) {
        for(int i=0; i < stars; i++) {
            double rStar = randomRange(0., clusterSize);
            double thetaStar = randomRange(0., 360.);
            Vect starVect = Vect.createPolar(rStar, thetaStar);
            starVect.plusEquals(offset);
            double x = starVect.x();
            double y = starVect.y();
            drawRandomStar(x, y, 20., 200.);
        }
    }
    public void drawRandomStar(double x, double y, double weightLo, double weightHi) {
        double weight = randomRange(weightLo, weightHi);
        float brite = randomRange(0.f, .7f);
        float sat = randomRange(0.f, .25f);
        float hue = randomRange(0.f, 1.f);
        Color starColor = Color.getHSBColor(hue, sat, brite);
        drawer.setPenColor_bg(starColor);
        drawer.filledCircle_bg(x, y, weight);
    }

    public void drawgrid() {
        double gridradius = radius * 1.02;
        drawer.setPenColor_bg(gridColor);
        drawer.setPenRadius_bg(.001);
        for(int i=0; i < gridCircles; i++) {
            drawer.circle_bg(0.0, 0.0, (double)i * gridradius / gridCircles);
        }
        for(int j = 0; j < gridRadii; j++) {
            double thetaR = j * 2. * Math.PI / gridRadii;
            Vect rad = Vect.createPolar(gridradius, thetaR);
            drawer.line_bg(0., 0., rad.x(), rad.y());
        }
    }

    Rock dragRock = null;
    Body dragBody = null;
    Body selectedBody = null;
    boolean dragChangesMassOnSelectedBody = false;
    public boolean dragChangesVelocityOnSelectedBody = false;
    double selectedBodyOriginalMass = 0.;
    double selectedBodyOriginalControlBoxDiagonal = 0.;

    //
    // attempt to detect a body being flung. figure out approx vector
    // of motion and impart that as the new velocity upon release.
    //
    private int numDragPoints  = 5;
    private Vect[] dragPoints  = new Vect[numDragPoints];
    private void resetDragPoints(Body body) {
        for(int i=0; i < numDragPoints; i++) {
            dragPoints[i] = body.pos;
        }
    }

    private void addDragPoint(double mx, double my) {
        for(int i=numDragPoints-1; i > 0; i--) {
            dragPoints[i] = dragPoints[i-1];
        }
        dragPoints[0] = new Vect(mx, my);
    }

    // TODO: the velocityDragFactor might need to be calculated from dt & time elapsed
    //       between drag points.   
    private double velocityDragFactor = 1.5;
    private Vect getDragVelocity() {
        Vect vel = new Vect(0., 0.);
        for(int i=0; i <numDragPoints - 1; i++) {
            Vect delta = dragPoints[i].minus(dragPoints[i+1]);
            vel.plusEquals(delta);
        }
        vel.timesEquals(1.0 / (numDragPoints - 1));
        vel.timesEquals(velocityDragFactor / dt);
        return vel;
    }
    
    public void selectBody(Body body) {
        selectedBody = body;
        if (body != null) {
            selectedBodyOriginalMass = body.mass;
            selectedBodyOriginalControlBoxDiagonal =
                selectedBody.getControlBoxWidth() * Math.sqrt(2.);
        }
    }
    
    public void mousePressed (MouseEvent e) {
        resetPlayTimer();
        if (mode == INITIAL_CONDITIONS_MODE) {
            mousePressed_InitialConditionsMode(e);
        } else {
            mousePressed_PlayMode(e);
        }
    }

    public void mousePressed_InitialConditionsMode(MouseEvent evt) {
        if (worldBuilder == null) {
            return;
        }
        if (debugLevel > 0) {
            System.out.println("Bing! [INITCOND]");
        }
        worldBuilder.setMousePressedTime(System.currentTimeMillis());
        double mx = drawer.mouseX();
        double my = drawer.mouseY();
        // TODO: if option key is down and a body is selected, clone it.
        if (evt.isControlDown() && evt.isAltDown()) {
            worldBuilder.controlAltMousePressed(mx, my);
            return;
        } else if (evt.isControlDown()) {
            worldBuilder.controlMousePressed(mx, my);
            return;
        } else if (evt.isMetaDown()) {
            worldBuilder.metaMousePressed(mx, my);
            return;
        } else if (evt.isAltDown()) {
            worldBuilder.altMousePressed(mx, my);
            return;
        } else if (evt.isShiftDown()) {
            worldBuilder.shiftMousePressed(mx, my);
        }

        //
        // if we're in a paused state, and
        // there's a selected rock or body, give first precedence to selecting the
        // control handle on that rockorbody's control box.
        //
        if (paused && selectedBody != null) {
            if (closeToVelocityVector(selectedBody, mx, my)) {
                dragChangesVelocityOnSelectedBody = true;
                worldBuilderDraw();
                return;

            } else if (closeToControlHandle(selectedBody, mx, my)) {
                dragChangesMassOnSelectedBody = true;
                selectedBody.highlightControlBox(true);
                selectedBodyOriginalMass = selectedBody.mass;
                selectedBodyOriginalControlBoxDiagonal =
                    selectedBody.getControlBoxWidth() * Math.sqrt(2.);
                worldBuilderDraw();
                return;
            }
        }

        //
        // select something if no modifiers are pressed
        //
        int selectedIndex = findRockIndex(mx, my, initialRocks, nrocks_initial);
        if (selectedIndex >= 0) {
            Rock rock = initialRocks[selectedIndex];
            //System.out.println("WB: [INIT] FOUND Rock[" + selectedIndex +  "]" + rock);
            /* debug
            System.out.print("      Rocks: ["); for(int i=0; i < nrocks; i++) { System.out.print(rocks[i] + "{" + rocks[i].pos + "} ");} System.out.println("]");
            System.out.print("      INITRocks: ["); for(int i=0; i < nrocks_initial; i++) { System.out.print(initialRocks[i] + "{" + initialRocks[i].pos + "} ");} System.out.println("]");
            */
            dragRock = rock;
            dragRock.select(-1);
            worldBuilder.selectRock(dragRock, selectedIndex);
            worldBuilderDraw();
        } else {
            selectedIndex = findBodyIndex(mx, my, initialBodies, nbodies_initial);
            if (selectedIndex >= 0) {
                Body body = initialBodies[selectedIndex];
                if (debugLevel > 0) {
                    System.out.println("WB: [INIT] FOUND Body[" + selectedIndex +  "]" + body);
                }
                dragBody = body;
                dragBody.select(-1);
                worldBuilder.selectBody(dragBody, selectedIndex);
                worldBuilderDraw();
            } else {
                if (debugLevel > 0) {
                    System.out.println("MISSED");
                }
                worldBuilder.unselect();
                worldBuilderDraw();
            }
        }
    }
    
    public void mousePressed_PlayMode(MouseEvent e) {
        if (debugLevel > 0) {
            System.out.println("Bing!");
        }
        double mx = drawer.mouseX();
        double my = drawer.mouseY();

        //
        // if we're in a paused state, and
        // there's a selected rock or body, give first precedence to selecting the
        // control handle on that rockorbody's control box.
        //
        if (paused && selectedBody != null) {
            if (closeToVelocityVector(selectedBody, mx, my)) {
                dragChangesVelocityOnSelectedBody = true;
                redraw();
                return; 

           } else if (closeToControlHandle(selectedBody, mx, my)) {
                dragChangesMassOnSelectedBody = true;
                selectedBody.highlightControlBox(true);
                selectedBodyOriginalMass = selectedBody.mass;
                selectedBodyOriginalControlBoxDiagonal = selectedBody.getControlBoxWidth() * Math.sqrt(2.);
                redraw();
                return;
            }
        }

        int selectedIndex = findRockIndex(mx, my, rocks, nrocks);
        if (selectedIndex >= 0) {
            Rock rock = rocks[selectedIndex];
            if (debugLevel > 0) {
                System.out.println("WB: FOUND Rock");
                System.out.print("      Rocks: ["); for(int i=0; i < nrocks; i++) { System.out.print(rocks[i] + "{" + rocks[i].pos + "} ");} System.out.println("]");
                System.out.print("      INITRocks: ["); for(int i=0; i < nrocks_initial; i++) { System.out.print(initialRocks[i] + "{" + initialRocks[i].pos + "} ");} System.out.println("]");
            }
            dragRock = rock;
            dragRock.select(-1); 
            worldBuilder.selectRock(dragRock, selectedIndex);
       } else {
            selectedIndex = findBodyIndex(mx, my, bodies, nbodies);
            if (selectedIndex >= 0) {
                Body body = bodies[selectedIndex];
                if (body != null && body.alive) {
                    if (debugLevel > 0) {
                        System.out.println("FOUND Body");
                    }
                    body.select(-1);
                    dragBody = body;
                    resetDragPoints(body);
                    worldBuilder.selectBody(dragBody, selectedIndex);
                    redraw();
                } else {
                    if (debugLevel > 0) {
                        System.out.println("HIT DEAD BODY!");
                    }
                    worldBuilder.unselect();
                    redraw();
                }
            } else {
                if (debugLevel > 0) {
                    System.out.println("MISSED");
                }
                worldBuilder.unselect();
                redraw();
            }
        }           
    }

    public void mouseClicked (MouseEvent e) { }
    public void mouseEntered (MouseEvent e) { }
    public void mouseExited  (MouseEvent e) { }
    public void mouseReleased(MouseEvent e) {
        resetPlayTimer();
        dragRock = null;
        dragBody = null;
        dragChangesMassOnSelectedBody = false;
        dragChangesVelocityOnSelectedBody = false;

        if (selectedBody != null) {
            selectedBody.highlightControlBox(false);
        }
        if (paused) {
            if (mode == INITIAL_CONDITIONS_MODE) {
                worldBuilderDraw();
                worldBuilder.updateSelected();
            } else {
                redraw();
            }
        } else {
            if (selectedBody != null &&
                mode == PLAYING_MODE &&
                !(selectedBody instanceof Rock)) {
                Vect newVel = getDragVelocity();
                selectedBody.setV(newVel);
            }
        }
    }
    public void mouseMoved(MouseEvent e) { }

    public void mouseDragged(MouseEvent e) {
        if (mode == INITIAL_CONDITIONS_MODE) {
            mouseDragged_InitialConditionsMode(e);
        } else {
            mouseDragged_PlayMode(e);
        }
    }
    
    public void mouseDragged_PlayMode(MouseEvent e) {
        // TODO: do we need the dragWaitDelay here?
        if (drawer.mousePressed()) {
            double mx = drawer.mouseX();
            double my = drawer.mouseY();
            if (dragChangesVelocityOnSelectedBody && selectedBody != null) {
                double deltax = mx - selectedBody.pos.x();
                double deltay = my - selectedBody.pos.y();
                double factor = ctlVelocityFactor * dt;
                selectedBody.v.setX(deltax / factor);
                selectedBody.v.setY(deltay / factor);
                redraw();
                return;
            } else if (dragChangesMassOnSelectedBody && selectedBody != null) {
                double deltax = mx - selectedBody.pos.x();
                double deltay = my - selectedBody.pos.y();
                double dist = Math.sqrt(deltax * deltax + deltay * deltay);
                double ratio = dist / selectedBodyOriginalControlBoxDiagonal;
                //System.out.println("HANDLEDRAG dist: " + dist + " cbDiag:
                double newMass = selectedBodyOriginalMass * ratio;
                if (Body.massToSizeMethod == Body.M2SM_LINEAR) {
                    // new mass is fine if we're using linear.
                } else if (Body.massToSizeMethod == Body.M2SM_CUBE_ROOT) {
                    double third = 1. / 3.;
                    double originalMassCubeRoot = Math.pow(selectedBodyOriginalMass, third);
                    double newMassCubeRoot = originalMassCubeRoot * ratio;
                    newMass = newMassCubeRoot * newMassCubeRoot * newMassCubeRoot;
                }
                selectedBody.setMass(newMass);
                redraw();
                return;
            }
            if (dragRock != null) {
                if (dragRock.constrainMovesToRadius()) {
                    dragRock.moveto_constrainRadius(mx, my);
                } else {
                    dragRock.moveto(mx, my);
                    if (paused) {
                        redraw();
                    }
                }
            } else if (dragBody != null) {
                dragBody.moveto(mx, my);
                if (paused) {
                    redraw();
                } else {
                    if (debugLevel > 0) {
                        System.out.println("AddDragPoint: (" + mx + ", " + my + ")");
                    }
                    addDragPoint(mx, my);
                }
            }
        }
    }    

    public void mouseDragged_InitialConditionsMode(MouseEvent e) {
        if (drawer.mousePressed()) {
            double mx = drawer.mouseX();
            double my = drawer.mouseY();
            if (dragChangesVelocityOnSelectedBody && selectedBody != null) {
                double deltax = mx - selectedBody.pos.x();
                double deltay = my - selectedBody.pos.y();
                double factor = ctlVelocityFactor * dt;
                selectedBody.v.setX(deltax / factor);
                selectedBody.v.setY(deltay / factor);
                worldBuilderDraw();
                return;

            } else if (dragChangesMassOnSelectedBody && selectedBody != null) {
                double deltax = mx - selectedBody.pos.x();
                double deltay = my - selectedBody.pos.y();
                double dist = Math.sqrt(deltax * deltax + deltay * deltay);
                double ratio = dist / selectedBodyOriginalControlBoxDiagonal;
                System.out.println("HANDLEDRAG dist: " + dist);
                selectedBody.setMass(selectedBodyOriginalMass * ratio);
                worldBuilderDraw();
                return;
            }
            //System.out.println("WB DRAG. [INIT COND]" + mx + ", " + my);
            if (worldBuilder != null) {
                worldBuilder.mouseDragged(mx, my);
            }
        }
    }

    public int findRockIndex(double x, double y, Rock[] rockList, int nrocks) {
        for(int i=0; i < nrocks; i++) {
            Rock rock = rockList[i];
            if (rock != null && rock.intersects(x, y, 1.5)) {
                return i;
            }
        }
        return -1;
    }
    
    public int findBodyIndex(double x, double y, Body[] bodyList, int nbodies) {
        for(int i=0; i < nbodies; i++) {
            Body body = bodyList[i];
            if (body != null && body.intersects(x, y, 2.0)) {
                return i;
            }
        }
        return -1;
    }

    public boolean closeToControlHandle(Body body, double x, double y) {
        return body.controlBoxHandleIntersects(x, y, 4.);
    }


    public boolean closeToVelocityVector(Body body, double x, double y) {
        if (body instanceof Rock) {
            return false;
        }
        Vect vdt = body.v.times(dt * ctlVelocityFactor);
        boolean result = body.pointIsClose(x, y, body.pos.x + vdt.x, body.pos.y + vdt.y, 4 * body.baseRadius);
        //System.out.println("Close to Vel. vdt: (" + vdt.x + ", " + vdt.y + ") (x,y)=( " + x + ", " +  y + ") pos: (" + body.pos.x + ", " + body.pos.y + ")     target: {" + (body.pos.x + vdt.x) + ", " + (body.pos.y + vdt.y) + "}  DELTA: [" + (body.pos.x + vdt.x - x)  + ", " + (body.pos.y + vdt.y - y) + "]  fudge: " + 2 * body.baseRadius + " ==> " + result );
        return result;
    }

    public Note playNote(Body b) {
        if (muted) {
            return null;
        }

        Playable playable = b.getPlayable();
        Note note = null;
        if (playable != null) {
            note = playable.nextNote(b, this);
        }
        return playNote(b, note, b.getChannel());
    }

    //TODO: make this into a Player interface
    //TODO: add notion of note duration 
    public Note playNote(Body b, Note note, int channel) {
        if (muted || note == null) {
            return null;
        }
        Vect v = b.getVelocity();
        if (v != null) {
            if (note.pitch > 0 || note.pitch == Note.REST_STOP) {
                if (debugLevel > 0) {
                    System.out.println("Note: " + note + "-" + channel + " [q: " + channelQueues[channel].size() +  " p:" + note.getPitch() + ", v:" + note.getVelocity() + "] BodyV: " + b.getVelocity().magnitude());
                }
            }
            float midiVel = (float)Math.max(midiVelocity_min[channel],
                                            Math.min(v.magnitude(), midiVelocity_max[channel]));
            note.setVelocity(midiVel);
        } else {
            if (debugLevel > 0) {
                System.out.println("Note: " + note + "-" + channel + " [p:" + note.getPitch() + ", v:" + note.getVelocity() + "] ROCK");
            }
        }

        return playNote(note, channel);
    }

    public Note playNote(Note note, int channel) {
        // TODO: migrate this into a particular player, or else generalize the
        //       note off queues with the note duration..
        // TODO: use the sequencer somehow if there are note durations?
        if ((int)note.pitch != Note.REST) {
            ChannelQueue q = channelQueues[channel];
            q.nq((int)note.pitch);
            // System.out.println("  ch: " + channel + " full after nq: " + q.full() + " s: " + q.size() + " top: " + q.top + " cur: " + q.cursor);
            if (q.full()) {
                int pitch = q.dq();
                if (debugLevel > 0) {
                    System.out.println("NoteOFF: " + pitch + "[ch: " + channel + "]");
                }
                midiStuff.noteOff(pitch, channel);
            }
        }
        long beatTime = BEAT_TIME_NOW;
        if (beatLocked) {
            beatTime = calculateBeatLocking(note, true);
        }
        midiStuff.playNote(note, channel, beatTime);
        return note;
        /*
        if (note.pitch > 0) {
            return true;
        } else {
            return false;
        }
        */
    }

    // mostly for previewing melodies & such.
    public void playNote(Note note, int channel, int durationMS) {
        try {
            playNote(note, channel);
            //System.out.println("o.playNote. played. now sleep for " + durationMS + ". current thread: " + Thread.currentThread());
            Thread.sleep(durationMS);
        } catch (InterruptedException ex) {
        }
    }

    /**
     *
     */
    public long calculateBeatLocking(Note note, boolean modifyVelocity) {
        long now = midiStuff.getMicrosecondPosition();
        long songPlayTime = now - t0_songStart;
        long timeInMeasure = songPlayTime % measureMs;
        int quants = (int)(timeInMeasure / quantMs);
        long deltaQ = timeInMeasure - quants * quantMs;
        long beatPlayTime = BEAT_TIME_NOW;
        
        System.out.println("BEAT LOCK. timeInMeasure: " + timeInMeasure + " quants: " + quants + " deltaQ: " + deltaQ + " quantMs: " + quantMs);
        if (deltaQ > .1 * quantMs && deltaQ < .9 * quantMs) { // TODO: add fudge factor?
            quants += 1;
            long numMeasures = (long)(songPlayTime / measureMs);
            long songMeasureTime = numMeasures * measureMs;
            beatPlayTime = t0_songStart + songMeasureTime + quants * quantMs;
            System.out.println("  now: " + now + " BPTime: " + beatPlayTime + " later: " + (beatPlayTime - now));
        }
        if (modifyVelocity) {
            if (quants == 0) {
                note.velocity *= measureBump;
            } else if (quants == halfMeasureQuants) {
                note.velocity *= halfMeasureBump;
            } else if (quants % beatQuants == 0) {
                note.velocity *= beatBump;
            }
        }
        return beatPlayTime;
    }


    public void notifyNotePlayed(Body body, Playable pl, Note note) {
        if (noteListeners != null) {
            for(Iterator it = noteListeners.iterator(); it.hasNext(); ){
                NoteListener ear = (NoteListener)it.next();
                ear.notePlayed(body, pl, note);
            }
        }
    }

    public void addNoteListener(NoteListener nl) {
        if (noteListeners == null) {
            noteListeners = new ArrayList();
        }
        noteListeners.add(nl);
    }
    
    public void preview(Playable playable, int channel) {
        preview(playable, channel, 200, 700);
    }

    public boolean previewing = false;

    public void preview(Playable playable, int channel, int durationLow, int durationHi) {
        System.out.println(" o:Preview: current thread: " + Thread.currentThread());
        System.out.println(" o:Preview. playable [" + playable.getType() + " ]: " + playable.getName());
        if (playable instanceof Note) {
            playNote((Note)playable, channel, 500);
        } else {
            previewing = true;
            playable.reset();
            while(previewing && !playable.atEnd()) {
                int durationMS = randomRange(durationLow, durationHi);
                Note note = playable.nextNote(null, this);
                if (debugLevel > 0) {
                    System.out.println("    playing note: " + note + " dur: " + durationMS);
                }
                playNote(note, channel, durationMS);
                if (debugLevel > 0) {
                    System.out.println("      played note. previewing=" + previewing);
                }
            }
            previewing = false;
            midiAllOff();
        }
    }

    public void stopPreview() {
        System.out.println("Orrery: stopPreview...");
        System.out.println(" o:stopPreview: current thread: " + Thread.currentThread());
        previewing = false;
    }

    public void cycle(double dt) {
        //System.out.print(". ");
        numCycles ++;
        drawer.clearbg(bgColor);
        increaseTime(dt); 
        draw(bodies, rocks, nbodies, nrocks);
        if (trails > 0) {
            drawTitle_fg();
        }
        drawer.show(true);
        repaint();
    }

    public void redraw() {
        // to prevent deadlocks, if the system is running, (i.e. not paused), we
        // don't need to do the redraw, because it will happen by itself.
        if (paused) {
            // and if we're paused, we need to give a little time to the cycle() thread
            // to finish the latest redraw, then do our drawing.
            try {
                Thread.sleep(100);
            } catch (Exception ex) {}
            drawer.clearbg(bgColor);
            draw(bodies, rocks, nbodies, nrocks); 
            drawer.show(true);
            repaint();
        }
    }

    ////////////////////////
    //// Drawing Thread ////
    ////////////////////////

    protected DrawingThread drawingThread = null;
    public void startDrawingThread(String filename) {
        System.out.println("start drawing thread(" + filename + "). drawingThread: " + drawingThread);
        if (drawingThread == null) {
            drawingThread = new DrawingThread(this);
            System.out.println("created drawing thread. ");
        }
        drawingThread.worldfile = filename;
        System.out.println("starting drawing thread: " + drawingThread);
        drawingThread.start();
        System.out.println("started drawing thread: " + drawingThread);
    }

    public void stopDrawingThread() {
        System.out.println("Orrery: stop drawing thread");
        if (drawingThread != null) {
            drawingThread.halt();
            drawingThread = null;
        }
    }

    public void resetPlayTimer() {
        // TODO: this might have a funny interaction with wingingOut.
        numCycles = 0;
    }
    
    class DrawingThread extends Thread {
        public boolean drawing = false;
        protected Orrery orrery;
        public String worldfile;
        public DrawingThread(Orrery orrery) {
            this.orrery = orrery;
        }
        public synchronized void run() {
            try {
                if (debugLevel > 0) {
                    System.out.println("DrawingThread.run. worldfile: " + worldfile);
                }
                drawing = true;
                paused = false;
                numCycles = 0;
                randomizePlayCycles();
                if (debugLevel > 0) {
                    System.out.println("ORRERY. playTimeRange: {" + playTimeLo + ", " + playTimeHi + "} playCycles: " + playCycles);
                }
                double dt = orrery.dt;
                if (orrery.local_dt > 0.) {
                    dt = orrery.local_dt;
                }
                t0_songStart = midiStuff.getMicrosecondPosition();
                while (drawing) {
                    if (playCycles > 0 && numCycles >= playCycles && !winkingOut) {
                        numCycles = 0;
                        winkOut();
                    } else if (!paused) {
                        long beforeCycleMs = System.currentTimeMillis();
                        orrery.cycle(dt);
                        long afterCycleMs = System.currentTimeMillis();
                        long actualCycleTime = afterCycleMs - beforeCycleMs;
                        if (orrery.cycleTime > 0 && actualCycleTime < orrery.cycleTime) {
                            //System.out.print("(" + actualCycleTime + "#" + orrery.cycleTime + ") ");
                            try { Thread.sleep(orrery.cycleTime - actualCycleTime); }
                            catch (InterruptedException e) { System.out.println("Error sleeping"); }
                        } else {
                            //System.out.print("(" + actualCycleTime + "|" + orrery.cycleTime + ") ");
                        }
                        //if (numCycles % 10 == 0) {
                        //    System.out.println();
                        //}
                    } else {
                        //System.out.println("paused.. waiting playCycles=" + playCycles + " numCycles=" + numCycles);
                        try { Thread.sleep(400); }
                        catch (InterruptedException e) { System.out.println("Error sleeping"); }
                    }                   
                }
            } catch (Exception ex) {
                System.err.println("Drawing Thread caught exception: " + ex);
                ex.printStackTrace(System.err);
            }
        }

        public  void halt() {
            if (debugLevel > 0) {
                System.out.println("\n\n\n======OrreryDrawingTHread   HALT!!!\n\n\n");
            }
            drawing = false;
        }
    }

    public void togglePause() {
        setPaused(paused);
    }
    
    public void setPaused(boolean val) {
        paused = val;
        if (paused) {
            midiAllOff();
            redraw();
        }
    }
    
    public boolean paused() {
        return paused;
    }

    public void toggleInfoDisplay() {
        if (displayInfo == -1) {
            displayInfo = 0;
        } else if (displayInfo == 0) {
            displayInfo = -1;
        }
    }
    
    public void showInfoDisplay(boolean val) {
        if (val) {
            displayInfo = -1;
        } else {
            displayInfo = 0;
        }
    }

    public void setDisplayInfo(int cycles) {
        displayInfo = cycles;
    }

    public void setDisplayInfoSeconds(float seconds) {
        displayInfo = secondsToCycles(seconds);
    }

    public void setInitialDisplayInfoSeconds(float seconds) {
        initialInfoDisplaySeconds = seconds;
    }
      
    public void showHelpInfo(List helpInfo) {
        this.helpInfo = helpInfo;
        this.showHelpInfo = true;
    }

    public void hideHelpInfo() {
        this.showHelpInfo = false;
    }


    public boolean getShowForceField() {
        return showForceField;
    }
    
    public void setShowForceField(boolean value) {
        if (isInitialConditionsMode()) {
            showForceField_keep = value;
        } else {
            showForceField = value;
        }
    }

    public void toggleForceField() {
        System.out.println("Toggle force vectors: " + !showForceField);
        showForceField = !showForceField;
    }

    public void toggleBodyForces() {
        showBodyForces = !showBodyForces;
    }

    public void setShowBodyForces(boolean value) {
        if (isInitialConditionsMode()) {
            this.showBodyForces_keep = value;
        } else {
            showBodyForces = value;
        }
    }

    public boolean getShowBodyForces() {
        return showBodyForces;
    }

    public void setShowVelocities(boolean value) {
        if (isInitialConditionsMode()) {
            showVelocities_keep = value;
        } else {
            showVelocities = value;
        }
    }

    public boolean getShowVelocities() {
        return showVelocities;
    }

    public void resetForceFieldGridVectors() {
        setForceFieldGridVectors(forceFieldGridVectors);
    }

    public void setForceFieldGridVectors(int value) {
        forceFieldGridVectors = value;
        gridSize = (2. * radius) / (forceFieldGridVectors -1);
        normalizedVectorLength = 1.25 * gridSize;
        System.out.println("SetForceFieldGridVectors(" + value + " normalizedVeectorLength: " + normalizedVectorLength + " radius = " + radius);
        forceVs = new Vect[forceFieldGridVectors][forceFieldGridVectors];
        for(int i=0; i < forceFieldGridVectors; i++) {
            for(int j=0; j < forceFieldGridVectors; j++) {
                forceVs[i][j] = new Vect();
            }
        }
    }

    public void pause() {
        setPaused(true);
    }

    public void resume() {
        setPaused(false);
    }
    
    public void singleStep() {
        if (paused) {
            cycle(dt);
        }
    }

    public void waitForDrawingThread() {
        if (drawingThread != null && drawingThread.drawing) {
            try {
                drawingThread.join();
                drawingThread = null;
                System.out.println("\n\n GODOT arrived at the drawing thread. \n\n");
            } catch (Exception ex) {
                System.out.println("Orrery.gotException waiting for drawing thread. ");
                ex.printStackTrace();
            }
        }
    }

    public void winkOut() {
        if (paused) {
            paused = false;
        }
        chooseRandomDoomsayersLament();
        if (kepler != null) {
            kepler.setMode("worldtransition", true);
            kepler.setMode("worldending", true);
        }
        Thread winkingOutThread = new WinkingOutThread(this);
        winkingOutThread.start();
    }

    public boolean isWinkingOut() {
        return winkingOut;
    }

    class WinkingOutThread extends Thread {
        private Orrery orrery;
        public WinkingOutThread(Orrery orrery) {
            this.orrery = orrery;
        }
        public synchronized void run() {
            orrery.winkOut(400, true);
        }
    }

    public void winkOut(int stepTime, boolean stopDrawingThreadAfterwards) {
        winkingOut = true;
        for(int i=0; i < nbodies; i++) {
            if (bodies[i] != null && bodies[i].alive) {
                bodies[i] = null;
                delay(randomRange(stepTime, 2 * stepTime));
            }
        }
        for(int i=0; i < nrocks; i++) {
            if (rocks[i] != null && rocks[i].alive) {
                rocks[i] = null;
                delay(randomRange((int)(stepTime/5), (int)(stepTime / 3)));
            }
        }
        for(int ch=0; ch < channelQueues.length; ch++) {
            ChannelQueue q = channelQueues[ch];
            while(!q.empty()) {
                int pitch = q.dq();
                System.out.println("NoteOFF: " + pitch + "[ch: " + ch + "]");
                midiStuff.noteOff(pitch, ch);
                delay(randomRange((int)(.5 * stepTime), (int)(1.2 * stepTime)));
            }
        }
        if (stopDrawingThreadAfterwards) {
            stopDrawingThread();
        }
        winkingOut = false;
    }


    
    public void toggleMuted() {
        muted = !muted;
    }
    
    public void setMuted(boolean val) {
        muted = val;
        if (val) {
            midiAllOff();
        }
    }
    
    public void midiAllOff() {
        for(int ch=0; ch < channelQueues.length; ch++) {
            ChannelQueue q = channelQueues[ch];
            while(!q.empty()) {
                int pitch = q.dq();
                midiStuff.noteOff(pitch, ch);
            }
        }
    }

    public static int randomRange(int low, int hi) {
        return low + (int)(Math.random() * (double)(hi - low));
    }

    public static double randomRange(double low, double hi) {
        return low + Math.random() * (hi - low);
    }

    public static float randomRange(float low, float hi) {
        return low + (float)Math.random() * (hi - low);
    }


    public void delay(int t) {
        try { Thread.sleep(t); }
        catch (InterruptedException e) { System.out.println("Error sleeping"); }
    }

    public void repaint() {
        repaintComponent.repaint();
    }

    public void setRepaintComponent(Component ear) {
        this.repaintComponent = ear;
    }

} 
