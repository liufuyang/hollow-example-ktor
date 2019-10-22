package com.example.hollow.generated;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.consumer.data.AbstractHollowDataAccessor;
import com.netflix.hollow.core.index.key.PrimaryKey;
import com.netflix.hollow.core.read.engine.HollowReadStateEngine;

@SuppressWarnings("all")
public class InfoEntityDataAccessor extends AbstractHollowDataAccessor<InfoEntity> {

    public static final String TYPE = "InfoEntity";
    private InfoEntityAPI api;

    public InfoEntityDataAccessor(HollowConsumer consumer) {
        super(consumer, TYPE);
        this.api = (InfoEntityAPI)consumer.getAPI();
    }

    public InfoEntityDataAccessor(HollowReadStateEngine rStateEngine, InfoEntityAPI api) {
        super(rStateEngine, TYPE);
        this.api = api;
    }

    public InfoEntityDataAccessor(HollowReadStateEngine rStateEngine, InfoEntityAPI api, String ... fieldPaths) {
        super(rStateEngine, TYPE, fieldPaths);
        this.api = api;
    }

    public InfoEntityDataAccessor(HollowReadStateEngine rStateEngine, InfoEntityAPI api, PrimaryKey primaryKey) {
        super(rStateEngine, TYPE, primaryKey);
        this.api = api;
    }

    @Override public InfoEntity getRecord(int ordinal){
        return api.getInfoEntity(ordinal);
    }

}