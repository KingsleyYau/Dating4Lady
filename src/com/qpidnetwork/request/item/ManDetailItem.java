package com.qpidnetwork.request.item;

import java.io.Serializable;

import com.qpidnetwork.request.RequestEnum.Children;
import com.qpidnetwork.request.RequestEnum.Country;
import com.qpidnetwork.request.RequestEnum.Drink;
import com.qpidnetwork.request.RequestEnum.Education;
import com.qpidnetwork.request.RequestEnum.Ethnicity;
import com.qpidnetwork.request.RequestEnum.Height;
import com.qpidnetwork.request.RequestEnum.Income;
import com.qpidnetwork.request.RequestEnum.Language;
import com.qpidnetwork.request.RequestEnum.Marry;
import com.qpidnetwork.request.RequestEnum.PHOTO_STATUS;
import com.qpidnetwork.request.RequestEnum.Profession;
import com.qpidnetwork.request.RequestEnum.Religion;
import com.qpidnetwork.request.RequestEnum.Smoke;
import com.qpidnetwork.request.RequestEnum.Weight;

public class ManDetailItem implements Serializable {
	
	private static final long serialVersionUID = 585399136851344089L;

	public ManDetailItem() {
		
	}

	/**
	 * 查询女士详细信息结构体
	 * @param man_id			男士id
	 * @param firstname			男士first name
	 * @param lastname			男士last name
	 * 
	 * @param country			国家
	 * @param province			省份
	 * @param city				城市
	 * 
	 * @param join_date			注册时间
	 * @param birthday			出生日期
	 * 
	 * @param weight			体重
	 * @param height			身高
	 * @param smoke				吸烟情况
	 * @param drink				喝酒情况
	 * @param language			语言
	 * @param religion			宗教情况
	 * @param education			教育情况
	 * @param profession		职业
	 * @param children			子女状况
	 * @param marry				婚姻状况
	 * @param income			收入情况
	 * @param ethnicity			种族
	 * 
	 * @param about_me			个人简介
	 * @param online			是否在线
	 * @param favorite			是否收藏
	 * 
	 * @param photo_url			男士头像URL
	 * @param photo_big_url		男士大头像URL
	 * @param photo_status		男士头像状态
	 * @param receive_admirer	是否接收意向信
	 */
	public ManDetailItem(
			String man_id,
			String firstname,
			String lastname,
			
			int country,
			String province,
			String city,
			
			String join_date,
			String birthday,
			
			int weight,
			int height,
			int smoke,
			int drink,
			int language,
			int religion,
			int education,
			int profession,
			int children,
			int marry,
			int income,
			int ethnicity,
			
			String about_me,
			boolean online,
			boolean favorite,
			
			String photo_url,
			String photo_big_url,
			int photo_status,
			
			boolean receive_admirer
			) {
		this.man_id = man_id;
		this.firstname = firstname;
		this.lastname = lastname;
		
		if( country < 0 || country >= Country.values().length ) {
			this.country = Country.values()[0];
		} else {
			this.country = Country.values()[country];
		}
		this.province = province;
		this.city = city;
		
		this.join_date = join_date;
		this.birthday = birthday;
		
		if( marry < 0 || marry >= Marry.values().length ) {
			this.marry = Marry.values()[0];
		} else {
			this.marry = Marry.values()[marry];
		}
		
		if( height < 0 || height >= Height.values().length ) {
			this.height = Height.values()[0];
		} else {
			this.height = Height.values()[height];
		}
		
		if( weight < 0 || weight >= Weight.values().length ) {
			this.weight = Weight.values()[0];
		} else {
			this.weight = Weight.values()[weight];
		}
		
		if( smoke < 0 || smoke >= Smoke.values().length ) {
			this.smoke = Smoke.values()[0];
		} else {
			this.smoke = Smoke.values()[smoke];
		}
		
		if( drink < 0 || drink >= Drink.values().length ) {
			this.drink = Drink.values()[0];
		} else {
			this.drink = Drink.values()[drink];
		}
		
		if( language < 0 || language >= Language.values().length ) {
			this.language = Language.values()[0];
		} else {
			this.language = Language.values()[language];
		}
		
		if( religion < 0 || religion >= Religion.values().length ) {
			this.religion = Religion.values()[0];
		} else {
			this.religion = Religion.values()[religion];
		}
		
		if( education < 0 || education >= Education.values().length ) {
			this.education = Education.values()[0];
		} else {
			this.education = Education.values()[education];
		}
		
		if( profession < 0 || profession >= Profession.values().length ) {
			this.profession = Profession.values()[0];
		} else {
			this.profession = Profession.values()[profession];
		}
		
		if( ethnicity < 0 || ethnicity >= Ethnicity.values().length ) {
			this.ethnicity = Ethnicity.values()[0];
		} else {
			this.ethnicity = Ethnicity.values()[ethnicity];
		}
		
		if( income < 0 || income >= Income.values().length ) {
			this.income = Income.values()[0];
		} else {
			this.income = Income.values()[income];
		}
		
		if( children < 0 || children >= Children.values().length ) {
			this.children = Children.values()[0];
		} else {
			this.children = Children.values()[children];
		}
		
		this.about_me = about_me;
		
		this.online = online;
		this.favorite = favorite;

		this.photo_url = photo_url;
		this.photo_big_url = photo_big_url;
		
		this.photo_status = PHOTO_STATUS.None;
		if ( photo_status >= 0 && photo_status < PHOTO_STATUS.values().length) {
			this.photo_status = PHOTO_STATUS.values()[photo_status];
		}
		
		this.receive_admirer = receive_admirer;
	}
	
	public String toString() {
		String result = "{ ";
		result += "man_id = " + man_id + ", \n";
		result += "firstname = " + firstname + ", \n";
		result += "lastname = " + lastname + ", \n";
		result += "country = " + country.name() + ", \n";
		result += "province = " + province + ", \n";
		result += "city = " + city + ", \n";
		result += "join_date = " + join_date + ", \n";
		result += "birthday = " + birthday + ", \n";
		result += "weight = " + weight.name() + ", \n";
		result += "height = " + height.name() + ", \n";
		result += "smoke = " + smoke.name() + ", \n";
		result += "drink = " + drink.name() + ", \n";
		result += "language = " + language.name() + ", \n";
		result += "religion = " + religion.name() + ", \n";
		result += "education = " + education.name() + ", \n";
		result += "profession = " + profession.name() + ", \n";
		result += "children = " + children.name() + ", \n";
		result += "marry = " + marry.name() + ", \n";
		result += "income = " + income.name() + ", \n";
		result += "ethnicity = " + ethnicity.name() + ", \n";
		result += "about_me = " + about_me + ", \n";
		result += "online = " + online + ", \n";
		result += "favorite = " + favorite + ", \n";
		result += "photo_url = " + photo_url + ", \n";
		result += "photo_big_url = " + photo_big_url + ", \n";
		result += "photo_status = " + photo_status.name() + ", \n";
		result += "receive_admirer = " + receive_admirer;
		result += " }";
		return result;
	}
	
	public String man_id;
	public String firstname;
	public String lastname;
	
	public Country country;
	public String province;
	public String city;
	
	public String join_date;
	public String birthday;
	
	public Weight weight;
	public Height height;
	public Smoke smoke;
	public Drink drink;
	public Language language;
	public Religion religion;
	public Education education;
	public Profession profession;
	public Children children;
	public Marry marry;
	public Income income;
	public Ethnicity ethnicity;
	
	public String about_me;
	
	public boolean online;
	public boolean favorite;
	
	public String photo_url;
	public String photo_big_url;
	
	public PHOTO_STATUS photo_status;	
	
	public boolean receive_admirer;
	
}
