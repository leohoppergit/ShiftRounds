package de.nulide.shiftcal.data.settings;

import java.util.HashMap;


public class Settings {

    private HashMap<String, String> settings;

    // FinalSettings
    public static final String ALARM_ON_OFF = "ALARM";
    public static final String ALARM_MINUTES = "ALARM-Minutes";
    public static final String ALARM_TONE = "ALARM_TONE";
    public static final String START_OF_WEEK = "START_OF_WEEK";
    public static final String SYNC = "SYNC";
    public static final String DUAL_SHIFT = "DUAL_SHIFT";
    public static final String WELCOME_INTRO_SHOWN = "INTRO";
    public static final String WEEK_OF_YEAR = "WEEK_OF_YEAR";
    public static final String INTRO_PRIVACY = "INTRO_PRIVACY";
    public static final String INTRO_SHIFT_SELECTOR = "INTRO_SHIFT_SELECTOR";
    public static final String INFO_SHIFT_SELECTOR_COUNT = "INFO_SHIFT_SELECTOR_COUNT";
    public static final String INTRO_FAB_MENU_EXPLANATION = "INFO_FAB_MENU_EXPLANATION";
    public static final String ALARM_SNOOZE_MINUTES = "ALARM_SNOOZE_MINUTES";
    public static final String PERM_ONE_PLUS_BACKGROUND_ACTIVITY = "PERM_ONE_PLUS_BACKGROUND_ACTIVITY";
    public static final String SERVER_SYNC_UUID = "SERVER_SYNC_UUID";
    public static final String SERVER_SYNC_HPW = "SERVER_SYNC_HPW";
    public static final String SERVER_SYNC_PW = "SERVER_SYNC_PW";
    public static final String SERVER_SYNC_SPECTATOR_UUID = "SERVER_SYNC_SPECTATOR_UUID";
    public static final String SERVER_SYNC_SPECTATOR_PW = "SERVER_SYNC_SPECTATOR_PW";
    public static final String CALENDAR_IN_FAMILY_MODE = "CALENDAR_IN_FAMILY_SYNC_VIEW";
    public static final String CALENDAR_IN_FAMILY_WEEK_VIEW = "CALENDAR_IN_FAMILY_WEEK_VIEW";
    public static final String SERVER_SYNC_SPECTATOR_LOGOUT = "SERVER_SYNC_SPECTATOR_LOGOUT";
    public static final String FAMILY_SYNC_INTRO_SHOWN = "FAMILY_SYNC_INTRO_SHOWN";
    public static final String INTRO_SHIFT_BLOCK_CREATOR = "INTRO_SHIFT_BLOCK_CREATOR";
    public static final String FAMILY_SYNC_LAST_UPLOAD_FAILED = "FAMILY_SYNC_LAST_UPLOAD_FAILED";
    public static final String LAST_POST_PROCESS_FAILED = "LAST_POST_PROCESS_FAILED";
    public static final String ALARM_SNOOZED_SHIFT = "ALARM_SNOOZED_SHIFT";
    public static final String ALARM_SNOOZED_NEW_TIME = "ALARM_SNOOZED_TIME";
    public static final String DB_MIGRATION_COMPLETED = "DB_MIGRATION";
    public static final String DB_MIGRATION_SHARED_RETRIEVED_COMPLETED = "DB_MIGRATION_SHARED";
    public static final String SWIFTSHIFT_IMPORT_PROMPT_HANDLED = "SWIFTSHIFT_IMPORT_PROMPT_HANDLED";


    public Settings() {
        settings = new HashMap<>();
    }

    public boolean isAvailable(String setting) {
        return settings.containsKey(setting);
    }

    public String getSetting(String key) {
        if (settings.containsKey(key)) {
            return settings.get(key);
        } else {
            switch (key) {
                case ALARM_SNOOZED_SHIFT:
                    return "-1";
                case ALARM_SNOOZED_NEW_TIME:
                    return Long.valueOf(-1L).toString();
                case START_OF_WEEK:
                case INFO_SHIFT_SELECTOR_COUNT:
                    return "0";
                case ALARM_SNOOZE_MINUTES:
                    return "5";
                case ALARM_MINUTES:
                    return "15";
                case ALARM_ON_OFF:
                case SYNC:
                case DUAL_SHIFT:
                case WELCOME_INTRO_SHOWN:
                case WEEK_OF_YEAR:
                case INTRO_PRIVACY:
                case INTRO_SHIFT_SELECTOR:
                case PERM_ONE_PLUS_BACKGROUND_ACTIVITY:
                case CALENDAR_IN_FAMILY_MODE:
                case SERVER_SYNC_SPECTATOR_LOGOUT:
                case FAMILY_SYNC_INTRO_SHOWN:
                case INTRO_SHIFT_BLOCK_CREATOR:
                case FAMILY_SYNC_LAST_UPLOAD_FAILED:
                case LAST_POST_PROCESS_FAILED:
                case CALENDAR_IN_FAMILY_WEEK_VIEW:
                case DB_MIGRATION_COMPLETED:
                case DB_MIGRATION_SHARED_RETRIEVED_COMPLETED:
                case INTRO_FAB_MENU_EXPLANATION:
                case SWIFTSHIFT_IMPORT_PROMPT_HANDLED:
                    return Boolean.valueOf(false).toString();
                case ALARM_TONE:
                case SERVER_SYNC_UUID:
                case SERVER_SYNC_HPW:
                case SERVER_SYNC_PW:
                case SERVER_SYNC_SPECTATOR_UUID:
                case SERVER_SYNC_SPECTATOR_PW:
                    return "";

            }
        }
        return "";
    }

    public <T> void setSetting(String setting, T value) {
        settings.put(setting, value.toString());
    }

    public void removeSetting(String setting) {
        settings.remove(setting);
    }

    public HashMap<String, String> getSettings() {
        return settings;
    }

    public void setSettings(HashMap<String, String> settings) {
        this.settings = settings;
    }
}
