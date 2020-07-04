package com.jamesdpeters.minecraft.chests.misc;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class Settings {

    private static String CHECK_UPDATE = "update-checker";
    private static String CHECK_UPDATE_PERIOD = "update-checker-period";
    private static String LIMIT_CHESTS = "limit-chestlinks";
    private static String LIMIT_CHESTS_NUMBER = "limit-chestlinks-amount";

    private static Settings cf;
    private FileConfiguration configuration;
    private Plugin plugin;

    private static boolean isUpdateCheckEnabled;
    private static int updateCheckerPeriod;
    private static boolean limitChests;
    private static int limitChestsAmount;

    public static void initConfig(Plugin plugin){
        cf = new Settings();
        cf.plugin = plugin;
        cf.configuration = plugin.getConfig();

        //DEFAULT VALUES
        cf.configuration.addDefault(CHECK_UPDATE,true);
        cf.configuration.addDefault(CHECK_UPDATE_PERIOD,60*60);
        cf.configuration.addDefault(LIMIT_CHESTS,false);
        cf.configuration.addDefault(LIMIT_CHESTS_NUMBER,0);

        cf.configuration.options().copyDefaults(true);
        cf.plugin.saveConfig();

        reloadConfig();
    }

    private static void save(){
        cf.plugin.saveConfig();
    }

    public static void reloadConfig(){
        cf.configuration = cf.plugin.getConfig();

        isUpdateCheckEnabled = cf.configuration.getBoolean(CHECK_UPDATE);
        updateCheckerPeriod  = cf.configuration.getInt(CHECK_UPDATE_PERIOD);
        limitChests = cf.configuration.getBoolean(LIMIT_CHESTS);
        limitChestsAmount = cf.configuration.getInt(LIMIT_CHESTS_NUMBER);
    }

    /**
     * GETTERS AND SETTERS
     */
    public static boolean isUpdateCheckEnabled() {
        return isUpdateCheckEnabled;
    }
    public static int getUpdateCheckerPeriodTicks() { return 20*updateCheckerPeriod;}
    public static boolean isLimitChests() { return limitChests; }
    public static int getLimitChestsAmount() { return  limitChestsAmount; }
}