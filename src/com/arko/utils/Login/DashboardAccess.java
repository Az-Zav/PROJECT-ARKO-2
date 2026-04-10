package com.arko.utils.Login;

import com.arko.view.MainAppShell;

/**
 * Routes the authenticated user (see {@link SessionManager}) to the correct dashboard card.
 */
public class DashboardAccess {

    public static void route(MainAppShell appShell) {
        appShell.showAfterLogin();
    }
}
