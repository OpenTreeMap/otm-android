package org.azavea.otm;

import java.io.InputStream;
import java.util.ArrayList;

import org.azavea.otm.data.Species;
import org.azavea.otm.data.SpeciesContainer;
import org.azavea.otm.filters.BooleanFilter;
import org.azavea.otm.filters.MapFilter;
import org.azavea.otm.filters.RangeFilter;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.ContainerRestHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import android.content.Context;


public class SearchManager {
	private MapFilter[] activeFilters = new MapFilter[0];
	private Context context;
	private RequestGenerator request = new RequestGenerator();
	
	public Species[] species;
	public MapFilter[] availableFilters;
	
	public SearchManager(Context context) throws Exception {
		this.context = context;
		loadSpeciesList();
		loadFilterDefinitions();
	}
	
	private void loadSpeciesList() {
		request.getAllSpecies(new ContainerRestHandler<SpeciesContainer>(new SpeciesContainer()) {
			@Override
			public void dataReceived(SpeciesContainer container) {
				species = container.getAll();
			}			
		});
	}
	
	private MapFilter makeMapFilter(String key, String name, String type) 
			throws Exception {
		if (type.equals("OTMBoolFilter")) {
			return new BooleanFilter(key, name);
		} else if (type.equals("OTMRangeFilter")) {
			return new RangeFilter(key, name);
		} else {
			throw new Exception("Invalid filter type defined in config: " + type);
		}
	}
	
	private void loadFilterDefinitions() throws Exception {
		// Load the filter definitions from the included XML resource, and parse them 
		// into filter objects
		InputStream filterFile = context.getResources().openRawResource(R.raw.filters);
		ArrayList<MapFilter> filterList = new ArrayList<MapFilter>();
		try {
			DocumentBuilder xml = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xml.parse(filterFile);
			NodeList filters = doc.getElementsByTagName("filter");
			for (int i=0; i < filters.getLength(); i++) {
				Node filter = filters.item(i);
				String key = filter.getAttributes().getNamedItem("key").getNodeValue();
				String name = filter.getAttributes().getNamedItem("name").getNodeValue();
				String type = filter.getAttributes().getNamedItem("type").getNodeValue();
				filterList.add(makeMapFilter(key, name, type));
			}
			availableFilters = filterList.toArray(new MapFilter[filterList.size()]);
			
		} catch (Exception e) {
			throw new Exception("Invalid filter xml file", e);
		}
	}
	
	public void clearFilters() {
		activeFilters = new MapFilter[0];
	}
	
	public MapFilter[] getActiveFilters() {
		return activeFilters;
	}
	
	/**
	 * Returns a comma separated string of active filter names
	 * 
	 */
	public String getActiveFilterDisplay() {
		String display = "", sep = "";
		for(MapFilter filter : activeFilters) {
			display += sep + filter.displayName;
			sep = ", ";
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
		for(MapFilter filter : activeFilters) {
			query += sep + filter.toQueryStringParam();
			sep = "&";
		}
		return query;
	}
	
}
	