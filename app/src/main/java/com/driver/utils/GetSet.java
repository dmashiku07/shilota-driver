package com.driver.utils;

/****************
 *
 * @author 'Hitasoft Technologies'
 *
 * Description:
 * This class is used for get and set logged user data
 *
 * Revision History:
 * Version 1.0 - Initial Version
 *
 *****************/
public class GetSet {
    private static boolean isLogged = false;
    private static String userId = null;
    private static String token = null;
    private static String userName = null;
    private static String email = null;
    private static String fullName = null;
    private static String imageUrl = null;
    private static String password = null;
    private static String wallet = null;

    public static String getAdUnitId() {
        return AdUnitId;
    }

    public static void setAdUnitId(String adUnitId) {
        AdUnitId = adUnitId;
    }

    private static String AdUnitId = "";

    public static String getIsBannerEnable() {
        return isBannerEnable;
    }

    public static void setIsBannerEnable(String isBannerEnable) {
        GetSet.isBannerEnable = isBannerEnable;
    }

    private static String isBannerEnable = "";

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        GetSet.password = password;
    }

    public static void Logingin(String userId, String email, String fullName, String imageUrl, String token, String wallet) {
        GetSet.isLogged = true;
        GetSet.userId = userId;
        GetSet.fullName = fullName;
        GetSet.imageUrl = imageUrl;
        GetSet.email = email;
        GetSet.token = token;
        GetSet.wallet = wallet;
    }

    public static String getWallet() {
        return wallet;
    }

    public static void setWallet(String wallet) {
        GetSet.wallet = wallet;
    }

    public static boolean isLogged() {
        return isLogged;
    }

    public static void setLogged(boolean isLogged) {
        GetSet.isLogged = isLogged;
    }

    public static String getUserId() {
        return userId;
    }


    public static void setUserId(String userId) {
        GetSet.userId = userId;
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {
        GetSet.email = email;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        GetSet.token = token;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        GetSet.userName = userName;
    }

    public static String getFullName() {
        return fullName;
    }

    public static void setFullName(String fullName) {
        GetSet.fullName = fullName;
    }

    public static String getImageUrl() {
        return imageUrl;
    }

    public static void setImageUrl(String imageUrl) {
        GetSet.imageUrl = imageUrl;
    }

    public static void logout() {
        GetSet.isLogged = false;
        GetSet.setUserId(null);
        GetSet.setUserName(null);
        GetSet.setImageUrl(null);
        GetSet.setFullName(null);
        GetSet.setToken(null);
        GetSet.setWallet(null);

    }

}
