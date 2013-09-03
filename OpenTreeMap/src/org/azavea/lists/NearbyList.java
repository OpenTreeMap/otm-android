package org.azavea.lists;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import org.azavea.lists.data.DisplayablePlot;
import org.azavea.otm.App;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.PlotContainer;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.ContainerRestHandler;
import org.azavea.otm.ui.MainMapActivity;
import org.azavea.otm.ui.TreeInfoDisplay;
import org.json.JSONException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class NearbyList implements InfoList {
	private PlotContainer nearbyPlots;
	private double lat;
	private double lon;
	private ArrayList<ListObserver> observers = new ArrayList<ListObserver>();
	private LocationManager locationManager;
	private boolean filterRecent;
	private boolean filterPending;
	
	public NearbyList() {
		lat = App.getStartPos().latitude;
		lon = App.getStartPos().longitude;
		filterRecent = false;
		filterPending = false;
	}
	
	public NearbyList(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
		filterRecent = false;
		filterPending = false;
	}
	
	public void setupLocationUpdating(Context applicationContext) {
		locationManager = (LocationManager) applicationContext.getSystemService(Context.LOCATION_SERVICE);
		
		LocationListener locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				lat = location.getLatitude();
				lon = location.getLongitude();
				update();
			}
			
			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
				
			}
		};
		if (locationManager != null) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
			setInitialLocation();
		}
		
	}
	
	public void setFilterRecent(boolean filterRecent) {
		this.filterRecent = filterRecent;
	}
	
	public void setFilterPending(boolean filterPending) {
		this.filterPending = filterPending;
	}
	
	@Override
	public DisplayablePlot[] getDisplayValues() {
		ArrayList<DisplayablePlot> listValues = new ArrayList<DisplayablePlot>();
		String mainInfo = "";
		String supplementaryInfo = "";
		String distance = "";
		
		try {
			if (nearbyPlots != null) {
				Map<Integer, Plot> plotObjects = nearbyPlots.getAll();
				for(Plot p : plotObjects.values()) {
					if (p.getTree() != null) {
						mainInfo = getSpecies(p);
						
						supplementaryInfo = getDiameter(p);
					} else {
						mainInfo = "Unassigned plot";
						
						supplementaryInfo = getPlotId(p);
					}
					distance = getDisplayDistance(p);
					listValues.add(new DisplayablePlot(p, mainInfo + ", " + supplementaryInfo + ", " + distance));
				}
			}
		} catch (JSONException e) {
			Log.d(App.LOG_TAG, "JSONException e: " + e.getMessage());
		}
		
		return listValues.toArray(new DisplayablePlot[0]);
	}
	
	private void setInitialLocation() {
		Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_FINE);
		String provider = locationManager.getBestProvider(crit, true);
		
		if (provider != null) {
			Location loc = locationManager.getLastKnownLocation(provider);
			if (loc != null) {
				lat = loc.getLatitude();
				lon = loc.getLongitude();
				update();
			}
		}
		
	}
	
	public void update() {
		RequestGenerator rg = new RequestGenerator();
		rg.getPlotsNearLocation(lat, lon, filterRecent, filterPending, new ContainerRestHandler<PlotContainer>(new PlotContainer()) {
			@Override
			public void dataReceived(PlotContainer responseObject) {
				super.dataReceived(responseObject);
				nearbyPlots = responseObject;
				notifyObservers();
			}
		});
	}
	
	private void notifyObservers() {
		for(ListObserver o : observers) {
			o.update();
		}
	}
	
	private String getDisplayDistance(Plot p) {
		String distance;
		float dist = getDistanceFromMyLocation(p);  // this is always in meters.
		Locale locale = Locale.getDefault();

		if (dist == -1.0) {
			distance = "Distance unknown";
		} else if (locale != null && locale.equals(Locale.US)) {
			float feet = (float) (dist * 3.38084);
			distance = Math.round(feet) + "ft";
		} else {
			distance = Math.round(dist) + "m";			
		}
		return distance;
	}

	private String getPlotId(Plot p) {
		String supplementaryInfo;
		try {
			supplementaryInfo = Integer.toString(p.getId());
		} catch (Exception e) {
			supplementaryInfo = "Missing ID";
		}
		return supplementaryInfo;
	}

	private String getDiameter(Plot p) {
		Locale l = Locale.getDefault();
		String unitString;
		if (l != null && l.equals(Locale.US)) {
			unitString = " in. Diameter";
		} else {
			unitString = " m. Diameter";
		}
		String diameter;
		try {
			diameter = p.getTree().getDbh()+ unitString;
		} catch (Exception e) {
			diameter = "Diameter missing";
		}
		return diameter;
	}

	private String getSpecies(Plot p) {
		String species;
		String speciesDefault = "Species missing";
		try {
			species = p.getTree().getSpeciesName();
			if (species == null) {
				species = speciesDefault;
			}
		} catch (Exception e) {
			species = speciesDefault;
		}
		return species;
	}

	@Override
	public void addObserver(ListObserver observer) {
		this.observers.add(observer);
	}

	@Override
	public Object[] getListValues() {
		Object[] plots = null;
		try {
			Map<Integer, Plot> plotMap = nearbyPlots.getAll();
			plots = plotMap.values().toArray();
			Log.d(App.LOG_TAG, "Number of list-values: " + plots.length);
		} catch (JSONException e) {
			return null;			
		}
		
		return plots;
	}
	
	private float getDistanceFromMyLocation(Plot p) {
		float[] distance = new float[1];
		
		try {
			double plotLat = p.getGeometry().getLat();
			double plotLon = p.getGeometry().getLon();
			Location.distanceBetween(lat, lon, plotLat, plotLon, distance);
			return distance[0];
		} catch (Exception e) {
			return -1;
		}
	}
}
