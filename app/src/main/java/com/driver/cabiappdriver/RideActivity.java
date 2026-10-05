package com.driver.cabiappdriver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.driver.external.FontCache;
import com.driver.helper.LocaleManager;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.driver.cabiappdriver.R;
import com.driver.model.RideHistoryData;
import com.driver.model.ScheduledRideData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hitasoft on 22/4/18.
 */

public class RideActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = RideActivity.class.getSimpleName();
    ImageView backBtn;
    TextView title;
    TabLayout tabLayout;
    ViewPager viewPager;

    public static void bookingRides(ApiInterface apiInterface, final BookingAdapter bookingAdapter, final ArrayList<RideHistoryData.result> datas,
                                    final RelativeLayout nullLay, final RelativeLayout progress, final SwipeRefreshLayout mSwipeRefreshLayout) {
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("type", "booking");

        Log.v(TAG, "bookingRidesParams" + map);
        Call<RideHistoryData> call3 = apiInterface.rideHistory(GetSet.getToken(), map);
        call3.enqueue(new Callback<RideHistoryData>() {
            @Override
            public void onResponse(Call<RideHistoryData> call, Response<RideHistoryData> response) {
                try {
                    progress.setVisibility(View.GONE);
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                    }
                    datas.clear();
                    RideHistoryData rideData = response.body();
                    Log.e("bookingres", "-" + rideData);
                    if (rideData != null && Constants.TRUE.equals(rideData.status) && rideData.result != null) {
                        datas.addAll(rideData.result);

                    } else if (rideData != null && "false".equals(rideData.status)) {

                    }

                    if (datas.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                    bookingAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<RideHistoryData> call, Throwable t) {
                Log.e(TAG, "bookingRidesError: " + t.getMessage());
                call.cancel();
                progress.setVisibility(View.GONE);
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                }
                if (datas.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                } else {
                    nullLay.setVisibility(View.GONE);
                }
            }
        });
    }


    public static void historyRides(ApiInterface apiInterface, final HistoryAdapter historyAdapter, final ArrayList<RideHistoryData.result> datas,
                                    final RelativeLayout nullLay, final RelativeLayout progress, final SwipeRefreshLayout mSwipeRefreshLayout) {
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("type", "passed");

        Log.v(TAG, "historyRidesParams=" + map);
        Call<RideHistoryData> call3 = apiInterface.rideHistory(GetSet.getToken(), map);
        call3.enqueue(new Callback<RideHistoryData>() {
            @Override
            public void onResponse(Call<RideHistoryData> call, Response<RideHistoryData> response) {
                try {
                    progress.setVisibility(View.GONE);
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                    }
                    datas.clear();
                    RideHistoryData rideData = response.body();
                    if (rideData != null && Constants.TRUE.equals(rideData.status) && rideData.result != null) {
                        datas.addAll(rideData.result);
                    } else if (rideData != null && "false".equals(rideData.status)) {

                    }

                    if (datas.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                    historyAdapter.notifyDataSetChanged();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<RideHistoryData> call, Throwable t) {
                Log.v(TAG, "historyRidesError: " + t.getMessage());
                call.cancel();
                if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
                }
                progress.setVisibility(View.GONE);
                if (datas.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                } else {
                    nullLay.setVisibility(View.GONE);
                }
            }
        });
    }

   /* public static void RideDetail(Context context, ApiInterface apiInterface, String onride_id, String from) {
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("onride_id", onride_id);
        map.put("type", from);

        Log.v("RideDetail params:", "" + map);
        Call<Map<String, String>> call3 = apiInterface.ridedetails(GetSet.getToken(), map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                try {
                    Log.v("RideDetail", "response=" + new Gson());
                    Map<String, String> rideData = response.body();
                    if (rideData != null && !rideData.isEmpty()) {
                        if (rideData.getItem("status").equals("true")) {
                            Intent b = new Intent(context, MapsActivity.class);
                            b.putExtra("pickuppoint", rideData.getItem("pickup_location"));
                            b.putExtra("pickuplat", Double.valueOf(rideData.getItem("pickup_lat")));
                            b.putExtra("pickuplng", Double.valueOf(rideData.getItem("pickup_lng")));
                            b.putExtra("droppoint", rideData.getItem("drop_location"));
                            b.putExtra("from", from);
                            b.putExtra("onrideid", onride_id);
                            b.putExtra("droplat", Double.valueOf(rideData.getItem("drop_lat")));
                            b.putExtra("droplng", Double.valueOf(rideData.getItem("drop_lng")));
                            context.startActivity(b);

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.v("Failed", "TEST");
                call.cancel();
            }
        });
    }*/

    public static void RideDetail(final Context context, final ApiInterface apiInterface,
                                  final String onride_id, final String from) {
        if (TextUtils.isEmpty(onride_id)) {
            Toast.makeText(context, context.getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
            return;
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("onride_id", onride_id);
        map.put("type", from);

        Log.v(TAG, "RideDetailParams:" + map);
        Call<HashMap<String, String>> call3 = apiInterface.ridedetails(GetSet.getToken(), map);
        call3.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                try {

                    HashMap<String, String> rideData = response.body();
                    Log.v(TAG, "RideDetailRes=" + rideData);
                    if (rideData != null && !rideData.isEmpty()) {
                        if (Constants.TRUE.equals(rideData.get("status"))) {
                            rideData.put(Constants.TAG_ONRIDE_ID, onride_id);
                            rideData.put("from", from);
                            Intent b = new Intent(context, MapsActivity.class);
                            b.putExtra("data", rideData);

                            context.startActivity(b);

                        } else if ("error".equalsIgnoreCase(rideData.get("status"))) {
                            GetSet.logout();
                            Intent intent = new Intent(context, WelcomeActivity.class);
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, context.getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, context.getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, context.getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                Log.e(TAG, "RideDetailError:" + t.getMessage());
                call.cancel();
            }
        });
    }

    private static String safeText(String value) {
        return value == null ? "" : value;
    }

    private static String safeAmount(String value) {
        return TextUtils.isEmpty(value) ? "0" : value;
    }

    private static String safeStatus(String value) {
        return TextUtils.isEmpty(value) ? "accepted" : value;
    }

    private static void setRideTime(TextView dateView, Context context, String pickupTime) {
        if (TextUtils.isEmpty(pickupTime)) {
            dateView.setText("");
            return;
        }
        try {
            dateView.setText(ApplicationClass.getRideTimeFromTimeStamp(context, Long.parseLong(pickupTime)));
        } catch (NumberFormatException e) {
            dateView.setText("");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_activity);
        backBtn = findViewById(R.id.backBtn);
        title = findViewById(R.id.title);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        if (LocaleManager.isRTL()) {
            backBtn.setRotation(180);
        } else {
            backBtn.setRotation(0);
        }
        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.ride_history));
        backBtn.setOnClickListener(this);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        changeTabsFont();
    }

    // For set custom font in tab
    private void changeTabsFont() {

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(FontCache.get("font_regular.ttf", this));
                }
            }
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Booking(), getString(R.string.booked));
        adapter.addFragment(new Upcoming(), getString(R.string.upcoming_rides));
        adapter.addFragment(new History(), getString(R.string.ride_history));
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backBtn) {
            finish();
        }
    }


    public static class Booking extends Fragment {

        BookingAdapter bookingAdapter;
        RecyclerView recyclerView;
        LinearLayoutManager linearLayoutManager;
        SwipeRefreshLayout mSwipeRefreshLayout = null;
        ApiInterface apiInterface;
        RelativeLayout nullLay, progress;
        TextView nullText;
        ImageView nullImage;
        FrameLayout mAdView;
        ArrayList<RideHistoryData.result> datas = new ArrayList<>();

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.ride_recyclerview_layout, container, false);

            recyclerView = rootView.findViewById(R.id.recyclerView);
            mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
            nullLay = rootView.findViewById(R.id.nullLay);
            nullText = rootView.findViewById(R.id.nullText);
            nullImage = rootView.findViewById(R.id.nullImage);
            progress = rootView.findViewById(R.id.progress);
            mAdView = rootView.findViewById(R.id.adLay);
            nullImage.setImageResource(R.drawable.no_ride);
            nullText.setText(getString(R.string.no_ride));

            mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));


            RelativeLayout.LayoutParams banerparams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            banerparams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            if (Constants.GOOGLE_ADS) {
                loadAd();
            }


            return rootView;
        }

        private void loadAd() {
            //Addon for googleads
            //BannerAdUtils.loadBannerAd(getActivity(), mAdView);
        }

        @Override
        public void onResume() {
            super.onResume();
            mSwipeRefreshLayout.setRefreshing(true);
            bookingRides(apiInterface, bookingAdapter, datas, nullLay, progress, mSwipeRefreshLayout);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            apiInterface = ApiClient.getClient().create(ApiInterface.class);

            linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

            bookingAdapter = new BookingAdapter(apiInterface, getActivity(), datas);
            recyclerView.setAdapter(bookingAdapter);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    bookingRides(apiInterface, bookingAdapter, datas, nullLay, progress, mSwipeRefreshLayout);
                    Log.v("Onrefresh:", "On refresh");
                }
            });
            //  bookingRides(apiInterface, bookingAdapter, datas, nullLay, progress, mSwipeRefreshLayout);
        }
    }

    public static class Upcoming extends Fragment {
        private ApiInterface apiInterface;
        private LinearLayout rideList;
        private TextView emptyText;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.ride_upcoming_fragment, container, false);
            apiInterface = ApiClient.getClient().create(ApiInterface.class);
            rideList = rootView.findViewById(R.id.rideList);
            emptyText = rootView.findViewById(R.id.emptyText);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            loadUpcomingRides();
        }

        private void loadUpcomingRides() {
            apiInterface.getScheduledUpcoming(GetSet.getToken(), GetSet.getUserId()).enqueue(new Callback<ScheduledRideData>() {
                @Override
                public void onResponse(Call<ScheduledRideData> call, Response<ScheduledRideData> response) {
                    ScheduledRideData body = response.body();
                    rideList.removeAllViews();
                    if (!response.isSuccessful() || body == null) {
                        emptyText.setVisibility(View.VISIBLE);
                        showUpcomingError(getString(R.string.tryagain));
                        return;
                    }
                    if (!Constants.TRUE.equals(body.status)) {
                        emptyText.setVisibility(View.VISIBLE);
                        if ("error".equalsIgnoreCase(body.status)) {
                            showUpcomingError(TextUtils.isEmpty(body.message)
                                    ? getString(R.string.tryagain) : body.message);
                        }
                        return;
                    }
                    if (body.result == null || body.result.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                        return;
                    }
                    emptyText.setVisibility(View.GONE);
                    for (ScheduledRideData.ScheduledRide ride : body.result) {
                        rideList.addView(buildRideRow(ride));
                    }
                }

                @Override
                public void onFailure(Call<ScheduledRideData> call, Throwable t) {
                    call.cancel();
                    emptyText.setVisibility(View.VISIBLE);
                    showUpcomingError(getString(R.string.tryagain));
                }
            });
        }

        private void showUpcomingError(String message) {
            if (getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        }

        private View buildRideRow(final ScheduledRideData.ScheduledRide ride) {
            Context context = requireContext();
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.VERTICAL);
            com.driver.helper.DriverUi.card(row);
            row.setOnClickListener(view -> RideDetail(
                    context,
                    apiInterface,
                    ride.onrideId,
                    safeStatus(ride.rideStatus)
            ));

            TextView title = new TextView(context);
            title.setText((ride.userName == null ? getString(R.string.rider) : ride.userName) + " - " + formatTime(ride.scheduleTime));
            title.setTextColor(getResources().getColor(R.color.textPrimary));
            com.driver.helper.DriverUi.title(title);
            row.addView(title);

            TextView route = new TextView(context);
            route.setText((ride.pickupLocation == null ? "" : ride.pickupLocation) + "\n" + (ride.dropLocation == null ? "" : ride.dropLocation));
            route.setTextColor(getResources().getColor(R.color.textsecondary));
            com.driver.helper.DriverUi.body(route);
            row.addView(route);

            TextView meta = new TextView(context);
            meta.setText((ride.categoryName == null ? "" : ride.categoryName) + " - "
                    + (ride.serviceType == null ? "ride" : ride.serviceType) + " - " + ride.rideStatus);
            meta.setTextColor(getResources().getColor(R.color.textsecondary));
            com.driver.helper.DriverUi.body(meta);
            row.addView(meta);

            Button preAccept = new Button(context);
            preAccept.setText(R.string.pre_accept);
            preAccept.setEnabled(!"accepted".equalsIgnoreCase(ride.rideStatus));
            preAccept.setOnClickListener(view -> preAcceptRide(ride.onrideId));
            row.addView(preAccept);
            return row;
        }

        private String formatTime(long scheduleTime) {
            if (scheduleTime <= 0) {
                return "";
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("Africa/Dar_es_Salaam"));
            return dateFormat.format(new Date(scheduleTime * 1000));
        }

        private void preAcceptRide(String onrideId) {
            Map<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_ONRIDE_ID, onrideId);
            apiInterface.preAcceptSchedule(GetSet.getToken(), map).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    Map<String, String> body = response.body();
                    if (getContext() != null) {
                        String message = body != null && body.get(Constants.TAG_MESSSAGE) != null
                                ? body.get(Constants.TAG_MESSSAGE) : getString(R.string.pre_accept);
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    loadUpcomingRides();
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                    if (getContext() != null) {
                        Toast.makeText(getContext(), getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public static class BookingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        ArrayList<RideHistoryData.result> Items;
        Context context;
        ApiInterface apiInterface;

        public BookingAdapter(ApiInterface apiInterface, Context context, ArrayList<RideHistoryData.result> Items) {
            this.Items = Items;
            this.context = context;
            this.apiInterface = apiInterface;

        }

        @Override
        public int getItemViewType(int position) {
            return Items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_history_item, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_loader, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                MyViewHolder viewHolder = (MyViewHolder) holder;
                RideHistoryData.result data = Items.get(position);
                if (data == null) {
                    return;
                }

                viewHolder.type.setVisibility(View.VISIBLE);
                switch (safeStatus(data.ride_status)) {
                    case "ontheway":
                        viewHolder.type.setText(context.getString(R.string.ontheway));
                        break;
                    default:
                        viewHolder.type.setText(safeStatus(data.ride_status));
                        break;
                }
                viewHolder.vehicleDetail.setText((safeText(data.category_name) + " " + safeText(data.vehicle_no)).trim());

                viewHolder.amount.setText(Constants.currency_symbol + " " + safeAmount(data.total_price));
                setRideTime(viewHolder.date, context, data.pickup_time);
            } else if (holder instanceof LoadingViewHolder) {

            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class LoadingViewHolder extends RecyclerView.ViewHolder {

            public LoadingViewHolder(View view) {
                super(view);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            RelativeLayout mainLay;
            TextView type, date, vehicleDetail, amount;

            public MyViewHolder(View view) {
                super(view);

                mainLay = view.findViewById(R.id.mainLay);
                type = view.findViewById(R.id.type);
                vehicleDetail = view.findViewById(R.id.vehicleDetail);
                date = view.findViewById(R.id.date);
                amount = view.findViewById(R.id.amount);
                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.mainLay) {
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION || position >= Items.size() || Items.get(position) == null) {
                        return;
                    }
                    RideHistoryData.result item = Items.get(position);
                    RideDetail(context, apiInterface, item.onride_id, safeStatus(item.ride_status));
                }
            }

        }
    }

    public static class History extends Fragment {

        HistoryAdapter historyAdapter;
        RecyclerView recyclerView;
        LinearLayoutManager linearLayoutManager;
        SwipeRefreshLayout mSwipeRefreshLayout = null;
        ApiInterface apiInterface;
        RelativeLayout nullLay, progress;
        TextView nullText;
        ImageView nullImage;
        FrameLayout mAdView;
        ArrayList<RideHistoryData.result> datas = new ArrayList<>();

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.ride_recyclerview_layout, container, false);

            recyclerView = rootView.findViewById(R.id.recyclerView);
            mAdView = rootView.findViewById(R.id.adLay);
            mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
            nullLay = rootView.findViewById(R.id.nullLay);
            nullText = rootView.findViewById(R.id.nullText);
            nullImage = rootView.findViewById(R.id.nullImage);
            progress = rootView.findViewById(R.id.progress);

            nullImage.setImageResource(R.drawable.no_ride);
            nullText.setText(getString(R.string.no_ride));

            mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));


            RelativeLayout.LayoutParams banerparams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            banerparams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            if (Constants.GOOGLE_ADS) {
                loadAd();
            }

            return rootView;
        }

        private void loadAd() {
            //Addon for googleads
            //BannerAdUtils.loadBannerAd(getActivity(), mAdView);
        }

        @Override
        public void onResume() {
            super.onResume();
            mSwipeRefreshLayout.setRefreshing(true);
            historyRides(apiInterface, historyAdapter, datas, nullLay, progress, mSwipeRefreshLayout);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            apiInterface = ApiClient.getClient().create(ApiInterface.class);

            linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

            historyAdapter = new HistoryAdapter(getActivity(), datas);
            recyclerView.setAdapter(historyAdapter);

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    historyRides(apiInterface, historyAdapter, datas, nullLay, progress, mSwipeRefreshLayout);
                }
            });
            //   historyRides(apiInterface, historyAdapter, datas, nullLay, progress, mSwipeRefreshLayout);
        }
    }

    public static class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        ArrayList<RideHistoryData.result> Items;
        Context context;

        public HistoryAdapter(Context context, ArrayList<RideHistoryData.result> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public int getItemViewType(int position) {
            return Items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_history_item, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_loader, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                MyViewHolder viewHolder = (MyViewHolder) holder;
                RideHistoryData.result data = Items.get(position);
                if (data == null) {
                    return;
                }

                viewHolder.imgRideStatus.setVisibility(View.VISIBLE);
                viewHolder.type.setVisibility(View.GONE);
                viewHolder.amount.setVisibility(View.VISIBLE);
                if ("completed".equals(data.ride_status)) {
                    viewHolder.amount.setText(Constants.currency_symbol + " " + safeAmount(data.total_price));

                    Glide.with(context).load(R.drawable.ic_completed).thumbnail(0.5f)
                            .apply(RequestOptions.circleCropTransform())
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(viewHolder.imgRideStatus);
                } else {
                    viewHolder.amount.setText(Constants.currency_symbol + " " + safeAmount(data.total_price));

                    Glide.with(context).load(R.drawable.ic_cancelled).thumbnail(0.5f)
                            .apply(RequestOptions.circleCropTransform())
                            .transition(new DrawableTransitionOptions().crossFade())
                            .into(viewHolder.imgRideStatus);
                }
                viewHolder.vehicleDetail.setText((safeText(data.category_name) + " " + safeText(data.vehicle_no)).trim());
                setRideTime(viewHolder.date, context, data.pickup_time);
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        public class LoadingViewHolder extends RecyclerView.ViewHolder {

            public LoadingViewHolder(View view) {
                super(view);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView type, date, vehicleDetail, amount;
            ImageView imgRideStatus;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                type = view.findViewById(R.id.type);
                mainLay = view.findViewById(R.id.mainLay);
                vehicleDetail = view.findViewById(R.id.vehicleDetail);
                date = view.findViewById(R.id.date);
                amount = view.findViewById(R.id.amount);
                imgRideStatus = view.findViewById(R.id.imgRideStatus);

                mainLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.mainLay) {
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION || position >= Items.size() || Items.get(position) == null) {
                        return;
                    }
                    RideHistoryData.result item = Items.get(position);
                    if (TextUtils.isEmpty(item.onride_id)) {
                        Toast.makeText(context, context.getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent ride = new Intent(context, RideCompleteActivity.class);
                    ride.putExtra("onride_id", item.onride_id);
                    ride.putExtra("from_history", true);
                    context.startActivity(ride);
                }
            }

        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
