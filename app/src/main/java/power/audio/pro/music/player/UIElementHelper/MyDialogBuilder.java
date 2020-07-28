package power.audio.pro.music.player.UIElementHelper;

import android.content.Context;
import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Custom material dialog builder for creating global animation and setting drawable gradient
 */

public class MyDialogBuilder extends MaterialDialog.Builder{

    public MyDialogBuilder(@NonNull Context context) {
        super(context);
    }

    @Override
    public MaterialDialog build() {
        return new MyDialog(this);
    }
}

class MyDialog extends MaterialDialog {

    MyDialog(Builder builder) {
        super(builder);
    }

    @Override
    public void show() {
        //if you add something here, remember to add that in FileSaveDialog and AfterSaveActionDialog of
        //ringtone cutter too
        //but even if you forget, disaster won't happen
        if(getWindow()!=null) {
            //getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
           // getWindow().setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());
        }

        super.show();
    }
}
