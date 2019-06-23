package top.soyask.calendarii.utils;

import org.greenrobot.eventbus.EventBus;

public class EventBusDefault {
    public static void register(Object subscriber) {
        EventBus.getDefault().register(subscriber);
    }

    public static void unregister(Object subscriber) {
        EventBus.getDefault().unregister(subscriber);
    }

    public static void post(Object message){
        EventBus.getDefault().post(message);
    }
}
