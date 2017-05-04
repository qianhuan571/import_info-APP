package com.example.harry.importinfo;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
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

/**
 * Created by HarryQian on 2017/5/3.
 */

public class ContactInfo {

    /** MUST exist */
    private String name; // 姓名

    /** 联系人电话信息 */
    public static class PhoneInfo{
        /** 联系电话类型 */
        public int type;
        /** 联系电话 */
        public String number;
    }

    /** 联系人邮箱信息 */
    public static class EmailInfo{
        /** 邮箱类型 */
        public int type;
        /** 邮箱 */
        public String email;
    }

    private List<PhoneInfo> phoneList = new ArrayList<PhoneInfo>(); // 联系号码
    private List<EmailInfo> emailList = new ArrayList<EmailInfo>(); // Email

    /**
     * 构造联系人信息
     * @param name 联系人姓名
     */
    public ContactInfo(String name) {
        this.name = name;
    }

    /** 姓名 */
    public String getName() {
        return name;
    }
    /** 姓名 */
    public ContactInfo setName(String name) {
        this.name = name;
        return this;
    }
    /** 联系电话信息 */
    public List<PhoneInfo> getPhoneList() {
        return phoneList;
    }
    /** 联系电话信息 */
    public ContactInfo setPhoneList(List<PhoneInfo> phoneList) {
        this.phoneList = phoneList;
        return this;
    }
    /** 邮箱信息 */
    public List<EmailInfo> getEmail() {
        return emailList;
    }
    /** 邮箱信息 */
    public ContactInfo setEmail(List<EmailInfo> email) {
        this.emailList = email;
        return this;
    }

    public String ContactsToString() {
        String phoneStr="";
        String emailStr="";
        for(PhoneInfo item : phoneList){
            phoneStr = phoneStr + '/' + item.number;
        }
        for(EmailInfo item : emailList){
            emailStr = emailStr + '/' + item.email;
        }
        return "{name: "+name+", number: "+ phoneStr +", email: "+ emailStr +"}";
    }

    /**
     * 联系人
     *         备份/还原操作
     * @author LW
     *
     */
    public static class ContactHandler {

        private static ContactHandler instance_ = new ContactHandler();

        /** 获取实例 */
        public static ContactHandler getInstance(){
            return instance_;
        }

        /**
         * 获取联系人指定信息
         * @param projection 指定要获取的列数组, 获取全部列则设置为null
         * @return
         * @throws Exception
         */
        public Cursor queryContact(Activity context, String[] projection){
            // 获取联系人的所需信息
            Cursor cur = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, null);
            return cur;
        }

        /**
         * 获取联系人信息
         * @param context
         * @return
         */
        public List<ContactInfo> getContactInfo(Activity context){
            List<ContactInfo> infoList = new ArrayList<ContactInfo>();

            Cursor cur = queryContact(context, null);

            if(cur.moveToFirst()){
                do{

                    // 获取联系人id号
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    // 获取联系人姓名
                    String displayName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    ContactInfo info = new ContactInfo(displayName);// 初始化联系人信息

                    // 查看联系人有多少电话号码, 如果没有返回0
                    int phoneCount = cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    if(phoneCount>0){

                        Cursor phonesCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id , null, null);

                        if(phonesCursor.moveToFirst()) {
                            List<ContactInfo.PhoneInfo> phoneNumberList = new ArrayList<ContactInfo.PhoneInfo>();
                            do{
                                // 遍历所有电话号码
                                String phoneNumber = phonesCursor.getString(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                // 对应的联系人类型
                                int type = phonesCursor.getInt(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                                // 初始化联系人电话信息
                                ContactInfo.PhoneInfo phoneInfo = new ContactInfo.PhoneInfo();
                                phoneInfo.type=type;
                                phoneInfo.number=phoneNumber;

                                phoneNumberList.add(phoneInfo);
                            }while(phonesCursor.moveToNext());
                            // 设置联系人电话信息
                            info.setPhoneList(phoneNumberList);
                        }
                    }

                    // 获得联系人的EMAIL
                    Cursor emailCur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID+"="+id, null, null);

                    if(emailCur.moveToFirst()){
                        List<ContactInfo.EmailInfo> emailList = new ArrayList<ContactInfo.EmailInfo>();
                        do{
                            // 遍历所有的email
                            String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA1));
                            int type = emailCur.getInt(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));

                            // 初始化联系人邮箱信息
                            ContactInfo.EmailInfo emailInfo=new ContactInfo.EmailInfo();
                            emailInfo.type=type;    // 设置邮箱类型
                            emailInfo.email=email;    // 设置邮箱地址

                            emailList.add(emailInfo);
                        }while(emailCur.moveToNext());

                        info.setEmail(emailList);
                    }

                    //Cursor postalCursor = getContentResolver().query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + "=" + id, null, null);
                    infoList.add(info);
                }while(cur.moveToNext());
            }
            return infoList;
        }

        /**
         * 备份联系人
         */
        public void backupContacts(Activity context, List<ContactInfo> infos){

            try {

                String path = Environment.getExternalStorageDirectory() + "/contacts.vcf";

                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path),"UTF-8");

                VCardComposer composer = new VCardComposer();

                for (ContactInfo info : infos)
                {
                    ContactStruct contact = new ContactStruct();
                    contact.name = info.getName();
                    // 获取联系人电话信息, 添加至 ContactStruct
                    List<ContactInfo.PhoneInfo> numberList = info
                            .getPhoneList();
                    for (ContactInfo.PhoneInfo phoneInfo : numberList)
                    {
                        contact.addPhone(phoneInfo.type, phoneInfo.number,
                                null, true);
                    }
                    // 获取联系人Email信息, 添加至 ContactStruct
                    List<ContactInfo.EmailInfo> emailList = info.getEmail();
                    for (ContactInfo.EmailInfo emailInfo : emailList)
                    {
                        contact.addContactmethod(Contacts.KIND_EMAIL,
                                emailInfo.type, emailInfo.email, null, true);
                    }
                    String vcardString = composer.createVCard(contact,
                            VCardComposer.VERSION_VCARD30_INT);
                    writer.write(vcardString);
                    writer.write("\n");

                    writer.flush();
                }
                writer.close();

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (VCardException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Toast.makeText(context, "备份成功！", Toast.LENGTH_SHORT).show();
        }


        /**
         * 获取vCard文件中的联系人信息
         * @return
         */
        public List<ContactInfo> restoreContacts(InputStream file) throws Exception {
            List<ContactInfo> contactInfoList = new ArrayList<ContactInfo>();

            VCardParser parse = new VCardParser();
            VDataBuilder builder = new VDataBuilder();

            //Log.i("Contact","before readfile");
            BufferedReader reader = new BufferedReader(new InputStreamReader(file, "UTF-8"));
            //Log.i("Contact","readfile");
            String vcardString = "";
            String line;
            while((line = reader.readLine()) != null) {
                vcardString += line + "\n";
            }
            reader.close();

            boolean parsed = parse.parse(vcardString, "UTF-8", builder);

            if(!parsed){
                throw new VCardException("Could not parse vCard file: "+ file);
            }

            List<VNode> pimContacts = builder.vNodeList;

            for (VNode contact : pimContacts) {

                ContactStruct contactStruct=ContactStruct.constructContactFromVNode(contact, 1);
                // 获取备份文件中的联系人电话信息
                List<ContactStruct.PhoneData> phoneDataList = contactStruct.phoneList;
                List<ContactInfo.PhoneInfo> phoneInfoList = new ArrayList<ContactInfo.PhoneInfo>();
                for(ContactStruct.PhoneData phoneData : phoneDataList){
                    ContactInfo.PhoneInfo phoneInfo = new ContactInfo.PhoneInfo();
                    phoneInfo.number=phoneData.data;
                    phoneInfo.type=phoneData.type;
                    phoneInfoList.add(phoneInfo);
                }

                // 获取备份文件中的联系人邮箱信息
                List<ContactStruct.ContactMethod> emailList = contactStruct.contactmethodList;
                List<ContactInfo.EmailInfo> emailInfoList = new ArrayList<ContactInfo.EmailInfo>();
                // 存在 Email 信息
                if (null!=emailList)
                {
                    for (ContactStruct.ContactMethod contactMethod : emailList)
                    {
                        if (Contacts.KIND_EMAIL == contactMethod.kind)
                        {
                            ContactInfo.EmailInfo emailInfo = new ContactInfo.EmailInfo();
                            emailInfo.email = contactMethod.data;
                            emailInfo.type = contactMethod.type;
                            emailInfoList.add(emailInfo);
                        }
                    }
                }
                ContactInfo info = new ContactInfo(contactStruct.name).setPhoneList(phoneInfoList).setEmail(emailInfoList);
                contactInfoList.add(info);
            }

            return contactInfoList;
        }


        /**
         * 向手机中录入联系人信息
         * @param info 要录入的联系人信息
         */
        public void addContacts(Activity context, ContactInfo info){
            ContentValues values = new ContentValues();
            //首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获取系统返回的rawContactId
            Uri rawContactUri = context.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
            long rawContactId = ContentUris.parseId(rawContactUri);

            //往data表入姓名数据
            values.clear();
            values.put(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId);
            values.put(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, info.getName());
            context.getContentResolver().insert(
                    android.provider.ContactsContract.Data.CONTENT_URI, values);

            // 获取联系人电话信息
            List<ContactInfo.PhoneInfo> phoneList = info.getPhoneList();
            /** 录入联系电话 */
            for (ContactInfo.PhoneInfo phoneInfo : phoneList) {
                values.clear();
                values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                // 设置录入联系人电话信息
                values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneInfo.number);
                values.put(ContactsContract.CommonDataKinds.Phone.TYPE, phoneInfo.type);
                // 往data表入电话数据
                context.getContentResolver().insert(
                        android.provider.ContactsContract.Data.CONTENT_URI, values);
            }

            // 获取联系人邮箱信息
            List<ContactInfo.EmailInfo> emailList = info.getEmail();

            /** 录入联系人邮箱信息 */
            for (ContactInfo.EmailInfo email : emailList) {
                values.clear();
                values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
                // 设置录入的邮箱信息
                values.put(ContactsContract.CommonDataKinds.Email.DATA, email.email);
                values.put(ContactsContract.CommonDataKinds.Email.TYPE, email.type);
                // 往data表入Email数据
                context.getContentResolver().insert(
                        android.provider.ContactsContract.Data.CONTENT_URI, values);
            }

        }

    }
}
