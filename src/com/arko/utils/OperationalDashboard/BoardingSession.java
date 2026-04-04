package com.arko.utils.OperationalDashboard;

import com.arko.model.POJO.Vessel;

/**
 * Shared boarding session state for a single vessel docking.
 *
 * Owned by ControlPanelController and injected into PassengerWaitlistController,
 * replacing the circular setter dependency between the two controllers.
 *
 * Lifecycle:
 *   markDocked()   — called when ARRIVED is pressed
 *   markDeparted() — called when DEPART is pressed, resets all state
 */
public class BoardingSession {

    private boolean docked          = false;
    private int     boardingLimit   = 0;
    private int     boardedCount    = 0;
    private String  dockedDirection = null;
    private Vessel dockedVessel    = null;

    // ── State transitions ─────────────────────────────────────────────────────

    /**
     * Marks the session as active (vessel has docked).
     * Called by ControlPanelController when ARRIVED is confirmed.
     */
    public void markDocked(Vessel vessel, int boardingLimit, String direction) {
        this.dockedVessel       = vessel;
        this.docked             = true;
        this.boardingLimit      = boardingLimit;
        this.boardedCount       = 0;
        this.dockedDirection    = direction;
    }

    /**
     * Resets all session state (vessel has departed).
     * Called by ControlPanelController when DEPART is confirmed.
     */
    public void markDeparted() {
        this.dockedVessel    = null;
        this.docked          = false;
        this.boardingLimit   = 0;
        this.boardedCount    = 0;
        this.dockedDirection = null;
    }

    public Vessel getDockedVessel() { return dockedVessel; }

    public String getDockedDirection() { return dockedDirection; }

    /**
     * Updates the boarding limit mid-session.
     * Called by ControlPanelController when the boarding preview is refreshed
     * (e.g. waitlist count changes after a new passenger registers).
     */
    public void setBoardingLimit(int limit) {
        this.boardingLimit = limit;
    }

    // ── Counter ───────────────────────────────────────────────────────────────

    /**
     * Increments the boarded count by n.
     * Called after each successful individual board action.
     */
    public void incrementBoarded(int n) {
        this.boardedCount += n;
    }

    /**
     * Seeds the boarded count from an existing DB value instead of starting at 0.
     * Called immediately after markDocked() on ARRIVED — queries how many passengers
     * were already boarded on this trip (e.g. at a previous station) so the counter
     * resumes correctly after a crash or restart mid-session.
     */
    public void seedBoardedCount(int count) {
        this.boardedCount = count;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /** Returns true only after ARRIVED is pressed and before DEPART is pressed. */
    public boolean isDocked() {
        return docked;
    }

    /** Returns true when boardedCount has reached or exceeded the boardingLimit. */
    public boolean isBoardingLimitReached() {
        return boardingLimit > 0 && boardedCount >= boardingLimit;
    }

    public int getBoardingLimit() {
        return boardingLimit;
    }

    public int getBoardedCount() {
        return boardedCount;
    }

    /**
     * Compact method to format the UI text for the current boarding progress.
     */
    public String getBoardingProgressText() {
        if (!docked) return "Boarding: 0 / 0";
        return "Boarding: " + boardedCount + " / " + boardingLimit;
    }
}