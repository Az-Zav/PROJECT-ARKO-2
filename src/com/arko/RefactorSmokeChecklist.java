package com.arko;

/**
 * Manual smoke checks after navigation/session refactors (run the app from {@link com.arko.view.MainAppShell}):
 * <ul>
 *   <li>Login as admin and as staff — correct dashboard card appears; single window.</li>
 *   <li>Forced password change — modal dialog; after save, dashboard shows; shell stays open.</li>
 *   <li>Admin sidebar: switch Profile / Vessels / Users / Stations / Reports; logout returns to login card.</li>
 *   <li>Staff: hamburger sidebar; Dashboard / Profile / Reports; map refreshes after trip actions.</li>
 *   <li>Reports: station filter and period navigation; toggle Trip Manifest — filters stay in sync.</li>
 *   <li>Logout — login fields clear; re-login does not leak prior session or report filters.</li>
 * </ul>
 */
public final class RefactorSmokeChecklist {
    private RefactorSmokeChecklist() {}
}
