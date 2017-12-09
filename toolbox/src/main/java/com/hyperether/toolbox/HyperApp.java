package com.hyperether.toolbox;

import android.app.Application;

/**
 * Class for managing application related behavior and providing context
 *
 * @author Slobodan Prijic
 * @version 1.0 - 12/09/2017
 */
public class HyperApp extends Application {

    private static HyperApp instance;

    public static synchronized HyperApp getInstance() {
        if (instance == null) {
            instance = new HyperApp();
        }
        return instance;
    }
}
