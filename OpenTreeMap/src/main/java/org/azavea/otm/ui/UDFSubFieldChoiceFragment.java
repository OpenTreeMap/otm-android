package org.azavea.otm.ui;

import android.os.Bundle;

import org.azavea.helpers.JSONHelper;
import org.azavea.otm.Choice;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link UDFSubFieldChoiceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UDFSubFieldChoiceFragment extends UDFChoiceFragment {

    private static final String NAME = "name";

    private String fieldName;

    /**
     * Creates a Fragment for showing choice values for a field in a collection UDF
     *
     * @param fieldDefinition The element of the collection UDF's datatype list
     *                        to use when rendering this fragment
     * @return A new instance of fragment UDFSubFieldChoiceFragment.
     */
    public static UDFSubFieldChoiceFragment newInstance(JSONObject fieldDefinition) {
        UDFSubFieldChoiceFragment fragment = new UDFSubFieldChoiceFragment();
        List<String> choiceValues = JSONHelper.jsonStringArrayToList(fieldDefinition.optJSONArray("choices"));
        ArrayList<Choice> choices = newArrayList(transform(choiceValues, c -> new Choice(c, c)));
        Bundle args = new Bundle();
        args.putSerializable(CHOICES, choices);
        args.putString(NAME, fieldDefinition.optString("name"));
        fragment.setArguments(args);
        return fragment;
    }

    public UDFSubFieldChoiceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fieldName = getArguments().getString(NAME);
            changeListener.setActionBarTitle(fieldName);
        }
    }

    @Override
    protected void callListener(Choice choice) {
        changeListener.onValueChanged(fieldName, choice.getValue());
    }
}
