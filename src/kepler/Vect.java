package kepler;


/*************************************************************************
 *
 *  Implementation of 2D a vector of real numbers.
 *
 *  Portions of this code were derived from code supplied with
 *        Introduction to Programming in Java:  An Interdisciplinary Approach,
 *        Robert Sedgewick and Kevin Wayne, Addison-Wesley, 2007. 
 *        ISBN 0-321-49805-4
 *        http://www.cs.princeton.edu/IntroProgramming
 *
 *    Specifically, in this class:
 *      draw(), times(), plus(), unit(), zero(), dot(), x(), y()
 *
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

public class Vect {
    protected double x;
    protected double y;

    public static final Vect ZERO = new Vect();

    public Vect(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vect(double[]  xy) {
        this.x = xy[0];
        this.y = xy[1];
    }

    // create a zero vector of length n
    public Vect() {
        this.x = 0.0;
        this.y = 0.0;
    }

    public Vect duplicate()  {
        Vect v = new Vect(this.x, this.y);
        return v;
    }

    public static Vect createPolar(double r, double theta) {
        return new Vect(r * Math.cos(theta), r * Math.sin(theta));
    }


    public static Vect createPolarDegrees(double r, double thetaD) {
        double thetaR = Math.toRadians(thetaD);
        return new Vect(r * Math.cos(thetaR), r * Math.sin(thetaR));
    }

    public void zero() {
        x = 0.0;
        y = 0.0;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double r() {
        return magnitude();
    }

    public double thetaDegrees() {
        double radians = Math.atan2(y, x);
        double theta = Math.toDegrees(radians);
        return theta;
    }

    public double theta() {
        double thetaRadians = Math.atan2(y, x);
        return thetaRadians;
    }

    // move theta while keeping r. 
    public Vect moveTheta(double theta) {
        double r = this.r();
        Vect point = Vect.createPolar(r, theta);
        return point;
    }

    public void thetaPlusEqualsDegrees(double deg) {
        double theta = this.theta();
        double rad = Math.toRadians(deg);
        setTheta(theta + rad);
    }
    
    public void setTheta(double theta) {
        double r = this.r();
        this.x = r * Math.cos(theta); 
        this.y = r * Math.sin(theta); 
    }

    public void setThetaDegrees(double thetaDeg) {
        setTheta(Math.toRadians(thetaDeg));
    }

    public void setR(double r) {
        if (this.r() == 0.) {
            this.x = r;
            this.y = 0.;
        } else {
            this.unitEquals();
            this.timesEquals(r);
        }
    }
        
    public void setX(double val) {
        this.x = val;
    }

    public void setY(double val) {
        this.y = val;
    }

    // return the inner product of this Vect a and b
    public double dot(Vect b) {
        return x * b.x + y * b.y;
    }

    // return the Euclidean norm of this Vect a
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    // return the corresponding unit vector
    public Vect unit() {
        if (x == 0.0 && y == 0.0) {
            // a zero vector cannot be made into a unit vector.
            return new Vect(0.0, 0.0);
        }
        Vect a = this;
        return a.times(1.0 / a.magnitude());
    }

    public Vect unitEquals() {
        if (x == 0.0 && y == 0.0) {
            // a zero vector cannot be made into a unit vector.
            return this;
        }
        return this.timesEquals(1.0 / this.magnitude());
    }


    // return a + b
    public Vect plus(Vect b) {
        Vect c = new Vect(x + b.x, y + b.y);
        return c;
    }

    // destructively add another vector to this one.
    // return self. 
    public Vect plusEquals(Vect b) {
        x += b.x;
        y += b.y;
        return this;
    }

    // return a - b
    public Vect minus(Vect b) {
        Vect c = new Vect(x - b.x, y - b.y);
        return c;
    }

    // destructively subtract another vector from this one.
    // return self. 
    public Vect minusEquals(Vect b) {
        x -= b.x;
        y -= b.y;
        return this;
    }

    // create and return a new object whose value is (this * factor)
    public Vect times(double factor) {
        Vect c = new Vect(x * factor, y * factor);
        return c;
    }

    // destructively scale this vector by a factor.
    // return self. 
    public Vect timesEquals(double factor) {
        x *= factor;
        y *= factor;
        return this;
    }

    //
    // return a new Vect that is a reflection off of a surface with the given normal vector.
    // formula: I' = I - 2(N dot I)N
    public Vect reflect(Vect normal) {
        double scale = -2.0 * this.dot(normal);
        Vect reflection = normal.times(scale);
        reflection.plusEquals(this);
        return reflection;
    }
    
    public void draw(StdDraw drawer) {
        draw(drawer, 0.0, 0.0);
    }
    
    public void draw(StdDraw drawer, double originX, double originY) {
        drawer.line(originX, x, originY, y);
    }
    
    // return a string representation of the vector
    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString(buf);
        return buf.toString();
    }
    
    public void toString(StringBuffer buf) {
        buf.append("( ");
        buf.append(x);
        buf.append(", ");
        buf.append(y);
        buf.append(")");
    }

    // test client
    public static void main(String[] args) {
        Vect x = new Vect(1.0, 2.0);
        Vect y = new Vect(5.0, 2.0);

        System.out.println("x        = " + x);
        System.out.println("y        = " + y);
        System.out.println("x + y    = " + x.plus(y));
        System.out.println("10x      = " + x.times(10.0));
        System.out.println("|x|      = " + x.magnitude());
        System.out.println("<x, y>   = " + x.dot(y));
        System.out.println("|x - y|  = " + x.minus(y).magnitude());

    }
}
