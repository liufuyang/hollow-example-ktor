package com.example.hollow.generated;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.Map;
import com.netflix.hollow.api.consumer.HollowConsumerAPI;
import com.netflix.hollow.api.custom.HollowAPI;
import com.netflix.hollow.core.read.dataaccess.HollowDataAccess;
import com.netflix.hollow.core.read.dataaccess.HollowTypeDataAccess;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;
import com.netflix.hollow.core.read.dataaccess.HollowListTypeDataAccess;
import com.netflix.hollow.core.read.dataaccess.HollowSetTypeDataAccess;
import com.netflix.hollow.core.read.dataaccess.HollowMapTypeDataAccess;
import com.netflix.hollow.core.read.dataaccess.missing.HollowObjectMissingDataAccess;
import com.netflix.hollow.core.read.dataaccess.missing.HollowListMissingDataAccess;
import com.netflix.hollow.core.read.dataaccess.missing.HollowSetMissingDataAccess;
import com.netflix.hollow.core.read.dataaccess.missing.HollowMapMissingDataAccess;
import com.netflix.hollow.api.objects.provider.HollowFactory;
import com.netflix.hollow.api.objects.provider.HollowObjectProvider;
import com.netflix.hollow.api.objects.provider.HollowObjectCacheProvider;
import com.netflix.hollow.api.objects.provider.HollowObjectFactoryProvider;
import com.netflix.hollow.api.sampling.HollowObjectCreationSampler;
import com.netflix.hollow.api.sampling.HollowSamplingDirector;
import com.netflix.hollow.api.sampling.SampleResult;
import com.netflix.hollow.core.util.AllHollowRecordCollection;

@SuppressWarnings("all")
public class InfoEntityAPI extends HollowAPI  {

    private final HollowObjectCreationSampler objectCreationSampler;

    private final StatusTypeAPI statusTypeAPI;
    private final StringTypeAPI stringTypeAPI;
    private final InfoEntityTypeAPI infoEntityTypeAPI;

    private final HollowObjectProvider statusProvider;
    private final HollowObjectProvider stringProvider;
    private final HollowObjectProvider infoEntityProvider;

    public InfoEntityAPI(HollowDataAccess dataAccess) {
        this(dataAccess, Collections.<String>emptySet());
    }

    public InfoEntityAPI(HollowDataAccess dataAccess, Set<String> cachedTypes) {
        this(dataAccess, cachedTypes, Collections.<String, HollowFactory<?>>emptyMap());
    }

    public InfoEntityAPI(HollowDataAccess dataAccess, Set<String> cachedTypes, Map<String, HollowFactory<?>> factoryOverrides) {
        this(dataAccess, cachedTypes, factoryOverrides, null);
    }

    public InfoEntityAPI(HollowDataAccess dataAccess, Set<String> cachedTypes, Map<String, HollowFactory<?>> factoryOverrides, InfoEntityAPI previousCycleAPI) {
        super(dataAccess);
        HollowTypeDataAccess typeDataAccess;
        HollowFactory factory;

        objectCreationSampler = new HollowObjectCreationSampler("Status","String","InfoEntity");

        typeDataAccess = dataAccess.getTypeDataAccess("Status");
        if(typeDataAccess != null) {
            statusTypeAPI = new StatusTypeAPI(this, (HollowObjectTypeDataAccess)typeDataAccess);
        } else {
            statusTypeAPI = new StatusTypeAPI(this, new HollowObjectMissingDataAccess(dataAccess, "Status"));
        }
        addTypeAPI(statusTypeAPI);
        factory = factoryOverrides.get("Status");
        if(factory == null)
            factory = new StatusHollowFactory();
        if(cachedTypes.contains("Status")) {
            HollowObjectCacheProvider previousCacheProvider = null;
            if(previousCycleAPI != null && (previousCycleAPI.statusProvider instanceof HollowObjectCacheProvider))
                previousCacheProvider = (HollowObjectCacheProvider) previousCycleAPI.statusProvider;
            statusProvider = new HollowObjectCacheProvider(typeDataAccess, statusTypeAPI, factory, previousCacheProvider);
        } else {
            statusProvider = new HollowObjectFactoryProvider(typeDataAccess, statusTypeAPI, factory);
        }

        typeDataAccess = dataAccess.getTypeDataAccess("String");
        if(typeDataAccess != null) {
            stringTypeAPI = new StringTypeAPI(this, (HollowObjectTypeDataAccess)typeDataAccess);
        } else {
            stringTypeAPI = new StringTypeAPI(this, new HollowObjectMissingDataAccess(dataAccess, "String"));
        }
        addTypeAPI(stringTypeAPI);
        factory = factoryOverrides.get("String");
        if(factory == null)
            factory = new StringHollowFactory();
        if(cachedTypes.contains("String")) {
            HollowObjectCacheProvider previousCacheProvider = null;
            if(previousCycleAPI != null && (previousCycleAPI.stringProvider instanceof HollowObjectCacheProvider))
                previousCacheProvider = (HollowObjectCacheProvider) previousCycleAPI.stringProvider;
            stringProvider = new HollowObjectCacheProvider(typeDataAccess, stringTypeAPI, factory, previousCacheProvider);
        } else {
            stringProvider = new HollowObjectFactoryProvider(typeDataAccess, stringTypeAPI, factory);
        }

        typeDataAccess = dataAccess.getTypeDataAccess("InfoEntity");
        if(typeDataAccess != null) {
            infoEntityTypeAPI = new InfoEntityTypeAPI(this, (HollowObjectTypeDataAccess)typeDataAccess);
        } else {
            infoEntityTypeAPI = new InfoEntityTypeAPI(this, new HollowObjectMissingDataAccess(dataAccess, "InfoEntity"));
        }
        addTypeAPI(infoEntityTypeAPI);
        factory = factoryOverrides.get("InfoEntity");
        if(factory == null)
            factory = new InfoEntityHollowFactory();
        if(cachedTypes.contains("InfoEntity")) {
            HollowObjectCacheProvider previousCacheProvider = null;
            if(previousCycleAPI != null && (previousCycleAPI.infoEntityProvider instanceof HollowObjectCacheProvider))
                previousCacheProvider = (HollowObjectCacheProvider) previousCycleAPI.infoEntityProvider;
            infoEntityProvider = new HollowObjectCacheProvider(typeDataAccess, infoEntityTypeAPI, factory, previousCacheProvider);
        } else {
            infoEntityProvider = new HollowObjectFactoryProvider(typeDataAccess, infoEntityTypeAPI, factory);
        }

    }

    public void detachCaches() {
        if(statusProvider instanceof HollowObjectCacheProvider)
            ((HollowObjectCacheProvider)statusProvider).detach();
        if(stringProvider instanceof HollowObjectCacheProvider)
            ((HollowObjectCacheProvider)stringProvider).detach();
        if(infoEntityProvider instanceof HollowObjectCacheProvider)
            ((HollowObjectCacheProvider)infoEntityProvider).detach();
    }

    public StatusTypeAPI getStatusTypeAPI() {
        return statusTypeAPI;
    }
    public StringTypeAPI getStringTypeAPI() {
        return stringTypeAPI;
    }
    public InfoEntityTypeAPI getInfoEntityTypeAPI() {
        return infoEntityTypeAPI;
    }
    public Collection<Status> getAllStatus() {
        return new AllHollowRecordCollection<Status>(getDataAccess().getTypeDataAccess("Status").getTypeState()) {
            protected Status getForOrdinal(int ordinal) {
                return getStatus(ordinal);
            }
        };
    }
    public Status getStatus(int ordinal) {
        objectCreationSampler.recordCreation(0);
        return (Status)statusProvider.getHollowObject(ordinal);
    }
    public Collection<HString> getAllHString() {
        return new AllHollowRecordCollection<HString>(getDataAccess().getTypeDataAccess("String").getTypeState()) {
            protected HString getForOrdinal(int ordinal) {
                return getHString(ordinal);
            }
        };
    }
    public HString getHString(int ordinal) {
        objectCreationSampler.recordCreation(1);
        return (HString)stringProvider.getHollowObject(ordinal);
    }
    public Collection<InfoEntity> getAllInfoEntity() {
        return new AllHollowRecordCollection<InfoEntity>(getDataAccess().getTypeDataAccess("InfoEntity").getTypeState()) {
            protected InfoEntity getForOrdinal(int ordinal) {
                return getInfoEntity(ordinal);
            }
        };
    }
    public InfoEntity getInfoEntity(int ordinal) {
        objectCreationSampler.recordCreation(2);
        return (InfoEntity)infoEntityProvider.getHollowObject(ordinal);
    }
    public void setSamplingDirector(HollowSamplingDirector director) {
        super.setSamplingDirector(director);
        objectCreationSampler.setSamplingDirector(director);
    }

    public Collection<SampleResult> getObjectCreationSamplingResults() {
        return objectCreationSampler.getSampleResults();
    }

}
