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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((age == null) ? 0 : age.hashCode());
		result = prime * result + ((birthday == null) ? 0 : birthday.hashCode());
		result = prime * result + (checkflg ? 1231 : 1237);
		result = prime * result + ((daykind == null) ? 0 : daykind.hashCode());
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)											return true;
		if (obj == null)											return false;
		if (getClass() != obj.getClass())							return false;
		
		ContactsStatus other = (ContactsStatus) obj;
		if (age == null) {	if (other.age != null)					return false;
		} else if (!age.equals(other.age))							return false;
		if (birthday == null) {	if (other.birthday != null)			return false;
		} else if (!birthday.equals(other.birthday))				return false;
		if (daykind == null) { if (other.daykind != null)			return false;
		} else if (!daykind.equals(other.daykind))					return false;
		if (displayName == null) { if (other.displayName != null)	return false;
		} else if (!displayName.equals(other.displayName))			return false;
		if (checkflg != other.checkflg)								return false;

		return true;
	}

	@Override
	public String toString() {
		return "output:" + displayName + " " + daykind + " " + birthday + " " + age;
	}
}
