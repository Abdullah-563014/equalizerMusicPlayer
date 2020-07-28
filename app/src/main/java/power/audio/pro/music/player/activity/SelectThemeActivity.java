package power.audio.pro.music.player.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import power.audio.pro.music.player.R;
import power.audio.pro.music.player.adapter.ImageGridAdapter;
import power.audio.pro.music.player.utils.SpUtils;
import power.audio.pro.music.player.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

public class SelectThemeActivity extends AppCompatActivity {

    private RecyclerView rv1;
    private RecyclerView rv2;
    private RecyclerView rv3;

    private RadioGroup radioThemeGroup;
    private RadioButton radioThemeButton;
    private TextView btnSaveTheme;

    private static final String THEME1 = "1"; // Color;
    private static final String THEME2 = "2"; // Human;
    private static final String THEME3 = "3"; // Nature;
    RelativeLayout rl2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_theme);
        Log.d("LuatND", "Save the theme onCreate");
        findViewById(R.id.layoutClose).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });
        rl2 = (RelativeLayout)findViewById(R.id.RelativeLayout1);
        Log.d("LuatND", "onCreate rl2: " + rl2);

        findViewById(R.id.select_theme1).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                LinearLayout linearLayout = (LinearLayout)findViewById(R.id.themesMainView);
                ThemeUtils.showTheme(linearLayout, THEME1, 1);
            }
        });

        findViewById(R.id.select_theme2).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                LinearLayout linearLayout = (LinearLayout)findViewById(R.id.themesMainView);
                ThemeUtils.showTheme(linearLayout, THEME2, 1);
            }
        });

        findViewById(R.id.select_theme3).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                LinearLayout linearLayout = (LinearLayout)findViewById(R.id.themesMainView);
                ThemeUtils.showTheme(linearLayout, THEME3, 1);
            }
        });

        themeSelection();
        addListenerOnButton();

        selectedCurrentTheme();

    }

    @Override
    protected void onResume() {
        super.onResume();

        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.themesMainView);
        ThemeUtils.showTheme(linearLayout);
    }

    private void selectedCurrentTheme() {
        Log.d("LuatND", "selected_theme: " + SpUtils.getString("selected_theme"));
        switch (SpUtils.getString("selected_theme")) {
            case THEME1:
                RadioButton radioButton1 = (RadioButton) findViewById(R.id.select_theme1);
                radioButton1.setChecked(true);
//                ThemeUtils.showTheme(linearLayout, THEME1, 2);
                break;
            case THEME2:
                RadioButton radioButton2 = (RadioButton) findViewById(R.id.select_theme2);
                radioButton2.setChecked(true);
//                ThemeUtils.showTheme(linearLayout, THEME2, 2);
                break;
            case THEME3:
                RadioButton radioButton3 = (RadioButton) findViewById(R.id.select_theme3);
                radioButton3.setChecked(true);
//                ThemeUtils.showTheme(linearLayout, THEME3, 2);
                break;
            default:
                RadioButton radioButton6 = (RadioButton) findViewById(R.id.select_theme1);
                radioButton6.setChecked(true);
//                ThemeUtils.showTheme(linearLayout, THEME1, 2);
                break;
        }

    }

    public void addListenerOnButton() {

        radioThemeGroup = (RadioGroup) findViewById(R.id.radioGroup1);
        btnSaveTheme = (TextView ) findViewById(R.id.layoutOk);

        btnSaveTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get selected radio button from radioGroup
                int selectedId = radioThemeGroup.getCheckedRadioButtonId();
                // find the radiobutton by returned id
                radioThemeButton = (RadioButton) findViewById(selectedId);

                switch (selectedId) {
                    case R.id.select_theme1:
                        if (rl2 != null) {
                            rl2.setBackgroundResource(R.drawable.color_1);
                        }
                        SpUtils.putString("selected_theme", THEME1);
                        Log.d("LuatND", "Save the theme 1: " + THEME1);
                        break;
                    case R.id.select_theme2:
                        if (rl2 != null) {
                            rl2.setBackgroundResource(R.drawable.human_1);
                        }
                        SpUtils.putString("selected_theme", THEME2);
                        Log.d("LuatND", "Save the theme 2: " + THEME2);
                        break;
                    case R.id.select_theme3:
                        if (rl2 != null) {
                            rl2.setBackgroundResource(R.drawable.nature_1);
                        }
                        SpUtils.putString("selected_theme", THEME3);
                        Log.d("LuatND", "Save the theme 3: " + THEME3);
                        break;
                    default:
                        if (rl2 != null) {
                            rl2.setBackgroundResource(R.drawable.human_1);
                        }
                        SpUtils.putString("selected_theme", THEME1);
                        break;
                }
                onBackPressed();
            }
        });

    }

    private void themeSelection() {
        rv1 = findViewById(R.id.rv_theme1);
        rv2 = findViewById(R.id.rv_theme2);
        rv3 = findViewById(R.id.rv_theme3);

        StaggeredGridLayoutManager sglm1 = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
        StaggeredGridLayoutManager sglm2 = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
        StaggeredGridLayoutManager sglm3 = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
        rv1.setLayoutManager(sglm1);
        rv2.setLayoutManager(sglm2);
        rv3.setLayoutManager(sglm3);

        List<Integer> imageList1 = new ArrayList<Integer>();
        List<Integer> imageList2 = new ArrayList<Integer>();
        List<Integer> imageList3 = new ArrayList<Integer>();
        imageList1.add(R.drawable.color_1);
        imageList1.add(R.drawable.color_2);
        imageList1.add(R.drawable.color_3);
        imageList1.add(R.drawable.color_4);
        imageList1.add(R.drawable.color_5);
        imageList1.add(R.drawable.color_6);
        imageList1.add(R.drawable.color_7);
        imageList2.add(R.drawable.human_1);
        imageList2.add(R.drawable.human_2);
        imageList2.add(R.drawable.human_3);
        imageList2.add(R.drawable.human_4);
        imageList2.add(R.drawable.human_5);
        imageList2.add(R.drawable.human_6);
        imageList2.add(R.drawable.human_7);
        imageList3.add(R.drawable.nature_1);
        imageList3.add(R.drawable.nature_2);
        imageList3.add(R.drawable.nature_3);
        imageList3.add(R.drawable.nature_4);
        imageList3.add(R.drawable.nature_5);
        imageList3.add(R.drawable.nature_6);
        imageList3.add(R.drawable.nature_7);


        ImageGridAdapter iga1 = new ImageGridAdapter(SelectThemeActivity.this, imageList1);
        ImageGridAdapter iga2 = new ImageGridAdapter(SelectThemeActivity.this, imageList2);
        ImageGridAdapter iga3 = new ImageGridAdapter(SelectThemeActivity.this, imageList3);
        rv1.setAdapter(iga1);
        rv2.setAdapter(iga2);
        rv3.setAdapter(iga3);
    }
}
