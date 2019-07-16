package com.tpp;

import com.tpp.exception.EventException;

public interface EventHandler {
    void handle(Event event) throws EventException;
}
