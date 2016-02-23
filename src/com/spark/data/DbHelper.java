package com.spark.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;
import com.spark.service.aidl.Device;
import com.spark.service.aidl.DictDay;
import com.spark.service.aidl.NiceAlarm;
import com.spark.service.aidl.Radiation;
import com.spark.sleep.DateUtils;
import com.spark.util.Constant;
import com.spark.util.FileUtil;
import com.spark.util.Trace;
import com.spark.util.Util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Administrator on 2014/8/6.
 */
public class DbHelper<T extends Table> extends OrmLiteSqliteOpenHelper {
	private static final String TAG = DbHelper.class.getSimpleName();
	private static final String ACCOUNT = "account";
	private static final int DATABASE_VERSION = 1; 
	private static String DATABASE_NAME =  "bracelet.db";
	private static String DATABASE_NAME_JOURN = "bracelet.db-journal";
	private static String DATABASE_PATH;
	private static String DATABASE_PATH_JOURN;
	private Dao<Data, Long> dataDao;
	private Dao<Device, Long> deviceDao;
	private Dao<FetalMovement, Long> fetalMovementDao;
	private Dao<UserInfoOctober, Long> octoberLongDao;
	private Dao<NiceAlarm, Long> niceAlarmDao;
	private Dao<Radiation, Long> radiationDao;
	private Dao<Schedule, Long> scheduleLongDao;


	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		if(Constant.DEBUG){
    		String table_path = FileUtil.get_TABLE_PATH(context);
            DATABASE_PATH = table_path + DATABASE_NAME;
            DATABASE_PATH_JOURN = table_path + DATABASE_NAME_JOURN;

			try {
				File f = new File(DATABASE_PATH);
				if (!f.exists()) {
					SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(
							DATABASE_PATH, null);
					onCreate(db);
					db.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
	}

	public void deleteDB(Context mContext) {
		if(Constant.DEBUG){
			File f = mContext.getDatabasePath(DATABASE_NAME);
			if (f.exists()) {
				f.delete();
			} else {
				mContext.deleteDatabase(DATABASE_NAME);
			}

			File file = mContext.getDatabasePath(DATABASE_PATH);
			if (file.exists()) {
				file.delete();
			}

			File file2 = mContext.getDatabasePath(DATABASE_PATH_JOURN);
			if (file2.exists()) {
				file2.delete();
			}			
		}
	}

	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		// TODO Auto-generated method stub
		if(Constant.DEBUG){
			return SQLiteDatabase.openDatabase(DATABASE_PATH, null,
					SQLiteDatabase.OPEN_READWRITE);			
		}else{
			return super.getWritableDatabase();
		}

	}

	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		// TODO Auto-generated method stub
		if(Constant.DEBUG){
			return SQLiteDatabase.openDatabase(DATABASE_PATH, null,
					SQLiteDatabase.OPEN_READONLY);			
		}else{
			return super.getReadableDatabase();
		}

	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase,
			ConnectionSource connectionSource) {
		try {
			TableUtils.createTableIfNotExists(connectionSource, NiceAlarm.class);
			TableUtils.createTableIfNotExists(connectionSource, Device.class);
			TableUtils.createTableIfNotExists(connectionSource, UserInfoOctober.class);
			TableUtils.createTableIfNotExists(connectionSource, Radiation.class);
			TableUtils.createTableIfNotExists(connectionSource, Schedule.class);
			TableUtils.createTableIfNotExists(connectionSource, FetalMovement.class);
			TableUtils.createTableIfNotExists(connectionSource, Data.class);
		} catch (SQLException e) {
			Trace.e(DbHelper.class.getName(), "创建数据库失败", e);
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase,
			ConnectionSource connectionSource, int i, int i2) {
		try {
			TableUtils.dropTable(connectionSource, NiceAlarm.class, true); 
			TableUtils.dropTable(connectionSource, Device.class, true); 
			TableUtils.dropTable(connectionSource, UserInfoOctober.class, true); 
			TableUtils.dropTable(connectionSource, Radiation.class, true); 
			TableUtils.dropTable(connectionSource, Schedule.class, true); 
			TableUtils.dropTable(connectionSource, FetalMovement.class, true); 
			TableUtils.dropTable(connectionSource, Data.class, true);			
			onCreate(sqLiteDatabase, connectionSource);
			Trace.e(DbHelper.class.getName(), "更新数据库成功");
		} catch (Exception e) {
			Trace.e(DbHelper.class.getName(), "更新数据库失败", e);
			e.printStackTrace();
		}
	}	
	
	@Override
	public void close() {
		super.close();
	}

	/*********************Radiation Start***************************/
	private Dao<Radiation, Long> getRadiationDao() throws SQLException {
		if (radiationDao == null)
			radiationDao = getDao(Radiation.class);
		return radiationDao;
	}
	
	
	public List<Radiation> getRadiations(String account, String address,
			String date) throws SQLException {
		Dao<Radiation, Long> dao = getRadiationDao();
		return dao.queryBuilder().where().eq("account", account).and()
				.eq("address", address).and().eq("date_flag", date).query();
	}

	public boolean saveRadiation(Radiation radiation) throws SQLException {
		Dao<Radiation, Long> dao = getRadiationDao();
		DatabaseConnection databaseConnection = null;
		databaseConnection = dao.getConnectionSource().getReadWriteConnection();
		dao.setAutoCommit(databaseConnection, false);
		dao.create(radiation);
		dao.commit(databaseConnection);
		return true;
	}

	public boolean saveRadiations(List<Radiation> radiations)
			throws SQLException {
		Dao<Radiation, Long> dao = getRadiationDao();
		DatabaseConnection databaseConnection = null;
		databaseConnection = dao.getConnectionSource().getReadWriteConnection();
		dao.setAutoCommit(databaseConnection, false);
		for (int i = 0; i < radiations.size(); i++) {
			dao.create(radiations.get(i));
		}
		dao.commit(databaseConnection);
		return true;
	}
	/*********************Radiation End***************************/

	/*********************NiceAlarm Start***************************/
	public Dao<NiceAlarm, Long> getNiceAlarmDao() throws SQLException {
		if (niceAlarmDao == null)
			niceAlarmDao = getDao(NiceAlarm.class);
		return niceAlarmDao;
	}

	public NiceAlarm getNiceAlarm(String account, String deviceAddress,
			int number) throws Exception {
		List<NiceAlarm> result = null;
		Dao<NiceAlarm, Long> dao = getNiceAlarmDao();
		QueryBuilder<NiceAlarm, Long> query = dao.queryBuilder();
		Where<NiceAlarm, Long> where = query.where();
		where.eq(ACCOUNT, account).and().eq(Device.ADDRESS, deviceAddress)
				.and().eq("number", number);
		result = where.query();
		if (null != result && result.size() > 0) {
			return result.get(result.size() - 1);
		}
		return null;
	}
	
	public void createNiceAlarm(NiceAlarm niceAlarm) throws Exception {
		Dao<NiceAlarm, Long> dao = getNiceAlarmDao();
		Trace.e(TAG, "dataDao.create(niceAlarm) " + dao.create(niceAlarm));
	}

	public void updateNiceAlarm(NiceAlarm niceAlarm) throws Exception {
		Dao<NiceAlarm, Long> dao = getNiceAlarmDao();
		Trace.e(TAG, "dataDao.update(niceAlarm) " + dao.update(niceAlarm));
	}	
	

	public List<NiceAlarm> getNiceAlarms(String account, String deviceAddress)
			throws Exception {
		List<NiceAlarm> result = null;
		Dao<NiceAlarm, Long> dao = getNiceAlarmDao();
		QueryBuilder<NiceAlarm, Long> query = dao.queryBuilder();
		result = query.where().eq(ACCOUNT, account).and()
				.eq(Device.ADDRESS, deviceAddress).query();
		for (NiceAlarm alarm : result) {
			Trace.e(TAG, alarm.toString());
		}

		if (null != result && result.size() > 0) {
			return result;
		}
		return null;
	}
	/*********************NiceAlarm End***************************/
	
	/*********************Device Start***************************/
	private Dao<Device, Long> getDeviceDao() throws SQLException {
		if (deviceDao == null)
			deviceDao = getDao(Device.class);
		return deviceDao;
	}
	
	public Device getDevice(String account, String address) throws Exception {
		List<Device> result = null;
		Dao<Device, Long> dao = getDeviceDao();
		QueryBuilder<Device, Long> query = dao.queryBuilder();
		Where<Device, Long> where = query.where();
		where.eq(ACCOUNT, account).and().eq(Device.ADDRESS, address);
		result = where.query();
		if (null != result && result.size() > 0) {
			return result.get(0);
		}
		return null;
	}

	public void updateDevice(Device device) throws SQLException {
		Dao<Device, Long> dao = getDeviceDao();
		Trace.e(TAG, "dao.update(Device) " + dao.update(device));
	}
	
	public void createDevice(Device device) throws SQLException {
		Dao<Device, Long> dao = getDeviceDao();
		Trace.e(TAG, "dao.create(Device) " + dao.create(device));
	}
	/*********************Device End***************************/	
	/*********************UserInfoOctober Start***************************/	
	private Dao<UserInfoOctober, Long> getOctoberDao() throws SQLException {
		if (octoberLongDao == null)
			octoberLongDao = getDao(UserInfoOctober.class);
		return octoberLongDao;
	}

	public void createOctober(UserInfoOctober user) throws Exception {
		Dao<UserInfoOctober, Long> userDao = getOctoberDao();
		userDao.create(user);
	}
	
	public void updateOctober(UserInfoOctober user) throws Exception {
		Dao<UserInfoOctober, Long> userDao = getOctoberDao();
		userDao.update(user);
	}	
	
	public UserInfoOctober getOctober(String account) throws Exception {
		List<UserInfoOctober> result = null;
		Dao<UserInfoOctober, Long> dao = getOctoberDao();
		QueryBuilder<UserInfoOctober, Long> query = dao.queryBuilder();
		Where<UserInfoOctober, Long> where = query.where();
		if (StringUtils.isEmpty(account))
			return null;
		where.eq(Util.getKeyType(account), account);
		result = where.query();
		if (null != result && result.size() == 1) {
			return result.get(0);
		}
		return null;
	}
	/*********************UserInfoOctober End***************************/	
	
	/*********************Schedule Start*******************************/	
	private Dao<Schedule, Long> getScheduleDao() throws SQLException {
		if (scheduleLongDao == null)
			scheduleLongDao = getDao(Schedule.class);
		return scheduleLongDao;
	}

	public Schedule getSchedule(int begin, int end, Schedule.Type type,
			String account) throws SQLException {
		Dao<Schedule, Long> dao = getScheduleDao();
		QueryBuilder<Schedule, Long> queryBuilder = dao.queryBuilder();
		Where<Schedule, Long> where = queryBuilder.orderBy("date_flag", false)
				.where();
		List<Schedule> list = where.eq("account", account).and()
				.eq("type", type).and().between("date_flag", begin, end)
				.query();
		if (list != null && list.size() > 0) {
			Trace.e(TAG, list.get(0).toString());
			return list.get(0);
		}
		return null;
	}

	
	public void createSchedule(Schedule schedule) throws SQLException {
		Dao<Schedule, Long> dao = getScheduleDao();
		Trace.e(TAG, schedule.toString());
		dao.create(schedule);
	}

	/*********************Schedule End*******************************/	
	
	/*********************PeriodFetalMovement Start*******************************/	

	/*********************PeriodFetalMovement End*******************************/
	
	/*********************FetalMovement Start*******************************/
	private Dao<FetalMovement, Long> getFetalMovementDao() throws SQLException {
		if (fetalMovementDao == null)
			fetalMovementDao = getDao(FetalMovement.class);
		return fetalMovementDao;
	}

	public boolean saveFetalMovement(FetalMovement data) throws SQLException {
		int dateTime;
		List<FetalMovement> list;
		String account, address;
		DatabaseConnection databaseConnection = null;
		Dao<FetalMovement, Long> dao = getFetalMovementDao();
		databaseConnection = dao.getConnectionSource().getReadWriteConnection();
		dao.setAutoCommit(databaseConnection, false);
		account = data.getAccount();
		address = data.getAddress();
		dateTime = data.getDateTime();
		list = dao.queryBuilder().where().eq("account", account).and()
				.eq("address", address).and().eq("type", 0).and()
				.eq("date_time", dateTime).query();
		if (list.size() == 0) {// 不存在才添加,要不然重复添加了
			dao.create(data);
		}
		dao.commit(databaseConnection);
		return true;
	}

	public boolean saveFetalMovement(List<FetalMovement> datas)
			throws SQLException {
		int dateTime;
		List<FetalMovement> list;
		String account, address;
		DatabaseConnection databaseConnection = null;
		Dao<FetalMovement, Long> dao = getFetalMovementDao();
		databaseConnection = dao.getConnectionSource().getReadWriteConnection();
		dao.setAutoCommit(databaseConnection, false);
		for (int i = 0; i < datas.size(); i++) {
			account = datas.get(i).getAccount();
			address = datas.get(i).getAddress();
			dateTime = datas.get(i).getDateTime();
			list = dao.queryBuilder().where().eq("account", account).and()
					.eq("address", address).and().eq("type", 0).and()
					.eq("date_time", dateTime).query();
			if (list.size() == 0) {// 不存在才添加,要不然重复添加了
				dao.create(datas.get(i));
			}
		}
		dao.commit(databaseConnection);
		return true;
	}

	public List<FetalMovement> getFetalMovement(String account, String address,
			Date date) throws SQLException {
		Dao<FetalMovement, Long> dao = getFetalMovementDao();
		return dao.queryBuilder().orderBy("date_time", true).where()
				.eq("account", account).and().eq("address", address).and()
				.eq("date_flag", Util.DateToString(date)).query();
	}

	public List<FetalMovement> getFetalMovementData(String account,
			String address, int begin, int end) throws SQLException {
		Dao<FetalMovement, Long> dao = getFetalMovementDao();
		return dao.queryBuilder().orderBy("date_time", true).where()
				.between("date_time", begin, end).and().eq("type", 0).query();
	}

	public FetalMovement getLastFetalMovementStart(String account,
			String address, int begin, int end) throws SQLException {
		Dao<FetalMovement, Long> dao = getFetalMovementDao();
		List<FetalMovement> list = dao.queryBuilder()
				.orderBy("date_time", true).where()
				.between("date_time", begin, end).and().eq("type", 1).query();
		if (list.size() > 0) {
			return list.get(list.size() - 1);
		}else{
			return null;
		}
	}	
	
	public FetalMovement getLastFetalMovementEnd(String account,
			String address, int begin, int end) throws SQLException {
		Dao<FetalMovement, Long> dao = getFetalMovementDao();
		List<FetalMovement> list = dao.queryBuilder()
				.orderBy("date_time", true).where()
				.between("date_time", begin, end).and().eq("type", -1).query();
		if (list.size() > 0) {
			return list.get(list.size() - 1);
		}else{
			return null;
		}
	}	
	
	public int getFetalMovementCountOf(String account, String address, Date date)
			throws SQLException {
		Dao<FetalMovement, Long> dao = getFetalMovementDao();

		return (int) dao.queryBuilder().where().eq("account", account).and()
				.eq("address", address).and().eq("type", 0).and()
				.eq("date_flag", Util.DateToString(date)).countOf();
	}

	public boolean insertFetalMovement(FetalMovement movement)
			throws SQLException {
		Dao<FetalMovement, Long> dao = getFetalMovementDao();
		return dao.create(movement) == 1;
	}

	/********************* FetalMovement End *******************************/
	public void createData(Data data) throws Exception {
		Dao<Data, Long> dao = getDataDao();
		dao.create(data);
	}

	private Dao<Data, Long> getDataDao() throws SQLException {
		if (dataDao == null)
			dataDao = getDao(Data.class);
		return dataDao;
	}

	public List<Data> getDatas() throws Exception {
		Dao<Data, Long> dao = getDataDao();
		return dao.queryForAll();
	}

	public List<Data> getDatas(String account) throws Exception {
		List<Data> result = null;
		Dao<Data, Long> dao = getDataDao();
		QueryBuilder<Data, Long> query = dao.queryBuilder();
		Where<Data, Long> where = query.where();
		where.eq("account", account);
		result = query.query();
		if (null == result) {
			result = new ArrayList<Data>();
		}
		return result;
	}

	public List<Data> getDatas(String account, String address, String date)
			throws SQLException {
		Dao<Data, Long> dao = getDataDao();
		return dao.queryBuilder().where().eq("account", account).and()
				.eq("address", address).and().eq("date_flag", date).query();
	}

	public Data getFirstData(String account, String address)
			throws SQLException {
		Dao<Data, Long> dao = getDataDao();
		return dao.queryBuilder().where().eq("account", account).and()
				.eq("address", address).queryForFirst();
	}

	// 批量保存计步信息
	public boolean saveDatasInfo(List<Data> datas) throws SQLException {
		DatabaseConnection databaseConnection = null;
		Dao<Data, Long> dao = getDataDao();
		databaseConnection = dao.getConnectionSource().getReadWriteConnection();
		dao.setAutoCommit(databaseConnection, false);
		for (int i = 0; i < datas.size(); i++) {
			dao.create(datas.get(i));
		}
		dao.commit(databaseConnection);
		return true;
	}

	public DictDay getDatas(String account, String address, Date date)
			throws SQLException {
		Dao<Data, Long> dao = getDataDao();
		List<Data> list = dao.queryBuilder().where().eq("account", account)
				.and().eq("address", address).and().eq("date_flag", date)
				.query();
		DictDay tmpDay = new DictDay();
		long dayStart = DateUtils.getDayStartEnd(date).get(DateUtils.BEGIN)
				.getTime();
		if (list != null && list.size() > 0) {
			long time;
			for (int i = 0; i < list.size(); i++) {
				time = list.get(i).getDateTime() * 1000L;
				tmpDay.m_step += list.get(i).getSteps();
				tmpDay.m_distance += list.get(i).getDistance();
				tmpDay.m_calorie += list.get(i).getCalorie();
				tmpDay.m_duration += list.get(i).getDuration();
				int idx = (int) ((time - dayStart) / (300 * 1000));
				tmpDay.m_org_steps[idx] = list.get(i).getSteps();
			}
		}
		return tmpDay;
	}
}
