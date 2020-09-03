
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;

import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;


/// hashmap of og vc and matches id to linked list
public class Main extends ListenerAdapter {
    private static JDA builder;
    private Map<String, List<VoiceChannel>> voiceChannelMaps = new HashMap<>();

    public static void main(String[] args) throws Exception {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("../VoiceToken")), "UTF8"));
            builder = new JDABuilder(AccountType.BOT).setToken(reader.readLine()).build();
            reader.close();
            builder.addEventListener(new Main());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        Guild guild = event.getGuild();
        VoiceChannel vc = event.getChannelJoined();
        try {
            addChannel(guild, vc);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


//        int userlimit = vc.getUserLimit();
//        guild.getnewName(vc.getName());

//        System.out.println(newName(newVC.getName()));


    }
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        Guild guild = event.getGuild();
        VoiceChannel vc = event.getChannelLeft();
        try {
            removeChannel(guild, vc);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
//        System.out.println("Moved");
        Guild guild = event.getGuild();
        VoiceChannel vc = event.getChannelLeft();
        try {
            removeChannel(guild, vc);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        vc = event.getChannelJoined();
        try {
            addChannel(guild, vc);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



    }


    private void removeChannel(Guild guild, VoiceChannel vc) throws InterruptedException {
//        System.out.println("left channel " + vc.getName());
        String ogName = vc.getName();
        String name;
        if (endsWithNumber(ogName)) {
            name = removeNum(ogName);
        } else {
            name = ogName;
        }

        List<VoiceChannel> vcRemove = voiceChannelMaps.get(name);
        List<VoiceChannel> vcNew = new LinkedList<>();
        boolean removedFlag = false;
//        System.out.println(vcRemove.size());

        if (vcRemove.size() == 1) {
            if (vc.getMembers().size() == 0 && vcRemove.get(0).getMembers().size() == 0) {
                vcRemove.get(0).delete().complete();
                vcRemove.remove(0);
            } else {
                vcNew.add(vcRemove.get(0));
            }
        } else if (vcRemove.size() == 2 && vcRemove.get(1).getMembers().size() == 0) {

            if (vc.getMembers().size() == 0 && vcRemove.get(0).getMembers().size() == 0) {
                for (VoiceChannel vcCurrent : vcRemove) {
                    vcCurrent.delete().complete();
                }
                vcRemove.clear();
            } else {
                vcNew = vcRemove;
            }
        } else {
            for (int i = 0; i < vcRemove.size(); i++) {
                // if someone leaving means channel is empty then remove channel and set flag
//                System.out.print("at " + i + " ");
                VoiceChannel currentVC = vcRemove.get(i);

                if (removedFlag) {
                    String channelName = name + " " + (i + 1);
//                    System.out.print("renamed channel " + currentVC.getName() + " to ");
                    currentVC.getManager().setName(channelName).complete();
//                    System.out.println(currentVC.getName());
                    vcNew.add(currentVC);

//                    Thread.sleep(1000);
                } else {

                    if (currentVC.getMembers().size() == 0) {
                        currentVC.delete().complete();
                        vcRemove.remove(i);
//                        System.out.println("removed channel " + currentVC.getName());
                        i--;
                        removedFlag = true;
                    } else {
                        vcNew.add(currentVC);
                    }
                }

//                System.out.println();
                // if removed then change names
            }
        }
        if (vcRemove.size() == 0) {
//            System.out.println(name + " removed from map");
            voiceChannelMaps.remove(name);
        } else {
            voiceChannelMaps.put(name, vcNew);
        }
    }

    private void addChannel(Guild guild,VoiceChannel vc) throws InterruptedException {
//        System.out.println("joined channel " + vc.getName());
        String ogName = vc.getName();
        String name;
//        System.out.println(ogName);
        if (endsWithNumber(ogName)) {
            name = removeNum(ogName);
        } else {
            name = ogName;
        }
//        System.out.println(name);

        // if map contains name of og vc (ex: "General")
        if (voiceChannelMaps.containsKey(name) ) {
//            System.out.println("this channel exists in map");
            List<VoiceChannel> currentChannels = voiceChannelMaps.get(name);
            VoiceChannel[] arrayofChannels = currentChannels.toArray(new VoiceChannel[currentChannels.size()]);

            // check if jumping into ogName means we need to create a new VC
            for (VoiceChannel channel : arrayofChannels) {
//                System.out.println(channel.getName());
            }

            if (!arrayofChannels[arrayofChannels.length-1].getId().equals(vc.getId()) ){
                VoiceChannel vcFromList = arrayofChannels[arrayofChannels.length -1];
//                System.out.println(vcFromList.getName());
                // if the next channel is already created
                return;
            } else {
                String channelName = name + ' ' + (arrayofChannels.length + 1);
                VoiceChannel newVC = vc.createCopy(guild).setName(channelName).complete();
                newVC.getManager().setPosition(vc.getPosition()).complete();
                currentChannels.add(newVC);
//                System.out.println("added channel " + channelName);
//                    Thread.sleep(1000);
            }

        } else {
            // add a linked list of voice channels
            List<VoiceChannel> vcList = new LinkedList<>();
            VoiceChannel newVC = vc.createCopy(guild).setName(newName(vc.getName())).complete();
            newVC.getManager().setPosition(vc.getPosition()).complete();
//            System.out.println("added channel " + newName(ogName));
            vcList.add(newVC);
            voiceChannelMaps.put(name, vcList);
//            System.out.println("added key in map " + name);
//            Thread.sleep(1000);
        }


    }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    private boolean endsWithNumber(String name) {
        String lastChar = name.substring(name.length()-1);
        return isInteger(lastChar);
    }

    private String removeNum (String name) {
        int lastChar = 0;
        for (int i = name.length(); i > 0; i--) {
            if (isInteger(name.substring(i-1,i)) || name.charAt(i-1) == ' ') {
                lastChar = i;
            } else {
                break;
            }
        }
        return name.substring(0, lastChar-1);
    }

    private String newName(String name) {
        String lastChar = name.substring(name.length()-1);
        String newName = "";
        if (isInteger(lastChar)) {
            int newNum = Integer.parseInt(lastChar) + 1;
            newName = name.substring(0,name.length() - 1) + newNum;
        } else {
            newName = name + " 1";
        }
        return newName;
    }
//    public void onMessageReceived(MessageReceivedEvent event) {
//        HashMap<String, Integer> roles = new HashMap<>();
//        String author = event.getAuthor().getId();
//
//        List<Role> role =  event.getGuild().getMemberById(author).getRoles();
//        for (Role r : role) {
//            roles.put(r.getName(),0);
//        }
//
//        Instant end = Instant.now();
//        timeElapsed = Duration.between(start, end);
//        if (timeElapsed.toMinutes() >= 5) {
//            players = new ArrayList<>();
//            started = false;
//        }
//
//        Message message = event.getMessage();
//
//
//        EmbedBuilder embedBuilder = new EmbedBuilder();
//        channel = event.getChannel();
//
//        if (event.getAuthor().getId().equals(botid) && message.getContentRaw().equals(joinMessage)) {
//
//            if (tournament_message.length() > 2) {
//                channel.deleteMessageById(tournament_message).queue();
//            }
//
//            tournament_message = message.getId();
//        }
//
//        if (event.getChannel().getName().equals("general") && !event.getAuthor().getId().equals(botid) && roles.containsKey("Player") ) {
//            Guild guild = event.getGuild();
//            String content = message.getContentRaw();
//            String[] split = content.split("\\s+", -1);
//            List<Member> mentioned = message.getMentionedMembers();
//
//
//
//            if (isTarget(split[0], botid) && split[1].toLowerCase().equals("help")) {
//
//                embedBuilder.setTitle("How this bot works", null);
//                embedBuilder.setDescription("This is a bot which helps create teams for a 2's tourneys. The lobby will be reset after 5 minutes of creating the teams. This bot can only be used by people with the player role");
//                embedBuilder.addField("Commands", "new, add, addfromvc, remove, remix, restart. All commands (except 'addfromvc') work by first @ing tourneyBot typing the command and then passing arguments", false);
//                embedBuilder.addField("new", "tourneyBot writes a message and anyone who reacts to it will be added to the tourney lobby", true);
//                embedBuilder.addField("add", "Anyone can add anyone else into the lobby by calling this command and @ing whoever they want to join", true);
//                embedBuilder.addField("addfromvc", "All players in the same voice channel as the player who calls this is added to the tournament", true);
//                embedBuilder.addField("remove", "Anyone can remove anyone else into the lobby by calling this command and @ing whoever they want to leave", true);
//                embedBuilder.addField("remix", "remixes the teams", true);
//                embedBuilder.addField("restart", "recreates the lobby", true);
//                embedBuilder.addField("end", "destroys the lobby", true);
//                embedBuilder.addField("help", "shows the help message", true);
//                embedBuilder.addField("debug", "shows who's currently in the lobby", true);
//                MessageEmbed m = embedBuilder.build();
//
//                channel.sendMessage(m).queue();
//                return;
//            }
//
//            if (isTarget(split[0], botid) && split[1].toLowerCase().equals("debug")) {
//                String s = "```" + "\n";
//                for (String p : players) {
//                    s += p + "\n";
//                    System.out.println(p);
//                }
//                s += "```";
//                channel.sendMessage(s).queue();
//                return;
//            }
//            if (isTarget(split[0], botid) && split[1].toLowerCase().equals("end")) {
//                channel.sendMessage("Lobby destroyed, thanks for that <@!" + author + ">" ).queue();
//                players = new ArrayList<>();
//                started = false;
//                return;
//            }
//            if (isTarget(split[0], botid) && split[1].toLowerCase().equals("remove")){
//                for (Member m : mentioned) {
//                    if (players.contains(getName(m))) {
//                        System.out.println("Player: " + getName(m) + " has been removed. Number of players is  : " + (players.size()+1));
//                        players.remove(getName(m));
//                        channel.sendMessage(getName(m) + " has been removed from queue").queue();
//                    }
//                }
//                if (players.size() < numofplayers) {
//                    started = false;
//                }
//                return;
//
//            }
//
//            if (!started) {
//
//                if (isTarget(split[0], botid) && split[1].toLowerCase().equals("addfromvc")) {
//                    try {
//                        VoiceChannel vc = Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel();
//                        if (vc != null) {
//                            List<Member> membersInVc = vc.getMembers();
//
//                            addPlayer(membersInVc);
//                            channel.sendMessage("```Added all players in " + vc.getName() + "```").queue();
//
//                        }
//
//                    } catch (NullPointerException ne) {
//                        ne.printStackTrace();
//                    }
//                    return;
//                }
//
//                if (isTarget(split[0], botid) && split[1].toLowerCase().equals("new")) {
//                    channel.sendMessage("```Starting new tournament```").queue();
//                    String m = joinMessage;
//
//                    channel.sendMessage(m).queue(message1 -> {message1.addReaction(EmojiParser.parseToUnicode(":thumbsup:")).queue();});
//                    return;
//                }
//                if (isTarget(split[0], botid) && split[1].toLowerCase().equals("add")){
//                   addPlayer(mentioned);
//                    return;
//
//                }
//            } else {
//                if (isTarget(split[0], botid) && split[1].toLowerCase().equals("remix")) {
//                    createTournament();
//                    return;
//                }
//                if (isTarget(split[0], botid) && split[1].toLowerCase().equals("restart")) {
//                    players = new ArrayList<>();
//                    started = false;
//                    String m = joinMessage;
//
//                    channel.sendMessage(m).queue();
//                    return;
//                }
//
//            }
//
//
//
//
//            // if next word is !r (for random) or !c (for captains)
//            // enter all players names
//
//            // (random team generator)
//            // create random 2s teams.
//            // create a bracket
//            // give :thumbsup: and :thumbsdown: to vote
//            // if more than half thumbs down then recreate the teams
//
//            // directly implement results into ladder
//
//
//
//        }
//    }
//
//    public void onMessageReactionAdd(MessageReactionAddEvent event) {
//        if (!event.getUserId().equals(botid) && event.getMessageId().equals(tournament_message)) {
//            Guild guild = event.getGuild();
//            Member member = guild.getMember((event.getUser()));
//            if (!players.contains(getName(member)) && players.size() < numofplayers) {
//                players.add(getName(member));
//                if (players.size() == numofplayers) {
//                    createTournament();
//                }
//            }
//
//        }
//
//
//    }
//    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
//        if (event.getMessageId().equals(tournament_message)) {
//            Guild guild = event.getGuild();
//            Member member = guild.getMember((event.getUser()));
//            players.remove(getName(member));
//        }
//
//    }
//
//    private String getName(Member member)
//    {
//        return member.getEffectiveName().replaceAll(" \".*\" ", " ");
//    }
//
//    private boolean isTarget(String arg, String id) {
////        for (String arg : args) {
//        if (arg.equals("<@!"+id+">")||arg.equals("<@"+id+">")) {
//            return true;
//        }
////        }
//        return false;
//    }
//
//    private void addPlayer(List<Member> members) {
//        for (Member m : members) {
//            if (!m.getId().equals(botid) && !players.contains(getName(m))) {
//                System.out.println("Player: " + getName(m) + " has been added. Number of players is  : " + (players.size()+1));
//                if (players.size() < numofplayers) {
//                    players.add(getName(m));
//                    if (players.size() == numofplayers) {
//                        createTournament();
//                    }
//                }
//            }
//
//        }
//        if (players.size() < numofplayers) {
//
//            String m = joinMessage;
//            channel.sendMessage(m).queue(message1 -> {message1.addReaction(EmojiParser.parseToUnicode(":thumbsup:")).queue();});
//        }
//    }
//    private void createTournament() {
//        //get players
//        start = Instant.now();
//        started = true;
//        List<String> currentplayers = new ArrayList<>();
//        for (String player : players) {
//            currentplayers.add(player);
//        }
//        System.out.println("Start tournament also players .size() = " + players.size());
//        Set<String> team1 = new HashSet<>();
//        Set<String> team2 = new HashSet<>();
//        Set<String> team3 = new HashSet<>();
//        Set<String> team4 = new HashSet<>();
//        Random rand = new Random();
//        String[] ordered = new String[currentplayers.size()];
//
//
//
//        int i = 0;
//        while (currentplayers.size() > 0) {
//            int r = rand.nextInt(currentplayers.size());
//            System.out.println("R is: " + r + " Player is: " + currentplayers.get(r) + " i is: " + i);
//            ordered[i] = currentplayers.get(r);
//            currentplayers.remove(r);
//            i++;
//        }
//        for (int n = 0; n < ordered.length; n++){
//            System.out.println(ordered[n]);
//        }
//        team1.add(ordered[0]);
//        team1.add(ordered[1]);
//        team2.add(ordered[2]);
//        team2.add(ordered[3]);
//        team3.add(ordered[4]);
//        team3.add(ordered[5]);
//        team4.add(ordered[6]);
//        team4.add(ordered[7]);
//
//
//        // print teams and brackets
//
//        String message = "```\nTeam 1: " + ordered[0] + " and " + ordered[1] + "\n" +
//                "Team 2: " + ordered[2] + " and " + ordered[3] + "\n" +
//                "Team 3: " + ordered[4] + " and " + ordered[5] + "\n" +
//                "Team 4: " + ordered[6] + " and " + ordered[7] + " ```";
//        channel.sendMessage(message).queue();
//        System.out.println("players.size() is now = " + players.size());
//    }
}
