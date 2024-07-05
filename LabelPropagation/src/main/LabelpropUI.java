
package org.fapra.LabelPropagation;


import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = StatisticsUI.class)
public class LabelpropUI implements StatisticsUI {

    private LabelPropagation statistic;

    @Override
    public JPanel getSettingsPanel() {
        return null;
    }

    @Override
    public void setup(Statistics ststcs) {
        this.statistic = (LabelPropagation) ststcs;
    }

    @Override
    public void unsetup() {
        this.statistic = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return LabelPropagation.class;
    }

    @Override
    public String getValue() {        
        return statistic!=null ? statistic.getValue() : null;
    }

    @Override
    public String getDisplayName() {
        return "Label Propagation_C";
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 993;
    }

    @Override
    public String getShortDescription() {
        return null;
    }
}
