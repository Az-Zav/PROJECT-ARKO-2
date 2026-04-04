package com.arko.utils.OperationalDashboard;

import com.arko.model.POJO.Vessel;

import java.util.List;
import java.util.Map;

/**
 * Implements the Pasig River boarding limit formula:
 *
 *   Lx = (C - Ox) - Σ min(Bi, Ri)   [with fair share floor and hard cap]
 *
 * Variables:
 *   C   = Total vessel capacity
 *   Ox  = Net occupancy (passengers remaining after current alighters exit)
 *   N   = Number of stations still ahead in the current direction (including current)
 *   Bi  = Fair share constant = C / N  (equal slice per remaining station)
 *   Ri  = Real-time demand at downstream station i (same-direction passengers only)
 *   Lx  = Boarding limit at current station
 *
 * Step-by-step:
 *   1. physicalVacancy = C - Ox
 *   2. For each downstream station i: reserve = min(Bi, Ri)
 *   3. totalReservations = Σ reserve
 *   4. rawLimit = physicalVacancy - totalReservations
 *   5. fairShareFloor = min(currentDemand, Bi)
 *   6. result = max(rawLimit, fairShareFloor)   ← never bypass current station unfairly
 *   7. finalLimit = min(result, physicalVacancy) ← never exceed physical space
 */
public final class BoardingCalculation {

    private BoardingCalculation() {}

    /**
     * Calculates the boarding limit at the current station.
     *
     * @param vessel               the docked vessel (provides C and Ox)
     * @param remainingStationIds  ordered IDs of stations ahead in the current direction
     *                             (from StationDAO.getRemainingStationIds)
     * @param downstreamDemand     map of stationId → waiting count for those stations
     *                             (from StationDAO.getDownstreamWaitingCounts — one batch query)
     * @param currentStationDemand number of same-direction passengers waiting at this station
     * @return boarding limit Lx, guaranteed >= 0
     */
    public static int calculate(
            Vessel vessel,
            List<Integer> remainingStationIds,
            Map<Integer, Integer> downstreamDemand,
            int currentStationDemand) {

        if (vessel == null) return 0;

        int C  = vessel.getMaxCapacity();
        int Ox = vessel.getCurrentLoad();

        // Step 1 — Physical vacancy
        int physicalVacancy = Math.max(0, C - Ox); // Guard rail to ensure is never negative
        if (physicalVacancy == 0) return 0; // Vessel full — no boarding possible

        // Step 2-3 — Fair share constant and downstream reservations
        // remainingStations = stations ahead + current station
        int N  = remainingStationIds.size() + 1; // FROM PARAMETER
        int Bi = (N > 1) ? C / N : C; // If no stations ahead, full capacity is fair share

        int totalReservations = 0;
        for (int stationId : remainingStationIds) {
            int Ri      = downstreamDemand.getOrDefault(stationId, 0);
            int reserve = Math.min(Bi, Ri);
            totalReservations += reserve;
        }

        // Step 4 — Raw boarding limit
        int rawLimit = physicalVacancy - totalReservations;

        // Step 5-6 — Fair share floor: current station is guaranteed its fair slice
        // if demand exists, even if rawLimit would be lower
        int fairShareFloor = Math.min(currentStationDemand, Bi);
        int result         = Math.max(rawLimit, fairShareFloor);

        // Step 7 — Hard cap: never exceed physical space
        return Math.min(result, physicalVacancy);
    }
}