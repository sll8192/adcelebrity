package com.yundian.celebrity.ui.wangyi.chatroom.adapter;

import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.netease.nim.uikit.common.ui.imageview.ImageViewEx;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseQuickAdapter;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.yundian.celebrity.R;
import com.yundian.celebrity.ui.wangyi.chatroom.helper.ChatRoomHelper;

/**
 * Created by huangjun on 2016/12/9.
 */
public class ChatRoomsAdapter extends BaseQuickAdapter<ChatRoomInfo, BaseViewHolder> {
    private final static int COUNT_LIMIT = 10000;

    public ChatRoomsAdapter(RecyclerView recyclerView) {
        super(recyclerView, R.layout.chat_room_item, null);
    }

    @Override
    protected void convert(BaseViewHolder holder, ChatRoomInfo room, int position, boolean isScrolling) {
        // bg
        holder.getConvertView().setBackgroundResource(R.drawable.list_item_bg_selecter);
        // cover
        ImageViewEx coverImage = holder.getView(R.id.cover_image);
        ChatRoomHelper.setCoverImage(room.getRoomId(), coverImage);
        // name
        holder.setText(R.id.tv_name, room.getName());
        // online count
        TextView onlineCountText = holder.getView(R.id.tv_online_count);
        setOnlineCount(onlineCountText, room);
    }

    private void setOnlineCount(TextView onlineCountText, ChatRoomInfo room) {
        if (room.getOnlineUserCount() < COUNT_LIMIT) {
            onlineCountText.setText(String.valueOf(room.getOnlineUserCount()));
        } else if (room.getOnlineUserCount() >= COUNT_LIMIT) {
            onlineCountText.setText(String.format("%.1f", room.getOnlineUserCount() / (float) COUNT_LIMIT) + "万");
        }
    }
}
