package com.arko.view.OperationalDashboard;

import com.arko.model.POJO.Passenger;
import com.arko.utils.OperationalDashboard.AppConstants;
import com.arko.utils.SessionManager;
import com.arko.view.ModernCard;
import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ManifestPanel extends ModernCard {

    // --- ACCESSIBLE COMPONENTS ---
    public JTable tableManifest;
    public DefaultTableModel manifestModel;
    public JLabel lblTripId;

    // --- CALLBACK INTERFACE ---
    @FunctionalInterface
    public interface ArrivalCallback {
        void onPassengerArrived(Passenger p);
    }

    private ArrivalCallback arrivalCallback;

    public ManifestPanel() {
        super("Passenger Manifest");

        String[] columns = {"Code", "Name", "From", "To", "Dir", "Action"};
        manifestModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column != 5) return false;
                Object val = getValueAt(row, 5);
                if (!(val instanceof Passenger)) return false;
                Passenger p = (Passenger) val;
                boolean alreadyArrived = AppConstants.PassengerStatus.ARRIVED.name()
                        .equalsIgnoreCase(p.getPassengerStatus());
                boolean atDestination = p.getDestinationStationID()
                        == SessionManager.getInstance().getCurrentStationId();
                return !alreadyArrived && atDestination;
            }
        };

        tableManifest = new JTable(manifestModel);

        UIStyler.styleManifestTable(tableManifest);
        UIStyler.configureColumnWidths(tableManifest);

        JScrollPane scrollPane = new JScrollPane(tableManifest);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        lblTripId = new JLabel("Trip: —");
        lblTripId.setFont(new Font("Inter", Font.BOLD, 11));
        lblTripId.setForeground(new Color(110, 117, 125));
        lblTripId.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTripId.setBorder(new EmptyBorder(0, 0, 6, 2));

        this.container.add(lblTripId, BorderLayout.NORTH);
        this.container.add(scrollPane, BorderLayout.CENTER);
    }

    // --- PUBLIC API FOR CONTROLLER ---
    public void setArrivalCallback(ArrivalCallback callback) {
        this.arrivalCallback = callback;
        setupTableEditors();
    }

    private void setupTableEditors() {
        tableManifest.getColumnModel().getColumn(5).setCellRenderer(new ArriveButtonRenderer());
        tableManifest.getColumnModel().getColumn(5).setCellEditor(new ArriveButtonEditor());
    }

    // --- INNER CLASSES FOR TABLE BUTTONS ---

    private class ArriveButtonRenderer extends JButton implements TableCellRenderer {

        public ArriveButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Inter", Font.BOLD, 10));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {

            if (!(value instanceof Passenger)) {
                setText("");
                setEnabled(false);
                return this;
            }

            Passenger p = (Passenger) value;
            boolean alreadyArrived = AppConstants.PassengerStatus.ARRIVED.name()
                    .equalsIgnoreCase(p.getPassengerStatus());
            boolean atDestination = p.getDestinationStationID()
                    == SessionManager.getInstance().getCurrentStationId();

            // ARRIVED passengers show a disabled "COMPLETED" label —
            // they remain visible in the manifest for reference but cannot
            // be processed again. BOARDED passengers only show an active
            // "ARRIVED" button if this station is their destination.
            if (alreadyArrived) {
                setText("COMPLETED");
                setEnabled(false);
                setBackground(new Color(220, 220, 220));
                setForeground(new Color(120, 120, 120));
            } else if (!atDestination) {
                setText("IN TRANSIT");
                setEnabled(false);
                setBackground(new Color(220, 220, 220));
                setForeground(new Color(120, 120, 120));
            } else {
                setText("ARRIVED");
                setEnabled(true);
                setBackground(new Color(40, 167, 69));
                setForeground(Color.WHITE);
            }

            return this;
        }
    }

    private class ArriveButtonEditor extends AbstractCellEditor implements TableCellEditor {

        private final JButton button;
        private Passenger currentPassenger;

        public ArriveButtonEditor() {
            button = new JButton("ARRIVED");
            button.setFont(new Font("Inter", Font.BOLD, 10));
            button.addActionListener(e -> {
                fireEditingStopped();

                // Guard: do not re-process a passenger who has already ARRIVED.
                // This prevents a double-decrement of vessel load if the editor
                // is somehow triggered on an already-completed row.
                if (currentPassenger == null) return;
                if (AppConstants.PassengerStatus.ARRIVED.name().equalsIgnoreCase(
                        currentPassenger.getPassengerStatus())) return;

                if (arrivalCallback != null) {
                    arrivalCallback.onPassengerArrived(currentPassenger);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value,
                boolean isSelected, int row, int col) {
            currentPassenger = (value instanceof Passenger) ? (Passenger) value : null;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return currentPassenger;
        }
    }
}