package com.hancho.hcontents;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityLevelChangeEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import com.hancho.hcontents.commands.RankCommand;

import java.util.*;

public class Content extends LinkedHashMap<String, Object> implements Listener{
    public final Hashtable<String, Long> timeAttackRecord = new Hashtable<>();
    public ArrayList<Location[]> generatedRollBack = new ArrayList<>();
    public HashSet<String> nowPlaying = new HashSet<>();
    public Location startPos1;
    public Location startPos2;
    public Location endPos1;
    public Location endPos2;
    public Location firstBlock;

    public Content(String name, LinkedHashMap<String, Object> map){
        this(name);
        this.putAll(map);
        this.generateRollBack(this.getRawRollBackData());
        this.startPos1 = Utils.stringToLocation((String) this.get("startPos1"));
        this.startPos2 = Utils.stringToLocation((String) this.get("startPos2"));
        this.endPos1 = Utils.stringToLocation((String) this.get("endPos1"));
        this.endPos2 = Utils.stringToLocation((String) this.get("endPos2"));
        this.firstBlock = Utils.stringToLocation((String) this.get("firstBlock"));
    }

    public Content(String name){
        this.setName(name);
        Server.getInstance().getCommandMap().register("contents",
                new RankCommand(this.getName() + "순위", this.getName() + " 랭크", this));
        Server.getInstance().getOnlinePlayers().forEach((u, p) -> p.sendCommandData());
        Server.getInstance().getPluginManager().registerEvents(this, HContents.INSTANCE);
    }

    public String getName(){
        return (String) this.get("name");
    }

    public void setName(String name){
        this.put("name", name);
    }

    public LinkedHashMap<String, Integer> getTimeAttackData(){
        return (LinkedHashMap<String, Integer>) this.getOrDefault("timeAttack", new LinkedHashMap<>());
    }

    public void setTimeAttackData(LinkedHashMap<String, Integer> map){
        this.put("timeAttack", map);
    }

    public void addTimeAttack(String playerName, int time){
        LinkedHashMap<String, Integer> data = this.getTimeAttackData();
        if(data.containsKey(playerName)){
            int oldTime = data.get(playerName);
            if(oldTime < time) return;
        }
        data.put(playerName, time);
        this.setTimeAttackData(data);
        Server.getInstance().broadcastMessage(HContents.PREFIX + playerName + "님이 §b" + this.getName() + "§f에서 "
                + time / 1000D + "초로 §b" + this.getMyTimeAttackRank(playerName) + " 위 §f했습니다.");
    }

    public int getMyTimeAttackRank(String playerName){
        LinkedHashMap<String, Integer> data = this.getTimeAttackData();
        int i = 1;
        int myTime = data.get(playerName);

        if(!data.containsKey(playerName)) return -1;
        for (String key : data.keySet()) {
            int time = data.get(key);
            if(key.equals(playerName)) continue;
            if(time < myTime || time == myTime) i++;
        }

        return i;
    }

    public LinkedHashMap<String, Integer> getTimeAttackRank(){
        LinkedHashMap<String, Integer> data = this.getTimeAttackData();
        LinkedHashMap<String, Integer> sortedData = new LinkedHashMap<>();
        ArrayList<String> keyList = new ArrayList<>(data.keySet());

        Collections.sort(keyList, (o1, o2) -> (data.get(o1).compareTo(data.get(o2))));
        for(String key : keyList){
            sortedData.put(key, data.get(key));
        }
        return sortedData;
    }

    public LinkedHashMap<String, String> getRawRollBackData(){
        return (LinkedHashMap<String, String>) this.getOrDefault("rollBacks", new LinkedHashMap<>());
    }

    public void setRawRollBackData(LinkedHashMap<String, String> map){
        this.put("rollBacks", map);
    }

    public void addRollBackData(String posKey, String targetLocation){
        LinkedHashMap<String, String> raw = this.getRawRollBackData();
        raw.put(posKey, targetLocation);
        this.setRawRollBackData(raw);
        this.generateRollBack(raw);
    }

    public void addRollBack(Location pos1, Location pos2, Location targetLocation){
        String posKey = Utils.locationToStringKey(pos1) + ":" + Utils.locationToStringKey(pos2);
        String target = Utils.locationToStringKey(targetLocation);
        this.addRollBackData(posKey, target);
    }

    public void generateRollBack(LinkedHashMap<String, String> map){
        //key = stringTargetLocation = 이동될 loc
        //value = stringLocation-StringLocation = (지점1-지점2)
        this.generatedRollBack.clear();
        map.forEach((k, v) -> {
            String[] posKey = k.split(":");
            Location pos1 = Utils.stringToLocation(posKey[0]);
            Location pos2 = Utils.stringToLocation(posKey[1]);
            Location targetLoc = Utils.stringToLocation(v);

            Location newPos1 = new Location(
                    Math.min(pos1.x, pos2.x),
                    Math.min(pos1.y, pos2.y),
                    Math.min(pos1.z, pos2.z), pos1.getLevel());

            Location newPos2 = new Location(
                    Math.max(pos1.x, pos2.x),
                    Math.max(pos1.y, pos2.y),
                    Math.max(pos1.z, pos2.z), pos1.getLevel());

            this.generatedRollBack.add(new Location[]{newPos1, newPos2, targetLoc});
        });
    }

    public void setStartPos(Location pos1, Location pos2){
        Location start = new Location(
                Math.min(pos1.x, pos2.x),
                Math.min(pos1.y, pos2.y),
                Math.min(pos1.z, pos2.z),
                pos1.getLevel());
        Location end = new Location(
                Math.max(pos1.x, pos2.x),
                Math.max(pos1.y, pos2.y),
                Math.max(pos1.z, pos2.z),
                pos1.getLevel());

        this.startPos1 = start;
        this.startPos2 = end;
        this.put("startPos1", Utils.locationToStringKey(start));
        this.put("startPos2", Utils.locationToStringKey(end));
    }

    public void setEndPos(Location pos1, Location pos2){
        Location start = new Location(
                Math.min(pos1.x, pos2.x),
                Math.min(pos1.y, pos2.y),
                Math.min(pos1.z, pos2.z),
                pos1.getLevel());
        Location end = new Location(
                Math.max(pos1.x, pos2.x),
                Math.max(pos1.y, pos2.y),
                Math.max(pos1.z, pos2.z),
                pos1.getLevel());

        this.endPos1 = start;
        this.endPos2 = end;
        this.put("endPos1", Utils.locationToStringKey(start));
        this.put("endPos2", Utils.locationToStringKey(end));
    }

    public void setFirstBlock(Location loc){
        this.firstBlock = loc;
        this.put("firstBlock", Utils.locationToStringKey(loc));
    }

    /**
     * author Solo
     */
    public boolean isInside(Location start, Location end, double x, double y, double z){
        if(start == null || end == null) return false;
        return (
                start.x <= x &&
                        start.y <= y &&
                        start.z <= z &&
                        end.x >= x &&
                        end.y >= y &&
                        end.z >= z
        );
    }

    /**
     *
     * @param postKey posKey
     */
    public boolean removeRollBack(Location postKey){
        LinkedHashMap<String, String> rawRollBackData = this.getRawRollBackData();
        int key = postKey.hashCode();

        boolean isExist = false;
        Location targetLoc = null;
        for (Location[] locations : this.generatedRollBack) {
            if(this.isInside(locations[0], locations[1], postKey.x, postKey.y, postKey.z)){
                isExist = true;
                targetLoc = locations[2];
                break;
            }
        }
        if(!isExist) return false;

        String targetBlock = Utils.locationToStringKey(targetLoc);
        LinkedHashMap<String, String> rawRollBack = this.getRawRollBackData();
        HashSet<String> keySet = new HashSet<>(rawRollBack.keySet());
        for (String posKey : keySet) {
            if(rawRollBack.get(posKey).equals(targetBlock)){
                rawRollBack.remove(posKey);
                break;
            }
        }
        this.setRawRollBackData(rawRollBackData);
        this.generateRollBack(rawRollBackData);
        return true;
    }

    public void removePlayer(Player player){
        this.nowPlaying.remove(player.getName());
        this.timeAttackRecord.remove(player.getName());
        HContents.nowPlay.remove(player.getName());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent ev){
        if(!(ev.getEntity() instanceof Player)) return;
        Player player = (Player) ev.getEntity();
        if(!this.nowPlaying.contains(player.getName())) return;

        if(player.getHealth() - ev.getFinalDamage() < 1){
            player.setHealth(player.getMaxHealth());
            player.teleport(this.firstBlock.add(0, 1, 0));
            this.removePlayer(player);
        }
    }

    @EventHandler
    public void onLevelChange(EntityLevelChangeEvent ev){
        if(!(ev.getEntity() instanceof Player)) return;
        this.removePlayer((Player) ev.getEntity());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent ev){
        Player player = ev.getPlayer();
        Location start = this.startPos1;
        if(start == null) return;
        Level level = this.startPos1.getLevel();
        if(level == null) return;
        if(!player.getLevel().getName().equals(this.startPos1.getLevel().getName())) return;

        if(this.nowPlaying.contains(player.getName())) {
            if (isInside(this.endPos1, this.endPos2, player.x, player.y, player.z)){
                this.nowPlaying.remove(player.getName());
                HContents.nowPlay.remove(player.getName());
                if(!this.timeAttackRecord.containsKey(player.getName())) return;
                int time = (int) (System.currentTimeMillis() - this.timeAttackRecord.get(player.getName()));
                this.addTimeAttack(player.getName(), time);
                player.sendMessage(HContents.PREFIX + "소요된 시간 : " + Utils.numToString(time) + "초");
                return;
            }
            for (Location[] locations : this.generatedRollBack) {
                if(this.isInside(locations[0], locations[1], player.x, player.y, player.z)){
                    player.teleport(locations[2]);
                    player.sendTitle("§f§oWhoosh!", "지정된 위치로 이동되었습니다", 10, 30, 10);
                    break;
                }
            }
        }
        if(isInside(this.startPos1, this.startPos2, player.x, player.y, player.z)){
            this.nowPlaying.add(player.getName());
            this.timeAttackRecord.put(player.getName(), System.currentTimeMillis());
            HContents.addNowPlaying(player, this);
            player.sendTip("§o카운팅 시작");
        }

    }

}
