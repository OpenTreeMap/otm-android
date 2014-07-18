package org.azavea.lists.data;

import org.azavea.otm.data.Plot;

public class DisplayablePlot implements DisplayableModel {
    private Plot plot;
    private String stringRepresentation;

    public DisplayablePlot() {
        plot = null;
        stringRepresentation = "";
    }

    public DisplayablePlot(Plot plot, String stringRepresentation) {
        this.plot = plot;
        this.stringRepresentation = stringRepresentation;
    }

    public Plot getPlot() {
        return plot;
    }

    public String toString() {
        return stringRepresentation;
    }
}
