package com.qpidnetwork.request.item;

import com.qpidnetwork.request.RequestEnum.Children;
import com.qpidnetwork.request.RequestEnum.Drink;
import com.qpidnetwork.request.RequestEnum.Education;
import com.qpidnetwork.request.RequestEnum.English;
import com.qpidnetwork.request.RequestEnum.Marry;
import com.qpidnetwork.request.RequestEnum.Profession;
import com.qpidnetwork.request.RequestEnum.Religion;
import com.qpidnetwork.request.RequestEnum.Smoke;
import com.qpidnetwork.request.RequestEnum.Zodiac;

/**
 * 查询个人资料
 * @author Samson Fan
 *
 */
public class MyProfileItem
{
	public MyProfileItem() {
		
	}

	public MyProfileItem(
			String ladyId,
			String firstname,
			String lastname,
			int age,
			String country,
			String province,
			String city,
			String birthday,
			int zodiac,
			String weight,
			String height,
			int smoke,
			int drink,
			int english,
			int religion,
			int education,
			int profession,
			int children,
			int marry,
			String aboutMe,
			String manAge1,
			String manAge2,
			String lastRefresh,
			String photoUrl,
			String[] photoUrls,
			String[] thumbUrls
			) 
	{
		this.ladyId = ladyId;
		this.firstname = firstname;
		this.lastname = lastname;
		this.age = age;
		this.country = country;
		this.province = province;
		this.city = city;
		this.birthday = birthday;
		
		if (zodiac < 0 || zodiac >= Zodiac.values().length) {
			this.zodiac = Zodiac.values()[0];
		}
		else {
			this.zodiac = Zodiac.values()[zodiac];
		}
		
		this.weight = weight;
		this.height = height;
		
		if(smoke < 0 || smoke >= Smoke.values().length) {
			this.smoke = Smoke.values()[0];
		} else {
			this.smoke = Smoke.values()[smoke];
		}
		
		if(drink < 0 || drink >= Drink.values().length) {
			this.drink = Drink.values()[0];
		} else {
			this.drink = Drink.values()[drink];
		}
		
		if (english < 0 || english >= English.values().length){
			this.english = English.values()[0];
		} 
		else {
			this.english = English.values()[english];
		}
		
		if(religion < 0 || religion >= Religion.values().length) {
			this.religion = Religion.values()[0];
		} else {
			this.religion = Religion.values()[religion];
		}
		
		if(education < 0 || education >= Education.values().length) {
			this.education = Education.values()[0];
		} else {
			this.education = Education.values()[education];
		}
		
		if(profession < 0 || profession >= Profession.values().length) {
			this.profession = Profession.values()[0];
		} else {
			this.profession = Profession.values()[profession];
		}
		
		if(children < 0 || children >= Children.values().length) {
			this.children = Children.values()[0];
		} else {
			this.children = Children.values()[children];
		}
		
		if(marry < 0 || marry >= Marry.values().length) {
			this.marry = Marry.values()[0];
		} else {
			this.marry = Marry.values()[marry];
		}
		
		this.aboutMe = aboutMe;
		this.manAge1 = manAge1;
		this.manAge2 = manAge2;
		this.lastRefresh = lastRefresh;
		this.photoUrl = photoUrl;
		this.photoUrls = photoUrls;
		this.thumbUrls = thumbUrls;
	}
	
	public String ladyId;
	public String firstname;
	public String lastname;
	public int age;
	public String country;
	public String province;
	public String city;
	public String birthday;
	public Zodiac zodiac;
	public String weight;
	public String height;
	public Smoke smoke;
	public Drink drink;
	public English english;
	public Religion religion;
	public Education education;
	public Profession profession;
	public Children children;
	public Marry marry;
	public String aboutMe;
	public String manAge1;
	public String manAge2;
	public String lastRefresh;
	public String photoUrl;
	public String[] photoUrls;
	public String[] thumbUrls;
}
