package com.arko.controller.ReportsDashboard;

import com.arko.model.DAO.PassengerDAO;
import com.arko.model.DAO.StationDAO;
import com.arko.model.POJO.Passenger;
import com.arko.model.POJO.Station;
import com.arko.model.POJO.Trip;
import com.arko.utils.SessionManager;
import com.arko.view.ReportsDashboard.TripManifestPanel;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class TripManifestController {

    private final TripManifestPanel panel;
    private final PassengerDAO      passengerDAO;
    private final StationDAO        stationDAO;
    private final boolean           isAdmin;

    // Own filter state — independent from the charts' state
    private LocalDate anchorDate = LocalDate.now();
    private String    periodType = "DAILY";
    private int       stationId;

    // Parallel list keeps Trip objects in sync with table rows
    private final ArrayList<Trip> loadedTrips = new ArrayList<>();

    public TripManifestController(TripManifestPanel panel) {
        this.panel        = panel;
        this.passengerDAO = new PassengerDAO();
        this.stationDAO   = new StationDAO();
        this.isAdmin      = SessionManager.getInstance().isCurrentStaffAdmin();

        initStationFilter();
        initActionListeners();
        wireTripSelection();
        refresh();
    }

    // ── Station dropdown setup ────────────────────────────────────────────────

    private void initStationFilter() {
        panel.cmbStation.removeAllItems();

        if (isAdmin) {
            panel.cmbStation.addItem("All Stations");
            List<Station> stations = stationDAO.getAllStations();
            for (Station s : stations) {
                panel.cmbStation.addItem(s.getStationName());
            }
            stationId = -1;
        } else {
            // Staff: locked to their own station
            stationId = SessionManager.getInstance().getCurrentStationId();
            Station station = stationDAO.getStationById(stationId);
            if (station != null) {
                panel.cmbStation.addItem(station.getStationName());
            }
            panel.cmbStation.setEnabled(false);
        }
    }

    // ── Action listeners ──────────────────────────────────────────────────────

    private void initActionListeners() {
        panel.cmbStation.addActionListener(e -> {
            if (!isAdmin) return;
            int idx = panel.cmbStation.getSelectedIndex();
            if (idx < 0) return;
            if (idx == 0) {
                stationId = -1;
            } else {
                List<Station> stations = stationDAO.getAllStations();
                int offset = idx - 1;
                if (offset < stations.size()) {
                    stationId = stations.get(offset).getStationID();
                }
            }
            refresh();
        });

        panel.cmbPeriod.addActionListener(e -> {
            Object sel = panel.cmbPeriod.getSelectedItem();
            if (sel == null) return;
            periodType = sel.toString().toUpperCase();
            anchorDate = LocalDate.now();
            refresh();
        });

        panel.btnBack.addActionListener(e -> {
            anchorDate = shiftAnchor(anchorDate, periodType, -1);
            refresh();
        });

        panel.btnForward.addActionListener(e -> {
            anchorDate = shiftAnchor(anchorDate, periodType, 1);
            refresh();
        });
    }

    // ── Trip row selection → passenger detail ─────────────────────────────────

    private void wireTripSelection() {
        panel.tripTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        if (e.getValueIsAdjusting()) return;
                        int row = panel.tripTable.getSelectedRow();
                        if (row < 0 || row >= loadedTrips.size()) return;
                        loadPassengersForTrip(loadedTrips.get(row));
                    }
                }
        );
    }

    // ── Called by ReportsController when the manifest view becomes active ─────
    // Syncs anchor + period from the shared filter so both views stay aligned.

    public void syncFromReportsFilter(LocalDate anchor, String period, int reportsStationId) {
        this.anchorDate = anchor;
        this.periodType = period;

        // Sync period dropdown without re-triggering the listener
        String display = period.substring(0, 1) + period.substring(1).toLowerCase();
        panel.cmbPeriod.removeActionListener(panel.cmbPeriod.getActionListeners()[0]);
        panel.cmbPeriod.setSelectedItem(display);
        panel.cmbPeriod.addActionListener(e -> {
            Object sel = panel.cmbPeriod.getSelectedItem();
            if (sel == null) return;
            periodType = sel.toString().toUpperCase();
            anchorDate = LocalDate.now();
            refresh();
        });

        // Only sync station for admin — staff stay locked to their own station
        if (isAdmin) {
            stationId = reportsStationId;
        }

        refresh();
    }

    // ── Core refresh ──────────────────────────────────────────────────────────

    public void refresh() {
        LocalDate start = deriveStart(anchorDate, periodType);
        LocalDate end   = deriveEnd(anchorDate, periodType);

        panel.lblPeriodDisplay.setText(buildPeriodLabel(anchorDate, periodType));
        updateNavButtons(end);

        panel.tripModel.setRowCount(0);
        panel.passengerModel.setRowCount(0);
        panel.lblSelectedTrip.setText("Select a trip above to view its passengers.");
        loadedTrips.clear();

        ArrayList<Trip> trips = passengerDAO.getTripsForDateRange(start, end, stationId);
        for (Trip t : trips) {
            loadedTrips.add(t);
            panel.tripModel.addRow(new Object[]{
                    t.getTripID(),
                    t.getVesselName(),
                    t.getDepartureTime(),
                    t.getTripDirection(),
                    t.getTripStatus(),
                    t.getCurrentLoad()
            });
        }
    }

    private void loadPassengersForTrip(Trip trip) {
        panel.passengerModel.setRowCount(0);
        panel.lblSelectedTrip.setText(
                "Trip #" + trip.getTripID() +
                        " — " + trip.getVesselName() +
                        " (" + trip.getTripDirection() + ")");

        List<Passenger> passengers = passengerDAO.getPassengersForTrip(trip.getTripID());
        for (Passenger p : passengers) {
            panel.passengerModel.addRow(new Object[]{
                    p.getBoardingCode(),
                    p.getFullName(),
                    p.getOriginCode(),
                    p.getDestinationCode(),
                    p.getClassification(),
                    p.getPassengerDirection(),
                    p.getPassengerStatus()
            });
        }
    }

    // ── Date helpers ──────────────────────────────────────────────────────────

    private LocalDate shiftAnchor(LocalDate anchor, String period, int direction) {
        if ("DAILY".equals(period))   return anchor.plusDays(direction);
        if ("WEEKLY".equals(period))  return anchor.plusWeeks(direction);
        if ("MONTHLY".equals(period)) return anchor.plusMonths(direction);
        return anchor.plusYears(direction);
    }

    private LocalDate deriveStart(LocalDate anchor, String period) {
        if ("DAILY".equals(period))   return anchor;
        if ("WEEKLY".equals(period))  return anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if ("MONTHLY".equals(period)) return anchor.withDayOfMonth(1);
        return anchor.withDayOfYear(1);
    }

    private LocalDate deriveEnd(LocalDate anchor, String period) {
        if ("DAILY".equals(period))   return anchor;
        if ("WEEKLY".equals(period))  return anchor.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        if ("MONTHLY".equals(period)) return anchor.with(TemporalAdjusters.lastDayOfMonth());
        return anchor.with(TemporalAdjusters.lastDayOfYear());
    }

    private String buildPeriodLabel(LocalDate anchor, String period) {
        if ("DAILY".equals(period))   return anchor.toString();
        if ("WEEKLY".equals(period)) {
            LocalDate mon = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate sat = anchor.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
            return mon + "  →  " + sat;
        }
        if ("MONTHLY".equals(period)) return anchor.getYear() + " — " + anchor.getMonth();
        return String.valueOf(anchor.getYear());
    }

    private void updateNavButtons(LocalDate end) {
        panel.btnForward.setEnabled(end.isBefore(LocalDate.now()));
    }
}