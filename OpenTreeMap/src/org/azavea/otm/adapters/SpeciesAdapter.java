package org.azavea.otm.adapters;

import org.azavea.otm.data.Species;
import org.azavea.otm.R;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SpeciesAdapter extends ArrayAdapter<Species>{

    Context context; 
    int layoutResourceId;    
    Species data[] = null;
    
    public SpeciesAdapter(Context context, int layoutResourceId, Species[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SpeciesHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new SpeciesHolder();
            
            holder.commonName = (TextView)row.findViewById(R.id.common_name);
            holder.scientificName = (TextView)row.findViewById(R.id.sci_name);
            
            row.setTag(holder);
        }
        else
        {
            holder = (SpeciesHolder)row.getTag();
        }
        
        // Use getItem as it's reference is always pointed to the data list
        // which is active.  When filtering is applied, data is not active, 
        // so data[position] will get the position from the unfiltered list.
        Species species = getItem(position);
        try {
			holder.commonName.setText(species.getCommonName());
			holder.scientificName.setText(species.getScientificName() + "asdf");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return row;
    }
    
    static class SpeciesHolder
    {
    	TextView commonName;
        TextView scientificName;
    }
}