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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
    protected void onResume() {
        super.onResume();
        try {
            InputStream infoStream = this.readFileFromAssets("info.vcf");
            this.importContact(infoStream);
        } catch (Exception e) {
            e.printStackTrace();
            showInfo.setText("errer in importing Contacts");
            Log.i("errer log","errer in importing Contacts");
            return;
        }

        try {
            InputStream smsStream = this.readFileFromAssets("sms.xml");
            this.importSMS(smsStream);
        } catch (Exception e) {
            e.printStackTrace();
            showInfo.setText("errer in importing Messages");
            Log.i("errer log","errer in importing Messages");
            return;
        }
        showInfo.setText("import done");
    }


    public InputStream readFileFromAssets(String file) throws Exception{
        InputStream inputStream = null;
        inputStream = getResources().getAssets().open(file);
        //BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream));
        return inputStream;
    }

    public InputStream readFileFromStorage(String file) throws Exception{
        InputStream inputStream = null;
        inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + file);
        //BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream));
        return inputStream;
    }

    public void importContact(InputStream infoStream) throws Exception{
        ContactInfo.ContactHandler infoHandler = new ContactInfo.ContactHandler();
        List<ContactInfo> infoList = infoHandler.restoreContacts(infoStream);

        for(ContactInfo item : infoList){
            Log.i("Contacts",item.ContactsToString());          //log the infoList of contacts
            infoHandler.addContacts(this,item);             //import contact one by one
        }
    }

    public void importSMS(InputStream smsStream) throws Exception{
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        Uri uri = Uri.parse("content://sms");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(smsStream);
        Element rootElement = doc.getDocumentElement();
        NodeList items = rootElement.getElementsByTagName("sms");
        int num = Integer.valueOf(rootElement.getAttribute("count"));
        for(int i=0;i<num;i++ ){
            Node item = items.item(i);
            NamedNodeMap attributes = item.getAttributes();

            Log.i("Messages",attributes.getNamedItem("address").getNodeValue());
            Log.i("Messages",attributes.getNamedItem("body").getNodeValue());

            values.put("protocol", attributes.getNamedItem("protocol").getNodeValue());
            values.put("address", attributes.getNamedItem("address").getNodeValue());
            values.put("date", attributes.getNamedItem("date").getNodeValue());
            values.put("type", attributes.getNamedItem("type").getNodeValue());
            values.put("subject", attributes.getNamedItem("subject").getNodeValue());
            values.put("body", attributes.getNamedItem("body").getNodeValue());
            values.put("read", attributes.getNamedItem("read").getNodeValue());
            values.put("status", attributes.getNamedItem("status").getNodeValue());

            resolver.insert(uri, values);  //  add to content://sms
            values.clear();
        }
    }
}




