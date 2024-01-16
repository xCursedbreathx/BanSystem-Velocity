package net.cursedbreath.bansystemv3.utils;

import net.cursedbreath.bansystemv3.BanSystem_Velocity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Formatter {

    public static String formatTime(long time) {

        BanSystem_Velocity.getLogger().error("Time: " + time);

        if(time == 0) {
            return "<red>Permanent<reset>";
        }
        long seconds = time;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;
        days %= 7;
        months %= 12;

        StringBuilder stringBuilder = new StringBuilder();
        if (years > 0) {
            stringBuilder.append(years).append("y ");
        }
        if (months > 0) {
            stringBuilder.append(months).append("M ");
        }
        if (weeks > 0) {
            stringBuilder.append(weeks).append("w ");
        }
        if (days > 0) {
            stringBuilder.append(days).append("d ");
        }
        if (hours > 0) {
            stringBuilder.append(hours).append("h ");
        }
        if (minutes > 0) {
            stringBuilder.append(minutes).append("m ");
        }
        if (seconds > 0) {
            stringBuilder.append(seconds).append("s ");
        }
        return stringBuilder.toString();
    }

    public static String formatDateTime(long time) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));

        return simpleDateFormat.format(new Date(time * 1000));

    }

}
