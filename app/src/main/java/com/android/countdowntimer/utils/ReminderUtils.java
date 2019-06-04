package com.android.countdowntimer.utils;

public class ReminderUtils {

    public static int getSingleRemindInterval(int data) {
        if (data < 10) {
            return RemindType.NONE_REMIND;
        } else {
            return data / 10;
        }
    }

    public static int getRemindType(int data) {
        return data % 10;
    }

    public static int buildReminder(int remindType, int singleRemindInterval) {
        if (remindType == RemindType.NONE_REMIND) {
            return RemindType.NONE_REMIND;
        } else {
            return singleRemindInterval * 10 + remindType;
        }

    }

}
