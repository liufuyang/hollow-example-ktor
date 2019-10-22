package com.example.hollow.generated;

import com.netflix.hollow.api.objects.delegate.HollowObjectDelegate;


@SuppressWarnings("all")
public interface InfoEntityDelegate extends HollowObjectDelegate {

    public int getId(int ordinal);

    public Integer getIdBoxed(int ordinal);

    public int getNameOrdinal(int ordinal);

    public int getStatusOrdinal(int ordinal);

    public long getTimeUpdated(int ordinal);

    public Long getTimeUpdatedBoxed(int ordinal);

    public InfoEntityTypeAPI getTypeAPI();

}