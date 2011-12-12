package kepler;


/*************************************************************************
 *
 *  Implementation of a 2D Body with a position, velocity and mass.
 *
 *  Portions of this code were derived from code supplied with
 *        Introduction to Programming in Java:  An Interdisciplinary Approach,
 *        Robert Sedgewick and Kevin Wayne, Addison-Wesley, 2007. 
 *        ISBN 0-321-49805-4
 *        http://www.cs.princeton.edu/IntroProgramming
 *
 *    Specifically, in this class:
 *      draw(), forceTo()
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

import util.StdDraw;

import java.awt.Color;

public class Body {
    public static final int REBOUND_NONE          = 0;
    public static final int REBOUND_SQUARE        = 1;
    public static final int REBOUND_CIRCLE        = 2;
    public static final int REBOUND_CIRCLE_FAKE   = 3;
    public static final int REBOUND_CIRCLE_NOTRAP = 4;
    public static final int REBOUND_TORUS         = 5;
    public static final int DEFAULT_REBOUND_METHOD = REBOUND_SQUARE;

    public static final int M2SM_LINEAR = 0;
    public static final int M2SM_CUBE_ROOT = 1;

    public Vect   pos = null;      // position
    public Vect   v   = null;     // velocity
    public double mass;         // mass
    public double radius;     // size of world
    //public int massToSizeMethod = M2SM_LINEAR;
    public static int massToSizeMethod = M2SM_CUBE_ROOT; // testing, for now

    protected static Orrery orrery;
    public static double orreryRadius; 
    public static double baseRadius       = 512.;    // minimum size, world coordinates
    public static double massToSizeFactor = 1.234; // mass affects size times this.

    /** keep a record of how the position was specified: polar or cartesian; offset or not.
        These will be needed later for building worlds and for writing out world files.
    */
    public boolean posSpecifiedPolar    = false;
    public boolean vSpecifiedPolar      = false;
    public boolean offsetSpecifiedPolar = false;
    public Vect    posOffset            = null;
    
    public static StdDraw drawer;
    
    protected int highlight = 0;
    protected int select = 0;
    protected int playing = 0;
    public    int channel = 0;
    public String instrument = null;
    public boolean alive  = true;
    
    protected        Color   color;            // color
    protected        Color   highlightColor;  // color
    protected        Color   playingColor;  // color
    protected        Color   selectColor;  // color
    protected        double  penRadius      = .025;
    private   static double  friction       = .99;
    protected static double  minDistance;
    public    static double  minMass        = -1.0;
    public    static double  maxMass        = 0.0;
    protected static double  basePenRadius  = 0.025;
    protected static int     reboundMethod  = REBOUND_SQUARE;
    protected        boolean constrainMovesToRadius = false;

    public static final int MUTATE_NONE = 0;
    public static final int MUTATE_MASS = 1;
    public static final int MUTATE_MASS_PERCENT = 2;
    public static final int MUTATE_VELOCITY_PERCENT = 3;
    public static final int MUTATE_CLONE = 4;
    public static final int MUTATE_KILLA = 5;
    public static final int MUTATE_BLACKHOLE = 6;

    public boolean is_mutator = false;
    public int mutating = 0;
    public int mutate_after = 0;
    public int mutate_mass_type = MUTATE_NONE;
    public int mutate_mass_chance = 0;
    public double mutate_mass_lo = 0.;
    public double mutate_mass_hi = 0.;

    public int mutate_velocity_type = MUTATE_NONE;
    public int mutate_velocity_chance = 0;
    public double mutate_velocity_lo = 0.;
    public double mutate_velocity_hi = 0.;

    public boolean mutate_clone  = false;
    public int mutate_clone_chance = 0;
    public double mutate_clone_lo = 0.;
    public double mutate_clone_hi = 0.;

    public boolean mutate_killa  = false;
    public int mutate_killa_chance = 0;

    public boolean mutate_blackhole  = false;
    public int mutate_blackhole_chance = 0;

    private static int next_nth = 0;
    private int nth=0;

    protected double repelFactor = 1.0;
    protected double gravityFactor = 1.0;

    protected Playable playable = null;
    public boolean playableIsShared = false;

    public Body() {
    }
    
    // Note that pos & v can be null at construction time. 
    public Body(Vect pos, Vect v, double mass, int numballs) {
        this.pos = pos;
        this.v = v;
        this.mass = mass;
        init(numballs);
    }
    
    public void init(int numballs) {
        setMinMaxMasses(mass);
        this.nth = next_nth;
        next_nth++;
        float hue = (float) ((1.0 / (float)numballs) * (float)nth);
        if (hue > 1.f) {
            hue = 1.f;
        }
        //color = Color.getHSBColor(hue, .70f, .65f);
        color = Color.getHSBColor(hue, .85f, .70f);
        highlightColor = Color.getHSBColor(hue, .95f, .95f);
        playingColor = Color.getHSBColor(hue, .95f, 1.0f);
        selectColor = Color.getHSBColor(hue, .9f, .80f);
    }

    public Body duplicate() {
        Vect clpos = this.pos.duplicate();
        Vect clv = this.v.duplicate();
        Body cl = new Body(clpos, clv, this.mass, orrery.maxBodies);
        cl.cloneMutatorData(this);
        cl.clonePlayable(this);
        cl.posSpecifiedPolar = this.posSpecifiedPolar;
        cl.vSpecifiedPolar = this.vSpecifiedPolar;
        cl.offsetSpecifiedPolar = this.offsetSpecifiedPolar;
        cl.posOffset = this.posOffset;
        return cl;
    }

    protected void cloneMutatorData(Body body) {
        this.is_mutator = body.is_mutator;
        this.mutate_after = body.mutate_after;

        this.mutate_mass_type   = body.mutate_mass_type;
        this.mutate_mass_chance = body.mutate_mass_chance;
        this.mutate_mass_lo     = body.mutate_mass_lo;
        this.mutate_mass_hi     = body.mutate_mass_hi;

        this.mutate_velocity_type   = body.mutate_velocity_type;
        this.mutate_velocity_chance = body.mutate_velocity_chance;
        this.mutate_velocity_lo     = body.mutate_velocity_lo;
        this.mutate_velocity_hi     = body.mutate_velocity_hi;

        this.mutate_clone        = body.mutate_clone;
        this.mutate_clone_chance = body.mutate_clone_chance;
        this.mutate_clone_lo     = body.mutate_clone_lo;
        this.mutate_clone_hi     = body.mutate_clone_hi;

        this.mutate_killa        = body.mutate_killa;
        this.mutate_killa_chance = body.mutate_killa_chance;

        this.mutate_blackhole        = body.mutate_blackhole;
        this.mutate_blackhole_chance = body.mutate_blackhole_chance;
    }

    public  void clonePlayable(Body body) {
        this.playable = body.playable;
        this.setChannel(body.channel);
    }

    public void resetPlayable() {
        if (playable != null) {
            playable.reset();
        }
    }

    public static void resetNth() {
        next_nth = 0;
    }

    public static void resetDefaults() {
        next_nth = 0;
        minMass = -1.0;
        maxMass = 0.0;
        baseRadius = 512.;
    }

    public static void setOrreryRadius(double val) {
        orreryRadius = val;
    }

    public static void setFriction(double val) {
        friction = val;
    }

    public static void setMinDistance(double val) {
        minDistance = val;
    }

    public static void setReboundMethod(int r) {
        reboundMethod = r;
    }
    public static int getReboundMethod() {
        return reboundMethod;
    }

    public static void setBaseRadius(double r) {
        baseRadius = r;
    }

    public double getRepelFactor() {
        return this.repelFactor;
    }
    public void setRepelFactor(double rf) {
        this.repelFactor = rf;
    }

    public double getGravityFactor() {
        return this.gravityFactor;
    }
    public void setGravityFactor(double f) {
        this.gravityFactor = f;
    }

    public Playable getPlayable() {
        return this.playable;
    }
    public void setPlayable(Playable val) {
        this.playable = val;
    }

    public boolean constrainMovesToRadius() {
        return constrainMovesToRadius;
    }
    
    public void setConstrainMovesToRadius(boolean val) {
        constrainMovesToRadius = val;
    }

    public double getMass() {
        return this.mass;
    }
    
    public Vect getPos() {
        return this.pos;
    }
    public Vect getVelocity() {
        return this.v;
    }
    
    public void move(Vect f, double dt) {
        Vect a = f.times(1/mass);
        //v = v.plus(a.times(dt));
        v.plusEquals(a.timesEquals(dt));
        //pos = pos.plus(v.times(dt));
        pos.plusEquals(v.times(dt));
    }

    public void moveStep(double dt)  {
        pos.plusEquals(v.times(dt));
    }

    public void moveStep()  {
        pos.plusEquals(v.times(orrery.dt));
    }

    // add reflection. 
    public void move(Vect f, double dt, double xbound, double ybound) {
        Vect a = f.times(1/mass);
        //v = v.plus(a.times(dt));
        v.plusEquals(a.timesEquals(dt));  // fix: combine dt and 1/mass
        // "friction"
        //v = v.times(friction);
        v.timesEquals(friction);
        //pos = pos.plus(v.times(dt)); // fix: this needs to be made non-copying (i.e. destructive vector ops)
        pos.plusEquals(v.times(dt));
        possiblyRebound(xbound, ybound);
    }

    public static String reboundMethodString(int rm) {
        switch( rm ) {
        case REBOUND_SQUARE:
            return "square";
        case REBOUND_NONE:
            return "none";
        case REBOUND_CIRCLE:
            return "circle";
        case REBOUND_CIRCLE_FAKE:
            return "circlefake";
        case REBOUND_CIRCLE_NOTRAP:
            return "circlenotrap";
        case REBOUND_TORUS:
            return "torus";
        default:
            return "square";
        }
    }
        
    public void possiblyRebound(double xbound, double ybound) {
        if (reboundMethod == REBOUND_SQUARE) {
            if (Math.abs(pos.x()) > xbound) {
                v.setX(0.0 - v.x());
            }
            if (Math.abs(pos.y()) > ybound) {
                v.setY( 0.0 - v.y());
            }
        } else if (reboundMethod == REBOUND_TORUS) {
            double x = pos.x();
            double y = pos.y();
            double diam = 2 * radius;
            double xout = xbound + diam;
            double yout = ybound + diam;
            if (x > xout ) {
                pos.setX(x - 2. * xout);
            } else if (x < -xout) {
                pos.setX(x + 2 * xout);
            }
            if (y > yout ) {
                pos.setY(y - 2. * yout);
            } else if (y < -yout) {
                pos.setY(y + 2 * yout);
            }
        } else if (reboundMethod == REBOUND_CIRCLE_FAKE) {
            if (pos.magnitude() >= xbound) {
                // cheat: simply reverse thevector...
                v.timesEquals(-1.0);
            }
        } else if (reboundMethod == REBOUND_CIRCLE) {
            if (pos.magnitude() >= xbound) {
                Vect normal = pos.unit();
                Vect reflection = this.v.reflect(normal);
                
                this.v = reflection;
            }
        } else if (reboundMethod == REBOUND_CIRCLE_NOTRAP) {
            if (pos.magnitude() > xbound) {
                Vect normal = pos.unit();
                Vect reflection = this.v.reflect(normal);
                System.out.println("\n");
                
                this.v = reflection;
                // stop getting trapped outside the rim
                // by repositioning back to the rim. 
                pos = normal.timesEquals(xbound);  
            }
        }
    }


    // add 1/n**3 repulsive force. 
    public Vect forceTo(Body b,
                        double globalGravityFactor,
                        double globalRepelFactor,
                        boolean checkForCollisions) {
        if (b == null) {
            return Vect.ZERO;
        }
        double G = 6.67e-11;
        double GRepel = -36.67e-10;
        //System.out.println("FOrceTo. " + getClass() + " r=" + r + " v=" + v);
        // note: this line wants a nondestructive vector minus. 
        Vect delta = pos.minus(b.pos);
        double massmass = mass * b.mass; 
        double dist = delta.magnitude();
        // test for collisions
        if (checkForCollisions && (dist < radius || dist < b.radius)) {
            registerCollision(b);
        }
        dist = Math.max(dist, minDistance);
        double dist2 = dist*dist;
        double dist3 = dist2 * dist;
        double F =
            (gravityFactor * globalGravityFactor * G * massmass) / (dist2) +
            (repelFactor * globalRepelFactor * GRepel * massmass) / (dist3);
        return delta.unitEquals().timesEquals(F);
    }

    public void setDrawColor(StdDraw drawer) {
        //System.out.println("BODY:Draw " + nth + " (" + pos.x() + ", " + pos.y() + ") r: " + radius);
        //drawer.setPenRadius(penRadius);
        if (select == -1) {
            drawer.setPenColor(selectColor);
        } else if (select > 0) {
            drawer.setPenColor(selectColor);
            select -= 1;
        } else if (playing > 0) {
            drawer.setPenColor(playingColor);
            highlight -= 1;
        } else if (highlight > 0) {
            drawer.setPenColor(highlightColor);
            highlight -= 1;
        } else if (highlight == -1) {
            drawer.setPenColor(highlightColor);
        } else {
            drawer.setPenColor(color);
        }
        if (mutating > 0) {
            drawer.setPenColor(selectColor);
            mutating -= 1;
        }
        //drawer.point(pos.x(), pos.y());
        if (playing > 0) {
            playing -= 1;
        }
    }
    public void draw(StdDraw drawer) {
        double x = pos.x();
        double y = pos.y();
        setDrawColor(drawer);
        drawer.filledCircle(x, y, radius);
        if (is_mutator) {
            drawer.setPenColor(selectColor);
            drawer.circle(x, y, radius * .75);
        }
        if (select == -1) {
            if (orrery.paused()) {
                drawSelectedControls(drawer, x, y);
            } else {
                double selectRadius = Math.max(radius * 1.5, baseRadius * 2.);
                drawer.circle(pos.x(), pos.y(), selectRadius);  // was: radius * 1.25
            }
        }
    }

    protected void drawSelectedControls(StdDraw drawer, double x, double y) {
        double ctlBoxHandleWidth = 1.5 * baseRadius;
        double boxWidth = getControlBoxWidth();
        drawer.setPenColor(selectColor);
        drawer.square(x, y, boxWidth);
        if (highlightControlBox) {
            drawer.setPenColor(Color.YELLOW);
        } else {
            drawer.setPenColor(Color.BLUE);
        }
        drawer.filledSquare(x + boxWidth, y + boxWidth, ctlBoxHandleWidth);
        drawer.filledSquare(x + boxWidth, y - boxWidth, ctlBoxHandleWidth);
        drawer.filledSquare(x - boxWidth, y + boxWidth, ctlBoxHandleWidth);
        drawer.filledSquare(x - boxWidth, y - boxWidth, ctlBoxHandleWidth);
        if (!(this instanceof Rock)) {
            Vect vdt = v.times(orrery.dt);
            if (orrery.dragChangesVelocityOnSelectedBody) {
                drawer.setPenColor(orrery.highlightVelocityColor);
            } else {
                drawer.setPenColor(orrery.velocityColor);
            }
            double velocityFactor = orrery.ctlVelocityFactor;
            orrery.drawVectWithHandle(drawer, pos.x, pos.y, vdt, velocityFactor, 1.3 * baseRadius);
        }
        orrery.drawBodyStats(this);
    }

    public double getControlBoxWidth() {
        return Math.max(radius * 1.5, baseRadius * 4.);
    }

    boolean highlightControlBox = false;
    public void highlightControlBox(boolean val) {
        this.highlightControlBox = val;
    }
    
    public String getInstrument() {
        return this.instrument;
    }

    public void setInstrument(String val) {
        this.instrument = val;
    }
    
    public void setChannel(int val) {
        this.channel = val;
    }
    public int getChannel() {
        return this.channel;
    }

    public void highlight(int val) {
        highlight = val;
    }

    public int getHighlight() {
        return highlight;
    }

    public void select(int val) {
        select = val;
    }

    public void unselect() {
        select(0);
    }
    
    public int getSelect() {
        return select;
    }

    // for how many more cycles will this body be playing?
    public boolean isPlaying() {
        return playing > 0;
    }
    public int getPlaying() {
        return playing;
    }
    public void setPlaying(int val) {
        this.playing = val;
    }

    public static double  getMaxMass() {
        return maxMass;
    }
    
    public static void setMaxMass(double mm) {
        maxMass = mm;
    }


    public static double getMinMass() {
        return minMass;
    }

    public static void setMinMass(double mm) {
        minMass = mm;
    }

    public void setMinMaxMasses(double mass) {
        if (minMass < 0.0 || mass < minMass) {
            minMass = mass;
        }
        if (mass > maxMass) {
            maxMass = mass;
        }
    }

    public void setMass(double val) {
        if (val != this.mass) {
            this.mass = val;
            massToSize(drawer);
        }
    }
    
    public void massToSize(StdDraw drawer) {
        double rank = (massType(mass) - massType(minMass)) / (massType(maxMass) - massType(minMass));
        // largest thing is 1/20 the radius of the orrery?
        double largest = 1.3 * orreryRadius / 20.;
        radius = baseRadius + rank * largest;
        // mutations can't shrink or grow too far. 
        if (radius <= 0.0) {
            radius = baseRadius;
        } else if (radius > largest * 3) {
            radius = largest*3;
        }
        penRadius = drawer.factorX(radius);
        //System.out.println("\nBody.massToSize()  mass: " + mass + " minMass: " + minMass + " maxMass: " + maxMass + " Urad: " + orreryRadius);
        //System.out.println("Body.massToSize() rank: " + rank + " radius: " + radius + " penRadius: " + penRadius + " [old way pen radius: " + basePenRadius * (1.0 + 3 * rank) + "]");
        //System.out.println("Body.massToSize() unfactorX(oldPenRad): " + drawer.unfactorX(oldPenRadius));
    }

    private double massType(double mass) {
        if (massToSizeMethod == M2SM_LINEAR) {
            return mass;
        } else if (massToSizeMethod == M2SM_CUBE_ROOT) {
            double cuberoot = 1. / 3.;
            return Math.pow(mass, cuberoot);
        }
        return mass;
    }

    // cheating for now. Seeing if x,y is within the square of
    // side=radius around the position. 
    public boolean intersects(double x, double y) {
        double intersectRadius = radius;
        return pointIsClose(x, y, pos.x(), pos.y(), intersectRadius);
    }

    public boolean intersects(double x, double y, double fudgeFactor) {
        double intersectRadius = radius * fudgeFactor;
        return pointIsClose(x, y, pos.x(), pos.y(), intersectRadius);
    }
            
    public boolean controlBoxHandleIntersects(double x, double y, double fudgeFactor) {
        double intersectRadius = baseRadius * fudgeFactor;
        double cbWidth = getControlBoxWidth();
        double posx = pos.x();
        double posy = pos.y();
        // Note: we have a large fudge radius on the control handles, and those may
        //       overlap the body for small-radius bodies, so we need to
        //       check to make sure the body isn't being intersected. 
        
        
        boolean result =
            !intersects(x, y, 1.0)  &&
            (pointIsClose(x, y, posx + cbWidth, posy + cbWidth, intersectRadius) ||
             pointIsClose(x, y, posx + cbWidth, posy - cbWidth, intersectRadius) ||
             pointIsClose(x, y, posx - cbWidth, posy + cbWidth, intersectRadius) ||
             pointIsClose(x, y, posx - cbWidth, posy - cbWidth, intersectRadius));
        return result;
    }

    public boolean pointIsClose(double x, double y,
                                double targetX, double targetY,
                                double intersectRadius) {
        boolean result = (x > targetX - intersectRadius && x < targetX + intersectRadius &&
                          y > targetY - intersectRadius && y < targetY + intersectRadius);
        return result;
    }

    public void moveto(double x, double y) {
        pos.setX(x);
        pos.setY(y);
    }

    public void moveto(Vect v) {
        pos.setX(v.x());
        pos.setY(v.y());
    }

    public void movetoPolar(double r, double theta) {
        Vect polar = Vect.createPolarDegrees(r, theta);
        moveto(polar.x(), polar.y());
    }

    public void moveto_constrainRadius(double x, double y) {
        double r = pos.r();
        Vect point = (new Vect(x, y)).unitEquals();
        pos = point.times(r);
    }

    public void setV(double vx, double vy) {
        v.setX(vx);
        v.setY(vy);
    }

    public void setV(Vect newV) {
        setV(newV.x(), newV.y());
    }
    

    
    public void registerCollision(Body culprit) {
        addCollision(this, culprit);
    }
    private static int numCollisions = 0;
    private static Body[][] collisions;
    public static void setupCollisions(int max) {
        collisions = new Body[max][];
    }
    
    public static int getNumCollisions() {
        return numCollisions;
    }
    public static void resetCollisions() {
        numCollisions = 0;
    }
    public static Body[][] getCollisions() {
        return collisions;
    }
    public static Body[] getCollision(int i) {
        if (i < numCollisions) {
            return collisions[i];
        }
        return null;
    }
    
    public static boolean addCollision(Body plaintiff, Body culprit) {
        for(int i=0; i < numCollisions; i++) {
            Body[] coll = collisions[i];
            if (culprit == coll[0] && plaintiff == coll[1]) {
                return false;
            }
        }
        collisions[numCollisions] = new Body[] {plaintiff, culprit};
        numCollisions++;
        return true;
    }

    public void die() {
        alive = false;
    }
    
    public void maybeMutate(Body otherBody, int nthCycle) {
        if (!is_mutator || otherBody == null || otherBody.mutating > 0) return;

        if (mutate_after > 0 && nthCycle < mutate_after) return;

        if (mutate_mass_type == MUTATE_MASS) {
            if (randomChance(mutate_mass_chance)) {
                otherBody.mass = randomRange(mutate_mass_lo, mutate_mass_hi);
                otherBody.massToSize(drawer);
                otherBody.mutating = 20;
            }
        } else  if (mutate_mass_type == MUTATE_MASS_PERCENT) {
            if (randomChance(mutate_mass_chance)) {
                double factor = randomRange(mutate_mass_lo, mutate_mass_hi) / 100.;
                otherBody.mass *= factor;
                otherBody.massToSize(drawer);
                otherBody.mutating = 20;
                //System.out.println("MUTATE MASS% : b4: "  + b4 + " factor: " + factor + " >> " + otherBody.mass);
            }
        }

        if (mutate_velocity_type == MUTATE_VELOCITY_PERCENT) {
            if (!(otherBody instanceof Rock) && randomChance(mutate_velocity_chance)) {
                double factor = randomRange(mutate_velocity_lo, mutate_velocity_hi) / 100.;
                otherBody.v.timesEquals(factor);
                otherBody.mutating = 20;
            }
        }

        if (mutate_clone)  {
            if (randomChance(mutate_clone_chance)) {
                
                double massFactor = randomRange(mutate_clone_lo, mutate_velocity_hi) / 100.;
                otherBody.mass *= massFactor;
                otherBody.massToSize(drawer);
                //System.out.println("duplicating: " + otherBody + " pos: " + otherBody.pos);
                Body cl = otherBody.duplicate();
                if (cl instanceof Rock)  {
                    orrery.addRock((Rock)cl);
                } else  {
                    //System.out.println("Adding clone: " + cl  + " pos: " + cl.pos);
                    //System.out.println(" B4 otherBody.v: " + otherBody.v);
                    //System.out.println(" B4 a       cl.v: " + cl.v);
                    orrery.addBody(cl);
                    otherBody.v.thetaPlusEqualsDegrees(30.);
                    cl.v.thetaPlusEqualsDegrees(-30.);
                    //System.out.println(" aft otherBody.v: " + otherBody.v);
                    //System.out.println(" aft        cl.v: " + cl.v);
                    double obr = otherBody.v.r();
                    if (obr > 60.) {
                        otherBody.v.setR(60.);
                    }
                    double clr = cl.v.r();
                    if (clr > 50.) {
                        cl.v.setR(50.);
                    }
                }
                otherBody.moveStep();
                cl.moveStep();
                otherBody.moveStep();
                cl.moveStep();
                otherBody.mutating = 30;
                cl.mutating = 30;
            }
        }

        if (mutate_killa)  {
            if (randomChance(mutate_killa_chance)) {
                otherBody.die();
                this.mutating = 20;
            } else {
                // if the other body escapes the guillotine, grant it immunity for a while
                otherBody.mutating = 40;
            }
        } else if (mutate_blackhole)  {
            if (randomChance(mutate_blackhole_chance)) {
                double otherBodyMass = otherBody.mass;
                this.mutating = 20;
                this.mass += otherBodyMass;
                this.massToSize(drawer);
                otherBody.die();
            } else {
                // if the other body escapes the guillotine, grant it immunity for a while
                otherBody.mutating = 40;
            }
        }



    }

    public static boolean randomChance(int chance) {
        double ran = Math.random();
        return (ran * 100. < chance);
    }

    public static double randomRange(double lo, double hi) {
        double delta = hi - lo;
        return lo + delta * Math.random();
    }
        
} 

