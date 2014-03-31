//package org.azavea.otm.filters;
//
//import org.azavea.otm.App;
//import org.azavea.otm.Choices;
//
//import android.view.View;
//
//import com.loopj.android.http.RequestParams;
//
//public class ChoiceFilter extends BaseFilter{
//	public Choices choices;
//	private int DEFAULT = -1;
//	private int selectedIndex = DEFAULT;
//	
//	public ChoiceFilter(String cqlKey, String nearestPlotKey, String label, String choiceKey) {
//		this.cqlKey = cqlKey;
//		this.nearestPlotKey = nearestPlotKey;
//		this.label = label;
//		this.choices = null; //App.getFieldManager().getChoicesByName(choiceKey);
//	}
//	
//	
//	@Override
//	public boolean isActive() {
//		return selectedIndex > DEFAULT; 
//	}
//
//	@Override
//	public void updateFromView(View view) {}
//	
//
//	@Override
//	public void clear() {
//		this.selectedIndex = DEFAULT;
//	}
//
//	@Override
//	public void addToCqlRequestParams(RequestParams rp) {
//		// Id of the choice at selected index
//		int value = choices.getValues().get(selectedIndex);
//		String typeValue = String.valueOf(value);
//		if (this.choices.getValueType().equals("bool")) {
//			typeValue = String.valueOf(value == 1);
//		}
//		// API expects <key>_<value>=1: pests_1=1, pests_2=1
//		rp.put(cqlKey + "_" + value, typeValue);
//		
//	}
//
//	public Integer getSelectedIndex() {
//		return this.selectedIndex;
//	}
//	
//	public void setSelectedIndex(Integer index) {
//		selectedIndex = index;
//	}
//	
//	public String getSelectedValueText() {
//		if (isActive()) {
//			return this.choices.getItems().get(this.selectedIndex);
//		} else { 
//			return "";
//		}
//	}
//	
//	public String getSelectedLabelText() {
//		String labelText = this.label;
//		if (isActive()) {
//			labelText += ": " + getSelectedValueText();
//		}
//		return labelText;
//	}
//
//
//	@Override
//	public void addToNearestPlotRequestParams(RequestParams rp) {
//		//TODO, I don't think the NP end point supports this filter!
//	}
//
//}
