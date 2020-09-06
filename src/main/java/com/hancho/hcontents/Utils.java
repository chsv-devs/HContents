package com.hancho.hcontents;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;

public class Utils {
    public static String locationToStringKey(Location location){
        return location.getLevel().getName() + "_" + location.getX() + "_" + location.getY() + "_" + location.getZ();
    }

    public static Location stringToLocation(String key){
        if(key == null) return null;
        String[] strings = key.split("_");
        Level level;
        double x, y, z;

        level = Server.getInstance().getLevelByName(strings[0]);
        x = Double.parseDouble(strings[1]);
        y = Double.parseDouble(strings[2]);
        z = Double.parseDouble(strings[3]);
        return new Location(x, y, z, level);
    }

    public static String numToString(long time){
        String leftTime = "";
        int oneSecond = 1000;
        int oneMin = oneSecond * 60;
        int oneHour = oneMin * 60;

        double hour = Math.floor(time / (oneHour));
        double min = Math.floor((time % oneHour) / oneMin);
        double second = Math.floor(((time % oneHour) % oneMin / oneSecond));
        if ((time / oneHour) > 0) {
            leftTime = "§b" + (int) hour + "§f시간 §b" + (int) min + "§f분" + "§b" + second + "§f초";
        } else if ((time / oneMin) > 0) {
            leftTime = "§b" + (int) min + "§f분" + "§b" + second + "§f초";
        }else{
            leftTime = "§b" + second + "§f초";
        }
        return leftTime;
    }
}
