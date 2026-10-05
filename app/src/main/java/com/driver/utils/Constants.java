package com.driver.utils;

/**
 * Created by hitasoft on 12/3/18.
 */

public class Constants {

    public final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    /*
     * SERVER SELECTION
     * Set USE_STAGING = true and fill in STAGING_HOST to point this build at the
     * Railway staging server. Set USE_STAGING = false before any production build.
     * STAGING_HOST is the host only: no https://, no trailing slash, no port.
     */
    public static final boolean USE_STAGING = true;
    public static final String STAGING_HOST = "shilota-admin-production.up.railway.app";
    public static final String PRODUCTION_HOST = "shilota.tz";
    public static final String SERVER_HOST = USE_STAGING ? STAGING_HOST : PRODUCTION_HOST;

    /* aligned with rider app */
    public static final String BASE_URL = "https://" + SERVER_HOST + "/";
    public static final String SOCKET_URL = "https://" + SERVER_HOST + "/";

    /**
     * AppRTC URL
     */
    public static final String APPRTC_URL = "https://" + SERVER_HOST;

    public static final int IMG_WT_HT = 200;

    public static final String TAG_STATUS = "status";
    public static final String TAG_MESSSAGE = "message";
    public static final String TAG_FULL_NAME = "full_name";
    public static final String TAG_EMAIL = "email";
    public static final String TAG_PASSWORD = "password";
    public static final String TAG_COUNTRY_CODE = "country_code";
    public static final String TAG_PHONE_NUMBER = "phone_number";
    public static final String TAG_USER_ID = "user_id";
    public static final String TAG_USER_NAME = "user_name";
    public static final String TAG_PICKUP_LOCATION = "pickup_location";
    public static final String TAG_DROP_LOCATION = "drop_location";
    public static final String TAG_VEHICLE_NAME = "vehicle_name";
    public static final String TAG_AVAILABLE_SEATS = "available_seats";
    public static final String TAG_BODY_TYPE = "body_type";
    public static final String TAG_AMENITITES = "amenities";
    public static final String TAG_VEHICLE_NUMBER = "vehicle_number";
    public static final String TAG_LICENSE_NO = "licence_no";
    public static final String TAG_LICENSE_DOC = "licence_doc";
    public static final String TAG_LICENSE_DATE = "licence_date";
    public static final String TAG_INSURANCE_NO = "insurance_no";
    public static final String TAG_INSURANCE_DOC = "insurance_doc";
    public static final String TAG_INSURANCE_DATE = "insurance_date";
    public static final String TAG_BOOK_NO = "book_no";
    public static final String TAG_BOOK_DOC = "book_doc";
    public static final String TAG_BOOK_DATE = "book_date";
    public static final String TAG_VEHICLE_DETAILS = "vehicle_details";
    public static final String TAG_LOCATION = "location";
    public static final String TAG_LATITUDE = "lat";
    public static final String TAG_LONGITUDE = "lon";
    public static final String TAG_PICKUP_LAT = "pickup_lat";
    public static final String TAG_PICKUP_LNG = "pickup_lng";
    public static final String TAG_DROP_LAT = "drop_lat";
    public static final String TAG_DROP_LNG = "drop_lng";
    public static final String TAG_PROFILE_IMAGE = "profileimage";
    public static final String TAG_NEW_PASSWORD = "newpassword";
    public static final String TAG_EMERGENCY_CONTACT = "emergency_contact";
    public static final String TAG_TYPE = "type";
    public static final String TAG_ROUTING_NO = "routingno";
    public static final String TAG_BANK_AC = "bankaccountno";
    public static final String TAG_NAME = "name";
    public static final String TAG_ADDRESS = "address";
    public static final String TAG_DRIVER_IMAGE = "driver_image";
    public static final String TAG_USER_IMAGE = "user_image";
    public static final String TAG_ONRIDE_ID = "onride_id";

    public static final int OVERLAY_REQUEST_CODE = 303;
    public static int OVERALL_LIMIT = 10;
    public static String DEFAULT_LANGUAGE = "English";
    public static String DEFAULT_LANGUAGE_CODE = "en";
    public static String TAG_LANGUAGE = "language";
    public static String TAG_LANGUAGE_CODE = "language_code";
    // Driver fares and balances are denominated in Tanzanian shillings.
    public static final String currency_symbol = "TSh";
    public static String emergencycontact = "";
    public static final boolean COMMISSION_ENABLED = true;
    public static final boolean DRIVER_SUBSCRIPTIONS_ENABLED = false;
    public static final boolean DRIVER_PRICING_ENABLED = false;
    public static final boolean PASSENGER_PRICE_BROADCAST_ENABLED = false;
    public static final boolean DRIVER_DESTINATION_BROADCAST_ENABLED = false;
    public static int DIALOG_DELAY = 2000;
    public static String TRUE = "true";

    public static final String TAG_AUDIO = "audio";
    public static final String TAG_VIDEO = "video";
    public static final String TAG_SENDER_ID = "sender_id";
    public static final String TAG_RECEIVER_ID = "receiver_id";
    public static final String TAG_CHAT_TYPE = "chat_type";
    public static final String TAG_FROM = "from";
    public static final String TAG_SEND = "send";
    public static final String TAG_RECEIVE = "receive";
    public static final String TAG_ROOM_ID = "room_id";
    public static final String TAG_ANDROID = "android";
    public static final String TAG_PLATFORM = "platform";
    public static final String TAG_USERTYPE = "user_type";
    public static final String TAG_TIMESTAMP = "timestamp";
    public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    public static final Boolean GOOGLE_ADS = false;
    public static final Boolean VIDEO_ADDON = false;
}