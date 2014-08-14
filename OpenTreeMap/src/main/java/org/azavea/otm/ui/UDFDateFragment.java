package org.azavea.otm.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import org.azavea.otm.R;
import org.azavea.otm.fields.DateField;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link org.azavea.otm.ui.UDFDateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UDFDateFragment extends Fragment {

    private static final String NAME = "name";

    private String fieldName;
    private UDFCollectionCreateActivity changeListener;

    /**
     * Creates a Fragment for setting choice values for a field in a collection UDF
     *
     * @param fieldDefinition The element of the collection UDF's datatype list
     *                        to use when rendering this fragment
     * @return A new instance of fragment UDFDateFragment.
     */
    public static UDFDateFragment newInstance(JSONObject fieldDefinition) {
        UDFDateFragment fragment = new UDFDateFragment();
        Bundle args = new Bundle();
        args.putString(NAME, fieldDefinition.optString("name"));
        fragment.setArguments(args);
        return fragment;
    }

    public UDFDateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fieldName = getArguments().getString(NAME);
            changeListener.setActionBarTitle(fieldName);
            // Today's date is perfectly valid, so we should immediately call valueChanged
            sendCurrentDate();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View layout = inflater.inflate(R.layout.collection_udf_add_date, container, false);
        DatePicker picker = (DatePicker) layout.findViewById(R.id.udf_datepicker);
        picker.init(picker.getYear(), picker.getMonth(), picker.getDayOfMonth(), (view, year, month, dayOfMonth) -> {
            String timestamp = DateField.getTimestamp(getActivity(), year, month, dayOfMonth);
            changeListener.onValueChanged(fieldName, timestamp);
        });
        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            changeListener = (UDFCollectionCreateActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must be UDFCollectionCreateActivity");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        changeListener = null;
    }

    private void sendCurrentDate() {
        Calendar cal = new GregorianCalendar();
        String timestamp = DateField.getTimestamp(getActivity(), cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        changeListener.onValueChanged(fieldName, timestamp);
    }
}
