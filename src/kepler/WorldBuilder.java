/***************************************************
 * Copyright 2007 by Simran Gleason                *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.util.Iterator;

public class WorldBuilder extends ControlPanel implements NoteListener {

    private static final int NONE = -1;
    private static final int BODY =  0;
    private static final int ROCK =  1;

    public String targetDirectory = "../../worlds/saved";
    
    ///////////////////////////////////////////////////////////////////
    /// Class variables that hold the ui widgets for the data that  ///
    /// will get edited for the bodies/rocks/melodies, etc.         ///
    ///////////////////////////////////////////////////////////////////

    private JPanel     wbPanel;
    private JLabel     massText_kiosk;
    private JTextField massText;
    private JSlider    massSlider;
    /*
    private JTextField posXText;
    private JTextField posYText;
    private JTextField posRText;
    private JTextField posThetaText;
    */
    private JTextField offsetXText;
    private JTextField offsetYText;

    private JTextField posCoord1Text;
    private JTextField posCoord2Text;
    private JLabel     posCoord1Label;
    private JLabel     posCoord2Label;
    private JLabel     posCoord1TextLabel;  // for Kiosk presentation of pos?
    private JLabel     posCoord2TextLabel;


    private JTextField vCoord1Text;
    private JTextField vCoord2Text;
    private JLabel     vCoord1Label;
    private JLabel     vCoord2Label;
    
    /*
    private JTextField vXText;
    private JTextField vYText;
    private JTextField vRText;
    private JTextField vThetaText;
    */

    private JToggleButton posCoordTypeToggle;
    private JToggleButton vCoordTypeToggle;

    /*
    private JRadioButton cartesianRadio_pos;
    private JRadioButton polarRadio_pos;
    private JRadioButton cartesianRadio_v = new JRadioButton();
    private JRadioButton polarRadio_v = new JRadioButton();
    */

    private JComboBox  playableTypeChooser; 
    private JComboBox  playableChooser;
    private boolean    playableChooserUpdating = false;
    private JTextField noteChooser;  
    private JComboBox  instrumentChooser;
    private JTextArea  playablePreviewText;

    private JComboBox  mutatorTypeChooser;
    private JTextField mutatorChanceText;
    private JTextField mutatorAfterText;
    private JTextField mutatorLowText;
    private JTextField mutatorHighText;

    private JButton    currentPreviewButton;
    private JButton    currentStopButton;
    private JButton    editedPreviewButton;
    private JButton    editedStopButton;

    private JFileChooser fileChooser;
    private File         fileChooserCurrentDirectory = null;

    /// melody/sequence editor
    private JComboBox melodySelector;
    
    /** the currently selected body. */
    private Body selected = null;
    private int selectedIndex = -1;
    private int selectedType = NONE;
    private Color backgroundColor = Color.BLACK;

    private boolean showingPhysicsMode = true;
    
    public WorldBuilder(Kepler kepler, Color backgroundColor) {
        super(kepler);
        this.backgroundColor = backgroundColor;
        wbPanel = makePanel(kepler);
        setPanel(wbPanel);
        if (!kepler.kioskMode()) {
            fileChooser = new JFileChooser();
        }
    }

    public boolean showingPhysicsMode() {
        return showingPhysicsMode;
    }

    public void setShowingPhysicsMode(boolean val) {
        showingPhysicsMode = val;
    }

    public Body getSelected() {
        return this.selected;
    }

    public void selectBody(Body body, int selectedIndex) {
        if (selected != null && selected != body) {
            selected.unselect();
            orrery.selectBody(null);
        }
        boolean updateInstruments = (selected != body);
        selected = body;
        orrery.selectBody(selected);
        selected.select(-1);
        this.selectedIndex = selectedIndex;
        this.selectedType = BODY;
        updateSelected(body, updateInstruments);
    }

    public void selectRock(Rock rock, int selectedIndex) {
        if (selected != null) {
            selected.unselect();
            orrery.selectBody(null);
        }
        boolean updateInstruments = (selected != rock);
        selected = rock;
        orrery.selectBody(selected);
        selected.select(-1);
        this.selectedType = ROCK;
        this.selectedIndex = selectedIndex;
        updateSelected(rock, updateInstruments);
    }

    public void selectPrevBodyOrRock() {
        if (orrery.isInitialConditionsMode()) {
            int numBodies = orrery.getInitialBodyCount();
            int numRocks = orrery.getInitialRockCount();
            selectPrevBodyOrRock(orrery.getInitialBodies(), orrery.getInitialRocks(),
                                 numBodies, numRocks);
            orrery.worldBuilderDraw();

        } else {
            int numBodies = orrery.getBodyCount();
            int numRocks = orrery.getRockCount();
            selectPrevBodyOrRock(orrery.getBodies(), orrery.getRocks(),
                                 numBodies, numRocks);
            orrery.redraw();
        }            
    }

    public void selectPrevBodyOrRock(Body[] bodies, Rock[] rocks,
                                     int numBodies, int numRocks) {
        boolean selectedIsRock = (selected != null && selected instanceof Rock);
        selectedIndex--;
        //
        // if we get to the end of the rocks, select the next body, and vice versa.
        //
        if (selectedIsRock && selectedIndex < 0) {
            if (numBodies > 0) {
                selectedIsRock = false;
                selectedIndex = numBodies - 1;
            } else {
                selectedIndex = numRocks - 1;
            }
                
        } else if (!selectedIsRock && selectedIndex < 0) {
            if (numRocks > 0) {
                selectedIsRock = true;
                selectedIndex = numRocks - 1;
            } else {
                selectedIndex = numBodies - 1;
            }
        }
        // TODO: need to redraw. 
        if (selectedIsRock) {
            selectRock(rocks[selectedIndex], selectedIndex);
        } else {
            selectBody(bodies[selectedIndex], selectedIndex);
        }
    }


    public void selectNextBodyOrRock() {
        if (orrery.isInitialConditionsMode()) {
            int numBodies = orrery.getInitialBodyCount();
            int numRocks = orrery.getInitialRockCount();
            selectNextBodyOrRock(orrery.getInitialBodies(), orrery.getInitialRocks(),
                                 numBodies, numRocks);
            orrery.worldBuilderDraw();

        } else {
            int numBodies = orrery.getBodyCount();
            int numRocks = orrery.getRockCount();
            selectNextBodyOrRock(orrery.getBodies(), orrery.getRocks(),
                                 numBodies, numRocks);
            orrery.redraw();
        }            
    }

    public void selectNextBodyOrRock(Body[] bodies, Rock[] rocks,
                                     int numBodies, int numRocks) {
        boolean selectedIsRock = (selected != null && selected instanceof Rock);
        selectedIndex++;
        //
        // if we get to the end of the rocks, select the next body, and vice versa.
        //
        if (selectedIsRock && selectedIndex >= numRocks) {
            if (numBodies > 0) {
                selectedIsRock = false;
            }
            selectedIndex = 0;
        } else if (!selectedIsRock && selectedIndex >= numBodies) {
            if (numRocks > 0) {
                selectedIsRock = true;
            }
            selectedIndex = 0;
        }
        if (selectedIsRock) {
            selectRock(rocks[selectedIndex], selectedIndex);
        } else {
            selectBody(bodies[selectedIndex], selectedIndex);
        }
    }

    public void unselect() {
        if (selected != null) {
            selected.unselect();
        }
        selected = null;
        orrery.selectBody(null);
        this.selectedIndex = -1;
        this.selectedType = NONE;
        updateSelected(null, true);
    }
    
    public JPanel makePanel(Kepler kepler) {
        if (kepler.kioskMode()) {
            return makePanel_kiosk(kepler);
        } else {
            return makePanel_std(kepler);
        }
    }

    public JPanel makePanel_kiosk(Kepler kepler) {
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.setBorder(new MatteBorder(0, 2, 2, 2, orrery.controlPanelBorder));
        panel.setLayout(new GridBagLayout());
        //panel.setMinimumSize(new Dimension(252, 300));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(makeImageLabel("vspacer400.gif", ""), gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        JLabel hspacer = makeImageLabel("spacer256.gif", "");
        hspacer.setBorder(new MatteBorder(1, 1, 1, 1, Color.RED));
        hspacer.setBackground(backgroundColor);
        panel.add(hspacer, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        //gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(makeBodyEditorPanel_kiosk(), gbc);

        /* moving bottom controls to external method
        // select next/prev body buttons
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(makeBottomControls_kiosk(backgroundColor), gbc);
        */

        return panel;
    }
    
    public JPanel makePanel_std(Kepler kepler) {
        JPanel panel = new JPanel();
        panel.setBackground(orrery.controlPanelBG);
        panel.setLayout(new GridBagLayout());
        panel.setSize(new Dimension(281, 293));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 0, 0);

        panel.add(makeImageLabel("KeplersWorldBuilder.gif", ""), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(makeModeTogglePanel(this, orrery), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets = new Insets(4, 0, 0, 0);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(makeSqueegee("Melodies", makePlayableEditorPanel(), false), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets = new Insets(4, 0, 0, 0);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(makeSqueegee("Instruments", makeInstrumentsEditorPanel(), false), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(makeSqueegee("SelectedBody", makeBodyEditorPanel_std(), true), gbc);

        // save/new/play command
        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(makeBottomControls_std(backgroundColor), gbc);

        return panel;
    }

    public JPanel getMusicPanel_kiosk(Kepler kepler) {
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.setBorder(new MatteBorder(0, 2, 2, 2, orrery.controlPanelBorder));
        panel.setLayout(new GridBagLayout());
        //panel.setMinimumSize(new Dimension(252, 300));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        JLabel vspacer = makeImageLabel("vspacer400.gif", "");
        //vspacer.setBorder(new MatteBorder(1, 1, 1, 1, Color.RED));
        panel.add(vspacer, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        JLabel hspacer = makeImageLabel("spacer256.gif", "");
        //hspacer.setBorder(new MatteBorder(1, 1, 1, 1, Color.RED));
        panel.add(hspacer, gbc);

        int gx = 0;
        int gy = 0;
        
        gbc.gridx = gx;
        gbc.gridy = gy;
        //gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 1;
        panel.add(makeMusicPanelContents_kiosk(), gbc);

        gbc.gridx = gx;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weighty = 1.0;
        gbc.weightx = 0.0;
        JLabel sp = makeTextLabel("");
        //sp.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.YELLOW));
        panel.add(sp, gbc);
        
        // select next/prev body buttons
        gbc.gridx = gx;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        //gbc.insets = new Insets(8, 0, 8, 0);
        gbc.anchor = GridBagConstraints.SOUTH;
        JPanel bck = makeBottomControls_kiosk(backgroundColor);
        //bck.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
        panel.add(bck, gbc);

        return panel;
    }

    private JPanel makeMusicPanelContents_kiosk() {
        return makePlayableSelectionPanel(backgroundColor);
    }
    
    private JPanel makeMusicPanelContents_kiosk_old() {
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        
        //panel.setBorder(new MatteBorder(1, 1, 1, 1, orrery.controlPanelSubBorder));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        JPanel playableEditor = makePlayableSelectionPanel(backgroundColor);
        panel.add(playableEditor, gbc);

        return panel;
    }

    public JPanel getBottomControls(Color backgroundColor) {
        if (kepler.kioskMode()) {
            return makeBottomControls_kiosk(backgroundColor);
        } else {
            return makeBottomControls_std(backgroundColor);
        }
    }
    
    private JPanel makeBottomControls_std(Color backgroundColor) {
        JPanel bottomControls = new JPanel();
        bottomControls.setBorder(new MatteBorder(1, 0, 0, 0, orrery.controlPanelBorder));
        bottomControls.setBackground(backgroundColor);
        
        // bottomControls
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 8, 0, 8);
        //gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton applyButton = makeImageButton("ApplyButton_up.gif", "ApplyButton_down.gif", "Apply");
        bottomControls.add(applyButton, gbc);
        applyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    applyWorldBuilderEdits();
                }
            });

        JButton applySelectNextButton = makeImageButton("ApplySelectNextButton_up.gif", "ApplySelectNextButton_down.gif", "Apply, then select next");
        bottomControls.add(applySelectNextButton, gbc);
        applySelectNextButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    applyWorldBuilderEdits();
                    selectNextBodyOrRock();
                }
            });

        JButton addBodyButton = makeImageButton("NewBody_up.gif", "NewBody_down.gif", "Add Body");
        bottomControls.add(addBodyButton, gbc);
        addBodyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addBody();
                }
            });

        JButton addRockButton = makeImageButton("NewRock_up.gif", "NewRock_down.gif", "Add Rock");
        bottomControls.add(addRockButton, gbc);
        addRockButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addRock();
                }
            });

        JButton deleteSelectedButton = makeImageButton("DeleteButton_up.gif", "DeleteButton_down.gif", "Delete Selected");
        bottomControls.add(deleteSelectedButton, gbc);
        deleteSelectedButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (selected != null) {
                        deleteSelected(selected);
                    }
                }
            });

        JButton saveButton = makeImageButton("SaveButton_up.gif", "SaveButton_down.gif", "Save the World");
        bottomControls.add(saveButton, gbc);
        saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // TODO: have a plug in world saver come up with a PrintStream
                    //       to write into, then call orrery.writeWorld(stream);

                    // TODO: determine whether to save or save-as
                    //       (first time a read-in world gets saved, make it save-as, with overwrite
                    //        comfirmation)
                    //

                    // TODO: bring up file chooser with the filename of the world.
                    String worldFilePath = orrery.getWorldFile();
                    File   worldFile = new File(worldFilePath);
                    String worldFileName = worldFile.getName();
                    if (fileChooserCurrentDirectory == null) {
                        File   targetDirectoryFile = new File(targetDirectory);
                        fileChooserCurrentDirectory = targetDirectoryFile;
                    }
                    
                    File   targetFile = new File(fileChooserCurrentDirectory, worldFileName);
                    
                    fileChooser.setCurrentDirectory(fileChooserCurrentDirectory);
                    fileChooser.setSelectedFile(targetFile);
                    int returnVal = fileChooser.showSaveDialog(wbPanel);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File saveFile = fileChooser.getSelectedFile();
                        fileChooserCurrentDirectory = fileChooser.getCurrentDirectory();
                        String canonicalPath;
                        try {
                            canonicalPath = saveFile.getCanonicalPath();
                        } catch (IOException ex) {
                            canonicalPath = saveFile.getAbsolutePath();
                        }                            
                        boolean actuallyWriteTheFile = true;
                        if (saveFile.exists()) {
                            Object[] options = {"Overwrite",
                                                "Cancel"};
                            String title = "Overwrite world file?";
                            String msg = "World file " + canonicalPath + " already exists. \nDo you want to overwrite it?";
                            //showOptionDialog(Component parentComponent, Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue);
                            ImageIcon icon = Kepler.createImageIcon("Question32.gif", "");
                            int n = JOptionPane.showOptionDialog(wbPanel,
                                                                 msg,
                                                                 title, 
                                                                 JOptionPane.YES_NO_OPTION,
                                                                 JOptionPane.QUESTION_MESSAGE,
                                                                 icon, 
                                                                 options,  //the titles of buttons
                                                                 options[1]); //default button title
                            actuallyWriteTheFile = (n == 0);
                        }
                        if (actuallyWriteTheFile) {
                            orrery.writeWorld(saveFile.getAbsolutePath());
                        }
                    } else {
                        System.out.println("Save command cancelled by user.");
                    }
                }
            });
        return bottomControls;
    }

    private JPanel makeBottomControls_kiosk(Color backgroundColor) {
        JPanel bottomControls = new JPanel();
        //bottomControls.setBorder(new MatteBorder(0, 0, 0, 0, orrery.controlPanelBorder));
        bottomControls.setBackground(backgroundColor);
        
        // bottomControls
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        //gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 8, 0, 8);
        //gbc.anchor = GridBagConstraints.EAST;
        //gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton selectPrevButton = makeImageButton("SelectPrevButton_up.gif", "SelectPrevButton_down.gif", "Select Prev");
        bottomControls.add(selectPrevButton, gbc);
        selectPrevButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    orrery.resetPlayTimer();
                    selectPrevBodyOrRock();
                }
            });

        JButton selectNextButton = makeImageButton("SelectNextButton_up.gif", "SelectNextButton_down.gif", "Select Next");
        bottomControls.add(selectNextButton, gbc);
        selectNextButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    orrery.resetPlayTimer();
                    selectNextBodyOrRock();
                }
            });

        return bottomControls;
    }

    public static JPanel makeModeTogglePanel(ControlPanel cp, final Orrery orrery) {
        JPanel panel = new JPanel();
        panel.setBackground(orrery.controlPanelBG);
        panel.setLayout(new GridBagLayout());
        //panel.setBorder(new MatteBorder(0, 0, 1, 0, orrery.controlPanelBorder));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        final JToggleButton playerTog =
            cp.makeImageToggleButton("PlayButton_up.gif", "PlayButton_down.gif", "Play mode");
        playerTog.setSelected(true);
        final JToggleButton wbTog = 
            cp.makeImageToggleButton("BuildButton_up.gif", "BuildButton_down.gif", "World Builder mode");
        playerTog.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("PLAYER toggle plressed.");
                    wbTog.setSelected(false);
                    orrery.playingMode();
                    System.out.println("Entering playing mode [B4 copyinitialbodies]. maxMass: " + orrery.maxMass + " Body.maxMass: " + Body.maxMass + " minMass: " + orrery.minMass + " Body.minMass: " + Body.minMass);
                    orrery.copyInitialBodies();
                    System.out.println("Entering playing mode [after copyinitialbodies]. maxMass: " + orrery.maxMass + " Body.maxMass: " + Body.maxMass + " minMass: " + orrery.minMass + " Body.minMass: " + Body.minMass);
                    orrery.resume();
                }
            });
        panel.add(playerTog, gbc);

        gbc.gridy = 0;
        gbc.gridx = 1;
        wbTog.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("WORLD BUILDER toggle pressed.");
                    playerTog.setSelected(false);
                    orrery.initialConditionsMode();
                    WorldBuilder wb = orrery.getWorldBuilder();
                    if (wb.selected == null) {
                        wb.selectNextBodyOrRock();
                    }
                }
            });
        panel.add(wbTog, gbc);

        //Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(playerTog);
        group.add(wbTog);

        return panel;
    }

    public void setInitialConditionsMode(boolean val) {
        if (val) {
            orrery.initialConditionsMode();
            kepler.setMode("initialconditions", val);
            unselect();
            selectNextBodyOrRock();
        } else {
            kepler.setMode("initialconditions", val);
            orrery.resetPlayTimer();
            System.out.println("Entering playing mode [B4 copyinitialbodies]. maxMass: " + orrery.maxMass + " Body.maxMass: " + Body.maxMass + " minMass: " + orrery.minMass + " Body.minMass: " + Body.minMass);
            orrery.redrawBackground();
            orrery.drawer.show(false);
            orrery.copyInitialBodies();
            System.out.println("Entering playing mode [after copyinitialbodies]. maxMass: " + orrery.maxMass + " Body.maxMass: " + Body.maxMass + " minMass: " + orrery.minMass + " Body.minMass: " + Body.minMass);
            unselect();
            //selectNextBodyOrRock();
            System.out.println("resuming.");
            orrery.drawer.show(true);
            orrery.repaint();
            orrery.resume();
            orrery.playingMode();
            System.out.println("resumED.");
        }
    }

    public void addBody() {
        addBody(0., 0.);
    }

    private void addBody(double px, double py) {
        Vect pos = new Vect(px, py);
        // TODO: if this position collides with an existing body, place it somewhere else.
        Vect v = new Vect(0., 0.);

        // TODO: might neeed to ask orery how many in the list
        double mass = orrery.findAverageMass(orrery.getInitialBodies());
        if (mass == 0.) {
            mass = Math.max(orrery.minMass, 1.9E14);
        }
        // TODO: increment maxBodies if necessary
        Body body = new Body(pos, v, mass, orrery.maxBodies);
        Rock[] rocklist;
        Body[] bodylist;
        if (orrery.isInitialConditionsMode()) {
            rocklist = orrery.getInitialRocks();
            bodylist = orrery.getInitialBodies();
        } else {
            rocklist = orrery.getRocks();
            bodylist = orrery.getBodies();
        }
        ensureBodyDoesntIntersectExisting(body, rocklist, bodylist);
        addBody(body);
    }

    public void addRock() {
        // TODO: if this position collides with an existing rock, place it somewhere else.
        
        addRock(0., 0.);
    }

    private void addRock(double px, double py) {
        System.out.println("Add Rock(" + px + ", " + py + ")");
        Vect pos = new Vect(px, py);
        // TODO: might neeed to ask orery how many in the list
        double mass = orrery.findAverageMass(orrery.getInitialRocks());
        if (mass == 0.) {
            mass = orrery.findAverageMass(orrery.getInitialBodies());
        }
        // TODO: increment maxBodies if necessary
        Rock rock = new Rock(pos, mass, orrery.maxRocks);
        Rock[] rocklist;
        Body[] bodylist;
        if (orrery.isInitialConditionsMode()) {
            rocklist = orrery.getInitialRocks();
            bodylist = orrery.getInitialBodies();
        } else {
            rocklist = orrery.getRocks();
            bodylist = orrery.getBodies();
        }

        ensureBodyDoesntIntersectExisting(rock, rocklist, bodylist);
        addRock(rock);
    }

    private void addBody(Body body) {
        if (orrery.isInitialConditionsMode()) {
            orrery.addInitialBody(body);
            orrery.worldBuilderDraw();
        } else {
            orrery.addBody(body);
            orrery.redraw();
        }
    }

    private void addRock(Rock rock) {
        if (orrery.isInitialConditionsMode()) {
            orrery.addInitialRock(rock);
            orrery.worldBuilderDraw();
        } else {
            orrery.addRock(rock);
            orrery.redraw();
        }
    }

    private void ensureBodyDoesntIntersectExisting(Body body, Rock[] rocklist, Body[] bodylist) {
        double rIncrement = orrery.radius / 13.;
        double thetaIncrement = 360. / 11.;
        boolean intersects = bodyIntersectsExisting(body, rocklist, bodylist);
        while (intersects) {
            Vect pos = body.pos;
            double r = pos.r() + rIncrement;
            if (r > orrery.radius) {
                r = 2 * Math.random() * rIncrement;
            }
            pos.setR(r);

            double thetaDegrees = pos.thetaDegrees() + thetaIncrement;
            if (thetaDegrees > 360.) {
                thetaDegrees =- 360.;
            }
            pos.setThetaDegrees(thetaDegrees);
            intersects = bodyIntersectsExisting(body, rocklist, bodylist);
        }
    }

    private boolean bodyIntersectsExisting(Body body, Rock[] rocklist, Body[] bodylist) {
        boolean found = false;
        for(int i=0; i < rocklist.length; i++) {
            if (rocklist[i] != null && rocklist[i].alive) {
                if (collides(body, rocklist[i], 1.5)) {
                    found = true;
                    return found;
                }
            }
        }
        for(int i=0; i < bodylist.length; i++) {
            if (bodylist[i] != null && bodylist[i].alive) {
                if (collides(body, bodylist[i], 1.5)) {
                    found = true;
                    return found;
                }
            }
        }
        return found;
    }

    private boolean collides(Body b1, Body b2, double fudge) {
        Vect delta = b1.pos.minus(b2.pos);
        double dist = delta.magnitude();
        return (dist < b1.radius * fudge || dist < b2.radius * fudge);
    }
    
    private void addClone(Body body, double mx, double my) {
        if (body instanceof Rock) {
            Rock rock = (Rock) body;
            Rock newRock = (Rock)rock.duplicate();
            newRock.pos = new Vect(mx, my);
            addRock(newRock);
        } else {
            System.out.println("WB: addClone(body: " + body + ") @ (" + mx + ", " + my + ")");
            Body newBody = body.duplicate();
            newBody.pos = new Vect(mx, my);
            addBody(newBody);
        }
    }


    public void deleteSelected() {
        System.out.println("WB: deleteSelected. selected: " + selected);
        if (selected != null) {
            deleteSelected(selected);
        }
    }
    
    private void deleteSelected(Body body) {
        if (body instanceof Rock) {
            deleteRock((Rock)body);
        } else {
            deleteBody(body);
        }
        orrery.worldBuilderDraw();
    }

    private void deleteBody(Body body) {
        if (orrery.isInitialConditionsMode()) {
            orrery.deleteInitialBody(body);
        } else {
            orrery.deleteBody(body);
        }
    }

    private void deleteRock(Rock rock) {
        if (orrery.isInitialConditionsMode()) {
            orrery.deleteInitialRock(rock);
        } else {
            orrery.deleteRock(rock);
        }
    }

    public void updateSelected() {
        updateSelected(selected, true);
    }
    
    private void updateSelected(Body body, boolean updateInstruments) {
        //System.out.println("UPdate Body. " + body);
        if (body == null) {
            clearRockBodyEditor();
        } else {
            updateBody(body, updateInstruments);
        }
    }
    
    private void updateSelected(Rock rock, boolean updateInstruments) {
        if (rock == null) {
            clearRockBodyEditor();
        } else {
            //System.out.println("UPdate ROCK. " + rock);
            updateRock(rock, updateInstruments);
        }
    }

    private void updateBody(Body body, boolean updateInstruments) {
        updateCommon(body, updateInstruments);
    }

    private void updateRock(Rock rock, boolean updateInstruments) {
        updateCommon(rock, updateInstruments);
    }

    private void updateCommon(Body body, boolean updateInstruments) {
       if (kepler.kioskMode()) {
            updateCommon_kiosk(body, updateInstruments);
        } else {
            updateCommon_std(body, updateInstruments);
        }
    }
    
    private void updateCommon_std(Body body, boolean updateInstruments) {
        displayText(massText, body.mass);
        //
        // Location
        //
        Vect p = body.pos;
        /* debug
        System.out.println("UpdateCOmmon. p: " + p);
        System.out.println("UpdateCOmmon. offset: " + body.posOffset);
        if (body.posOffset != null) {
            p = p.minus(body.posOffset);
            System.out.println("UpdateCOmmon. p- offset: " + p);
        }
        */
        if (body.posSpecifiedPolar) {
            posCoordTypeToggle.setSelected(true);
            double r = p.r();
            double theta = p.thetaDegrees();
            posCoord1Label.setText("r");
            posCoord2Label.setText("theta");
            displayText(posCoord1Text,  r);
            displayText(posCoord2Text,  theta);
        } else {
            posCoordTypeToggle.setSelected(false);
            double x = p.x();
            double y = p.y();
            posCoord1Label.setText("X");
            posCoord2Label.setText("Y");
            displayText(posCoord1Text,  x);
            displayText(posCoord2Text,  y);
        }
        if (body.posOffset != null) {
            displayText(offsetXText, body.posOffset.x());
            displayText(offsetYText, body.posOffset.y());
        } else {
            displayText(offsetXText, "");
            displayText(offsetYText, "");
        }

        //
        // Velocity
        //
        if (!(body instanceof Rock)) {
            if (body.vSpecifiedPolar) {
                vCoordTypeToggle.setSelected(true);
                double vr = body.v.r();
                double vtheta = body.v.thetaDegrees();
                posCoord1Label.setText("r");
                posCoord2Label.setText("theta");
                displayText(vCoord1Text,  vr);
                displayText(vCoord2Text,  vtheta);
            } else {
                vCoordTypeToggle.setSelected(false);
                double vx = body.v.x();
                double vy = body.v.y();
                posCoord1Label.setText("X");
                posCoord2Label.setText("Y");
                displayText(vCoord1Text,  vx);
                displayText(vCoord2Text,  vy);
            }
        }

        //
        // Melody, instrument
        //
        updatePlayableChooser(body, updateInstruments);
        
        updateMutatorPanel(selected);
        
    }

    private void updateCommon_kiosk(Body body, boolean updateInstruments) {
        //
        // Mass
        //
        /*
        displayText(massText_kiosk, body.mass);
        int massSliderVal = uncookLinearValue(body.mass,
                                              mass_slider_kiosk_low, mass_slider_kiosk_high) - 1;
        if (massSliderVal < 0) {
            massSliderVal = 0;
        }
        massSlider.setValue(massSliderVal);
        */
        
        //
        // Location
        //
        /*
        Vect p = body.pos;
        System.out.println("UpdateCOmmon. p: " + p);
        System.out.println("UpdateCOmmon. offset: " + body.posOffset);
        if (body.posOffset != null) {
            p = p.minus(body.posOffset);
            System.out.println("UpdateCOmmon. p- offset: " + p);
        }
        double x = p.x();
        double y = p.y();
        double r = p.r();
        double theta = p.thetaDegrees();
        System.out.println("UpdateCOmmon. x: " + x + " y: " + y + " r: " + r + " thdeg: " + theta);
        if (body.posSpecifiedPolar) {
            //polarRadio_pos.setSelected(true);
            posCoord1Label.setText("r");
            posCoord2Label.setText("th");
            displayText(posCoord1Text,  r);
            displayText(posCoord2Text,  theta);
        } else {
            posCoord1Label.setText("X");
            posCoord2Label.setText("Y");
            displayText(posCoord1Text,  x);
            displayText(posCoord2Text,  y);
            //cartesianRadio_pos.setSelected(true);
        }
        */

        //
        // Melody, instrument
        //
        if (!showingPhysicsMode) {
            updatePlayableChooser(body, updateInstruments);
        }

    }

    private void updatePlayableChooser(Body body, boolean updateInstruments) {
        System.out.println("UPC: body=" + body + " @ " + body.pos + " body.playable: " + body.getPlayable());
        if (body.getPlayable() == null) {
            playableTypeChooser.setSelectedItem("");
            System.out.println("Calling ppulate plbl chooser on playableChooser (from update common) BODY HAS NULL PLAYABLE");
            populatePlayableChooser("", "");
            //selectPlayable("", "");

        } else {
            if (true || updateInstruments) {
                Playable playable = body.getPlayable();
                String ptype = playable.getType();
                populatePlayableChooser(ptype, playable.getName());
                //playableTypeChooser.setSelectedItem(ptype);
                System.out.println(" calling selectplayable: ptype=" + ptype + " name: " + playable.getName());
                selectPlayable(ptype, playable.getName());
                showPlayableCurrentState(playable);
            }
        }
        String instrumentName = (String)orrery.channelMap.get("" + body.channel);
        populateInstrumentChooser();
        if (instrumentName == null) {
            instrumentChooser.setSelectedItem("");
        } else {
            instrumentChooser.setSelectedItem(instrumentName);
        }
    }

    public void showPlayableCurrentState(final Playable playable) {
        Runnable doWorkRunnable = new Runnable() {
                public void run() { 
                    if (playablePreviewText != null && playable != null) {
                        StringBuffer bodybuf = new StringBuffer();
                        playable.writeCurrentState(bodybuf);
                        //playable.writeBody(bodybuf);
                        playablePreviewText.setText(bodybuf.toString());
                    }
                }
            };
        SwingUtilities.invokeLater(doWorkRunnable);
    }

                
    private void populateInstrumentChooser() {
        instrumentChooser.removeAllItems();
        instrumentChooser.addItem("");
        int numChannels = 16;
        for(int i=0; i < numChannels; i++) {
            String name = orrery.getChannelName(i);
            instrumentChooser.addItem(name);
        }
    }

    private void clearRockBodyEditor() {
        if (kepler.kioskMode()) {
            clearRockBodyEditor_kiosk();
        } else {
            clearRockBodyEditor_std();
        }
    }

    private void clearRockBodyEditor_std() {
        massText.setText("");
        posCoord1Text.setText("");
        posCoord2Text.setText("");
        vCoord1Text.setText("");
        vCoord2Text.setText("");
        offsetXText.setText("");
        offsetYText.setText("");
        playableTypeChooser.setSelectedItem("");
        selectPlayable("", "");
        instrumentChooser.setSelectedItem("");
    }

    private void clearRockBodyEditor_kiosk() {
        massText_kiosk.setText("");
        posCoord1TextLabel.setText("");
        posCoord2TextLabel.setText("");
    }

    private JPanel makeBodyEditorPanel_std() {
        return makeBodyEditorPanel_std(BODY);
    }
    
    private JPanel makeBodyEditorPanel_std(int type) {
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(makeTextLabel("Mass"), gbc);
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 1, 2, 0);
        massText = makeNumberField();
        panel.add(massText, gbc);
    
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(makeLocationPanel(), gbc);
        if (type == BODY) {
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.gridwidth = 3;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(makeVelocityPanel(), gbc);
        }

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(makePlayableSelectionPanel(backgroundColor), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(makeMutatorPanel(), gbc);
        return panel;
    }


    private JPanel makeBodyEditorPanel_kiosk() {
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        //panel.setBorder(new MatteBorder(1, 1, 1, 1, orrery.controlPanelSubBorder));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        addMass_kiosk(panel);
        addLocations_kiosk(panel);

        return panel;
    }

    double mass_slider_kiosk_low = 1.e14;
    double mass_slider_kiosk_high = 100.e14;
    JPanel addMass_kiosk(JPanel panel) {
        //panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(orrery.controlPanelSubBorder, 1),
        //                                                 "Mass"));
        //panel.setBorder(new MatteBorder(1, 1, 1, 1, orrery.controlPanelSubBorder));
        GridBagConstraints gbc = new GridBagConstraints();


        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 1, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(makeTextLabel("Mass"), gbc);

        gbc.gridy++;
        massSlider = makeSliderV();
        massSlider.setBackground(backgroundColor);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridheight = 2;
        panel.add(massSlider, gbc);

        gbc.gridy += 2;
        gbc.gridheight = 1;
        massText_kiosk = makeValueLabel("");
        // TODO: readonly
        panel.add(massText_kiosk, gbc);

        massSlider.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                    orrery.resetPlayTimer();
                    JSlider source = (JSlider)e.getSource();
                    int rawValue = (int)source.getValue() + 1;
                    double cookedValue = cookLinearValue(rawValue, mass_slider_kiosk_low, mass_slider_kiosk_high);
                    System.out.println("MassSlider. stateChanged() raw: " + rawValue + " cooked: " + cookedValue);
                    
                    if (selected != null) {
                        String displayable = "" + cookedValue;
                        massText_kiosk.setText(displayable);

                        double mass = cookedValue;
                        selected.setMass(mass);
                        orrery.worldBuilderDraw();
                    }
                }
            });

        return panel;
    }
    
    JPanel addLocations_kiosk(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.insets = new Insets(50, 0, 2, 5);
        posCoord1Label = makeTextLabel("X");
        panel.add(posCoord1Label, gbc);
        posCoord1TextLabel = makeValueLabel("");
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel.add(posCoord1TextLabel, gbc);

        gbc.gridy++;
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = new Insets(0, 0, 0, 5);
        posCoord2Label = makeTextLabel("");
        panel.add(posCoord2Label, gbc);
        posCoord2TextLabel = makeValueLabel("");
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(posCoord2TextLabel, gbc);

        return panel;
    }
    
    // controls for the playable editor. 
    JComboBox  peSelectorName = null;
    JComboBox  peSelectorType = null;
    JTextField peEditorName = null;
    JTextArea  peEditorBody = null;
    JComboBox  pePreviewInstrument = null;

    private JPanel makePlayableEditorPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        peSelectorType = makeDropDown();
        peSelectorType.addItem("melody");
        peSelectorType.addItem("scale");
        peSelectorType.addItem("sequence");
        peSelectorType.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    orrery.resetPlayTimer();
                    JComboBox cb = (JComboBox)evt.getSource();
                    String type = (String)cb.getSelectedItem();
                    System.out.println("Calling ppulate plbl chooser on peSelectorName");
                    populatePlayableChooser(peSelectorType, peSelectorName, type, "__first__");
                }
            });
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.;
        panel.add(peSelectorType, gbc);

        peSelectorName = makeDropDown();
        gbc.gridx ++;
        gbc.weightx = 1.;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(peSelectorName, gbc);
        peSelectorName.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    orrery.resetPlayTimer();
                    JComboBox cb = (JComboBox)evt.getSource();
                    String name = (String)cb.getSelectedItem();
                    fillPlayableEditor((String)peSelectorType.getSelectedItem(), name);
                }
            });

        // TODO: make the name editable in the combobox
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0.;
        gbc.gridwidth = 1;
        panel.add(makeTextLabel("name"), gbc);
        peEditorName = makeTextField(15);
        gbc.gridx++;
        gbc.weightx = 1.;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(peEditorName, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.;
        gbc.weighty = 1.;
        gbc.fill = GridBagConstraints.BOTH;
        peEditorBody = makeTextArea();
        //JScrollPane scrollPane = new JScrollPane(peEditorBody); 
        //panel.add(scrollPane, gbc);
        panel.add(peEditorBody, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0.;
        gbc.weighty = 0.;
        gbc.gridwidth = 2;

        JPanel ctlpanel = new JPanel();
        ctlpanel.setBorder(new MatteBorder(0, 0, 0, 0, orrery.controlPanelBorder));
        ctlpanel.setBackground(orrery.controlPanelBG);
        ctlpanel.setLayout(new GridBagLayout());
        panel.add(ctlpanel, gbc);

        GridBagConstraints cgbc = new GridBagConstraints();

        cgbc.gridx = 0;
        cgbc.gridy = 0;
        cgbc.insets = new Insets(6, 4, 4, 0);
        JButton updateButton = makeImageButton("UpdateButton_small_up.gif", "UpdateButton_small_down.gif", "Update named melody/sequence/etc.");
        updateButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String type = (String)peSelectorType.getSelectedItem();
                    // TODO: find way to keep the old name, for when we consolidate the peSelectorName and peEditorName,
                    // and in case the user has changed the name, but still hits update. 
                    String name = (String)peSelectorName.getSelectedItem();
                    String maybeNewName = peEditorName.getText();
                    String body = peEditorBody.getText();
                    updatePlayable(type, name, maybeNewName, body);
                }
            });
        ctlpanel.add(updateButton, cgbc);

        cgbc.gridx++;
        cgbc.weightx = 0.;
        cgbc.weighty = 0.;
        JButton addButton = makeImageButton("AddButton_small_up.gif", "AddButton_small_down.gif", "Add new melody/sequence/etc.");
        addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String type = (String)peSelectorType.getSelectedItem();
                    String newName = peEditorName.getText();
                    String body = peEditorBody.getText();
                    addPlayable(type, newName, body);
                }
            });
        ctlpanel.add(addButton, cgbc);


        cgbc.gridx++;
        cgbc.weightx = 0.;
        cgbc.weighty = 0.;
        JButton deleteButton = makeImageButton("DeleteButton_small_up.gif", "DeleteButton_small_down.gif", "Delete melody/sequence/etc.");
        deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String type = (String)peSelectorType.getSelectedItem();
                    String name = (String)peSelectorName.getSelectedItem();
                    deletePlayable(type, name);
                }
            });
        ctlpanel.add(deleteButton, cgbc);

        cgbc.gridx++;
        cgbc.weightx = 0.;
        cgbc.weighty = 0.;
        JButton resetButton = makeImageButton("ResetButton_small_up.gif", "ResetButton_small_down.gif", "Reset melody editor.");
        resetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String type = (String)peSelectorType.getSelectedItem();
                    String name = (String)peSelectorName.getSelectedItem();
                    fillPlayableEditor(type, name);
                }
            });
        ctlpanel.add(resetButton, cgbc);

        cgbc.gridx++;
        cgbc.weightx = 1.0;
        ctlpanel.add(new JLabel(""), cgbc);

        cgbc.gridx++;
        cgbc.weightx = 0.;
        cgbc.weighty = 0.;
        cgbc.insets = new Insets(6, 12, 4, 0);
        editedPreviewButton = makeImageButton("PreviewButton_small_up.gif", "PreviewButton_small_down.gif", "Preview on this instrument");
        editedPreviewButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String type = (String)peSelectorType.getSelectedItem();
                    String maybeNewName = peEditorName.getText();
                    String body = peEditorBody.getText();
                    String instrument = (String)pePreviewInstrument.getSelectedItem();
                    previewEditedPlayable(type, maybeNewName, body, instrument);
                }
            });
        ctlpanel.add(editedPreviewButton, cgbc);
        editedStopButton = makeImageButton("StopButton_small_up.gif", "StopButton_small_down.gif", "Stop preview in progress");
        editedStopButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    stopPreviewThread();
                }
            });
        editedStopButton.setVisible(false);
        ctlpanel.add(editedStopButton, cgbc);

        cgbc.gridx++;
        cgbc.weightx = 0.;
        cgbc.weighty = 0.;
        cgbc.insets = new Insets(6, 0, 4, 0);
        ctlpanel.add(makeTextLabel(" on "), cgbc);

        pePreviewInstrument = makeDropDown();
        cgbc.gridx++;
        cgbc.gridwidth = 2;
        cgbc.weightx = 1.0;
        cgbc.weighty = 0.;
        cgbc.insets = new Insets(6, 0, 4, 8);
        ctlpanel.add(pePreviewInstrument, cgbc);

        return panel;
    }

    private void updatePlayable(String type, String name, String maybeNewName, String body) {
        // for now: if the name is new, add a new playable.
        Playable pl = findPlayable(type, maybeNewName);
        if (pl == null) {
            addPlayable(type, maybeNewName, body);
            return;
        }
        Playable newPl = orrery.parsePlayable(type, maybeNewName, body);
        orrery.storePlayable(newPl);

        //TODO: run through the (initial) bodies & rocks, and refresh the playable on any body that has this one.
        int nbodies_initial = orrery.getInitialBodyCount();
        int nrocks_initial = orrery.getInitialRockCount();
        Body [] initial_bodies = orrery.getInitialBodies();
        Rock [] initial_rocks = orrery.getInitialRocks();

        for(int i=0; i < nbodies_initial; i++) {
            Body b = initial_bodies[i];
            if (b != null) {
                Playable bpl = b.getPlayable();
                if (bpl != null &&
                    bpl.getType().equals(type) &&
                    bpl.getName().equals(maybeNewName)) {
                    orrery.setPlayable(b, type, maybeNewName, bpl.isShared());
                }
            }
        }
        for(int i=0; i < nrocks_initial; i++) {
            Rock r = initial_rocks[i];
            if (r != null) {
                Playable rpl = r.getPlayable();
                if (rpl != null &&
                    rpl.getType().equals(type) &&
                    rpl.getName().equals(maybeNewName)) {
                    orrery.setPlayable(r, type, maybeNewName, rpl.isShared());
                }
            }
        }
    }

    private void addPlayable(String type, String newName, String body) {
        Playable pl = findPlayable(type, newName);
        if (pl != null) {
            // INDICATE ERROR!!
            System.out.println("ERROR: " + type + " " + newName + " already exists.");
            return;
        }
        Playable newPl = orrery.parsePlayable(type, newName, body);
        orrery.storePlayable(newPl);
    }

    private void deletePlayable(String type, String name) {
        orrery.removePlayable(type, name);
        // TODO: remove that playable from any bodies that have it. 
        int nbodies_initial = orrery.getInitialBodyCount();
        int nrocks_initial = orrery.getInitialRockCount();
        Body [] initial_bodies = orrery.getInitialBodies();
        Rock [] initial_rocks = orrery.getInitialRocks();
        for(int i=0; i < nbodies_initial; i++) {
            Body b = initial_bodies[i];
            if (b != null) {
                Playable bpl = b.getPlayable();
                if (bpl != null &&
                    bpl.getType().equals(type) &&
                    bpl.getName().equals(name)) {
                    b.setPlayable(null);
                }
            }
        }
        for(int i=0; i < nrocks_initial; i++) {
            Rock r = initial_rocks[i];
            if (r != null) {
                Playable rpl = r.getPlayable();
                if (rpl != null &&
                    rpl.getType().equals(type) &&
                    rpl.getName().equals(name)) {
                    r.setPlayable(null);
                }
            }
        }
    }

    private void previewEditedPlayable(String type, String currentName, String body, String instrument) {
        Playable playable = orrery.parsePlayable(type, currentName, body);
        int channel = orrery.getInstrumentChannel(instrument);
        if (playable != null && channel != -1) {
            startPreviewThread(playable, channel, editedPreviewButton, editedStopButton);
        }
    }

    private void fillPlayableEditor(String type, String name) {
        Playable pl = findPlayable(type, name);
        if (pl == null) {
            peEditorName.setText("");
            peEditorBody.setText("");
        } else {
            peEditorName.setText(name);
            StringBuffer bodybuf = new StringBuffer();
            pl.writeBody(bodybuf);
            peEditorBody.setText(bodybuf.toString());
        }
    }

    private Playable findPlayable(String type, String name) {
        if (type.equals("note")) {
            return new Note(name);
        } else if (type.equals("melody")) {
            return orrery.getMelody(name);
        } else if (type.equals("scale")) {
            return orrery.getScale(name);
        } else if (type.equals("sequence")) {
            return orrery.getSequence(name);
        }
        return null;
    }

    private JTextField[] channelNames;
    private JTextField[] channelBanks;
    private JTextField[] channelPrograms;
    
    private JPanel makeInstrumentsEditorPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(orrery.controlPanelBG);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        int numChannels = 16;
        channelNames = new JTextField[numChannels];
        channelBanks = new JTextField[numChannels];
        channelPrograms = new JTextField[numChannels];
        
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(makeTextLabel("channel"), gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(makeTextLabel(" name"), gbc);
        gbc.gridx++;
        panel.add(makeTextLabel("bank"), gbc);
        gbc.gridx++;
        panel.add(makeTextLabel("program"), gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(makeTextLabel(""), gbc); // later: ""
        
        for(int i=0; i < numChannels; i++) {
            //String name = orrery.getChannelName(i);
            //int bank = orrery.getChannelBank(i);
            //int program = orrery.getChannelProgram(i);

            gbc.gridy = i + 1;
            gbc.gridx = 0;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.EAST;
            panel.add(makeTextLabel("" + i), gbc);

            gbc.gridx++;
            gbc.anchor = GridBagConstraints.WEST;
            channelNames[i] = makeTextField();
            channelNames[i].setColumns(15);
            panel.add(channelNames[i], gbc);
            gbc.gridx++;
            channelBanks[i] = makeTextField();
            channelBanks[i].setColumns(4);
            panel.add(channelBanks[i], gbc);
            gbc.gridx++;
            channelPrograms[i] = makeTextField();
            channelPrograms[i].setColumns(4);
            panel.add(channelPrograms[i], gbc);
            gbc.gridx++;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(makeTextLabel(""), gbc); // later: ""
        }
        return panel;
    }

    private void fillInstrumentsPanel() {
        if (kepler.kioskMode()) {
            return;
        }
        int numChannels = 16;
        System.out.println("Fill instruments panel");
        pePreviewInstrument.removeAllItems();
        for(int i=0; i < numChannels; i++) {
            String name = orrery.getChannelName(i);
            int bank = orrery.getChannelBank(i);
            int program = orrery.getChannelProgram(i);
            System.out.println("   ch: " + i + " " + name + " " + bank + " " + program);
            if (name == null || name.trim().equals("")) {  
                name = "inst" + i;
            }
            channelNames[i].setText(name);
            channelBanks[i].setText("" + bank);
            channelPrograms[i].setText("" + program);
            pePreviewInstrument.addItem(name);

        }
    }


    private JPanel makeSqueegee(String title, JPanel contentPanel, boolean open) {
        JPanel panel = new JPanel();
        panel.setBackground(orrery.controlPanelBG);
        //panel.setBackground(orrery.controlPanelSubBorder);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new MatteBorder(0, 0, 1, 0, orrery.squeegeeTabBG));
        GridBagConstraints gbc = new GridBagConstraints();

        //
        // Title with expand/collapse button
        //
        ImageIcon expandTabIcon = Kepler.createImageIcon("expand" + title + ".gif", "");
        ImageIcon collapseTabIcon = Kepler.createImageIcon("collapse" + title + ".gif", "");
        JToggleButton tabButton = new JToggleButton(expandTabIcon);
        tabButton.setBorder(new MatteBorder(0, 0, 0, 0, Color.RED));
        //tabButton.setBorderPainted(false);
        tabButton.setMargin(new Insets(0, 0, 0, 0));
        tabButton.setContentAreaFilled(false);
        tabButton.setBackground(orrery.bgColor);
        tabButton.setSelectedIcon(collapseTabIcon);
        tabButton.setPressedIcon(collapseTabIcon);
        tabButton.setSelected(open);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(tabButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(new JLabel(""), gbc); // filler
        
        contentPanel.setBorder(new MatteBorder(2, 1, 0, 1, orrery.squeegeeTabBG));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(contentPanel, gbc);
        contentPanel.setVisible(open);

        tabButton.addActionListener(new SqueegeeTabListener(tabButton, contentPanel));

        return panel;
    }

    class SqueegeeTabListener implements ActionListener {
        private JToggleButton button;
        private JPanel contentPanel;
        public SqueegeeTabListener(JToggleButton button, JPanel contentPanel) {
            this.button = button;
            this.contentPanel = contentPanel;
        }
        public void actionPerformed(ActionEvent evt) {
            boolean open = button.isSelected();
            contentPanel.setVisible(open);
        }
    }


    
    private JComboBox  makePlayableTypeChooser() {
        JComboBox ptDrop = makeDropDown();
        ptDrop.addItem("");
        ptDrop.addItem("note");
        ptDrop.addItem("melody");
        ptDrop.addItem("sequence");
        ptDrop.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (!playableChooserUpdating) {
                        orrery.resetPlayTimer();
                        JComboBox cb = (JComboBox)evt.getSource();
                        String type = (String)cb.getSelectedItem();
                        System.out.println("Calling ppulate plbl chooser on playableChooser (from ptype drop)");
                        populatePlayableChooser(type, "");
                    }
                }
            });
        return ptDrop;
    }

    private void selectPlayable(String playableType, String playableSelection) {
        selectPlayable(playableTypeChooser, playableChooser, playableType, playableSelection);
    }
    
    private void selectPlayable(JComboBox playableTypeChooser,
                                JComboBox playableChooser,
                                String playableType,
                                String playableSelection) {
        playableTypeChooser.setSelectedItem(playableType);
        System.out.println("selectPlayable(" + playableType + ", " + playableSelection + ")");
        if (playableType.equals("")) {
            playableChooser.setSelectedItem("");
            noteChooser.setText("");
        } else if (playableType.equals("note")) {
            playableChooser.setSelectedItem("");
            noteChooser.setText(playableSelection);
        }  else if (playableType.equals("melody")) {
            playableChooser.setSelectedItem(playableSelection);
            noteChooser.setText("");
        }  else if (playableType.equals("sequence")) {
            playableChooser.setSelectedItem(playableSelection);
            noteChooser.setText("");
        } else {
            playableChooser.setSelectedItem(playableSelection);
            noteChooser.setText("");
        }
    }
    
    private void populatePlayableChooser(String playableType,
                                         String playableSelection) {
        populatePlayableChooser(playableTypeChooser, playableChooser, playableType, playableSelection);
    }
    
    private void populatePlayableChooser(JComboBox playableTypeDrop,
                                         JComboBox playableDrop,
                                         String playableType,
                                         String playableSelection) {
        if (playableTypeDrop == null || playableDrop == null) {
            return;
        }
        /*
        if (kepler.kioskMode()) {
            return;
        }
        */
        playableChooserUpdating = true;
        System.out.println("PopulatePlayableChooser(drop=" + playableDrop + " type=" + playableType + ", selection: " + playableSelection + ")");
        if (playableType.equals("note")) {
            // do something funky here for notes.
            // or switch the playable chooser with a text field for showing the note
            playableDrop.setVisible(false);
            if (noteChooser != null) {
                noteChooser.setVisible(true);
            }
        } else {
            String first = null;
            playableDrop.removeAllItems();
            playableDrop.addItem("");
            if (playableType.equals("melody")) {
                for(Iterator it = orrery.getMelodyNames(); it.hasNext(); ) {
                    String mname = (String)it.next();
                    if (first == null) {
                        first = mname;
                    }
                    System.out.println("Adding melody: " + mname);
                    playableDrop.addItem(mname);
                }
            } else if (playableType.equals("scale")) {
                for(Iterator it = orrery.getScaleNames(); it.hasNext(); ) {
                    String sname = (String)it.next();
                    if (first == null) {
                        first = sname;
                    }
                    System.out.println("Adding scale: " + sname);
                    playableDrop.addItem(sname);
                }
            } else if (playableType.equals("sequence")) {
                for(Iterator it = orrery.getSequenceNames(); it.hasNext(); ) {
                    String sname = (String)it.next();
                    if (first == null) {
                        first = sname;
                    }
                    playableDrop.addItem(sname);
                }
            }
            if (noteChooser != null) {
                noteChooser.setVisible(false);
            }
            playableDrop.setVisible(true);

            System.out.println("PPC: playableSelection: " + playableSelection + " first: " + first);
            if (playableSelection == "__first__") {
                playableSelection = first;
            }
        }

        selectPlayable(playableTypeDrop, playableDrop, playableType, playableSelection);
        playableChooserUpdating = false;
    }
    
    private JPanel makeRockEditorPanel() {
        return makeBodyEditorPanel_std(ROCK);
    }

    private JPanel makeLocationPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(orrery.controlPanelBG);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(orrery.controlPanelSubBorder, 1),
                                                         "Location"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        posCoordTypeToggle = makeImageToggleButton("CartesianPolarToggle_cart.gif",
                                                   "CartesianPolarToggle_polar.gif",
                                                   "Select cartesian or polar coordinate system.");
        posCoordTypeToggle.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (posCoordTypeToggle.isSelected()) {
                        posCoord1Label.setText("r");
                        posCoord2Label.setText("theta");
                        // convert numbers in the field from polar to cartesian
                        try {
                            double x = extractDouble(posCoord1Text, "posX");
                            double y = extractDouble(posCoord2Text, "posY");
                            Vect p = new Vect(x, y); 
                            displayText(posCoord1Text, p.r());
                            displayText(posCoord2Text, p.thetaDegrees());
                        } catch (Exception ex) {
                            // if there aren't number in there, leave it alone.
                        }
                    } else {
                        posCoord1Label.setText("X");
                        posCoord2Label.setText("Y");
                        // convert numbers in the field from polar to cartesian
                        try {
                            double r =     extractDouble(posCoord1Text, "posR");
                            double theta = extractDouble(posCoord2Text, "posTheta");
                            Vect p = Vect.createPolarDegrees(r, theta);
                            displayText(posCoord1Text, p.x());
                            displayText(posCoord2Text, p.y());
                        } catch (Exception ex) {
                            // if there aren't number in there, leave it alone.
                        }
                    }
                }
            });
        /*
        cartesianRadio_pos = makeRadioButton("Cartesian_on.gif", "Cartesian_off.gif", "Cartesian");
        cartesianRadio_pos.setForeground(orrery.titleColor);
        cartesianRadio_pos.setBackground(orrery.controlPanelBG);
        */
        panel.add(posCoordTypeToggle, gbc);
        gbc.gridx++;
        gbc.insets = new Insets(0, 5, 0, 0);
        posCoord1Label = makeTextLabel("X");
        panel.add(posCoord1Label, gbc);
        posCoord1Text = makeNumberField();
        gbc.gridx++;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(posCoord1Text, gbc);

        // TODO: twiddling the coordinate type toggles needs to change the display in the
        //       fields.
        gbc.gridx++;
        gbc.insets = new Insets(0, 15, 0, 0);
        posCoord2Label = makeTextLabel("Y");
        panel.add(posCoord2Label, gbc);
        posCoord2Text = makeNumberField();
        gbc.gridx++;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(posCoord2Text, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(makeTextLabel("Offset"), gbc);
        gbc.gridx++;
        gbc.insets = new Insets(0, 5, 0, 0);
        panel.add(makeTextLabel("X"), gbc);
        offsetXText = makeNumberField();
        gbc.gridx++;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(offsetXText, gbc);

        gbc.gridx++;
        gbc.insets = new Insets(0, 15, 0, 0);
        panel.add(makeTextLabel("Y"), gbc);
        offsetYText = makeNumberField();
        gbc.gridx++;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(offsetYText, gbc);
                  
        return panel;
    }

    private JPanel makeVelocityPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(orrery.controlPanelBG);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(orrery.controlPanelSubBorder, 1),
                                                         "Velocity"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        vCoordTypeToggle = makeImageToggleButton("CartesianPolarToggle_cart.gif",
                                                 "CartesianPolarToggle_polar.gif",
                                                 "Select cartesian or polar coordinate system.");
        vCoordTypeToggle.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (vCoordTypeToggle.isSelected()) {
                        vCoord1Label.setText("r");
                        vCoord2Label.setText("theta");
                        // convert numbers in the field from cartesian  to polar
                        try {
                            double x = extractDouble(vCoord1Text, "vX");
                            double y = extractDouble(vCoord2Text, "vY");
                            Vect p = new Vect(x, y); 
                            displayText(vCoord1Text, p.r());
                            displayText(vCoord2Text, p.thetaDegrees());
                        } catch (Exception ex) {
                            // if there aren't number in there, leave it alone.
                        }
                    } else {
                        vCoord1Label.setText("X");
                        vCoord2Label.setText("Y");
                        // convert numbers in the field from polar to cartesian
                        try {
                            double r = extractDouble(vCoord1Text, "vR");
                            double theta = extractDouble(vCoord2Text, "vTheta");
                            Vect p = Vect.createPolarDegrees(r, theta);
                            displayText(vCoord1Text, p.x());
                            displayText(vCoord2Text, p.y());
                        } catch (Exception ex) {
                            // if there aren't number in there, leave it alone.
                        }
                    }
                }
            }); 
        /*
        cartesianRadio_v = makeRadioButton("Cartesian_on.gif", "Cartesian_off.gif", "Cartesian");
        cartesianRadio_v.setForeground(orrery.titleColor);
        cartesianRadio_v.setBackground(orrery.controlPanelBG);
        */
        panel.add(vCoordTypeToggle, gbc);
        gbc.gridx++;
        vCoord1Label = makeTextLabel("X");
        panel.add(vCoord1Label, gbc);
        vCoord1Text = makeNumberField();
        gbc.gridx++;
        panel.add(vCoord1Text, gbc);

        gbc.gridx++;
        vCoord2Label = makeTextLabel("Y");
        panel.add(vCoord2Label, gbc);
        vCoord2Text = makeNumberField();
        gbc.gridx++;
        panel.add(vCoord2Text, gbc);
                  
        return panel;
    }

    JPanel makePlayableSelectionPanel(Color backgroundColor) {
        if (kepler.kioskMode()) {
            return makePlayableSelectionPanel_kiosk(backgroundColor);
        } else {
            return makePlayableSelectionPanel_std(backgroundColor);
        }
    }
    
    JPanel makePlayableSelectionPanel_std(Color backgroundColor) {
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(orrery.controlPanelSubBorder, 1),
                                                         "Play"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(makeTextLabel("Type"), gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        playableTypeChooser = makePlayableTypeChooser();
        panel.add(playableTypeChooser, gbc);

        gbc.gridx++;
        noteChooser = makeTextField();
        playableChooser = makeDropDown();
        playableChooser.setVisible(false);
        panel.add(noteChooser, gbc);
        panel.add(playableChooser, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(makeTextLabel("Instrument"), gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        // TODO: make this a drop-down
        instrumentChooser = makeDropDown();
        panel.add(instrumentChooser, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        currentPreviewButton = makeImageButton("PreviewButton_small_up.gif", "PreviewButton_small_down.gif", "Preview");
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(currentPreviewButton, gbc);
        currentPreviewButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    previewCurrentPlayableInstrumentSelection();
                }
            });
        currentStopButton = makeImageButton("StopButton_small_up.gif", "StopButton_small_down.gif", "Stop preview in progress");
        currentStopButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    stopPreviewThread();
                }
            });
        currentStopButton.setVisible(false);
        panel.add(currentStopButton, gbc);

        return panel;
    }

    JPanel makePlayableSelectionPanel_kiosk(Color backgroundColor) {
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.setLayout(new GridBagLayout());
        //panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(orrery.controlPanelSubBorder, 1),
        //                                                 "Play"));
        //panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GREEN));
        GridBagConstraints gbc = new GridBagConstraints();
        int gx = 0; // starting point for non-spacer stuff?
        int lmargin = 8;
        int rmargin = 6;
        
        gbc.gridx = gx;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(22, lmargin, 0, rmargin);
        panel.add(makeTextLabel("Play"), gbc);

        gbc.gridx = gx;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3, lmargin, 0, 0);
        playableTypeChooser = makePlayableTypeChooser();
        panel.add(playableTypeChooser, gbc);

        gbc.gridx = gx + 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 0, 0, rmargin);
        noteChooser = makeTextField();
        playableChooser = makeDropDown();
        playableChooser.setVisible(true);
        noteChooser.setVisible(false);
        panel.add(noteChooser, gbc);
        panel.add(playableChooser, gbc);
        playableChooser.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (!playableChooserUpdating && selected != null) {
                        Playable newPl = getCurrentPlayableSelection();
                        selected.setPlayable(newPl);
                        updateSelected(selected, true);
                    }
                }
            });
                    
        gbc.gridx = gx;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, lmargin, 0, rmargin);
        panel.add(makeTextLabel("Instrument"), gbc);

        gbc.gridx = gx;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        //gbc.weightx = .5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(3, lmargin, 0, rmargin);
        // TODO: make this a drop-down
        instrumentChooser = makeDropDown();
        panel.add(instrumentChooser, gbc);
        instrumentChooser.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String instrument = getCurrentInstrumentSelection();
                    selected.setInstrument(instrument);
                    int ch = orrery.getInstrumentChannel(instrument);
                    selected.setChannel(ch);
                    updateSelected(selected, true);
                }
            });

        gbc.gridy++;
        gbc.gridx = gx + 1;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        currentPreviewButton = makeImageButton("PreviewButton_small_up.gif", "PreviewButton_small_down.gif", "Preview");
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(8, lmargin, rmargin, 0);
        panel.add(currentPreviewButton, gbc);
        currentPreviewButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    previewCurrentPlayableInstrumentSelection();
                }
            });
        currentStopButton = makeImageButton("StopButton_small_up.gif", "StopButton_small_down.gif", "Stop preview in progress");
        currentStopButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    stopPreviewThread();
                }
            });
        currentStopButton.setVisible(false);
        panel.add(currentStopButton, gbc);

        gbc.gridx = gx;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        //gbc.weightx = 1.0;
        gbc.insets = new Insets(8, lmargin, 0, rmargin);
        playablePreviewText = makeTextArea(5, 10, backgroundColor, orrery.controlPanelValueBG);
        panel.add(playablePreviewText, gbc);

        return panel;
    }

    JPanel makeMutatorPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(orrery.controlPanelBG);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(orrery.controlPanelSubBorder, 1),
                                                         "Mutator"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(makeTextLabel("Type"), gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        mutatorTypeChooser = makeMutatorTypeChooser();
        panel.add(mutatorTypeChooser, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(makeTextLabel("Chance"), gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        mutatorChanceText = makeTextField();
        panel.add(mutatorChanceText, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(makeTextLabel("After"), gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        mutatorAfterText = makeTextField();
        panel.add(mutatorAfterText, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(makeTextLabel("Low"), gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        mutatorLowText = makeTextField();
        panel.add(mutatorLowText, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(makeTextLabel("High"), gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        mutatorHighText = makeTextField();
        panel.add(mutatorHighText, gbc);

        return panel;
    }

    private void updateMutatorPanel(Body selected) {
        if (!selected.is_mutator) {
            mutatorTypeChooser.setSelectedItem("");
            displayText(mutatorChanceText, "");
            displayText(mutatorAfterText, "");
            displayText(mutatorLowText, "");
            displayText(mutatorHighText, "");
            return;
        }
        int    chance = 0;
        int    after = selected.mutate_after;
        String type = "";
        double low = -1.;
        double high = -1.;
        if (selected.mutate_mass_type != Body.MUTATE_NONE &&
            selected.mutate_mass_chance > 0.) {
            if (selected.mutate_mass_type == Body.MUTATE_MASS_PERCENT) {
                type = "mass%";
            } else if (selected.mutate_mass_type == Body.MUTATE_MASS) {
                type = "mass";
            }
            chance = selected.mutate_mass_chance;
            low =    selected.mutate_mass_lo;
            high =   selected.mutate_mass_hi;
        } else if (selected.mutate_velocity_type != Body.MUTATE_NONE &&
                   selected.mutate_velocity_chance > 0.) {
            // NOTE: not implementing absolute velocity mutators here. 
            type = "velocity%";
            chance = selected.mutate_velocity_chance;
            low =    selected.mutate_velocity_lo;
            high =   selected.mutate_velocity_hi;
        } else if (selected.mutate_clone &&
                   selected.mutate_clone_chance > 0.) {
            type = "clone";
            chance = selected.mutate_clone_chance;
            low =    selected.mutate_clone_lo;
            high =   selected.mutate_clone_hi;
        } else if (selected.mutate_killa &&
                   selected.mutate_killa_chance > 0.) {
            type = "killa";
            chance = selected.mutate_killa_chance;
        } else if (selected.mutate_blackhole &&
                   selected.mutate_blackhole_chance > 0.) {
            type = "blackhole";
            chance = selected.mutate_blackhole_chance;
        }

        mutatorTypeChooser.setSelectedItem(type);
        displayText(mutatorChanceText, chance);
        if (after > 0) {
            displayText(mutatorAfterText, after);
        }
        if (low >= 0.) {
            displayText(mutatorLowText, low);
        } else {
            displayText(mutatorLowText, "");
        }
        if (high >= 0.) {
            displayText(mutatorHighText, high);
        } else {
            displayText(mutatorHighText, "");
        }
    }

    private void applySelectedMutatorEdits(Body selected) {
        String type = (String)mutatorTypeChooser.getSelectedItem();
        int    chance = extractInt(mutatorChanceText, "Mutator Chance", 0);
        int    after  = extractInt(mutatorAfterText,  "Mutate After", -1);
        double low    = extractDouble(mutatorLowText,    "Mutator Low value", 0.);
        double high   = extractDouble(mutatorHighText,   "Mutator High value", 0.);
        System.out.println(" APPLY MUTATOR. type: " + type + " chance: " + chance + " after: " + after + " low: " + low + " high: " + high);
        selected.mutate_mass_type = Body.MUTATE_NONE;
        selected.mutate_velocity_type = Body.MUTATE_NONE;
        selected.mutate_clone = false;
        selected.mutate_killa = false;
        selected.mutate_blackhole = false;
        
        if (type.equals("")) {
            selected.is_mutator = false;
            return;
        }
        selected.mutate_after = after;
        selected.is_mutator = true;
        if (type.equals("mass%")) {
            selected.mutate_mass_chance = chance;
            selected.mutate_mass_type = Body.MUTATE_MASS_PERCENT;
            selected.mutate_mass_lo = low;
            selected.mutate_mass_hi = high;
        } else if (type.equals("mass")) {
            selected.mutate_mass_chance = chance;
            selected.mutate_mass_type = Body.MUTATE_MASS;
            selected.mutate_mass_lo = low;
            selected.mutate_mass_hi = high;
        } else if (type.equals("velocity%")) {
            selected.mutate_velocity_chance = chance;
            selected.mutate_velocity_type = Body.MUTATE_VELOCITY_PERCENT;
            selected.mutate_velocity_lo = low;
            selected.mutate_velocity_hi = high;
        } else if (type.equals("clone")) {
            selected.mutate_clone = true;
            selected.mutate_clone_chance = chance;
            selected.mutate_clone_lo = low;
            selected.mutate_clone_hi = high;
            System.out.println("Applied a clone. chance: " + chance + " lo: " + low + " hi: " + high);
        } else if (type.equals("killa")) {
            selected.mutate_killa = true;
            selected.mutate_killa_chance = chance;
        } else if (type.equals("blackhole")) {
            selected.mutate_blackhole = true;
            selected.mutate_blackhole_chance = chance;
        } 
    }

    private JComboBox  makeMutatorTypeChooser() {
        JComboBox mtDrop = makeDropDown();
        mtDrop.addItem("");
        mtDrop.addItem("mass");
        mtDrop.addItem("mass%");
        mtDrop.addItem("velocity%");
        mtDrop.addItem("clone");
        mtDrop.addItem("killa");
        mtDrop.addItem("blackhole");
        return mtDrop;
    }

    /////////////////////////////////////////////////////////////////////////


    public void worldRead() {
        fillInstrumentsPanel();
        populatePlayableChooser(peSelectorType, peSelectorName, "melody", "__first__");
    }

    /**
     * override this to receive indication that a new world has started
     */
    public void dawnOfCreation() {
    }

    /**
     * override this to receive indication that a world has met its demise
     */
    // TODO: make this vetoable, in case of uncommitted edits. 
    public void endOfTheWorld() {
    }


    public void applyWorldBuilderEdits() {
        System.out.println("APPLY!!!11!!!11Eleven!");
        try {
            applyWorldMetadataEdits();
            applySelectedBodyEdits(selected);
            applyMelodyEdits();
            applyInstrumentEdits();
            // then redisplay, in case some values change others, e.g. when the user changes
            // the position using polar coordinates, the cartesian coordinates should update
            // to follow.
            updateSelected(selected, true);
        } catch (DataException ex) {
            displayError(ex);
        }
        orrery.worldBuilderDraw();
    }
    
    public void applySelectedBodyEdits(Body selected) throws DataException {
        if (selected == null) {
            return;
        }
        double mass = extractDouble(massText, "mass");
        selected.setMass(mass);
        applySelectedPosEdits(selected);
        if (! (selected instanceof Rock)) {
            applySelectedVEdits(selected);
        }
        applySelectedPlayableEdits(selected);
        applySelectedMutatorEdits(selected);
    }
    
    public void applySelectedPosEdits(Body selected) throws DataException {
        Vect offset = selected.posOffset;
        if (offset != null) {
            Vect pMinusOffset = selected.pos.minus(offset);
        }
            
        if (!emptyText(offsetXText) && !emptyText(offsetYText)) {
            double offsetX = extractDouble(offsetXText, "offsetX");
            double offsetY = extractDouble(offsetYText, "offsetY");
            offset = new Vect(offsetX, offsetY);
            selected.posOffset = offset;
        }
        Vect newP = null;
        if (posCoordTypeToggle.isSelected()) {
            double posX = extractDouble(posCoord1Text, "posX");
            double posY = extractDouble(posCoord2Text, "posY");
            newP = new Vect(posX, posY);
            selected.posSpecifiedPolar = false;  // TODO: make an accessor.
        } else {
            double posR = extractDouble(posCoord1Text, "posR");
            double posTheta = extractDouble(posCoord2Text, "posY");
            newP = Vect.createPolarDegrees(posR, posTheta);
            selected.posSpecifiedPolar = true;  // TODO: make an accessor. 
        }
        if (offset != null && newP != null) {
            newP.plusEquals(offset);
        }
        selected.moveto(newP);
    }

    public void applySelectedVEdits(Body selected) throws DataException {
        Vect newV = selected.v;
        if (vCoordTypeToggle.isSelected()) {
            double vR = extractDouble(vCoord1Text, "vR");
            double vTheta = extractDouble(vCoord2Text, "vTheta");
            newV = Vect.createPolarDegrees(vR, vTheta);
            selected.vSpecifiedPolar = true;  // TODO: make an accessor. 
        } else {
            double vX = extractDouble(vCoord1Text, "vX");
            double vY = extractDouble(vCoord1Text, "vY");
            // Note: take into account offset.
            newV = new Vect(vX, vY);
            selected.vSpecifiedPolar = false;  // TODO: make an accessor.
        }
        System.out.println("apply v. newV: " + newV);
        selected.setV(newV);
    }

    public void applySelectedPlayableEdits(Body selected) throws DataException {
        Playable newPl = getCurrentPlayableSelection();
        selected.setPlayable(newPl);
        String instrument =  getCurrentInstrumentSelection();
        selected.setInstrument(instrument);
        int ch = orrery.getInstrumentChannel(instrument);
        selected.setChannel(ch);
    }

    private void displayError(DataException ex) {
        System.out.println("WorldBuilder caught exception extracting data field " + ex.getField());
        // TODO: Put the errant field name on the screen. 
    }
        
    public void applyMelodyEdits() throws DataException {
    }
    public void applyInstrumentEdits() throws DataException {
        int numChannels = 16;
        System.out.println("Apply Instrument Edits!");
        for(int i=0; i < numChannels; i++) {
            String name = channelNames[i].getText();
            if (name != null && !name.equals("")) {
                String bankStr = channelBanks[i].getText();
                String programStr = channelPrograms[i].getText();
                try {
                    int bank = Integer.parseInt(bankStr);
                    int program = Integer.parseInt(programStr);
                    orrery.setChannelInfo(i, name, bank, program);
                    System.out.println("  ch: " + i + " " + name + " " + bank + " " + program);
                } catch (NumberFormatException ex) {
                    // TODO: alert.
                    System.out.println("Exception parsing instrument " + i + ": " + ex);
                }
            }
        }
        // TODO: check if anything changed here
        orrery.reloadPatches();

    }

    public void applyWorldMetadataEdits() throws DataException {
    }


    long mousePressedTime = 0;
    long dragWaitDelay = 200;
    int ndrags = 0;
    public void setMousePressedTime(long pt) {
        mousePressedTime = pt;
        ndrags = 0;
    }
    
    public void mouseDragged(double mx, double my) {
        // TODO: don't drag if to close to the time the mouse was pressed.
        long now = System.currentTimeMillis();
        long delay = mousePressedTime == 0? 0 : now - mousePressedTime;
        //long delay = 2000l;
        if (selected != null && delay > dragWaitDelay) {
            ndrags++;
            selected.moveto(mx, my);
            if (ndrags % 10 == 0) {
                //updateSelected(selected, false);
            }
            orrery.worldBuilderDraw();
        }
    }

    public void altMousePressed(double mx, double my) {
    }

    public void metaMousePressed(double mx, double my) {
        // Meta is for moving bodies around. 
    }

    public void controlAltMousePressed(double mx, double my) {
        // TODO: toggle to choose whether creating rock or body
        if (selected != null) {
            addClone(selected, mx, my);
            orrery.worldBuilderDraw();
        }
    }

    public void controlMousePressed(double mx, double my) {

    }

    public void shiftMousePressed(double mx, double my) {

    }


    ///////////////////////////////////////////
    /// Previewing instruments & playables. ///
    ///////////////////////////////////////////

    private void previewCurrentPlayableInstrumentSelection() {
        Playable playable = getCurrentPlayableSelection();
        if (playable != null) {
            String instrument = getCurrentInstrumentSelection();
            if (instrument != null) {
                int ch = orrery.getInstrumentChannel(instrument);
                if (ch != -1) {
                    startPreviewThread(playable, ch, currentPreviewButton, currentStopButton);
                    //orrery.preview(playable, ch);
                }
            }
        }
    }

    protected PreviewThread previewThread = null;
    private void startPreviewThread(Playable playable, int channel,
                                       JButton previewButton, JButton stopButton ) {
        previewThread = new PreviewThread(orrery);
        previewThread.setPlayable(playable, channel);
        previewThread.setButtons(previewButton, stopButton);
        previewThread.start();
        Thread.yield();
    }

    private void stopPreviewThread() {
        if (previewThread != null) {
            previewThread.halt();
            previewThread = null;
        }
    }

    class PreviewThread extends Thread {
        private JButton previewButton = null;
        private JButton stopButton = null;
        private Orrery orrery;
        private Playable playable = null;
        private int channel = 0;
        
        public PreviewThread(Orrery orrery) {
            this.orrery = orrery;
        }
        public void setButtons(JButton previewButton, JButton stopButton) {
            this.previewButton = previewButton;
            this.stopButton = stopButton;
        }
        public void setPlayable(Playable playable, int channel) {
            System.out.println("WB: PreviewThread.setPlayable: " + playable+ " playable.getName(): " + playable.getName());
            this.playable = playable;
            this.channel = channel;
        }

        public synchronized void run() {
            stopButton.setVisible(true);
            previewButton.setVisible(false);
            preview(this.playable, this.channel);
            previewButton.setVisible(true);
            stopButton.setVisible(false);
        }

        public void halt() {
            stopPreview();
        }

        public void preview(Playable playable, int channel) {
            preview(playable, channel, 200, 700);
        }
        
        public boolean previewing = false;
        
        public void preview(Playable playable, int channel, int durationLow, int durationHi) {
            if (playable instanceof Note) {
                orrery.playNote((Note)playable, channel, 500);
            } else {
                previewing = true;
                playable.reset();
                while(previewing && !playable.atEnd()) {
                    int durationMS = orrery.randomRange(durationLow, durationHi);
                    Note note = playable.nextNote(null, orrery);
                    showPlayableCurrentState(playable);

                    Thread.yield();
                    System.out.println("    playing note: " + note + " dur: " + durationMS);
                    orrery.playNote(note, channel, durationMS);
                    System.out.println("      played note. previewing=" + previewing);
                }
                previewing = false;
                orrery.midiAllOff();
            }
        }
        
        public void stopPreview() {
            previewing = false;
        }

    }

    //
    // from NoteListener interface
    //
    // (Perhaps this should be sent off to a different thread?)
    public void notePlayed(Body body, Playable pl, Note note) {
        //System.out.println("WB:note played: " + note.getName() + " body: " + body + " selected: " + selected);
        if (showingPhysicsMode) {
            return;
        }

        if (body != null && body == selected) {
            //System.out.println("   playing note (" + note.getName() + " from selected!!!!!!!!!!!!!!!!!");
            showPlayableCurrentState(pl);
        }
    }

    private Playable getCurrentPlayableSelection() {
        String playableType = (String)playableTypeChooser.getSelectedItem();
        if (playableType == null || playableType.equals("")) {
            return null;
        }
        if (playableType.equals("note")) {
            String noteName = noteChooser.getText();
            if (noteName == null || noteName.trim().equals("")) {
                return null;
            }
            return new Note(noteName);
        } else if (playableType.equals("melody")) {
            String melodyName = (String)playableChooser.getSelectedItem();
            if (melodyName == null || melodyName.equals("")) {
                return null;
            }
            return orrery.getMelody(melodyName);
        } else if (playableType.equals("sequence")) {
            String sequenceName = (String)playableChooser.getSelectedItem();
            if (sequenceName == null || sequenceName.equals("")) {
                return null;
            }
            return orrery.getSequence(sequenceName);
        } else {
            return null;
        }
    }

    private String getCurrentInstrumentSelection() {
        return (String)instrumentChooser.getSelectedItem();
    }

    public void setMode(String mode, boolean val) {
    }

}
