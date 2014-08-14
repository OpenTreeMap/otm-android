package org.azavea.otm.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.common.base.Predicate;

import org.azavea.helpers.JSONHelper;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;

public class UDFCollectionDefinition extends Model implements Parcelable {

    private static final String DATA_TYPE = "data_type";
    private static final String CAN_EDIT = "can_write";
    private static final String FIELD_NAME = "name";
    private static final String COLLECTION_KEY = "field_key";
    private static final String DISPLAY_NAME = "display_name";

    public UDFCollectionDefinition(JSONObject data) {
        setData(data);
    }

    // This constructor, writeToParcel, describeContents, and the CREATOR field are all used for serialization

    /**
     * ****** Serialization ********
     */
    private UDFCollectionDefinition(Parcel in) {
        try {
            this.data = new JSONObject(in.readString());
        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "Received a Parcel with unparseable json");
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(data.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<UDFCollectionDefinition> CREATOR
            = new Parcelable.Creator<UDFCollectionDefinition>() {

        public UDFCollectionDefinition createFromParcel(Parcel in) {
            return new UDFCollectionDefinition(in);
        }

        public UDFCollectionDefinition[] newArray(int size) {
            return new UDFCollectionDefinition[size];
        }
    };

    /**
     * ****** Helper methods ********
     */
    public JSONArray getDataTypes() {
        return data.isNull(DATA_TYPE) ? new JSONArray() : data.optJSONArray(DATA_TYPE);
    }

    public boolean isWritable() {
        return data.optBoolean(CAN_EDIT);
    }

    public boolean isEditable() {
        return !getTypesForEdit().isEmpty() && isWritable();
    }

    public String getCollectionKey() {
        return safeGetString(COLLECTION_KEY);
    }


    public LinkedHashMap<String, JSONObject> groupTypesByName() {
        // It is easier to work with the udf fields by name, so we pull it into a LinkedHashMap
        final LinkedHashMap<String, JSONObject> map = new LinkedHashMap<>();
        final JSONArray dataTypes = getDataTypes();
        for (int i = 0; i < dataTypes.length(); i++) {
            final JSONObject dataType = dataTypes.optJSONObject(i);
            map.put(dataType.optString(FIELD_NAME), dataType);
        }
        return map;
    }

    /**
     * @return A list of sub-fields to use in UDFCollectionCreateActivity
     */
    public List<JSONObject> getTypesForAdd() {
        return getTypesForPredicate(j -> !j.has("default"));
    }

    /**
     * @return A list of sub-fields to use in UDFCollectionEditActivity
     */
    public List<JSONObject> getTypesForEdit() {
        return getTypesForPredicate(j -> j.has("default"));
    }

    private List<JSONObject> getTypesForPredicate(Predicate<JSONObject> pred) {
        return newArrayList(filter(groupTypesByName().values(), pred));
    }

    public String getDisplayName() {
        return safeGetString(DISPLAY_NAME);
    }

    public String getLabel() {
        final String collectionKey = safeGetString(COLLECTION_KEY);
        final String displayName = safeGetString(DISPLAY_NAME);
        final App app = App.getAppInstance();
        final String model = collectionKey.contains(".") ? collectionKey.split("[.]")[0] : null;
        if ("tree".equals(model)) {
            return String.format("%s - %s", app.getString(R.string.tree), displayName);
        } else if ("plot".equals(model)) {
            return String.format("%s - %s", app.getString(R.string.planting_site), displayName);
        } else {
            return displayName;
        }
    }
}
