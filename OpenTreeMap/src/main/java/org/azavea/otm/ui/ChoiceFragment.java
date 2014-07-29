package org.azavea.otm.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.azavea.helpers.JSONHelper;
import org.azavea.helpers.UDFCollectionHelper;
import org.azavea.otm.Choice;
import org.azavea.otm.R;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChoiceFragment.Listener} interface
 * to handle interaction events.
 * Use the {@link ChoiceFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ChoiceFragment extends Fragment {

    private static final String CHOICES = "choices";

    private List<Choice> choices;
    private Listener listener;

    /**
     * Creates a Fragment for showing choice values for a field in a collection UDF
     *
     * @param fieldDefinition The element of the collection UDF's datatype list
     *                        to use when rendering this fragment
     * @return A new instance of fragment UDFCollectionChoiceField.
     */
    public static ChoiceFragment newInstance(JSONObject fieldDefinition) {
        List<String> choiceValues = JSONHelper.jsonStringArrayToList(fieldDefinition.optJSONArray("choices"));
        ArrayList<Choice> choices = newArrayList(transform(choiceValues, v -> new Choice(v, v)));
        return newInstance(choices);
    }


    public static ChoiceFragment newInstance(ArrayList<Choice> choices) {
        ChoiceFragment fragment = new ChoiceFragment();
        Bundle args = new Bundle();
        args.putSerializable(CHOICES, choices);
        fragment.setArguments(args);
        return fragment;
    }

    public ChoiceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            choices = (List<Choice>) getArguments().getSerializable(CHOICES);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View layout = inflater.inflate(R.layout.collection_udf_add_choice, container, false);

        List<String> choiceLabels = transform(choices, Choice::getText);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.simple_list_item, choiceLabels);

        final ListView list = (ListView) layout.findViewById(R.id.udf_choices);
        list.setAdapter(adapter);
        list.setOnItemClickListener((listView, rowView, pos, id) -> {
            if (listener != null) {
                listener.onChoiceSelected(choices.get(pos).getValue());
            }
        });

        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface Listener {
        public void onChoiceSelected(String choiceValue);
    }

    private static LinkedHashMap<String, JSONObject> groupUdfDefinitionsByKey(final List<JSONObject> udfDefinitions) {
        final LinkedHashMap<String, JSONObject> udfDefMap = new LinkedHashMap<>(udfDefinitions.size());
        for (JSONObject udfDef : udfDefinitions) {
            if (!udfDef.isNull(UDFCollectionHelper.COLLECTION_KEY)) {
                final String udfDefKey = udfDef.optString(UDFCollectionHelper.COLLECTION_KEY);
                udfDefMap.put(udfDefKey, udfDef);
            }
        }
        return udfDefMap;
    }
}
