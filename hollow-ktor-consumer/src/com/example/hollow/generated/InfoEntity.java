package com.example.hollow.generated;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.consumer.index.UniqueKeyIndex;
import com.netflix.hollow.api.objects.HollowObject;
import com.netflix.hollow.core.schema.HollowObjectSchema;

@SuppressWarnings("all")
public class InfoEntity extends HollowObject {

    public InfoEntity(InfoEntityDelegate delegate, int ordinal) {
        super(delegate, ordinal);
    }

    public int getId() {
        return delegate().getId(ordinal);
    }

    public Integer getIdBoxed() {
        return delegate().getIdBoxed(ordinal);
    }

    public HString getName() {
        int refOrdinal = delegate().getNameOrdinal(ordinal);
        if(refOrdinal == -1)
            return null;
        return  api().getHString(refOrdinal);
    }

    public Status getStatus() {
        int refOrdinal = delegate().getStatusOrdinal(ordinal);
        if(refOrdinal == -1)
            return null;
        return  api().getStatus(refOrdinal);
    }

    public long getTimeUpdated() {
        return delegate().getTimeUpdated(ordinal);
    }

    public Long getTimeUpdatedBoxed() {
        return delegate().getTimeUpdatedBoxed(ordinal);
    }

    public InfoEntityAPI api() {
        return typeApi().getAPI();
    }

    public InfoEntityTypeAPI typeApi() {
        return delegate().getTypeAPI();
    }

    protected InfoEntityDelegate delegate() {
        return (InfoEntityDelegate)delegate;
    }

    /**
     * Creates a unique key index for {@code InfoEntity} that has a primary key.
     * The primary key is represented by the type {@code int}.
     * <p>
     * By default the unique key index will not track updates to the {@code consumer} and thus
     * any changes will not be reflected in matched results.  To track updates the index must be
     * {@link HollowConsumer#addRefreshListener(HollowConsumer.RefreshListener) registered}
     * with the {@code consumer}
     *
     * @param consumer the consumer
     * @return the unique key index
     */
    public static UniqueKeyIndex<InfoEntity, Integer> uniqueIndex(HollowConsumer consumer) {
        return UniqueKeyIndex.from(consumer, InfoEntity.class)
            .bindToPrimaryKey()
            .usingPath("id", int.class);
    }

}