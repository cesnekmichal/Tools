package com.tool.gui.cs;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;

/**
 *
 * @author Česnek Michal, UNIDATAZ s.r.o.
 */
public class ActionsComboBox extends JComboBox<Actions>{

    public ActionsComboBox() {
        setModel(new ActionsComboBoxModel());
        setRenderer(new ActionsComboBoxRenderer());
        ComboBoxUtils.setWidthBasedOnRenderer(this);
        addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED){
                    Actions action = (Actions) e.getItem();
                    for (ActionsConsumer actionsConsumer : actionsConsumers) {
                        actionsConsumer.accept(action);
                    }
                }
            }
        });
        setSelectedIndex(-1);
    }
    
    List<ActionsConsumer> actionsConsumers = new ArrayList<>();
    public void addActionsConsumer(ActionsConsumer actionsConsumer) {
        actionsConsumers.add(actionsConsumer);
    }
    
    public void fireActionsConsumers(){
        for (ActionsConsumer actionsConsumer : actionsConsumers) {
            actionsConsumer.accept((Actions)getModel().getSelectedItem());
        }
    }

    @Override
    public Actions getSelectedItem() {
        Object o = super.getSelectedItem();
        return (o instanceof Actions) ? (Actions)o : null;
    }
    
}
