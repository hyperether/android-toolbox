package com.hyperether.toolbox;

import android.content.Context;

/**
 * Class for managing application related behavior and providing context
 *
 * @author Slobodan Prijic
 * @version 1.0 - 12/09/2017
 */
public class HyperApp {

    private static HyperApp instance;
    private Context context;
    private boolean debugActive;

    public static synchronized HyperApp getInstance() {
        if (instance == null) {
            instance = new HyperApp();
        }
        return instance;
    }

    public void setContext(Context ctxt) {
        context = ctxt;
    }

    public Context getApplicationContext() {
        return context;
    }

    public void setDebugActive(boolean debugActive) {
        this.debugActive = debugActive;
    }

    public boolean isDebugActive() {
        return debugActive;
    }
}
