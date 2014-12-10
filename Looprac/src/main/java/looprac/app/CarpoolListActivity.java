package looprac.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

@SuppressWarnings("all")
public class CarpoolListActivity extends Activity {

    /* List item container class */
    private static class ScheduleVH {
        TextView routeTV;
        TextView deparTimeTV;
        TextView priceTV;
        Button carpoolInfoBT;
    }

    private BaseAdapter carpoolListAdapter;

    private ArrayList<CarpoolInfo> carpoolInfoList;
    private ArrayList<CarpoolInfo> tempCarpoolInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpool_list);

        /* Demo test code */
        carpoolInfoList = ((SerializableList) getIntent().getSerializableExtra("carpoollistdata")).getList();
        Collections.sort(carpoolInfoList, new CompRating());
        tempCarpoolInfoList = new ArrayList<CarpoolInfo>(carpoolInfoList);

        carpoolListAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                if (tempCarpoolInfoList.size() == 0) {
                    return 1;
                }
                else {
                    return tempCarpoolInfoList.size();
                }
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup viewGroup) {
                ScheduleVH scheduleVH;
                if (convertView == null) {
                    convertView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.carpool_info_item, null);
                    scheduleVH = new ScheduleVH();
                    scheduleVH.routeTV = (TextView) convertView.findViewById(R.id.route);
                    scheduleVH.deparTimeTV = (TextView) convertView.findViewById(R.id.departuretimeT);
                    scheduleVH.priceTV = (TextView) convertView.findViewById(R.id.price);
                    scheduleVH.carpoolInfoBT = (Button) convertView.findViewById(R.id.moreinfo);
                    convertView.setTag(scheduleVH);
                }
                else {
                    scheduleVH = (ScheduleVH) convertView.getTag();
                }
                if (tempCarpoolInfoList.size() == 0) {
                    scheduleVH.routeTV.setText("No carpool available");
                    scheduleVH.deparTimeTV.setVisibility(View.GONE);
                    scheduleVH.priceTV.setVisibility(View.GONE);
                    scheduleVH.carpoolInfoBT.setVisibility(View.GONE);
                }
                else {
                    scheduleVH.routeTV.setText(tempCarpoolInfoList.get(position).getDeparLoc().getCity() + " -> " + tempCarpoolInfoList.get(position).getArrLoc().getCity());
                    scheduleVH.deparTimeTV.setText(tempCarpoolInfoList.get(position).getDeparTime());
                    scheduleVH.priceTV.setText("$" + Integer.toString(tempCarpoolInfoList.get(position).getPrice()));
                    if (scheduleVH.carpoolInfoBT.getVisibility() == View.GONE) {
                        scheduleVH.carpoolInfoBT.setVisibility(View.VISIBLE);
                    }
                    if (scheduleVH.deparTimeTV.getVisibility() == View.GONE) {
                        scheduleVH.deparTimeTV.setVisibility(View.VISIBLE);
                    }
                    if (scheduleVH.priceTV.getVisibility() == View.GONE) {
                        scheduleVH.priceTV.setVisibility(View.VISIBLE);
                    }
                    scheduleVH.carpoolInfoBT.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Bundle carpoolData = new Bundle();
                            carpoolData.putSerializable("carpooldata", tempCarpoolInfoList.get(position));
                            carpoolData.putInt("preavtivity", ConstantVal.CARPOOLLIST_CARPOOLDETAIL);
                            Intent carpoolDetailIntent = new Intent(CarpoolListActivity.this, CarpoolDetailActivity.class);
                            carpoolDetailIntent.putExtras(carpoolData);
                            startActivity(carpoolDetailIntent);
                        }
                    });
                }
                return convertView;
            }
        };
        final ListView detailInfoLV = (ListView) findViewById(R.id.carpoollist);
        detailInfoLV.setAdapter(carpoolListAdapter);

        SeekBar ratingFilter = (SeekBar) findViewById(R.id.ratingfileter);
        if (tempCarpoolInfoList == null) {
            ratingFilter.setVisibility(View.GONE);
        }
        else {
            ratingFilter.setVisibility(View.VISIBLE);
        }
        ratingFilter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                tempCarpoolInfoList.clear();
                for (int i = 0; i < carpoolInfoList.size(); i++) {
                    float rating = carpoolInfoList.get(i).getDriRating();
                    if (rating >= progress || (progress == 0 && rating < 0)) {
                        tempCarpoolInfoList.add(carpoolInfoList.get(i));
                    }
                }
                carpoolListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
