package org.azavea.map;
/*
import java.util.Timer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

// Adapted from http://bricolsoftconsulting.com/2011/10/31/extending-mapview-to-add-a-change-event/
public class OTMMapView extends MapView {
	// ------------------------------------------------------------------------
    // LISTENER DEFINITIONS
    // ------------------------------------------------------------------------
 
    // Change listener
    public interface OnChangeListener
    {
        public void onChange(MapView view, int newZoom, int oldZoom);
    }
	
    private OTMMapView mThis;
    private long mEventsTimeout = 250L;     // Set this variable to your preferred timeout
    private boolean mIsTouched = false;
    private int mLastZoomLevel;
    private OTMMapView.OnChangeListener mChangeListener = null;
	
	
	// ------------------------------------------------------------------------
    // RUNNABLES
    // ------------------------------------------------------------------------
	private Runnable mOnChangeTask = new Runnable()
	{
		@Override
		public void run()
		{
			if (mChangeListener != null) mChangeListener.onChange(mThis, getZoomLevel(), mLastZoomLevel);
			mLastZoomLevel = getZoomLevel();	
		}
	};
	
	// ------------------------------------------------------------------------
	// CONSTRUCTORS
	// ------------------------------------------------------------------------
	public OTMMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public OTMMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public OTMMapView(Context context, String apiKey) {
		super(context, apiKey);
	}
	
	// ------------------------------------------------------------------------
    // GETTERS / SETTERS
    // ------------------------------------------------------------------------
    public void setOnChangeListener(OTMMapView.OnChangeListener l)
    {
        mChangeListener = l;
    }
 
    // ------------------------------------------------------------------------
    // EVENT HANDLERS
    // ------------------------------------------------------------------------
    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        // Set touch internal
        mIsTouched = (ev.getAction() != MotionEvent.ACTION_UP);
 
        return super.onTouchEvent(ev);
    }
 
    @Override
    public void computeScroll()
    {
        super.computeScroll();
 
        // Check for change
        if (isZoomChange())
        {
            // If computeScroll called before timer counts down we should drop it and
            // start counter over again
            resetMapChangeTimer();
        }
    }
 
    // ------------------------------------------------------------------------
    // TIMER RESETS
    // ------------------------------------------------------------------------
 
    private void resetMapChangeTimer()
    {
        OTMMapView.this.removeCallbacks(mOnChangeTask);
        OTMMapView.this.postDelayed(mOnChangeTask, mEventsTimeout);
    }
 
    // ------------------------------------------------------------------------
    // CHANGE FUNCTIONS
    // ------------------------------------------------------------------------
 
    private boolean isZoomChange()
    {
        return (getZoomLevel() != mLastZoomLevel);
    }
}
*/