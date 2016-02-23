package com.spark.data;


import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "t_user")
class UserInfo implements Serializable,Table{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField
    private String nickname="请登录";
    @DatabaseField
    private String mobile;
    @DatabaseField
    private String password = "";
    @DatabaseField
    private String pwdType = "";
    @DatabaseField
    private int sex = 0;
    @DatabaseField
    private int birthday = 0;
    @DatabaseField
    private String email;
    @DatabaseField
    private String face = "";
    @DatabaseField
    private int subscribe = 0;
    @DatabaseField
    private int subscribeTime = 0;
    @DatabaseField
    private int userRight = 0;
    @DatabaseField
    private String city = "";
    @DatabaseField
    private String rovince = "";
    @DatabaseField
    private String country = "";
    @DatabaseField
    private int lastLoginTime = 0;
    @DatabaseField
    private int ver = 0;


    /**
     * default constructor
     */
    public UserInfo() {
    }


    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMobile() {
        return this.mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPwdType() {
        return this.pwdType;
    }

    public void setPwdType(String pwdType) {
        this.pwdType = pwdType;
    }

    public int getSex() {
        return this.sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getBirthday() {
        return this.birthday;
    }

    public void setBirthday(int birthday) {
        this.birthday = birthday;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFace() {
        return this.face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public int getSubscribe() {
        return this.subscribe;
    }

    public void setSubscribe(int subscribe) {
        this.subscribe = subscribe;
    }

    public int getSubscribeTime() {
        return this.subscribeTime;
    }

    public void setSubscribeTime(int subscribeTime) {
        this.subscribeTime = subscribeTime;
    }

    public int getUserRight() {
        return this.userRight;
    }

    public void setUserRight(int userRight) {
        this.userRight = userRight;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRovince() {
        return this.rovince;
    }

    public void setRovince(String rovince) {
        this.rovince = rovince;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getLastLoginTime() {
        return this.lastLoginTime;
    }

    public void setLastLoginTime(int lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public int getVer() {
        return this.ver;
    }

    public void setVer(int ver) {
        this.ver = ver;
    }


    @Override
    public String toString() {
        return "UserInfo{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", mobile='" + mobile + '\'' +
                ", password='" + password + '\'' +
                ", pwdType='" + pwdType + '\'' +
                ", sex=" + sex +
                ", birthday=" + birthday +
                ", email='" + email + '\'' +
                ", face='" + face + '\'' +
                ", subscribe=" + subscribe +
                ", subscribeTime=" + subscribeTime +
                ", userRight=" + userRight +
                ", city='" + city + '\'' +
                ", rovince='" + rovince + '\'' +
                ", country='" + country + '\'' +
                ", lastLoginTime=" + lastLoginTime +
                ", ver=" + ver +
                '}';
    }


    private static enum Gender {
        unknown,
        male,
        female
    }

    public Gender getGender() {
        if (sex == 0) return Gender.male;
        else if (sex == 1) return Gender.female;
        else return Gender.unknown;
    }

    public void setGender(int gender) {
        this.sex = gender;
    }
}