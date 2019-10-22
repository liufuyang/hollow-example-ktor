package com.example.hollow.generated;

import com.netflix.hollow.api.objects.delegate.HollowObjectAbstractDelegate;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;
import com.netflix.hollow.core.schema.HollowObjectSchema;
import com.netflix.hollow.api.custom.HollowTypeAPI;
import com.netflix.hollow.api.objects.delegate.HollowCachedDelegate;

@SuppressWarnings("all")
public class InfoEntityDelegateCachedImpl extends HollowObjectAbstractDelegate implements HollowCachedDelegate, InfoEntityDelegate {

    private final Integer id;
    private final int nameOrdinal;
    private final int statusOrdinal;
    private final Long timeUpdated;
    private InfoEntityTypeAPI typeAPI;

    public InfoEntityDelegateCachedImpl(InfoEntityTypeAPI typeAPI, int ordinal) {
        this.id = typeAPI.getIdBoxed(ordinal);
        this.nameOrdinal = typeAPI.getNameOrdinal(ordinal);
        this.statusOrdinal = typeAPI.getStatusOrdinal(ordinal);
        this.timeUpdated = typeAPI.getTimeUpdatedBoxed(ordinal);
        this.typeAPI = typeAPI;
    }

    public int getId(int ordinal) {
        if(id == null)
            return Integer.MIN_VALUE;
        return id.intValue();
    }

    public Integer getIdBoxed(int ordinal) {
        return id;
    }

    public int getNameOrdinal(int ordinal) {
        return nameOrdinal;
    }

    public int getStatusOrdinal(int ordinal) {
        return statusOrdinal;
    }

    public long getTimeUpdated(int ordinal) {
        if(timeUpdated == null)
            return Long.MIN_VALUE;
        return timeUpdated.longValue();
    }

    public Long getTimeUpdatedBoxed(int ordinal) {
        return timeUpdated;
    }

    @Override
    public HollowObjectSchema getSchema() {
        return typeAPI.getTypeDataAccess().getSchema();
    }

    @Override
    public HollowObjectTypeDataAccess getTypeDataAccess() {
        return typeAPI.getTypeDataAccess();
    }

    public InfoEntityTypeAPI getTypeAPI() {
        return typeAPI;
    }

    public void updateTypeAPI(HollowTypeAPI typeAPI) {
        this.typeAPI = (InfoEntityTypeAPI) typeAPI;
    }

}