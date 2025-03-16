package com.example.monsterapp.util.Event;

import java.util.Map;

/**
 * イベントクラス
 */
public class Event {
    public EventCode eventCode;
    public boolean isBattleEvent;

    public Event(EventCode eventCode) {
        this.eventCode = eventCode;
        isBattleEvent = false;
    }

    public Event(EventCode eventCode, boolean isBattleEvent) {
        this.eventCode = eventCode;
        this.isBattleEvent = isBattleEvent;
    }
}
