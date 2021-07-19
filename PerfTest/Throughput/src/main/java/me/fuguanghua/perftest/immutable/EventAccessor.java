package me.fuguanghua.perftest.immutable;

public interface EventAccessor<T>
{
    T take(long sequence);
}
