/***************************************************
 * Copyright 2007 by Simran Gleason                *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;

import java.awt.Color;

import util.StdDraw;

public class Rock extends Body {
    private static int next_nthrock = 0;
    private int nthrock=0;
    public double move_theta = 0.;

    public Rock() {
    }
    
    public Rock(Vect pos, double mass, int numrocks) {
        this.penRadius = 0.05;
        this.pos = pos;
        this.mass = mass;
        init(numrocks);
    }

    public void init(int numrocks) {
        setMinMaxMasses(mass);
        this.nthrock = next_nthrock;
        next_nthrock++;
        float hue = (float) ((1.0 / (float)numrocks) * (float)nthrock);
        color = Color.getHSBColor(hue, .4f, .4f);
        highlightColor = Color.getHSBColor(hue, .4f, .6f);
        playingColor = Color.getHSBColor(hue, .65f, 1.0f);
        selectColor = Color.getHSBColor(hue, 1.f, .5f);
    }

    public Body duplicate() {
        Vect clpos = this.pos.duplicate();
        Rock cl = new Rock(clpos, this.mass, orrery.maxRocks);
        cl.cloneMutatorData(this);
        cl.clonePlayable(this);
        cl.move_theta = this.move_theta;
        return cl;
    }

    public static void resetDefaults() {
        next_nthrock = 0;
    }


    public static void resetNth() {
        next_nthrock = 0;
    }

    public void maybeMove() {
        if (move_theta != 0.) {
            pos.thetaPlusEqualsDegrees(move_theta);
        }
    }
    public void draw(StdDraw drawer) {
        //System.out.println("ROCK:Draw " + nth + " (" + pos.x() + ", " + pos.y() + ") r: " + radius);
        //drawer.setPenRadius(penRadius);
        //drawer.setPenColor(color);
        //drawer.point(pos.x(), pos.y());
        double x = pos.x();
        double y = pos.y();
        setDrawColor(drawer);
        drawer.filledSquare(x, y, radius);
        if (is_mutator) {
            drawer.setPenColor(selectColor);
            drawer.square(x, y, radius * .75);
        }
        if (select == -1) {
            if (orrery.paused()) {
                drawSelectedControls(drawer, x, y);
            } else {
                double selectRadius = Math.max(radius * 1.5, baseRadius * 2.);
                drawer.square(pos.x(), pos.y(), selectRadius);
            }
        }
    }
} 

