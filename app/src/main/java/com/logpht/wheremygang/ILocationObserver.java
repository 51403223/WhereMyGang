package com.logpht.wheremygang;

public interface ILocationObserver<T> {
    void handleDataChange(T data);
    void handleLocationConnectionLost();
}
