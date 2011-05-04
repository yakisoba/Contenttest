package com.blogspot.yakisobayuki.birth2cal2;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

import android.util.Log;

public class BirthdayFormat {
	final Boolean logstatus = false;

	// 今日の日付取得
	final Calendar mCalendar = Calendar.getInstance();
	final int mYear = mCalendar.get(Calendar.YEAR);
	final int mMonth = mCalendar.get(Calendar.MONTH);
	final int mDay = mCalendar.get(Calendar.DAY_OF_MONTH);

	@SuppressWarnings("unused")
	public String DateCheck(String date) {
		int check_year = 0;
		int check_month = 0;
		int check_day = 0;
		int check_date = 0;
		String date_tmp = null;

		if (date.length() > 10) { // 文字数が多い場合は対象外
			return null;

		} else if (date.length() == 10) { // 文字数は正しい
			if (date.indexOf("-") != -1 && date.substring(4, 5).equals("-")
					&& date.substring(7, 8).equals("-")) {

				try {
					check_year = Integer.parseInt(date.substring(0, 4));
					check_month = Integer.parseInt(date.substring(5, 7));
					check_day = Integer.parseInt(date.substring(8, 10));

					if (check_month == 0 || check_month > 12) {
						return null;
					} else if (check_day == 0 || check_day > 31) {
						return null;
					} else {
						return date; // 正しいのでそのまま返す
					}

				} catch (Exception e) {
					Log.e("Birth2Cal", e + " [target] " + date);
					return null;
				}

			} else { // 10文字だけど”-”がない
				try {
					check_year = Integer.parseInt(date.substring(0, 4));
					check_month = Integer.parseInt(date.substring(5, 7));
					check_day = Integer.parseInt(date.substring(8, 10));

					if (check_month == 0 || check_month > 12) {
						return null;
					} else if (check_day == 0 || check_day > 31) {
						return null;
					} else {
						date_tmp = date.substring(0, 4) + "-"
								+ date.substring(5, 7) + "-"
								+ date.substring(8, 10);
						return date_tmp;
					}
				} catch (Exception e) {
					Log.e("Birth2Cal", e + " [target] " + date);
					return null;
				}
			}

		} else if (date.length() < 10) { // 文字数が正しくない
			int hit[] = { 0, 0, 0 };
			int hit_point = 0;

			for (int i = 0; i < date.length(); i++) {
				try {
					// 1文字ずつ拾ってきて文字か数値か判定。文字ならエラーになる。
					check_date = Integer.parseInt(date.substring(i, i + 1));
				} catch (Exception e) {
					// 文字ならヒットした位置とヒットした数を記録
					try {
						hit[hit_point] = i;
						hit_point++;

						// ヒットが3つ以上になった場合は対象外
						if (hit_point > 2) {
							return null;
						}
					} catch (Exception e2) {
						Log.e("Birth2Cal", e + " [target] " + date);
						return null; // 範囲外になったときように念のため
					}
				}
			}

			if (hit_point == 2) {
				try {
					if (hit[0] < 4 || (hit[1] - hit[0]) < 1
							|| date.length() - hit[1] < 1) {
						return null; // 年が4桁以下か月/日が0桁以下のとき
					} else {
						NumberFormat nf1 = new DecimalFormat("0000");
						NumberFormat nf2 = new DecimalFormat("00");
						check_year = Integer
								.parseInt(date.substring(0, hit[0]));
						check_month = Integer.parseInt(date.substring(
								hit[0] + 1, hit[1]));
						check_day = Integer.parseInt(date.substring(hit[1] + 1,
								date.length()));

						if (check_month == 0 || check_month > 12) {
							return null;
						} else if (check_day == 0 || check_day > 31) {
							return null;
						} else {
							date_tmp = nf1.format(check_year) + "-"
									+ nf2.format(check_month) + "-"
									+ nf2.format(check_day);
							return date_tmp;
						}
					}
				} catch (Exception e) {
					Log.e("Birth2Cal", e + " [target] " + date);
					return null;
				}

			} else if (hit_point == 0 && date.length() == 8) { // 区切り文字がないけど20000101なので判定できる
				try {
					check_year = Integer.parseInt(date.substring(0, 4));
					check_month = Integer.parseInt(date.substring(4, 6));
					check_day = Integer.parseInt(date.substring(6, 8));

					if (check_month == 0 || check_month > 12) {
						return null;
					} else if (check_day == 0 || check_day > 31) {
						return null;
					} else {
						date_tmp = date.substring(0, 4) + "-"
								+ date.substring(4, 6) + "-"
								+ date.substring(6, 8);
						return date_tmp;
					}
				} catch (Exception e) {
					Log.e("Birth2Cal", e + " [target] " + date);
					return null;
				}
			} else { // 2000111みたいな1月11日なのか11月1日なのか判定不可
				return null;
			}
		} else {
			return null;
		}
	}
}
