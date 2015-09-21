package net.husht.searchcities;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

/**
 * Created by tom on 9/21/15.
 *
 * DelayedAutoCompleteTextView: Subclass of AutoCompleteTextView able to delay autocomplete actions
 * in order to limit API calls.
 * You can also set a ProgressBar to give UI feedback when the filtering is performed.
 */

public class DelayedAutoCompleteTextView extends AutoCompleteTextView {

    private static final String TAG = "DelayedACTextView";
    private static final int AUTOCOMPLETE_DELAY = 500;

    private ProgressBar mLoadingIndicator;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private int mDelay = AUTOCOMPLETE_DELAY;


    /**
     * Constructors
     */

    public DelayedAutoCompleteTextView(Context context) {
        super(context);
    }

    public DelayedAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DelayedAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DelayedAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Configure the delay before filtering is performed after user input
     */

    public int getDelay() {
        return mDelay;
    }

    public void setDelay(int delay) {
        mDelay = delay;
    }

    /**
     * Set a ProgressBar that will be used to indicate a work in progress when filtering is performed
     */

    public void setLoadingIndicator(ProgressBar loadingIndicator) {
        mLoadingIndicator = loadingIndicator;
    }


    /**
     * Override filtering methods to show/hide loading indicator and delay filtering actions
     */

    @Override
    protected void performFiltering(final CharSequence text, final int keyCode) {
        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        mRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Perform search !");
                if (mLoadingIndicator != null) {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                }
                DelayedAutoCompleteTextView.super.performFiltering(text, keyCode);
            }
        };
        mHandler.postDelayed(mRunnable, mDelay);
    }

    @Override
    public void onFilterComplete(int count) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
        }
        super.onFilterComplete(count);
    }
}
