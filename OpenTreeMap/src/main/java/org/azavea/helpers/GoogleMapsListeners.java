package org.azavea.helpers;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class GoogleMapsListeners {
    // We need to set a listener for marker drag, or markers will not give the correct position
    // when we later call marker.getPosition()
    // Note that we do not have to actually *do* anything in our listener
    public static class NoopDragListener implements GoogleMap.OnMarkerDragListener {

        @Override
        public void onMarkerDragStart(Marker marker) {

        }

        @Override
        public void onMarkerDrag(Marker marker) {

        }

        @Override
        public void onMarkerDragEnd(Marker marker) {

        }
    }
}
