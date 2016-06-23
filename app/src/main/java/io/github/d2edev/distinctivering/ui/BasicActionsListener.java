package io.github.d2edev.distinctivering.ui;

/**
 * Created by d2e on 23.06.16.
 * interface to pass to MainActivity requests for UI actions activity has to process:
 *
 */

public interface BasicActionsListener {
    void callDeleteUI();
    void callAddUI();
    void callMainUI();
}
