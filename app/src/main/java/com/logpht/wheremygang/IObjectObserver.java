package com.logpht.wheremygang;

// this interface is used for location change
public interface IObjectObserver<T> {
    void registerObserver(ILocationObserver observer);
    void notifyObservers(T data);
    void notifyLocationConnectionLost();
}
