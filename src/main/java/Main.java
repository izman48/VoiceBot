
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;

import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;


/// hashmap of og vc and matches id to linked list
public class Main extends ListenerAdapter {
    private static JDA builder;
    private static List<String> myID = new ArrayList<>();

    private static String botID = "751047187626197012";
    private Map<String, List<VoiceChannel>> voiceChannelMaps = new HashMap<>();

    public static void main(String[] args) throws Exception {
        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("../Token/voiceToken")), "UTF8"));
            builder = new JDABuilder(AccountType.BOT).setToken(reader.readLine()).build();
            reader.close();
            myID.add("399120960906854411"); //me
            myID.add("259031785759965185"); //walden
            builder.addEventListener(new Main());
//            System.out.println("started");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        Guild guild = event.getGuild();
        VoiceChannel vc = event.getChannelJoined();
//        System.out.println(vc.getPosition());
        try {
            addChannel(guild, vc);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
//        System.out.println("Moved");
        Guild guild = event.getGuild();
        VoiceChannel vcLeft = event.getChannelLeft();
        VoiceChannel vcJoined = event.getChannelJoined();

        try {
            removeChannel(guild, vcLeft);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // if someone leaves a vc before they are moved
        try {
            addChannel(guild, vcJoined);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String user = event.getAuthor().getId();
        MessageChannel channel = event.getChannel();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        Guild guild = event.getGuild();
        String[] words = event.getMessage().getContentRaw().split("\\s+");
        for (String id: myID) {
            if (user.equals(id)) {

                if (isTarget(words[0], botID) && words[1].toLowerCase().equals("help")) {

                    embedBuilder.setTitle("Bot Commands", null);
                    embedBuilder.setDescription("For the extremely likely event that someone breaks voice channels these commands can be used by admins");
                    embedBuilder.addField("Commands", "reset, empty, print, empty-safe", false);
                    embedBuilder.addField("reset", "it removes all channels with a number at the end and resets the memory with a new hashmap", true);
                    embedBuilder.addField("empty", "it removes all channels with a number at the end", true);
                    embedBuilder.addField("print", "prints the contents of the memory", true);
                    embedBuilder.addField("empty-safe", "it iterates through the hashmap and removes any channels that shouldn't have been there, then updates the memory", true);

                    MessageEmbed m = embedBuilder.build();

                    channel.sendMessage(m).complete();
                    return;
                }
                if (isTarget(words[0], botID) && words[1].toLowerCase().equals("reset")) {
                    printMem(channel);
                    removeAllChannels(guild);
                    voiceChannelMaps = new HashMap<>();
                    return;
                }
                if (isTarget(words[0], botID) && words[1].toLowerCase().equals("empty")) {
                    printMem(channel);
                    removeAllChannels(guild);
                    return;
                }
                if (isTarget(words[0], botID) && words[1].toLowerCase().equals("print")) {
                    printMem(channel);
                    return;
                }
                if (isTarget(words[0], botID) && words[1].toLowerCase().equals("empty-safe")) {
                    printMem(channel);
                    removeEmptyChannels();
                    return;
                }
            }
        }
//        removeEmptyChannels();
    }
    private void printMem(MessageChannel channel) {
        Set<String> mapSet = voiceChannelMaps.keySet();
        if (mapSet.isEmpty()) {
            return;
        }
        String print = "";
        for (String key: mapSet ) {
            print += ("KEY: " + key + " ");
            List<VoiceChannel> vcList = voiceChannelMaps.get(key);
            print += ("VALUE:");
            for (VoiceChannel vc : vcList) {
                print += (" " + vc.getName());
            }
        }
        channel.sendMessage(print).complete();
    }

    private void removeChannel(Guild guild, VoiceChannel vc) throws InterruptedException {

        //Version 1.1

        // When someone leave channel "Channel Name n" it checks if that channel is empty
        // if it is then it removes that channel and renames everything else to be consistent
        // if channel name doesn't end with n then they were just moved so it does nothing


//        String ogName = vc.getName();
//        if (!endsWithNumber(ogName)) {
//            //they have been moved
//            return;
//        }
//        String name = removeNum(ogName);
//        List<VoiceChannel> vcRemove = voiceChannelMaps.get(name);
//        List<VoiceChannel> vcNew = new LinkedList<>();
//        boolean removedFlag = false;
//
//        if (vc.getMembers().size() == 0) {
//            for (int i = 0; i < vcRemove.size(); i++) {
//                // if someone leaving means channel is empty then remove channel and set flag
////                System.out.print("at " + i + " ");
//                VoiceChannel currentVC = vcRemove.get(i);
//
//                if (removedFlag) {
//                    String channelName = name + " " + (i + 1);
////                    System.out.print("renamed channel " + currentVC.getName() + " to ");
//                    currentVC.getManager().setName(channelName).complete();
////                    System.out.println(currentVC.getName());
//                    vcNew.add(currentVC);
//                } else {
//
//                    if (currentVC.getMembers().size() == 0) {
//                        currentVC.delete().complete();
//                        vcRemove.remove(i);
////                        System.out.println("removed channel " + currentVC.getName());
//                        i--;
//                        removedFlag = true;
//                    } else {
//                        vcNew.add(currentVC);
//                    }
//                }
//
////                System.out.println();
//                // if removed then change names
//            }
//
//            if (vcNew.size() == 0) {
////            System.out.println(name + " removed from map");
//                voiceChannelMaps.remove(name);
//            } else {
//                voiceChannelMaps.put(name, vcNew);
//            }
//        }









//        /*Version 1.0
//        System.out.println("left channel " + vc.getName());
        String ogName = vc.getName();
        String name;
        if (endsWithNumber(ogName)) {
            name = removeNum(ogName);
        } else {
            name = ogName;
        }
        if (!voiceChannelMaps.containsKey(name)) {
            removeEmptyChannels();
            return;
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
        if (vcNew.size() == 0) {
//            System.out.println(name + " removed from map");
            voiceChannelMaps.remove(name);
        } else {
            voiceChannelMaps.put(name, vcNew);
        }


    }

    private void addChannel(Guild guild,VoiceChannel vc) throws InterruptedException {
        //Version 1.1

        // When someone joins channel "Channel Name" it creates a new channel called "Channel Name n" and moves them there
        // If it ends with a number do nothing

//        String ogName = vc.getName();
//        if (endsWithNumber(ogName)) {
//            return;
//        }
//        // only creates when it joins an og channel
//        List<VoiceChannel> vcList;
//        VoiceChannel newVC;
//        if (voiceChannelMaps.containsKey(ogName)) {
//            vcList = voiceChannelMaps.get(ogName);
//            int newNum = vcList.size()+1;
//            newVC = vc.createCopy(guild).setName(ogName + " " + newNum).complete();
//
//
//        } else {
//            // add a linked list of voice channels
//            vcList = new LinkedList<>();
//            newVC = vc.createCopy(guild).setName(ogName + " " + 1).complete();
////            System.out.println("added channel " + newName(ogName));
////            System.out.println("added key in map " + name);
//        }
////        System.out.println("OG VC POSITION IS: " + vc.getPosition());
//        int decrement = 0;
//        int newPosition = vc.getPositionRaw();
//        // if we left a voice channel
////        if (vcLeft != null) {
////            // if that vc still has channels
////            if (voiceChannelMaps.containsKey(vcLeft.getName())) {
////
////                decrement = voiceChannelMaps.get(vcLeft.getName()).size()+1;
////            } else {
////                // if vcList no longer exists (there was only 1 channel when we left)
////                // if the vc we left was before the vc we joined then we need to make up for cache
////                if (vcLeft.getPosition() < vc.getPosition())
////                    decrement = 1;
////            }
////
////            newPosition = (vc.getPosition() <= decrement) ? decrement : vc.getPositionRaw()-decrement;
////        } else {
////            newPosition = vc.getPositionRaw();
////        }
//
//        newVC.getManager().setPosition(newPosition).complete();
//        guild.moveVoiceMember(member, newVC).complete();
//        vcList.add(newVC);
//        voiceChannelMaps.put(ogName, vcList);








//        /*
        //Version 1.0
//        System.out.println("joined channel " + vc.getName());
        String ogName = vc.getName();
        String name;
//        System.out.println(ogName);
        if (endsWithNumber(ogName)) {
            name = removeNum(ogName);
        } else {
            name = ogName;
        }
        VoiceChannel ogVC = guild.getVoiceChannelsByName(name,false).get(0);
//        System.out.println(name);

        // if map contains name of og vc (ex: "General")
        if (voiceChannelMaps.containsKey(name) ) {
//            System.out.println("this channel exists in map");
            List<VoiceChannel> currentChannels = voiceChannelMaps.get(name);
            VoiceChannel[] arrayofChannels = currentChannels.toArray(new VoiceChannel[currentChannels.size()]);

            // check if jumping into ogName means we need to create a new VC
            VoiceChannel lastChannel = arrayofChannels[arrayofChannels.length-1];
            if ((lastChannel.getId().equals(vc.getId()) && ogVC.getMembers().size() != 0) || (ogName.equals(name) && lastChannel.getMembers().size() != 0)){

                String channelName = name + ' ' + (arrayofChannels.length + 1);
                VoiceChannel newVC = vc.createCopy(guild).setName(channelName).complete();
                newVC.getManager().setPosition(ogVC.getPositionRaw() + arrayofChannels.length-1).complete();
                currentChannels.add(newVC);
//                System.out.println("added channel " + channelName);
            } else {
                return;
            }

        } else {
            // add a linked list of voice channels
            List<VoiceChannel> vcList = new LinkedList<>();
            VoiceChannel newVC = vc.createCopy(guild).setName(newName(vc.getName())).complete();
            newVC.getManager().setPosition(vc.getPositionRaw()).complete();
//            System.out.println("added channel " + newName(ogName));
            vcList.add(newVC);
            voiceChannelMaps.put(name, vcList);
//            System.out.println("added key in map " + name);
        }
//        */

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

    private void removeAllChannels(Guild guild) {
        List<VoiceChannel> allChannels = guild.getVoiceChannels();
        for (VoiceChannel voice: allChannels ) {
            if (endsWithNumber(voice.getName())) {
                voice.delete().queue();
            }
        }
    }
    private void removeEmptyChannels() {
        for (Map.Entry<String, List<VoiceChannel>> entry : voiceChannelMaps.entrySet() ) {
            List<VoiceChannel> vcList = entry.getValue();

            int emptyChannel = 0;
            boolean flag = false;
            for (int i = 0; i < vcList.size(); i++) {
                VoiceChannel vc = vcList.get(i);
                if (vc.getMembers().size() == 0 && flag) {
                    break;
                }
                if (vc.getMembers().size() == 0 && !flag) {
                    flag = true;
                    emptyChannel = i;
                }
            }


            List<VoiceChannel> vcNew = new LinkedList<>();
            for (int i = 0; i < vcList.size(); i++) {
                VoiceChannel vc = vcList.get(i);
                if (i == emptyChannel) {
                    vcNew.add(vc);
                    continue;
                }    if (vc.getMembers().size() > 0) {
                    vcNew.add(vc);
                } else {
                    vc.delete().complete();
                }
            }
            if (vcNew.size() == 0) {
                voiceChannelMaps.remove(entry.getKey());
            } else {
                voiceChannelMaps.put(entry.getKey(), vcNew);

            }
        }
    }
    private boolean isTarget(String arg, String id) {
//        for (String arg : args) {
        if (arg.equals("<@!"+id+">")||arg.equals("<@"+id+">")) {
            return true;
        }
//        }
        return false;
    }


}
