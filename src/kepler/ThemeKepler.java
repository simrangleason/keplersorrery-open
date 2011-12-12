/***************************************************
 * Copyright 2007 by Simran Gleason                *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;

import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * This class describes a theme using "primary" colors.
 * You can change the colors to anything else you want.
 *
 * 1.9 07/26/04
 */
public class ThemeKepler extends DefaultMetalTheme {
    Orrery orrery;

    private ColorUIResource fg, valueFG, titleFG;
    private ColorUIResource bg, valueBG, selectedBG;
    private ColorUIResource focus;
    private ColorUIResource controlShadow;
    private ColorUIResource primary2;
    private ColorUIResource primary3;
    private ColorUIResource pcshadow;
    private ColorUIResource border, borderDark, borderHighlight;
    private ColorUIResource systext, contentText;
    
    public ThemeKepler(Orrery orrery) {
        this.orrery  = orrery;
        titleFG       = new ColorUIResource(orrery.controlPanelTitle);
        fg            = new ColorUIResource(orrery.controlPanelFG);
        valueFG       = new ColorUIResource(orrery.controlPanelValueFG);
        bg            = new ColorUIResource(orrery.controlPanelBG);
        valueBG       = new ColorUIResource(orrery.controlPanelValueBG);
        selectedBG    = new ColorUIResource(orrery.controlPanelSelectedBG);
        focus         = new ColorUIResource(orrery.controlPanelBG);
        primary2      = new ColorUIResource(Color.RED); // orrery.controlPanelFG);
        primary3      = new ColorUIResource(orrery.controlPanelBG);
        controlShadow = new ColorUIResource(orrery.controlPanelSubBorder);
        pcshadow      = new ColorUIResource(orrery.controlPanelBorder);
        systext       = new ColorUIResource(orrery.controlPanelFG);
        border        = new ColorUIResource(orrery.controlPanelBorder);
        borderDark    = new ColorUIResource(orrery.controlPanelSubBorder);
        borderHighlight = new ColorUIResource(orrery.controlPanelBorderHighlight);
        contentText   = new ColorUIResource(orrery.controlPanelValueFG);
    }

    public String getName() { return "Kepler"; }


    public    ColorUIResource getFocusColor()               { return focus; }
    protected ColorUIResource getPrimary3()                 { return borderDark; }
    public    ColorUIResource getPrimaryControlShadow()     { return selectedBG; }
    public    ColorUIResource getPrimaryControlDarkShadow() { return borderDark; }
    public    ColorUIResource getPrimaryControlHighlight()  { return borderHighlight; } 
    public    ColorUIResource getControl()                  { return bg; }
    public    ColorUIResource getControlShadow()            { return controlShadow; }
    public    ColorUIResource getControlDarkShadow()        { return borderDark; }
    public    ColorUIResource getControlHighlight()         { return borderDark; }
    public    ColorUIResource getMenuBackground()           { return valueBG; }
    public    ColorUIResource getMenuSelectedBackground()   { return selectedBG; }
    public    ColorUIResource getWindowBackground()         { return valueBG; }
    public    ColorUIResource getWindowTitleBackground()    { return bg; }
    public    ColorUIResource getSystemTextColor()          { return systext; }
    public    ColorUIResource getControlTextColor()         { return fg; }
    public    ColorUIResource getUserTextColor()            { return valueFG; }
    

}
