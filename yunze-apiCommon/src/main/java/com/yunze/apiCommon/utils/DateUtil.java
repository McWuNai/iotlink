package com.yunze.apiCommon.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * Title: 系统时间公共类
 * </p>
 * <li>提供取得系统时间的所有共用方法</li>
 * 
 * @author apple
 */
public class DateUtil {

	private static Date date;
	private static long orderNum = 0l;
	private static String sDate;
	private static final Calendar CALENDAR = Calendar.getInstance();
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
	/** 定义常量 **/
	public static final String DATE_JFP_STR = "yyyyMM";
	public static final String DATE_FULL_STR = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_SMALL_STR = "yyyy-MM-dd";
	public static final String DATE_KEY_STR = "yyMMddHHmmss";
	private static String ymdhms = "yyyy-MM-dd HH:mm:ss";
	private static String ymd = "yyyy-MM-dd";
	public static SimpleDateFormat ymdSDF = new SimpleDateFormat(ymd);
	private static String year = "yyyy";
	private static String month = "MM";
	private static String day = "dd";
	public static SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat(ymdhms);
	public static SimpleDateFormat yearSDF = new SimpleDateFormat(year);
	public static SimpleDateFormat monthSDF = new SimpleDateFormat(month);
	public static SimpleDateFormat daySDF = new SimpleDateFormat(day);

	public static SimpleDateFormat yyyyMMddHHmm = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");

	public static SimpleDateFormat yyyyMMddHH_NOT_ = new SimpleDateFormat("yyyyMMdd");

	public static long DATEMM = 86400L;

	/**
	 * 获得当前时间 格式：2014-12-02 10:38:53
	 * 
	 * @return String
	 */
	public static String getCurrentTime() {
		return yyyyMMddHHmmss.format(new Date());
	}

	public static boolean checkExpiredTime(Date expiredTime) {
		if (expiredTime != null) {
			Date nowDate = new Date();
			if (expiredTime.before(nowDate)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * 可以获取昨天的日期 格式：2014-12-01
	 * 
	 * @return String
	 */
	public static String getYesterdayYYYYMMDD() {
		Date date = new Date(System.currentTimeMillis() - DATEMM * 1000L);
		String str = yyyyMMdd.format(date);
		try {
			date = yyyyMMddHHmmss.parse(str + " 00:00:00");
			return yyyyMMdd.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 可以获取后退N天的日期 格式：传入2 得到2014-11-30
	 * 
	 * @param backDay
	 * @return String
	 */
	public String getStrDate(String backDay) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, Integer.parseInt("-" + backDay));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String back = sdf.format(calendar.getTime());
		return back;
	}

	/**
	 * 获取当前的年、月、日
	 * 
	 * @return String
	 */
	public static String getCurrentYear() {
		return yearSDF.format(new Date());
	}

	public static String getCurrentMonth() {
		return monthSDF.format(new Date());
	}

	public static String getCurrentDay() {
		return daySDF.format(new Date());
	}

	/**
	 * 获取年月日 也就是当前时间 格式：2014-12-02
	 * 
	 * @return String
	 */
	public static String getCurrentymd() {
		return ymdSDF.format(new Date());
	}

	/**
	 * 获取今天0点开始的秒数
	 * 
	 * @return long
	 */
	public static long getTimeNumberToday() {
		Date date = new Date();
		String str = yyyyMMdd.format(date);
		try {
			date = yyyyMMdd.parse(str);
			return date.getTime() / 1000L;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0L;
	}

	/**
	 * 获取今天的日期 格式：20141202
	 * 
	 * @return String
	 */
	public static String getTodateString() {
		String str = yyyyMMddHH_NOT_.format(new Date());
		return str;
	}

	/**
	 * 判断两个日期是否是同一天
	 * 
	 * @param date1
	 *            date1
	 * @param date2
	 *            date2
	 * @return
	 */
	public static boolean isSameDate(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);

		boolean isSameYear = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
		boolean isSameMonth = isSameYear && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
		boolean isSameDate = isSameMonth && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);

		return isSameDate;
	}

	/**
	 * 获取昨天的日期 格式：20141201
	 * 
	 * @return String
	 */
	public static String getYesterdayString() {
		Date date = new Date(System.currentTimeMillis() - DATEMM * 1000L);
		String str = yyyyMMddHH_NOT_.format(date);
		return str;
	}

	/**
	 * 获得昨天零点
	 * 
	 * @return Date
	 */
	public static Date getYesterDayZeroHour() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR, 0);
		return cal.getTime();
	}

	/**
	 * 把long型日期转String ；---OK
	 * 
	 * @param date
	 *            long型日期；
	 * @param format
	 *            日期格式；
	 * @return
	 */
	public static String longToString(long date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		// 前面的lSysTime是秒数，先乘1000得到毫秒数，再转为java.util.Date类型
		Date dt2 = new Date(date * 1000L);
		String sDateTime = sdf.format(dt2); // 得到精确到秒的表示：08/31/2006 21:08:00
		return sDateTime;
	}

	/**
	 * 获得今天零点
	 *
	 * @return Date
	 */
	public static Date getTodayZeroHour() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR, 0);
		return cal.getTime();
	}

	/**
	 * 获得昨天23时59分59秒
	 *
	 * @return
	 */
	public static Date getYesterDay24Hour() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.HOUR, 23);
		return cal.getTime();
	}

	/**
	 * String To Date ---OK
	 *
	 * @param date
	 *            待转换的字符串型日期；
	 * @param format
	 *            转化的日期格式
	 * @return 返回该字符串的日期型数据；
	 */
	public static Date stringToDate(String date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			return sdf.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * 获得指定日期所在的自然周的第一天，即周日
	 *
	 * @param date
	 *            日期
	 * @return 自然周的第一天
	 */
	public static Date getStartDayOfWeek(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_WEEK, 1);
		date = c.getTime();
		return date;
	}

	/**
	 * 获得指定日期所在的自然周的最后一天，即周六
	 *
	 * @param date
	 * @return
	 */
	public static Date getLastDayOfWeek(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_WEEK, 7);
		date = c.getTime();
		return date;
	}

	/**
	 * 获得指定日期所在当月第一天
	 *
	 * @param date
	 * @return
	 */
	public static Date getStartDayOfMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DAY_OF_MONTH, 1);
		date = c.getTime();
		return date;
	}

	/**
	 * 获得指定日期所在当月最后一天
	 *
	 * @param date
	 * @return
	 */
	public static Date getLastDayOfMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DATE, 1);
		c.add(Calendar.MONTH, 1);
		c.add(Calendar.DATE, -1);
		date = c.getTime();
		return date;
	}

	/**
	 * 获得指定日期的下一个月的第一天
	 *
	 * @param date
	 * @return
	 */
	public static Date getStartDayOfNextMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, 1);
		c.set(Calendar.DAY_OF_MONTH, 1);
		date = c.getTime();
		return date;
	}

	/**
	 * 获得指定日期的下一个月的最后一天
	 *
	 * @param date
	 * @return
	 */
	public static Date getLastDayOfNextMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.DATE, 1);
		c.add(Calendar.MONTH, 2);
		c.add(Calendar.DATE, -1);
		date = c.getTime();
		return date;
	}

	public static Date getTo30MINUTE() {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, 30);
		date = c.getTime();
		return date;
	}

	/**
	 *
	 * 求某一个时间向前多少秒的时间(currentTimeToBefer)---OK
	 *
	 * @param givedTime
	 *            给定的时间
	 * @param interval
	 *            间隔时间的毫秒数；计算方式 ：n(天)*24(小时)*60(分钟)*60(秒)(类型)
	 * @param format_Date_Sign
	 *            输出日期的格式；如yyyy-MM-dd、yyyyMMdd等；
	 */
	public static String givedTimeToBefer(String givedTime, long interval, String format_Date_Sign) {
		String tomorrow = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format_Date_Sign);
			Date gDate = sdf.parse(givedTime);
			long current = gDate.getTime(); // 将Calendar表示的时间转换成毫秒
			long beforeOrAfter = current - interval * 1000L; // 将Calendar表示的时间转换成毫秒
			Date date = new Date(beforeOrAfter); // 用timeTwo作参数构造date2
			tomorrow = new SimpleDateFormat(format_Date_Sign).format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return tomorrow;
	}

	/**
	 * 把String 日期转换成long型日期；---OK
	 *
	 * @param date
	 *            String 型日期；
	 * @param format
	 *            日期格式；
	 * @return
	 */
	public static long stringToLong(String date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date dt2 = null;
		long lTime = 0;
		try {
			dt2 = sdf.parse(date);
			// 继续转换得到秒数的long型
			lTime = dt2.getTime() / 1000;
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return lTime;
	}

	/**
	 * 得到二个日期间的间隔日期；
	 *
	 * @param endTime
	 *            结束时间
	 * @param beginTime
	 *            开始时间
	 * @param isEndTime
	 *            是否包含结束日期；
	 * @return
	 */
	public static Map<String, String> getTwoDay(String endTime, String beginTime, boolean isEndTime) {
		Map<String, String> result = new HashMap<String, String>();
		if ((endTime == null || endTime.equals("") || (beginTime == null || beginTime.equals(""))))
			return null;
		try {
			Date date = ymdSDF.parse(endTime);
			endTime = ymdSDF.format(date);
			Date mydate = ymdSDF.parse(beginTime);
			long day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
			result = getDate(endTime, Integer.parseInt(day + ""), isEndTime);
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * 得到二个日期间的间隔日期；
	 *
	 * @param endTime
	 *            结束时间
	 * @param beginTime
	 *            开始时间
	 * @param isEndTime
	 *            是否包含结束日期；
	 * @return
	 */
	public static Integer getTwoDayInterval(String endTime, String beginTime, boolean isEndTime) {
		if ((endTime == null || endTime.equals("") || (beginTime == null || beginTime.equals(""))))
			return 0;
		long day = 0l;
		try {
			Date date = ymdSDF.parse(endTime);
			endTime = ymdSDF.format(date);
			Date mydate = ymdSDF.parse(beginTime);
			day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
		} catch (Exception e) {
			return 0;
		}
		return Integer.parseInt(day + "");
	}

	/**
	 * 根据结束时间以及间隔差值，求符合要求的日期集合；
	 * 
	 * @param endTime
	 * @param interval
	 * @param isEndTime
	 * @return
	 */
	public static Map<String, String> getDate(String endTime, Integer interval, boolean isEndTime) {
		Map<String, String> result = new HashMap<String, String>();
		if (interval == 0 || isEndTime) {
			if (isEndTime)
				result.put(endTime, endTime);
		}
		if (interval > 0) {
			int begin = 0;
			for (int i = begin; i < interval; i++) {
				endTime = givedTimeToBefer(endTime, DATEMM, ymd);
				result.put(endTime, endTime);
			}
		}
		return result;
	}

	/**
	 * 使用预设格式提取字符串日期
	 * 
	 * @param strDate
	 *            日期字符串
	 * @return
	 */
	public static Date parse(String strDate) {
		return parse(strDate, DATE_FULL_STR);
	}

	/**
	 * 使用用户格式提取字符串日期
	 * 
	 * @param strDate
	 *            日期字符串
	 * @param pattern
	 *            日期格式
	 * @return
	 */
	public static Date parse(String strDate, String pattern) {
		SimpleDateFormat df = new SimpleDateFormat(pattern);
		try {
			return df.parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 两个时间比较
	 * 
	 * @param date
	 * @return
	 */
	public static int compareDateWithNow(Date date1) {
		Date date2 = new Date();
		int rnum = date1.compareTo(date2);
		return rnum;
	}

	/**
	 * 两个时间比较(时间戳比较)
	 * 
	 * @param date
	 * @return
	 */
	public static int compareDateWithNow(long date1) {
		long date2 = dateToUnixTimestamp();
		if (date1 > date2) {
			return 1;
		} else if (date1 < date2) {
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * 获取系统当前时间
	 * 
	 * @return
	 */
	public static String getNowTime() {
		SimpleDateFormat df = new SimpleDateFormat(DATE_FULL_STR);
		return df.format(new Date());
	}

	/**
	 * 获取系统当前时间
	 * 
	 * @return
	 */
	public static String getNowTime(String type) {
		SimpleDateFormat df = new SimpleDateFormat(type);
		return df.format(new Date());
	}

	/**
	 * 获取系统当前计费期
	 * 
	 * @return
	 */
	public static String getJFPTime() {
		SimpleDateFormat df = new SimpleDateFormat(DATE_JFP_STR);
		return df.format(new Date());
	}

	/**
	 * 将指定的日期转换成Unix时间戳
	 * 
	 * @param String
	 *            date 需要转换的日期 yyyy-MM-dd HH:mm:ss
	 * @return long 时间戳
	 */
	public static long dateToUnixTimestamp(String date) {
		long timestamp = 0;
		try {
			timestamp = new SimpleDateFormat(DATE_FULL_STR).parse(date).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return timestamp;
	}

	/**
	 * 将指定的日期转换成Unix时间戳
	 * 
	 * @param String
	 *            date 需要转换的日期 yyyy-MM-dd
	 * @return long 时间戳
	 */
	public static long dateToUnixTimestamp(String date, String dateFormat) {
		long timestamp = 0;
		try {
			timestamp = new SimpleDateFormat(dateFormat).parse(date).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return timestamp;
	}

	/**
	 * 将当前日期转换成Unix时间戳
	 * 
	 * @return long 时间戳
	 */
	public static long dateToUnixTimestamp() {
		long timestamp = new Date().getTime();
		return timestamp;
	}

	/**
	 * 将Unix时间戳转换成日期
	 * 
	 * @param long
	 *            timestamp 时间戳
	 * @return String 日期字符串
	 */
	public static String unixTimestampToDate(long timestamp) {
		SimpleDateFormat sd = new SimpleDateFormat(DATE_FULL_STR);
		sd.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		return sd.format(new Date(timestamp));
	}

	/**
	 * 生成订单编号
	 * 
	 * @return
	 */
	public static synchronized String getOrderNo() {

		String str = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		if (sDate == null || !sDate.equals(str)) {
			sDate = str;
			orderNum = 0l;
		}
		orderNum++;
		long orderNo = Long.parseLong(str) * 10000;
		orderNo += orderNum;
		return orderNo + "";
	}

	/**
	 * 取得当前时间
	 * 
	 * @return 当前日期（Date）
	 */
	public static Date getCurrentDate() {
		return new Date();
	}

	public static Date getDateByMonth(int Month) {

		Date date = new Date();// 当前日期
		Calendar calendar = Calendar.getInstance();// 日历对象
		calendar.setTime(date);// 设置当前日期
		calendar.add(Calendar.MONTH, Month);// 月份减一

		return calendar.getTime();
	}

	/**
	 * 取得昨天此时的时间
	 * 
	 * @return 昨天日期（Date）
	 */
	public static Date getYesterdayDate() {
		return new Date(getCurTimeMillis() - 0x5265c00L);
	}

	/**
	 * 取得去过i天的时间
	 * 
	 * @param i
	 *            过去时间天数
	 * @return 昨天日期（Date）
	 */
	public static Date getPastdayDate(int i) {
		return new Date(getCurTimeMillis() - 0x5265c00L * i);
	}

	/**
	 * 取得当前时间的长整型表示
	 * 
	 * @return 当前时间（long）
	 */
	public static long getCurTimeMillis() {
		return System.currentTimeMillis();
	}

	/**
	 * 取得当前时间的特定表示格式的字符串
	 * 
	 * @param formatDate
	 *            时间格式（如：yyyy/MM/dd hh:mm:ss）
	 * @return 当前时间
	 */
	public static synchronized String getCurFormatDate(String formatDate) {
		date = getCurrentDate();
		simpleDateFormat.applyPattern(formatDate);
		return simpleDateFormat.format(date);
	}

	/**
	 * 取得某日期时间的特定表示格式的字符串
	 * 
	 * @param format
	 *            时间格式
	 * @param date
	 *            某日期（Date）
	 * @return 某日期的字符串
	 */
	public static synchronized String getDate2Str(String format, Date date) {
		simpleDateFormat.applyPattern(format);
		return simpleDateFormat.format(date);
	}

	/**
	 * 将日期转换为长字符串（包含：年-月-日 时:分:秒）
	 * 
	 * @param date
	 *            日期
	 * @return 返回型如：yyyy-MM-dd HH:mm:ss 的字符串
	 */
	public static String getDate2LStr(Date date) {
		return getDate2Str("yyyy-MM-dd HH:mm:ss", date);
	}
	/**
	 * 将日期转换为长字符串（包含：年-月-日 时:分:秒）
	 * 
	 * @param date
	 *            日期
	 * @return 返回型如：yyyy-MM-dd HH:mm:ss 的字符串
	 */
	public static String getDate2LStrSSS(Date date) {
		return getDate2Str("yyyy-MM-dd HH:mm:ss.SSS", date);
	}
	/**
	 * 将日期转换为长字符串（包含：年/月/日 时:分:秒）
	 * 
	 * @param date
	 *            日期
	 * @return 返回型如：yyyy/MM/dd HH:mm:ss 的字符串
	 */
	public static String getDate2LStr2(Date date) {
		return getDate2Str("yyyy/MM/dd HH:mm:ss", date);
	}

	/**
	 * 将日期转换为中长字符串（包含：年-月-日 时:分）
	 * 
	 * @param date
	 *            日期
	 * @return 返回型如：yyyy-MM-dd HH:mm 的字符串
	 */
	public static String getDate2MStr(Date date) {
		return getDate2Str("yyyy-MM-dd HH:mm", date);
	}

	/**
	 * 将日期转换为中长字符串（包含：年/月/日 时:分）
	 * 
	 * @param date
	 *            日期
	 * @return 返回型如：yyyy/MM/dd HH:mm 的字符串
	 */
	public static String getDate2MStr2(Date date) {
		return getDate2Str("yyyy/MM/dd HH:mm", date);
	}

	/**
	 * 将日期转换为短字符串（包含：年-月-日）
	 * 
	 * @param date
	 *            日期
	 * @return 返回型如：yyyy-MM-dd 的字符串
	 */
	public static String getDate2SStr(Date date) {
		return getDate2Str("yyyy-MM-dd", date);
	}

	/**
	 * 将日期转换为短字符串（包含：年/月/日）
	 * 
	 * @param date
	 *            日期
	 * @return 返回型如：yyyy/MM/dd 的字符串
	 */
	public static String getDate2SStr2(Date date) {
		return getDate2Str("yyyy/MM/dd", date);
	}

	/**
	 * 取得型如：yyyyMMddhhmmss的字符串
	 * 
	 * @param date
	 * @return 返回型如：yyyyMMddhhmmss 的字符串
	 */
	public static String getDate2All(Date date) {
		return getDate2Str("yyyyMMddhhmmss", date);
	}
	/**
	 * 取得型如：yyyyMMddHHmmss的字符串（24小时制）
	 * 
	 * @param date
	 * @return 返回型如：yyyyMMddHHmmss 的字符串
	 */
	public static String getDate2All24(Date date) {
		return getDate2Str("yyyyMMddHHmmss", date);
	}
	/**
	 * 取得型如：yyyyMMddhhmmss的字符串
	 * 
	 * @param date
	 * @return 返回型如：yyyyMMddhhmmss 的字符串
	 */
	public static String getDate2(Date date) {
		return getDate2Str("yyyyMMdd", date);
	}

	/**
	 * 将长整型数据转换为Date后，再转换为型如yyyy-MM-dd HH:mm:ss的长字符串
	 * 
	 * @param l
	 *            表示某日期的长整型数据
	 * @return 日期型的字符串
	 */
	public static String getLong2LStr(long l) {
		date = getLongToDate(l);
		return getDate2LStr(date);
	}

	/**
	 * 将长整型数据转换为Date后，再转换为型如yyyy-MM-dd的长字符串
	 * 
	 * @param l
	 *            表示某日期的长整型数据
	 * @return 日期型的字符串
	 */
	public static String getLong2SStr(long l) {
		date = getLongToDate(l);
		return getDate2SStr(date);
	}

	/**
	 * 将长整型数据转换为Date后，再转换指定格式的字符串
	 * 
	 * @param l
	 *            表示某日期的长整型数据
	 * @param formatDate
	 *            指定的日期格式
	 * @return 日期型的字符串
	 */
	public static String getLong2SStr(long l, String formatDate) {
		date = getLongToDate(l);
		simpleDateFormat.applyPattern(formatDate);
		return simpleDateFormat.format(date);
	}

	private static synchronized Date getStrToDate(String format, String str) {
		simpleDateFormat.applyPattern(format);
		try {
			return simpleDateFormat.parse(str);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	/**
	 * 将某指定的字符串转换为某类型的字符串
	 * 
	 * @param format
	 *            转换格式
	 * @param str
	 *            需要转换的字符串
	 * @return 转换后的字符串
	 */
	public static String getStr2Str(String format, String str) {
		date = getStrToDate(format, str);
		return getDate2Str(format, date);
	}

	/**
	 * 将某指定的字符串转换为型如：yyyy-MM-dd HH:mm:ss的时间
	 * 
	 * @param str
	 *            将被转换为Date的字符串
	 * @return 转换后的Date
	 */
	public static Date getStr2LDate(String str) {
		return getStrToDate("yyyy-MM-dd HH:mm:ss", str);
	}

	/**
	 * 将某指定的字符串转换为型如：yyyy/MM/dd HH:mm:ss的时间
	 * 
	 * @param str
	 *            将被转换为Date的字符串
	 * @return 转换后的Date
	 */
	public static Date getStr2LDate2(String str) {
		return getStrToDate("yyyy/MM/dd HH:mm:ss", str);
	}

	/**
	 * 将某指定的字符串转换为型如：yyyy-MM-dd HH:mm的时间
	 * 
	 * @param str
	 *            将被转换为Date的字符串
	 * @return 转换后的Date
	 */
	public static Date getStr2MDate(String str) {
		return getStrToDate("yyyy-MM-dd HH:mm", str);
	}

	/**
	 * 将某指定的字符串转换为型如：yyyy/MM/dd HH:mm的时间
	 * 
	 * @param str
	 *            将被转换为Date的字符串
	 * @return 转换后的Date
	 */
	public static Date getStr2MDate2(String str) {
		return getStrToDate("yyyy/MM/dd HH:mm", str);
	}

	/**
	 * 将某指定的字符串转换为型如：yyyy-MM-dd的时间
	 * 
	 * @param str
	 *            将被转换为Date的字符串
	 * @return 转换后的Date
	 */
	public static Date getStr2SDate(String str) {
		return getStrToDate("yyyy-MM-dd", str);
	}

	/**
	 * 将某指定的字符串转换为型如：yyyy-MM-dd的时间
	 * 
	 * @param str
	 *            将被转换为Date的字符串
	 * @return 转换后的Date
	 */
	public static Date getStr2SDate2(String str) {
		return getStrToDate("yyyy/MM/dd", str);
	}

	/**
	 * 将某长整型数据转换为日期
	 * 
	 * @param l
	 *            长整型数据
	 * @return 转换后的日期
	 */
	public static Date getLongToDate(long l) {
		return new Date(l);
	}

	/**
	 * 以分钟的形式表示某长整型数据表示的时间到当前时间的间隔
	 * 
	 * @param l
	 *            长整型数据
	 * @return 相隔的分钟数
	 */
	public static int getOffMinutes(long l) {
		return getOffMinutes(l, getCurTimeMillis());
	}

	/**
	 * 以分钟的形式表示两个长整型数表示的时间间隔
	 * 
	 * @param from
	 *            开始的长整型数据
	 * @param to
	 *            结束的长整型数据
	 * @return 相隔的分钟数
	 */
	public static int getOffMinutes(long from, long to) {
		return (int) ((to - from) / 60000L);
	}

	/**
	 * 以微秒的形式赋值给一个Calendar实例
	 * 
	 * @param l
	 *            用来表示时间的长整型数据
	 */
	public static void setCalendar(long l) {
		CALENDAR.clear();
		CALENDAR.setTimeInMillis(l);
	}

	/**
	 * 以日期的形式赋值给某Calendar
	 * 
	 * @param date
	 *            指定日期
	 */
	public static void setCalendar(Date date) {
		CALENDAR.clear();
		CALENDAR.setTime(date);
	}

	/**
	 * 在此之前要由一个Calendar实例的存在
	 * 
	 * @return 返回某年
	 */
	public static int getYear() {
		return CALENDAR.get(1);
	}

	/**
	 * 在此之前要由一个Calendar实例的存在
	 * 
	 * @return 返回某月
	 */
	public static int getMonth() {
		return CALENDAR.get(2) + 1;
	}

	/**
	 * 在此之前要由一个Calendar实例的存在
	 * 
	 * @return 返回某天
	 */
	public static int getDay() {
		return CALENDAR.get(5);
	}

	/**
	 * 在此之前要由一个Calendar实例的存在
	 * 
	 * @return 返回某小时
	 */
	public static int getHour() {
		return CALENDAR.get(11);
	}

	/**
	 * 在此之前要由一个Calendar实例的存在
	 * 
	 * @return 返回某分钟
	 */
	public static int getMinute() {
		return CALENDAR.get(12);
	}

	/**
	 * 在此之前要由一个Calendar实例的存在
	 * 
	 * @return 返回某秒
	 */
	public static int getSecond() {
		return CALENDAR.get(13);
	}

	// Grace style
	public static final String PATTERN_GRACE = "yyyy/MM/dd HH:mm:ss";
	public static final String PATTERN_GRACE_NORMAL = "yyyy/MM/dd HH:mm";
	public static final String PATTERN_GRACE_SIMPLE = "yyyy/MM/dd";

	// Classical style
	public static final String PATTERN_CLASSICAL = "yyyy-MM-dd HH:mm:ss";
	public static final String PATTERN_CLASSICAL_NORMAL = "yyyy-MM-dd HH:mm";
	public static final String PATTERN_CLASSICAL_SIMPLE = "yyyy-MM-dd";

	private DateUtil() {
		// Cannot be instantiated
	}

	/**
	 * 根据默认格式将日期转格式化成字符串
	 * 
	 * @param date
	 *            指定日期
	 * @return 返回格式化后的字符串
	 */
	public static String format(Date date) {
		return format(date, PATTERN_CLASSICAL);
	}

	/**
	 * 根据指定格式将指定日期格式化成字符串
	 * 
	 * @param date
	 *            指定日期
	 * @param pattern
	 *            指定格式
	 * @return 返回格式化后的字符串
	 */
	public static String format(Date date, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}

	/**
	 * 获取时间date1与date2相差的秒数
	 * 
	 * @param date1
	 *            起始时间
	 * @param date2
	 *            结束时间
	 * @return 返回相差的秒数
	 */
	public static int getOffsetSeconds(Date date1, Date date2) {
		int seconds = (int) ((date2.getTime() - date1.getTime()) / 1000);
		return seconds;
	}

	/**
	 * 获取时间date1与date2相差的分钟数
	 * 
	 * @param date1
	 *            起始时间
	 * @param date2
	 *            结束时间
	 * @return 返回相差的分钟数
	 */
	public static int getOffsetMinutes(Date date1, Date date2) {
		return getOffsetSeconds(date1, date2) / 60;
	}

	/**
	 * 获取时间date1与date2相差的小时数
	 * 
	 * @param date1
	 *            起始时间
	 * @param date2
	 *            结束时间
	 * @return 返回相差的小时数
	 */
	public static int getOffsetHours(Date date1, Date date2) {
		return getOffsetMinutes(date1, date2) / 60;
	}

	/**
	 * 获取时间date1与date2相差的天数数
	 * 
	 * @param date1
	 *            起始时间
	 * @param date2
	 *            结束时间
	 * @return 返回相差的天数
	 */
	public static int getOffsetDays(Date date1, Date date2) {
		return getOffsetHours(date1, date2) / 24;
	}

	/**
	 * 获取时间date1与date2相差的周数
	 * 
	 * @param date1
	 *            起始时间
	 * @param date2
	 *            结束时间
	 * @return 返回相差的周数
	 */
	public static int getOffsetWeeks(Date date1, Date date2) {
		return getOffsetDays(date1, date2) / 7;
	}

	/**
	 * 获取重置指定日期的时分秒后的时间
	 * 
	 * @param date
	 *            指定日期
	 * @param hour
	 *            指定小时
	 * @param minute
	 *            指定分钟
	 * @param second
	 *            指定秒
	 * @return 返回重置时分秒后的时间
	 */
	public static Date getResetTime(Date date, int hour, int minute, int second) {
		Calendar cal = Calendar.getInstance();
		if (date != null) {
			cal.setTime(date);
		}
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.SECOND, minute);
		cal.set(Calendar.MINUTE, second);
		return cal.getTime();
	}

	/**
	 * 返回指定日期的起始时间
	 * 
	 * @param date
	 *            指定日期（例如2014-08-01）
	 * @return 返回起始时间（例如2014-08-01 00:00:00）
	 */
	public static Date getIntegralStartTime(Date date) {
		return getResetTime(date, 0, 0, 0);
	}

	/**
	 * 返回指定日期的结束时间
	 * 
	 * @param date
	 *            指定日期（例如2014-08-01）
	 * @return 返回结束时间（例如2014-08-01 23:59:59）
	 */
	public static Date getIntegralEndTime(Date date) {
		return getResetTime(date, 23, 59, 59);
	}

	/**
	 * 获取指定日期累加年月日后的时间
	 * 
	 * @param date
	 *            指定日期
	 * @param year
	 *            指定年数
	 * @param month
	 *            指定月数
	 * @param day
	 *            指定天数
	 * @return 返回累加年月日后的时间
	 */
	public static Date rollDate(Date date, int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		if (date != null) {
			cal.setTime(date);
		}
		cal.add(Calendar.YEAR, year);
		cal.add(Calendar.MONTH, month);
		cal.add(Calendar.DAY_OF_MONTH, day);
		return cal.getTime();
	}

	/**
	 * 获取指定日期累加指定月数后的时间
	 * 
	 * @param date
	 *            指定日期
	 * @param month
	 *            指定月数
	 * @return 返回累加月数后的时间
	 */
	public static Date rollMonth(Date date, int month) {
		return rollDate(date, 0, month, 0);
	}

	/**
	 * 获取指定日期累加指定天数后的时间
	 * 
	 * @param date
	 *            指定日期
	 * @param day
	 *            指定天数
	 * @return 返回累加天数后的时间
	 */
	public static Date rollDay(Date date, int day) {
		return rollDate(date, 0, 0, day);
	}

	/**
	 * 计算指定日期所在月份的天数
	 * 
	 * @param date
	 *            指定日期
	 * @return 返回所在月份的天数
	 */
	public static int getDayOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		if (date != null) {
			cal.setTime(date);
		}
		int dayOfMonth = cal.getActualMaximum(Calendar.DATE);
		return dayOfMonth;
	}

	/**
	 * 获取当月第一天的起始时间，例如2014-08-01 00:00:00
	 * 
	 * @return 返回当月第一天的起始时间
	 */
	public static Date getMonthStartTime() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return getIntegralStartTime(cal.getTime());
	}

	/**
	 * 获取当月最后一天的结束时间，例如2014-08-31 23:59:59
	 * 
	 * @return 返回当月最后一天的结束时间
	 */
	public static Date getMonthEndTime() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, getDayOfMonth(cal.getTime()));
		return getIntegralEndTime(cal.getTime());
	}

	/**
	 * 获取上个月第一天的起始时间，例如2014-07-01 00:00:00
	 * 
	 * @return 返回上个月第一天的起始时间
	 */
	public static Date getLastMonthStartTime() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return getIntegralStartTime(cal.getTime());
	}

	/**
	 * 获取上个月最后一天的结束时间，例如2014-07-31 23:59:59
	 * 
	 * @return 返回上个月最后一天的结束时间
	 */
	public static Date getLastMonthEndTime() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		cal.set(Calendar.DAY_OF_MONTH, getDayOfMonth(cal.getTime()));
		return getIntegralEndTime(cal.getTime());
	}

	/**
	 * 获取下个月第一天的起始时间，例如2014-09-01 00:00:00
	 * 
	 * @return 返回下个月第一天的起始时间
	 */
	public static Date getNextMonthStartTime() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return getIntegralStartTime(cal.getTime());
	}

	/**
	 * 获取下个月最后一天的结束时间，例如2014-09-30 23:59:59
	 * 
	 * @return 返回下个月最后一天的结束时间
	 */
	public static Date getNextMonthEndTime() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, getDayOfMonth(cal.getTime()));
		return getIntegralEndTime(cal.getTime());
	}

	/**
	 * 获取当前季度第一天的起始时间
	 * 
	 * @return 返回当前季度第一天的起始时间
	 */
	public static Date getQuarterStartTime() {
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		if (month < 3) {
			cal.set(Calendar.MONTH, 0);
		} else if (month < 6) {
			cal.set(Calendar.MONTH, 3);
		} else if (month < 9) {
			cal.set(Calendar.MONTH, 6);
		} else {
			cal.set(Calendar.MONTH, 9);
		}
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return getIntegralStartTime(cal.getTime());
	}

	/**
	 * 获取当前季度最后一天的结束时间
	 * 
	 * @return 返回当前季度最后一天的结束时间
	 */
	public static Date getQuarterEndTime() {
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		if (month < 3) {
			cal.set(Calendar.MONTH, 2);
		} else if (month < 6) {
			cal.set(Calendar.MONTH, 5);
		} else if (month < 9) {
			cal.set(Calendar.MONTH, 8);
		} else {
			cal.set(Calendar.MONTH, 11);
		}
		cal.set(Calendar.DAY_OF_MONTH, getDayOfMonth(cal.getTime()));
		return getIntegralEndTime(cal.getTime());
	}

	/**
	 * 获取前一个工作日
	 * 
	 * @return 返回前一个工作日
	 */
	public static Date getPrevWorkday() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			cal.add(Calendar.DAY_OF_MONTH, -2);
		}
		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
			cal.add(Calendar.DAY_OF_MONTH, -1);
		}
		return getIntegralStartTime(cal.getTime());
	}

	/**
	 * 获取下一个工作日
	 * 
	 * @return 返回下个工作日
	 */
	public static Date getNextWorkday() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
			cal.add(Calendar.DAY_OF_MONTH, 2);
		}
		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}
		return getIntegralStartTime(cal.getTime());
	}

	/**
	 * 获取当周的第一个工作日
	 * 
	 * @return 返回第一个工作日
	 */
	public static Date getFirstWorkday() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return getIntegralStartTime(cal.getTime());
	}

	/**
	 * 获取当周的最后一个工作日
	 * 
	 * @return 返回最后一个工作日
	 */
	public static Date getLastWorkday() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		return getIntegralStartTime(cal.getTime());
	}

	/**
	 * 判断指定日期是否是工作日
	 * 
	 * @param date
	 *            指定日期
	 * @return 如果是工作日返回true，否则返回false
	 */
	public static boolean isWorkday(Date date) {
		Calendar cal = Calendar.getInstance();
		if (date != null) {
			cal.setTime(date);
		}
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		return !(dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
	}

	/**
	 * 获取指定日期是星期几
	 * 
	 * @param date
	 *            指定日期
	 * @return 返回星期几的描述
	 */
	public static String getWeekdayDesc(Date date) {
		final String[] weeks = new String[] { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		Calendar cal = Calendar.getInstance();
		if (date != null) {
			cal.setTime(date);
		}
		return weeks[cal.get(Calendar.DAY_OF_WEEK) - 1];
	}

	/**
	 * 获取指定日期距离当前时间的时间差描述（如3小时前、1天前）
	 * 
	 * @param date
	 *            指定日期
	 * @return 返回时间差的描述
	 */
	public static String getTimeOffsetDesc(Date date) {
		int seconds = getOffsetSeconds(date, new Date());
		if (Math.abs(seconds) < 60) {
			return Math.abs(seconds) + "秒" + (seconds > 0 ? "前" : "后");
		}
		int minutes = seconds / 60;
		if (Math.abs(minutes) < 60) {
			return Math.abs(minutes) + "分钟" + (minutes > 0 ? "前" : "后");
		}
		int hours = minutes / 60;
		if (Math.abs(hours) < 60) {
			return Math.abs(hours) + "小时" + (hours > 0 ? "前" : "后");
		}
		int days = hours / 24;
		if (Math.abs(days) < 7) {
			return Math.abs(days) + "天" + (days > 0 ? "前" : "后");
		}
		int weeks = days / 7;
		if (Math.abs(weeks) < 5) {
			return Math.abs(weeks) + "周" + (weeks > 0 ? "前" : "后");
		}
		int monthes = days / 30;
		if (Math.abs(monthes) < 12) {
			return Math.abs(monthes) + "个月" + (monthes > 0 ? "前" : "后");
		}
		int years = monthes / 12;
		return Math.abs(years) + "年" + (years > 0 ? "前" : "后");
	}
}
