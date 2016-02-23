package com.spark.sleep;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.spark.data.Data;
import com.spark.util.Trace;


public class SleepCounter {
	public static final String TAG = SleepCounter.class.getSimpleName();
	public static final int DAY_RECORD_NUMS = 288;
	public static final int DEEP_SLEEP_THRESHOLD = 2;
	public static final int LIGHT_SLEEP_THRESHOLD = 100;
	public static final int DEEP_SLEEP_CONTIN_NUMS = 3;
	public static final int LIGHT_SLEEP_CONTIN_NUMS = 12;
	public static final int NOWEAR_SLEEP_CONTIN_NUMS = 20;
	public static final int SLEEP_FLAG_WAKE = 0x00;
	public static final int SLEEP_FLAG_LIGHT = 0x01;
	public static final int SLEEP_FLAG_DEEP =  0x02;
	public static final int SLEEP_FLAG_NOWEAR =0x04;
	public static final long SPAN_10MINS = 10 * 60 * 1000;

	public static List<SleepDB1> org2Range(List<Data> sleepData) {
		if (sleepData == null)
			return null;
		boolean complete = false;
		int lightSleepCnt = 0;
		int deepSleepCnt = 0;
		int noWearCnt = 0;
		List<SleepRecord> sleepRecords = new ArrayList<SleepRecord>();

		//第一步-->sleep==0 times / sleep<=100 times > 70%
		for (int i = 0; i < sleepData.size(); i++) {
			Data data = sleepData.get(i);
			int tick = data.getDateTime();
			int action = data.getSleep();
			int sleepNum = 0;

			SleepRecord sleepRecord = new SleepRecord(tick, SLEEP_FLAG_WAKE);
			sleepRecords.add(sleepRecord);

			if (action == 0) {
				noWearCnt++;
			}
			if (action <= LIGHT_SLEEP_THRESHOLD) {
				lightSleepCnt++;
				if (i == sleepData.size() - 1) {
					complete = true;
					sleepNum = i;
				}
			} else {
				complete = true;
				sleepNum = i - 1;
			}

			if (complete) {
				if (lightSleepCnt >= LIGHT_SLEEP_CONTIN_NUMS) {
					if (noWearCnt * 100 / lightSleepCnt > 70) {
						for (int j = 0; j < lightSleepCnt; j++) {
							sleepRecords.get(sleepNum - j).setSleepValue(SLEEP_FLAG_NOWEAR);
						}
					}
				}
				complete = false;
				noWearCnt = 0;
				lightSleepCnt = 0;
			}
		}

		//第二步-->sleep == 0, continuous times >= 20
		noWearCnt = 0;
		lightSleepCnt = 0;
		for (int i = 0; i < sleepData.size(); i++) {
			Data data = sleepData.get(i);
			int action = data.getSleep();

			if (sleepRecords.get(i).getSleepValue() == SLEEP_FLAG_NOWEAR) {
				continue;
			}
			if (action == 0) {
				noWearCnt++;
			} else {
				noWearCnt = 0;
			}
			if (noWearCnt >= NOWEAR_SLEEP_CONTIN_NUMS) {
				for (int j = 0; j < noWearCnt; j++) {
					sleepRecords.get(i - j).setSleepValue(SLEEP_FLAG_NOWEAR);
				}
			}
		}

		//第三步-->判断深度睡眠、浅度睡眠、清醒状态
		for (int i = 0; i < sleepData.size(); i++) {
			Data data = sleepData.get(i);
			int action = data.getSleep();

			if (sleepRecords.get(i).getSleepValue() == SLEEP_FLAG_NOWEAR) {
				deepSleepCnt = 0;
				lightSleepCnt = 0;
				continue;
			}
			if (action <= DEEP_SLEEP_THRESHOLD) {
				deepSleepCnt++;
				lightSleepCnt++;
			} else if (action <= LIGHT_SLEEP_THRESHOLD) {
				deepSleepCnt = 0;
				lightSleepCnt++;
			} else {
				deepSleepCnt = 0;
				lightSleepCnt = 0;
			}

			if (deepSleepCnt >= DEEP_SLEEP_CONTIN_NUMS) {
				for (int j = 0; j < deepSleepCnt; j++) {
					sleepRecords.get(i - j).setSleepValue(SLEEP_FLAG_DEEP);
				}
			}
			if (lightSleepCnt >= LIGHT_SLEEP_CONTIN_NUMS) {
				for (int j = 0; j < lightSleepCnt; j++) {
					sleepRecords.get(i - j).setSleepValue(SLEEP_FLAG_LIGHT);
				}
			}
		}

		//第四步-->从深度睡眠、浅度睡眠、清醒状态计算出{起、止、类别}
		int state = 0;
		complete = false;
		int sleepValue = 0;
		int next_sleepStatus = 0;
		int next_time_start = 0;
		int next_end_start = 0;
		int SleepStatus = 0;
		int time_start = 0;
		int time_end = 0;
		List<SleepDB1> sleepList = new ArrayList<SleepDB1>();// 初始化解析结果
		for (int i = 0; i < sleepData.size(); i++) {
			sleepValue = sleepRecords.get(i).getSleepValue();

			// 把状态归一化
			if (sleepValue == SLEEP_FLAG_DEEP) {
				sleepValue = SLEEP_FLAG_WAKE;
			} else if (sleepValue == (SLEEP_FLAG_LIGHT | SLEEP_FLAG_DEEP)) {
				sleepValue = SLEEP_FLAG_DEEP;
			}

			switch (state) {
			case 0: // 检测进入状态
				state = 1;
				time_start = sleepRecords.get(i).getTick();
				time_end = sleepRecords.get(i).getTick();
				SleepStatus = sleepValue;
				break;
			case 1: // 检测退出状态
				if (sleepValue == SleepStatus) {
					time_end = sleepRecords.get(i).getTick();
				} else {
					state = 1;
					complete = true;
					next_time_start = sleepRecords.get(i).getTick();
					next_end_start = sleepRecords.get(i).getTick();
					next_sleepStatus = sleepValue;
				}
				if (i == sleepData.size() - 1) {
					complete = true;
				}

				if (complete) {
					switch (SleepStatus) {
					case SLEEP_FLAG_WAKE: {
						SleepDB1 sleep = new SleepDB1(time_start, time_end + 300,
								SleepState.sober);
						sleepList.add(sleep);
					}
					break;

					case SLEEP_FLAG_LIGHT: {
						SleepDB1 sleep = new SleepDB1(time_start, time_end + 300,
								SleepState.lightSleep);
						sleepList.add(sleep);
					}
					break;

					case SLEEP_FLAG_DEEP: {
						SleepDB1 sleep = new SleepDB1(time_start, time_end + 300,
								SleepState.deepSleep);
						sleepList.add(sleep);
					}
					break;
					case SLEEP_FLAG_NOWEAR:
						SleepDB1 sleep = new SleepDB1(time_start, time_end + 300,
								SleepState.noWear);
						sleepList.add(sleep);
						break;
					default:
						break;
					}
					complete = false;
					SleepStatus = next_sleepStatus;
					time_start = next_time_start;
					time_end = next_end_start;
				}
				break;
			default:
				state = 0;
				break;
			}
		}

		return sleepList;
	}



	public static SleepDB2 range2Valid(List<SleepDB1> sleepDB1s) {
		List<SleepDB1> sleepDB1sValid = new ArrayList<SleepDB1>();
		SleepDB2 sleepDB2 = new SleepDB2();

		//第一步-->从{起、止、类别}中筛选出睡眠段
		for(int i = 0; i < sleepDB1s.size(); i++) {
			if((sleepDB1s.get(i).getState() == SleepState.lightSleep)
					|| (sleepDB1s.get(i).getState() == SleepState.deepSleep)) {
				sleepDB1sValid.add(sleepDB1s.get(i));
			}
		}

		//第二步-->给{起、止、类别}进行连续性处理防止睡眠途中上厕所等短暂活动，并输出新的{起、止、类别}
		for(int i = 0; i < sleepDB1sValid.size() - 1; i++) {
			long prevFirstTick = sleepDB1sValid.get(i).getDateFirst();
			long prevLastTick = sleepDB1sValid.get(i).getDateLast();
			long nextFirstTick = sleepDB1sValid.get(i + 1).getDateFirst();
			long nextLastTick = sleepDB1sValid.get(i + 1).getDateLast();
			SleepDB1 sleepDB1; 

			if(nextFirstTick - prevLastTick <= SPAN_10MINS / 1000) {
				sleepDB1 = new SleepDB1(prevFirstTick, nextLastTick, SleepState.lightSleep);
				sleepDB1sValid.remove(i);
				sleepDB1sValid.remove(i);
				sleepDB1sValid.add(i, sleepDB1);
				--i;
			}
		}	

		//第三步-->给新的{起、止、类别}进行排序
		long len1 = 0, len2 = 0;
		for(int i = 0; i < sleepDB1sValid.size(); i++) {
			for(int j = i + 1; j < sleepDB1sValid.size(); j++) {
				len1 = sleepDB1sValid.get(i).getDateLast() - sleepDB1sValid.get(i).getDateFirst();
				len2 = sleepDB1sValid.get(j).getDateLast() - sleepDB1sValid.get(j).getDateFirst();
				if(len1 < len2) {
					SleepDB1 sleepDB1 = sleepDB1sValid.get(i);
					sleepDB1sValid.set(i, sleepDB1sValid.get(j));
					sleepDB1sValid.set(j, sleepDB1);
				}
			}
		}

		//第四步-->输出sleepDB2,如果有效睡眠时长小于4个小时，则认为睡眠无效
		if(sleepDB1sValid.size() != 0) {
			long startTime = sleepDB1sValid.get(0).getDateFirst();
			long endTime = sleepDB1sValid.get(0).getDateLast();
			long len = endTime - startTime;
			Trace.e(TAG, "startTime " + startTime + ", endTime " + endTime + ", len" + len);
			if(len < 60 * 60 * 4) {
				return null;
			}
			int deepTime = 0;
			int lightTime = 0;
			int soberCnt = 0;

			for(int i = 0; i < sleepDB1s.size(); i++) {
				if((sleepDB1s.get(i).getDateFirst() >= startTime)
						&& (sleepDB1s.get(i).getDateLast() <= endTime)) {
					if(sleepDB1s.get(i).getState() == SleepState.lightSleep) {
						lightTime += sleepDB1s.get(i).getDateLast() - sleepDB1s.get(i).getDateFirst();
					}else if(sleepDB1s.get(i).getState() == SleepState.deepSleep){
						deepTime += sleepDB1s.get(i).getDateLast() - sleepDB1s.get(i).getDateFirst();
					}else if(sleepDB1s.get(i).getState() == SleepState.sober) {
						soberCnt++;
					}
				}
			}			
			sleepDB2.setStartTime(startTime);
			sleepDB2.setEndTime(endTime);
			sleepDB2.setDeepSleep(deepTime);
			sleepDB2.setLightSleep(lightTime);
			sleepDB2.setSoberCnt(soberCnt);
		}else {
			sleepDB2 = null;
		}

		return sleepDB2;
	}

	public static SleepDB2 range2Valid(List<SleepDB1> sleepDB1s, Date begin, Date end, int sleepLen) {
		
		return null;
	}

	public static List<Data> fillOrgData(List<Data> datas){
		if(datas.size() <= 1) {
			return datas;
		}
		
		for(int i = 0; i < datas.size() - 1; i++) {
			int curTime = datas.get(i).getDateTime();
			if((datas.get(i + 1).getDateTime() - curTime) > 300) {
				Data data = new Data(curTime + 300);
				datas.add(i + 1, data);
			}
		}	
		
		return datas;
	}

}
