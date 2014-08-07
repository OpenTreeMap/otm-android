package org.azavea.otm.ui;

import android.os.Bundle;

import org.azavea.otm.Choice;

import java.util.ArrayList;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link UDFKeyChoiceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UDFKeyChoiceFragment extends UDFChoiceFragment {

    /**
     * Creates a Fragment for showing choice values for a field in a collection UDF
     *
     * @param choices The choices to show
     * @return A new instance of fragment UDFKeyChoiceFragment.
     */
    public static UDFKeyChoiceFragment newInstance(ArrayList<Choice> choices) {
        UDFKeyChoiceFragment fragment = new UDFKeyChoiceFragment();
        Bundle args = new Bundle();
        args.putSerializable(CHOICES, choices);
        fragment.setArguments(args);
        return fragment;
    }

    public UDFKeyChoiceFragment() {
        // Required empty public constructor
    }

    @Override
    protected void callListener(Choice choice) {
        listener.onUDFFieldSelected(choice.getValue());
    }
}
