package com.tool.gui.cs;

import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author Česnek Michal, UNIDATAZ s.r.o.
 */
public class ActionsComboBoxModel extends DefaultComboBoxModel<Actions>{

    public ActionsComboBoxModel() {
        super(Actions.values());
    }
    
}
