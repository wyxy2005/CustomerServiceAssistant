package com.pcjh.assistant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.mengma.asynchttp.JsonUtil;
import com.pcjh.assistant.entity.Matrial;
import com.pcjh.assistant.entity.Tag;
import com.pcjh.assistant.entity.Users;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by szhua on 2016/10/18.
 */
public class DbManager {
    private DatabaseHelper helper;
    private SQLiteDatabase db;
    private Context context ;


    private DbManager(){
    }

    public DbManager(Context context)
    {
        helper = new DatabaseHelper(context);
        // 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        if(db==null)
        db = helper.getWritableDatabase();
    }

    public void addCollectMatrial(Matrial matrial ,String createtime){
        db.beginTransaction();
        try {
            db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_NAME_MATARIAL_COLLECT +"( 'createtime' , 'material_id','json' )"
                    + " VALUES(? , ? , ? )", new Object[]{createtime, matrial.getId(), JsonUtil.pojo2json(matrial)});
            db.setTransactionSuccessful(); // 设置事务成功完成
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("szhua",e.toString());
        }finally {
            db.endTransaction();
        }
    }

    public void addUpdateTime(String uin ,String updatetime){
        db.beginTransaction();
        try {
            db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_NAME_UPDATE_TIME +"( 'lastCreateTime' , 'uin' )"
                    + " VALUES(? , ? )", new Object[]{updatetime, uin});
            db.setTransactionSuccessful(); // 设置事务成功完成
        } finally {
            db.endTransaction();
        }
    }

    public void addTags(List<Tag> tags){
        db.beginTransaction();
        try
        {
            for (Tag tag : tags)
            {
                db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_NAME_TAG +"( 'type' , 'name' )"
                        + " VALUES(? , ?)", new Object[]{tag.getType(), tag.getName()});
                // 带两个参数的execSQL()方法，采用占位符参数？，把参数值放在后面，顺序对应
                // 一个参数的execSQL()方法中，用户输入特殊字符时需要转义
                // 使用占位符有效区分了这种情况
            }
            db.setTransactionSuccessful(); // 设置事务成功完成
        }
        finally
        {
            db.endTransaction(); // 结束事务
        }
    }




    /**
     *向数据库中添加微信用户信息 ；
     * @param user
     *
     */
    public void addWxUserInfoToDb(Users user)
    {
        db.beginTransaction(); // 开始事务
        try
        {
                db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_NAME_User +"('nickName','face','dbPath','password','uin')"
                        + " VALUES(?, ?, ?, ?, ? )", new Object[]{user.getNickName(),
                        user.getFace(), user.getDbPath(),user.getPassword(),user.getUin() });
            db.setTransactionSuccessful(); // 设置事务成功完成
        }
        finally
        {
            db.endTransaction(); // 结束事务
        }
    }





    /**
     * update person's age
     *
     * @param person
     */
    public void updateUser(Users person)
    {
        ContentValues cv = new ContentValues();
        cv.put("nickName", person.getNickName());
        db.update(DatabaseHelper.TABLE_NAME_User, cv, "nickName = ?",
                new String[] { person.getNickName() });
    }

    /**
     * 更新用户的上传聊天记录的时间 ;
     * @param uin
     * @param updatetime
     */
    public void updateUpdateTime(String uin ,String updatetime){
        ContentValues cv =new ContentValues() ;
        cv.put("lastCreateTime",updatetime);
        db.update(DatabaseHelper.TABLE_NAME_UPDATE_TIME,cv,"uin = ?",new String[]{uin});
    }

    /**
     * 删除标签 ;
     * @param tags
     */
    public void deleteTags (ArrayList<Tag> tags){
        for (Tag tag : tags) {
            db.delete(DatabaseHelper.TABLE_NAME_TAG,"name = ?" ,new String[]{tag.getName()});
        }
    }

    /**
     * 删除素材 ;
     * @param matrial
     */
    public void deleteMatrial(Matrial matrial){
        db.delete(DatabaseHelper.TABLE_NAME_MATARIAL_COLLECT,"material_id = ?" ,new String[]{matrial.getId()});
    }

    /**
     * 获得指定时间的收藏素材 ;
     * @param date
     * @return
     */
    public List<Matrial> queryCollectMatrials(String date){

        ArrayList<Matrial> matrials =new ArrayList<>() ;
        String sql ="SELECT * FROM " + DatabaseHelper.TABLE_NAME_MATARIAL_COLLECT +" where createtime = '"+date+"'" ;
        Cursor c = db.rawQuery(sql,
                null);
        while (c.moveToNext())
        {
            Matrial matrial =null;
            String json =c.getString(c.getColumnIndex("json")) ;
            try {
             matrial =JsonUtil.json2pojo(json,Matrial.class) ;
            } catch (IOException e){
                e.printStackTrace();
            }
            if (matrial!=null) {
                matrials.add(matrial);
            }
        }
        c.close();
        return  matrials ;
    }

    /**
     * 获得收藏的素材;
     * @return
     */
    public List<Matrial> queryCollectMatrials(){
        ArrayList<Matrial> matrials =new ArrayList<>() ;
        Cursor c = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME_MATARIAL_COLLECT  ,
                null);
        while (c.moveToNext())
        {
            Matrial matrial1 =null;
            String json =c.getString(c.getColumnIndex("json")) ;
            String createtime =c.getString(c.getColumnIndex("createtime"));
            Log.i("szhua",createtime);
            try {
            matrial1 =JsonUtil.json2pojo(json,Matrial.class) ;
            } catch (IOException e){
                e.printStackTrace();
            }
            if (matrial1!=null) {
                matrials.add(matrial1);
            }
        }
        c.close();
        return  matrials ;
    }

    /**
     * 查询时候有当前材料的收藏;
     * @param id
     * @return
     */
    public boolean checkIsCollect(String id){
        Cursor c = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME_MATARIAL_COLLECT +" where material_id = "+ id,
                null);
        ArrayList<Matrial> matrials =new ArrayList<>() ;
        while (c.moveToNext())
        {
            Matrial matrial = null;
            String  json =c.getString(c.getColumnIndex("json")) ;
            try {
            matrial=   JsonUtil.json2pojo(json,Matrial.class) ;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (matrial!=null) {
                matrials.add(matrial);
            }
        }
        c.close();
        if(matrials.size()>0){
            return  true ;
        }
        return  false ;
    }

    /*
     获得数据库中的tag ;
     */
    public List<Tag> queryTag(){
        ArrayList<Tag> tags =new ArrayList<Tag>() ;
        Cursor c = queryTheCursor(DatabaseHelper.TABLE_NAME_TAG);
        while (c.moveToNext())
        {
            Tag tag =new Tag() ;
            String name =c.getString(c.getColumnIndex("name")) ;
            String type =c.getString(c.getColumnIndex("type")) ;
            tag.setName(name);
            tag.setType(type);
            tags.add(tag) ;
        }
        c.close();
        return tags;
    }


    /**
     * 获取数据库中储存的用户 ;
     * @return List<Person>
     */
    public  List<Users> query()
    {
        ArrayList<Users> persons = new ArrayList<Users>();
        Cursor c = queryTheCursor(DatabaseHelper.TABLE_NAME_User);
        while (c.moveToNext())
        {
            Users person = new Users();
            person.id = c.getInt(c.getColumnIndex("id"));
            person.nickName = c.getString(c.getColumnIndex("nickName"));
            person.face =c.getString(c.getColumnIndex("face")) ;
            person.uin =c.getString(c.getColumnIndex("uin")) ;
            person.dbPath =c.getString(c.getColumnIndex("dbPath")) ;
            person.password =c.getString(c.getColumnIndex("password")) ;
            persons.add(person);
        }
        c.close();
        return persons;
    }

    /**
     * @return Cursor
     */
    public Cursor queryTheCursor(String tableName)
    {
      //  Log.d(AppConstants.LOG_TAG, "DBManager --> queryTheCursor");
        Cursor c = db.rawQuery("SELECT * FROM " + tableName,
                null);
        return c;
    }
    /**
     * close database
     */
    public void closeDB()
    {
        // 释放数据库资源
        db.close();
    }
}
