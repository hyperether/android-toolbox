package com.hyperether.toolbox;

import android.content.Context;

/**
 * Class for creating toolbox configuration builder
 * <p>
 * Created by Slobodan on 12/11/2017.
 */

public class HyperConfig {

    private HyperConfig(Builder builder, Context context) {
        HyperApp.getInstance().setContext(context);
        HyperApp.getInstance().setDebugActive(builder.debug);
    }

    public static class Builder {

        private boolean debug = false;

        public HyperConfig build(Context context) {
            return new HyperConfig(this, context);
        }

        /**
         * If not set default value will be false
         *
         * @param debug If is true {@link HyperLog} is set to debug mode.
         *
         * @return builder instance
         */
        public Builder setDebug(boolean debug) {
            this.debug = debug;
            return this;
        }
    }
}
