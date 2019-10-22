package com.example.hollow.generated;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.consumer.index.AbstractHollowUniqueKeyIndex;
import com.netflix.hollow.api.consumer.index.HollowUniqueKeyIndex;
import com.netflix.hollow.core.schema.HollowObjectSchema;

/**
 * @deprecated see {@link com.netflix.hollow.api.consumer.index.UniqueKeyIndex} which can be built as follows:
 * <pre>{@code
 *     UniqueKeyIndex<InfoEntity, K> uki = UniqueKeyIndex.from(consumer, InfoEntity.class)
 *         .usingBean(k);
 *     InfoEntity m = uki.findMatch(k);
 * }</pre>
 * where {@code K} is a class declaring key field paths members, annotated with
 * {@link com.netflix.hollow.api.consumer.index.FieldPath}, and {@code k} is an instance of
 * {@code K} that is the key to find the unique {@code InfoEntity} object.
 */
@Deprecated
@SuppressWarnings("all")
public class InfoEntityPrimaryKeyIndex extends AbstractHollowUniqueKeyIndex<InfoEntityAPI, InfoEntity> implements HollowUniqueKeyIndex<InfoEntity> {

    public InfoEntityPrimaryKeyIndex(HollowConsumer consumer) {
        this(consumer, true);
    }

    public InfoEntityPrimaryKeyIndex(HollowConsumer consumer, boolean isListenToDataRefresh) {
        this(consumer, isListenToDataRefresh, ((HollowObjectSchema)consumer.getStateEngine().getNonNullSchema("InfoEntity")).getPrimaryKey().getFieldPaths());
    }

    public InfoEntityPrimaryKeyIndex(HollowConsumer consumer, String... fieldPaths) {
        this(consumer, true, fieldPaths);
    }

    public InfoEntityPrimaryKeyIndex(HollowConsumer consumer, boolean isListenToDataRefresh, String... fieldPaths) {
        super(consumer, "InfoEntity", isListenToDataRefresh, fieldPaths);
    }

    @Override
    public InfoEntity findMatch(Object... keys) {
        int ordinal = idx.getMatchingOrdinal(keys);
        if(ordinal == -1)
            return null;
        return api.getInfoEntity(ordinal);
    }

}