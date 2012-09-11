package org.azavea.otm;

import java.text.NumberFormat;

import org.azavea.otm.data.Model;
import org.json.JSONException;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class EcoField extends Field {

	private static final String VALUE_KEY = ".value";
	private static final String UNIT_KEY = ".unit";
	private static final String AMOUNT_KEY = ".dollars";
	
	protected EcoField(String key, String label, int minimumToEdit, 
			String keyboard, String format, String type)	{
		super(key, label, minimumToEdit, keyboard, format, type);
	}
	
	@Override
	public View renderForDisplay(LayoutInflater layout, Model model) 
			throws JSONException {
		View container = layout.inflate(R.layout.plot_ecofield_row, null);
        ((TextView)container.findViewById(R.id.field_label)).setText(this.label);
        
        // Extract the value of this type of eco benefit
        String value = getValueForKey(this.key + VALUE_KEY, model.getData()).toString();
        String units = getValueForKey(this.key + UNIT_KEY, model.getData()).toString();
        ((TextView)container.findViewById(R.id.field_value))
        	.setText(value + " " + units);
        
        // The dollar amount of the benefit
        NumberFormat currency = NumberFormat.getCurrencyInstance();
        Double amount = (Double)getValueForKey(this.key + AMOUNT_KEY, model.getData());
        ((TextView)container.findViewById(R.id.field_money))
        	.setText(currency.format(amount));
    	
        return container;
	}
}