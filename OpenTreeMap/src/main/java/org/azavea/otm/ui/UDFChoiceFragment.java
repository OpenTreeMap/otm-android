package org.azavea.otm.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.azavea.otm.Choice;
import org.azavea.otm.R;

import java.util.List;

import static com.google.common.collect.Lists.transform;


public abstract class UDFChoiceFragment extends Fragment {

    protected static final String CHOICES = "choices";
    protected UDFCollectionActivity changeListener;

    private List<Choice> choices;
    private int currentRow = -1;

    public UDFChoiceFragment() {
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
            currentRow = pos;
            setSelectedStyle(list);
            if (changeListener != null) {
                callListener(choices.get(pos));
            }
        });

        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            changeListener = (UDFCollectionActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must be UDFCollectionCreateActivity");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        changeListener = null;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden && currentRow != -1 && changeListener != null) {
            callListener(choices.get(currentRow));
        }
    }

    protected abstract void callListener(Choice choice);

    public void setSelectedStyle(ListView list) {
        for (int i = list.getFirstVisiblePosition(); i <= list.getLastVisiblePosition(); i++) {
            View row = list.getChildAt(i);
            row.setBackgroundResource(R.drawable.list_element_border);
        }
        View selectedRow = list.getChildAt(currentRow);
        if (selectedRow != null) {
            selectedRow.setBackgroundResource(R.drawable.list_header_border);
        }
    }
}
