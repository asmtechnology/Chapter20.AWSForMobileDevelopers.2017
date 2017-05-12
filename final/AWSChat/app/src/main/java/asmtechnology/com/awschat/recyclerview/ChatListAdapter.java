package asmtechnology.com.awschat.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import asmtechnology.com.awschat.R;
import asmtechnology.com.awschat.controllers.ChatManager;
import asmtechnology.com.awschat.interfaces.RecyclerViewHolderListener;
import asmtechnology.com.awschat.models.Chat;
import asmtechnology.com.awschat.models.Message;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private RecyclerViewHolderListener mListener;
    private Chat mChat;
    private String mCurrentUserId;

    private int SENT_TEXT_VIEW = 0;
    private int SENT_IMAGE_VIEW = 1;
    private int RECEIVED_TEXT_VIEW = 2;
    private int RECEIVED_IMAGE_VIEW = 3;

    public class SentTextViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public int itemIndex;

        public SentTextViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_text);
        }
    }

    public class SentImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public int itemIndex;

        public SentImageViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.imageView);
        }
    }

    public class ReceivedTextViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public int itemIndex;

        public ReceivedTextViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_text);
        }
    }

    public class ReceivedImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public int itemIndex;

        public ReceivedImageViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.imageView);
        }
    }

    public ChatListAdapter(Context context, RecyclerViewHolderListener listener, Chat chat, String currentUserId) {
        mContext = context;
        mListener = listener;
        mCurrentUserId = currentUserId;
        mChat = chat;
    }

    public void setChat(Chat c) {
        mChat = c;
    }

    @Override
    public int getItemViewType(int position) {

        if (mChat == null) {
            return SENT_TEXT_VIEW;
        }

        ChatManager chatManager = ChatManager.getInstance(mContext);
        ArrayList<Message> messages = chatManager.conversations.get(mChat);

        Message message = messages.get(position);
        String messageText = message.getMessage_text();
        String senderId = message.getSender_id();

        if (messageText.equals("NA")) {
            // image
            if (senderId.equals(mCurrentUserId)) {
                return SENT_IMAGE_VIEW;
            } else {
                return RECEIVED_IMAGE_VIEW;
            }
        } else {
            // text
            if (senderId.equals(mCurrentUserId)) {
                return SENT_TEXT_VIEW;
            } else {
                return RECEIVED_TEXT_VIEW;
            }
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == SENT_TEXT_VIEW) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sent_text_row, parent, false);
            return new ChatListAdapter.SentTextViewHolder(itemView);

        } else if (viewType == SENT_IMAGE_VIEW) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sent_image_row, parent, false);
            return new ChatListAdapter.SentImageViewHolder(itemView);

        } else if (viewType == RECEIVED_TEXT_VIEW) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.received_text_row, parent, false);
            return new ChatListAdapter.ReceivedTextViewHolder(itemView);

        } else if (viewType == RECEIVED_IMAGE_VIEW) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.received_image_row, parent, false);
            return new ChatListAdapter.ReceivedImageViewHolder(itemView);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (mChat == null) {
            return;
        }

        ChatManager chatManager = ChatManager.getInstance(mContext);
        ArrayList<Message> messages = chatManager.conversations.get(mChat);

        Message message = messages.get(position);
        String messageText = message.getMessage_text();
        String senderId = message.getSender_id();

        if (holder.getItemViewType() == SENT_TEXT_VIEW) {
            ((SentTextViewHolder) holder).itemIndex = position;
            ((SentTextViewHolder) holder).messageText.setText(messageText);

        } else if (holder.getItemViewType() == SENT_IMAGE_VIEW) {
            ((SentImageViewHolder) holder).itemIndex = position;

        } else if (holder.getItemViewType() == RECEIVED_TEXT_VIEW) {
            ((ReceivedTextViewHolder) holder).itemIndex = position;
            ((ReceivedTextViewHolder) holder).messageText.setText(messageText);

        } else if (holder.getItemViewType() == RECEIVED_IMAGE_VIEW) {
            ((ReceivedImageViewHolder) holder).itemIndex = position;
        }

    }

    @Override
    public int getItemCount() {

        if (mChat == null) {
            return 0;
        }

        ChatManager chatManager = ChatManager.getInstance(mContext);
        ArrayList<Message> messages = chatManager.conversations.get(mChat);

        return messages.size();
    }
}
