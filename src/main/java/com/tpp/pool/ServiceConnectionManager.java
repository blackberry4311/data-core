package com.tpp.pool;

public interface ServiceConnectionManager<T> {
    T connectService() throws Exception;

    void disconnectService(T service) throws Exception;
}

