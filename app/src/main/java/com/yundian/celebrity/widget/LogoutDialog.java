package com.yundian.celebrity.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.View;
import android.widget.TextView;

import com.yundian.celebrity.bean.EventBusMessage;
import com.yundian.celebrity.ui.main.activity.MainActivity;
import com.yundian.celebrity.R;
import com.yundian.celebrity.base.baseapp.AppManager;
import com.yundian.celebrity.ui.wangyi.config.preference.Preferences;
import com.yundian.celebrity.ui.wangyi.login.LogoutHelper;
import com.yundian.celebrity.utils.SharePrefUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2017/6/26.
 */

public class LogoutDialog extends Dialog{
    private volatile static LogoutDialog logout;
    private LogoutDialog(Context context) {
        super(context,0);
    }

    private LogoutDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    public static LogoutDialog getInstance(final Context context) {
        if (logout == null) {
            synchronized (SharePrefUtil.class) {
                if (logout == null) {
                    logout = new LogoutDialog(context,R.style.myDialog);
                    logout.setContentView(R.layout.mach_sucess_choose);
                    TextView tvSure = (TextView) logout.findViewById(R.id.btn_sure);
                    tvSure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            logout.dismiss();
                            //logout(context);
                        }
                    });
                }
            }
        }
        return logout;
    }

    private  void logout(Context context) {
        SharePrefUtil.getInstance().clearUserInfo();
        SharePrefUtil.getInstance().clearUserLoginInfo();
        Preferences.saveUserToken("");
        LogoutHelper.logout();
//        DataCacheManager.clearDataCache();  //清空缓存
        EventBus.getDefault().postSticky(new EventBusMessage(2));  //登录取消消息
        if ((Activity)context instanceof MainActivity ==false){
            AppManager.getAppManager().finishActivity((Activity)context);
        }
//        startActivity(LoginActivity.class);
    }
}
