package asmtechnology.com.awschat.controllers;

import android.content.Context;

import java.util.ArrayList;

import asmtechnology.com.awschat.models.User;

public class ChatManager {

    public ArrayList<User> friendList;
    public ArrayList<User> potentialFriendList;

    private Context mContext;

    private static ChatManager instance = null;
    private ChatManager() {}

    public static ChatManager getInstance(Context context) {
        if(instance == null) {
            instance = new ChatManager();
            instance.friendList = new ArrayList<User>();
            instance.potentialFriendList = new ArrayList<User>();
        }

        instance.mContext = context;
        return instance;
    }

    public void clearFriendList() {
        friendList.clear();
    }

    public void  addFriend(User user) {
        friendList.add(user);
    }

    public void  clearPotentialFriendList() {
        potentialFriendList.clear();
    }

    public void  addPotentialFriend(User user) {
        potentialFriendList.add(user);
    }

}
