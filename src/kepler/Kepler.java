/*************************************************************************
 * Kepler
 *  Implements the Main program wrapper of Kepler's Orrery.
 *
 *  Portions of this system were derived from code supplied with
 *        Introduction to Programming in Java:  An Interdisciplinary Approach,
 *        Robert Sedgewick and Kevin Wayne, Addison-Wesley, 2007. 
 *        ISBN 0-321-49805-4
 *        http://www.cs.princeton.edu/IntroProgramming
 *
 *    Specifically:
 *        Orrery.increaseTime() and Orrery.draw(), from Universe.java
 *        Body.forceTo(), from Body.java
 *        util.StdDraw.java, from StdDraw.java
 *        util.TokenReader, from StdIn.java
 *        parts of Vect, from Vector.java
 *
 *************************************************************************/

/***************************************************
 * Copyright 2007 by Simran Gleason                *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;

import util.StdDraw;
import util.TokenReader;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

public class Kepler implements ActionListener, KeyListener {

    protected Orrery orrery;
    protected WorldBuilder worldBuilder = null;
    protected MidiStuff midiStuff;
    protected IndraThread indraThread;
    protected boolean fullscreen = false;
    protected boolean decorated = false;
    protected JFrame frame;
    protected Container drawingPane;
    protected ArrayList controlPanels;
    
    StdDraw drawer;
    TokenReader reader;

    protected double dt = 75.;
    protected int playTimeLo = 0;
    protected int playTimeHi = 0;
    protected PlaylistManager playlistManager = null;
    protected String playlistPathPrefix = null;

    private   int initialPause = 0;
    protected int trails = 0;
    protected int saved_trails = 0;
    protected boolean reloadWorld =  false;
    protected String targetDirectory = null;
    protected boolean kioskMode = true;
    protected boolean showMenuBar = false;
    protected static ArrayList iconPath;
    protected static String iconBaseDir;
    
    public Kepler(int canvasSize, PlaylistManager playlistManager, boolean kioskMode) {
        drawer = new StdDraw(canvasSize);
        reader = new TokenReader();
        this.kioskMode = kioskMode;
        if (kioskMode) {
            prependIconDir("kiosk");
        }
        controlPanels = new ArrayList();
        this.playlistManager = playlistManager;
    }

    static {
        iconBaseDir = "images";
        iconPath = new ArrayList();
        prependIconDir("base");
    }
    
    public void initOrrery() {
        orrery = new Orrery(reader, drawer);
	orrery.setKepler(this);
        orrery.setRepaintComponent(drawer.getDrawingPane());
    }

    public Orrery getOrrery() {
        return this.orrery;
    }

    public void setWorldBuilder(WorldBuilder wb) {
        this.worldBuilder = wb;
    }

    public WorldBuilder getWorldBuilder() {
        return this.worldBuilder;
    }

    private static void setupControlPanels(Kepler kepler) {
        WorldBuilder wb = new WorldBuilder(kepler, kepler.orrery.buildBG);
        kepler.orrery.setWorldBuilder(wb);
        kepler.orrery.addNoteListener(wb);
        kepler.setWorldBuilder(wb);
        if (kepler.kioskMode()) {
            kepler.controlPanels.add(new KioskControlPanel(kepler, wb));
        } else {
            MissionControlPanel mcp = new MissionControlPanel(kepler, kepler.orrery.playBG); 
            if (kepler.targetDirectory != null) {
                wb.targetDirectory = kepler.targetDirectory;
            }
            kepler.controlPanels.add(mcp);
            kepler.controlPanels.add(wb);
        }
    }
        

    public static JFrame recreateFrame(JFrame frame, Kepler kepler, StdDraw drawer, boolean decorated) {
        frame = new JFrame();
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(frame);
        } 
        catch (Exception  e) {
            System.err.println("Could not load LookAndFeel: " + e);
            e.printStackTrace();
            System.out.println(" System LAF classname: " + UIManager.getSystemLookAndFeelClassName());
            System.out.println(" System CP LAF classname: " + UIManager.getCrossPlatformLookAndFeelClassName() );
        }
        frame.setBackground(kepler.orrery.controlPanelBG);
        Kepler.KeplerWindowListener kwl = new KeplerWindowListener(kepler);
        frame.addWindowListener(kwl);
        frame.addWindowStateListener(kwl);
        kepler.drawingPane = drawer.getDrawingPane();
        // the frame for drawing to the screen
        frame.setVisible(false);

        setupControlPanels(kepler);

        Container content = frame.getContentPane();

        // possible trick to get fullscreen working?
        if (content instanceof JComponent) {
            ((JComponent)content).setOpaque(false);
        }
        content.setLayout(new GridBagLayout());
        content.setBackground(kepler.orrery.controlPanelBG);
        GridBagConstraints gbc = new GridBagConstraints();

        /* this seems to be messing up our coordinates on resize
        // floater thing to keep the universe centered
        gbc.gridx=0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 2;
        content.add(new JLabel(""), gbc);
        */

        gbc.gridx = 0;
        gbc.gridy = 0;
        //gbc.gridheight = 2;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 0, 10, 0);
        //gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        //((JLabel)kepler.drawingPane).setBorder(new MatteBorder(0, 0, 0, 1, Color.RED));

        content.add(kepler.drawingPane, gbc);
        //frame.setContentPane(kepler.drawingPane);

        // floater thing to keep the universe at the top of the pane
        gbc.gridx=0;
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 2;
        JLabel spacer = new JLabel("");
        gbc.fill = GridBagConstraints.VERTICAL;
        //spacer.setBorder(new MatteBorder(0, 0, 0, 1, Color.BLUE));

        content.add(spacer, gbc);

        frame.setResizable(decorated);
        frame.setUndecorated(!decorated);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);            // closes all windows
        // frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);      // closes only current window
        frame.setTitle("Kepler's Orrery");
        /*
        JMenuBar mb = kepler.createMenuBar();
        frame.setJMenuBar(mb);
        mb.setVisible(kepler.showMenuBar);
        */
        /*        
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.gridx = 2;
        content.add(new JLabel(""), gbc);
        */

        JPanel controlButtonsPanel = null;
        if (! kepler.kioskMode()) {
            gbc.gridx = 2;
            gbc.gridy = 1;
            gbc.weightx = 0.0;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.SOUTH;
            controlButtonsPanel = new JPanel();
            controlButtonsPanel.setBackground(kepler.orrery.controlPanelBG);
            controlButtonsPanel.setLayout(new GridBagLayout());
            controlButtonsPanel.setSize(controlButtonSize, controlButtonSize * kepler.controlPanels.size());
            content.add(controlButtonsPanel, gbc);
        }

        // floater thing to keep the control panels on the right. 
        gbc.gridx++;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 2;
        gbc.insets = new Insets(0, 0, 2, 0);
        content.add(new JLabel(""), gbc);
        
        GridBagConstraints cbGbc = null;
        if (!kepler.kioskMode()) {
            cbGbc = new GridBagConstraints();
            cbGbc.gridx = 0;
            cbGbc.gridy = 0;
            cbGbc.anchor = GridBagConstraints.SOUTH;
            cbGbc.weighty = 1.0;
            controlButtonsPanel.add(new JLabel(""), cbGbc);
            cbGbc.weighty = 0.0;
        }
        if (kepler.kioskMode()) {
            gbc.gridx++;
            gbc.gridy = -1;
        }

        for(Iterator it = kepler.controlPanels.iterator(); it.hasNext(); ) {
            ControlPanel controlPanel = (ControlPanel)it.next();
            if (kepler.kioskMode()) {
                gbc.gridy++;
                gbc.gridheight = 1;
                gbc.anchor = GridBagConstraints.NORTHEAST;
                gbc.insets = new Insets(10, 5, 10, 30);
                gbc.fill = GridBagConstraints.BOTH;
                content.add(controlPanel.getPanel(), gbc);
            } else {
                gbc.gridx++;
                gbc.gridy = 0;
                gbc.gridheight = 1;
                gbc.anchor = GridBagConstraints.NORTH;
                content.add(new JLabel(""), gbc);
                gbc.gridy = 1;
                gbc.gridheight = 1;
                gbc.insets = new Insets(0, 2, 10, 5);
                gbc.anchor = GridBagConstraints.NORTH;
                gbc.fill = GridBagConstraints.VERTICAL;
                //controlPanel.getPanel().setBorder(new MatteBorder(0, 0, 0, 1,
                //                                                kepler.orrery.controlPanelBorder));
                content.add(controlPanel.getPanel(), gbc);
                controlPanel.hide();
                JToggleButton controlButton = kepler.createControlButton(controlPanel);
                cbGbc.gridy++;
                cbGbc.insets = new Insets(0, 0, 2, 0);
                controlButtonsPanel.add(controlButton, cbGbc);
                JLabel sp = new JLabel("  ");
                spacer.setSize(controlButtonSize, controlButtonSize);
                cbGbc.gridy++;
                controlButtonsPanel.add(sp, cbGbc);
            }
        }
        /*
        if (kepler.kioskMode()) {
            JLabel spacer = new JLabel("  ");
            spacer.setSize(controlButtonSize, controlButtonSize);
            gbc.gridy++;
            gbc.fill = GridBagConstraints.VERTICAL;
            content.add(spacer, gbc);
        }
        */
        frame.pack();
        return frame;
    }

    static class KeplerWindowListener extends WindowAdapter {
        private Kepler kepler;
        public KeplerWindowListener(Kepler _kepler) {
            kepler = _kepler;
        }
        public void windowStateChanged(WindowEvent we) {
            System.out.println("\n\n\n KEPLER got window state change.. " + we);
            //if (we.getNewState() == WindowEvent.COMPONENT_RESIZED) {
                System.out.println(" KEPLER got window resize.");
                kepler.repack();
                // }
        }
    }

    public void repack() {
        if (frame != null && !fullscreen) {
            frame.pack();
        }
    }
    
    private static int controlButtonSize = 20;
    private JToggleButton createControlButton(ControlPanel controlPanel) {
        ImageIcon normalIcon = createImageIcon("plus20.gif", "Open Control Panel");
        ImageIcon pressedIcon = createImageIcon("minus20.gif", "Close control panel");
        JToggleButton button = new JToggleButton(normalIcon);
        button.setBorderPainted(false);
        button.setPressedIcon(pressedIcon);
        button.setSelectedIcon(pressedIcon);
        button.setSize(controlButtonSize, controlButtonSize);
        button.setBackground(orrery.bgColor);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        button.addActionListener(new ControlButtonActionListener(controlPanel, button));
        return button;
    }

    class ControlButtonActionListener implements java.awt.event.ActionListener {
        private ControlPanel controlPanel;
        private JToggleButton button;
        public ControlButtonActionListener(ControlPanel controlPanel, JToggleButton button) {
            this.controlPanel = controlPanel;
            this.button = button;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
            if (button.isSelected()) {
                controlPanel.show();
                // take care of switching button image?
            } else {
                controlPanel.hide();
            }
        }
    }

    public static void prependIconDir(String dir) {
        iconPath.add(0, iconBaseDir + "/" + dir);
    }

    public static void prependIconPath(String path) {
        iconPath.add(0, path);
    }

    
    /** Returns an ImageIcon,
        looking in order in the directories in the icon path to find
        the image.
        returns null if it can't find the image in the path.
    */
    public static ImageIcon createImageIcon(String name, String description) {
        for(Iterator it = iconPath.iterator(); it.hasNext(); ) {
            String dir = (String)it.next();
            String path = dir + "/" + name;
            java.net.URL imgURL = Kepler.class.getResource(path);
            if (imgURL != null) {
                return new ImageIcon(imgURL, description);
            }
        }
        System.err.println("Couldn't find icon file: " + name);
        return null;
    }


    public void repaint() {
        frame.repaint();
    }

    public void addControlPanel(ControlPanel p) {
        if (controlPanels == null) {
            controlPanels = new ArrayList();
        }
        controlPanels.add(p);
    }
    
    public void setPlayTimes(int playTimeLo, int playTimeHi) {
        this.playTimeLo = playTimeLo;
        this.playTimeHi = playTimeHi;
        orrery.setPlayTimes(playTimeLo, playTimeHi);
    }

    public void setTrails(int trails) {
        this.trails = trails;
        orrery.setTrails(trails);
    }

    public boolean kioskMode() {
        return this.kioskMode;
    }
    
    public PlaylistManager getPlaylistManager() {
        return this.playlistManager;
    }

    public void setDt(double dt) {
        this.dt = dt;
    }

    public void writeWorldsAsXML(boolean flipY) {
        playlistManager.reset();
        playlistManager.setLoop(false);
        while(playlistManager.hasOnDeckItem()) {
            Playlist.Item worldItem =
                playlistManager.getOnDeckItem();
            String filename = worldItem.getFull();
            String xmlFilename = filename + ".kow";
            System.out.println("writing world [" + filename + "] as xml: [" + xmlFilename + "]  flipY:" + flipY + "...");
            orrery.clearWorld();
            orrery.setWorldFile(filename);
            reader.open(filename);
            orrery.clearBG();
            orrery.readWorld();
            reader.close();
            
            orrery.writeWorldXML(xmlFilename, flipY);
            playlistManager.placeNextOnDeck();
        }
    }
    
    public void startKepling() {
        drawer.postDisplayInit();
        if (indraThread == null) {
            indraThread = new IndraThread(this, dt);
            setPaused(false);
            indraThread.start();
        }
    }

    public void stopKepling() {
        stopIndraThread();
    }
    
    public void stopIndraThread() {
        if (indraThread != null) {
            indraThread.halt();
        }
    }
    
    class IndraThread extends Thread {
        public    boolean yuga = true;
        protected Kepler  kepler;
        protected double  dt;

        public IndraThread(Kepler jk, double dt) {
            this.kepler = jk;
            this.dt = dt;
        }
        
        public synchronized void run() {
            // true the first time. 
            yuga = true;
            // next time through, only if loopPlayList is true.
            System.out.println("Playlist Loop start");
            // not needed?kepler.playlistManager.setShuffle(shufflePlaylist);
            kepler.playlistManager.reset();
            orrery = kepler.orrery;

            while(playlistManager.hasOnDeckItem() && yuga) {
                Playlist.Item worldItem =
                    kepler.playlistManager.getOnDeckItem();
                String filename = worldItem.getFull();
                if (playlistPathPrefix != null) {
                    filename = playlistPathPrefix + File.separatorChar + filename;
                }
                System.out.println("\n\n");
                System.out.println("/////////////////////////////////////////////////////////");
                System.out.println("////   Kepler's Orrery.                              ////");
                System.out.print("////     " + filename);
                if (filename.length() <= 44) {
                    for(int i = 0; i < 44 - filename.length(); i++) {
                        System.out.print(' ');
                    }
                    System.out.println("////");
                } else {
                    System.out.println();
                }
                System.out.println("/////////////////////////////////////////////////////////");
                kepler.setMode("worldtransition", true);
                kepler.setMode("worldstarting", true);
                orrery.setDt(dt);
                orrery.clearWorld();
                System.out.println("Setting orrery world file: " + filename);
                orrery.setWorldFile(filename);
                reader.open(filename);
                orrery.clearBG();
                drawer.initbg(orrery.bgColor);
                orrery.readWorld();
                orrery.copyInitialBodies(); 
                if (controlPanels != null) {
                    for(Iterator it = controlPanels.iterator(); it.hasNext(); ) {
                        ControlPanel controlPanel = (ControlPanel)it.next();
                        controlPanel.worldRead();
                    }
    
                }
                if (initialPause > 0) {
                    try { Thread.sleep(1000 * initialPause); }
                    catch (InterruptedException e) { System.out.println("Error sleeping"); }
                }
                orrery.setupDrawing();
                orrery.draw_slowly(orrery.creationDelay);
                kepler.setMode("worldstarting", false);
                kepler.setMode("worldtransition", false);

                orrery.startDrawingThread(filename);
                if (controlPanels != null) {
                    for(Iterator it = controlPanels.iterator(); it.hasNext(); ) {
                        ControlPanel controlPanel = (ControlPanel)it.next();
                        controlPanel.dawnOfCreation();
                    }
                }                    
                // wait for it... (might need to be in own thread, like polarball's listenerthread)
                try { Thread.sleep(10000); }
                catch (InterruptedException e) { System.out.println("Error sleeping"); }
                System.out.println("Kepler: waiting for drawing thread.");
                orrery.waitForDrawingThread();
                System.out.println("Kepler: done waiting for drawing thread.");
                reader.close();
                if (controlPanels != null) {
                    for(Iterator it = controlPanels.iterator(); it.hasNext(); ) {
                        ControlPanel controlPanel = (ControlPanel)it.next();
                        controlPanel.endOfTheWorld();
                    }
                }                    
                if (initialPause > 0) {
                    try { Thread.sleep(1000 * initialPause / 2); }
                    catch (InterruptedException e) { System.out.println("Error sleeping"); }
                }
                if (!reloadWorld) {
                    kepler.playlistManager.placeNextOnDeck();
                }
                reloadWorld =  false;
            }
            System.exit(0);
        }

        public void halt() {
            yuga = false;
        }
    }


    public void playPrev() {
        if (indraThread == null) {
            return;
        }
        playlistManager.placePrevOnDeck();
        reloadWorld = true;
        setMode("worldtransition", true);
        orrery.winkOut();
        orrery.possiblyResetPlayCycles();
    }
    
    public void playNext() {
        System.out.println("Kepler: playNext()");
        setMode("worldtransition", true);
        orrery.winkOut();
        orrery.possiblyResetPlayCycles();
    }

    // operates on current playlist. 
    public void playWorld(Playlist.Item world) {
        if (indraThread == null) {
            return;
        }
        playlistManager.placeItemOnDeck(world);
        reloadWorld = true;
        setMode("worldtransition", true);
        orrery.winkOut();
        orrery.possiblyResetPlayCycles();
        orrery.resetPlayTimer();
    }

    public void reloadWorld() {
        System.out.println("Kepler: reloadWorld()");
        reloadWorld = true;
        playNext();
    }

    public void setPaused(boolean paused) {
        orrery.setPaused(paused);
        setMode("pause", paused);
    }
    
    public void setMode(String mode, boolean value) {
        for(Iterator it = controlPanels.iterator(); it.hasNext(); ) {
            ControlPanel controlPanel = (ControlPanel)it.next();
            controlPanel.setMode(mode, value);
        }
    }
    
    public void winkOut() {
        orrery.winkOut();
    }

    // create the menu bar
    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem menuItem1 = new JMenuItem("Save...   ");
        menuItem1.addActionListener(this);
        menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItem1);

        JMenuItem menuItemP = new JMenuItem("Pause...   ");
        menuItemP.addActionListener(this);
        menuItemP.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItemP);

        JMenuItem menuItemR = new JMenuItem("Resume...   ");
        menuItemR.addActionListener(this);
        menuItemR.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItemR);

        JMenuItem menuItemN = new JMenuItem("Next...   ");
        menuItemN.addActionListener(this);
        menuItemN.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItemN);

        JMenuItem menuItemF = new JMenuItem("FullScreen...   ");
        menuItemF.addActionListener(this);
        menuItemF.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItemF);

        JMenuItem menuItemQ = new JMenuItem("Quit...   ");
        menuItemQ.addActionListener(this);
        menuItemQ.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItemQ);
        return menuBar;
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        System.out.println("KEPLER  action command: " + cmd);
        if (cmd.equals("Quit...   ")) {
            System.exit(0);
        } else if (cmd.equals("FullScreen...   ")) {
            toggleFullScreen();
        } else if (cmd.equals("Pause...   ")) {
            System.out.println("We should pause...?");
            orrery.pause();
        } else if (cmd.equals("Resume...   ")) {
            System.out.println("We should resume...?");
            orrery.resume();
        } else if (cmd.equals("Next...   ")) {
            playNext();
        } else if (cmd.equals("Quit...   ")) {
            System.out.println("bye bye plane!");
        } else if (cmd.equals("Save...   ")) {
            saveDrawing();
        }
    }

    public void toggleFullScreen() {
        System.out.println("Toggling fullscreen: [" + fullscreen + "]");
        setFullScreen(!fullscreen);
    }
    
    public void setFullScreen(boolean val) {
        /* this bit hasn't been working...
           boolean decorated = !fullscreen;
           recreateFrame(drawer, decorated);
        */
        fullscreen = val;
        drawer.fullscreen(frame, fullscreen);
    }

    public void toggleTrails() {
        if (trails == 0) {
            if (saved_trails == 0) {
                trails = 25;
            } else {
                trails = saved_trails;
            }
        } else {
            saved_trails = trails;
            trails = 0;
        }
        setTrails(trails);
    }

    public void setTrails(boolean val) {
        if (val) {
            if (trails == 0) {
                if (saved_trails == 0) {
                    trails = 25;
                } else {
                    trails = saved_trails;
                }
            }
        } else {
            saved_trails = trails;
            trails = 0;
        }
        setTrails(trails);
    }
    public void saveDrawing() {
        FileDialog chooser = new FileDialog(this.frame, "Use a .png or .jpg extension", FileDialog.SAVE);
        chooser.setVisible(true);
        String filename = chooser.getFile();
        if (filename != null) {
            // keep File.separatorChar here, because this action happens on the client
            drawer.save(chooser.getDirectory() + File.separator + chooser.getFile());
        }
    }

    public void  keyTyped(KeyEvent e) {
        //lastKeyTyped = e.getKeyChar();
        //  (migrate this stuff from StdDraw)
    }
    public void keyPressed   (KeyEvent e)   { }
    public void keyReleased  (KeyEvent e)   {
        System.out.println("KEPLER got KeyEvent: [" + e.getKeyChar() + "]");
        System.out.println("KEPLER got KeyEvent: " + e);
        char ch = e.getKeyChar();
        if (ch == 'Q') {
            orrery.setMuted(true);
            orrery.stopDrawingThread();
            stopKepling();
            System.exit(0);
        } else if (ch == 'X') {
            orrery.midiAllOff();
            orrery.stopDrawingThread();
            stopKepling();
        } else if (ch == 'F') {
            toggleFullScreen();
        } else if (ch == 'T') {
            toggleTrails();
        } else if (ch == 'P') {
            orrery.togglePause();
        } else if (ch == 'M') {
            orrery.toggleMuted();
        } else if (ch == 'R') {
            reloadWorld();
        } else if (ch == 'N') {
            playNext();
        } else if (ch == 'S') {
            orrery.midiAllOff();
            orrery.stopDrawingThread();
        } else if (ch == 'I') {
            orrery.toggleInfoDisplay();
        } else if (ch == 'V') {
            orrery.toggleForceField();
        } else if (ch == 'v') {
            orrery.toggleBodyForces();
        } else if (ch == 'H' || ch == 'h' || ch == '?') {
            orrery.showHelpInfo(getRuntimeKeysHelp());
        } else if (ch == ' ') {
            orrery.hideHelpInfo();
        } else if (ch == 'D') {
            int dbl = orrery.getDebugLevel();
            dbl ++;
            if (dbl > 1) {
                dbl = 0;
            }
            orrery.setDebugLevel(dbl);
        } 
    }

    List runtimeKeysHelp = null;
    public List getRuntimeKeysHelp() {
        if (runtimeKeysHelp == null) {
            List help = new ArrayList();
            help.add("Command Keys:");
            help.add("");
            help.add("  F -- Toggle full screen");
            help.add("  T -- Toggle trails");
            help.add("  P -- Toggle Pause");
            help.add("  D -- Toggle debug info");
            help.add("  V -- Toggle force field.");
            help.add("  v -- Toggle body force vectors.");
            help.add("  I -- Toggle info display.");
            help.add("  R -- Reload world");
            help.add("  N -- Next world");
            help.add("  H, ? -- Show this list");
            help.add("  Q -- Quit");
            help.add("");
            help.add("  Space bar to hide list");
            runtimeKeysHelp = help;
        }
        return runtimeKeysHelp;
    }
    
    public static void readPlaylistFile(String playlistFile, Playlist playlist)  throws IOException {
        System.out.println("Kepler:readPlayListFile(" + playlistFile + ")");
        TokenReader reader = new TokenReader(playlistFile);
        boolean done = false;
        String prefix = null;
        while (!done) {
            String token = reader.readToken();
            if (token == null) {
                done = true;
            } else if (token.equalsIgnoreCase("prefix")) {
                prefix = reader.readToken();
                playlist.setPrefix(prefix);
            } else if (token.equalsIgnoreCase("title")) {
                String title = reader.readLine().trim();
                playlist.setTitle(title);
            } else {
                playlist.add(token);
            }
        }
    }

    public static void usage() {
        StringBuffer buf = new StringBuffer();
        usage(buf);
        System.out.println(buf.toString());
    }
    
    public static void usage(StringBuffer buf) {
        buf.append("java kepler.Kepler [options]*  [WorldFiles]*\n");
        buf.append("  options:\n");
        buf.append("    --time <seconds>      Playing time for each world\n");
        buf.append("    --timerange <low> <high>  Playing time for each world randomly varies \n");
        buf.append("                          between <low> and <high>\n");
        buf.append("    --pause <seconds>     Initial pause for each world: after drawing\n");
        buf.append("                          and before starting the simulation\n");
        buf.append("    --cycletime <ms>      Minimum time for each increment cycle. \n");
        buf.append("                          If cycles take less time than specified, the \n");
        buf.append("                          simulation thread will sleep for the difference.\n");
        buf.append("    --dt <mtu>            World-coordinate time for each cycle.\n");
        buf.append("                          (<mtu> is 'mythical time units')\n");
        buf.append("    --debug               turn on debugging information (mostly prints \n");
        buf.append("                          notes as they're played)\n");
        buf.append("    --trails <cycles>     Set fading trails to fade over a specified \n");
        buf.append("                          number of cycles.\n");
        buf.append("    --forcefield          Show force vectors.\n");
        buf.append("    --forcefieldgrid <n>  number of vectors.\n");
        buf.append("    --canvas <pixels>     Canvas size, in pixels\n");
        buf.append("    --fullscreen          Start in fullscreen mode, with no frame decorations.\n");
        buf.append("    --undecorated         No frame decorations.\n");
        buf.append("    --playlist <file>     Specify a playlist. \n");
        buf.append("                          The playlist comprises the world files specified.\n");
        buf.append("                          in --playlist arguments concatenated with world.\n");
        buf.append("                          files specified on the command line, in order.\n");
        buf.append("    --playlistPathPrefix <path> path from which to start looking for the \n");
        buf.append("                          files in playlists. If specified,\n");
        buf.append("                          prepends <path>/ to the playlist prefixes.");
        buf.append("    --loop                Loop the playlist. \n");
        buf.append("    --shuffle             Randomize the order of the playlist.\n");
        buf.append("    --info <sec>          Set number of seconds to display info at beginning of each world.\n");
        buf.append("                            0: don't show info; -1: show all the time.\n");
        buf.append("    --kiosk               Bring controllers up in kiosk mode.\n");
        buf.append("    --writexml            Write the worlds out in xml mode, as /kow files.\n");
        buf.append("    --flipY <T/F>         Flip the Y-coordinate when writing world out as xml. (Default T)\n");
        buf.append("                          The worlds will be written right next to their originals.\n");
        buf.append("                          And then the system will exit.");
        buf.append("\n");
        buf.append(" Keyboard commands while simulation is running:\n");
        buf.append("    F  -- Toggle fullscreen mode. If system starts up in fullscreen mode, \n");
        buf.append("          the frame will be undecorated. \n");
        buf.append("    P  -- Toggle Pause mode.\n");
        buf.append("    N  -- Next world. The stars will wink out, the sounds will turn off, and\n");
        buf.append("          the next world in the playlist will start.\n");
        buf.append("    S  -- Stop the world. \n");
        buf.append("          The simulation will stop. But unfortunately, at the moment, \n");
        buf.append("          the sounds will continue annoyingly. \n");
        buf.append("    Q  -- Quit.\n");
        buf.append("    D  -- Toggle debug mode.\n");
        buf.append("    T  -- Toggle trails. Will turn trails on or off. \n");
    }   

    public static void setLookAndFeel() {
        try {
            // Set cross-platform Java L&F (also called "Metal")
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } 
        catch (UnsupportedLookAndFeelException e) {
            // handle exception
        }
        catch (ClassNotFoundException e) {
            // handle exception
        }
        catch (InstantiationException e) {
            // handle exception
        }
        catch (IllegalAccessException e) {
            // handle exception
        }
    }

    public static void setTheme(Orrery orrery) {
        ThemeKepler theme = new ThemeKepler(orrery);
        MetalLookAndFeel.setCurrentTheme(theme);
    }
    
    public static void main(String[] args) {
        //setLookAndFeel();
        PlaylistManager playlistManager = new PlaylistManager();
        Playlist defaultPlaylist = new Playlist();
        playlistManager.addPlaylist(defaultPlaylist);

        int i = 0;
        double dt = 75.;
        int cycleTime = 0;
        int playTimeLo = 0;
        int playTimeHi = 0;
        int debugLevel = 0;
        int initialPause = 0;
        boolean fullscreen = false;
        boolean undecorated = false;
        int canvasSize = 0;
        int trails = 0; 
        boolean showForceField = false;
        int forceFieldGridVectors = 0;
        String targetDirectory = null;
        float initialInfoSeconds = 0.5f;
        boolean kioskMode = true;
        boolean writeXMLAndExit = false;
        boolean flipY = true;
        String playlistPathPrefix = null;

        if (args.length == 0) {
            usage();
            System.exit(0);
        }
        if (args.length > 0) {
            while (i < args.length) {
                try {
                    if (args[i].equalsIgnoreCase("--help") || args[i].equalsIgnoreCase("-h")) {
                        usage();
                        System.exit(0);
                    } else if (args[i].equalsIgnoreCase("--time") || args[i].equalsIgnoreCase("--playtime")) {
                        i++;
                        playTimeLo = Integer.parseInt(args[i]);
                        playTimeHi = playTimeLo;
                        i++;
                    } else if (args[i].equalsIgnoreCase("--timerange")) {
                        i++;
                        playTimeLo = Integer.parseInt(args[i]);
                        i++;
                        playTimeHi = Integer.parseInt(args[i]);
                        i++;

                    } else if (args[i].equalsIgnoreCase("--cycletime")) {
                        i++;
                        cycleTime = Integer.parseInt(args[i]);
                        i++;

                    } else if (args[i].equalsIgnoreCase("--dt")) {
                        i++;
                        dt = Double.parseDouble(args[i]);
                        i++;

                    } else if (args[i].equalsIgnoreCase("--kiosk")) {
                        i++;
                        kioskMode = true;
                    } else if (args[i].equalsIgnoreCase("--nokiosk")) {
                        i++;
                        kioskMode = false;
                    } else if (args[i].equalsIgnoreCase("--trails")) {
                        i++;
                        trails = Integer.parseInt(args[i]);
                        i++;
                    } else if (args[i].equalsIgnoreCase("--forcefield")) {
                        i++;
                        showForceField = true;
                    } else if (args[i].equalsIgnoreCase("--forcefieldgrid")) {
                        i++;
                        forceFieldGridVectors = Integer.parseInt(args[i]);
                        i++;
                    } else if (args[i].equalsIgnoreCase("--debug")) {
                        i++;
                        debugLevel = 1;


                    } else if (args[i].equalsIgnoreCase("--pause")) {
                        i++;
                        initialPause = Integer.parseInt(args[i]);
                        i++;

                    } else if (args[i].equalsIgnoreCase("--canvas")) {
                        i++;
                        canvasSize = Integer.parseInt(args[i]);
                        i++;

                    } else if (args[i].equalsIgnoreCase("--full") || args[i].equalsIgnoreCase("--fullscreen")) {
                        i++;
                        fullscreen = true;
                    } else if (args[i].equalsIgnoreCase("--undecorated")) {
                        i++;
                        undecorated = true;
                        

                    } else if (args[i].equalsIgnoreCase("--loop")) {
                        i++;
                        playlistManager.setLoop(true);

                    } else if (args[i].equalsIgnoreCase("--shuffle")) {
                        i++;
                        // these newfangled kinds of shuffling don't quite
                        // exist yet. 
                        //playlistManager.setShuffleWithinPlaylists(true);
                        playlistManager.setShuffle(true);

                    } else if (args[i].equalsIgnoreCase("--shuffle_across")) {
                        i++;
                        //playlistManager.setShuffleAcrossPlaylists(true);

                    } else if (args[i].equalsIgnoreCase("--noloop")) {
                        i++;
                        playlistManager.setLoop(false);

                    } else if (args[i].equalsIgnoreCase("--playlist")) {
                        i++;
                        String playlistFile = args[i];
                        //
                        // If there was no playlist in the args, and the current playlist
                        // has 
                        i++;
                        try {
                            Playlist playlist = new Playlist();
                            playlistManager.addPlaylist(playlist);
                            readPlaylistFile(playlistFile, playlist);
                        } catch (Exception ex) {
                            System.out.println("Kepler caught exception reading playlist file: " + ex);
                        }

                    } else if (args[i].equalsIgnoreCase("--playlistPathPrefix")) {
                        i++;
                        playlistPathPrefix = args[i];
                        if (playlistPathPrefix.endsWith("/") ||
                            playlistPathPrefix.endsWith("\\")) {
                            playlistPathPrefix =
                                playlistPathPrefix.substring(0, playlistPathPrefix.length() - 1);
                        }
                        i++;
                    } else if (args[i].equalsIgnoreCase("--writexml")) {
                        writeXMLAndExit = true;
                        i++;

                    } else if (args[i].equalsIgnoreCase("--flipy")) {
                        i++;
                        flipY = (args[i].equalsIgnoreCase("t"));
                        i++;

                    } else if (args[i].equalsIgnoreCase("--info")) {
                        i++;
                        initialInfoSeconds = Float.parseFloat(args[i]);
                        i++;

                    } else if (args[i].equalsIgnoreCase("--targetdir")) {
                        i++;
                        targetDirectory = args[i];
                        i++;

                    } else {
                        String name = Playlist.extractName(args[i]);
                        String full = args[i];
                        defaultPlaylist.add(name, full);
                        i++;
                    }
                } catch (NumberFormatException ex) {
                    // skip this arg
                    i++;
                }
            }
        }

        playlistManager.compactPlaylists();
        Kepler jk = new Kepler(canvasSize, playlistManager, kioskMode);
        jk.initialPause = initialPause;
        jk.fullscreen = fullscreen;
        jk.decorated = !undecorated && !fullscreen;
        jk.playlistPathPrefix = playlistPathPrefix;
        if (targetDirectory != null) {
            jk.targetDirectory = targetDirectory;
        }
        jk.initOrrery();
        if (writeXMLAndExit) {
            jk.writeWorldsAsXML(flipY);
            System.exit(0);
        }
        setTheme(jk.orrery);
        jk.orrery.setInitialDisplayInfoSeconds(initialInfoSeconds);
        jk.frame = recreateFrame(jk.frame, jk, jk.drawer, jk.decorated);
        jk.frame.getContentPane().addKeyListener(jk);    // JLabel cannot get keyboard focus
        jk.drawer.getDrawingPane().addKeyListener(jk);

        jk.frame.setBackground(jk.orrery.bgColor);


        if (fullscreen) {
            jk.drawer.fullscreen(jk.frame, true);
        }
        jk.frame.pack();

        jk.frame.setVisible(true);

        jk.orrery.setDebugLevel(debugLevel);
        if (cycleTime > 0 ) {
            jk.orrery.setCycleTime(cycleTime);
        }
        jk.setPlayTimes(playTimeLo, playTimeHi);
        jk.setTrails(trails);
        if (forceFieldGridVectors > 0) { 
            jk.orrery.setForceFieldGridVectors(forceFieldGridVectors);
        }
        jk.orrery.setShowForceField(showForceField);
        jk.setDt(dt);
        jk.startKepling();
    }
}
