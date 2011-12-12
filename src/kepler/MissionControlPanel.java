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

public class MissionControlPanel extends ControlPanel {
    private JSlider gravitySlider = null;
    private JLabel gravityValue = null;
    private JLabel gravityHeaderLabel = null;
    private JLabel gravityLabel = null;

    private JSlider repulsionSlider = null;
    private JLabel repulsionValue = null;
    private JLabel repulsionLabel = null;

    private JSlider frictionSlider = null;
    //private JTextField frictionValue = null;
    private JLabel frictionValue = null;
    private JLabel frictionLabel = null;
    
    private JToggleButton trailsCheck = null;
    private JToggleButton forceFieldCheck = null;
    private JToggleButton bodyForceCheck = null;
    private JToggleButton velocitiesCheck = null;

    private JPanel  bottomControls;
    private JButton selectPrevButton;
    private JButton selectNextButton;
    private JButton addRockButton;
    private JButton addBodyButton;
    private JButton deleteSelectedButton;
    
    Color backgroundColor;

    public MissionControlPanel(Kepler kepler, Color backgroundColor) {
        super(kepler);
        this.backgroundColor = backgroundColor;
        if (backgroundColor == null && kepler.kioskMode()) {
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
        //panel.setMinimumSize(new Dimension(256, 300));
        
        panel.setBorder(new MatteBorder(0, 2, 2, 2, orrery.controlPanelBorder));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.;
        gbc.weighty = 1.;
        gbc.gridheight = 10;
        JLabel vspacer = makeImageLabel("vspacer400.gif", "");
        vspacer.setBackground(backgroundColor);
        //vspacer.setBorder(new MatteBorder(1, 1, 1, 1, Color.RED));
        panel.add(vspacer, gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.weightx = 1.;
        gbc.weighty = 0.;
        gbc.insets = new Insets(0, 0, 3, 0);
        JLabel hspacer = makeImageLabel("spacer256.gif", "");
        hspacer.setBackground(backgroundColor);
        panel.add(hspacer, gbc);

        int gx = 0;
        int gy = 1;
        // gravity header label
        GridBagConstraints gridBagConstraintsGH = new GridBagConstraints();
        gridBagConstraintsGH.fill = GridBagConstraints.VERTICAL;
        gridBagConstraintsGH.gridwidth = 2;
        gridBagConstraintsGH.gridx = gx + 0;
        gridBagConstraintsGH.gridy = gy + 0;
        gridBagConstraintsGH.weightx = 0.0;
        gridBagConstraintsGH.insets = new Insets(10, 0, 0, 0);

        // gravity (attraction) label
        GridBagConstraints gridBagConstraintsGL = new GridBagConstraints();
        gridBagConstraintsGL.fill = GridBagConstraints.VERTICAL;
        gridBagConstraintsGL.gridx = gx;
        gridBagConstraintsGL.gridy = gy + 1;
        gridBagConstraintsGL.weightx = 0.0;
        gridBagConstraintsGL.insets = new Insets(0, 0, 0, 0);
        // gravity slider
        GridBagConstraints gridBagConstraintsGS = new GridBagConstraints();
        gridBagConstraintsGS.insets = new Insets(0, 0, 0, 0);
        gridBagConstraintsGS.gridx = gx;
        gridBagConstraintsGS.gridy = gy + 2;
        gridBagConstraintsGS.weighty = 0.5;
        // gravity value
        GridBagConstraints gridBagConstraintsGV = new GridBagConstraints();
        gridBagConstraintsGV.fill = GridBagConstraints.VERTICAL;
        gridBagConstraintsGV.gridx = gx;
        gridBagConstraintsGV.gridy = gy + 3;
        gridBagConstraintsGV.weightx = 0.0;
        gridBagConstraintsGV.insets = new Insets(0, 0, 0, 0);

        panel.add(getGravityHeaderLabel(), gridBagConstraintsGH);
        panel.add(getGravityLabel(),       gridBagConstraintsGL);
        JLabel sliderVSpacer = makeImageLabel("vspacer200.gif", "");
        sliderVSpacer.setBackground(backgroundColor);
        //sliderVSpacer.setBorder(new MatteBorder(1, 1, 1, 1, Color.BLUE));
        panel.add(sliderVSpacer,           gridBagConstraintsGS);
        panel.add(getGravitySlider(),      gridBagConstraintsGS);
        panel.add(getGravityValue(),       gridBagConstraintsGV);

        // Repulsion slider. 
        // REPULSION label
        GridBagConstraints gridBagConstraintsRL = new GridBagConstraints();
        gridBagConstraintsRL.fill = GridBagConstraints.VERTICAL;
        gridBagConstraintsRL.gridx = gx + 1;
        gridBagConstraintsRL.gridy = gy + 1;
        gridBagConstraintsRL.weightx = 0.0;
        gridBagConstraintsRL.insets = new Insets(0, 0, 0, 0);
        // REPULSION slider
        GridBagConstraints gridBagConstraintsRS = new GridBagConstraints();
        gridBagConstraintsRS.insets = new Insets(0, 0, 0, 0);
        gridBagConstraintsRS.gridx = gx + 1;
        gridBagConstraintsRS.gridy = gy + 2;
        // REPULSION VALUE
        GridBagConstraints gridBagConstraintsRV = new GridBagConstraints();
        gridBagConstraintsRV.fill = GridBagConstraints.VERTICAL;
        gridBagConstraintsRV.gridx = gx + 1;
        gridBagConstraintsRV.gridy = gy + 3;
        gridBagConstraintsRV.weightx = 0.0;
        gridBagConstraintsRV.insets = new Insets(0, 0, 0, 0);

        panel.add(getRepulsionLabel(), gridBagConstraintsRL);
        panel.add(getRepulsionSlider(), gridBagConstraintsRS);
        panel.add(getRepulsionValue(), gridBagConstraintsRV);


        // Friction slider. 
        // label
        GridBagConstraints gridBagConstraintsFL = new GridBagConstraints();
        gridBagConstraintsFL.fill = GridBagConstraints.VERTICAL;
        gridBagConstraintsFL.gridx = gx + 2;
        gridBagConstraintsFL.gridy = gy + 1;
        gridBagConstraintsFL.weightx = 0.0;
        gridBagConstraintsFL.insets = new Insets(0, 0, 0, 0);
        // friction slider
        GridBagConstraints gridBagConstraintsFS = new GridBagConstraints();
        gridBagConstraintsFS.insets = new Insets(0, 0, 0, 0);
        gridBagConstraintsFS.gridx = gx + 2;
        gridBagConstraintsFS.gridy = gy + 2;
        // friction value
        GridBagConstraints gridBagConstraintsFV = new GridBagConstraints();
        gridBagConstraintsFV.fill = GridBagConstraints.VERTICAL;
        gridBagConstraintsFV.gridx = gx + 2;
        gridBagConstraintsFV.gridy = gy + 3;
        gridBagConstraintsFV.weightx = 0.0;
        gridBagConstraintsFV.anchor = GridBagConstraints.CENTER;
        gridBagConstraintsFV.insets = new Insets(0, 0, 0, 0);

        panel.add(getFrictionLabel(), gridBagConstraintsFL);
        panel.add(getFrictionSlider(), gridBagConstraintsFS);
        panel.add(getFrictionValue(), gridBagConstraintsFV);


        // trails
        GridBagConstraints gridBagConstraintsTrails = new GridBagConstraints();
        gridBagConstraintsTrails.gridx = gx + 0;
        gridBagConstraintsTrails.gridy = gy + 4;
        gridBagConstraintsTrails.gridwidth=3;
        gridBagConstraintsTrails.insets = new Insets(8, 16, 0, 0);
        gridBagConstraintsTrails.anchor = GridBagConstraints.WEST;
        // force field
        GridBagConstraints gridBagConstraintsFF = new GridBagConstraints();
        gridBagConstraintsFF.gridx = gx + 0;
        gridBagConstraintsFF.gridy = gy + 5;
        gridBagConstraintsFF.gridwidth=3;
        gridBagConstraintsFF.insets = new Insets(0, 16, 0, 0);
        gridBagConstraintsFF.anchor = GridBagConstraints.WEST;
        // body forces
        GridBagConstraints gridBagConstraintsBF = new GridBagConstraints();
        gridBagConstraintsBF.gridx = gx + 0;
        gridBagConstraintsBF.gridy = gy + 6;
        gridBagConstraintsBF.gridwidth=3;
        gridBagConstraintsBF.insets = new Insets(0, 16, 0, 0);
        gridBagConstraintsBF.anchor = GridBagConstraints.WEST;
        // velocities
        GridBagConstraints gridBagConstraintsV = new GridBagConstraints();
        gridBagConstraintsV.gridx = gx + 0;
        gridBagConstraintsV.gridy = gy + 7;
        gridBagConstraintsV.gridwidth=3;
        gridBagConstraintsV.insets = new Insets(0, 16, 8, 0);
        gridBagConstraintsV.anchor = GridBagConstraints.WEST;


        panel.add(getTrailsCheck(), gridBagConstraintsTrails);
        panel.add(getForceFieldCheck(), gridBagConstraintsFF);
        panel.add(getBodyForceCheck(), gridBagConstraintsBF);
        panel.add(getVelocitiesCheck(), gridBagConstraintsV);

        /*
        if (!kepler.kioskMode()) {
            // bottomControls
            BottomControlsPanel bcp = new BottomControlsPanel(kepler);
            GridBagConstraints gridBagConstraintsBC = new GridBagConstraints();
            gridBagConstraintsBC.gridx = gx + 0;
            gridBagConstraintsBC.gridy = gy + 8;
            gridBagConstraintsBC.gridwidth = 4;
            gridBagConstraintsBC.weightx = 1.0;
            gridBagConstraintsBC.insets = new Insets(8, 8, 0, 8);
            //gridBagConstraintsBC.anchor = GridBagConstraints.EAST;
            gridBagConstraintsBC.fill = GridBagConstraints.HORIZONTAL;
            panel.add(bcp.getPanel(), gridBagConstraintsBC);
        }
        */

        if (kepler.kioskMode()) {
            gbc.gridx = gx + 0;
            gbc.gridy = gy + 7;
            gbc.gridwidth = 4;
            gbc.weightx = 0.;
            gbc.weighty = 1.0;
            gbc.insets = new Insets(0, 0, 0, 0);

            panel.add(makeTextLabel(""), gbc);

            gbc.gridy++;
            gbc.gridx = gx + 0;
            gbc.gridwidth = 4;
            gbc.weightx = 1.0;
            gbc.weighty = 0.;
            panel.add(makeBottomControls_kiosk(backgroundColor), gbc);
        }            

        return panel;
    }

    private JPanel makeBottomControls_kiosk(Color backgroundColor) {
        bottomControls = new JPanel();
        bottomControls.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 2, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        
        //bottomControls.setBorder(new MatteBorder(0, 0, 0, 0, orrery.controlPanelBorder));
        bottomControls.setBackground(backgroundColor);
        
        selectPrevButton = makeImageButton("SelectPrevButton_up.gif",
                                           "SelectPrevButton_down.gif",
                                           "SelectPrevButton_disabled.gif",
                                           "Select Prev");
        bottomControls.add(selectPrevButton, gbc);
        selectPrevButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    orrery.resetPlayTimer();
                    orrery.getWorldBuilder().selectPrevBodyOrRock();
                }
            });

        addBodyButton = makeImageButton("NewBody_up.gif",
                                        "NewBody_down.gif",
                                        "NewBody_disabled.gif",
                                        "New Body");
        gbc.gridx ++;
        bottomControls.add(addBodyButton, gbc);
        addBodyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    orrery.getWorldBuilder().addBody();
                }
            });

        addRockButton = makeImageButton("NewRock_up.gif",
                                        "NewRock_down.gif",
                                        "NewRock_disabled.gif",
                                        "New Rock");
        gbc.gridx ++;
        bottomControls.add(addRockButton, gbc);
        addRockButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    orrery.getWorldBuilder().addRock();
                }
            });

        deleteSelectedButton = makeImageButton("deleteSelected_up.gif",
                                               "deleteSelected_down.gif",
                                               "deleteSelected_disabled.gif",
                                        "Delete Selected Rock or Body");
        gbc.gridx ++;
        bottomControls.add(deleteSelectedButton, gbc);
        deleteSelectedButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    orrery.getWorldBuilder().deleteSelected();
                }
            });

        selectNextButton = makeImageButton("SelectNextButton_up.gif",
                                           "SelectNextButton_down.gif",
                                           "SelectNextButton_disabled.gif",
                                           "Select Next");
        gbc.gridx ++;
        bottomControls.add(selectNextButton, gbc);
        selectNextButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    orrery.resetPlayTimer();
                    orrery.getWorldBuilder().selectNextBodyOrRock();
                }
            });

        return bottomControls;
    }

 
    /**
     * This method initializes gravitySlider    
     *  
     * @return javax.swing.JSlider  
     */
    private JSlider getGravitySlider() {
        if (gravitySlider == null) {
            gravitySlider = new JSlider();
            gravitySlider.setName("gravitySlider");
            gravitySlider.setPreferredSize(new Dimension(30, 150));
            gravitySlider.setOrientation(JSlider.VERTICAL);
            gravitySlider.setBackground(backgroundColor);
            gravitySlider.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                    orrery.resetPlayTimer();
                    JSlider source = (JSlider)e.getSource();
                    int rawValue = (int)source.getValue();
                    double cookedValue = cookFactorValue(rawValue, 50, 11.);
                    //System.out.println("GravSlider. stateChanged() raw: " + rawValue + " cooked: " + cookedValue); 
                    if (!source.getValueIsAdjusting()) {
                        orrery.setGravityFactorBody(cookedValue);
                        orrery.setGravityFactorRock(cookedValue);
                    } else {
                        String displayable = "" + cookedValue;
                        if (displayable.length() > 5) {
                            displayable = displayable.substring(0, 5);
                        }
                        gravityValue.setText(displayable);
                        //System.out.println("r:" + rawValue + " | c:" + cookedValue );
                    }
                    
                }
            });
        }
        return gravitySlider;
    }

    /**
     * This method initializes gravityValue 
     *  
     * @return javax.swing.JLabel   
     */
    private JLabel getGravityValue() {
        if (gravityValue == null) {
            gravityValue = new JLabel("1.00");
            gravityValue.setForeground(orrery.controlPanelValueFG);
            gravityValue.setName("gravityValue");
        }
        return gravityValue;
    }

    /**
     * This method initializes gravityLabel 
     *  
     * @return javax.swing.JLabel   
     */
    private JLabel getGravityHeaderLabel() {
        if (gravityHeaderLabel == null) {
            gravityHeaderLabel = new JLabel(Kepler.createImageIcon("gravity_header.gif",
                                                                   "gravity attracts at the inverse square law"));
        }
        return gravityHeaderLabel;
    }

    /**
     * This method initializes gravityLabel 
     *  
     * @return javax.swing.JLabel   
     */
    private JLabel getGravityLabel() {
        if (gravityLabel == null) {
            gravityLabel = new JLabel(Kepler.createImageIcon("gravity_attract.gif",
                                                             "gravity attracts at the inverse square law"));
            gravityLabel.setBackground(orrery.titleColor);
            //gravityLabel.setMargin(new Insets(0, 0, 0, 0));
            gravityLabel.setBorder(new EmptyBorder(0, 0, 0, 0));

        }
        return gravityLabel;
    }
    
    /**
     * This method initializes repulsionSlider    
     *  
     * @return javax.swing.JSlider  
     */
    private JSlider getRepulsionSlider() {
        if (repulsionSlider == null) {
            repulsionSlider = new JSlider();
            repulsionSlider.setName("repulsionSlider");
            repulsionSlider.setPreferredSize(new Dimension(30, 150));
            repulsionSlider.setOrientation(JSlider.VERTICAL);
            repulsionSlider.setBackground(backgroundColor);
            repulsionSlider.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                    orrery.resetPlayTimer();
                    JSlider source = (JSlider)e.getSource();
                    int rawValue = (int)source.getValue();
                    double cookedValue = cookFactorValue(rawValue, 50, 11.);
                    if (!source.getValueIsAdjusting()) {
                        orrery.setRepelFactorBody(cookedValue);
                        orrery.setRepelFactorRock(cookedValue);
                    } else {
                        String displayable = "" + cookedValue;
                        if (displayable.length() > 5) {
                            displayable = displayable.substring(0, 5);
                        }
                        repulsionValue.setText(displayable);
                        System.out.println("r:" + rawValue + " | c:" + cookedValue );
                    }
                    
                }
            });
        }
        return repulsionSlider;
    }

    /**
     * This method initializes repulsionValue 
     *  
     * @return javax.swing.JLabel   
     */
    private JLabel getRepulsionValue() {
        if (repulsionValue == null) {
            repulsionValue = new JLabel("1.00");
            repulsionValue.setForeground(orrery.controlPanelValueFG);
            repulsionValue.setName("repulsionValue");
        }
        return repulsionValue;
    }

    /**
     * This method initializes repulsionLabel 
     *  
     * @return javax.swing.JLabel   
     */
    private JLabel getRepulsionLabel() {
        if (repulsionLabel == null) {
            repulsionLabel = new JLabel(Kepler.createImageIcon("gravity_repel.gif",
                                                               "...and repels at the inverse cube law"));
            //gravityLabel.setMargin(new Insets(0, 0, 0, 0));
            repulsionLabel.setBorder(new EmptyBorder(0, 0, 0, 0));

        }
        return repulsionLabel;
    }
    

    private double frictionLowRange = 0.;
    private double frictionHighRange = 0.2;
    /**
     * This method initializes frictionSlider    
     *  
     * @return javax.swing.JSlider  
     */
    private JSlider getFrictionSlider() {
        if (frictionSlider == null) {
            frictionSlider = new JSlider();
            frictionSlider.setName("frictionSlider");
            frictionSlider.setPreferredSize(new Dimension(30, 150));
            frictionSlider.setOrientation(JSlider.VERTICAL);
            frictionSlider.setBackground(backgroundColor);
            frictionSlider.setForeground(orrery.controlPanelFG);
            frictionSlider.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent e) {
                    orrery.resetPlayTimer();
                    JSlider source = (JSlider)e.getSource();
                    int rawValue = (int)source.getValue();
                    double cookedValue = cookLinearValue(rawValue, frictionLowRange, frictionHighRange);
                    //System.out.println("FrictionSlider. stateChanged() raw: " + rawValue + " cooked: " + cookedValue);
                    cookedValue = Math.min(1.0, cookedValue);
                    if (!source.getValueIsAdjusting()) {
                        //System.out.println("Setting friction on orrery: " + (1.0 - cookedValue));
                        orrery.setFriction(1.0 - cookedValue);
                    } else {
                        String displayable = "" + cookedValue;
                        if (displayable.length() > 5) {
                            displayable = displayable.substring(0, 5);
                        }
                        frictionValue.setText(displayable );
                        //System.out.println("r:" + rawValue + " | c:" + cookedValue );
                    }
                    
                }
            });
        }
        return frictionSlider;
    }

    /**
     * This method initializes frictionValue 
     *  
     * @return javax.swing.JLabel   
     */
    private JLabel getFrictionValue() {
        if (frictionValue == null) {
            frictionValue = new JLabel("0.990");
            frictionValue.setForeground(orrery.controlPanelValueFG);
            //frictionValue.setName("frictionValue");
        }
        return frictionValue;
    }

    /**
     * This method initializes frictionLabel 
     *  
     * @return javax.swing.JLabel   
     */
    private JLabel getFrictionLabel() {
        if (frictionLabel == null) {
            frictionLabel = new JLabel(Kepler.createImageIcon("friction.gif", "'Aether friction'"));
        }
        return frictionLabel;
    }
    

    public void setGravityFactor(double val) {
        System.out.println("MISSIONCONTROL: setGrasvFactor("+ val + ") uncoook: " + uncookFactorValue(val, 50, 11.));
        String displayable = "" + val;
        if (displayable.length() > 5) {
            displayable = displayable.substring(0,5);
        }
        gravityValue.setText(displayable);
        gravitySlider.setValue(uncookFactorValue(val, 50, 11.));
    }
    
    public void setRepulsionFactor(double val) {
        System.out.println("MISSIONCONTROL: setRepelFactor("+ val + ") uncook: " + uncookFactorValue(val, 50, 11.));
        String displayable = "" + val;
        if (displayable.length() > 5) {
            displayable = displayable.substring(0,5);
        }
        repulsionValue.setText(displayable);
        repulsionSlider.setValue(uncookFactorValue(val, 50, 11.));
    }

    // friction is in the range of 1.0 (no friction) to 0 (infinite friction)
    //  reasonable range: .8 - 1.0
    //  For displaying it, we use 0. (no friction) to .2 (pretty high friction)
    public void setFriction(double val) {
        //System.out.println("MISSIONCONTROL: setFriction("+ val + ") uncook: " +
        //                   uncookLinearValue(val, frictionLowRange, frictionHighRange));
        double displayVal = 1.0 - val;
        String displayable = "" + displayVal;
        if (displayable.length() > 5) {
            displayable = displayable.substring(0, 5);
        }
        frictionValue.setText(displayable);
        frictionSlider.setValue(uncookLinearValue(displayVal, frictionLowRange, frictionHighRange));
    }

    public void worldRead() {
        setGravityFactor(orrery.getGravityFactorBody());
        setRepulsionFactor(orrery.getRepelFactorBody());
        setFriction(orrery.getFriction());
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
     * This method initializes trailsCheck      
     *  
     * @return javax.swing.JCheckBox    
     */
    private JToggleButton getTrailsCheck() {
        if (trailsCheck == null) {
            String buttonImage = "trails_off.gif";
            String buttonPressedImage = "trails_on.gif";
            trailsCheck = new JToggleButton(Kepler.createImageIcon(buttonImage,
                                                                   "Turn on trails"));
            ImageIcon pressedIcon = Kepler.createImageIcon(buttonPressedImage,
                                                           "Turn off trails");
            trailsCheck.setBorderPainted(false);
            trailsCheck.setPressedIcon(pressedIcon);
            trailsCheck.setSelectedIcon(pressedIcon);
            trailsCheck.setMargin(new Insets(0, 0, 0, 0));
            trailsCheck.setContentAreaFilled(false);

            //trailsCheck.setText("Trails");
            //trailsCheck.setForeground(orrery.controlPanelFG);
            trailsCheck.setBackground(backgroundColor);
            trailsCheck.addChangeListener(new javax.swing.event.ChangeListener() {
                    public void stateChanged(javax.swing.event.ChangeEvent e) {
                        orrery.resetPlayTimer();
                        kepler.setTrails(trailsCheck.isSelected());
                    }
                });
        }
        return trailsCheck;
    }

    /**
     * This method initializes forceFieldCheck  
     *  
     * @return javax.swing.JCheckBox    
     */
    private JToggleButton getForceFieldCheck() {
        if (forceFieldCheck == null) {
            String buttonImage = "forcefield_off.gif";
            String buttonPressedImage = "forcefield_on.gif";
            forceFieldCheck = new JToggleButton(Kepler.createImageIcon(buttonImage,
                                                                       "Turn on force field"));
            ImageIcon pressedIcon = Kepler.createImageIcon(buttonPressedImage,
                                                           "Turn off force field");
            forceFieldCheck.setBorderPainted(false);
            forceFieldCheck.setPressedIcon(pressedIcon);
            forceFieldCheck.setSelectedIcon(pressedIcon);
            forceFieldCheck.setMargin(new Insets(0, 0, 0, 0));
            forceFieldCheck.setContentAreaFilled(false);

            forceFieldCheck.addChangeListener(new javax.swing.event.ChangeListener() {
                    public void stateChanged(javax.swing.event.ChangeEvent e) {
                        orrery.resetPlayTimer();
                        orrery.setShowForceField(forceFieldCheck.isSelected());
                    }
                });       
        }
        return forceFieldCheck;
    }

    /**
     * This method initializes bodyForceCheck   
     *  
     * @return javax.swing.JCheckBox    
     */
    private JToggleButton getBodyForceCheck() {
        if (bodyForceCheck == null) {
            String buttonImage = "bodyforces_off.gif";
            String buttonPressedImage = "bodyforces_on.gif";
            bodyForceCheck = new JToggleButton(Kepler.createImageIcon(buttonImage,
                                                                      "Turn on force field"));
            ImageIcon pressedIcon = Kepler.createImageIcon(buttonPressedImage,
                                                           "Turn off force field");
            bodyForceCheck.setBorderPainted(false);
            bodyForceCheck.setPressedIcon(pressedIcon);
            bodyForceCheck.setSelectedIcon(pressedIcon);
            bodyForceCheck.setMargin(new Insets(0, 0, 0, 0));
            bodyForceCheck.setContentAreaFilled(false);
            bodyForceCheck.addChangeListener(new javax.swing.event.ChangeListener() {
                    public void stateChanged(javax.swing.event.ChangeEvent e) {
                        orrery.resetPlayTimer();
                        orrery.setShowBodyForces(bodyForceCheck.isSelected());
                    }
                });      
        }
        return bodyForceCheck;
    }

    private JToggleButton getVelocitiesCheck() {
        if (velocitiesCheck == null) {
            String buttonImage = "velocities_off.gif";
            String buttonPressedImage = "velocities_on.gif";
            velocitiesCheck = new JToggleButton(Kepler.createImageIcon(buttonImage,
                                                                      "Turn on force field"));
            ImageIcon pressedIcon = Kepler.createImageIcon(buttonPressedImage,
                                                           "Turn off force field");
            velocitiesCheck.setBorderPainted(false);
            velocitiesCheck.setPressedIcon(pressedIcon);
            velocitiesCheck.setSelectedIcon(pressedIcon);
            velocitiesCheck.setMargin(new Insets(0, 0, 0, 0));
            velocitiesCheck.setContentAreaFilled(false);
            velocitiesCheck.addChangeListener(new javax.swing.event.ChangeListener() {
                    public void stateChanged(javax.swing.event.ChangeEvent e) {
                        orrery.resetPlayTimer();
                        orrery.setShowVelocities(velocitiesCheck.isSelected());
                    }
                });      
        }
        return velocitiesCheck;
    }

    public void setMode(String mode, boolean val) {
        if (mode.equals("pause")) {
            if (orrery.isWinkingOut()) {
                val = false;
            }
            hideBottomButtons(val);
            //enableBottomButtons(val);
        } else if (mode.equals("worldtransition")) {
            hideBottomButtons(!val);
            //enableBottomButtons(!val);
        }
    }

    private void hideBottomButtons(boolean val) {
        //System.out.println(" hide bottom(" + val + "). bottomControls: " + bottomControls);
        if (bottomControls != null) {
            bottomControls.setVisible(val);
        }
    }

    private void enableBottomButtons(boolean val) {
        if (selectPrevButton != null) {
            selectPrevButton.setEnabled(val);
        }
        if (selectNextButton != null) {
            selectNextButton.setEnabled(val);
        }
        if (addRockButton != null) {
            addRockButton.setEnabled(val);
        }
        if (addBodyButton != null) {
            addBodyButton.setEnabled(val);
        }
        if (deleteSelectedButton != null) {
            deleteSelectedButton.setEnabled(val);
        }
    }
} 
