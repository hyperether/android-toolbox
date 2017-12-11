package com.hyperether.toolbox;

import android.os.Build;
import android.support.annotation.AnimRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public abstract class HyperFragmentStackHandler {
    private static final String TAG = HyperFragmentStackHandler.class.getSimpleName();

    protected FragmentManager mFragmentManager;
    private String mActiveFragmentTag;
    private List<String> mFragmentStack = new ArrayList<>();

    public HyperFragmentStackHandler(AppCompatActivity act) {
        this.mFragmentManager = act.getSupportFragmentManager();
    }

    protected void clearFragmentList() {
        if (mFragmentStack != null)
            mFragmentStack.clear();
    }

    public String getActiveFragmentTag() {
        return mActiveFragmentTag;
    }

    /**
     * Method returns fragment depth counting from top of stack
     *
     * @param tag fragment tag
     *
     * @return -1 if fragment not found
     */
    protected int getFragmentBackStackDepth(String tag) {
        int position = getLatestFragmentPosition(tag);
        if (position == -1)
            return -1;
        else
            return mFragmentStack.size() - position;
    }

    /**
     * Method returns fragment position counting from top of stack
     *
     * @param tag fragment tag
     *
     * @return -1 if fragment not found
     */
    protected int getLatestFragmentPosition(String tag) {
        int position = -1;
        if (!mFragmentStack.isEmpty())
            for (int i = mFragmentStack.size() - 1; i >= 0; i--) {
                String fTag = mFragmentStack.get(i);
                if (tag.equals(fTag)) {
                    position = i;
                    break;
                }
            }
        return position;
    }

    /**
     * This method is used for placing fragment on screen Requires fragment instance. This class has
     * methods for check if fragment already exist, so this method should not be called from other
     * classes
     *
     * @param fragment
     * @param tag
     */
    protected void setFragment(Fragment fragment,
                               String tag,
                               int container,
                               @AnimRes int enter,
                               @AnimRes int exit,
                               @AnimRes int popEnter,
                               @AnimRes int popExit) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        if (enter != 0 || exit != 0 || popEnter != 0 || popExit != 0)
            fragmentTransaction.setCustomAnimations(enter, exit, popEnter, popExit);
        setFragment(fragment, tag, container, fragmentTransaction);
    }

    protected void setFragment(Fragment fragment,
                               String tag,
                               int container) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        setFragment(fragment, tag, container, fragmentTransaction);
    }

    private void setFragment(Fragment fragment,
                             String tag,
                             int container,
                             FragmentTransaction fragmentTransaction) {
        try {
            fragmentTransaction.replace(container, fragment, tag);

            if (mFragmentStack != null &&
                    mFragmentStack.size() > 0 &&
                    tag.equals(mFragmentStack.get(mFragmentStack.size() - 1))) {
                // fragment is on top of our navigation stack; do not duplicate it, just replace it
            } else {
                fragmentTransaction.addToBackStack(tag);
                mActiveFragmentTag = tag;
                mFragmentStack.add(tag);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (!mFragmentManager.isDestroyed())
                    fragmentTransaction.commit();
                else {
                    HyperLog.getInstance().e(TAG,
                            "setFragment", "fragment manager is Destroyed!");
//                    instance = null;
                }
            } else {
                fragmentTransaction.commit();
            }
        } catch (Exception e) {
            HyperLog.getInstance().e(TAG, "setFragment", e);
        }
    }

    /**
     * Check if fragment is already loaded and saved in backstack
     *
     * @param mTag fragment tag
     *
     * @return Fragment instance if exist or null
     */
    protected Fragment getFragmentFromBackStack(String mTag) {
        Fragment fragment = null;
        try {
            for (String stackTag : mFragmentStack) {
                if (stackTag.equals(mTag)) {
                    // fragment is on our navigation stack; do not duplicate it, just replace it
                    fragment = mFragmentManager.findFragmentByTag(mTag);
                    break;
                }
            }
        } catch (Exception e) {
            HyperLog.getInstance().e(TAG, "getFragmentFromBackStack", e);
        }
        return fragment;
    }

    /**
     * Remove last fragment and preview previous one
     *
     * @return new fragment tag
     */
    public String setStepBack() {
        popNavigationList(mActiveFragmentTag);
        return mActiveFragmentTag;
    }

    protected void popNavigationList(String tag) {
        if (mFragmentStack != null && mFragmentStack.size() > 0) {
            if (mFragmentStack.get(mFragmentStack.size() - 1).equals(tag)) {
                mFragmentStack.remove(mFragmentStack.size() - 1);
                if (mFragmentStack != null && mFragmentStack.size() > 0)
                    mActiveFragmentTag = mFragmentStack.get(mFragmentStack.size() - 1);
                else
                    mActiveFragmentTag = null;
            }
        }
    }

    public void clearFragmentStack() {
        removeAllFragments();
        clearFragmentBackStack();
        if (!isStackEmpty())
            mFragmentStack.clear();
    }

    public void clearFragmentBackStack() {
        int backStack = mFragmentManager.getBackStackEntryCount();
        for (int i = 0; i < backStack; i++) {
            try {
                mFragmentManager.popBackStackImmediate();
            } catch (Exception e) {
                try {

                    mFragmentManager.popBackStack();
                } catch (Exception ex) {
                    HyperLog.getInstance().e(TAG, "clearFragmentBackStack", ex);
                }
            }
        }
    }

    public boolean isStackEmpty() {
        return mFragmentStack == null || mFragmentStack.isEmpty();
    }

    public void removeAllFragments() {
        List<Fragment> list = mFragmentManager.getFragments();
        if (list != null && list.size() > 0) {
            for (int i = list.size() - 1; i >= 0; i--) {
                Fragment fragment = list.get(i);
                if (fragment != null) {
                    removeFragment(fragment);
                }
            }
        }
    }

    /**
     * This method can be called only for fragment set by setFragment method, which adds fragment in
     * back stack
     *
     * @param fragment fragment to be removed
     */
    public void removeFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction trans = mFragmentManager.beginTransaction();
            trans.remove(fragment);
            try {
                trans.commitAllowingStateLoss();
            } catch (Exception e) {
                HyperLog.getInstance().e(TAG, "removeFragment", e);

            }
            trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

            try {
                mFragmentManager.popBackStack();
            } catch (Exception e) {
                HyperLog.getInstance().e(TAG, "removeFragment", e);
            }
        }
    }

    public int getFragmentCount() {
        return mFragmentStack.size();
    }
}
