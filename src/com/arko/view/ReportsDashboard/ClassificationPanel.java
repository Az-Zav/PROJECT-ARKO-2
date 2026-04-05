package com.arko.view.ReportsDashboard;

import com.arko.view.ModernCard;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.RingPlot;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.util.ArrayList;

public class ClassificationPanel extends ModernCard {

    private ChartPanel currentChartPanel;

    public ClassificationPanel() {
        super("Passenger Classification");
    }

    /**
     * Called by ReportsController.refreshAllCharts() with fresh data.
     *
     * data — list of String[]{"classification", "count"} from PassengerDAO.
     *        The four expected keys are: Regular, Student, Senior, PWD.
     *        Missing keys (zero passengers) simply won't appear as slices.
     */
    public void updateChart(ArrayList<String[]> data) {

        // Step 1: Build the pie dataset from DB results.
        // DefaultPieDataset takes a key (slice label) and a numeric value.
        DefaultPieDataset dataset = new DefaultPieDataset();

        if (data.isEmpty()) {
            // No data — show a single placeholder slice so the chart
            // isn't blank, which looks broken rather than intentional
            dataset.setValue("No Data", 1);
        } else {
            for (String[] row : data) {
                String classification = row[0];
                double count          = Double.parseDouble(row[1]);
                dataset.setValue(classification, count);
            }
        }

        // Step 2: Create the pie chart.
        // JFreeChart does not have a built-in donut factory method —
        // we create a pie chart and set the interior gap via the plot
        // to achieve the donut appearance.
        JFreeChart chart = ChartFactory.createPieChart(
                "",       // title — card header already labels it
                dataset,
                true,     // legend — needed here so user knows which slice is which
                false,    // tooltips
                false     // urls
        );

        // Step 3: Convert pie to donut by setting a hole in the center.
        // RingPlot is JFreeChart's built-in donut — we cast the plot and
        // replace it with a RingPlot built from the same dataset.
        RingPlot ringPlot =
                new org.jfree.chart.plot.RingPlot(dataset);
        ringPlot.setSectionDepth(0.4);        // controls ring thickness (0=thin, 1=full pie)
        ringPlot.setSeparatorsVisible(false); // clean look — no divider lines between slices
        ringPlot.setBackgroundPaint(Color.WHITE);
        ringPlot.setOutlineVisible(false);

        // Adds the actual numeric count to the labels
        // {0} = Key (e.g., Regular), {1} = Value (the count), {2} = Percentage
        ringPlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})"));

        // Styling to make numbers readable
        ringPlot.setLabelBackgroundPaint(Color.WHITE);
        ringPlot.setLabelFont(new Font("Inter", Font.PLAIN, 11));

        // Step 4: Assign colors to the four classification slices.
        // Fixed colors so the legend is always consistent regardless of
        // which classifications actually have data in the current window.
        ringPlot.setSectionPaint("Regular", new Color(0,  102, 204));
        ringPlot.setSectionPaint("Student", new Color(0,  180, 100));
        ringPlot.setSectionPaint("Senior",  new Color(255, 165,  0));
        ringPlot.setSectionPaint("PWD",     new Color(220,  50, 50));
        ringPlot.setSectionPaint("No Data", new Color(220, 220, 220));

        // Step 5: Apply the ring plot to the chart
        JFreeChart donutChart = new JFreeChart(
                "",                                      // no title
                JFreeChart.DEFAULT_TITLE_FONT,
                ringPlot,
                true                                     // create legend
        );
        donutChart.setBackgroundPaint(Color.WHITE);

        // Step 6: Swap the ChartPanel in container
        if (currentChartPanel != null) {
            container.remove(currentChartPanel);
        }
        currentChartPanel = new ChartPanel(donutChart);
        container.add(currentChartPanel, BorderLayout.CENTER);
        container.revalidate();
        container.repaint();
    }
}