package com.nex;

import java.lang.ref.WeakReference;

public class PublisherSubscriber {

    private final WeakReference<Object> subscriber;

    public PublisherSubscriber(Object subscriber) {
        this.subscriber = new WeakReference<>(subscriber);
    }

    public void send(String string) {
        //((MainActivity)subscriber.get()).subscriber(string);
    }
}
