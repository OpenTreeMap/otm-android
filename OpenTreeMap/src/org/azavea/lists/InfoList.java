package org.azavea.lists;

import org.azavea.lists.data.DisplayableModel;

import android.content.Context;

public interface InfoList {
	public DisplayableModel[] getDisplayValues();
	public Object[] getListValues();
	public void addObserver(ListObserver o);
	public void setupLocationUpdating(Context applicationContext);
    public void removeLocationUpdating();
}
