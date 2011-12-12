/*************************************************************************
 *  Compilation:  javac StdDraw.java
 *  Execution:    java StdDraw
 *
 *  Standard graphics library.
 *
 *  For documentation, see http://www.cs.princeton.edu/introcs/15inout
 *
 *  Portions of this class were derived from code supplied with
 *        Introduction to Programming in Java:  An Interdisciplinary Approach,
 *        Robert Sedgewick and Kevin Wayne, Addison-Wesley, 2007. 
 *        ISBN 0-321-49805-4
 *        http://www.cs.princeton.edu/IntroProgramming
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

package util;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.ImageIO;


public final class StdDraw implements ActionListener, MouseListener, MouseMotionListener, KeyListener {

    // pre-defined colors
    public static final Color BLACK      = Color.BLACK;
    public static final Color BLUE       = Color.BLUE;
    public static final Color CYAN       = Color.CYAN;
    public static final Color DARK_GRAY  = Color.DARK_GRAY;
    public static final Color GRAY       = Color.GRAY;
    public static final Color GREEN      = Color.GREEN;
    public static final Color LIGHT_GRAY = Color.LIGHT_GRAY;
    public static final Color MAGENTA    = Color.MAGENTA;
    public static final Color ORANGE     = Color.ORANGE;
    public static final Color PINK       = Color.PINK;
    public static final Color RED        = Color.RED;
    public static final Color WHITE      = Color.WHITE;
    public static final Color YELLOW     = Color.YELLOW;

    // default colors
    public static final Color DEFAULT_PEN_COLOR   = BLACK;
    public static final Color DEFAULT_CLEAR_COLOR = WHITE;

    // default canvas size is SIZE-by-SIZE
    private static final int DEFAULT_SIZE = 480;
    // default pen radius
    private static final double DEFAULT_PEN_RADIUS = 0.002;

    // current pen color
    private Color penColor;

    private int width  = DEFAULT_SIZE;
    private int height = DEFAULT_SIZE;


    // current pen radius
    private double penRadius;

    // show we draw immediately or wait until next show?
    private boolean defer = false;

    // boundary of drawing canvas, 5% border
    private static final double BORDER = 0.025;
    private static final double DEFAULT_XMIN = 0.0;
    private static final double DEFAULT_XMAX = 1.0;
    private static final double DEFAULT_YMIN = 0.0;
    private static final double DEFAULT_YMAX = 1.0;

    private double xmin, ymin, xmax, ymax;
    private double text_point_x;
    private double text_point_y;

    // default font
    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 14);

    // current font
    private  Font font;
    private double line_height;

    // double(triple) buffered graphics
    private  BufferedImage offscreenImage, onscreenImage, backgroundImage;
    private  Graphics2D offscreen, onscreen, background;
    private Composite offscreenComposite;

    // trails...
    private int trails = 0;
    private AlphaComposite trailsAlphaComposite = null;

    JLabel draw;

    // mouse state
    private  boolean mousePressed = false;
    private  double mouseX = 0;
    private  double mouseY = 0;

    // keyboard state
    private  char lastKeyTyped = (char)-1;

    public StdDraw(int canvasSize) {
        if (canvasSize != 0) {
            initCanvasSize(canvasSize);
        }
        init();
    }

    public void initCanvasSize(int cs) {
        width = cs;
        height = cs;
    }
    
    // set the window size to w-by-h pixels
    public  void setCanvasSize(int w, int h) {
        boolean changed = false;
        if (w < 1 || h < 1) throw new RuntimeException("width and height must be positive");
        if (width != w) {
            width = w;
            changed = true;
        }
        if (height != h) {
            height = h;
            changed = true;
        }
        if (changed) {
            init();
        }
    }


    // init
    private  void init() {
        backgroundImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        onscreenImage  = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        background = backgroundImage.createGraphics();
        offscreen = offscreenImage.createGraphics();
        offscreenComposite = offscreen.getComposite(); 

        onscreen  = onscreenImage.createGraphics();
        setXscale();
        setYscale();
        offscreen.setColor(DEFAULT_CLEAR_COLOR);
        offscreen.fillRect(0, 0, width, height);
        setPenColor();
        setPenRadius();
        setFont();
        clear();

        // add antialiasing
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                                  RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        offscreen.addRenderingHints(hints);
        background.addRenderingHints(hints);

        ImageIcon icon = new ImageIcon(onscreenImage);
        draw = new JLabel(icon);

        draw.addMouseListener(this);
        draw.addMouseMotionListener(this);
        postDisplayInit();
    }

    public void postDisplayInit() {
        setupFont(this.font, offscreen);
        setupFont(this.font, background);
    }


    public Container getDrawingPane() {
        return draw;
    }

    public void setTrails(int trails) { 
        System.out.println("STDDRAW: set trails (" + trails + ")");
        this.trails = trails;
        
        if (trails == 0) {
            trailsAlphaComposite = null;
        } else {
            float alpha = 1.0f - 1.0f / (float)trails;
            System.out.println("STDDRAW:  trails alpha: " + alpha);
            trailsAlphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha);
        }
    }

    public static void fullscreen(Window window, boolean onness) {
        GraphicsDevice device;
        device = 
            GraphicsEnvironment.
            getLocalGraphicsEnvironment().
            getDefaultScreenDevice();
        if ( device.isFullScreenSupported() ) {
            if (onness) {
                device.setFullScreenWindow(window);
                window.repaint();
            } else {
                device.setFullScreenWindow(null);
                window.repaint();
            }
        }
        else { 
            System.err.println("Full screen not supported"); 
        }
    }

    public  void addMouseListener(MouseListener squeek) {
        draw.addMouseListener(squeek);
    }

    public void addMouseMotionListener(MouseMotionListener squeek) {
        draw.addMouseMotionListener(squeek);
    }

    private ArrayList actionListeners = new ArrayList();
    public void addActionListener(ActionListener ears) {
        actionListeners.add(ears);
    }
    


   /*************************************************************************
    *  User and screen coordinate systems
    *************************************************************************/

    // change the user coordinate system
    public void setXscale() { setXscale(DEFAULT_XMIN, DEFAULT_XMAX); }
    public void setYscale() { setYscale(DEFAULT_YMIN, DEFAULT_YMAX); }
    private double xscale, yscale;
    public void setXscale(double min, double max) {
        double size = max - min;
        xmin = min - BORDER * size;
        xmax = max + BORDER * size;
        xscale = width / (xmax - xmin);
    }
    public void setYscale(double min, double max) {
        double size = max - min;
        ymin = min - BORDER * size;
        ymax = max + BORDER * size;
        yscale =  height / (ymax - ymin);
    }

    // helper functions that scale from user coordinates to screen coordinates and back
    //public  double scaleX (double x) { return width  * (x - xmin) / (xmax - xmin); }
    public   double scaleX (double x) { return  (x - xmin) * xscale; }
    //public double scaleY (double y) { return height * (ymax - y) / (ymax - ymin); }
    public  double scaleY (double y) { return (ymax - y)  * yscale; }
    //public double factorX(double w) { return w * width  / Math.abs(xmax - xmin);  }
    public   double factorX(double w) { return w * xscale;  }
    //public double factorY(double h) { return h * height / Math.abs(ymax - ymin);  }
    public   double factorY(double h) { return h * yscale;  }
    public   double userX  (double x) { return xmin + x * (xmax - xmin) / width;    }
    public   double userY  (double y) { return ymax - y * (ymax - ymin) / height;   }
    public   double unfactorX  (double x) { return x * (xmax - xmin) / width;    }
    public   double unfactorY  (double y) { return y * (ymax - ymin) / height;   }


    // clear the screen with given color
    public  void clear() { clear(DEFAULT_CLEAR_COLOR); }
    public  void clear(Color color) {
        offscreen.setColor(color);
        offscreen.fillRect(0, 0, width, height);
        offscreen.setColor(penColor);
        show();
    }
    public void initbg(Color color) {
        if (color != null) {
            offscreen.setColor(color);
            offscreen.fillRect(0, 0, width, height);
            offscreen.setColor(penColor);
        }
        offscreen.drawImage(backgroundImage, 0, 0, null);
    }
    
    public  void clearbg(Color color) {
        if (trails > 0) {
            if (color != null) {
                offscreen.setColor(color);
                offscreen.fillRect(0, 0, width, height);
                offscreen.setColor(penColor);
                // possible fix: don't draw bg if there are trails; just clear
                offscreen.setComposite(trailsAlphaComposite);
                offscreen.drawImage(onscreenImage, 0, 0, null);
                offscreen.setComposite(offscreenComposite);
            }
        } else {
            offscreen.drawImage(backgroundImage, 0, 0, null);
        }
            
        show();
    }

    // set the pen size
    public  void setPenRadius() { setPenRadius(DEFAULT_PEN_RADIUS); }
    public  void setPenRadius(double r) {
        if (r < 0) throw new RuntimeException("pen radius must be positive");
        penRadius = r * DEFAULT_SIZE;
        BasicStroke stroke = new BasicStroke((float) penRadius);
        offscreen.setStroke(stroke);
    }
    public  void setPenRadius_bg(double r) {
        if (r < 0) throw new RuntimeException("pen radius must be positive");
        double penRadius = r * DEFAULT_SIZE;
        BasicStroke stroke = new BasicStroke((float) penRadius);
        background.setStroke(stroke);
    }


    // set the pen color
    public  void setPenColor() { setPenColor(DEFAULT_PEN_COLOR); }
    public  void setPenColor(Color color) {
        penColor = color;
        offscreen.setColor(penColor);
    }

    public  void setPenColor_bg(Color color) {
        background.setColor(color);
    }

    // write the given string in the current font
    public  void setFont() { setFont(DEFAULT_FONT); }
    public  void setFont(Font f) { font = f; }


   /*************************************************************************
    *  Drawing geometric shapes.
    *************************************************************************/

    // draw a line from (x0, y0) to (x1, y1)
    public  void line(double x0, double y0, double x1, double y1) {
        offscreen.draw(new Line2D.Double(scaleX(x0), scaleY(y0), scaleX(x1), scaleY(y1)));
        show();
    }

    // draw a line from (x0, y0) to (x1, y1)
    public  void line_bg(double x0, double y0, double x1, double y1) {
        background.draw(new Line2D.Double(scaleX(x0), scaleY(y0), scaleX(x1), scaleY(y1)));
    }


    // draw one pixel at (x, y)
    private void pixel(double x, double y) {
        offscreen.fillRect((int) Math.round(scaleX(x)), (int) Math.round(scaleY(y)), 1, 1);
    }

    // draw one pixel at (x, y)
    private void pixel_bg(double x, double y) {
        background.fillRect((int) Math.round(scaleX(x)), (int) Math.round(scaleY(y)), 1, 1);
    }


    // draw point at (x, y)
    public  void point(double x, double y) {
        double xs = scaleX(x);
        double ys = scaleY(y);
        double r = penRadius;
        // double ws = factorX(2*r);
        // double hs = factorY(2*r);
        // if (ws <= 1 && hs <= 1) pixel(x, y);
        if (r <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - r/2, ys - r/2, r, r));
        System.out.println("POINT: x: " + x + " xs: " + xs + "y: " + y + " ys: " + ys + "r: " + r + " xs-r/2: " + (xs - r/2));
        show();
    }

    // draw point at (x, y)
    public  void point_bg(double x, double y) {
        double xs = scaleX(x);
        double ys = scaleY(y);
        double r = penRadius;
        // double ws = factorX(2*r);
        // double hs = factorY(2*r);
        // if (ws <= 1 && hs <= 1) pixel(x, y);
        if (r <= 1) pixel_bg(x, y);
        else background.fill(new Ellipse2D.Double(xs - r/2, ys - r/2, r, r));
    }


    // draw circle of radius r, centered on (x, y); degenerate to pixel if small
    public  void circle_bg(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("circle radius can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel_bg(x, y);
        else background.draw(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
    }

    // draw circle of radius r, centered on (x, y); degenerate to pixel if small
    public  void circle(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("circle radius can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        show();
    }

    // draw filled circle of radius r, centered on (x, y); degenerate to pixel if small
    public  void filledCircle(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("circle radius can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        //double ws = factorX(2*r);
        //double hs = factorY(2*r);
        double rs = factorX(2 * r);
        //if (ws <= 1 && hs <= 1) pixel_bg(x, y);
        //else offscreen.fill(new Ellipse2D.Double(xs - ws/2, ys - hs/2, rs, rs));
        if (rs <= 1.) {
            if (trails > 0) {
                pixel(x, y);
            } else {
                pixel_bg(x, y);
            }
        }
        else offscreen.fill(new Ellipse2D.Double(xs - rs/2, ys - rs/2, rs, rs));
        show();
    }

    // draw filled circle of radius r, centered on (x, y); degenerate to pixel if small
    public  void filledCircle_bg(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("circle radius can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else background.fill(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
    }

    // draw arc of radius r, centered on (x, y), from angle1 to angle2 (in degrees)
    public  void arc(double x, double y, double r, double angle1, double angle2) {
        if (r < 0) throw new RuntimeException("arc radius can't be negative");
        while (angle2 < angle1) angle2 += 360;
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Arc2D.Double(xs - ws/2, ys - hs/2, ws, hs, angle1, angle2 - angle1, Arc2D.OPEN));
        show();
    }

    // draw squared of side length 2r, centered on (x, y); degenerate to pixel if small
    public  void square(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("square side length can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) {
            pixel(x, y);
        } else {
            offscreen.draw(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        }
        show();
    }

    // draw squared of side length 2r, centered on (x, y); degenerate to pixel if small
    public  void square_bg(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("square side length can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) {
            pixel(x, y);
        } else {
            background.draw(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        }
    }

    // draw squared of side length 2r, centered on (x, y); degenerate to pixel if small
    public  void filledSquare(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("square side length can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) {
            pixel(x, y);
        } else {
            offscreen.fill(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        }
        show();
    }

    // draw squared of side length 2r, centered on (x, y); degenerate to pixel if small
    public  void filledSquare_bg(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("square side length can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else background.fill(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
    }

    // draw a polygon with the given (x[i], y[i]) coordinates
    public  void polygon(double[] x, double[] y) {
        int N = x.length;
        GeneralPath path = new GeneralPath();
        path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));
        for (int i = 0; i < N; i++)
            path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
        path.closePath();
        offscreen.draw(path);
        show();
    }

    // draw a polygon with the given (x[i], y[i]) coordinates
    public  void polygon_bg(double[] x, double[] y) {
        int N = x.length;
        GeneralPath path = new GeneralPath();
        path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));
        for (int i = 0; i < N; i++)
            path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
        path.closePath();
        background.draw(path);
    }

    // draw a filled polygon with the given (x[i], y[i]) coordinates
    public  void filledPolygon(double[] x, double[] y) {
        int N = x.length;
        GeneralPath path = new GeneralPath();
        path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));
        for (int i = 0; i < N; i++)
            path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
        path.closePath();
        offscreen.fill(path);
        show();
    }

    // draw a filled polygon with the given (x[i], y[i]) coordinates
    public  void filledPolygon_bg(double[] x, double[] y) {
        int N = x.length;
        GeneralPath path = new GeneralPath();
        path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));
        for (int i = 0; i < N; i++)
            path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
        path.closePath();
        background.fill(path);
    }



   /*************************************************************************
    *  Drawing images.
    *************************************************************************/

    // get an image from the given filename
    private Image getImage(String filename) {

        // to read from file
        ImageIcon icon = new ImageIcon(filename);

        // try to read from URL
        if ((icon == null) || (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
            try {
                URL url = new URL(filename);
                icon = new ImageIcon(url);
            } catch (Exception e) { /* not a url */ }
        }

        // in case file is inside a .jar
        if ((icon == null) || (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
            URL url = StdDraw.class.getResource(filename);
            if (url == null) throw new RuntimeException("image " + filename + " not found");
            icon = new ImageIcon(url);
        }

        return icon.getImage();
    }

    // draw picture (gif, jpg, or png) centered on (x, y)
    public  void picture(double x, double y, String s) {
        Image image = getImage(s);
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = image.getWidth(null);
        int hs = image.getHeight(null);
        if (ws < 0 || hs < 0) throw new RuntimeException("image " + s + " is corrupt");

        offscreen.drawImage(image, (int) Math.round(xs - ws/2.0), (int) Math.round(ys - hs/2.0), null);
        show();
    }

    // draw picture (gif, jpg, or png) centered on (x, y), rescaled to w-by-h
    public  void picture(double x, double y, String s, double w, double h) {
        Image image = getImage(s);
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(w);
        double hs = factorY(h);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else {
            offscreen.drawImage(image, (int) Math.round(xs - ws/2.0),
                                       (int) Math.round(ys - hs/2.0),
                                       (int) Math.round(ws),
                                       (int) Math.round(hs), null);
        }
        show();
    }


   /*************************************************************************
    *  Drawing text.
    *************************************************************************/
    public void text_line(String s) {
        text(text_point_x, text_point_y, s);
    }

    public void setupFont(Font font, Graphics2D graphics) {
        graphics.setFont(font);
        FontMetrics metrics = graphics.getFontMetrics();
        line_height = unfactorY(metrics.getHeight());
    }

    public double getLineHeight() {
        return line_height;
    }
    
    public void next_line() {
        text_point_y -= line_height;
    }
        
    public void set_text_point(double x, double y) {
        text_point_x = x;
        text_point_y = y;
    }

    
    // write the given text string in the current font, center on (x, y)
    public  void text(double x, double y, String s) {
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int hs = metrics.getDescent();
        offscreen.drawString(s, (float) xs, (float) (ys + hs));
        show();
    }

    // write the given text string in the current font, center on (x, y)
    public  void text_center(double x, double y, String s) {
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = metrics.stringWidth(s);
        int hs = metrics.getDescent();
        offscreen.drawString(s, (float) (xs - ws/2.0), (float) (ys + hs));
        show();
    }

    public  void text_bg_center(double x, double y, String s) {
        System.out.println("Drawing text BG [" + s + "]");
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = metrics.stringWidth(s);
        int hs = metrics.getDescent();
        background.drawString(s, (float) (xs - ws/2.0), (float) (ys + hs));
        show();
    }

    public  void text_bg(double x, double y, String s) {
        System.out.println("Drawing text BG [" + s + "]");
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int hs = metrics.getDescent();
        background.drawString(s, (float) xs, (float) (ys + hs));
        show();
    }

    // display on screen and pause for t milliseconds
    public  void show(int t) {
        defer = true;
        onscreen.drawImage(offscreenImage, 0, 0, null);
        //frame.repaint();
        try { Thread.sleep(t); }
        catch (InterruptedException e) { System.out.println("Error sleeping"); }
    }

    public  void show(boolean defer) {
        this.defer = defer;
        onscreen.drawImage(offscreenImage, 0, 0, null);
        //frame.repaint();
    }


    // display on-screen
    public  void show() {
        if (!defer) onscreen.drawImage(offscreenImage, 0, 0, null);
        //if (!defer) frame.repaint();
    }


   /*************************************************************************
    *  Save drawing to a file.
    *************************************************************************/

    // save to file - suffix must be png, jpg, or gif
    public  void save(String filename) {
        File file = new File(filename);
        String suffix = filename.substring(filename.lastIndexOf('.') + 1);

        // png files
        if (suffix.toLowerCase().equals("png")) {
            try { ImageIO.write(offscreenImage, suffix, file); }
            catch (IOException e) { e.printStackTrace(); }
        }

        // need to change from ARGB to RGB for jpeg
        // reference: http://archives.java.sun.com/cgi-bin/wa?A2=ind0404&L=java2d-interest&D=0&P=2727
        else if (suffix.toLowerCase().equals("jpg")) {
            WritableRaster raster = offscreenImage.getRaster();
            WritableRaster newRaster;
            newRaster = raster.createWritableChild(0, 0, width, height, 0, 0, new int[] {0, 1, 2});
            DirectColorModel cm = (DirectColorModel) offscreenImage.getColorModel();
            DirectColorModel newCM = new DirectColorModel(cm.getPixelSize(),
                                                          cm.getRedMask(),
                                                          cm.getGreenMask(),
                                                          cm.getBlueMask());
            BufferedImage rgbBuffer = new BufferedImage(newCM, newRaster, false,  null);
            try { ImageIO.write(rgbBuffer, suffix, file); }
            catch (IOException e) { e.printStackTrace(); }
        }

        else {
            System.out.println("Invalid image file type: " + suffix);
        }
    }


    // open a save dialog when the user selects "Save As" from the menu
    public void actionPerformed(ActionEvent e) {
        for(Iterator it = actionListeners.iterator(); it.hasNext(); ) {
            ActionListener ears = (ActionListener) it.next();
            ears.actionPerformed(e);
        }
    }


   /*************************************************************************
    *  Mouse interactions.
    *************************************************************************/

    public  boolean mousePressed() { return mousePressed; }
    public  double mouseX()        { return mouseX;       }
    public  double mouseY()        { return mouseY;       }
    

    public void mouseClicked (MouseEvent e) { }
    public void mouseEntered (MouseEvent e) { }
    public void mouseExited  (MouseEvent e) { }
    public void mousePressed (MouseEvent e) {
        mouseX = userX(e.getX());
        mouseY = userY(e.getY());
        mousePressed = true;
    }
    public void mouseReleased(MouseEvent e) { mousePressed = false; }
    public void mouseDragged(MouseEvent e)  {
        mouseX = userX(e.getX());
        mouseY = userY(e.getY());
    }

    public void mouseMoved(MouseEvent e) {
        mouseX = userX(e.getX());
        mouseY = userY(e.getY());
    }    


   /*************************************************************************
    *  Keyboard interactions.
    *************************************************************************/

    public  boolean hasNextKeyTyped() { return lastKeyTyped != (char)-1; }
    public  char nextKeyTyped() {
        char c = lastKeyTyped;
        lastKeyTyped = (char)-1;
        return c;
    }

    public void  keyTyped(KeyEvent e) {
        lastKeyTyped = e.getKeyChar();
    }
    public void keyPressed   (KeyEvent e)   { }
    public void keyReleased  (KeyEvent e)   { }




    // test client
    public static void main(String[] args) {
        StdDraw drawer = new StdDraw(0);
        drawer.square(.2, .8, .1);
        drawer.filledSquare(.8, .8, .2);
        drawer.circle(.8, .2, .2);

        drawer.setPenColor(StdDraw.MAGENTA);
        drawer.setPenRadius(.02);
        drawer.arc(.8, .2, .1, 200, 45);

        // draw a blue diamond
        drawer.setPenRadius();
        drawer.setPenColor(StdDraw.BLUE);
        double[] x = { .1, .2, .3, .2 };
        double[] y = { .2, .3, .2, .1 };
        drawer.filledPolygon(x, y);

        // text
        drawer.text(0.8, 0.2, "centered");
    }

}
