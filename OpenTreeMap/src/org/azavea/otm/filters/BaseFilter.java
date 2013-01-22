package org.azavea.otm.filters;

import com.loopj.android.http.RequestParams;

import android.view.View;

public abstract class BaseFilter {
	/**
	 * The key for the filter which is used as a query string argument
	 */
	public String key;
	
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
	 * Return a string for this filter to be used in a URL query string
	 * parameter
	 */
	public abstract String toQueryStringParam();

	/**
	 *  Reset the filter to the default state
	 */
	public abstract void clear();

	// Add this filter to a RequestParams object.
	public abstract void addToRequestParams(RequestParams rp);
}
