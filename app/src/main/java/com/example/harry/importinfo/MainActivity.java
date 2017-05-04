package com.example.harry.importinfo;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import a_vcard.android.provider.Contacts;
import a_vcard.android.syncml.pim.VDataBuilder;
import a_vcard.android.syncml.pim.VNode;
import a_vcard.android.syncml.pim.vcard.ContactStruct;
import a_vcard.android.syncml.pim.vcard.VCardComposer;
import a_vcard.android.syncml.pim.vcard.VCardException;
import a_vcard.android.syncml.pim.vcard.VCardParser;


public class MainActivity extends AppCompatActivity {

    private TextView showInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showInfo = (TextView) findViewById(R.id.showInfo);
        showInfo.setText("importing");
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*****try read file from assets****/
//        try {
//            InputStreamReader inputReader = new InputStreamReader( getResources().getAssets().open("info.vcf") );
//            BufferedReader bufReader = new BufferedReader(inputReader);
//            String line="";
//            String Result="";
//            while((line = bufReader.readLine()) != null)
//                Result += line;
//            Log.i("main",Result);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        /*ContactInfo.ContactHandler infoHandler = new ContactInfo.ContactHandler();
        try {

            InputStream infoStream = null;
            infoStream = getResources().getAssets().open("info.vcf");   //import form assets
            //infoStream = new FileInputStream( (String)Environment.getExternalStorageDirectory()+"/info.vcf" );  //import form storage file
            List<ContactInfo> infoList = infoHandler.restoreContacts(infoStream);

            for(ContactInfo item : infoList){
                Log.i("info",item.ContactsToString());          //log the infoList of contacts
                infoHandler.addContacts(this,item);             //import contact one by one
            }
            showInfo.setText("import done");
        } catch (Exception e) {
            e.printStackTrace();
            showInfo.setText("error in importing");
        }*/

        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        //protocol="0" address="SMS address:1" date="315982369064" type="1" subject="null" body="Message Number:1"
        // toa="null" sc_toa="null" service_center="null" read="0" status="-1" locked="0" date_sent="0"
        // readable_date="Jan 6, 1980 4:52:49 AM" contact_name="(Unknown)"
        values.put("protocol", "0");
        values.put("address", "18221208972");//指定短信的发件人
        values.put("date", System.currentTimeMillis());
        values.put("type", "1");
        values.put("subject", "qianhuan");
        values.put("body", "哈哈哈哈哈");
        //values.put("toa", "null");
        //values.put("sc_toa", "null");
        //values.put("service_center", "null");
        values.put("read", "1");
        values.put("status", "-1");
        //values.put("locked", "0");
        values.put("date_sent", "0");
        //values.put("readable_date","Jan 6, 1980 4:52:49 AM");
        //values.put("contact_name", "(Unknown)");



        resolver.insert(Uri.parse("content://sms"), values);
        showInfo.setText("import done");
    }

}




