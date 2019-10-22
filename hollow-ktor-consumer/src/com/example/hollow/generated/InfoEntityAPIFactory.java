package com.example.hollow.generated;

import com.netflix.hollow.api.client.HollowAPIFactory;
import com.netflix.hollow.api.custom.HollowAPI;
import com.netflix.hollow.api.objects.provider.HollowFactory;
import com.netflix.hollow.core.read.dataaccess.HollowDataAccess;
import java.util.Collections;
import java.util.Set;

@SuppressWarnings("all")
public class InfoEntityAPIFactory implements HollowAPIFactory {

    private final Set<String> cachedTypes;

    public InfoEntityAPIFactory() {
        this(Collections.<String>emptySet());
    }

    public InfoEntityAPIFactory(Set<String> cachedTypes) {
        this.cachedTypes = cachedTypes;
    }

    @Override
    public HollowAPI createAPI(HollowDataAccess dataAccess) {
        return new InfoEntityAPI(dataAccess, cachedTypes);
    }

    @Override
    public HollowAPI createAPI(HollowDataAccess dataAccess, HollowAPI previousCycleAPI) {
        if (!(previousCycleAPI instanceof InfoEntityAPI)) {
            throw new ClassCastException(previousCycleAPI.getClass() + " not instance of InfoEntityAPI");        }
        return new InfoEntityAPI(dataAccess, cachedTypes, Collections.<String, HollowFactory<?>>emptyMap(), (InfoEntityAPI) previousCycleAPI);
    }

}