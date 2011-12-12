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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class BottomControlsPanel extends ControlPanel {
    private JToggleButton resetToggle = null;
    private JButton stepButton = null;
    private JButton nextWorldButton = null;
    private JToggleButton pauseToggle = null;
    private JToggleButton muteToggle = null;
    private JToggleButton fullScreenToggle = null;

    private boolean includeFrameControls = true;
    Color backgroundColor;

    public BottomControlsPanel(Kepler kepler) {
        super(kepler);
        setPanel(makePanel(kepler));
    }

    public BottomControlsPanel(Kepler kepler, boolean includeFrameControls) {
        super(kepler);
        this.includeFrameControls = includeFrameControls;
        if (kepler.kioskMode()) {
            backgroundColor = orrery.controlPanelBG3;
        } else {
            backgroundColor = orrery.controlPanelBG;
        }
        setPanel(makePanel(kepler));
    }

    public JPanel makePanel(Kepler kepler) {
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.setLayout(new GridBagLayout());
        panel.setSize(new Dimension(256, 48));
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel bottomControls = new JPanel();
        bottomControls.setBorder(new MatteBorder(0, 0, 0, 0, orrery.controlPanelBorder));
        bottomControls.setBackground(orrery.controlPanelBG);
        
        // bottomControls
        gbc.gridx  = 0;
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 8, 0, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        
        panel.add(bottomControls, gbc);
        
        resetToggle = makeResetToggle();
        bottomControls.add(resetToggle);
        bottomControls.add(getPauseToggle());
        bottomControls.add(getStepButton());
        if (!kepler.kioskMode()) {
            bottomControls.add(getMuteToggle());
            if (includeFrameControls) {
                bottomControls.add(getFullScreenToggle());
            }
        }

        return panel;
    }

 
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
     * This method initializes pauseToggle       
     *  
     * @return javax.swing.JCheckBox    
     */
    private JToggleButton getPauseToggle() {
        if (pauseToggle == null) {
            String buttonImage = "pause.gif";
            String buttonPressedImage = "play.gif";
            String disabledImage = "play_pause_disabled.gif";
            pauseToggle = new JToggleButton(Kepler.createImageIcon(buttonImage, "Pause"));
            ImageIcon pressedIcon = Kepler.createImageIcon(buttonPressedImage, "Play");
            pauseToggle.setBorderPainted(false);
            pauseToggle.setPressedIcon(pressedIcon);
            pauseToggle.setSelectedIcon(pressedIcon);
            ImageIcon disabledIcon = Kepler.createImageIcon(disabledImage, "Play");
            pauseToggle.setDisabledIcon(disabledIcon);
            pauseToggle.setDisabledSelectedIcon(disabledIcon);
            pauseToggle.setMargin(new Insets(0, 0, 0, 0));
            pauseToggle.setContentAreaFilled(false);
            pauseToggle.addChangeListener(new javax.swing.event.ChangeListener() {
                    public void stateChanged(javax.swing.event.ChangeEvent e) {
                        if (!pauseToggle.isEnabled()) {
                            return;
                        }

                        orrery.resetPlayTimer();
                        boolean pausing = pauseToggle.isSelected();
                        if (orrery.isInitialConditionsMode()) {
                            if (pausing) {
                                // ignore pausing while in initial conditions mode
                            } else {
                                // restart from initial conditions mode
                                System.out.println("PAUSE button while in init cond mode. restarting...");
                                orrery.getWorldBuilder().setInitialConditionsMode(false);
                                resetToggle.setSelected(false);
                                // treat like a world restart.
                            }
                        } else {
                            orrery.setPaused(pauseToggle.isSelected());
                            kepler.setMode("pause", pauseToggle.isSelected());
                        }
                    }
                });      
        }
        return pauseToggle;
    }

    /**
     * This method initializes muteToggle        
     *  
     * @return javax.swing.JCheckBox    
     */
    private JToggleButton getMuteToggle() {
        if (muteToggle == null) {
            String buttonImage = "mute_off.gif";
            String buttonPressedImage = "mute_on.gif";
            muteToggle = new JToggleButton(Kepler.createImageIcon(buttonImage, "Turn on force field"));
            ImageIcon pressedIcon = Kepler.createImageIcon(buttonPressedImage, "Turn off force field");
            muteToggle.setBorderPainted(false);
            muteToggle.setPressedIcon(pressedIcon);
            muteToggle.setSelectedIcon(pressedIcon);
            muteToggle.setMargin(new Insets(0, 0, 0, 0));
            muteToggle.setContentAreaFilled(false);

            muteToggle.addChangeListener(new javax.swing.event.ChangeListener() {
                    public void stateChanged(javax.swing.event.ChangeEvent e) {
                        orrery.setMuted(muteToggle.isSelected());
                    }
                });     
        }
        return muteToggle;
    }

    /**
     * This method initializes fullScreenToggle  
     *  
     * @return javax.swing.JCheckBox    
     */
    private JToggleButton getFullScreenToggle() {
        if (fullScreenToggle == null) {
            String buttonImage = "fullscreen_off.gif";
            String buttonPressedImage = "fullscreen_on.gif";
            fullScreenToggle = new JToggleButton(Kepler.createImageIcon(buttonImage, "Turn on full screen"));
            ImageIcon pressedIcon = Kepler.createImageIcon(buttonPressedImage, "Turn off full screen");
            fullScreenToggle.setBorderPainted(false);
            fullScreenToggle.setPressedIcon(pressedIcon);
            fullScreenToggle.setSelectedIcon(pressedIcon);
            fullScreenToggle.setMargin(new Insets(0, 0, 0, 0));
            fullScreenToggle.setContentAreaFilled(false);
            fullScreenToggle.addChangeListener(new javax.swing.event.ChangeListener() {
                    public void stateChanged(javax.swing.event.ChangeEvent e) {
                        kepler.setFullScreen(fullScreenToggle.isSelected());
                    }
                });     
        }
        return fullScreenToggle;
    }

    private JToggleButton makeResetToggle() {
        final JToggleButton resetTog =
            makeImageToggleButton("ResetToggle_up.gif",
                                  "ResetToggle_down.gif",
                                  "ResetToggle_disabled.gif",
                                  "Reset world to initial conditions");
        resetTog.setSelected(false);
        resetTog.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Reset toggle pressed.");
                    boolean initialConditionsMode = resetTog.isSelected();
                    // TODO: this is for restarting the world when in initial conditions mode.
                    WorldBuilder wb = orrery.getWorldBuilder();
                    if (initialConditionsMode) {
                        orrery.setPaused(true);
                        wb.setInitialConditionsMode(true);
                        kepler.setMode("initialconditions", true);
                        kepler.setMode("pause", true);
                    } else {
                        wb.setInitialConditionsMode(false);
                        kepler.setMode("initialconditions", false);
                        kepler.setMode("pause", false);
                    }
                }
            });
        return resetTog;
    }


    private JButton getStepButton() {
        if (stepButton == null) {
            String buttonImage = "SingleStep.gif";
            String buttonPressedImage = "SingleStep_pressed.gif";
            String disabledImage = "SingleStep_disabled.gif";
            String tooltip = "Single Step";
            stepButton = new JButton(Kepler.createImageIcon(buttonImage, tooltip));
            ImageIcon pressedIcon = Kepler.createImageIcon(buttonPressedImage, tooltip);
            ImageIcon disabledIcon = Kepler.createImageIcon(disabledImage, tooltip);
            stepButton.setBorderPainted(false);
            stepButton.setPressedIcon(pressedIcon);
            stepButton.setSelectedIcon(pressedIcon);
            stepButton.setDisabledIcon(disabledIcon);
            stepButton.setMargin(new Insets(0, 0, 0, 0));
            stepButton.setContentAreaFilled(false);
            stepButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        orrery.resetPlayTimer();
                        if (orrery.isInitialConditionsMode()) {
                            WorldBuilder wb = orrery.getWorldBuilder();
                            wb.setInitialConditionsMode(false);
                            kepler.setMode("initialconditions", false);
                            kepler.setMode("pause", true);
                            orrery.pause();
                        }
                        orrery.singleStep();
                    }
                });     
        }
        return stepButton;
    }

    public void setMode(String mode, boolean val) {
        if (mode.equals("pause")) {
            pauseToggle.setSelected(val);
            if (stepButton != null) {
                //System.out.println("BottomControls.setMode(pause, " + val + ") stepButton.setEnabled: " + !val);
                stepButton.setEnabled(val);
            }
        } else if (mode.equals("initialconditions")) {
            stepButton.setEnabled(!val);
            resetToggle.setSelected(val);
        } else if (mode.equals("worldtransition")) {
            stepButton.setEnabled(!val);
            resetToggle.setEnabled(!val);
            resetToggle.setSelected(false);
            pauseToggle.setEnabled(false);
            pauseToggle.setSelected(false);
            pauseToggle.setEnabled(!val);
        }
    }
} 
