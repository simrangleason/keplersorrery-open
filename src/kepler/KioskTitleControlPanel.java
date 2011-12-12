/***************************************************
 * Copyright 2007 by Simran Gleason                *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class KioskTitleControlPanel extends ControlPanel {
    private JLabel titleLabel = null;
    public KioskTitleControlPanel(Kepler kepler) {
        super(kepler);
        setPanel(makePanel(kepler));
    }

    public JPanel makePanel(Kepler kepler) {
        JPanel panel = new JPanel();
        panel.setBackground(orrery.controlPanelBG);
        panel.setLayout(new GridBagLayout());
        panel.setSize(new Dimension(281, 293));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(15, 10, 3, 10);

        panel.add(makeImageLabel("KeplersKioskTitle.gif", ""), gbc);

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
}
