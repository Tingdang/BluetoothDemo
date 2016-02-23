package com.spark.sleep;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.spark.data.Data;
import com.spark.service.aidl.DictDay;
import com.spark.util.Trace;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

public class DBUtil extends  SQLiteOpenHelper{//ToSDCardSQLiteOpenHelper
	//private static final String DIR="spark/table";
    private static final String name = "database.db";//数据库名称  
    private static final int version = 1;//数据库版本 
    private static final String TAG = DBUtil.class.getSimpleName();
	private static final String TABLE_ORG = "ORG_";
	private static final String TABLE_DB1 = "DB1_";
	private static final String TABLE_DB2 = "DB2_";
	public enum OrgEnum{
		id,
		tick,
		steps,
		distance,
		calorie,
		run_time,
		sleep_time
	};
	public enum DB1Enum{
		id,
		start,
		end,
		type
	};
	public enum DB2Enum{
		id,
		start,
		end,
		deep,
		light,
		sober,
		isUserSelf
	};

	private final Context context;
	
	public DBUtil(Context context) {
		//super(context, DIR, name, null, version);
		// TODO Auto-generated constructor stub
		super(context, name, null, version);
		this.context = context;
	}	    


	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}


	
	public boolean deleteDB() {
		return context.deleteDatabase(name);
	}

	private void creatDB(String address, String tableName, String className) {
		SQLiteDatabase db = getWritableDatabase();
		String sql = "";
		
		if(TextUtils.isEmpty(tableName)) {
			if(StringUtils.isEmpty(address)){
				return;
			}
			tableName = address.replace(":", "");
		}
		if(TextUtils.isEmpty(className)) {
			return;
		}
		if(className.equals(Data.class.getSimpleName())) {
			sql = "create table if not exists" + " " + TABLE_ORG + tableName + 
					"(id integer primary key autoincrement, tick integer UNIQUE, steps integer, distance integer, calorie integer, run_time integer, sleep_time integer)";
		}else if(className.equals(SleepDB1.class.getSimpleName())) {
			sql = "create table if not exists" + " " + TABLE_DB1 + tableName + 
					"(id integer primary key, start long UNIQUE, end long, type text)";
		}else if(className.equals(SleepDB2.class.getSimpleName())) {
			sql = "create table if not exists" + " " + TABLE_DB2 + tableName + 
					"(id integer primary key autoincrement, start long UNIQUE, end long, deep integer, light integer, sober integer, isUserSelf integer)";
		}

		if(!TextUtils.isEmpty(sql)) {
			try {
				db.execSQL(sql);
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean insertData(String address, Data datas) {
		if(datas == null){
			return true;
		}
		SQLiteDatabase db = getWritableDatabase();
		if(StringUtils.isEmpty(address)){
			return false;
		}
		String tableName = address.replace(":", "");
		ContentValues values = new ContentValues();

		try {
			this.creatDB(address, null, Data.class.getSimpleName());
			this.creatDB(address, null, SleepDB1.class.getSimpleName());
			this.creatDB(address, null, SleepDB2.class.getSimpleName());

//			if(datas.size() > 1){
//				String whereArgs[] = new String[2];
//				whereArgs[0] = "" + datas.get(0).getDateTime();
//				whereArgs[1] = "" + datas.get(datas.size() - 1).getDateTime();
//				db.delete(TABLE_ORG + tableName, OrgEnum.tick.toString()+">=? and " + OrgEnum.tick.toString() +"<=?", whereArgs);				
//			}else{
//				String whereArgs[] = new String[1];
//				whereArgs[0] = String.valueOf(datas.get(0).getDateTime());
//				db.delete(TABLE_ORG + tableName, OrgEnum.tick.toString() + "=?", whereArgs);	
//			}

			db.beginTransaction();
			values.put(OrgEnum.tick.toString(), datas.getDateTime());
			values.put(OrgEnum.steps.toString(), datas.getSteps());
			values.put(OrgEnum.distance.toString(), (int) datas.getDistance());
			values.put(OrgEnum.calorie.toString(), datas.getCalorie());
			values.put(OrgEnum.run_time.toString(), (int) datas.getDuration());
			values.put(OrgEnum.sleep_time.toString(), datas.getSleep());
			db.insert(TABLE_ORG + tableName, null, values);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			db.endTransaction();
			db.close();
		}

		return true;
	}	
	
	
	public boolean insertORG(String address, List<Data> datas) {
		if(datas.size() == 0){
			return true;
		}
		SQLiteDatabase db = getWritableDatabase();
		if(StringUtils.isEmpty(address)){
			return false;
		}
		String tableName = address.replace(":", "");
		ContentValues values = new ContentValues();

		try {
			this.creatDB(address, null, Data.class.getSimpleName());
			this.creatDB(address, null, SleepDB1.class.getSimpleName());
			this.creatDB(address, null, SleepDB2.class.getSimpleName());

//			if(datas.size() > 1){
//				String whereArgs[] = new String[2];
//				whereArgs[0] = "" + datas.get(0).getDateTime();
//				whereArgs[1] = "" + datas.get(datas.size() - 1).getDateTime();
//				db.delete(TABLE_ORG + tableName, OrgEnum.tick.toString()+">=? and " + OrgEnum.tick.toString() +"<=?", whereArgs);				
//			}else{
//				String whereArgs[] = new String[1];
//				whereArgs[0] = String.valueOf(datas.get(0).getDateTime());
//				db.delete(TABLE_ORG + tableName, OrgEnum.tick.toString() + "=?", whereArgs);	
//			}

			db.beginTransaction();
			for (Data data : datas) {
				values.put(OrgEnum.tick.toString(), data.getDateTime());
				values.put(OrgEnum.steps.toString(), data.getSteps());
				values.put(OrgEnum.distance.toString(), (int) data.getDistance());
				values.put(OrgEnum.calorie.toString(), data.getCalorie());
				values.put(OrgEnum.run_time.toString(), (int) data.getDuration());
				values.put(OrgEnum.sleep_time.toString(), data.getSleep());
				db.insert(TABLE_ORG + tableName, null, values);
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			db.endTransaction();
			db.close();
		}

		return true;
	}
	
	public int queryEarliestDay_ORG(String address) {
		SQLiteDatabase db = getWritableDatabase();
		if(StringUtils.isEmpty(address)){
			return 0;
		}
		String tableName = address.replace(":", "");
		Cursor cursor = null;
		int tick = 0;

		try {
			cursor = db.rawQuery("select * from" + " " + TABLE_ORG + tableName + " where id desc limit 0,1", null);
			if(null != cursor &&  cursor.getCount() > 0){
				cursor.moveToFirst();
				tick = cursor.getInt(OrgEnum.tick.ordinal());	
				cursor.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();		
			return 0;
		} finally {
			db.close();
		}
		return tick;
	}		
	
	
	public int queryLatestDay_ORG(String address) {
		SQLiteDatabase db = getWritableDatabase();
		if(StringUtils.isEmpty(address)){
			return 0;
		}
		String tableName = address.replace(":", "");
		Cursor cursor = null;
		int tick = 0;

		try {
			cursor = db.rawQuery("select * from" + " " + TABLE_ORG + tableName + " order by id desc limit 0,1", null);
			if(null != cursor &&  cursor.getCount() > 0){
				cursor.moveToFirst();
				tick = cursor.getInt(OrgEnum.tick.ordinal());	
			}
		} catch (SQLException e) {
			e.printStackTrace();	
			return 0;
		} finally {
			db.close();
		}
		return tick;
	}	
	
	
	public DictDay queryDayData_ORG(String address, Date date) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = null;
		long dayStart = DateUtils.getDayStartEnd(date).get(DateUtils.BEGIN).getTime();
		long dayEnd   = dayStart + DateUtils.DAY_SPAN;
		if(StringUtils.isEmpty(address)){
			return null;
		}		
		String tableName = address.replace(":", "");
		DictDay tmpDay = new DictDay();
		
		try {
			cursor = db.rawQuery("SELECT * FROM " + TABLE_ORG + tableName  + " WHERE tick >= " + dayStart / 1000 + " AND tick < " + dayEnd / 1000, null);
			if(cursor.getCount() == 0) {
				return null;
			}
			while(cursor.moveToNext())
			{
				long time = cursor.getInt(OrgEnum.tick.ordinal()) * 1000l;
				int step = cursor.getInt(OrgEnum.steps.ordinal());
				int distance = cursor.getInt(OrgEnum.distance.ordinal());
				int calorie = cursor.getInt(OrgEnum.calorie.ordinal());
				int duration = cursor.getInt(OrgEnum.run_time.ordinal());
				
				tmpDay.m_step += step;
				tmpDay.m_distance += distance;
				tmpDay.m_calorie += calorie;
				tmpDay.m_duration += duration;
				
				int idx = (int)((time - dayStart) / (300*1000));
				tmpDay.m_org_steps[idx] = step;
			}
			for(int i = 0; i < tmpDay.m_org_steps.length; i++) {
				tmpDay.m_step_hours[i / 12] += tmpDay.m_org_steps[i];
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			db.close();
		}

		return tmpDay;
	}
		
	public List<Data> queryRawData_ORG(String address, int startTick) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = null;
		if(StringUtils.isEmpty(address)){
			return null;
		}		
		String tableName = address.replace(":", "");
		List<Data> datas = new ArrayList<Data>();
		long startTime = startTick;
		long endTime = System.currentTimeMillis() / 1000;

		try {
			// 第一步-->从sleepDB1中获取临界点信息,并删除临界点
			cursor = db.rawQuery("select * from" + " " + TABLE_DB1 + tableName, null);
			if(null != cursor){
				int num = cursor.getCount();
				Trace.e(TAG, "queryRawData_ORG---->num:" + num);
				if (num != 0) {
					do {
						cursor.moveToPosition(num - 1);
						String str = cursor.getString(DB1Enum.type.ordinal());
						db.delete(TABLE_DB1 + tableName, "id=?", new String[] {String.valueOf(num)});
						if (SleepState.sober.toString().equals(str) || SleepState.noWear.toString().equals(str)) {
							startTime = cursor.getLong(DB1Enum.start.ordinal());
							break;
						}
					} while (--num > 0);
					Trace.e(TAG, "queryRawData_ORG---->last record startTime" + startTime);
				}				
			}


			// 第二步-->从ORG中获取最新的原始数据与临界数据
			cursor = db.rawQuery("select * from" + " " + TABLE_ORG + tableName + " where tick >=" + startTime + " and tick <= " + endTime, null);
			while (cursor.moveToNext()) {
				int tick = cursor.getInt(OrgEnum.tick.ordinal());
				int sleep_time = cursor.getInt(OrgEnum.sleep_time.ordinal());
				Data data = new Data(tick, sleep_time);
				datas.add(data);
			}
			cursor.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			db.close();
		}

		return datas;
	}
	
	public boolean insertDB1(String address, List<SleepDB1> sleeps) {
		SQLiteDatabase db = getWritableDatabase();
		if(StringUtils.isEmpty(address)){
			return false;
		}		
		String tableName = address.replace(":", "");
		ContentValues values = new ContentValues();

		try {
			db.beginTransaction();
			for (SleepDB1 sleep : sleeps) {
				values.put(DB1Enum.start.toString(), sleep.getDateFirst());
				values.put(DB1Enum.end.toString(), sleep.getDateLast());
				values.put(DB1Enum.type.toString(), sleep.getState().toString());
				db.insert(TABLE_DB1 + tableName, null, values);
			}
			db.setTransactionSuccessful();

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			db.endTransaction();
			db.close();
		}

		return true;
	}
	
	public long queryRawTick_DB2(String address) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = null;
		if(StringUtils.isEmpty(address)){
			return 0;
		}		
		String tableName = address.replace(":", "");
		long rawTick = 0;

		try {
			cursor = db.rawQuery("select * from" + " " + TABLE_DB2 + tableName, null);
			int num = cursor.getCount();
			Trace.e(TAG, "queryRawTick_DB2---->num:" + num);
			if(num != 0) {
				cursor.moveToLast();
				rawTick = cursor.getLong(DB2Enum.end.ordinal());
				rawTick = 1000l * rawTick + DateUtils.DAY_SPAN;
			}else {
				cursor = db.rawQuery("select * from" + " " + TABLE_DB1 + tableName, null);
				cursor.moveToFirst();
				rawTick = 1000l * cursor.getLong(DB1Enum.start.ordinal());
			}
		}catch(SQLException e) {
			e.printStackTrace();
			return 0;
		} finally {
			Trace.e(TAG, "queryRawTick_DB2---->rawTick:" + rawTick);
			db.close();
		}

		return rawTick;
	}
	
	public List<SleepDB1> queryDB1(String address, Date date){
		SQLiteDatabase db = getWritableDatabase();
		Cursor cursor = null;
		if(StringUtils.isEmpty(address)){
			return null;
		}		
		String tableName = address.replace(":", "");
		Map<String, Date> map = DateUtils.getDayStartEnd(date);
		List<SleepDB1> sleepDB1s = new ArrayList<SleepDB1>();
		long startTime = 0;
		long endTime = 0;
		
		try {
			//第一步-->从sleepDB1中获取与当天有交集的睡眠段
			startTime = map.get(DateUtils.BEGIN).getTime() / 1000;
			endTime   = map.get(DateUtils.END).getTime() / 1000;
			String sql = "select * from" + " " + TABLE_DB1 + tableName + " where (end >= " + startTime + " and end <= " + endTime + ") or " + 
					"(start >= " + startTime + " and start <= " + endTime + ") or " +
					"(start < " + startTime + " and end > " + endTime + ")";
			cursor = db.rawQuery(sql, null);
			while(cursor.moveToNext()) {
				int id = cursor.getInt(DB1Enum.id.ordinal());
				long start = cursor.getLong(DB1Enum.start.ordinal());
				long end = cursor.getLong(DB1Enum.end.ordinal());
				SleepState state = SleepState.noWear;
				String str = cursor.getString(DB1Enum.type.ordinal());
				if(SleepState.sober.toString().equals(str)) {
					state = SleepState.sober;
				}else if(SleepState.deepSleep.toString().equals(str)) {
					state = SleepState.deepSleep;
				}else if(SleepState.lightSleep.toString().equals(str)) {
					state = SleepState.lightSleep;
				}
				SleepDB1 sleep = new SleepDB1(start, end, state);
				sleep.setId(id);
				sleepDB1s.add(sleep);
			}
			cursor.close();
			if (sleepDB1s.size() == 0) {
				return sleepDB1s;
			}
			
			//第二步-->对睡眠段进行昨日睡眠数据的添加
			long startId = sleepDB1s.get(0).getId();
			long dateFirst = sleepDB1s.get(0).getDateFirst();
			SleepState sleepState = sleepDB1s.get(0).getState();
			if((dateFirst < startTime) &&
					((sleepState == SleepState.deepSleep) || (sleepState == SleepState.lightSleep))) {
				for(long id = startId; id > 1; ) {
					--id;
					cursor = db.rawQuery("select * from" + " " + TABLE_DB1 + tableName + " where id=" + id, null);
					cursor.moveToFirst();
					SleepState state = SleepState.noWear;
					String str = cursor.getString(DB1Enum.type.ordinal());
					if(SleepState.sober.toString().equals(str)) {
						state = SleepState.sober;
					}else if(SleepState.deepSleep.toString().equals(str)) {
						state = SleepState.deepSleep;
					}else if(SleepState.lightSleep.toString().equals(str)) {
						state = SleepState.lightSleep;
					}
					
					if(state == SleepState.deepSleep ||
							state == SleepState.lightSleep) {
						long start = cursor.getLong(DB1Enum.start.ordinal());
						long end = cursor.getLong(DB1Enum.end.ordinal());
						
						SleepDB1 sleep = new SleepDB1(start, end, state);
						sleepDB1s.add(0, sleep);
						cursor.close();
					} else {
						cursor.close();
						break;
					}
				}
			}
			
			//第三步-->修正每天睡眠的结束点
			if(sleepDB1s.size() != 0) {
				int lastDex = sleepDB1s.size() - 1;
				if(sleepDB1s.get(lastDex).getDateLast() > endTime) {
					sleepDB1s.get(lastDex).setDateLast(endTime);
				}	
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			db.close();
		}

		return sleepDB1s;
	}
	
	public boolean insertDB2(String address, List<SleepDB2> sleeps) {
		SQLiteDatabase db = getWritableDatabase();
		if(StringUtils.isEmpty(address)){
			return false;
		}		
		String tableName = address.replace(":", "");
		ContentValues values = new ContentValues();

		try {
			db.beginTransaction();
			for (SleepDB2 sleep : sleeps) {
				values.put(DB2Enum.start.toString(), sleep.getStartTime());
				values.put(DB2Enum.end.toString(), sleep.getEndTime());
				values.put(DB2Enum.deep.toString(), sleep.getDeepSleep());
				values.put(DB2Enum.light.toString(), sleep.getLightSleep());
				values.put(DB2Enum.sober.toString(), sleep.getSoberCnt());
				values.put(DB2Enum.isUserSelf.toString(), 0);
				db.insert(TABLE_DB2 + tableName, null, values);
			}
			db.setTransactionSuccessful();

		}catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		finally {
			db.endTransaction();
			db.close();
		}
		
		return true;
	}
	
	public SleepDB2 queryDB2(String address, Date date) {
		SQLiteDatabase db = getWritableDatabase();
		if(StringUtils.isEmpty(address)){
			return null;
		}
		String tableName = address.replace(":", "");
		Map<String, Date> map = DateUtils.getDayStartEnd(date);
		long startTime = map.get(DateUtils.BEGIN).getTime() / 1000;
		long endTime   = map.get(DateUtils.END).getTime() / 1000;
		SleepDB2 sleepDB2 = new SleepDB2();
		
		try {
			Cursor cursor = db.rawQuery("select * from" + " " + TABLE_DB2 + tableName + " where end >=" + startTime + " and end <= " + endTime, null);
			int num = cursor.getCount();
			Trace.e(TAG, "queryDB2---->num:" + num);
			if(num != 0) {
				cursor.moveToFirst();
				long start = cursor.getLong(DB2Enum.start.ordinal());
				long end = cursor.getLong(DB2Enum.end.ordinal());
				int deep = cursor.getInt(DB2Enum.deep.ordinal());
				int light = cursor.getInt(DB2Enum.light.ordinal());
				int sober = cursor.getInt(DB2Enum.sober.ordinal());
				sleepDB2.setStartTime(start);
				sleepDB2.setEndTime(end);
				sleepDB2.setDeepSleep(deep);
				sleepDB2.setLightSleep(light);
				sleepDB2.setSoberCnt(sober);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			db.close();
		}
		return sleepDB2;
	}
	
	
	public void deleteDB1(String address){
		SQLiteDatabase db = getWritableDatabase();
		if(StringUtils.isEmpty(address)){
			return;
		}		
		String tableName = address.replace(":", "");	
		try {
			db.delete(TABLE_DB1 + tableName, null, null);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			db.close();
		}
	}
	
	public void deleteDB2(String address){
		SQLiteDatabase db = getWritableDatabase();
		if(StringUtils.isEmpty(address)){
			return;
		}
		String tableName = address.replace(":", "");	
		try {
			db.delete(TABLE_DB2 + tableName, null, null);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			db.close();
		}
	}	
}
