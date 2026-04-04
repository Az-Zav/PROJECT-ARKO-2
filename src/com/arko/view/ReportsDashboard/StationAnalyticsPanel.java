package com.arko.view.ReportsDashboard;

import com.arko.view.ModernCard;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.util.ArrayList;

public class StationAnalyticsPanel extends ModernCard {

    private ChartPanel currentChartPanel;
    private boolean    isAdmin;

    public StationAnalyticsPanel(boolean isAdmin) {
        super(isAdmin
                ? "System-Wide Station Comparison"
                : "Station Performance: Boarding & Alighting");
        this.isAdmin = isAdmin;
    }

    /**
     * Called by ReportsController.refreshAllCharts() with fresh data.
     *
     * data    — two-element list from PassengerDAO.getBoardingAlightingData():
     *             index 0 = boarding rows:  String[]{"stationName", "count"}
     *             index 1 = alighting rows: String[]{"stationName", "count"}
     * isAdmin — true: grouped bar chart with one group per station
     *           false: two horizontal bars for the staff's single station
     */
    public void updateChart(ArrayList<ArrayList<String[]>> data, boolean isAdmin) {
        this.isAdmin = isAdmin;

        ArrayList<String[]> boardingRows  = data.get(0);
        ArrayList<String[]> alightingRows = data.get(1);

        // Step 1: Build dataset
        // Rows from both lists are merged into a single DefaultCategoryDataset.
        // Series names are "Boarding" and "Alighting".
        // Category (x-axis group) is the station name.
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        if (isAdmin) {
            // Admin: one group per station, two bars per group
            // We need all station names from both lists combined.
            // Use a LinkedHashSet to collect unique station names in order.
            java.util.LinkedHashSet<String> stationNames = new java.util.LinkedHashSet<>();
            for (String[] row : boardingRows)  stationNames.add(row[0]);
            for (String[] row : alightingRows) stationNames.add(row[0]);

            // Initialize all stations to 0 for both series
            for (String name : stationNames) {
                dataset.addValue(0, "Boarding",  name);
                dataset.addValue(0, "Alighting", name);
            }

            // Overlay boarding counts
            for (String[] row : boardingRows) {
                dataset.addValue(Double.parseDouble(row[1]), "Boarding", row[0]);
            }

            // Overlay alighting counts
            for (String[] row : alightingRows) {
                dataset.addValue(Double.parseDouble(row[1]), "Alighting", row[0]);
            }

        } else {
            // Staff: single station, two bars side by side
            // boardingRows and alightingRows each have at most one entry
            String stationName   = "";
            double boardingCount = 0;
            double alightingCount = 0;

            if (!boardingRows.isEmpty()) {
                stationName   = boardingRows.get(0)[0];
                boardingCount = Double.parseDouble(boardingRows.get(0)[1]);
            }
            if (!alightingRows.isEmpty()) {
                // Use boarding station name as the group key if alighting
                // returned a different name (shouldn't happen, but safe fallback)
                if (stationName.isEmpty()) stationName = alightingRows.get(0)[0];
                alightingCount = Double.parseDouble(alightingRows.get(0)[1]);
            }

            dataset.addValue(boardingCount,  "Boarding",  stationName);
            dataset.addValue(alightingCount, "Alighting", stationName);
        }

        // Step 2: Create the bar chart
        // Admin uses VERTICAL (grouped bars per station across x-axis).
        // Staff uses HORIZONTAL (two bars side by side for their station) —
        // visually cleaner for a single-station comparison of just two values.
        PlotOrientation orientation = isAdmin
                ? PlotOrientation.VERTICAL
                : PlotOrientation.HORIZONTAL;

        JFreeChart chart = ChartFactory.createBarChart(
                "",               // title — card header already labels it
                "",               // x-axis label
                "Passengers",     // y-axis label
                dataset,
                orientation,
                true,             // legend — needed to distinguish Boarding vs Alighting
                false,            // tooltips
                false             // urls
        );

        // Step 3: Minimal styling
        chart.setBackgroundPaint(Color.WHITE);
        chart.getPlot().setBackgroundPaint(Color.WHITE);

        // Step 4: Color the two series consistently across both views
        org.jfree.chart.renderer.category.BarRenderer renderer =
                (org.jfree.chart.renderer.category.BarRenderer)
                        ((org.jfree.chart.plot.CategoryPlot) chart.getPlot()).getRenderer();

        renderer.setSeriesPaint(0, new Color(0,  102, 204)); // Boarding  — blue
        renderer.setSeriesPaint(1, new Color(220, 80,  80)); // Alighting — red

        // Step 5: Swap the ChartPanel in container
        if (currentChartPanel != null) {
            container.remove(currentChartPanel);
        }
        currentChartPanel = new ChartPanel(chart);
        container.add(currentChartPanel, BorderLayout.CENTER);
        container.revalidate();
        container.repaint();
    }
}