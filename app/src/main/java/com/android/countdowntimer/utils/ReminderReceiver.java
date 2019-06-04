package com.android.countdowntimer.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String notificationTitle = intent.getStringExtra(NotificationUtils.EXTRA_NOTIFICATION_TITLE);
        String content = intent.getStringExtra(NotificationUtils.EXTRA_NOTIFICATION_CONTENT);
        NotificationUtils.showNotification(context, notificationTitle, content);
    }

}
