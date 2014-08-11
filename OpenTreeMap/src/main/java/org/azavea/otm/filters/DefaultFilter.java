package org.azavea.otm.filters;

import org.azavea.otm.data.UDFCollectionDefinition;
import org.json.JSONObject;

import java.util.Arrays;

import static com.google.common.collect.Iterables.getLast;

public class DefaultFilter extends BooleanFilter {
    private final Object value;

    public DefaultFilter(String key, String identifier, String label, JSONObject fieldDef) {
        super(key, identifier, label);

        // We need to look up the default value from the field definition.
        // This will be a sub field if the field is a collection UDF
        if (fieldDef.optBoolean("is_collection")) {
            final UDFCollectionDefinition udfDef = new UDFCollectionDefinition(fieldDef);
            final String subField = getLast(Arrays.asList(identifier.split("[.]")));
            final JSONObject subFieldDef = udfDef.groupTypesByName().get(subField);
            this.value = subFieldDef.opt("default");
        } else {
            this.value = fieldDef.opt("default");
        }
    }

    @Override
    public JSONObject getFilterObject() {
        return buildNestedFilter(this.identifier, "IS", this.value);
    }
}
