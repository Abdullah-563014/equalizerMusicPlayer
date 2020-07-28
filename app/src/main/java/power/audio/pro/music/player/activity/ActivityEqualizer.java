package power.audio.pro.music.player.activity;

import android.annotation.SuppressLint;
import android.media.audiofx.PresetReverb;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import power.audio.pro.music.player.CustomView.VerticalSeekBar;
import power.audio.pro.music.player.MainApplication;
import power.audio.pro.music.player.R;
import power.audio.pro.music.player.UIElementHelper.MyDialogBuilder;
import power.audio.pro.music.player.equalizer.EqualizerSetting;
import power.audio.pro.music.player.utils.ThemeUtils;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ActivityEqualizer extends AppCompatActivity {

    //views
    @BindView(R.id.equalizerScrollView)
    ScrollView mScrollView;

    @BindView(R.id.equalizerLinearLayout)
    View equalizerView;

    // 50Hz pro controls.
    @BindView(R.id.equalizer50Hz)
    VerticalSeekBar equalizer50HzSeekBar;
    @BindView(R.id.text50HzGain)
    TextView text50HzGainTextView;
    @BindView(R.id.text50Hz)
    TextView text50Hz;

    // 130Hz pro controls.
    @BindView(R.id.equalizer130Hz)
    VerticalSeekBar equalizer130HzSeekBar;
    @BindView(R.id.text130HzGain)
    TextView text130HzGainTextView;
    @BindView(R.id.text130Hz)
    TextView text130Hz;

    // 320Hz pro controls.
    @BindView(R.id.equalizer320Hz)
    VerticalSeekBar equalizer320HzSeekBar;
    @BindView(R.id.text320HzGain)
    TextView text320HzGainTextView;
    @BindView(R.id.text320Hz)
    TextView text320Hz;

    // 800 Hz pro controls.
    @BindView(R.id.equalizer800Hz)
    VerticalSeekBar equalizer800HzSeekBar;
    @BindView(R.id.text800HzGain)
    TextView text800HzGainTextView;
    @BindView(R.id.text800Hz)
    TextView text800Hz;

    // 2 kHz pro controls.
    @BindView(R.id.equalizer2kHz)
    VerticalSeekBar equalizer2kHzSeekBar;
    @BindView(R.id.text2kHzGain)
    TextView text2kHzGainTextView;
    @BindView(R.id.text2kHz)
    TextView text2kHz;

    // 5 kHz pro controls.
    @BindView(R.id.equalizer5kHz)
    VerticalSeekBar equalizer5kHzSeekBar;
    @BindView(R.id.text5kHzGain)
    TextView text5kHzGainTextView;
    @BindView(R.id.text5kHz)
    TextView text5kHz;

    // 12.5 kHz pro controls.
    @BindView(R.id.equalizer12_5kHz)
    VerticalSeekBar equalizer12_5kHzSeekBar;
    @BindView(R.id.text12_5kHzGain)
    TextView text12_5kHzGainTextView;
    @BindView(R.id.text12_5kHz)
    TextView text12_5kHz;

    // Equalizer preset controls.
//    @BindView(R.id.loadPresetButton)
//    RelativeLayout loadPresetButton;
    @BindView(R.id.saveAsPresetButton)
    RelativeLayout saveAsPresetButton;
    @BindView(R.id.resetAllButton)
    RelativeLayout resetAllButton;
    //    @BindView(R.id.load_preset_text)
//    TextView loadPresetText;
    @BindView(R.id.save_as_preset_text)
    TextView savePresetText;
    @BindView(R.id.reset_all_text)
    TextView resetAllText;

    // Temp variables that hold the pro's settings.
    private int fiftyHertzLevel = 16;
    private int oneThirtyHertzLevel = 16;
    private int threeTwentyHertzLevel = 16;
    private int eightHundredHertzLevel = 16;
    private int twoKilohertzLevel = 16;
    private int fiveKilohertzLevel = 16;
    private int twelvePointFiveKilohertzLevel = 16;

    // Temp variables that hold audio fx settings.
    private int virtualizerLevel;
    private int bassBoostLevel;
    private int enhancementLevel;
    private int reverbSetting;
    private boolean customEqualizer = false;
    //Audio FX elements.
    @BindView(R.id.virtualizer_seekbar)
    SeekBar virtualizerSeekBar;
    @BindView(R.id.bass_boost_seekbar)
    SeekBar bassBoostSeekBar;
    @BindView(R.id.enhancer_seekbar)
    SeekBar enhanceSeekBar;
    @BindView(R.id.reverb_spinner)
    Spinner reverbSpinner;
    @BindView(R.id.preset_spinner)
    Spinner presetSpinner;
    @BindView(R.id.virtualizer_title_text)
    TextView virtualizerTitle;
    @BindView(R.id.bass_boost_title_text)
    TextView bassBoostTitle;
    @BindView(R.id.reverb_title_text)
    TextView reverbTitle;

    @BindView(R.id.enhancer_title_text)
    TextView enhancerTitleText;

    @BindView(R.id.tv2)
    TextView enhancertv2;

    @BindView(R.id.tv3)
    TextView enhancertv3;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_equalizer);
        ButterKnife.bind(this);

        //action bar
        Toolbar toolbar = findViewById(R.id.toolbar_);
        toolbar.setTitle(R.string.equalizer_titl);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //Init reverb presets.
        ArrayList<String> reverbPresets = new ArrayList<String>();
        reverbPresets.add(getString(R.string.preset_none));
        reverbPresets.add(getString(R.string.preset_large_hall));
        reverbPresets.add(getString(R.string.preset_large_room));
        reverbPresets.add(getString(R.string.preset_medium_hall));
        reverbPresets.add(getString(R.string.preset_medium_room));
        reverbPresets.add(getString(R.string.preset_small_room));
        reverbPresets.add(getString(R.string.preset_plate));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, reverbPresets);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reverbSpinner.setAdapter(dataAdapter);

        //load data from db here
        try{
            String[] dataPreset = MainApplication.getService().getEqualizerHelper().getPresetList();
            ArrayList<String> dataPresets = new ArrayList<String>(Arrays.asList(dataPreset));
            dataPresets.add(0, getString(R.string.preset_custom));
            ArrayAdapter<String> presetAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dataPresets);
            presetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            presetSpinner.setAdapter(presetAdapter);
        }catch (Exception e) {
            e.printStackTrace();
        }


        //Set the max values for the seekbars.
        virtualizerSeekBar.setMax(1000);
        bassBoostSeekBar.setMax(1000);
        enhanceSeekBar.setMax(1000);

        resetAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reverbSpinner.setSelection(0, false);
                presetSpinner.setSelection(0, false);
                //Reset preset setting
                resetEQ();
                //Show a confirmation toast.
                Toast.makeText(getApplicationContext(), R.string.equ_reset_toast, Toast.LENGTH_SHORT).show();

            }

        });


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            enhancerTitleText.setVisibility(View.GONE);
            enhanceSeekBar.setVisibility(View.GONE);
            enhancertv2.setVisibility(View.GONE);
            enhancertv3.setVisibility(View.GONE);
        }else{
            enhanceSeekBar.setOnSeekBarChangeListener(enhanceListener);
        }

        saveAsPresetButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showSavePresetDialog();
            }
        });

        equalizer50HzSeekBar.setOnSeekBarChangeListener(equalizer50HzListener);
        equalizer130HzSeekBar.setOnSeekBarChangeListener(equalizer130HzListener);
        equalizer320HzSeekBar.setOnSeekBarChangeListener(equalizer320HzListener);
        equalizer800HzSeekBar.setOnSeekBarChangeListener(equalizer800HzListener);
        equalizer2kHzSeekBar.setOnSeekBarChangeListener(equalizer2kHzListener);
        equalizer5kHzSeekBar.setOnSeekBarChangeListener(equalizer5kHzListener);
        equalizer12_5kHzSeekBar.setOnSeekBarChangeListener(equalizer12_5kHzListener);

        virtualizerSeekBar.setOnSeekBarChangeListener(virtualizerListener);
        bassBoostSeekBar.setOnSeekBarChangeListener(bassBoostListener);
        reverbSpinner.setOnItemSelectedListener(reverbListener);
        presetSpinner.setOnItemSelectedListener(presetListener);

        try {
            new AsyncInitSlidersTask().execute(MainApplication.getService().getEqualizerHelper().getLastEquSetting());
        }catch (Exception e) {
            e.printStackTrace();
        }

        equalizer50HzSeekBar.setOnTouchListener(listener);
        equalizer130HzSeekBar.setOnTouchListener(listener);
        equalizer320HzSeekBar.setOnTouchListener(listener);
        equalizer800HzSeekBar.setOnTouchListener(listener);
        equalizer2kHzSeekBar.setOnTouchListener(listener);
        equalizer5kHzSeekBar.setOnTouchListener(listener);
        equalizer12_5kHzSeekBar.setOnTouchListener(listener);

    }

    protected void resetEQ(){
        //Reset all sliders to 0.
        equalizer50HzSeekBar.setProgressAndThumb(16);
        equalizer130HzSeekBar.setProgressAndThumb(16);
        equalizer320HzSeekBar.setProgressAndThumb(16);
        equalizer800HzSeekBar.setProgressAndThumb(16);
        equalizer2kHzSeekBar.setProgressAndThumb(16);
        equalizer5kHzSeekBar.setProgressAndThumb(16);
        equalizer12_5kHzSeekBar.setProgressAndThumb(16);
        virtualizerSeekBar.setProgress(0);
        bassBoostSeekBar.setProgress(0);
        enhanceSeekBar.setProgress(0);
        //Apply the new setings to the service.
        applyCurrentEQSettings();
    }

    View.OnTouchListener listener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                mScrollView.requestDisallowInterceptTouchEvent(true);
            }else if(event.getAction() ==MotionEvent.ACTION_UP) {
                mScrollView.requestDisallowInterceptTouchEvent(false);                }
            return false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        MainApplication.isAppVisible = true;
        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.activity_equalizer);
        ThemeUtils.showTheme(relativeLayout);
    }

    @Override
    protected void onPause() {
        EqualizerSetting equalizerSetting = getCurrentEquSetting();
        if(equalizerSetting != null)
            MainApplication.getService().getEqualizerHelper().storeLastEquSetting(equalizerSetting);
        MainApplication.isAppVisible = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @NonNull
    private EqualizerSetting getCurrentEquSetting() {
        EqualizerSetting equalizerSetting = new EqualizerSetting();
        equalizerSetting.setFiftyHertz(fiftyHertzLevel);
        equalizerSetting.setOneThirtyHertz(oneThirtyHertzLevel);
        equalizerSetting.setThreeTwentyHertz(threeTwentyHertzLevel);
        equalizerSetting.setEightHundredHertz(eightHundredHertzLevel);
        equalizerSetting.setTwoKilohertz(twoKilohertzLevel);
        equalizerSetting.setFiveKilohertz(fiveKilohertzLevel);
        equalizerSetting.setTwelvePointFiveKilohertz(twelvePointFiveKilohertzLevel);
        equalizerSetting.setVirtualizer(virtualizerLevel);
        equalizerSetting.setBassBoost(bassBoostLevel);
        equalizerSetting.setEnhancement(enhancementLevel);
        equalizerSetting.setReverb(reverbSetting);
        return equalizerSetting;
    }

    /**
     * 50 Hz pro seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer50HzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
            try {
                //Get the appropriate pro band.
                short sixtyHertzBand = MainApplication.getService().getEqualizerHelper().getEqualizer().getBand(50000);

                //Set the gain level text based on the slider position.
                if (seekBarLevel==16) {
                    text50HzGainTextView.setText("0 dB");
                    MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(sixtyHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {

                    if (seekBarLevel==0) {
                        text50HzGainTextView.setText("-" + "15 dB");
                        MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(sixtyHertzBand, (short) (-1500));
                    } else {
                        text50HzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
                        MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(sixtyHertzBand, (short) -((16-seekBarLevel)*100));
                    }

                } else if (seekBarLevel > 16) {
                    text50HzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
                    MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(sixtyHertzBand, (short) ((seekBarLevel-16)*100));
                }

                fiftyHertzLevel = seekBarLevel;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            presetSpinner.setSelection(0);
            customEqualizer = true;
            EqualizerSetting equalizerSetting = getCurrentEquSetting();
            MainApplication.getService().getEqualizerHelper().storeLastEquSetting(equalizerSetting);
        }

    };

    /**
     * 130 Hz pro seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer130HzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {

            try {
                //Get the appropriate pro band.
                short twoThirtyHertzBand = MainApplication.getService().getEqualizerHelper().getEqualizer().getBand(130000);

                //Set the gain level text based on the slider position.
                if (seekBarLevel==16) {
                    text130HzGainTextView.setText("0 dB");
                    MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(twoThirtyHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {

                    if (seekBarLevel==0) {
                        text130HzGainTextView.setText("-" + "15 dB");
                        MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(twoThirtyHertzBand, (short) (-1500));
                    } else {
                        text130HzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
                        MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(twoThirtyHertzBand, (short) -((16-seekBarLevel)*100));
                    }

                } else if (seekBarLevel > 16) {
                    text130HzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
                    MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(twoThirtyHertzBand, (short) ((seekBarLevel-16)*100));
                }

                oneThirtyHertzLevel = seekBarLevel;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub
            presetSpinner.setSelection(0);
            customEqualizer = true;
            EqualizerSetting equalizerSetting = getCurrentEquSetting();
            MainApplication.getService().getEqualizerHelper().storeLastEquSetting(equalizerSetting);
        }

    };

    /**
     * 320 Hz pro seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer320HzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {

            try {
                //Get the appropriate pro band.
                short nineTenHertzBand = MainApplication.getService().getEqualizerHelper().getEqualizer().getBand(320000);

                //Set the gain level text based on the slider position.
                if (seekBarLevel==16) {
                    text320HzGainTextView.setText("0 dB");
                    MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(nineTenHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {

                    if (seekBarLevel==0) {
                        text320HzGainTextView.setText("-" + "15 dB");
                        MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(nineTenHertzBand, (short) (-1500));
                    } else {
                        text320HzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
                        MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(nineTenHertzBand, (short) -((16-seekBarLevel)*100));
                    }

                } else if (seekBarLevel > 16) {
                    text320HzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
                    MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(nineTenHertzBand, (short) ((seekBarLevel-16)*100));
                }

                threeTwentyHertzLevel = seekBarLevel;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub
            presetSpinner.setSelection(0);
            customEqualizer = true;
            EqualizerSetting equalizerSetting = getCurrentEquSetting();
            MainApplication.getService().getEqualizerHelper().storeLastEquSetting(equalizerSetting);
        }

    };

    /**
     * 800 Hz pro seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer800HzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {

            try {
                //Get the appropriate pro band.
                short threeKiloHertzBand = MainApplication.getService().getEqualizerHelper().getEqualizer().getBand(800000);

                //Set the gain level text based on the slider position.
                if (seekBarLevel==16) {
                    text800HzGainTextView.setText("0 dB");
                    MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(threeKiloHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {

                    if (seekBarLevel==0) {
                        text800HzGainTextView.setText("-" + "15 dB");
                        MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(threeKiloHertzBand, (short) (-1500));
                    } else {
                        text800HzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
                        MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(threeKiloHertzBand, (short) -((16-seekBarLevel)*100));
                    }

                } else if (seekBarLevel > 16) {
                    text800HzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
                    MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(threeKiloHertzBand, (short) ((seekBarLevel-16)*100));
                }

                eightHundredHertzLevel = seekBarLevel;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub
            presetSpinner.setSelection(0);
            customEqualizer = true;
            EqualizerSetting equalizerSetting = getCurrentEquSetting();
            MainApplication.getService().getEqualizerHelper().storeLastEquSetting(equalizerSetting);
        }

    };

    /**
     * 2 kHz pro seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer2kHzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {

            try {
                //Get the appropriate pro band.
                short fourteenKiloHertzBand = MainApplication.getService().getEqualizerHelper().getEqualizer().getBand(2000000);

                //Set the gain level text based on the slider position.
                if (seekBarLevel==16) {
                    text2kHzGainTextView.setText("0 dB");
                    MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(fourteenKiloHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {

                    if (seekBarLevel==0) {
                        text2kHzGainTextView.setText("-" + "15 dB");
                        MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(fourteenKiloHertzBand, (short) (-1500));
                    } else {
                        text2kHzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
                        MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(fourteenKiloHertzBand, (short) -((16-seekBarLevel)*100));
                    }

                } else if (seekBarLevel > 16) {
                    text2kHzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
                    MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(fourteenKiloHertzBand, (short) ((seekBarLevel-16)*100));
                }

                twoKilohertzLevel = seekBarLevel;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub
            presetSpinner.setSelection(0);
            customEqualizer = true;
            EqualizerSetting equalizerSetting = getCurrentEquSetting();
            MainApplication.getService().getEqualizerHelper().storeLastEquSetting(equalizerSetting);

        }

    };

    /**
     * 5 kHz pro seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer5kHzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {

            try {
                //Get the appropriate pro band.
                short fiveKiloHertzBand = MainApplication.getService().getEqualizerHelper().getEqualizer().getBand(5000000);

                //Set the gain level text based on the slider position.
                if (seekBarLevel==16) {
                    text5kHzGainTextView.setText("0 dB");
                    MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(fiveKiloHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {

                    if (seekBarLevel==0) {
                        text5kHzGainTextView.setText("-" + "15 dB");
                        MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(fiveKiloHertzBand, (short) (-1500));
                    } else {
                        text5kHzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
                        MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(fiveKiloHertzBand, (short) -((16-seekBarLevel)*100));
                    }

                } else if (seekBarLevel > 16) {
                    text5kHzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
                    MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(fiveKiloHertzBand, (short) ((seekBarLevel-16)*100));
                }

                fiveKilohertzLevel = seekBarLevel;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub
            presetSpinner.setSelection(0);
            customEqualizer = true;
            EqualizerSetting equalizerSetting = getCurrentEquSetting();
            MainApplication.getService().getEqualizerHelper().storeLastEquSetting(equalizerSetting);

        }

    };

    /**
     * 12.5 kHz pro seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer12_5kHzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {

            try {
                //Get the appropriate pro band.
                short twelvePointFiveKiloHertzBand = MainApplication.getService().getEqualizerHelper().getEqualizer().getBand(9000000);

                //Set the gain level text based on the slider position.
                if (seekBarLevel==16) {
                    text12_5kHzGainTextView.setText("0 dB");
                    MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(twelvePointFiveKiloHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {

                    if (seekBarLevel==0) {
                        text12_5kHzGainTextView.setText("-" + "15 dB");
                        MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(twelvePointFiveKiloHertzBand, (short) (-1500));
                    } else {
                        text12_5kHzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
                        MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(twelvePointFiveKiloHertzBand, (short) -((16-seekBarLevel)*100));
                    }

                } else if (seekBarLevel > 16) {
                    text12_5kHzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
                    MainApplication.getService().getEqualizerHelper().getEqualizer().setBandLevel(twelvePointFiveKiloHertzBand, (short) ((seekBarLevel-16)*100));
                }

                twelvePointFiveKilohertzLevel = seekBarLevel;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub
            presetSpinner.setSelection(0);
            customEqualizer = true;
            EqualizerSetting equalizerSetting = getCurrentEquSetting();
            MainApplication.getService().getEqualizerHelper().storeLastEquSetting(equalizerSetting);

        }

    };

    /**
     * Spinner listener for reverb effects.
     */
    private AdapterView.OnItemSelectedListener reverbListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {

            if (MainApplication.getService()!=null)
                if (index==0) {
                    MainApplication.getService().getEqualizerHelper().getPresetReverb().setPreset(PresetReverb.PRESET_NONE);
                    reverbSetting = 0;
                } else if (index==1) {
                    MainApplication.getService().getEqualizerHelper().getPresetReverb().setPreset(PresetReverb.PRESET_LARGEHALL);
                    reverbSetting = 1;
                } else if (index==2) {
                    MainApplication.getService().getEqualizerHelper().getPresetReverb().setPreset(PresetReverb.PRESET_LARGEROOM);
                    reverbSetting = 2;
                } else if (index==3) {
                    MainApplication.getService().getEqualizerHelper().getPresetReverb().setPreset(PresetReverb.PRESET_MEDIUMHALL);
                    reverbSetting = 3;
                } else if (index==4) {
                    MainApplication.getService().getEqualizerHelper().getPresetReverb().setPreset(PresetReverb.PRESET_MEDIUMROOM);
                    reverbSetting = 4;
                } else if (index==5) {
                    MainApplication.getService().getEqualizerHelper().getPresetReverb().setPreset(PresetReverb.PRESET_SMALLROOM);
                    reverbSetting = 5;
                } else if (index==6) {
                    MainApplication.getService().getEqualizerHelper().getPresetReverb().setPreset(PresetReverb.PRESET_PLATE);
                    reverbSetting = 6;
                }

                else
                    reverbSetting = 0;

            EqualizerSetting equalizerSetting = getCurrentEquSetting();
            try{
                MainApplication.getService().getEqualizerHelper().storeLastEquSetting(equalizerSetting);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }

    };

    /**
     * Spinner listener for reverb effects.
     */
    private AdapterView.OnItemSelectedListener presetListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
            if (MainApplication.getService()!=null) {
                Log.d("LuatND", "EQ: " + presetSpinner.getSelectedItem().toString());
                if (index > 0) {
                    new AsyncInitSlidersTask().execute(MainApplication.getService().getEqualizerHelper().getPreset(presetSpinner.getSelectedItem().toString()));
                } else {
                    //Reset preset setting
                    resetEQ();
//                    Log.d("PlayerService", "fiftyHertzBandValue: " + customEqualizer);
//                    Log.d("PlayerService", "fiftyHertzBandValue: " + presetSpinner.getSelectedItem().toString());
                    if(!customEqualizer)
                        applyCurrentEQSettings();
                }

                MainApplication.getPref().edit().putInt("pref_selected_item", presetSpinner.getSelectedItemPosition()).apply();
                EqualizerSetting equalizerSetting = getCurrentEquSetting();
                MainApplication.getService().getEqualizerHelper().storeLastEquSetting(equalizerSetting);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }

    };

    /**
     * Bass boost listener.
     */
    private SeekBar.OnSeekBarChangeListener bassBoostListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
            MainApplication.getService().getEqualizerHelper().getBassBoost().setStrength((short) arg1);
            bassBoostLevel = (short) arg1;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
            EqualizerSetting equalizerSetting = getCurrentEquSetting();
            MainApplication.getService().getEqualizerHelper().storeLastEquSetting(equalizerSetting);
        }

    };

    /**
     * Enhance listener.
     */
    private SeekBar.OnSeekBarChangeListener enhanceListener = new SeekBar.OnSeekBarChangeListener() {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                MainApplication.getService().getEqualizerHelper().getEnhancer().setTargetGain((short) arg1);
            }
            enhancementLevel = (short) arg1;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
            EqualizerSetting equalizerSetting = getCurrentEquSetting();
            MainApplication.getService().getEqualizerHelper().storeLastEquSetting(equalizerSetting);
        }

    };

    /**
     * Virtualizer listener.
     */
    private SeekBar.OnSeekBarChangeListener virtualizerListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
            MainApplication.getService().getEqualizerHelper().getVirtualizer().setStrength((short) arg1);
            virtualizerLevel = (short) arg1;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
            EqualizerSetting equalizerSetting = getCurrentEquSetting();
            MainApplication.getService().getEqualizerHelper().storeLastEquSetting(equalizerSetting);
        }

    };

    /**
     * Builds the "Save Preset" dialog. Does not call the show() method, so you
     * should do this manually when calling this method.
     *
     * @return A fully built AlertDialog reference.
     */
    private void showSavePresetDialog() {

        new MyDialogBuilder(this)
                .title(R.string.title_save_preset)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(getString(R.string.hint_save_preset), "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Do something
                        //Get the preset name from the text field.
                        if(input.equals("")){
                            Toast.makeText(getApplicationContext(), R.string.error_valid_preset_name_toast, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        MainApplication.getService().getEqualizerHelper().insertPreset(input.toString(), getCurrentEquSetting());

                        //load data from db here
                        String[] dataPreset = MainApplication.getService().getEqualizerHelper().getPresetList();
                        ArrayList<String> dataPresets = new ArrayList<String>(Arrays.asList(dataPreset));
                        dataPresets.add(0, "None");
                        ArrayAdapter<String> presetAdapter = new ArrayAdapter<String>(ActivityEqualizer.this, android.R.layout.simple_spinner_item, dataPresets);
                        presetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        presetSpinner.setAdapter(presetAdapter);
                        presetAdapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), R.string.preset_saved_toast, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .negativeText(R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Builds the "Load Preset" dialog. Does not call the show() method, so this
     * should be done manually after calling this method.
     *
     * @return A fully built AlertDialog reference.
     */
//    private void showLoadPresetDialog() {
//
//        //load data from db here
//        String[] array = MainApplication.getService().getEqualizerHelper().getPresetList();
//
//        for (String s:array) {
//            Log.d("ActivityEqualizer", "showLoadPresetDialog: array " + s);
//        }
//
//        new MyDialogBuilder(this)
//                .title(R.string.title_load_preset)
//                .items(array)
//                .itemsCallback(new MaterialDialog.ListCallback() {
//                    @Override
//                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
//                        new AsyncInitSlidersTask().execute(MainApplication.getService().getEqualizerHelper().getPreset(text.toString()));
//                    }
//                })
//                .show();
//
//    }

    /**
     * Applies the current EQ settings to the service.
     */
    public void applyCurrentEQSettings() {
        if (MainApplication.getService()!=null)
            return;

        equalizer50HzListener.onProgressChanged(equalizer50HzSeekBar, equalizer50HzSeekBar.getProgress(), true);
        equalizer130HzListener.onProgressChanged(equalizer130HzSeekBar, equalizer130HzSeekBar.getProgress(), true);
        equalizer320HzListener.onProgressChanged(equalizer320HzSeekBar, equalizer320HzSeekBar.getProgress(), true);
        equalizer800HzListener.onProgressChanged(equalizer800HzSeekBar, equalizer800HzSeekBar.getProgress(), true);
        equalizer2kHzListener.onProgressChanged(equalizer2kHzSeekBar, equalizer2kHzSeekBar.getProgress(), true);
        equalizer5kHzListener.onProgressChanged(equalizer5kHzSeekBar, equalizer5kHzSeekBar.getProgress(), true);
        equalizer12_5kHzListener.onProgressChanged(equalizer12_5kHzSeekBar, equalizer12_5kHzSeekBar.getProgress(), true);

        virtualizerListener.onProgressChanged(virtualizerSeekBar, virtualizerSeekBar.getProgress(), true);
        bassBoostListener.onProgressChanged(bassBoostSeekBar, bassBoostSeekBar.getProgress(), true);
        enhanceListener.onProgressChanged(enhanceSeekBar, enhanceSeekBar.getProgress(), true);
        reverbListener.onItemSelected(reverbSpinner, null, reverbSpinner.getSelectedItemPosition(), 0l);

    }

    @SuppressLint("StaticFieldLeak")
    public class AsyncInitSlidersTask extends AsyncTask<EqualizerSetting, String, Boolean> {

        EqualizerSetting equalizerSetting;

        @Override
        protected Boolean doInBackground(EqualizerSetting... params) {
            equalizerSetting = params[0];
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if(equalizerSetting==null) return;



            fiftyHertzLevel = equalizerSetting.getFiftyHertz();
            oneThirtyHertzLevel = equalizerSetting.getOneThirtyHertz();
            threeTwentyHertzLevel = equalizerSetting.getThreeTwentyHertz();
            eightHundredHertzLevel = equalizerSetting.getEightHundredHertz();
            twoKilohertzLevel = equalizerSetting.getTwoKilohertz();
            fiveKilohertzLevel = equalizerSetting.getFiveKilohertz();
            twelvePointFiveKilohertzLevel = equalizerSetting.getTwelvePointFiveKilohertz();
            reverbSetting = equalizerSetting.getReverb();

            equalizerSetting = MainApplication.getService().getEqualizerHelper().getLastEquSetting();
            if(equalizerSetting==null) return;

            virtualizerLevel = equalizerSetting.getVirtualizer();
            bassBoostLevel = equalizerSetting.getBassBoost();
            enhancementLevel = equalizerSetting.getEnhancement();

            //Move the sliders to the pro settings.
            equalizer50HzSeekBar.setProgressAndThumb(fiftyHertzLevel);
            equalizer130HzSeekBar.setProgressAndThumb(oneThirtyHertzLevel);
            equalizer320HzSeekBar.setProgressAndThumb(threeTwentyHertzLevel);
            equalizer800HzSeekBar.setProgressAndThumb(eightHundredHertzLevel);
            equalizer2kHzSeekBar.setProgressAndThumb(twoKilohertzLevel);
            equalizer5kHzSeekBar.setProgressAndThumb(fiveKilohertzLevel);
            equalizer12_5kHzSeekBar.setProgressAndThumb(twelvePointFiveKilohertzLevel);
            virtualizerSeekBar.setProgress(virtualizerLevel);
            bassBoostSeekBar.setProgress(bassBoostLevel);
            enhanceSeekBar.setProgress(enhancementLevel);
            reverbSpinner.setSelection(reverbSetting, false);
            presetSpinner.setSelection(MainApplication.getPref().getInt("pref_selected_item", 0), false);

            //50Hz Band.
            if (fiftyHertzLevel==16) {
                text50HzGainTextView.setText("0 dB");
            } else if (fiftyHertzLevel < 16) {

                if (fiftyHertzLevel==0) {
                    text50HzGainTextView.setText("-" + "15 dB");
                } else {
                    text50HzGainTextView.setText("-" + (16-fiftyHertzLevel) + " dB");
                }

            } else if (fiftyHertzLevel > 16) {
                text50HzGainTextView.setText("+" + (fiftyHertzLevel-16) + " dB");
            }

            //130Hz Band.
            if (oneThirtyHertzLevel==16) {
                text130HzGainTextView.setText("0 dB");
            } else if (oneThirtyHertzLevel < 16) {

                if (oneThirtyHertzLevel==0) {
                    text130HzGainTextView.setText("-" + "15 dB");
                } else {
                    text130HzGainTextView.setText("-" + (16-oneThirtyHertzLevel) + " dB");
                }

            } else if (oneThirtyHertzLevel > 16) {
                text130HzGainTextView.setText("+" + (oneThirtyHertzLevel-16) + " dB");
            }

            //320Hz Band.
            if (threeTwentyHertzLevel==16) {
                text320HzGainTextView.setText("0 dB");
            } else if (threeTwentyHertzLevel < 16) {

                if (threeTwentyHertzLevel==0) {
                    text320HzGainTextView.setText("-" + "15 dB");
                } else {
                    text320HzGainTextView.setText("-" + (16-threeTwentyHertzLevel) + " dB");
                }

            } else if (threeTwentyHertzLevel > 16) {
                text320HzGainTextView.setText("+" + (threeTwentyHertzLevel-16) + " dB");
            }

            //800Hz Band.
            if (eightHundredHertzLevel==16) {
                text800HzGainTextView.setText("0 dB");
            } else if (eightHundredHertzLevel < 16) {

                if (eightHundredHertzLevel==0) {
                    text800HzGainTextView.setText("-" + "15 dB");
                } else {
                    text800HzGainTextView.setText("-" + (16-eightHundredHertzLevel) + " dB");
                }

            } else if (eightHundredHertzLevel > 16) {
                text800HzGainTextView.setText("+" + (eightHundredHertzLevel-16) + " dB");
            }

            //2kHz Band.
            if (twoKilohertzLevel==16) {
                text2kHzGainTextView.setText("0 dB");
            } else if (twoKilohertzLevel < 16) {

                if (twoKilohertzLevel==0) {
                    text2kHzGainTextView.setText("-" + "15 dB");
                } else {
                    text2kHzGainTextView.setText("-" + (16-twoKilohertzLevel) + " dB");
                }

            } else if (twoKilohertzLevel > 16) {
                text2kHzGainTextView.setText("+" + (twoKilohertzLevel-16) + " dB");
            }

            //5kHz Band.
            if (fiveKilohertzLevel==16) {
                text5kHzGainTextView.setText("0 dB");
            } else if (fiveKilohertzLevel < 16) {

                if (fiveKilohertzLevel==0) {
                    text5kHzGainTextView.setText("-" + "15 dB");
                } else {
                    text5kHzGainTextView.setText("-" + (16-fiveKilohertzLevel) + " dB");
                }

            } else if (fiveKilohertzLevel > 16) {
                text5kHzGainTextView.setText("+" + (fiveKilohertzLevel-16) + " dB");
            }

            //12.5kHz Band.
            if (twelvePointFiveKilohertzLevel==16) {
                text12_5kHzGainTextView.setText("0 dB");
            } else if (twelvePointFiveKilohertzLevel < 16) {

                if (twelvePointFiveKilohertzLevel==0) {
                    text12_5kHzGainTextView.setText("-" + "15 dB");
                } else {
                    text12_5kHzGainTextView.setText("-" + (16-twelvePointFiveKilohertzLevel) + " dB");
                }

            } else if (twelvePointFiveKilohertzLevel > 16) {
                text12_5kHzGainTextView.setText("+" + (twelvePointFiveKilohertzLevel-16) + " dB");
            }

        }

    }

//
//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }
}

