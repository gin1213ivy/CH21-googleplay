package tw.tcnr110a.cloud21a;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Q0401 extends AppCompatActivity {

    private LinearLayout li01;
    private TextView mTxtResult,mDesc,t_count,u_loading;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout laySwipe;
    private String ul="https://data.coa.gov.tw/Service/OpenData/Tarivariety.aspx";
    private ArrayList<Map<String, Object>> mList;
    private int total,t_total;
    private int nowposition;
    private Button mBtnBrowseWWW;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //-----------------------------------------
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        //-----------------------------------------
        super.onCreate(savedInstanceState);
        setContentView(R.layout.q0401);
        setupViewComponent();
    }

    private void setupViewComponent() {
        Intent intent=this.getIntent();
        String mode_title = intent.getStringExtra("class_title");
        this.setTitle(mode_title);
        //--------------------------------------------------------------
        li01 = (LinearLayout) findViewById(R.id.li01);
        li01.setVisibility(View.GONE);
        mTxtResult = (TextView)findViewById(R.id.q0401_name);
        mDesc =  (TextView)findViewById(R.id.q0401_descr);
        mBtnBrowseWWW = (Button) findViewById(R.id.q0401_b001);

        mDesc.setMovementMethod(ScrollingMovementMethod.getInstance());
        mDesc.scrollTo(0, 0);//textview 回頂端

        recyclerView =(RecyclerView) findViewById(R.id.recyclerView);
        t_count = (TextView)findViewById(R.id.count);

//        b001=(Button)findViewById(R.id.q0401_b001);//前往網站
//        b001.setOnClickListener(b001On);

        //--------------------RecyclerView設定 上下滑動------------------
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                li01.setVisibility(View.GONE);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        //--------------設定下載中-----------
        u_loading = (TextView)findViewById(R.id.u_loading);
        u_loading.setVisibility(View.GONE);
        //-------------------------------------
        laySwipe = (SwipeRefreshLayout)findViewById(R.id.laySwipe);
        laySwipe.setOnRefreshListener(onSwipeToRefresh);
        laySwipe.setSize(SwipeRefreshLayout.LARGE);
        // 設置下拉多少距離之後開始刷新數據
        laySwipe.setDistanceToTriggerSync(200);
        // 設置進度條背景顏色
        laySwipe.setProgressBackgroundColorSchemeColor(getColor(android.R.color.background_light));
        // 設置刷新動畫的顏色，可以設置1或者更多
        laySwipe.setColorSchemeResources(
                android.R.color.holo_red_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_dark,
                android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_purple,
                android.R.color.holo_orange_dark);

/*        setProgressViewOffset : 設置進度圓圈的偏移量。
        第一個參數表示進度圈是否縮放，
        第二個參數表示進度圈開始出現時距頂端的偏移，
        第三個參數表示進度圈拉到最大時距頂端的偏移。*/
        laySwipe.setProgressViewOffset(true, 0, 50);
//=====================
        onSwipeToRefresh.onRefresh();  //開始轉圈下載資料
        //-------------------------
    }
    private final SwipeRefreshLayout.OnRefreshListener onSwipeToRefresh = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            //-------------------------------------設置對話盒
            mTxtResult.setText("");
            MyAlertDialog myAltDlg = new MyAlertDialog(Q0401.this);
            myAltDlg.setTitle(getString(R.string.q0401_dialog_title));
            myAltDlg.setMessage(getString(R.string.q0401_dialog_t001) + getString(R.string.q0401_dialog_b001));
            myAltDlg.setIcon(android.R.drawable.ic_menu_info_details);
            myAltDlg.setCancelable(false);
            myAltDlg.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.q0401_dialog_positive), altDlgOnClkPosiBtnLis);
            myAltDlg.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.q0401_dialog_neutral), altDlgOnClkNeutBtnLis);
            myAltDlg.show();
            //------------------------------------
        }
    };
    private DialogInterface.OnClickListener altDlgOnClkPosiBtnLis = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //-----------------開始執行下載----------------
            laySwipe.setRefreshing(true);
            u_loading.setVisibility(View.VISIBLE);
            mTxtResult.setText(getString(R.string.q0401_name) + "");
            mDesc.setText("");
            mDesc.scrollTo(0, 0);//textview 回頂端
            //------------------------------------------------
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //=================================
                    setDatatolist();
                    //=================================
                    //----------SwipeLayout 結束 --------
                    //可改放到最終位置 u_importopendata()
                    u_loading.setVisibility(View.GONE);
                    laySwipe.setRefreshing(false);
                    Toast.makeText(getApplicationContext(), getString(R.string.q0401_loadover), Toast.LENGTH_SHORT).show();
                }
            }, 1000);//5秒
        }
    };
    private DialogInterface.OnClickListener altDlgOnClkNeutBtnLis = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //對話盒監聽"取消"
            u_loading.setVisibility(View.GONE);
            laySwipe.setRefreshing(false);
        }
    };
    private void setDatatolist(){//----------放JSON 到 RecyclerView
        //==================================
        u_importopendata();  //-----------下載Opendata
        //==================================
        //設定Adapter
        final ArrayList<Q0401_Post> mData = new ArrayList<>();
        for (Map<String, Object> m : mList) {
            if (m != null) {
                String title = m.get("title").toString().trim();//------------------品種名稱
                String description = m.get("description").toString().trim();//------簡述
                String link = m.get("link").toString().trim(); //-------------描述
//************************************************************
                mData.add(new Q0401_Post(title, description, link));
//************************************************************
            } else {
                return;
            }
        }


        ArrayList<Q0401_Post> mdata;
        Q0401_RecyclerAdapter adapter = new Q0401_RecyclerAdapter(this, mData);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
// ************************************
        adapter.setOnItemClickListener(new Q0401_RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                li01.setVisibility(View.VISIBLE);
//                Toast.makeText(M2205.this, "onclick" + mData.get(position).hotelName.toString(), Toast.LENGTH_SHORT).show();
                mTxtResult.setText(getString(R.string.title) + mData.get(position).description);
                mDesc.setText(mData.get(position).title);
                mDesc.scrollTo(0, 0); //textview 回頂端
                nowposition = position;
                t_count.setText(getString(R.string.q0401_ncount) + total + "/" + t_total + "   (" + (nowposition + 1) + ")");

            }
        });
//********************************* ****
        recyclerView.setAdapter(adapter);
    }

    private void u_importopendata(){//-----------下載Opendata
        try {
            //-------------------------------
            String Task_opendata  = new TransTask().execute(ul).get();
            //-------解析 json   帶有多層結構---------------------------
            mList = new ArrayList<Map<String, Object>>();

            JSONArray info = new JSONArray(Task_opendata);
            total = 0;
            t_total = info.length(); //總筆數
            //------JSON 排序----------------------------------------
            info = sortJsonArray(info);
            total = info.length(); //有效筆數
            t_count.setText(getString(R.string.q0401_ncount) + total + "/" + t_total);
            //-----開始逐筆轉換-----
            total = info.length();
            t_count.setText(getString(R.string.q0401_ncount) + total);


            for (int i = 0; i < info.length(); i++) {
                Map<String, Object> item = new HashMap<String, Object>();
                String title = info.getJSONObject(i).getString("title");//-------------------品種名稱
                String Description = info.getJSONObject(i).getString("description");//-------簡述
                String link = info.getJSONObject(i).getString("link");//---------------描述

                //-------key+變數------------------------
                item.put("title", title);
                item.put("description", Description);
                item.put("link", link);

                //-------------------------------
                mList.add(item);
            }
            t_count.setText(getString(R.string.q0401_ncount) + total + "/" + t_total);
            //--------------------------------------------------------------------------
        }catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

//----------SwipeLayout 結束 --------
    }

    private JSONArray sortJsonArray(JSONArray jsonArray) {//county自訂義的排序method
        //County自定義的排序Method
        final ArrayList<JSONObject> json = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {  //將資料存入ArrayList json中
            try {
                if(//排除json資料中沒有照片、沒有郵遞區號的部分
                        jsonArray.getJSONObject(i).getString("title").trim().length()>0 &&
                                //!jsonArray.getJSONObject(i).getString("PCode").trim().substring(0,1).equals("9")&&
                                jsonArray.getJSONObject(i).getString("link").trim().length()>0
                                &&!jsonArray.getJSONObject(i).getString("link").trim().trim().equals("null")

                )

                {
                    json.add(jsonArray.getJSONObject(i));
                }

                //-----------資料全部抓取-----------
                //json.add(jsonArray.getJSONObject(i));
            } catch (JSONException jsone) {
                jsone.printStackTrace();
            }
        }
        //---------------------------------------------------------------
        Collections.sort(json, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject jsonOb1, JSONObject jsonOb2) {
                // 用多重key 排序
                String lidZipcode = "", ridZipcode = "";
//                String lidStatus="",ridStatus="";
//                String lidPM25="",ridPM25="";
                try {
                    lidZipcode = jsonOb1.getString("title");
                    ridZipcode = jsonOb2.getString("title");

                } catch (JSONException jsone) {
                    jsone.printStackTrace();
                }
//                return lidCounty.compareTo(ridCounty)+lidStatus.compareTo(ridStatus);
                return lidZipcode.compareTo(ridZipcode);
            }
        });
        return new JSONArray(json);//回傳排序縣市後的array
    }
    //-------------------------------------------生命週期------------------------------
    @Override
    public void onBackPressed() {
        //禁用返回鍵
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.finish();
    }

    //------------------------------------------Menu--------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.q0401_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        li01.setVisibility(View.GONE);
        switch (item.getItemId()) {
            case R.id.action_settings:
                finish();
                break;
            case R.id.menu_top:
                nowposition = 0; // 第一筆資料
                recyclerView.scrollToPosition(nowposition); // 跳到第N筆資料
                t_count.setText(getString(R.string.q0401_ncount) + total + "/" + t_total + "   (" + (nowposition + 1) + ")");
                break;

            case R.id.menu_next:
                nowposition = nowposition + 10; // N+100筆資料
                if (nowposition > total - 1) {
                    nowposition = total - 1;
                }
                recyclerView.scrollToPosition(nowposition);
                t_count.setText(getString(R.string.q0401_ncount) + total + "/" + t_total + "   (" + (nowposition + 1) + ")");
                break;

            case R.id.menu_back:
                nowposition = nowposition - 10; // N-100筆資料
                if (nowposition < 0) {
                    nowposition = 0;
                }
                recyclerView.scrollToPosition(nowposition);
                t_count.setText(getString(R.string.q0401_ncount) + total + "/" + t_total + "   (" + (nowposition + 1) + ")");
                break;

            case R.id.menu_end:
                nowposition = total - 1; // 跳到最後一筆資料
                recyclerView.scrollToPosition(nowposition);
                t_count.setText(getString(R.string.q0401_ncount) + total + "/" + t_total + "   (" + (nowposition + 1) + ")");
                break;

            case R.id.menu_load:
                onSwipeToRefresh.onRefresh();  //開始轉圈下載資料
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //*********************************************************************
    private class TransTask extends AsyncTask<String, Void, String> {
        String ans;

        @Override
        protected String doInBackground(String... params) {
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(params[0]);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                String line = in.readLine();
                while (line != null) {
                    sb.append(line);
                    line = in.readLine();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ans = sb.toString();
            //------------
            return ans;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            parseJson(s);
        }

        private void parseJson(String s) {
        }

    }
    //----------------------------------------------
    private final View.OnClickListener b001On =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            u_importopendata();  //下載Opendata
            final ArrayList<Q0401_Post> mBtndata = new ArrayList<>();
            for (Map<String, Object> b : mList) {
                if (b != null) {
                    String title = b.get("title").toString().trim(); //住址
                    String Description = b.get("description").toString().trim(); //描述
//                    String link = b.get("link").toString().trim(); //描述

                    //************************************************************
//                    mBtndata.add(new Q0401_Post(title,Description, link));
                    // mData.add(new Post(FarmNm_CH, Picture1, Description, Zipcode, Px, Py));
                    //************************************************************
                } else {
                    return;
                }

            }

//        switch (v.getId()){
//        case R.id.q0401_b001:
//        String btnweb=mBtndata.get(nowposition).link;
//        // web=mData.get(position).FarmNm_CH
//        uri= Uri.parse("https://www.tari.gov.tw/");
//        uri= Uri.parse(btnweb);
//        it=new Intent(Intent.ACTION_VIEW,uri);
//        startActivity(it);
//        break;
//        }
//            switch (v.getId()) {
//
//                // 1.開網頁 (政府資料開放平台)
//                case R.id.q0401_b001:
//                    uri = Uri.parse("https://data.gov.tw/");
//                    it = new Intent(Intent.ACTION_VIEW, uri);
//                    startActivity(it);
//                    break;

            }

        };

    }
