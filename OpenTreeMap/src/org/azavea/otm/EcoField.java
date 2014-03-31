package org.azavea.otm;

import java.text.NumberFormat;
import org.azavea.otm.data.Plot;
import org.json.JSONException;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class EcoField extends Field {

	private static final String VALUE_KEY = ".value";
	private static final String UNIT_KEY = ".unit";
	private static final String AMOUNT_KEY = ".dollars";
	
	// Eco fields are calculated, not edited, so they have much less
	// information in their definition.
	protected EcoField(String key, String label, boolean canEdit, 
			String keyboard, String format, String type)	{
		super(key, label, canEdit, keyboard, format, type, null, null, null, false, "", 0);
	}
	
	@Override
	public View renderForDisplay(LayoutInflater layout, Plot model, Context context) 
			throws JSONException {
		View container = layout.inflate(R.layout.plot_ecofield_row, null);
        ((TextView)container.findViewById(R.id.field_label)).setText(this.label);
        
        // Extract the value of this type of eco benefit
        Object value = getValueForKey(this.key + VALUE_KEY, model.getData());
        Object units = getValueForKey(this.key + UNIT_KEY, model.getData());
        
        NumberFormat currency = NumberFormat.getCurrencyInstance(App.getFieldManager().getLocale());
        currency.setMaximumFractionDigits(2);
        
        if (value != null) {
            String valueTruncated = String.format("%.1f", value);
            
        	((TextView)container.findViewById(R.id.field_value))
	        	.setText(valueTruncated + " " + units);
	        
	        // The dollar amount of the benefit
	        Double amount = (Double)getValueForKey(this.key + AMOUNT_KEY, model.getData());
	        ((TextView)container.findViewById(R.id.field_money))
	        	.setText(currency.format(amount));
        } else {
        	Double amount = 0.0;
            ((TextView)container.findViewById(R.id.field_money))
        	.setText(currency.format(amount));
        }
        return container;
	}
}
