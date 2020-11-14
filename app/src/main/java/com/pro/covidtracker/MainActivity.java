package com.pro.covidtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<CountryItem> countryItemArrayList;
    private CountryAdapter countryAdapter;
    private ArrayList<CoronaItem> coronaItemArrayList;
    private RequestQueue requestQueue;
    Spinner spinner_state,spinner;
    ProgressBar progressBar;
    Timer timer;
    SharedPref sharedPreferences;
    SharedPreferences LastSelect;
    int count=0;
    SharedPreferences.Editor editor;
    static boolean b = true;
    private TextView dailyDeath, dailyConfirm, dailyRecover, dateHeaders, totalDeath, totalConfirmed, totalRecovered;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = new SharedPref(this);
        if(!sharedPreferences.loadNightModeState()){

            b=true;
            setTheme(R.style.AppTheme);
        }
        else{
            b=false;
            setTheme(R.style.DarkAppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      //  progressBar = findViewById(R.id.progressbar);
        dailyConfirm=findViewById(R.id.dailyConfirmed);
        dailyDeath=findViewById(R.id.dailyDeaths);
        dailyRecover=findViewById(R.id.dailyRecovered);
        dateHeaders=findViewById(R.id.dateHeader);
        totalRecovered=findViewById(R.id.totalRecovered);
        totalConfirmed=findViewById(R.id.totalConfirmed);
        totalDeath=findViewById(R.id.totalDeaths);
        spinner = findViewById(R.id.spinner_countries);
       // spinner_state = findViewById(R.id.spinner_state);

        recyclerView=findViewById(R.id.myRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        coronaItemArrayList = new ArrayList<>();

        requestQueue = Volley.newRequestQueue(this);

        initList();

        LastSelect = getSharedPreferences("Last Setting", Context.MODE_PRIVATE);
        editor=LastSelect.edit();
       // prog();


        final int LastClick = LastSelect.getInt("LastClick",0);
        countryAdapter = new CountryAdapter(this, countryItemArrayList);
        spinner.setAdapter(countryAdapter);
        spinner.setSelection(LastClick);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               CountryItem clickedItem = (CountryItem) adapterView.getItemAtPosition(i);

               editor.putInt("LastClick",i).commit();

               if(i==0){


                   jsonParse();
               }
               else if(i==1){

                   jsonParseCan();
                   jsonParseCanHead();
               }
           }

           @Override
           public void onNothingSelected(AdapterView<?> adapterView) {

           }
       });
    }

    private void prog() {
       timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                count++;
                progressBar.setProgress(count);
                if(count==100){
                    timer.cancel();
                }
            }
        };
        timer.schedule(timerTask,0,100);
    }

    private void initList() {

        countryItemArrayList = new ArrayList<>();
        countryItemArrayList.add(new CountryItem(R.drawable.india));
        countryItemArrayList.add(new CountryItem(R.drawable.canada));
    }

    private void jsonParse(){

        String url = "https://api.covid19india.org/data.json";
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{

                    coronaItemArrayList.clear();
                    JSONArray todayAndTotalDataArray = response.getJSONArray("statewise");
                    JSONObject todayAndTotalDataJsonObject= todayAndTotalDataArray.getJSONObject(0);

                    String dailyConfirmed = todayAndTotalDataJsonObject.getString("deltaconfirmed");
                    String dailyDeaths = todayAndTotalDataJsonObject.getString("deltadeaths");
                    String dailyRec = todayAndTotalDataJsonObject.getString("deltarecovered");
                    String dataHeader = todayAndTotalDataJsonObject.getString("lastupdatedtime").substring(0,5);

                    dataHeader= getFormattedDate(dataHeader);

                    dailyConfirm.setText(String.format("+%s", dailyConfirmed));
                    dailyDeath.setText(String.format("+%s", dailyDeaths));
                    dailyRecover.setText(String.format("+%s", dailyRec));
                    dateHeaders.setText(dataHeader);

                    String totalDeathsFetched = todayAndTotalDataJsonObject.getString("deaths");
                    String totalRecoveredFetched = todayAndTotalDataJsonObject.getString("recovered");
                    String totalConfirmedFetched = todayAndTotalDataJsonObject.getString("confirmed");

                    totalConfirmed.setText(totalConfirmedFetched);
                    totalDeath.setText(totalDeathsFetched);
                    totalRecovered.setText(totalRecoveredFetched);

                    for(int i=1;i<todayAndTotalDataArray.length();i++){

                        JSONObject stateWiseArrayJsonObject = todayAndTotalDataArray.getJSONObject(i);

                        String active = stateWiseArrayJsonObject.getString("active");
                        String death = stateWiseArrayJsonObject.getString("deaths");
                        String recovered = stateWiseArrayJsonObject.getString("recovered");
                        String state = stateWiseArrayJsonObject.getString("state");
                        String confirmed = stateWiseArrayJsonObject.getString("confirmed");
                        String lastUpdated = stateWiseArrayJsonObject.getString("lastupdatedtime");


                        String todayActive = stateWiseArrayJsonObject.getString("deltaconfirmed");
                        String todayDeath = stateWiseArrayJsonObject.getString("deltadeaths");
                        String todayRecovered = stateWiseArrayJsonObject.getString("deltarecovered");

                        CoronaItem coronaItem = new CoronaItem(state, death, active, recovered, confirmed, lastUpdated, todayDeath, todayRecovered, todayActive);

                        coronaItemArrayList.add(coronaItem);
                        //Collections.sort(coronaItemArrayList,coronaItem.getState());
                    }

                    recyclerViewAdapter= new RecyclerViewAdapter(MainActivity.this, coronaItemArrayList);
                    recyclerView.setAdapter(recyclerViewAdapter);
                }
                catch(JSONException ex){
                    ex.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();
            }
        });

        requestQueue.add(request);
    }

    private void jsonParseCanHead(){

        String url = "https://api.covid19tracker.ca/summary";
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONArray todayAndTotalDataArray = response.getJSONArray("data");
                    JSONObject todayAndTotalDataJsonObject = todayAndTotalDataArray.getJSONObject(0);

                    String dailyConfirmed = todayAndTotalDataJsonObject.getString("change_cases");
                    String dailyDeaths = todayAndTotalDataJsonObject.getString("change_fatalities");
                    String dailyRec = todayAndTotalDataJsonObject.getString("change_recoveries");
                    String dataHeader = todayAndTotalDataJsonObject.getString("latest_date").substring(5, 10);

                    String finals = dataHeader.substring(3,5)+"-"+dataHeader.substring(0,2);



                    dataHeader =getFormattedDate(finals);

                    dailyConfirm.setText(String.format("+%s", dailyConfirmed));
                    dailyDeath.setText(String.format("+%s", dailyDeaths));
                    dailyRecover.setText(String.format("+%s", dailyRec));
                    dateHeaders.setText(dataHeader);

                    String totalDeathsFetched = todayAndTotalDataJsonObject.getString("total_fatalities");
                    String totalRecoveredFetched = todayAndTotalDataJsonObject.getString("total_recoveries");
                    String totalConfirmedFetched = todayAndTotalDataJsonObject.getString("total_cases");

                    totalConfirmed.setText(totalConfirmedFetched);
                    totalDeath.setText(totalDeathsFetched);
                    totalRecovered.setText(totalRecoveredFetched);
                }
                catch(JSONException ex){
                    ex.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();
            }
        });

        requestQueue.add(request);
    }

    private void jsonParseCan(){

        String url = "https://api.opencovid.ca/summary?loc=prov";


        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{

                    coronaItemArrayList.clear();
                    JSONArray todayAndTotalDataArray = response.getJSONArray("summary");

                    for(int i=0;i<todayAndTotalDataArray.length();i++){

                        JSONObject stateWiseArrayJsonObject = todayAndTotalDataArray.getJSONObject(i);

                        String active = stateWiseArrayJsonObject.getString("active_cases");
                        String death = stateWiseArrayJsonObject.getString("cumulative_deaths");
                        String recovered = stateWiseArrayJsonObject.getString("cumulative_recovered");
                        String state = stateWiseArrayJsonObject.getString("province");
                        String confirmed = stateWiseArrayJsonObject.getString("cumulative_cases");
                        String lastUpdated = stateWiseArrayJsonObject.getString("date");

                        String dataHeader = getFormattedDate(lastUpdated);
                        String todayActive = stateWiseArrayJsonObject.getString("active_cases_change");
                        String todayDeath = stateWiseArrayJsonObject.getString("deaths");
                        String todayRecovered = stateWiseArrayJsonObject.getString("recovered");

                        CoronaItem coronaItem = new CoronaItem(state, death, active, recovered, confirmed, dataHeader, todayDeath, todayRecovered, todayActive);

                        coronaItemArrayList.add(coronaItem);
                        //Collections.sort(coronaItemArrayList,coronaItem.getState());
                    }

                    recyclerViewAdapter= new RecyclerViewAdapter(MainActivity.this, coronaItemArrayList);
                    recyclerView.setAdapter(recyclerViewAdapter);
                }
                catch(JSONException ex){
                    ex.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();
            }
        });

        requestQueue.add(request);
    }

    private String getFormattedDate(String dateHeader){

        switch(dateHeader.substring(3,5)){

            case "01":
                return dateHeader.substring(0,2)+" Jan";
            case "02":
                return dateHeader.substring(0,2)+" Feb";
            case "03":
                return dateHeader.substring(0,2)+" Mar";
            case "04":
                return dateHeader.substring(0,2)+" Apr";
            case "05":
                return dateHeader.substring(0,2)+" May";
            case "06":
                return dateHeader.substring(0,2)+" Jun";
            case "07":
                return dateHeader.substring(0,2)+" Jul";
            case "08":
                return dateHeader.substring(0,2)+" Aug";
            case "09":
                return dateHeader.substring(0,2)+" Sep";
            case "10":
                return dateHeader.substring(0,2)+" Oct";
            case "11":
                return dateHeader.substring(0,2)+" Nov";
            case "12":
                return dateHeader.substring(0,2)+" Dec";

            default:
                return null;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){


            case R.id.dark_mode:

                if(b){
                    sharedPreferences.setNightModeState(true);

                    Anim();
                   // recreate();
                    Toast.makeText(MainActivity.this, "Night Mode", Toast.LENGTH_SHORT).show();
                }
                else{
                    sharedPreferences.setNightModeState(false);

                    Anim();
                    //recreate();
                    Toast.makeText(MainActivity.this, "Light Mode", Toast.LENGTH_SHORT).show();
                }
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void Anim(){

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(intent);
                finish();
            }
        },250);
    }

}