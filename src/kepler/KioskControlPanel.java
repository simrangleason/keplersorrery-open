/***************************************************
 * Copyright 2007 by Simran Gleason                *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class KioskControlPanel extends ControlPanel {
    private JPanel physicsPanel = null;
    private JPanel musicPanel = null;
    private JToggleButton physicsMusicToggle = null;
    private Color  physicsBG;
    private Color  musicBG;
    private WorldBuilder worldBuilder;
    private MissionControlPanel missionControlPanel;
    private BottomControlsPanel bottomControls;
    private JLabel worldStateLabel;
    private JButton prevWorldButton;
    private JButton nextWorldButton;
    
    public KioskControlPanel(Kepler kepler, WorldBuilder wb) {
        super(kepler);
        worldBuilder = wb;
        setPanel(makePanel(kepler, wb));
    }

    public JPanel makePanel(Kepler kepler, WorldBuilder wb) {
        JPanel panel = new JPanel();
        panel.setBackground(orrery.controlPanelBG);
        panel.setLayout(new GridBagLayout());
        panel.setMinimumSize(new Dimension(256, 300));
        //panel.setBorder(new MatteBorder(1, 1, 1, 1, java.awt.Color.GREEN));


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(new KioskTitleControlPanel(kepler).getPanel());

        physicsPanel = makePhysicsPanel(kepler);
        musicPanel = makeMusicPanel(wb);
        musicPanel.setSize(new Dimension(252, 300));
        musicPanel.setMaximumSize(new Dimension(252, 300));
        
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        physicsMusicToggle = makePhysicsMusicToggle(kepler, physicsPanel, musicPanel);
        gbc.insets = new Insets(0, 0, -1, 0);
        gbc.weightx = 0.;
        panel.add(physicsMusicToggle, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        //gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(-3, 0, 0, 0);
        panel.add(physicsPanel, gbc);
        physicsPanel.setVisible(true);
        gbc.insets = new Insets(-3, 0, 0, 0);
        panel.add(musicPanel, gbc);
        musicPanel.setVisible(true);

        gbc.gridy++;
        gbc.insets = new Insets(9, 0, 0, 0);
        bottomControls = new BottomControlsPanel(kepler);
        panel.add(bottomControls.getPanel(), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        //gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);
        worldStateLabel = makeTextLabel("");
        worldStateLabel.setForeground(orrery.countdownColor);
        worldStateLabel.setVisible(false);
        panel.add(worldStateLabel, gbc);

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(makeTextLabel(""), gbc);

        gbc.gridy++;
        gbc.weighty = 0.;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(makePlaylistPanel(), gbc);

        return panel;
    }

    public JPanel makePhysicsPanel_oops(Kepler kepler) {
        missionControlPanel = new MissionControlPanel(kepler, orrery.physicsBG);
        return missionControlPanel.getPanel();
    }
    
    public JPanel makePhysicsPanel(Kepler kepler) {
        JPanel panel = new JPanel();
        panel.setBackground(orrery.controlPanelBG);
        panel.setLayout(new GridBagLayout());
        panel.setSize(new Dimension(256, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        missionControlPanel = new MissionControlPanel(kepler, orrery.physicsBG);
        panel.add(missionControlPanel.getPanel(), gbc);

        return panel;
    }

    JPanel  makeMusicPanel_maybe(WorldBuilder wb) {
        return wb.getMusicPanel_kiosk(kepler);
    }
    
    JPanel  makeMusicPanel(WorldBuilder wb) {
        JPanel panel = new JPanel();
        panel.setBackground(orrery.controlPanelBG);
        panel.setLayout(new GridBagLayout());
        panel.setSize(new Dimension(256, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        //gbc.weightx = 1.0;
        panel.add(wb.getMusicPanel_kiosk(kepler), gbc);

        return panel;
    }

    public JToggleButton makePhysicsMusicToggle(final Kepler kepler,
                                                final JPanel physicsPanel,
                                                final JPanel musicPanel) {
        final JToggleButton physicsMusicTog =
            makeImageToggleButton("PhysicsMusicToggle_music.gif",
                                  "PhysicsMusicToggle_physics.gif",
                                  "Change modes");
        physicsMusicTog.setSelected(true);
        physicsMusicTog.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("PHYSICS|MUSIC toggle pressed.");
                    boolean physicsMode = physicsMusicTog.isSelected();
                    // TODO: this is for restarting the world when in initial conditions mode.
                    worldBuilder.setShowingPhysicsMode(physicsMode);
                    if (physicsMode) {
                        //orrery.playingMode();
                        physicsPanel.setVisible(true);
                        musicPanel.setVisible(false);
                        //System.out.println("Entering physicsing mode [B4 copyinitialbodies]. maxMass: " + orrery.maxMass + " Body.maxMass: " + Body.maxMass + " minMass: " + orrery.minMass + " Body.minMass: " + Body.minMass);
                        //orrery.copyInitialBodies();
                        //System.out.println("Entering physicsing mode [after copyinitialbodies]. maxMass: " + orrery.maxMass + " Body.maxMass: " + Body.maxMass + " minMass: " + orrery.minMass + " Body.minMass: " + Body.minMass);
                        //orrery.resume();
                    } else {
                        System.out.println("MUSIC toggle pressed.");
                        physicsPanel.setVisible(false);
                        musicPanel.setVisible(true);
                        if (worldBuilder.getSelected() == null) {
                            worldBuilder.selectNextBodyOrRock();
                        } else {
                            worldBuilder.updateSelected();
                        }
                    }
                }
            });

        return physicsMusicTog;
    }
    
    JComboBox playlistDrop = null;
    JComboBox playlistSelectorDrop = null;
    boolean   playlistDropActive = false;
    boolean   playlistSelectorDropActive = false;
    public JPanel makePlaylistPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(orrery.controlPanelBG);
        panel.setLayout(new GridBagLayout());
        //panel.setBorder(new MatteBorder(0, 0, 1, 0, orrery.controlPanelBorder));
        GridBagConstraints gbc = new GridBagConstraints();
        prevWorldButton = makeImageButton("PrevWorld.gif",
                                          "PrevWorld_pressed.gif",
                                          "PrevWorld_disabled.gif",
                                          "Switch to previous world.");
        prevWorldButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    orrery.resetPlayTimer();
                    kepler.playPrev();
                }
            });     
        nextWorldButton = makeImageButton("NextWorld.gif",
                                          "NextWorld_pressed.gif",
                                          "NextWorld_disabled.gif",
                                          "Switch to next world.");
        nextWorldButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    orrery.resetPlayTimer();
                    kepler.playNext();
                }
            });
        PlaylistManager playlistManager = kepler.getPlaylistManager();
        if (playlistManager.numPlaylists() > 1) {
            System.out.println("make playlist panel: making playlist selector dorp down");
            playlistSelectorDrop = makeDropDown();
            ArrayList playlists = playlistManager.getPlaylists();
            for(Iterator it=playlists.iterator(); it.hasNext(); ) {
                Playlist pl = (Playlist)it.next();
                String title = pl.getTitle();
                if (title == null) {
                    title = "";
                }
                playlistSelectorDrop.addItem(title);
                playlistSelectorDrop.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (playlistSelectorDropActive) {
                                JComboBox cb = (JComboBox)evt.getSource();
                                String playlistTitle = (String)cb.getSelectedItem();
                                PlaylistManager playlistManager = kepler.getPlaylistManager();
                                Playlist pl = playlistManager.getPlaylistByTitle(playlistTitle);
                                System.out.println("*** playlistSelector. getPlByTitle(" + playlistTitle + ") => " + pl);
                                if (pl != null) {
                                    // do something...
                                    populatePlaylistDrop(pl);
                                    playlistManager.placePlaylistOnDeck(pl);
                                    Playlist.Item item = playlistManager.getOnDeckItem();

                                    System.out.println("calling kepler.playItem(" + item + ")");
                                    kepler.playWorld(item);
                                }
                            }
                        }
                    });
            }
            
        }
        playlistDrop = makeDropDown();
        playlistDrop.addItem("");
        playlistDrop.setSelectedItem("");
        playlistDrop.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (playlistDropActive) {
                        JComboBox cb = (JComboBox)evt.getSource();
                        Playlist.Item item = (Playlist.Item)cb.getSelectedItem();
                        System.out.println("calling kepler.playItem(" + item + ")");
                        kepler.playWorld(item);
                    }
                }
            });
        gbc.gridy = 0;
        System.out.println("playlistselecgtor drop: " + playlistSelectorDrop);
        if (playlistSelectorDrop != null) {
            gbc.gridx = 0;
            panel.add(makeTextLabel("Playlist"), gbc);
            gbc.gridx++;
            gbc.gridwidth=1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(playlistSelectorDrop, gbc);
            gbc.gridy++;
        }
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(prevWorldButton, gbc);
        gbc.gridx++;
        panel.add(playlistDrop, gbc);
        gbc.gridx++;
        panel.add(nextWorldButton, gbc);
        return panel;
    }

    public void worldRead() {
        System.out.println("KIOSK CONTROL world read.");
        playlistDropActive = false;
        playlistSelectorDropActive = false;
        playlistDrop.removeAllItems();
        PlaylistManager playlistManager = kepler.getPlaylistManager();
        Playlist playlist = playlistManager.getOnDeckPlaylist();
        System.out.println("  playlist size: " + playlist.size());
        if (playlistSelectorDrop != null) {
            playlistSelectorDrop.setSelectedItem(playlist.getTitle());
        }
        populatePlaylistDrop(playlist);
        System.out.println("orrery.world file: " + orrery.getWorldFile());
        String fullWorldPath = orrery.getWorldFile();
        // TODO: won't work -- might have changed playlists...
        //       but the current one should be remembered in the playlistManager, right?
        // still might not work...
        Playlist.Item currentItem = playlistManager.getCurrentItem();
        playlistDrop.setSelectedItem(currentItem);
        playlistDropActive = true;
        playlistSelectorDropActive = true;
        missionControlPanel.worldRead();
        worldBuilder.worldRead();
        bottomControls.worldRead();
    }

    private void populatePlaylistDrop(Playlist playlist) {
        System.out.println("PopulatePlaylistDrop:");
        for(Iterator it=playlist.getUnshuffledItems().iterator(); it.hasNext(); ) {
            Playlist.Item worldItem = (Playlist.Item)it.next();
            System.out.println("   world: " + worldItem);
            playlistDrop.addItem(worldItem);
        }
    }
    
    // TODO: generalize this to make it a mode change
    //       so we can handle several modes, like initial conditions, paused, etc. 
    public void setMode(String mode, boolean val) {
        missionControlPanel.setMode(mode, val);
        worldBuilder.setMode(mode, val);
        bottomControls.setMode(mode, val);
        if (mode.equals("pause")) {
            // nothing yet
        } else if (mode.equals("initialconditions")) {
            // nothing yet
        } else if (mode.equals("worldtransition")) {
            nextWorldButton.setEnabled(!val);
            prevWorldButton.setEnabled(!val);
            playlistDrop.setEnabled(!val);
            worldStateLabel.setVisible(val);
        } else if (mode.equals("worldending")) {
            if (val) {
                worldStateLabel.setText("World Ending!");
            } else {
                worldStateLabel.setText("");
            }
        } else if (mode.equals("worldstarting")) {
            if (val) {
                worldStateLabel.setText("World Starting!");
            } else {
                worldStateLabel.setText("");
            }
        }
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

}

