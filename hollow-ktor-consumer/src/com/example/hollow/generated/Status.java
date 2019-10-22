package com.example.hollow.generated;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.objects.HollowObject;
import com.netflix.hollow.core.schema.HollowObjectSchema;

@SuppressWarnings("all")
public class Status extends HollowObject {

    public Status(StatusDelegate delegate, int ordinal) {
        super(delegate, ordinal);
    }

    public String get_name() {
        return delegate().get_name(ordinal);
    }

    public boolean is_nameEqual(String testValue) {
        return delegate().is_nameEqual(ordinal, testValue);
    }

    public InfoEntityAPI api() {
        return typeApi().getAPI();
    }

    public StatusTypeAPI typeApi() {
        return delegate().getTypeAPI();
    }

    protected StatusDelegate delegate() {
        return (StatusDelegate)delegate;
    }

}