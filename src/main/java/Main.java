
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

        removeEmptyChannels();
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

    private void removeEmptyChannels() {
        for (Map.Entry<String, List<VoiceChannel>> entry : voiceChannelMaps.entrySet() ) {
            List<VoiceChannel> vcList = entry.getValue();
            List<VoiceChannel> vcNew = new LinkedList<>();
            for (VoiceChannel vc : vcList) {
                if (vc.getMembers().size() > 0) {
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

}
