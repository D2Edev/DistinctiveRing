package io.github.d2edev.distinctivering.logic;

import android.app.Application;

/**
 * Created by d2e on 13.07.16.
 */
public class MyApp extends Application {
    private boolean splashShown;

    public boolean isSplashShown() {
        return splashShown;
    }

    public void setSplashShown(boolean splashShown) {
        this.splashShown = splashShown;
    }
}
