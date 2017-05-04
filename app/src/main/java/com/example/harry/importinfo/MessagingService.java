package com.example.harry.importinfo;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;

/**
 * Created by HarryQian on 2017/5/4.
 */

public class MessagingService extends IntentService {

    /**
     * 一个构造函数是必须的，并且你必须调用父类的IntentService(String)以传入工作线程的名字.
     */
    public MessagingService() {
        super("HelloIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }
}
