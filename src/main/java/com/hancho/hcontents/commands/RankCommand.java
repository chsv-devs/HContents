package com.hancho.hcontents.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.plugin.Plugin;
import com.hancho.hcontents.Content;
import com.hancho.hcontents.Utils;

import java.util.LinkedHashMap;

public class RankCommand extends Command {
    public Content content;

    public RankCommand(String name, String description, Content content) {
        super(name, description);
        this.content = content;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        StringBuilder sb = new StringBuilder();
        LinkedHashMap<String, Integer> rank = this.content.getTimeAttackRank();
        int i = 1;
        for (String playerName : rank.keySet()) {
            int time = rank.get(playerName);
            sb.append("§o§f[§b ").append(i).append("§f위 ] ")
                    .append(playerName).append(" §f: ").append(Utils.numToString(time)).append("\n");
            i ++;
        }
        if(sender.isPlayer()){
            ((Player) sender).showFormWindow(new FormWindowSimple(this.content.getName() + "순위", sb.toString()));
        }else{
            sender.sendMessage(sb.toString());
        }
        return false;
    }
}
