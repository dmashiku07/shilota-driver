package com.driver.cabiappdriver;

import android.content.Context;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.driver.helper.LocaleManager;
import com.driver.helper.RecyclerItemClickListener;
import com.driver.cabiappdriver.R;

import com.driver.model.AdminData;
import com.driver.cabiappdriver.databinding.ActivityTypeListBinding;
import com.driver.cabiappdriver.databinding.TypeListItemBinding;

import java.util.ArrayList;

import static com.driver.cabiappdriver.VehicleDetailsActivity.REQUEST_BODY_TYPE;

public class TypeListActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = TypeListActivity.class.getSimpleName();
    public ArrayList<String> selectedIds = new ArrayList<String>();
    public ArrayList<String> selectedNames = new ArrayList<String>();
    ActivityTypeListBinding listBinding;
    ImageView backBtn;
    TextView title;
    LinearLayoutManager linearLayoutManager;
    RecyclerAdapter recyclerAdapter;
    String from, selectedName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_list);
        listBinding = DataBindingUtil.setContentView(this, R.layout.activity_type_list);
        backBtn = findViewById(R.id.backBtn);

        if (LocaleManager.isRTL()){
            backBtn.setRotation(180);
        } else {
            backBtn.setRotation(0);
        }
        backBtn.setVisibility(View.VISIBLE);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        listBinding.recyclerView.setLayoutManager(linearLayoutManager);
        backBtn.setOnClickListener(this);
        Bundle bundle = this.getIntent().getExtras();
        from = bundle.getString("from");
        Log.v(TAG, "" + bundle.getString("selectedName"));
        if (bundle.getString("selectedName") != null)
            selectedName = bundle.getString("selectedName");

        if (bundle.getStringArrayList("selectedNames") != null)
            selectedNames = bundle.getStringArrayList("selectedNames");

        if (bundle.getStringArrayList("selectedId") != null)
            selectedIds = bundle.getStringArrayList("selectedId");

        Log.v(TAG, "from=" + from);
        if (from.equals("body_type")) {
            listBinding.pageTitle.setText(R.string.vehicle_type_title);
            recyclerAdapter = new RecyclerAdapter(this, SplashActivity.bodyTypeArrayList, null, from);
            listBinding.recyclerView.setAdapter(recyclerAdapter);
            recyclerAdapter.notifyDataSetChanged();
            listBinding.nextBtn.setVisibility(View.GONE);
        } else if (from.equals("amenities")) {
            listBinding.pageTitle.setText(R.string.amenities_title);
            recyclerAdapter = new RecyclerAdapter(this, null, SplashActivity.amenitiesArrayList, from);
            listBinding.recyclerView.setAdapter(recyclerAdapter);
            recyclerAdapter.notifyDataSetChanged();
            listBinding.nextBtn.setVisibility(View.VISIBLE);
            listBinding.nextBtn.setOnClickListener(this);
        }
        listBinding.recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, listBinding.recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (from.equals("body_type")) {
                    selectedName = SplashActivity.bodyTypeArrayList.get(position).name;
                    recyclerAdapter.notifyDataSetChanged();
                    Intent nextPgInt = new Intent(TypeListActivity.this, VehicleDetailsActivity.class);
                    nextPgInt.putExtra("from", from);
                    nextPgInt.putExtra("selectedName", selectedName);
                    nextPgInt.putExtra("selectedId", SplashActivity.bodyTypeArrayList.get(position).id);
                    setResult(REQUEST_BODY_TYPE, nextPgInt);
                    finish();
                } else if (from.equals("amenities")) {
                    if (selectedIds.contains(recyclerAdapter.getItem(position).amentiesid)) {
                        selectedIds.remove(recyclerAdapter.getItem(position).amentiesid);
                        selectedNames.remove(recyclerAdapter.getItem(position).amentiesname);
                    } else {
                        selectedIds.add(recyclerAdapter.getItem(position).amentiesid);
                        selectedNames.add(recyclerAdapter.getItem(position).amentiesname);
                    }
                    recyclerAdapter.notifyDataSetChanged();
//                    Log.e(TAG, "listToJSONArray: " + selectedIds.size());
//                    Log.e(TAG, "listToJSONArray: " + selectedNames.size());
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.backBtn) {
            onBackPressed();

        } else if (id == R.id.nextBtn) {
            String selectedName = getStringFromList(selectedNames);
            Intent nextPgInt = new Intent(TypeListActivity.this, VehicleDetailsActivity.class);
            nextPgInt.putExtra("from", "addvehicle");
            nextPgInt.putExtra("selectedName", selectedName);
            nextPgInt.putStringArrayListExtra("selectedNames", selectedNames);
            nextPgInt.putStringArrayListExtra("selectedId", selectedIds);

            setResult(RESULT_OK, nextPgInt);
            finish();
        }
    }


    private String getStringFromList(ArrayList<String> selectedNames) {
        if (selectedNames.size() == 0)
            return "";
        else {
            return TextUtils.join(",", selectedNames);
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
        ArrayList<AdminData.Amenities> itemList;
        ArrayList<AdminData.BodyType> itemList1;
        Context context;
        String from;

        public RecyclerAdapter(Context context, ArrayList<AdminData.BodyType> items, ArrayList<AdminData.Amenities> items1, String type) {
            this.context = context;
            itemList = items1;
            itemList1 = items;
        }

        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TypeListItemBinding itemBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.type_list_item, parent, false);
            return new RecyclerAdapter.ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            if (holder.getItemViewType() == 0) {
                holder.getBinding().typeName.setText(itemList.get(position).amentiesname);
                if (selectedIds.contains(itemList.get(position).amentiesid)) {
                    holder.getBinding().tick.setVisibility(View.VISIBLE);
                } else {
                    holder.getBinding().tick.setVisibility(View.INVISIBLE);
                }
            } else {
                holder.getBinding().typeName.setText(itemList1.get(position).name);
                if (itemList1.get(position).name.equals(selectedName)) {
                    holder.getBinding().tick.setVisibility(View.VISIBLE);
                } else {
                    holder.getBinding().tick.setVisibility(View.INVISIBLE);
                }
                /*holder.getBinding().typeLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedType=itemList1.getItem(position).name;
                        Intent nextPgInt=new Intent(TypeListActivity.this,VehicleDetailsActivity.class);
                        nextPgInt.putExtra("from",from);
                        nextPgInt.putExtra("selectedName",selectedType);
                        setResult(REQUEST_BODY_TYPE,nextPgInt);
                        finish();
                    }
                });*/
            }
        }

        @Override
        public int getItemViewType(int position) {
            return itemList != null ? 0 : 1;
        }

        @Override
        public int getItemCount() {
            return itemList != null ? itemList.size() : itemList1.size();
        }

        public AdminData.Amenities getItem(int position) {
            return itemList.get(position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TypeListItemBinding listItemBinding;

            public ViewHolder(TypeListItemBinding binding) {
                super(binding.getRoot());
                listItemBinding = binding;
            }

            public TypeListItemBinding getBinding() {
                return listItemBinding;
            }
        }
    }
}
