/***************************************************
 * Copyright 2007 by Simran Gleason                *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.text.NumberFormat;

/**
 * Control panel to change various parameters on kepler's orrery.
 *
 *   Super class that handles connection to the Kepler object, and such.
 *   subclass this for particular interfaces. 
 */

public class ControlPanel {
    protected Kepler kepler;
    protected Orrery orrery;
    protected JPanel controlPanel;
    protected int keepWidth = 0;
    /** for number formatting */
    protected NumberFormat doublesFormat;
       

    public ControlPanel(Kepler kepler) {
        this.kepler = kepler;
        this.orrery = kepler.getOrrery();
        doublesFormat = NumberFormat.getInstance();
        doublesFormat.setMinimumFractionDigits(4);
        doublesFormat.setGroupingUsed(false);
    }

    public void setPanel(JPanel  controlPanel) {
        this.controlPanel = controlPanel;
    }

    public JPanel getPanel() {
        return this.controlPanel;
    }

    /**
     * override this to receive indication that a new world has been read in.
     */
    public void worldRead() {
    }
    /**
     * override this to receive indication that a new world has started
     */
    public void dawnOfCreation() {
    }

    /**
     * override this to receive indication that a world has met its demise
     */
    public void endOfTheWorld() {
    }

    /**
     * override this to receive indication that a mode change that could affect
     * a control panel has happened, e.g.: a world has been paused or unpaused
     */
    public void setMode(String modeName, boolean val) {
    }
    
    public void show(boolean show) {
        this.getPanel().setVisible(show);
    }
    
    public void show() {
        if (keepWidth > 0) {
            controlPanel.setSize(keepWidth, controlPanel.getHeight());
        }
        show(true);
        kepler.repack();
    }

    public void hide() {
        keepWidth = getPanel().getWidth();
        controlPanel.setSize(0, controlPanel.getHeight());
        show(false);
    }

    //////////////////////////////////////////////////////////
    /// Utility routines for making control panel widgets  ///
    //////////////////////////////////////////////////////////
    protected JLabel makeTextLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(orrery.controlPanelFG);
        //l.setMargin(new Insets(0, 0, 0, 0));
        l.setBorder(new EmptyBorder(0, 0, 0, 0));
        return l;
    }

    protected JLabel makeValueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(orrery.controlPanelValueFG);
        //l.setMargin(new Insets(0, 0, 0, 0));
        l.setBorder(new EmptyBorder(0, 0, 0, 0));
        return l;
    }
    
    protected JLabel makeImageLabel(String image, String tooltip) {
        JLabel l =
            new JLabel(Kepler.createImageIcon(image, tooltip));
        l.setForeground(orrery.controlPanelFG);
        //l.setMargin(new Insets(0, 0, 0, 0));
        l.setBorder(new EmptyBorder(0, 0, 0, 0));
        return l;
    }



    protected JLabel makeTitleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(orrery.titleColor);
        //l.setMargin(new Insets(0, 0, 0, 0));
        l.setBorder(new EmptyBorder(0, 0, 0, 0));
        return l;
    }

    protected JTextField makeNumberField() {
        JTextField text = new JTextField();
        text.setBorder(new MatteBorder(1, 2, 1, 3, orrery.controlPanelValueBG));
        text.setForeground(orrery.controlPanelValueFG);
        text.setBackground(orrery.controlPanelValueBG);
        text.setColumns(9);
        return text;
    }

    protected JTextField makeTextField(String initialValue, int columns) {
        JTextField jtf = makeTextField(initialValue);
        jtf.setColumns(columns);
        return jtf;
    }


        
    protected JTextField makeTextField(int columns) {
        JTextField jtf = makeTextField();
        jtf.setColumns(columns);
        return jtf;
    }

    protected JTextField makeTextField(String initialValue) {
        JTextField jtf = makeTextField();
        jtf.setText(initialValue);
        return jtf;
    }

    protected JTextField makeTextField() {
        JTextField text = new JTextField();
        text.setBorder(new MatteBorder(1, 2, 1, 3, orrery.controlPanelBG));
        text.setForeground(orrery.controlPanelValueFG);
        text.setBackground(orrery.controlPanelValueBG);
        text.setColumns(12);
        return text;
    }

    protected JTextArea makeTextArea() {
        return makeTextArea(5, 20, orrery.controlPanelBG, orrery.controlPanelValueBG);
    }
    
    protected JTextArea makeTextArea(int row, int col,
                                     Color backgroundColor,
                                     Color valueBackgroundColor) {
        JTextArea text = new JTextArea(row, col);
        text.setLineWrap(true);
        text.setBorder(new MatteBorder(1, 2, 1, 3, backgroundColor));
        text.setForeground(orrery.controlPanelValueFG);
        text.setBackground(valueBackgroundColor);
        return text;
    }

    protected JButton makeTextButton(String title) {
        JButton button = new JButton(title);
        button.setBorder(new MatteBorder(1, 1, 1, 1, orrery.controlPanelFG));
        button.setForeground(orrery.controlPanelFG);
        button.setBackground(orrery.controlPanelBG);
        return button;
    }

    protected JButton makeImageButton(String imageUp, String imageDown,
                                      String disabledImage,
                                      String tooltip) {
        JButton button = makeImageButton(imageUp, imageDown, tooltip);
        ImageIcon disabledIcon = Kepler.createImageIcon(disabledImage, tooltip);
        button.setDisabledIcon(disabledIcon);
        return button;
    }
        
    protected JButton makeImageButton(String imageUp, String imageDown, String tooltip) {
        JButton button =
            new JButton(Kepler.createImageIcon(imageUp, tooltip));
        ImageIcon pressedIcon = Kepler.createImageIcon(imageDown, tooltip);
        button.setBorderPainted(false);
        button.setPressedIcon(pressedIcon);
        button.setSelectedIcon(pressedIcon);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setContentAreaFilled(false);
        return button;
    }

    protected JToggleButton makeImageToggleButton(String imageUp, String imageDown, String disabledImage, String tooltip) {
        JToggleButton tbutton = makeImageToggleButton(imageUp, imageDown, tooltip);
        ImageIcon disabledIcon = Kepler.createImageIcon(disabledImage, tooltip);
        tbutton.setDisabledIcon(disabledIcon);
        tbutton.setDisabledSelectedIcon(disabledIcon);
        return tbutton;
    }
        
    protected JToggleButton makeImageToggleButton(String imageUp, String imageDown, String tooltip) {
        JToggleButton tbutton =
            new JToggleButton(Kepler.createImageIcon(imageUp, tooltip));
        ImageIcon pressedIcon = Kepler.createImageIcon(imageDown, tooltip);
        tbutton.setBorderPainted(false);
        tbutton.setPressedIcon(pressedIcon);
        tbutton.setSelectedIcon(pressedIcon);
        tbutton.setMargin(new Insets(0, 0, 0, 0));
        tbutton.setContentAreaFilled(false);
        return tbutton;
    }

    protected JRadioButton makeRadioButton(String imageOn, String imageOff, String tooltip) {
        ImageIcon offIcon = Kepler.createImageIcon(imageOff, tooltip);
        ImageIcon onIcon = Kepler.createImageIcon(imageOn, tooltip);
        JRadioButton tbutton =
            new JRadioButton(offIcon);
        tbutton.setBorderPainted(false);
        tbutton.setPressedIcon(onIcon);
        tbutton.setSelectedIcon(onIcon);
        tbutton.setMargin(new Insets(0, 0, 0, 0));
        tbutton.setContentAreaFilled(false);
        return tbutton;
    }

    protected JComboBox makeDropDown() {
        JComboBox drop = new JComboBox();
        drop.setBorder(new MatteBorder(1, 2, 1, 3, orrery.controlPanelBG));
        drop.setForeground(orrery.controlPanelValueFG);
        drop.setBackground(orrery.controlPanelValueBG);
        drop.setLightWeightPopupEnabled(false);
             
        return drop;
    }

    protected JSlider makeSliderH() {
        JSlider slider = new JSlider();
        slider.setName("slider");
        slider.setPreferredSize(new Dimension(200, 60));
        slider.setOrientation(JSlider.HORIZONTAL);
        slider.setBackground(orrery.controlPanelBG);

        return slider;
    }

    protected JSlider makeSliderV() {
        JSlider slider = new JSlider();
        slider.setName("slider");
        slider.setOrientation(JSlider.VERTICAL);
        slider.setPreferredSize(new Dimension(30, 200));
        slider.setMinimumSize(new Dimension(30, 200));
        slider.setBackground(orrery.controlPanelBG);

        return slider;
    }



    // later: cookLogValue((int raw, double min, double max)
    // turn an actual value into a  positional value (e.g. slider)
    public double cookLinearValue(int raw, double min, double max) {
        int rangeHi = 100;
        int rangeLow = 0;
        double cooked;
        double range = max - min;
        double position = (double) (raw - rangeLow) / (double) (rangeHi - rangeLow);
        cooked = position * range;
        return cooked;
    }
    
    // turn a positional value (e.g. slider) into the actual value
    // TODO: refactor to superclass
    public int uncookLinearValue(double cooked, double min, double max) {
        int rangeHi = 100;
        int rangeLow = 0;
        int uncooked;
        double range = max - min;
        double position = cooked / range;
        
        uncooked = (int)((rangeHi - rangeLow) * position) + rangeLow;
        return uncooked;
    }
    
    public double cookFactorValue(int raw, int unityPoint, double maxFactor) {
        double cooked;
        int rangeHi = 100;
        int rangeLow = 0;
        // if below the UnityPoint, range from 0. to 1.
        if (raw < unityPoint) {
            double position = raw - rangeLow;
            double lowerRange = unityPoint - rangeLow; 
            cooked = position / lowerRange;
        } else {
            double position = raw - unityPoint;
            double upperRange = rangeHi - unityPoint;
            cooked = 1.0 + (maxFactor - 1.0) * position / upperRange; 
        }
        return cooked;
    }

    public int uncookFactorValue(double cooked, int unityPoint, double maxFactor) {
        int rangeHi = 100;
        int rangeLow = 0;
        double uncooked;
        // if below the UnityPoint, range from 0. to 1.
        //System.out.println("UNCookFactort. cooked="+ cooked);
        if (cooked < 1.) {
            double lowerRange = unityPoint - rangeLow;
            uncooked = cooked * lowerRange;
            //System.out.println("  lowerRange: " + lowerRange + " uncooked: " + uncooked);
        } else {
            double upperRange = rangeHi - unityPoint;
            double position = cooked - 1.0;
            uncooked = unityPoint + upperRange * position / maxFactor; 
            //System.out.println("  upperRange: " + upperRange + " position: " + position + " uncooked: " + uncooked);
        }
        return (int)uncooked;
    }


    protected int extractInt(JTextField text, String name) throws DataException {
        try {
            int i = Integer.parseInt(text.getText());
            return i;
        } catch (NumberFormatException ex) {
            throw new DataException(name, "", ex);
        }
    }

    protected int extractInt(JTextField text, String name, int defaultValue) {
        try {
            int i = Integer.parseInt(text.getText());
            return i;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    protected float extractFloat(JTextField text, String name) throws DataException {
        try {
            float f = Float.parseFloat(text.getText());
            return f;
        } catch (NumberFormatException ex) {
            throw new DataException(name, "", ex);
        }
    }

    protected float extractFloat(JTextField text, String name, float defaultValue) {
        try {
            float f = Float.parseFloat(text.getText());
            return f;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    protected double extractDouble(JTextField text, String name) throws DataException {
        try {
            double f = Double.parseDouble(text.getText());
            return f;
        } catch (NumberFormatException ex) {
            throw new DataException(name, "", ex);
        }
    }

    protected double extractDouble(JTextField text, String name, double defaultValue) {
        try {
            double f = Double.parseDouble(text.getText());
            return f;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    protected boolean emptyText(JTextField text) {
        String t = text.getText();
        return  (t == null || t.equals(""));
    }
    
    protected void displayText(JTextField text, String valueStr) {
        text.setText(valueStr);
    }

    protected void displayText(JTextField text, double value) {
        // TODO: use numberFormat to get the decimals correct etc.
        String valueStr;
        if (Math.abs(value) > 1.E6) {
            valueStr = "" + value;
        } else {
            valueStr = doublesFormat.format(value);
        }
        text.setText(valueStr);
    }

    protected void displayText(JTextField text, int value) {
        // TODO: use numberFormat to get the decimals correct etc. 
        text.setText("" + value);
    }

    protected void displayText(JTextField text, boolean value) {
        // TODO: use numberFormat to get the decimals correct etc. 
        text.setText("" + value);
    }

    protected void displayText(JLabel text, String valueStr) {
        text.setText(valueStr);
    }

    protected void displayText(JLabel text, double value) {
        // TODO: use numberFormat to get the decimals correct etc.
        String valueStr;
        if (Math.abs(value) > 1.E6) {
            valueStr = "" + value;
        } else {
            valueStr = doublesFormat.format(value);
        }
        text.setText(valueStr);
    }

    protected void displayText(JLabel text, int value) {
        // TODO: use numberFormat to get the decimals correct etc. 
        text.setText("" + value);
    }

    protected void displayText(JLabel text, boolean value) {
        // TODO: use numberFormat to get the decimals correct etc. 
        text.setText("" + value);
    }

    class DataException extends Exception {
        /** the offending data field */
        String field;
        public DataException(String field, String message, Throwable rootCause) {
            super(message, rootCause);
            this.field = field;
        }
        public String getField() {
            return this.field;
        }
    }
}


        
        