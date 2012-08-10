package org.azavea.otm;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.azavea.otm.data.Species;
import org.azavea.otm.data.SpeciesContainer;
import org.azavea.otm.filters.BooleanFilter;
import org.azavea.otm.filters.BaseFilter;
import org.azavea.otm.filters.SpeciesFilter;
import org.azavea.otm.filters.RangeFilter;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.ContainerRestHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.View;


public class FilterManager {
	private Context context;
	private RequestGenerator request = new RequestGenerator();
	
	// All filters loaded from configuration file, set with the latest state
	private  LinkedHashMap<String, BaseFilter> allFilters = new LinkedHashMap<String, BaseFilter>();
	
	// List of all species received from the API
	private LinkedHashMap<Integer,Species> species = new LinkedHashMap<Integer,Species>();
	
	
	public FilterManager(Context context) throws Exception {
		this.context = context;
		loadSpeciesList();
		loadFilterDefinitions();
	}
	
	public void loadSpeciesList() {} {
		loadSpeciesList(null);
	}
	
	public void loadSpeciesList(final Callback callback) {
		Log.d(App.LOG_TAG, "Species requested");
		
		// If species were already lazy loaded, return immediately
    	if (species.size() > 0 && callback != null) {
    		Log.d(App.LOG_TAG, "Species list already loaded");
    		handleSpeciesCallback(callback, true);
    		return;
    	}
    	
		request.getAllSpecies(new ContainerRestHandler<SpeciesContainer>(new SpeciesContainer()) {

			@Override
			public void dataReceived(SpeciesContainer container) {
				species = container.getAll();
				Log.d(App.LOG_TAG, "Species received: " + species.size());
				if (callback != null) {
					handleSpeciesCallback(callback, true);
				}
			}		
			
			@Override
			public void onFailure(Throwable e, String message){
				Log.e(App.LOG_TAG, message, e);
				if (callback != null) {
					handleSpeciesCallback(callback, false);
				}
			}
		});
	}
	
	private void handleSpeciesCallback(Callback callback, boolean success) {
		Message resultMessage = new Message();
    	Bundle data = new Bundle();
    	data.putBoolean("success", success);
		resultMessage.setData(data);
		callback.handleMessage(resultMessage);
	}
	
	private BaseFilter makeMapFilter(String key, String name, String type) 
			throws Exception {
		
		if (type.equals("OTMBoolFilter")) {
			return new BooleanFilter(key, name);
		} else if (type.equals("OTMRangeFilter")) {
			return new RangeFilter(key, name);
		} else if (type.equals("OTMListFilter")) {
			return new SpeciesFilter(key, name);
		}
		else {
			throw new Exception("Invalid filter type defined in config: " + type);
		}
	}
	
	private void loadFilterDefinitions() throws Exception {
		// Load the filter definitions from the included XML resource, and parse them 
		// into filter objects
		InputStream filterFile = context.getResources().openRawResource(R.raw.filters);
		try {
			DocumentBuilder xml = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xml.parse(filterFile);
			NodeList filters = doc.getElementsByTagName("filter");
			for (int i=0; i < filters.getLength(); i++) {
				Node filter = filters.item(i);
				String key = filter.getAttributes().getNamedItem("key").getNodeValue();
				String name = filter.getAttributes().getNamedItem("name").getNodeValue();
				String type = filter.getAttributes().getNamedItem("type").getNodeValue();
				allFilters.put(key, makeMapFilter(key, name, type));
			}
		} catch (Exception e) {
			throw new Exception("Invalid filter xml file", e);
		}
	}
	
	/**
	 * All species objects indexed by their id.
	 */
	public LinkedHashMap<Integer,Species> getSpecies() {
		return species;
	}
	
	public Species getSpecieById(int id) {
		return species.get(id);
	}
	
	public BaseFilter getFilter(String key) {
		return allFilters.get(key);
	}
	
	public LinkedHashMap<String,BaseFilter> getFilters() {
		return allFilters;
	}
	
	/**
	 * Reset all active filters to their default state
	 */
	public void clearActiveFilters() {
		for (LinkedHashMap.Entry<String,BaseFilter> entry : allFilters.entrySet()) {
			entry.getValue().clear();
		}
	}
	
	/**
	 * Update the values of a given filter from a filter view control
	 * @param key - The filter key
	 * @param view - The view which contains value for the filter
	 */
	public void updateFilterFromView(String key, View view) {
		allFilters.get(key).updateFromView(view);
	}
	
	/**
	 * Returns a comma separated string of active filter names
	 * 
	 */
	public String getActiveFilterDisplay() {
		String display = "", sep = "";
		for(Map.Entry<String, BaseFilter> entry : allFilters.entrySet()) {
			BaseFilter filter = entry.getValue();
			if (filter.isActive()) {
				display += sep + filter.displayName;
				sep = ", ";
			}
		}
		return display;		
	}
	
	/**
	 * Get a string of key=value parameters for use in a query string.
	 * If there are no active filters, it will return an empty string
	 * so the call can be appended without checking if there are filters
	 */
	public String getActiveFiltersAsQueryString() {
		String query = "", sep = "";
		for(Map.Entry<String, BaseFilter> entry : allFilters.entrySet()) {
			BaseFilter filter = entry.getValue();
			if (filter.isActive()) {
				query += sep + filter.toQueryStringParam();
				sep = "&";
			}
		}
		return query;
	}
	
}
	