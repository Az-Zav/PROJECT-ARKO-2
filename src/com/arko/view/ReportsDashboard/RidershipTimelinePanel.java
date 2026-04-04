package com.arko.view.ReportsDashboard;

import com.arko.view.ModernCard;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

public class RidershipTimelinePanel extends ModernCard {

    // Holds a reference to the currently displayed ChartPanel so we can
    // remove it cleanly before adding the next one — per the coderanch
    // reference: remove the old handle, not container.removeAll()
    private ChartPanel currentChartPanel;

    public RidershipTimelinePanel() {
        super("Total Ridership Timeline");
        // Chart will be populated by updateChart() — nothing to build here
    }

    /**
     * Called by ReportsController.refreshAllCharts() with fresh data.
     *
     * data     — list of int[]{timeUnit, count} from PassengerDAO
     * period   — "DAILY", "WEEKLY", "MONTHLY", or "YEARLY"
     * anchor   — the anchor date, used to resolve WEEKLY week numbers to labels
     */
    public void updateChart(ArrayList<int[]> data, String period, LocalDate anchor) {

        // Step 1: Build the fixed x-axis labels for this period type.
        // We always render ALL expected time units — missing ones stay at 0.
        // This ensures the chart axis is always complete even on slow days.
        String[] labels = buildLabels(period, anchor);

        // Step 2: Build the dataset, initialized to 0 for every label
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (String label : labels) {
            dataset.addValue(0, "Ridership", label);
        }

        // Step 3: Overlay DB results onto the pre-filled dataset.
        // timeUnit from SQL maps to a label index — resolveLabel() converts it.
        for (int[] row : data) {
            int    timeUnit = row[0];
            int    count    = row[1];
            String label    = resolveLabel(timeUnit, period, anchor);
            if (label != null) {
                dataset.addValue(count, "Ridership", label);
            }
        }

        // Step 4: Build the JFreeChart bar chart
        JFreeChart chart = ChartFactory.createBarChart(
                "",                    // chart title — card header already labels it
                "",                    // x-axis label
                "Passengers",          // y-axis label
                dataset,
                PlotOrientation.VERTICAL,
                false,                 // legend — not needed for single series
                false,                 // tooltips
                false                  // urls
        );

        // Step 5: Minimal styling — white background to match ModernCard
        chart.setBackgroundPaint(Color.WHITE);
        chart.getPlot().setBackgroundPaint(Color.WHITE);

        // Step 6: Swap the ChartPanel in container
        if (currentChartPanel != null) {
            container.remove(currentChartPanel);
        }
        currentChartPanel = new ChartPanel(chart);
        container.add(currentChartPanel, BorderLayout.CENTER);
        container.revalidate();
        container.repaint();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds the complete ordered list of x-axis labels for the given period.
     *
     * DAILY:   "0h" through "23h"  (24 labels)
     * WEEKLY:  "Mon" through "Sat" (6 labels — Sun excluded, ops are Mon-Sat)
     * MONTHLY: "Week 1" through "Week N" covering the month's ISO weeks
     * YEARLY:  "Jan" through "Dec" (12 labels)
     */
    private String[] buildLabels(String period, LocalDate anchor) {
        if ("DAILY".equals(period)) {
            String[] labels = new String[24];
            for (int h = 0; h < 24; h++) labels[h] = h + "h";
            return labels;

        } else if ("WEEKLY".equals(period)) {
            return new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        } else if ("MONTHLY".equals(period)) {
            // Count distinct ISO week numbers in this month
            LocalDate start = anchor.withDayOfMonth(1);
            LocalDate end   = anchor.withDayOfMonth(anchor.lengthOfMonth());
            java.util.LinkedHashSet<String> weeks = new java.util.LinkedHashSet<>();
            LocalDate cursor = start;
            while (!cursor.isAfter(end)) {
                int weekNum = cursor.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
                weeks.add("Week " + weekNum);
                cursor = cursor.plusDays(1);
            }
            return weeks.toArray(new String[0]);

        } else {
            // YEARLY
            String[] labels = new String[12];
            for (int m = 0; m < 12; m++) {
                labels[m] = java.time.Month.of(m + 1)
                        .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            }
            return labels;
        }
    }

    /**
     * Converts a raw SQL timeUnit integer back to the matching label string.
     *
     * DAILY:   HOUR() returns 0–23   → "0h" … "23h"
     * WEEKLY:  DAYOFWEEK() returns 2–7 (MySQL: 1=Sun, 2=Mon … 7=Sat)
     * MONTHLY: WEEK() returns ISO week number → "Week N"
     * YEARLY:  MONTH() returns 1–12  → "Jan" … "Dec"
     */
    private String resolveLabel(int timeUnit, String period, LocalDate anchor) {
        if ("DAILY".equals(period)) {
            return timeUnit + "h";

        } else if ("WEEKLY".equals(period)) {
            // MySQL DAYOFWEEK: 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat
            // We only show Mon(2) through Sat(7)
            switch (timeUnit) {
                case 2: return "Mon";
                case 3: return "Tue";
                case 4: return "Wed";
                case 5: return "Thu";
                case 6: return "Fri";
                case 7: return "Sat";
                default: return null; // Sunday — excluded
            }

        } else if ("MONTHLY".equals(period)) {
            return "Week " + timeUnit;

        } else {
            // YEARLY: MONTH() returns 1–12
            if (timeUnit < 1 || timeUnit > 12) return null;
            return java.time.Month.of(timeUnit)
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        }
    }
}