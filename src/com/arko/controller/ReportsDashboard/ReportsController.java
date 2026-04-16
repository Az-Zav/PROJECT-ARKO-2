package com.arko.controller.ReportsDashboard;

import com.arko.model.DAO.PassengerDAO;
import com.arko.model.DAO.StationDAO;
import com.arko.model.POJO.Station;
import com.arko.utils.SessionManager;
import com.arko.view.ReportsDashboard.ClassificationPanel;
import com.arko.view.ReportsDashboard.ReportsFilterPanel;
import com.arko.view.ReportsDashboard.RidershipTimelinePanel;
import com.arko.view.ReportsDashboard.StationAnalyticsPanel;
import com.arko.view.ReportsDashboard.TripManifestPanel;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReportsController {

    // panel fields
    private final ReportsFilterPanel     filterPanel;
    private final RidershipTimelinePanel timelinePanel;
    private final ClassificationPanel    classificationPanel;
    private final StationAnalyticsPanel  analyticsPanel;

    // DAO Fields
    private final PassengerDAO passengerDAO;
    private final StationDAO   stationDAO;

    // local field to check for admin role
    private final boolean isAdmin;

    /** Cached for admin station dropdown — avoids repeated DB hits on each selection event. */
    private List<Station> cachedAdminStations = Collections.emptyList();

    private final TripManifestController tripManifestController;
    private final TripManifestPanel      tripManifestPanel;
    private final CardLayout innerCard;
    private final JPanel innerPanel;
    private boolean                      manifestActive = false;

    public ReportsController(ReportsFilterPanel filterPanel,
                             RidershipTimelinePanel timelinePanel,
                             ClassificationPanel classificationPanel,
                             StationAnalyticsPanel analyticsPanel,
                             PassengerDAO passengerDAO,
                             StationDAO stationDAO,
                             TripManifestController tripManifestController,
                             TripManifestPanel tripManifestPanel,
                             CardLayout innerCard,
                             JPanel innerPanel) {

        this.filterPanel         = filterPanel;
        this.timelinePanel       = timelinePanel;
        this.classificationPanel = classificationPanel;
        this.analyticsPanel      = analyticsPanel;

        this.passengerDAO = passengerDAO;
        this.stationDAO   = stationDAO;
        this.isAdmin      = SessionManager.getInstance().isCurrentStaffAdmin();

        this.tripManifestController = tripManifestController;
        this.tripManifestPanel      = tripManifestPanel;
        this.innerCard              = innerCard;
        this.innerPanel             = innerPanel;


        initFilterDefaults();
        initActionListeners();
        runFirstLoad();
    }

    private void showManifest() {
        manifestActive = true;
        innerCard.show(innerPanel, "MANIFEST");
        filterPanel.btnToggleManifest.setText("CHARTS");

        // Sync manifest filter to current chart filter state, then load
        tripManifestController.syncFromReportsFilter(
                SessionManager.getInstance().getReportsAnchorDate(),
                SessionManager.getInstance().getReportsPeriodType(),
                SessionManager.getInstance().getReportsStationId()
        );
    }

    private void showCharts() {
        manifestActive = false;
        innerCard.show(innerPanel, "CHARTS");
        filterPanel.btnToggleManifest.setText("MANIFEST");
    }

    /**
     * Populates the station dropdown and locks it for staff.
     * Sets the initial stationId in SessionManager based on role.
     */
    private void initFilterDefaults() {
        filterPanel.cmbStation.removeAllItems();

        if (isAdmin) {
            filterPanel.cmbStation.addItem("All Stations");
            List<Station> stations = stationDAO.getAllStations();
            cachedAdminStations = new ArrayList<>(stations);
            for (Station s : stations) {
                filterPanel.cmbStation.addItem(s.getStationName());
            }
            SessionManager.getInstance().setReportsStationId(-1); // default: all
        } else {
            // Staff sees only their own station — dropdown is locked
            int stationId = SessionManager.getInstance().getCurrentStationId();
            Station station = stationDAO.getStationById(stationId);
            if (station != null) {
                filterPanel.cmbStation.addItem(station.getStationName());
            }
            filterPanel.cmbStation.setEnabled(false);
            SessionManager.getInstance().setReportsStationId(stationId);
        }

        // Default period is DAILY
        SessionManager.getInstance().setReportsPeriodType("DAILY");
    }

    /**
     * Wires all filter panel controls to the controller.
     * Every control eventually calls refreshAllCharts().
     */
    private void initActionListeners() {

        // Station dropdown — only fires meaningfully for admin
        filterPanel.cmbStation.addActionListener(e -> {
            int selectedIndex = filterPanel.cmbStation.getSelectedIndex();
            if (selectedIndex < 0) return;

            if (isAdmin) {
                if (selectedIndex == 0) {
                    SessionManager.getInstance().setReportsStationId(-1);
                } else {
                    int stationIndex = selectedIndex - 1;
                    if (stationIndex < cachedAdminStations.size()) {
                        SessionManager.getInstance().setReportsStationId(
                                cachedAdminStations.get(stationIndex).getStationID());
                    }
                }
            }
            refreshAllCharts();
        });

        // Period type dropdown
        filterPanel.cmbPeriod.addActionListener(e -> {
            Object selected = filterPanel.cmbPeriod.getSelectedItem();
            if (selected == null) return;

            String period = selected.toString().toUpperCase();
            SessionManager.getInstance().setReportsPeriodType(period);

            // Reset anchor to "now" whenever period type changes
            SessionManager.getInstance().setReportsAnchorDate(LocalDate.now());

            refreshAllCharts();
        });

        // Back button — shifts anchor one period backward
        filterPanel.btnBack.addActionListener(e -> {
            LocalDate anchor = SessionManager.getInstance().getReportsAnchorDate();
            String period    = SessionManager.getInstance().getReportsPeriodType();

            if      ("DAILY".equals(period))   anchor = anchor.minusDays(1);
            else if ("WEEKLY".equals(period))  anchor = anchor.minusWeeks(1);
            else if ("MONTHLY".equals(period)) anchor = anchor.minusMonths(1);
            else if ("YEARLY".equals(period))  anchor = anchor.minusYears(1);

            SessionManager.getInstance().setReportsAnchorDate(anchor);
            refreshAllCharts();
        });

        // Forward button — shifts anchor one period forward
        filterPanel.btnForward.addActionListener(e -> {
            LocalDate anchor = SessionManager.getInstance().getReportsAnchorDate();
            String period    = SessionManager.getInstance().getReportsPeriodType();

            if      ("DAILY".equals(period))   anchor = anchor.plusDays(1);
            else if ("WEEKLY".equals(period))  anchor = anchor.plusWeeks(1);
            else if ("MONTHLY".equals(period)) anchor = anchor.plusMonths(1);
            else if ("YEARLY".equals(period))  anchor = anchor.plusYears(1);

            SessionManager.getInstance().setReportsAnchorDate(anchor);
            refreshAllCharts();
        });

        // Export button — placeholder for File 8
        filterPanel.btnExport.addActionListener(e -> {
            // TODO: hook up ReportsPDFExporter in File 8
            javax.swing.JOptionPane.showMessageDialog(null,
                    "PDF Export coming soon.", "Export", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        });

        //MANIFEST TOGGLE LISTENER
        filterPanel.btnToggleManifest.addActionListener(e -> {
            if (manifestActive) showCharts();
            else showManifest();
        });

        // Trip Manifest "back" to charts
        if (tripManifestPanel != null && tripManifestPanel.btnViewCharts != null) {
            tripManifestPanel.btnViewCharts.addActionListener(e -> showCharts());
        };
    }

    /**
     * Called once during construction.
     * Checks if today has data — if not, falls back to yesterday.
     * Then runs the first full chart refresh.
     */
    private void runFirstLoad() {
        LocalDate today = LocalDate.now();

        if (!passengerDAO.hasArrivedDataForDate(today)) {
            SessionManager.getInstance().setReportsAnchorDate(today.minusDays(1));
        } else {
            SessionManager.getInstance().setReportsAnchorDate(today);
        }

        refreshAllCharts();
    }

    // ── Core pipeline ─────────────────────────────────────────────────────────

    /**
     * The single method called by every action listener.
     * Reads filter state, derives date range, queries DAO, updates all panels.
     */
    public void refreshAllCharts() {
        String    period    = SessionManager.getInstance().getReportsPeriodType();
        LocalDate anchor    = SessionManager.getInstance().getReportsAnchorDate();
        int       stationId = SessionManager.getInstance().getReportsStationId();

        // Step 1: derive startDate and endDate from anchor + period
        LocalDate startDate = deriveStartDate(anchor, period);
        LocalDate endDate   = deriveEndDate(anchor, period);

        // Step 2: update the period label in the filter panel
        filterPanel.lblPeriodDisplay.setText(buildPeriodLabel(anchor, period));

        // Step 3: query DAO for all three datasets
        ArrayList<int[]> timelineData =
                passengerDAO.getRidershipTimeline(startDate, endDate, period, stationId);

        ArrayList<String[]> classificationData =
                passengerDAO.getClassificationBreakdown(startDate, endDate, stationId);

        ArrayList<ArrayList<String[]>> boardingAlightingData =
                passengerDAO.getBoardingAlightingData(startDate, endDate, stationId);

        // Step 4: query total ridership count for the label
        int total = 0;
        for (int[] row : timelineData) total += row[1];
        filterPanel.lblTotalRidership.setText("Total Ridership: " + total);

        // Step 5: push data to each chart panel
        timelinePanel.updateChart(timelineData, period, startDate);
        classificationPanel.updateChart(classificationData);
        analyticsPanel.updateChart(boardingAlightingData, isAdmin);

        //Step 6: COmpare then disable anchor if currentdate
        updateNavButtons();
    }

    // ── Date range derivation ─────────────────────────────────────────────────

    /**
     * Derives the start of the window from the anchor date and period type.
     *
     * DAILY:   the anchor date itself (one full day)
     * WEEKLY:  the Monday of the anchor date's week
     * MONTHLY: the first day of the anchor date's month
     * YEARLY:  January 1 of the anchor date's year
     */
    private LocalDate deriveStartDate(LocalDate anchor, String period) {
        if ("DAILY".equals(period)) {
            return anchor;
        } else if ("WEEKLY".equals(period)) {
            // Monday of the week containing anchor
            return anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        } else if ("MONTHLY".equals(period)) {
            return anchor.withDayOfMonth(1);
        } else {
            // YEARLY
            return anchor.withDayOfYear(1);
        }
    }

    /**
     * Derives the end of the window from the anchor date and period type.
     *
     * DAILY:   the anchor date itself
     * WEEKLY:  the Saturday of the anchor date's week (ops are Mon-Sat only)
     * MONTHLY: the last day of the anchor date's month
     * YEARLY:  December 31 of the anchor date's year
     */
    private LocalDate deriveEndDate(LocalDate anchor, String period) {
        if ("DAILY".equals(period)) {
            return anchor;
        } else if ("WEEKLY".equals(period)) {
            // Saturday of the week containing anchor
            return anchor.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        } else if ("MONTHLY".equals(period)) {
            return anchor.with(TemporalAdjusters.lastDayOfMonth());
        } else {
            // YEARLY
            return anchor.with(TemporalAdjusters.lastDayOfYear());
        }
    }

    /**
     * Builds a human-readable label for the current period window.
     * Displayed in filterPanel.lblPeriodDisplay so the user always knows
     * exactly what date range they are looking at.
     */
    private String buildPeriodLabel(LocalDate anchor, String period) {
        if ("DAILY".equals(period)) {
            return anchor.toString(); // e.g. "2026-03-29"
        } else if ("WEEKLY".equals(period)) {
            LocalDate mon = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate sat = anchor.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
            return mon + "  →  " + sat;
        } else if ("MONTHLY".equals(period)) {
            return anchor.getYear() + " — " + anchor.getMonth().toString();
        } else {
            return String.valueOf(anchor.getYear());
        }
    }

    /**
     * Disables next button in navigation if time period is latest
     */
    public void updateNavButtons(){
        LocalDate today = LocalDate.now();
        LocalDate anchor = SessionManager.getInstance().getReportsAnchorDate();
        String    period = SessionManager.getInstance().getReportsPeriodType();

        LocalDate end = deriveEndDate(anchor, period); //store the current end date of the period
        boolean isLatest = !end.isBefore(today); //checks if end date is before today, returns true if either today or after

        filterPanel.btnForward.setEnabled(!isLatest); //do not enable if latest
    }
}
