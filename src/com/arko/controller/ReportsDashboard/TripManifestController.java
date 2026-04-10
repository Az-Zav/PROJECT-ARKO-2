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
import java.awt.event.ActionListener;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TripManifestController {

    private final TripManifestPanel panel;
    private final PassengerDAO      passengerDAO;
    private final StationDAO        stationDAO;
    private final boolean           isAdmin;

    private LocalDate anchorDate = LocalDate.now();
    private String    periodType = "DAILY";
    private int       stationId;

    private final ArrayList<Trip> loadedTrips = new ArrayList<>();

    private List<Station> cachedStations = Collections.emptyList();

    private final ActionListener periodComboListener;

    public TripManifestController(TripManifestPanel panel,
                                  PassengerDAO passengerDAO,
                                  StationDAO stationDAO) {
        this.panel        = panel;
        this.passengerDAO = passengerDAO;
        this.stationDAO   = stationDAO;
        this.isAdmin      = SessionManager.getInstance().isCurrentStaffAdmin();

        this.periodComboListener = e -> {
            Object sel = this.panel.cmbPeriod.getSelectedItem();
            if (sel == null) return;
            periodType = sel.toString().toUpperCase();
            anchorDate = LocalDate.now();
            refresh();
        };

        initStationFilter();
        initActionListeners();
        wireTripSelection();
        refresh();
    }

    private void initStationFilter() {
        panel.cmbStation.removeAllItems();

        if (isAdmin) {
            panel.cmbStation.addItem("All Stations");
            List<Station> stations = stationDAO.getAllStations();
            cachedStations = new ArrayList<>(stations);
            for (Station s : stations) {
                panel.cmbStation.addItem(s.getStationName());
            }
            stationId = -1;
        } else {
            stationId = SessionManager.getInstance().getCurrentStationId();
            Station station = stationDAO.getStationById(stationId);
            if (station != null) {
                panel.cmbStation.addItem(station.getStationName());
            }
            panel.cmbStation.setEnabled(false);
        }
    }

    private void initActionListeners() {
        panel.cmbStation.addActionListener(e -> {
            if (!isAdmin) return;
            int idx = panel.cmbStation.getSelectedIndex();
            if (idx < 0) return;
            if (idx == 0) {
                stationId = -1;
            } else {
                int offset = idx - 1;
                if (offset < cachedStations.size()) {
                    stationId = cachedStations.get(offset).getStationID();
                }
            }
            refresh();
        });

        panel.cmbPeriod.addActionListener(periodComboListener);

        panel.btnBack.addActionListener(e -> {
            anchorDate = shiftAnchor(anchorDate, periodType, -1);
            refresh();
        });

        panel.btnForward.addActionListener(e -> {
            anchorDate = shiftAnchor(anchorDate, periodType, 1);
            refresh();
        });
    }

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

    public void syncFromReportsFilter(LocalDate anchor, String period, int reportsStationId) {
        this.anchorDate = anchor;
        this.periodType = period;

        String display = period.substring(0, 1) + period.substring(1).toLowerCase();
        panel.cmbPeriod.removeActionListener(periodComboListener);
        panel.cmbPeriod.setSelectedItem(display);
        panel.cmbPeriod.addActionListener(periodComboListener);

        if (isAdmin) {
            stationId = reportsStationId;
        }

        refresh();
    }

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
