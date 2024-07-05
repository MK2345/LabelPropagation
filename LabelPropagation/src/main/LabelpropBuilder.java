
package org.fapra.LabelPropagation;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = StatisticsBuilder.class)
public class LabelpropBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return "Label Propagation";
    }

    @Override
    public Statistics getStatistics() {
        return new LabelPropagation();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return LabelPropagation.class;
    }
}
