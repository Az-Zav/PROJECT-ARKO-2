package com.arko.controller.ManageEntities;

import com.arko.model.DAO.VesselDAO;
import com.arko.model.POJO.Vessel;
import com.arko.view.AdminDashboard.ManageEntities.ManageVesselsPanel;
import com.arko.view.AdminDashboard.ManageEntities.VesselFormDialog;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class ManageVesselsController {

    private final ManageVesselsPanel panel;
    private final VesselDAO vesselDAO;

    public ManageVesselsController(ManageVesselsPanel panel, VesselDAO vesselDAO) {
        this.panel     = panel;
        this.vesselDAO = vesselDAO;

        // 1. Setup the Actions Column (index 4: VesselID, Name, MaxCapacity, Status, Actions)
        panel.table.getColumnModel().getColumn(4).setCellRenderer(new ActionCellRenderer());
        panel.table.getColumnModel().getColumn(4).setCellEditor(new ActionCellEditor());

        // 2. Bind the "Add Vessel" Button
        panel.btnAdd.addActionListener(e -> handleAddVessel());

        // 3. Initial Data Load
        refreshTable();
    }

    public void refreshTable() {
        panel.tableModel.setRowCount(0);
        List<Vessel> vessels = vesselDAO.getAllVessels();

        for (Vessel v : vessels) {
            panel.tableModel.addRow(new Object[]{
                    v.getVesselID(),
                    v.getVesselName(),
                    v.getMaxCapacity(),
                    v.getVesselStatus(),
                    v   // Vessel POJO stored in Actions column for editor access
            });
        }
    }

    private void handleAddVessel() {
        Window parentWindow = SwingUtilities.getWindowAncestor(panel);
        VesselFormDialog dialog = new VesselFormDialog(parentWindow, null);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            if (!isCapacityValid(dialog)) return;

            Vessel newVessel = extractVesselFromDialog(dialog, new Vessel());

            if (vesselDAO.createVessel(newVessel)) {
                refreshTable();
                JOptionPane.showMessageDialog(panel,
                        "Vessel added successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(panel,
                        "Failed to save vessel. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleUpdateVessel(Vessel existingVessel) {
        Window parentWindow = SwingUtilities.getWindowAncestor(panel);
        VesselFormDialog dialog = new VesselFormDialog(parentWindow, existingVessel);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            if (!isCapacityValid(dialog)) return;

            Vessel candidate = new Vessel();
            candidate.setVesselID(existingVessel.getVesselID());
            extractVesselFromDialog(dialog, candidate);

            if (vesselDAO.updateVessel(candidate)) {
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(panel,
                        "Failed to update vessel.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteVessel(Vessel v) {
        int confirm = JOptionPane.showConfirmDialog(panel,
                "Are you sure you want to delete vessel \"" + v.getVesselName() + "\"?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (vesselDAO.deleteVessel(v.getVesselID())) {
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(panel,
                        "Failed to delete vessel.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Validates that Max Capacity is a positive integer.
     */
    private boolean isCapacityValid(VesselFormDialog dialog) {
        try {
            int capacity = Integer.parseInt(dialog.txtMaxCapacity.getText().trim());
            if (capacity <= 0) throw new NumberFormatException();
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(panel,
                    "Max Capacity must be a positive whole number.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    private Vessel extractVesselFromDialog(VesselFormDialog dialog, Vessel vessel) {
        vessel.setVesselName(dialog.txtName.getText().trim());
        vessel.setMaxCapacity(Integer.parseInt(dialog.txtMaxCapacity.getText().trim()));
        vessel.setVesselStatus((String) dialog.cbStatus.getSelectedItem());
        return vessel;
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
        private Vessel currentVessel;

        public ActionCellEditor() {
            container.setOpaque(true);
            btnEdit.addActionListener(e -> {
                stopCellEditing();
                handleUpdateVessel(currentVessel);
            });
            btnDelete.addActionListener(e -> {
                stopCellEditing();
                handleDeleteVessel(currentVessel);
            });
            container.add(btnEdit);
            container.add(btnDelete);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {
            currentVessel = (Vessel) value;
            container.setBackground(table.getSelectionBackground());
            return container;
        }

        @Override
        public Object getCellEditorValue() {
            return currentVessel;
        }
    }
}