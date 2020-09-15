package com.hancho.hcontents;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class HContents extends PluginBase implements Listener {
    public static final String PREFIX = "§o[ §7＃ §f] ";
    public static HContents INSTANCE;
    public static HashMap<String, Content> nowPlay = new HashMap<>();

    public LinkedHashMap<String, Content> data = new LinkedHashMap<>();
    public HashMap<String, Content> queue = new HashMap<>();
    public HashMap<String, Location> queue2 = new HashMap<>();
    public boolean isReady = false;

    public static void addNowPlaying(Player player, Content content){
        if(nowPlay.containsKey(player.getName())){
            Content oldContent = nowPlay.get(player.getName());
            if(!content.equals(oldContent)) {
                oldContent.removePlayer(player);
            }
        }
        HContents.nowPlay.put(player.getName(), content);
    }

    @Override
    public void onEnable() {
        if(INSTANCE == null) INSTANCE = this;
        Config config = new Config(this.getDataFolder().getAbsolutePath() + "/contents.yml", Config.YAML);
        ((LinkedHashMap<String, LinkedHashMap<String, Object>>)new LinkedHashMap<>(config.getAll()).clone())
                .forEach((k, v) -> this.data.put(k, new Content(k, v)));
        this.getServer().getPluginManager().registerEvents(this, this);

        this.getServer().getScheduler().scheduleRepeatingTask(this, () -> {
            long current = System.currentTimeMillis();
            this.getServer().getOnlinePlayers().forEach((u, p) -> {
                Content content = nowPlay.get(p.getName());
                if(content != null){
                    if(!content.timeAttackRecord.containsKey(p.getName())) return;
                    long startTime = content.timeAttackRecord.get(p.getName());
                    p.sendTip(Utils.numToString(current - startTime));
                    //p.sendTip(sdf.format((current - startTime) / 1000D) + "초");
                }
            });
        }, 10, false);
        this.isReady = true;
    }

    @Override
    public void onDisable() {
        if(!isReady) return;
        this.save();
    }

    public void save(){
        Config config = new Config(this.getDataFolder().getAbsolutePath() + "/contents.yml", Config.YAML);
        config.setAll(new LinkedHashMap<>(data));
        config.save();
    }

    @EventHandler
    public void onTouch(PlayerInteractEvent ev){
        if(ev.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        String name = ev.getPlayer().getName();
        if(this.queue.containsKey(name)){
            Content content = this.queue.get(name);
            if(this.queue2.containsKey("sz_" + name)){
                if(this.queue2.containsKey("sz1_" + name)){
                    content.setStartPos(this.queue2.get("sz1_" + name), ev.getBlock().getLocation());
                    this.queue2.remove("sz_" + name);
                    this.queue2.remove("sz1_" + name);
                    ev.getPlayer().sendMessage(PREFIX + "성공적으로 지정되었습니다.");
                }else{
                    this.queue2.put("sz1_" + name, ev.getBlock().getLocation());
                    ev.getPlayer().sendMessage(PREFIX + "2지점을 선택해주세요.");
                }
            }else if(this.queue2.containsKey("ez_" + name)){
                if(this.queue2.containsKey("ez1_" + name)){
                    content.setEndPos(this.queue2.get("ez1_" + name), ev.getBlock().getLocation());
                    this.queue2.remove("ez_" + name);
                    this.queue2.remove("ez1_" + name);
                    ev.getPlayer().sendMessage(PREFIX + "성공적으로 지정되었습니다.");
                }else{
                    this.queue2.put("ez1_" + name, ev.getBlock().getLocation());
                    ev.getPlayer().sendMessage(PREFIX + "2지점을 선택해주세요.");
                }
            }else if(this.queue2.containsKey("sb_" + name)){
                content.setFirstBlock(ev.getBlock().getLocation());
                this.queue2.remove("sb_" + name);
                ev.getPlayer().sendMessage(PREFIX + "설정되었습니다.");
            }else if(this.queue2.containsKey("rba_" + name)){
                if(this.queue2.containsKey("rba1_" + name)){
                    if(this.queue2.containsKey("rba2_" + name)) {
                        content.addRollBack(this.queue2.get("rba1_" + name),
                                this.queue2.get("rba2_" + name),
                                ev.getBlock().getLocation());
                        this.queue2.remove("rba_" + name);
                        this.queue2.remove("rba1_" + name);
                        this.queue2.remove("rba2_" + name);
                        ev.getPlayer().sendMessage(PREFIX + "성공적으로 지정되었습니다.");
                    }else{
                        this.queue2.put("rba2_" + name, ev.getBlock().getLocation());
                        ev.getPlayer().sendMessage(PREFIX + "3지점을 선택해주세요.");
                    }
                }else{
                    this.queue2.put("rba1_" + name, ev.getBlock().getLocation());
                    ev.getPlayer().sendMessage(PREFIX + "2지점을 선택해주세요.");
                }
            }else if(this.queue2.containsKey("rbr_" + name)){
                this.queue2.remove("rbr_" + name);
                if(content.removeRollBack(ev.getBlock().getLocation())){
                    ev.getPlayer().sendMessage(PREFIX + "성공적으로 제거 되었습니다.");
                }else{
                    ev.getPlayer().sendMessage(PREFIX + "롤백 구역을 찾지 못했습니다.");
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("컨텐츠추가")){
            if(args.length < 1){
                sender.sendMessage(PREFIX + "사용법 : /컨텐츠추가 <이름>");
                return true;
            }
            Content content = new Content(args[0]);
            this.data.put(args[0], content);
            this.queue.put(sender.getName(), content);

            sender.sendMessage(PREFIX + "다음 명령어들로 명령어로 컨텐츠를 수정하세요." +
                    "\n/컨텐츠시작구역\n/컨텐츠종료구역\n/컨텐츠시작블럭\n컨텐츠롤백추가 \n등...");
        }else if(command.getName().equals("컨텐츠시작구역")
                || command.getName().equals("컨텐츠종료구역")
                || command.getName().equals("컨텐츠시작블럭")
                || command.getName().equals("컨텐츠롤백추가")
                || command.getName().equals("컨텐츠롤백제거")){
            Content content = this.queue.get(sender.getName());
            if(content == null){
                sender.sendMessage(PREFIX + "/컨텐츠수정 <컨텐츠이름> 명령어를 먼저 사용해주세요.");
                return true;
            }
            if(command.getName().equals("컨텐츠시작구역")){
                this.queue2.put("sz_" + sender.getName(), new Location());
            }else if(command.getName().equals("컨텐츠종료구역")) {
                this.queue2.put("ez_" + sender.getName(), new Location());
            }else if(command.getName().equals("컨텐츠시작블럭")) {
                this.queue2.put("sb_" + sender.getName(), new Location());
            }else if(command.getName().equals("컨텐츠롤백추가")){
                this.queue2.put("rba_" + sender.getName(), new Location());
            }else if(command.getName().equals("컨텐츠롤백제거")){
                this.queue2.put("rbr_" + sender.getName(), new Location());
            }
            sender.sendMessage(PREFIX + "블럭을 터치해주세요.");
        }else if(command.getName().equals("컨텐츠수정")){
            if(args.length < 1){
                sender.sendMessage(PREFIX + "사용법 : /컨텐츠수정 <컨텐츠이름>");
                return true;
            }
            Content content = this.data.get(args[0]);
            if(content == null){
                sender.sendMessage(PREFIX + "해당 컨텐츠를 찾을 수 없습니다.");
                return true;
            }
            this.queue.put(sender.getName(), content);
            sender.sendMessage(PREFIX + "해당 컨텐츠가 지정되었습니다.");
        }else if(command.getName().equals("컨텐츠목록")){
            StringBuilder sb = new StringBuilder();
            for (String name : this.data.keySet()) {
                sb.append(PREFIX).append(" ").append(name).append("\n");
            }
            sender.sendMessage(sb.toString());
        }else if(command.getName().equals("컨텐츠제거")){
            if(args.length < 1){
                sender.sendMessage(PREFIX + "사용법 : /컨텐츠제거 <컨텐츠이름>");
                return true;
            }
            if(this.data.remove(args[0]) != null){
                sender.sendMessage(PREFIX + "성공적으로 " + args[0] + "이 제거되었습니다");
            }else{
                sender.sendMessage(PREFIX + args[0] + "을 찾을 수 없습니다. /컨텐츠목록 을 시도해보세요.");
            }
        }else if(command.getName().equals("컨텐츠순위삭제")){
            Content content = this.queue.get(sender.getName());
            if(content == null){
                sender.sendMessage(PREFIX + "/컨텐츠수정 <컨텐츠이름> 명령어를 먼저 사용해주세요.");
                return true;
            }
            if(args.length < 1){
                sender.sendMessage(PREFIX + "/컨텐츠순위삭제 <닉네임>");
                return true;
            }
            LinkedHashMap<String, Integer> data = content.getTimeAttackData();
            if(data.containsKey(args[0])){
                data.remove(args[0]);
                content.setTimeAttackData(data);
            }else{
                sender.sendMessage(PREFIX + "해당 플레이어를 찾지 못했습니다. (대소문자 구분)");
                return true;
            }
        }
        return true;
    }
}
