package com.example.hollow.generated;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.consumer.data.AbstractHollowDataAccessor;
import com.netflix.hollow.core.index.key.PrimaryKey;
import com.netflix.hollow.core.read.engine.HollowReadStateEngine;

@SuppressWarnings("all")
public class StatusDataAccessor extends AbstractHollowDataAccessor<Status> {

    public static final String TYPE = "Status";
    private InfoEntityAPI api;

    public StatusDataAccessor(HollowConsumer consumer) {
        super(consumer, TYPE);
        this.api = (InfoEntityAPI)consumer.getAPI();
    }

    public StatusDataAccessor(HollowReadStateEngine rStateEngine, InfoEntityAPI api) {
        super(rStateEngine, TYPE);
        this.api = api;
    }

    public StatusDataAccessor(HollowReadStateEngine rStateEngine, InfoEntityAPI api, String ... fieldPaths) {
        super(rStateEngine, TYPE, fieldPaths);
        this.api = api;
    }

    public StatusDataAccessor(HollowReadStateEngine rStateEngine, InfoEntityAPI api, PrimaryKey primaryKey) {
        super(rStateEngine, TYPE, primaryKey);
        this.api = api;
    }

    @Override public Status getRecord(int ordinal){
        return api.getStatus(ordinal);
    }

}