package com.example.hollow.generated;

import com.netflix.hollow.api.objects.delegate.HollowObjectDelegate;


@SuppressWarnings("all")
public interface StatusDelegate extends HollowObjectDelegate {

    public String get_name(int ordinal);

    public boolean is_nameEqual(int ordinal, String testValue);

    public StatusTypeAPI getTypeAPI();

}