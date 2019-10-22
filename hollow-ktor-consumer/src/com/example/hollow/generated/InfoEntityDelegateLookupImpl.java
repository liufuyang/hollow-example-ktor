package com.example.hollow.generated;

import com.netflix.hollow.api.objects.delegate.HollowObjectAbstractDelegate;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;
import com.netflix.hollow.core.schema.HollowObjectSchema;

@SuppressWarnings("all")
public class InfoEntityDelegateLookupImpl extends HollowObjectAbstractDelegate implements InfoEntityDelegate {

    private final InfoEntityTypeAPI typeAPI;

    public InfoEntityDelegateLookupImpl(InfoEntityTypeAPI typeAPI) {
        this.typeAPI = typeAPI;
    }

    public int getId(int ordinal) {
        return typeAPI.getId(ordinal);
    }

    public Integer getIdBoxed(int ordinal) {
        return typeAPI.getIdBoxed(ordinal);
    }

    public int getNameOrdinal(int ordinal) {
        return typeAPI.getNameOrdinal(ordinal);
    }

    public int getStatusOrdinal(int ordinal) {
        return typeAPI.getStatusOrdinal(ordinal);
    }

    public long getTimeUpdated(int ordinal) {
        return typeAPI.getTimeUpdated(ordinal);
    }

    public Long getTimeUpdatedBoxed(int ordinal) {
        return typeAPI.getTimeUpdatedBoxed(ordinal);
    }

    public InfoEntityTypeAPI getTypeAPI() {
        return typeAPI;
    }

    @Override
    public HollowObjectSchema getSchema() {
        return typeAPI.getTypeDataAccess().getSchema();
    }

    @Override
    public HollowObjectTypeDataAccess getTypeDataAccess() {
        return typeAPI.getTypeDataAccess();
    }

}