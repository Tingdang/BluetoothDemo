package com.spark.data;

import android.util.SparseArray;
import android.util.SparseIntArray;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.spark.util.Constant;

import java.io.Serializable;
import java.util.*;

@DatabaseTable(tableName = "t_user_october")
public class UserInfoOctober extends UserInfo {

	private static final long serialVersionUID = -3459727399910742407L;

	@DatabaseField
	private float stature;
	@DatabaseField
	private float weight;

	@DatabaseField(columnName = "expected_date_of_confinement")
	private int expectedDate;
	
	private PregnantSate pregnantState;
	
	private SparseArray<Float> heightUterusMin = new SparseArray<Float>(21);
	private SparseArray<Float> heightUterusMax = new SparseArray<Float>(21);
	private SparseArray<Float> heightUterusAvg = new SparseArray<Float>(21);

	private SparseIntArray abdominalCircumferenceMin = new SparseIntArray(6);
	private SparseIntArray abdominalCircumferenceMax = new SparseIntArray(6);
	private SparseIntArray abdominalCircumferenceAvg = new SparseIntArray(6);
	
	public UserInfoOctober() {
		heightUterusMax.put(20, 20.5f);
		heightUterusMax.put(21, 21.5f);
		heightUterusMax.put(22, 22.5f);
		heightUterusMax.put(23, 23.5f);
		heightUterusMax.put(24, 24.5f);
		heightUterusMax.put(25, 25.5f);
		heightUterusMax.put(26, 26.5f);
		heightUterusMax.put(27, 27.5f);
		heightUterusMax.put(28, 28.5f);
		heightUterusMax.put(29, 29.5f);
		heightUterusMax.put(30, 30.5f);
		heightUterusMax.put(31, 31.5f);
		heightUterusMax.put(32, 32.5f);
		heightUterusMax.put(33, 33.5f);
		heightUterusMax.put(34, 34.5f);
		heightUterusMax.put(35, 35.5f);
		heightUterusMax.put(36, 36.5f);
		heightUterusMax.put(37, 37.5f);
		heightUterusMax.put(38, 38.5f);
		heightUterusMax.put(39, 38.5f);
		heightUterusMax.put(40, 38.5f);

		heightUterusMin.put(20, 16f);
		heightUterusMin.put(21, 17f);
		heightUterusMin.put(22, 18f);
		heightUterusMin.put(23, 19f);
		heightUterusMin.put(24, 20f);
		heightUterusMin.put(25, 21f);
		heightUterusMin.put(26, 21.5f);
		heightUterusMin.put(27, 22.5f);
		heightUterusMin.put(28, 23f);
		heightUterusMin.put(29, 23.5f);
		heightUterusMin.put(30, 24f);
		heightUterusMin.put(31, 25f);
		heightUterusMin.put(32, 26f);
		heightUterusMin.put(33, 27f);
		heightUterusMin.put(34, 27.5f);
		heightUterusMin.put(35, 28.5f);
		heightUterusMin.put(36, 29f);
		heightUterusMin.put(37, 29.5f);
		heightUterusMin.put(38, 30.5f);
		heightUterusMin.put(39, 31f);
		heightUterusMin.put(40, 32f);

		heightUterusAvg.put(20, (16f + 20.5f) / 2);
		heightUterusAvg.put(21, (17f + 21.5f) / 2);
		heightUterusAvg.put(22, (18f + 22.5f) / 2);
		heightUterusAvg.put(23, (19f + 23.5f) / 2);
		heightUterusAvg.put(24, (20f + 24.5f) / 2);
		heightUterusAvg.put(25, (21f + 25.5f) / 2);
		heightUterusAvg.put(26, (21.5f + 26.5f) / 2);
		heightUterusAvg.put(27, (22.5f + 27.5f) / 2);
		heightUterusAvg.put(28, (23f + 28.5f) / 2);
		heightUterusAvg.put(29, (23.5f + 29.5f) / 2);
		heightUterusAvg.put(30, (24f + 30.5f) / 2);
		heightUterusAvg.put(31, (25f + 31.5f) / 2);
		heightUterusAvg.put(32, (26f + 32.5f) / 2);
		heightUterusAvg.put(33, (27f + 33.5f) / 2);
		heightUterusAvg.put(34, (27.5f + 34.5f) / 2);
		heightUterusAvg.put(35, (28.5f + 35.5f) / 2);
		heightUterusAvg.put(36, (29f + 36.5f) / 2);
		heightUterusAvg.put(37, (29.5f + 37.5f) / 2);
		heightUterusAvg.put(38, (30.5f + 38.5f) / 2);
		heightUterusAvg.put(39, (31f + 38.5f) / 2);
		heightUterusAvg.put(40, (32f + 38.5f) / 2);

		abdominalCircumferenceMin.put(5, 76);
		abdominalCircumferenceMin.put(6, 80);
		abdominalCircumferenceMin.put(7, 82);
		abdominalCircumferenceMin.put(8, 84);
		abdominalCircumferenceMin.put(9, 86);
		abdominalCircumferenceMin.put(10, 89);

		abdominalCircumferenceMax.put(5, 89);
		abdominalCircumferenceMax.put(6, 91);
		abdominalCircumferenceMax.put(7, 94);
		abdominalCircumferenceMax.put(8, 95);
		abdominalCircumferenceMax.put(9, 98);
		abdominalCircumferenceMax.put(10, 100);

		abdominalCircumferenceAvg.put(5, 82);
		abdominalCircumferenceAvg.put(6, 85);
		abdominalCircumferenceAvg.put(7, 87);
		abdominalCircumferenceAvg.put(8, 89);
		abdominalCircumferenceAvg.put(9, 92);
		abdominalCircumferenceAvg.put(10, 94);
	}	
	
	public float getStature() {
		return this.stature;
	}

	public void setStature(float stature) {
		this.stature = stature;
	}

	public float getWeight() {
		return this.weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public float getAnticipateWeight() {
		int[] a = getWeekAndDay((int) (new Date().getTime() / 1000L));
		return (float) ((getMaxWeight(a[0]) + getMinWeight(a[0])) / 2f);
	}

	public long getBeginPregnant() {
		return getExpectedDateOfConfinement() * 1000L - 280 * Constant.DAY;
	}

	public void setPregnantState(PregnantSate pregnantSate) {
		this.pregnantState = pregnantSate;
	}

	public int getExpectedDateOfConfinement() {
		return expectedDate;
	}

	public int getDay(int date) {
		GregorianCalendar calendar = new GregorianCalendar();
		Date date1 = new Date();
		date1.setTime(expectedDate * 1000L);
		calendar.setTime(date1);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		long start = expectedDate * 1000L - 280 * Constant.DAY;
		long toDay = date * 1000L - start;
		int day = (int) (toDay / Constant.DAY);
		return day;
	}

	public int[] getWeekAndDay(int date) {
//		GregorianCalendar calendar = new GregorianCalendar();
//		Date date1 = new Date();
//		date1.setTime(expectedDate * 1000L);
//		calendar.setTime(date1);
//		calendar.set(Calendar.HOUR_OF_DAY, 23);
//		calendar.set(Calendar.MINUTE, 59);
//		calendar.set(Calendar.SECOND, 59);
//		date1.setTime(calendar.getTimeInMillis());
		
		long start = expectedDate * 1000L - 280 * Constant.DAY;
		int day = (int) ((expectedDate * 1000L - date * 1000L) / Constant.DAY);
		long toDay = date * 1000L - start;
		day = (int) (toDay / Constant.DAY);
		int mod = 1 + day % 7, week = 1 + day / 7;
		return new int[] { week, mod };
	}

	public int getCurrentWeekDayBegin() {
		return getWeekDayBeginAndEnd(getWeekAndDay((int) (new Date().getTime() / 1000L))[0])[0];
	}

	public int[] getWeekDayBeginAndEnd(int week) {
		week -= 1;
		long temp = expectedDate * 1000L;
		Date date = new Date();
		long temp1 = week * 7 * Constant.DAY + (temp - 280L * Constant.DAY);// 某孕周开始时间＝怀孕开始时间（怀孕开始时间＝预产期－280天）+（某孕周-1）*7天
		temp1 += Constant.DAY;
		date.setTime(temp1);
		return new int[] { (int) (date.getTime() / 1000L),
				(int) ((date.getTime() + 7 * Constant.DAY) / 1000L) };
	}

	/**
	 * @return 当前孕月开始时间
	 */
	public int getCurrentMonthBegin() {
		long temp = expectedDate * 1000L;
		Date date = new Date();
		long temp1 = date.getTime() - (temp - 280L * Constant.DAY);// 当前孕周天数＝当前时间减去怀孕开始时间（怀孕开始时间＝预产期－28天）
		long day = ((temp1 / Constant.DAY) % 28);//
		date.setTime(date.getTime() - day * Constant.DAY);
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		date.setTime(calendar.getTimeInMillis());
		return (int) (date.getTime() / 1000L);
	}

	/**
	 * @param date
	 *            指定日期
	 * @return 获取孕月和天 arr【0】：孕月，arr【1】天
	 */
	public int[] getMonthAndDayOfDate(int date) {
		long pregnantStart = getBeginPregnant();
		long dayL = date * 1000L - pregnantStart;// 怀孕已有天数
		int day = (int) (dayL / Constant.DAY);
		System.out.println("怀孕已有天数:" + day);
		int month = day / 28;
		day = day % 28;
		month++;// 孕月
		if (day == 0) {
			day = 28;
			month--;
		}
		return new int[] { month, day };
	}

	public int[] getMonthBeginAndEnd(int month) {
		long pregnantStart = getBeginPregnant();
		int day = 28 * month;
		int monthL = day / 28;
		long start = pregnantStart + 28 * Constant.DAY * monthL;
		long end = start + 28 * Constant.DAY;
		return new int[] { (int) (start / 1000L), (int) (end / 1000L) };
	}

	/**
	 * @return al出生，pr备孕，ea早期，me中期，ad晚期
	 */
	public PregnantSate getPregnantState(Date curDate) {
		long temp = expectedDate;
		/**
		 * 孕早期，m1-m3 <=90 孕中期，m4-m6 孕晚期，m7-m10 >240
		 */
		Date date = new Date();
		date.setTime(temp * 1000L);
		long current = curDate.getTime();
		temp = date.getTime();
		if (pregnantState != PregnantSate.already
				&& pregnantState != PregnantSate.prepare) {
			if (temp > (280 * Constant.DAY + current)) {
				pregnantState = PregnantSate.prepare;
			} else if (temp <= (current + 90 * Constant.DAY)) {
				pregnantState = PregnantSate.early;
			} else if (temp < (current + 240 * Constant.DAY)) {
				pregnantState = PregnantSate.metaphase;
			} else {
				pregnantState = PregnantSate.advanced;
			}
		}
		return pregnantState;
	}

	public void setExpectedDateOfConfinement(int expectedDate) {
		this.expectedDate = expectedDate;
	}

	public double getMinWeight(int week) {
		if (getBMI() == UserInfoOctober.BMI.fat) {
			return getWeight() + week * 7.0 / 40;
		} else if (getBMI() == UserInfoOctober.BMI.thin) {
			return getWeight() + week * 12.5 / 40;
		} else if (getBMI() == UserInfoOctober.BMI.kuantaqsytzo) {
			return getWeight() + week * 5.0 / 40;
		} else {
			return getWeight() + week * 11.5 / 40;
		}
	}
	
	public double getMaxWeight(int week) {
		if (getBMI() == UserInfoOctober.BMI.fat) {
			return getWeight() + week * 11.5 / 40;
		} else if (getBMI() == UserInfoOctober.BMI.thin) {
			return getWeight() + week * 18.0 / 40;
		} else if (getBMI() == UserInfoOctober.BMI.kuantaqsytzo) {
			return getWeight() + week * 9.0 / 40;
		} else {
			return getWeight() + week * 16.0 / 40;
		}
	}

	public int getMaxAbdominalCircumference(int month) {
		return abdominalCircumferenceMax.get(month,100);
	}

	public int getAvgAbdominalCircumference(int month) {
		return abdominalCircumferenceAvg.get(month,82);
	}

	public int getMinAbdominalCircumference(int month) {
		return abdominalCircumferenceMin.get(month,72);
	}

	public float getMaxHeightOfUterus(int key) {
		return heightUterusMax.get(key, 0f);
	}

	public float getMinHeightOfUterus(int key) {
		return heightUterusMin.get(key,0f);
	}

	public float getAVGHeightOfUterus(int key) {
		return heightUterusAvg.get(key,0f);
	}

	public BMI getBMI() {
		if (getStature() == 0 || getWeight() == 0)
			return BMI.standard;
		double value = getWeight() / (getStature() * getStature());
		if (18.5 > value) {
			return BMI.thin;
		} else if (24 <= value && value < 28) {
			return BMI.fat;
		} else if (value >= 28) {
			return BMI.kuantaqsytzo;
		} else {
			return BMI.standard;
		}
	}	
	
	public enum PregnantSate {
		/**
		 * 备孕
		 */
		prepare,
		/**
		 * 宝宝已经出生
		 */
		already,
		/**
		 * 孕早期
		 */
		early,
		/**
		 * 孕中期
		 */
		metaphase,
		/**
		 * 孕晚期
		 */
		advanced
	}
	
	public enum BMI implements Serializable {
		thin, // 廋
		standard, // 标准
		fat, // 肥
		kuantaqsytzo// 很肥
	}
}