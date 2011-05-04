package com.blogspot.yakisobayuki.birth2cal2;

public class ContactsStatus {

	private String displayName;
	private String daykind;
	private String birthday;
	private String age;
	private boolean checkflg;

	public String getDisplayName() {
		return displayName;
	}

	public String getDayKind() {
		return daykind;
	}

	public String getBirth() {
		return birthday;
	}

	public String getAge() {
		return age;
	}

	public boolean getCheckFlag() {
		return checkflg;
	}

	public void setCheckFlag(boolean checkflg) {
		this.checkflg = checkflg;
	}

	public void setParam(String displayName, String daykind, String birthday,
			String age) {
		this.displayName = displayName;
		this.daykind = daykind;
		this.birthday = birthday;
		this.age = age;
	}
}
