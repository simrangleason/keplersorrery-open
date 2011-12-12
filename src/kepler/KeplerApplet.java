/***************************************************
 * Copyright 2007 by Simran Gleason                *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;
import java.applet.Applet;
import com.sun.j3d.utils.applet.MainFrame;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JLabel;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

import util.TokenReader;

public class KeplerApplet extends Applet  {
    protected Container drawingPane;
    private boolean loopPlaylist = true;
    private String playlistName = "";
    private ArrayList playlistNames = null;
    private boolean shuffle = false;
    private int playTimeLow = 60;
    private int playTimeHigh = 90;
    private int cycleTime = 0; // i.e. use default
    private int canvas = 600;
    private Kepler kepler = null;
    private HashMap localParams = null;

    public String getLocalParameter(String pname) {
        if (localParams == null) {
            return null;
        }
        return (String)localParams.get(pname);
    }

    public void setLocalParameter(String pname, String value) {
        if (localParams == null) {
            localParams = new HashMap();
        }
        localParams.put(pname, value);
    }

    public String getParameter(String pname) {
        String result = getLocalParameter(pname);
        if (result == null) {
            result = super.getParameter(pname);
        }
        return result;
    }
                             
    public String getParameter(String pname, String defaultValue) {
        String p = getParameter(pname);
        if (p == null) {
            p = defaultValue;
        }
        return p;
    }

    public ArrayList getStringListParameter(String pname, char splitchar, ArrayList defaultValue) {
        String p = getParameter(pname);
        if (p == null) {
            return defaultValue;
        }
        ArrayList splat = splitString(p, splitchar);
        return splat;
    }

    private ArrayList splitString(String source, char splitchar) {
        // not using splitchar at the moment...
        TokenReader streader = TokenReader.makeStringTokenReader(source);
        ArrayList splits = new ArrayList();
        try {
            String token = streader.readToken();
            while (token != null) {
                splits.add(token);
                token = streader.readToken();
            }
        } catch (IOException ex) {
            System.out.println("KeplerApplet caught exception splitting string " + source + ": " + ex);
        }
        return splits;
    }
            
    public int getIntParameter(String pname, int defaultValue) {
        String p = getParameter(pname);
        if (p != null) {
            try {
                return Integer.parseInt(p);
            } catch (NumberFormatException ex) {
            }
        }
        return defaultValue;
    }

    public float getFloatParameter(String pname, float defaultValue) {
        String p = getParameter(pname);
        if (p != null) {
            try {
                return Float.parseFloat(p);
            } catch (NumberFormatException ex) {
            }
        }
        return defaultValue;
    }

    public boolean getBooleanParameter(String pname, boolean defaultValue) {
        String p = getParameter(pname);
        if (p != null) {
            try {
                return Boolean.valueOf(p).booleanValue();
            } catch (Exception ex) {
            }
        }
        return defaultValue;
    }

    public void start() {
        canvas = getIntParameter("canvas", canvas);
        // TODO: allow multiple playlists
        //playlistName = getParameter("playlist", playlistName);
        playlistNames = getStringListParameter("playlist", ' ', new ArrayList());
        String timep = getParameter("time");
        if (timep != null) {
            int time = getIntParameter("time", playTimeLow);
            playTimeLow = time;
            playTimeHigh = time;
        }
        cycleTime = getIntParameter("cycletime", 0);
        shuffle = getBooleanParameter("shuffle", shuffle);
        loopPlaylist = getBooleanParameter("loop", loopPlaylist);
        float initialInfoSeconds = getFloatParameter("info", 1.f);
    
        // pass in the panel.?
        boolean kioskMode = true;
        PlaylistManager playlistManager = new PlaylistManager();
        // need to read the playlists here...
        readPlaylists(playlistManager, playlistNames);
        
        kepler = new Kepler(canvas, playlistManager, kioskMode);
        kepler.setLookAndFeel();
        kepler.prependIconDir("applet");
        drawingPane = kepler.drawer.getDrawingPane();
        kepler.initOrrery();
        kepler.setTheme(kepler.orrery);
        kepler.orrery.setInitialDisplayInfoSeconds(initialInfoSeconds);

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 0.0;
        this.add(drawingPane, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 1.0;
        this.add(new JLabel(""), gbc);

        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 2, 10, 5);
        WorldBuilder wb = new WorldBuilder(kepler, kepler.orrery.buildBG);
        kepler.orrery.setWorldBuilder(wb);
        kepler.setWorldBuilder(wb);
        KioskControlPanel kcp = new KioskControlPanel(kepler, wb);
        kepler.controlPanels.add(kcp);
        this.add(kcp.getPanel(), gbc);
        kcp.show();
        
        /*  replacing the old mission control panel with the new kiosk control panel
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 2, 10, 5);
        boolean includeFrameControls = false;
        MissionControlPanel missionControl = new MissionControlPanel(kepler, kepler.orrery.playBG);
        this.add(missionControl.getPanel(), gbc);
        kepler.addControlPanel(missionControl);
        missionControl.show();

        gbc.gridy++;
        BottomControlsPanel bottomControls = new BottomControlsPanel(kepler, includeFrameControls);
        this.add(bottomControls.getPanel(), gbc);
        kepler.addControlPanel(bottomControls);
        bottomControls.show();
        */
        /* old one-playlist way:
        Playlist playlist = new Playlist();
        playlistManager.addPlaylist(playlist);
        try {
            Kepler.readPlaylistFile(playlistName, playlist);
        } catch (IOException ex) {
            // what to do in applets?
        }
        */
        playlistManager.setLoop(loopPlaylist);
        playlistManager.setShuffle(shuffle);
        playlistManager.compactPlaylists();
        playlistManager.reset();
        kepler.setTrails(0);
        // on for debugging ...
        //kepler.orrery.showInfoDisplay(true);
        this.setBackground(kepler.orrery.bgColor);  this.addKeyListener(kepler);    

        if (cycleTime > 0 ) {
            kepler.orrery.setCycleTime(cycleTime);
        }
        kepler.setPlayTimes(playTimeLow, playTimeHigh);
        kepler.startKepling();
    
    }

    private void readPlaylists(PlaylistManager playlistManager, ArrayList playlistNames) {
        for(Iterator it = playlistNames.iterator(); it.hasNext(); ) {
            String playlistName = (String)it.next();
            System.out.println(" ADDING playlist: " + playlistName);
            Playlist playlist = new Playlist();
            playlistManager.addPlaylist(playlist);
            try {
                Kepler.readPlaylistFile(playlistName, playlist);
            } catch (IOException ex) {
                System.out.println("Caught exception reading playlist: " + playlistName);
                ex.printStackTrace();
                // what to do in applets?
            }
        }
        System.out.println("APPLET read playlists. ->" + playlistManager.numPlaylists());
    }

    public void stop() {
        if (kepler != null) {
            kepler.orrery.midiAllOff();
            kepler.orrery.stopDrawingThread();
            kepler.stopKepling();
        }
    }

    public static void main(String[] args) {
        KeplerApplet applet = new KeplerApplet();
        // set the args?
        applet.setLocalParameter("canvas", "680");
        applet.setLocalParameter("time", "80");
        applet.setLocalParameter("loop", "true");
        applet.setLocalParameter("shuffle", "true");
        applet.setLocalParameter("playlist",
                                 "http://art.net/simran/GenerativeMusic/Kepler/Worlds/artdotnet_music.kpl http://art.net/simran/GenerativeMusic/Kepler/Worlds/artdotnet_symmetries.kpl http://art.net/simran/GenerativeMusic/Kepler/Worlds/artdotnet_exercises.kpl");
        new MainFrame(applet, 950, 780);
    }

}