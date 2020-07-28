package power.audio.pro.music.player.utils;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import power.audio.pro.music.player.R;

public class ThemeUtils {
    private static final String THEME1 = "1";
    private static final String THEME2 = "2";
    private static final String THEME3 = "3";

    public static void showTheme(RelativeLayout relativeLayout) {
        if (relativeLayout != null) {
            switch (SpUtils.getString("selected_theme")) {
                case THEME1:
                default:
                    switch (SpUtils.getInt("selected_theme_item")) {
                        case 2:
                            relativeLayout.setBackgroundResource(R.drawable.color_2);
                            break;
                        case 3:
                            relativeLayout.setBackgroundResource(R.drawable.color_3);
                            break;
                        case 4:
                            relativeLayout.setBackgroundResource(R.drawable.color_4);
                            break;
                        case 5:
                            relativeLayout.setBackgroundResource(R.drawable.color_5);
                            break;
                        case 6:
                            relativeLayout.setBackgroundResource(R.drawable.color_6);
                            break;
                        case 7:
                            relativeLayout.setBackgroundResource(R.drawable.color_7);
                            break;
                        default:
                            relativeLayout.setBackgroundResource(R.drawable.color_1);
                            break;
                    }

                    break;
                case THEME2:
                    switch (SpUtils.getInt("selected_theme_item")) {
                        case 2:
                            relativeLayout.setBackgroundResource(R.drawable.human_2);
                            break;
                        case 3:
                            relativeLayout.setBackgroundResource(R.drawable.human_3);
                            break;
                        case 4:
                            relativeLayout.setBackgroundResource(R.drawable.human_4);
                            break;
                        case 5:
                            relativeLayout.setBackgroundResource(R.drawable.human_5);
                            break;
                        case 6:
                            relativeLayout.setBackgroundResource(R.drawable.human_6);
                            break;
                        case 7:
                            relativeLayout.setBackgroundResource(R.drawable.human_7);
                            break;
                        default:
                            relativeLayout.setBackgroundResource(R.drawable.human_1);
                            break;
                    }
                    break;
                case THEME3:
                    switch (SpUtils.getInt("selected_theme_item")) {
                        case 2:
                            relativeLayout.setBackgroundResource(R.drawable.nature_2);
                            break;
                        case 3:
                            relativeLayout.setBackgroundResource(R.drawable.nature_3);
                            break;
                        case 4:
                            relativeLayout.setBackgroundResource(R.drawable.nature_4);
                            break;
                        case 5:
                            relativeLayout.setBackgroundResource(R.drawable.nature_5);
                            break;
                        case 6:
                            relativeLayout.setBackgroundResource(R.drawable.nature_6);
                            break;
                        case 7:
                            relativeLayout.setBackgroundResource(R.drawable.nature_7);
                            break;
                        default:
                            relativeLayout.setBackgroundResource(R.drawable.nature_1);
                            break;
                    }
                    break;
            }
            relativeLayout.getBackground().setAlpha(102);
        }
    }

    public static void showTheme(LinearLayout relativeLayout) {
        if (relativeLayout != null) {
            switch (SpUtils.getString("selected_theme")) {
                case THEME1:
                default:
                    switch (SpUtils.getInt("selected_theme_item")) {
                        case 2:
                            relativeLayout.setBackgroundResource(R.drawable.color_2);
                            break;
                        case 3:
                            relativeLayout.setBackgroundResource(R.drawable.color_3);
                            break;
                        case 4:
                            relativeLayout.setBackgroundResource(R.drawable.color_4);
                            break;
                        case 5:
                            relativeLayout.setBackgroundResource(R.drawable.color_5);
                            break;
                        case 6:
                            relativeLayout.setBackgroundResource(R.drawable.color_6);
                            break;
                        case 7:
                            relativeLayout.setBackgroundResource(R.drawable.color_7);
                            break;
                        default:
                            relativeLayout.setBackgroundResource(R.drawable.color_1);
                            break;
                    }

                    break;
                case THEME2:
                    switch (SpUtils.getInt("selected_theme_item")) {
                        case 2:
                            relativeLayout.setBackgroundResource(R.drawable.human_2);
                            break;
                        case 3:
                            relativeLayout.setBackgroundResource(R.drawable.human_3);
                            break;
                        case 4:
                            relativeLayout.setBackgroundResource(R.drawable.human_4);
                            break;
                        case 5:
                            relativeLayout.setBackgroundResource(R.drawable.human_5);
                            break;
                        case 6:
                            relativeLayout.setBackgroundResource(R.drawable.human_6);
                            break;
                        case 7:
                            relativeLayout.setBackgroundResource(R.drawable.human_7);
                            break;
                        default:
                            relativeLayout.setBackgroundResource(R.drawable.human_1);
                            break;
                    }
                    break;
                case THEME3:
                    switch (SpUtils.getInt("selected_theme_item")) {
                        case 2:
                            relativeLayout.setBackgroundResource(R.drawable.nature_2);
                            break;
                        case 3:
                            relativeLayout.setBackgroundResource(R.drawable.nature_3);
                            break;
                        case 4:
                            relativeLayout.setBackgroundResource(R.drawable.nature_4);
                            break;
                        case 5:
                            relativeLayout.setBackgroundResource(R.drawable.nature_5);
                            break;
                        case 6:
                            relativeLayout.setBackgroundResource(R.drawable.nature_6);
                            break;
                        case 7:
                            relativeLayout.setBackgroundResource(R.drawable.nature_7);
                            break;
                        default:
                            relativeLayout.setBackgroundResource(R.drawable.nature_1);
                            break;
                    }
                    break;
            }
//            relativeLayout.setBackgroundColor(Color.parseColor("#80000000"));
            relativeLayout.getBackground().setAlpha(102);
        }
    }


    public static void showTheme(LinearLayout linearLayout, String theme, int item) {
        if (linearLayout != null) {
            switch (theme) {
                case THEME1:
                    switch (item) {
                        case 2:
                            linearLayout.setBackgroundResource(R.drawable.color_2);
                            break;
                        case 3:
                            linearLayout.setBackgroundResource(R.drawable.color_3);
                            break;
                        case 4:
                            linearLayout.setBackgroundResource(R.drawable.color_4);
                            break;
                        case 5:
                            linearLayout.setBackgroundResource(R.drawable.color_5);
                            break;
                        case 6:
                            linearLayout.setBackgroundResource(R.drawable.color_6);
                            break;
                        case 7:
                            linearLayout.setBackgroundResource(R.drawable.color_7);
                            break;
                        default:
                            linearLayout.setBackgroundResource(R.drawable.color_1);
                            break;
                    }

                    break;
                case THEME2:
                    switch (SpUtils.getInt("selected_theme_item")) {
                        case 2:
                            linearLayout.setBackgroundResource(R.drawable.human_2);
                            break;
                        case 3:
                            linearLayout.setBackgroundResource(R.drawable.human_3);
                            break;
                        case 4:
                            linearLayout.setBackgroundResource(R.drawable.human_4);
                            break;
                        case 5:
                            linearLayout.setBackgroundResource(R.drawable.human_5);
                            break;
                        case 6:
                            linearLayout.setBackgroundResource(R.drawable.human_6);
                            break;
                        case 7:
                            linearLayout.setBackgroundResource(R.drawable.human_7);
                            break;
                        default:
                            linearLayout.setBackgroundResource(R.drawable.human_1);
                            break;
                    }
                    break;
                case THEME3:
                    switch (SpUtils.getInt("selected_theme_item")) {
                        case 2:
                            linearLayout.setBackgroundResource(R.drawable.nature_2);
                            break;
                        case 3:
                            linearLayout.setBackgroundResource(R.drawable.nature_3);
                            break;
                        case 4:
                            linearLayout.setBackgroundResource(R.drawable.nature_4);
                            break;
                        case 5:
                            linearLayout.setBackgroundResource(R.drawable.nature_5);
                            break;
                        case 6:
                            linearLayout.setBackgroundResource(R.drawable.nature_6);
                            break;
                        case 7:
                            linearLayout.setBackgroundResource(R.drawable.nature_7);
                            break;
                        default:
                            linearLayout.setBackgroundResource(R.drawable.nature_1);
                            break;
                    }
                    break;
                default:
                    linearLayout.setBackgroundResource(R.drawable.human_2);
                    break;
            }
//            relativeLayout.setBackgroundColor(Color.parseColor("#80000000"));
            linearLayout.getBackground().setAlpha(102);
        }
    }
}
