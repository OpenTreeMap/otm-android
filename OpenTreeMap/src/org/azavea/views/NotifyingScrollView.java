package org.azavea.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

public class NotifyingScrollView extends ScrollView {

	private OnScrollToBottomListener bottomListener = null;
	
	public interface OnScrollToBottomListener {
		void OnScrollToBottom();
	}
	public NotifyingScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setOnScrollToBottomListener(OnScrollToBottomListener listener) {
	        bottomListener = listener;
	}
	   
	public void onScrollToBottom() {}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) 
	{
	        // Grab the last child placed in the ScrollView, we need it to determinate the bottom position.
	        View view = (View) getChildAt(getChildCount()-1);
	        
	        // Calculate the scrolldiff
	        int diff = (view.getBottom()-(getHeight()+getScrollY()));
	        
	        if( diff == 0 )
	        {
	        	if (bottomListener != null) {
	        		bottomListener.OnScrollToBottom();
	        	}
	        }
	        
	        super.onScrollChanged(l, t, oldl, oldt);
	        this.onScrollToBottom();
	}
}
