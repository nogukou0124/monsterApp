package com.example.monsterapp.utils.Event;

import java.util.Map;

/**
 * イベントクラス
 */
public class Event {
    public EventCode eventCode;
    public Map<String, String> params;

    public Event(EventCode eventCode) {
        this.eventCode = eventCode;
    }

    public Event(EventCode eventCode, Map<String, String> params) {
        this.eventCode = eventCode;
        this.params = params;
    }
}
