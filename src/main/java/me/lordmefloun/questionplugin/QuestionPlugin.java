package me.lordmefloun.questionplugin;

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


                        p.sendActionBar(ChatColor.translateAlternateColorCodes('&', message));
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


                            p.sendMessage(ChatColor.GREEN + "Your question has been created");
                            p.sendMessage("");
                            for (String message : config.getStringList("messages.CreateMessage")){

                                message = message.replaceAll("%QUESTION%", Otazka);

                                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                            }
                            runnable();
                        } else {
                            p.sendMessage(ChatColor.RED + "Question already exists");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "Write question");
                    }
                }
                else p.sendMessage(ChatColor.RED + "You don't have permission to do that");
            }
            else if (args[0].equalsIgnoreCase("yes")){
                if(p.hasPermission("question.vote")) {
                    if (Otazka != null) {
                        if (!(votedYes.contains(p) || votedNo.contains(p))) {
                            p.sendMessage(ChatColor.GREEN + "Thanks for voting! (yes)");
                            votedYes.add(p);
                        } else {
                            p.sendMessage(ChatColor.RED + "You've already voted!");
                        }
                    }
                    else {
                        p.sendMessage(ChatColor.RED + "No question has started yet");
                    }
                }
                else p.sendMessage(ChatColor.RED + "You don't have permission to do that");

            }
            else if (args[0].equalsIgnoreCase("no")){
                if(p.hasPermission("question.vote")) {
                    if (Otazka != null) {
                        if (!(votedYes.contains(p) || votedNo.contains(p))) {
                            p.sendMessage(ChatColor.GREEN + "Thanks for voting! (no)");
                            votedNo.add(p);
                        } else {
                            p.sendMessage(ChatColor.RED + "You've already voted!");
                        }
                    }
                    else {
                        p.sendMessage(ChatColor.RED + "No question has started yet");
                    }
                }
                else p.sendMessage(ChatColor.RED + "You don't have permission to do that");
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
}
