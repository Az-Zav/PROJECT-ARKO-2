package com.arko.controller.ManageEntities;

import com.arko.model.DAO.StationDAO;
import com.arko.model.POJO.Station;
import com.arko.view.AdminDashboard.ManageEntities.ManageStationsPanel;
import com.arko.view.AdminDashboard.ManageEntities.StationFormDialog;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class ManageStationsController {

    private final ManageStationsPanel panel;
    private final StationDAO stationDAO;

    public ManageStationsController(ManageStationsPanel panel, StationDAO stationDAO) {
        this.panel      = panel;
        this.stationDAO = stationDAO;

        // 1. Setup the Actions Column (index 4: StationID, Name, Code, Status, Actions)
        panel.table.getColumnModel().getColumn(4).setCellRenderer(new ActionCellRenderer());
        panel.table.getColumnModel().getColumn(4).setCellEditor(new ActionCellEditor());

        // 2. Bind the "Add Station" Button
        panel.btnAdd.addActionListener(e -> handleAddStation());

        // 3. Initial Data Load
        refreshTable();
    }

    public void refreshTable() {
        panel.tableModel.setRowCount(0);
        List<Station> stations = stationDAO.getAllStations();

        for (Station s : stations) {
            panel.tableModel.addRow(new Object[]{
                    s.getStationID(),
                    s.getStationName(),
                    s.getStationCode(),
                    s.getOperationalStatus(),
                    s   // Station POJO stored in Actions column for editor access
            });
        }
    }

    private void handleAddStation() {
        Window parentWindow = SwingUtilities.getWindowAncestor(panel);
        StationFormDialog dialog = new StationFormDialog(parentWindow, null);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            if (!isCodeValid(dialog)) return;

            Station newStation = extractStationFromDialog(dialog, new Station());

            if (stationDAO.createStation(newStation)) {
                refreshTable();
                JOptionPane.showMessageDialog(panel,
                        "Station added successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(panel,
                        "Failed to save station. Code may already exist.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleUpdateStation(Station existingStation) {
        Window parentWindow = SwingUtilities.getWindowAncestor(panel);
        StationFormDialog dialog = new StationFormDialog(parentWindow, existingStation);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            if (!isCodeValid(dialog)) return;

            Station updatedStation = extractStationFromDialog(dialog, existingStation);

            if (stationDAO.updateStation(updatedStation)) {
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(panel,
                        "Failed to update station.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteStation(Station s) {
        int confirm = JOptionPane.showConfirmDialog(panel,
                "Are you sure you want to delete station \"" + s.getStationName() + "\"?\n" +
                        "This may affect existing trips assigned to this station.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (stationDAO.deleteStation(s.getStationID())) {
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(panel,
                        "Failed to delete station. It may still be referenced by existing trips or staff.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Validates that the station code is not blank and is within a reasonable length (3-5 chars).
     */
    private boolean isCodeValid(StationFormDialog dialog) {
        String code = dialog.txtCode.getText().trim();
        if (code.isBlank() || code.length() > 5) {
            JOptionPane.showMessageDialog(panel,
                    "Station code must be between 1 and 5 characters.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private Station extractStationFromDialog(StationFormDialog dialog, Station station) {
        station.setStationName(dialog.txtName.getText().trim());
        station.setStationCode(dialog.txtCode.getText().trim().toUpperCase());
        station.setOperationalStatus((String) dialog.cbStatus.getSelectedItem());
        return station;
    }

    // ── INNER CLASSES FOR CUSTOM TABLE BUTTONS ──────────────────────

    private class ActionCellRenderer extends JPanel implements TableCellRenderer {
        private final JButton btnEdit   = new JButton("✎");
        private final JButton btnDelete = new JButton("🗑");

        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
            setOpaque(true);
            styleActionButton(btnEdit,   new Color(40, 167, 69));
            styleActionButton(btnDelete, new Color(220, 53, 69));
            add(btnEdit);
            add(btnDelete);
        }

        private void styleActionButton(JButton b, Color c) {
            b.setFocusPainted(false);
            b.setFont(new Font("Inter", Font.BOLD, 12));
            b.setForeground(c);
            b.setBorder(BorderFactory.createLineBorder(c, 1));
            b.setBackground(Color.WHITE);
            b.setPreferredSize(new Dimension(30, 25));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    private class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel   container = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        private final JButton  btnEdit   = new JButton("✎");
        private final JButton  btnDelete = new JButton("🗑");
        private Station currentStation;

        public ActionCellEditor() {
            container.setOpaque(true);
            btnEdit.addActionListener(e -> {
                stopCellEditing();
                handleUpdateStation(currentStation);
            });
            btnDelete.addActionListener(e -> {
                stopCellEditing();
                handleDeleteStation(currentStation);
            });
            container.add(btnEdit);
            container.add(btnDelete);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {
            currentStation = (Station) value;
            container.setBackground(table.getSelectionBackground());
            return container;
        }

        @Override
        public Object getCellEditorValue() {
            return currentStation;
        }
    }
}