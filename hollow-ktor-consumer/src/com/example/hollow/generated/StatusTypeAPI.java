package com.example.hollow.generated;

import com.netflix.hollow.api.custom.HollowObjectTypeAPI;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;

@SuppressWarnings("all")
public class StatusTypeAPI extends HollowObjectTypeAPI {

    private final StatusDelegateLookupImpl delegateLookupImpl;

    public StatusTypeAPI(InfoEntityAPI api, HollowObjectTypeDataAccess typeDataAccess) {
        super(api, typeDataAccess, new String[] {
            "_name"
        });
        this.delegateLookupImpl = new StatusDelegateLookupImpl(this);
    }

    public String get_name(int ordinal) {
        if(fieldIndex[0] == -1)
            return missingDataHandler().handleString("Status", ordinal, "_name");
        boxedFieldAccessSampler.recordFieldAccess(fieldIndex[0]);
        return getTypeDataAccess().readString(ordinal, fieldIndex[0]);
    }

    public boolean is_nameEqual(int ordinal, String testValue) {
        if(fieldIndex[0] == -1)
            return missingDataHandler().handleStringEquals("Status", ordinal, "_name", testValue);
        return getTypeDataAccess().isStringFieldEqual(ordinal, fieldIndex[0], testValue);
    }

    public StatusDelegateLookupImpl getDelegateLookupImpl() {
        return delegateLookupImpl;
    }

    @Override
    public InfoEntityAPI getAPI() {
        return (InfoEntityAPI) api;
    }

}