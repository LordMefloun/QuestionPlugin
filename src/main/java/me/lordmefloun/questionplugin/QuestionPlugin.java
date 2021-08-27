package me.lordmefloun.questionplugin;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.logging.log4j.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Executor;

public final class QuestionPlugin extends JavaPlugin {


    String Prefix;
    HashSet<Player> votedNo = new HashSet<>();
    HashSet<Player> votedYes = new HashSet<>();
    String Otazka;
    FileConfiguration config = this.getConfig();



    @Override
    public void onEnable() {

        this.saveDefaultConfig();
        this.getCommand("q").setExecutor(this);
        Prefix = ChatColor.translateAlternateColorCodes('&', "&8(&a&l+&8)");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }



    public void reset(){
        Otazka = null;
        votedNo.clear();
        votedYes.clear();
    }

    public void runnable(){
        actionbar();
        new BukkitRunnable(){
            @Override
            public void run() {
                for (String message : config.getStringList("messages.EndMessage")){

                    message = message.replaceAll("%QUESTION%", Otazka);
                    message = message.replaceAll("%VOTEDNO%", Integer.toString(votedNo.size()));
                    message = message.replaceAll("%VOTEDYES%", Integer.toString(votedYes.size()));


                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
                reset();
            }
        }.runTaskLater(this, 20*config.getInt("votetime"));
    }


    public void actionbar(){
        new BukkitRunnable(){
            @Override
            public void run() {

                if(Otazka != null) {

                    for (Player p : Bukkit.getOnlinePlayers()) {

                        String message = config.getString("actionbar");
                        message = message.replaceAll("%QUESTION%", Otazka);
                        message = message.replaceAll("%VOTEDNO%", Integer.toString(votedNo.size()));
                        message = message.replaceAll("%VOTEDYES%", Integer.toString(votedYes.size()));


                        try {
                            p.sendActionBar(ChatColor.translateAlternateColorCodes('&', message));
                        }
                        catch (NoSuchMethodError e){
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
                        }

                    }
                }
                else{
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(this, 0, 1);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)){
            return false;
        }

        
        Player p = (Player) sender;

        if(args.length > 0){

            if(args[0].equalsIgnoreCase("create")){
                if(p.hasPermission("question.create")) {
                    if (args.length > 1) {
                        if (Otazka == null) {


                            String[] createQuestion = args;
                            String createdQuestion = "";

                            for (String str : createQuestion) {
                                if (str != createQuestion[0]) {
                                    if (str == createQuestion[1]) {
                                        createdQuestion += str;
                                    } else {
                                        createdQuestion += " " + str;
                                    }
                                }
                            }


                            Otazka = createdQuestion;


                            p.sendMessage(MessageFromConfig("questionCreatedMessage"));
                            p.sendMessage("");
                            for (String message : config.getStringList("messages.CreateMessage")){

                                message = message.replaceAll("%QUESTION%", Otazka);

                                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                            runnable();
                        } else {
                            p.sendMessage(MessageFromConfig("questionExistsMessage"));
                        }
                    } else {
                        p.sendMessage(MessageFromConfig("questionMissingMessage"));
                    }
                }
                else p.sendMessage(MessageFromConfig("noPermissionMessage"));
            }
            else if (args[0].equalsIgnoreCase("yes")){
                if(p.hasPermission("question.vote")) {
                    if (Otazka != null) {
                        if (!(votedYes.contains(p) || votedNo.contains(p))) {
                            p.sendMessage(MessageFromConfig("votedYesMessage"));
                            votedYes.add(p);
                        } else {
                            p.sendMessage(MessageFromConfig("alreadyVotedMessage"));
                        }
                    }
                    else {
                        p.sendMessage(MessageFromConfig("noQuestionMessage"));
                    }
                }
                else p.sendMessage(MessageFromConfig("noPermissionMessage"));

            }
            else if (args[0].equalsIgnoreCase("no")){
                if(p.hasPermission("question.vote")) {
                    if (Otazka != null) {
                        if (!(votedYes.contains(p) || votedNo.contains(p))) {
                            p.sendMessage(MessageFromConfig("votedNoMessage"));
                            votedNo.add(p);
                        } else {
                            p.sendMessage(MessageFromConfig("alreadyVotedMessage"));
                        }
                    }
                    else {
                        p.sendMessage(MessageFromConfig("noQuestionMessage"));
                    }
                }
                else p.sendMessage(MessageFromConfig("noPermissionMessage"));
            }


        }
        else{
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b&lQuestions"));
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "   &8- &a/q create <question> &2Create question"));
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "   &8- &a/q yes &2vote for question"));
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "   &8- &a/q no &2vote for question"));
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        }


        return false;
    }


    public String MessageFromConfig(String configpath){
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString(configpath));
    }
}
