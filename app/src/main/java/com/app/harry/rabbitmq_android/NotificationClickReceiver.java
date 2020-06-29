package com.app.harry.rabbitmq_android;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * @author Jiang
 * @date 2020-06-29
 */
public class NotificationClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getBundleExtra("bundle");
        String myTest = data.getString("aaa");
        Toast.makeText(context, "我被点击了", Toast.LENGTH_LONG).show();
        //取消通知
        NotificationManager service = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        service.cancel(100);
    }
}