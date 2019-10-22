package com.example.hollow.generated;

import com.netflix.hollow.api.custom.HollowObjectTypeAPI;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;

@SuppressWarnings("all")
public class InfoEntityTypeAPI extends HollowObjectTypeAPI {

    private final InfoEntityDelegateLookupImpl delegateLookupImpl;

    public InfoEntityTypeAPI(InfoEntityAPI api, HollowObjectTypeDataAccess typeDataAccess) {
        super(api, typeDataAccess, new String[] {
            "id",
            "name",
            "status",
            "timeUpdated"
        });
        this.delegateLookupImpl = new InfoEntityDelegateLookupImpl(this);
    }

    public int getId(int ordinal) {
        if(fieldIndex[0] == -1)
            return missingDataHandler().handleInt("InfoEntity", ordinal, "id");
        return getTypeDataAccess().readInt(ordinal, fieldIndex[0]);
    }

    public Integer getIdBoxed(int ordinal) {
        int i;
        if(fieldIndex[0] == -1) {
            i = missingDataHandler().handleInt("InfoEntity", ordinal, "id");
        } else {
            boxedFieldAccessSampler.recordFieldAccess(fieldIndex[0]);
            i = getTypeDataAccess().readInt(ordinal, fieldIndex[0]);
        }
        if(i == Integer.MIN_VALUE)
            return null;
        return Integer.valueOf(i);
    }



    public int getNameOrdinal(int ordinal) {
        if(fieldIndex[1] == -1)
            return missingDataHandler().handleReferencedOrdinal("InfoEntity", ordinal, "name");
        return getTypeDataAccess().readOrdinal(ordinal, fieldIndex[1]);
    }

    public StringTypeAPI getNameTypeAPI() {
        return getAPI().getStringTypeAPI();
    }

    public int getStatusOrdinal(int ordinal) {
        if(fieldIndex[2] == -1)
            return missingDataHandler().handleReferencedOrdinal("InfoEntity", ordinal, "status");
        return getTypeDataAccess().readOrdinal(ordinal, fieldIndex[2]);
    }

    public StatusTypeAPI getStatusTypeAPI() {
        return getAPI().getStatusTypeAPI();
    }

    public long getTimeUpdated(int ordinal) {
        if(fieldIndex[3] == -1)
            return missingDataHandler().handleLong("InfoEntity", ordinal, "timeUpdated");
        return getTypeDataAccess().readLong(ordinal, fieldIndex[3]);
    }

    public Long getTimeUpdatedBoxed(int ordinal) {
        long l;
        if(fieldIndex[3] == -1) {
            l = missingDataHandler().handleLong("InfoEntity", ordinal, "timeUpdated");
        } else {
            boxedFieldAccessSampler.recordFieldAccess(fieldIndex[3]);
            l = getTypeDataAccess().readLong(ordinal, fieldIndex[3]);
        }
        if(l == Long.MIN_VALUE)
            return null;
        return Long.valueOf(l);
    }



    public InfoEntityDelegateLookupImpl getDelegateLookupImpl() {
        return delegateLookupImpl;
    }

    @Override
    public InfoEntityAPI getAPI() {
        return (InfoEntityAPI) api;
    }

}