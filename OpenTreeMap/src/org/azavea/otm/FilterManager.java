package org.azavea.otm;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.azavea.otm.data.Species;
import org.azavea.otm.data.SpeciesContainer;
import org.azavea.otm.filters.BooleanFilter;
import org.azavea.otm.filters.BaseFilter;
import org.azavea.otm.filters.ChoiceFilter;
import org.azavea.otm.filters.SpeciesFilter;
import org.azavea.otm.filters.RangeFilter;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.ContainerRestHandler;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.loopj.android.http.RequestParams;

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
		Log.d(App.LOG_TAG, "Creating new instance of Filter Manager");
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
				try {
					species = (LinkedHashMap<Integer, Species>) container.getAll();
					Log.d(App.LOG_TAG, "Species received: " + species.size());
					if (callback != null) {
						handleSpeciesCallback(callback, true);
					}
				} catch (JSONException e) {
					//TODO: do we need to do something to notify the user that species weren't retrieved?
					Log.e(App.LOG_TAG, "Error in Species retrieval: " + e.getMessage());
					e.printStackTrace();
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
	
	private BaseFilter makeMapFilter(String key, String name, String type, 
			String trueval, String choice) 
			throws Exception {
		
		if (type.equals("OTMBoolFilter")) {
			return new BooleanFilter(key, name, trueval);
		} else if (type.equals("OTMRangeFilter")) {
			return new RangeFilter(key, name);
		} else if (type.equals("OTMListFilter")) {
			return new SpeciesFilter(key, name);
		} else if (type.equals("OTMChoiceFilter")) {
			return new ChoiceFilter(key, name, choice);
		}
		else {
			throw new Exception("Invalid filter type defined in config: " + type);
		}
	}
	
	private void loadFilterDefinitions() throws Exception {
		// Load the filter definitions from the included XML resource, and parse them 
		// into filter objects
		InputStream filterFile = context.getResources().openRawResource(R.raw.configuration);
		try {
			DocumentBuilder xml = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xml.parse(filterFile);
			NodeList filters = doc.getElementsByTagName("filter");
			for (int i=0; i < filters.getLength(); i++) {
				Node filter = filters.item(i);
				NamedNodeMap attributes = filter.getAttributes();
				String key = attributes.getNamedItem("key").getNodeValue();
				String name = attributes.getNamedItem("name").getNodeValue();
				String type = attributes.getNamedItem("type").getNodeValue();
				
				Node choiceNode = attributes.getNamedItem("choice");
				String choice = "";
				if (choiceNode != null) {
					choice = choiceNode.getNodeValue();
				}
				
				Node truevalNode = attributes.getNamedItem("trueval");
				String trueval = "";
				if (truevalNode != null) {
					trueval = truevalNode.getNodeValue();
				}
				
				allFilters.put(key, makeMapFilter(key, name, type, trueval, choice));
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
				display += sep + filter.label;
				sep = ", ";
			}
		}
		return display;		
	}
	
	
	/**
	 * Returns a RequestParams object loaded with the filter values.
	 */
	public RequestParams getActiveFiltersAsRequestParams() {
		RequestParams rp = new RequestParams();
		for(Map.Entry<String, BaseFilter> entry : allFilters.entrySet()) {
			BaseFilter filter = entry.getValue();
			if (filter.isActive()) {
				filter.addToRequestParams(rp);
			}
		}
		return rp;
		
	}
}
	