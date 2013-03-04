package org.azavea.otm.filters;

import com.loopj.android.http.RequestParams;

import android.view.View;

public abstract class BaseFilter {
	/**
	 * The key for the filter which is used as a query string argument
	 * (Modulo the 2 end points: CQL and nearest plot.)
	 */
	public String cqlKey;
	public String nearestPlotKey;
	
	/**
	 * The name to display on the filter bar when active
	 */
	public String displayName;
	
	/**
	 * The name to display as a filter label
	 */
	public String label;
	
	/**
	 *  Checks if this filter currently has an active value
	 */
	public abstract boolean isActive();
	
	/**
	 *  Update the value of the filter, and its active status
	 *  from a view of the corresponding type
	 */
	public abstract void updateFromView(View view);

	/**
	 *  Reset the filter to the default state
	 */
	public abstract void clear();

	/**
	 *  Called when the filter is active...
	 *  Add this filter to a RequestParams object configured for the CQL end point.
	 */
	public abstract void addToCqlRequestParams(RequestParams rp);

	/**
	 * Same as above method, but for the nearestPlot REST end point. 
	 */
	public abstract void addToNearestPlotRequestParams(RequestParams rp);
	
}
