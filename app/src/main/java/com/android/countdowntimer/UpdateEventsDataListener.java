package com.android.countdowntimer;

public interface UpdateEventsDataListener {

    void onSortUpdate(int type);

    void onIsAscUpdate(boolean isAsc);

    void onEventDelete();

}
