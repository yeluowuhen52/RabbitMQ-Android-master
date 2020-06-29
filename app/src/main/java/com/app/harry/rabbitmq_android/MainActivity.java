package com.app.harry.rabbitmq_android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView showMessageTv;
    private EditText contentEt;
    private StringBuilder sb = new StringBuilder();

    //自己配置发送接受队列名称和绑定key
    private final String sendQueueOne = "testOne";
    private final String sendRoutingKey = "two.rabbit.ok";
    private final String receiveQueueOne = "TestDirectQueue";
    private final String receiveQueueTwo = "testTwo";
    private final String receiveRoutingKey = "TestDirectRouting";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contentEt = findViewById(R.id.et_content);
        showMessageTv = findViewById(R.id.tv_show_message);
        showMessageTv.setMovementMethod(ScrollingMovementMethod.getInstance());

        RabbitMQUtil.initService("172.17.28.201", 5672, "mytest", "mytest");
        RabbitMQUtil.initExchange("MyTestDirectExchange", "direct");
    }

    @Override
    protected void onDestroy() {
        RabbitMQUtil.getInstance().close();
        super.onDestroy();
    }

    public void sendQueue(View view) {
        final String message = contentEt.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(MainActivity.this, "消息不能为空！！！", Toast.LENGTH_SHORT).show();
            return;
        }

        RabbitMQUtil.getInstance().sendQueueMessage(message, sendQueueOne, new RabbitMQUtil.SendMessageListener() {
            @Override
            public void sendMessage(final boolean isSuccess) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isSuccess) {
                            sb.append("发送队列消息：").append(message).append("\n");
                            showMessageTv.setText(sb);
                            contentEt.setText("");

                        } else {
                            Toast.makeText(MainActivity.this, "发送消息失败，请检查网络后稍后再试！！！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });
    }

    public void sendRouting(View view) {
        final String message = contentEt.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(MainActivity.this, "消息不能为空！！！", Toast.LENGTH_SHORT).show();
            return;
        }

        RabbitMQUtil.getInstance().sendRoutingKeyMessage(message, sendRoutingKey, new RabbitMQUtil.SendMessageListener() {
            @Override
            public void sendMessage(final boolean isSuccess) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isSuccess) {
                            sb.append("发送routing消息：").append(message).append("\n");
                            contentEt.setText("");
                            showMessageTv.setText(sb);

                        } else {
                            Toast.makeText(MainActivity.this, "发送消息失败，请检查网络后稍后再试！！！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    public void listenQueue(View view) {


        RabbitMQUtil.getInstance().receiveQueueMessage(receiveQueueOne, new RabbitMQUtil.ReceiveMessageListener() {
            @Override
            public void receiveMessage(String message) {
                sb.append("收到了queue消息：").append(message).append("\n");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //1.获取通知管理器类
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        /**
                         * 兼容Android版本8.0系统
                         */
                        String channeId = "1";
                        String channelName = "default";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationChannel channel = new NotificationChannel(channeId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                            channel.enableLights(true);         // 开启指示灯，如果设备有的话
                            channel.setLightColor(Color.RED);   // 设置指示灯颜色
                            channel.setShowBadge(true);         // 检测是否显示角标
                            notificationManager.createNotificationChannel(channel);
                        }
                        //2.构建通知类
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "1");
                        builder.setSmallIcon(R.mipmap.ic_launcher);//设置小图标
                        builder.setContentTitle("微信");//标题
                        builder.setContentText(sb);//内容
                        builder.setWhen(System.currentTimeMillis());    //时间

                        Intent intent = new Intent(MainActivity.this, NotificationClickReceiver.class);

                        Bundle bundle = new Bundle();
                        bundle.putString("aaa", "aaa");
                        intent.setAction("MessageHandleService");
                        intent.putExtra("bundle", bundle);

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
                        builder.setContentIntent(pendingIntent);

                        //3.获取通知
                        Notification notification = builder.build();
//                        4. 发送通知
                        notificationManager.notify(100, notification);
                        showMessageTv.setText(sb);
                    }
                });
            }
        });
    }

    public void listenRouting(View view) {
        RabbitMQUtil.getInstance().receiveRoutingKeyMessage(receiveRoutingKey, new RabbitMQUtil.ReceiveMessageListener() {
            @Override
            public void receiveMessage(String message) {
                sb.append("收到了routing消息：").append(message).append("\n");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showMessageTv.setText(sb);
                    }
                });
            }
        });
    }
}
