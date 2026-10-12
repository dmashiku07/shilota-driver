package com.driver.utils;

import com.driver.model.AdminData;
import com.driver.model.AcceptanceSettingsData;
import com.driver.model.Dashboard;
import com.driver.model.DemandHeatmapData;
import com.driver.model.DriverSubscriptionStatusData;
import com.driver.model.HelpData;
import com.driver.model.LostItemData;
import com.driver.model.LostItemMessageData;
import com.driver.model.NotificationData;
import com.driver.model.ProfileData;
import com.driver.model.RideHistoryData;
import com.driver.model.ScheduledRideData;
import com.driver.model.SubscriptionPlanData;
import com.driver.model.SubscriptionPurchaseData;
import com.driver.model.UserData;
import com.driver.model.VehicleData;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface ApiInterface {

    @GET("driverapi/admindatas")
    Call<AdminData> adminData();

    @FormUrlEncoded
    @POST("driverapi/signup")
    Call<HashMap<String, String>> signup(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/getotp")
    Call<UserData> getDriverOTP(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/resetpasswordbyotp")
    Call<HashMap<String, String>> resetPasswordByOtp(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/signin")
    Call<UserData> signin(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("api/getotp")
    Call<UserData> getOTP(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("api/verifyotp")
    Call<UserData> verifyOTP(@FieldMap Map<String, String> params);
    @Multipart
    @POST("driverapi/uploaddoc")
    Call<Map<String, String>> uploadDoc(@Header("Authorization") String user_token, @Part MultipartBody.Part image, @Part("user_id") RequestBody user_id, @Part("uploaddocfor") RequestBody uploaddocfor);

    @FormUrlEncoded
    @POST("driverapi/postvehicledetails")
    Call<HashMap<String, String>> postVehicleDetails(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @GET("driverapi/profile/{userid}")
    Call<ProfileData> profile(@Header("Authorization") String user_token, @Path("userid") String user_id);

    @FormUrlEncoded
    @POST("driverapi/changepassword")
    Call<Map<String, String>> changePassword(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/liveme")
    Call<Map<String, String>> onlineStatus(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @Multipart
    @POST("driverapi/uploadprofileimage")
    Call<Map<String, String>> uploadProfileImage(@Header("Authorization") String user_token, @Part MultipartBody.Part image, @Part("user_id") RequestBody user_id);

    @FormUrlEncoded
    @POST("driverapi/updateprofile")
    Call<ProfileData> updateProfile(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/paymentinfo")
    Call<HashMap<String, String>> paymentinfo(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/goride")
    Call<HashMap<String, String>> goride(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/rideoffers/submit")
    Call<HashMap<String, Object>> submitRideOffer(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/rideoffers/withdraw")
    Call<HashMap<String, Object>> withdrawRideOffer(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @GET("driverapi/paymentinfo/{userid}")
    Call<HashMap<String, String>> getPaymentinfo(@Header("Authorization") String user_token, @Path("userid") String user_id);

    @GET("driverapi/getvehicledetails/{userid}")
    Call<VehicleData> getVehicleDetails(@Header("Authorization") String user_token, @Path("userid") String user_id);

    @GET("driverapi/dashboard/{userid}")
    Call<Dashboard> getDashBoard(@Header("Authorization") String user_token, @Path("userid") String user_id);

    @GET("driverapi/helppages")
    Call<HelpData> getHelp();

    @FormUrlEncoded
    @POST("driverapi/whereiam")
    Call<Map<String, String>> changeStatus(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/routepath")
    Call<HashMap<String, String>> routePath(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/completeride")
    Call<Map<String, String>> completeride(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/pushsignin")
    Call<Map<String, String>> pushsignin(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "driverapi/pushsignout", hasBody = true)
    Call<Map<String, String>> pushsignout(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/contactus")
    Call<Map<String, String>> contactus(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/addmoney")
    Call<Map<String, String>> addMoney(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @GET("driverapi/wallethistory/{user_id}")
    Call<com.driver.model.WalletHistoryData> walletHistory(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @FormUrlEncoded
    @POST("driverapi/ridehistory")
    Call<RideHistoryData> rideHistory(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/ridedetails")
    Call<HashMap<String, String>> ridedetails(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/sosalert")
    Call<Map<String, String>> sosAlert(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/blockrider")
    Call<Map<String, String>> blockRider(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @GET("driverapi/acceptancesettings/{userid}")
    Call<AcceptanceSettingsData> getAcceptanceSettings(@Header("Authorization") String user_token, @Path("userid") String user_id);

    @FormUrlEncoded
    @POST("driverapi/acceptancesettings")
    Call<AcceptanceSettingsData> saveAcceptanceSettings(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @GET("driverapi/notifications/{user_id}")
    Call<NotificationData> getNotifications(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @GET("driverapi/lostitems/{user_id}")
    Call<LostItemData> getLostItems(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @FormUrlEncoded
    @POST("driverapi/lostitems/{lost_item_id}/status")
    Call<Map<String, String>> updateLostItemStatus(@Header("Authorization") String user_token, @Path("lost_item_id") String lost_item_id, @FieldMap Map<String, String> params);

    @GET("driverapi/lostitems/{lost_item_id}/messages")
    Call<LostItemMessageData> getLostItemMessages(@Header("Authorization") String user_token, @Path("lost_item_id") String lost_item_id);

    @FormUrlEncoded
    @POST("driverapi/lostitems/{lost_item_id}/messages")
    Call<Map<String, String>> sendLostItemMessage(@Header("Authorization") String user_token, @Path("lost_item_id") String lost_item_id, @FieldMap Map<String, String> params);

    @GET("safety/demandheatmap?hours=24")
    Call<DemandHeatmapData> getDemandHeatmap(@Header("Authorization") String user_token);

    @GET("driverapi/scheduledupcoming/{user_id}")
    Call<ScheduledRideData> getScheduledUpcoming(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @GET("driverapi/subscriptionplans/{user_id}")
    Call<SubscriptionPlanData> getSubscriptionPlans(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @GET("driverapi/subscriptions/current/{user_id}")
    Call<DriverSubscriptionStatusData> getCurrentSubscription(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @GET("driverapi/subscriptions/history/{user_id}")
    Call<DriverSubscriptionStatusData> getSubscriptionHistory(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @FormUrlEncoded
    @POST("driverapi/subscriptions/initiate")
    Call<SubscriptionPurchaseData> initiateSubscriptionPurchase(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @GET("driverapi/subscriptions/verifyselcom/{transid}")
    Call<SubscriptionPurchaseData> verifySubscriptionSelcom(@Path("transid") String transid);

    @FormUrlEncoded
    @POST("driverapi/destinationbroadcasts")
    Call<HashMap<String, Object>> createDestinationBroadcast(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @GET("driverapi/destinationbroadcastsettings")
    Call<HashMap<String, Object>> getDestinationBroadcastSettings(@Header("Authorization") String user_token);

    @GET("driverapi/destinationbroadcasts/{user_id}")
    Call<HashMap<String, Object>> getDestinationBroadcasts(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @GET("driverapi/destinationbroadcasts/requests/{user_id}")
    Call<HashMap<String, Object>> getDestinationBroadcastRequests(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @GET("driverapi/destinationbroadcasts/verifyselcom/{transid}")
    Call<HashMap<String, Object>> verifyDestinationBroadcastSelcom(@Header("Authorization") String user_token, @Path("transid") String transid);

    @FormUrlEncoded
    @POST("driverapi/destinationbroadcasts/retrypayment")
    Call<HashMap<String, Object>> retryDestinationBroadcastPayment(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/destinationbroadcasts/requests/decision")
    Call<HashMap<String, Object>> decideDestinationBroadcastRequest(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/destinationbroadcasts/status")
    Call<HashMap<String, Object>> updateDestinationBroadcastStatus(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/preacceptschedule")
    Call<Map<String, String>> preAcceptSchedule(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/completeridedetails")
    Call<Map<String, String>> completeridedetails(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/paybycash")
    Call<Map<String, String>> paybycash(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/makecall")
    Call<Map<String, String>> makeCall(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/endcall")
    Call<Map<String, String>> endCall(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @GET("driverapi/call/ice-servers/{userid}")
    Call<HashMap<String, Object>> getCallIceServers(@Header("Authorization") String userToken, @Path("userid") String userId);

    @FormUrlEncoded
    @POST("driverapi/deleteaccount")
    Call<Map<String, String>> deleteAccount(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/getselcomurl")
    Call<UserData> getSelComUrl(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("driverapi/paybyselcom")
    Call<Map<String, String>> paybyselcom(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @GET("driverapi/verifyselcom/{transid}")
    Call<UserData> verifySelcom(@Path("transid") String transid);

    @GET
    Call<UserData> getMethod(@Url String url);
}
