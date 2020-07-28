package com.nvp.easypermissions;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.StringRes;

public class PermissionBuilder {
    private PermissionListener listener;
    private String[] permissions;
    private CharSequence rationaleTitle;
    private CharSequence rationaleMessage;
    private CharSequence denyTitle;
    private CharSequence denyMessage;
    private CharSequence settingButtonText;
    private boolean hasSettingBtn = true;

    private CharSequence deniedCloseButtonText;
    private CharSequence rationaleConfirmText;
    private Context context;

    public PermissionBuilder(Context context) {
        this.context = context;
        deniedCloseButtonText = context.getString(R.string.cancel);
        rationaleConfirmText = context.getString(R.string.confirm);
        denyMessage = context.getString(R.string.deny_message);
    }

    public void check() {
        if (listener == null) {
            throw new IllegalArgumentException("You must setPermissionListener() on FragileHeartPermission");
        } else if (permissions == null || permissions.length == 0) {
            throw new IllegalArgumentException("You must setPermissions() on FragileHeartPermission");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            listener.onPermissionGranted();
            return;
        }

        if (NvpPermission.isGranted(context, permissions)) {
            listener.onPermissionGranted();
            return;
        }

        Intent intent = new Intent(context, NvpPermissionActivity.class);
        intent.putExtra(NvpPermissionActivity.EXTRA_PERMISSIONS, permissions);

        intent.putExtra(NvpPermissionActivity.EXTRA_RATIONALE_TITLE, rationaleTitle);
        intent.putExtra(NvpPermissionActivity.EXTRA_RATIONALE_MESSAGE, rationaleMessage);
        intent.putExtra(NvpPermissionActivity.EXTRA_DENY_TITLE, denyTitle);
        intent.putExtra(NvpPermissionActivity.EXTRA_DENY_MESSAGE, denyMessage);
        intent.putExtra(NvpPermissionActivity.EXTRA_PACKAGE_NAME, context.getPackageName());
        intent.putExtra(NvpPermissionActivity.EXTRA_SETTING_BUTTON, hasSettingBtn);
        intent.putExtra(NvpPermissionActivity.EXTRA_DENIED_DIALOG_CLOSE_TEXT, deniedCloseButtonText);
        intent.putExtra(NvpPermissionActivity.EXTRA_RATIONALE_CONFIRM_TEXT, rationaleConfirmText);
        intent.putExtra(NvpPermissionActivity.EXTRA_SETTING_BUTTON_TEXT, settingButtonText);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        NvpPermissionActivity.startActivity(context, intent, listener);
        NvpPermission.setFirstRequest(context, permissions);
    }

    public PermissionBuilder setPermissionListener(PermissionListener listener) {
        this.listener = listener;
        return this;
    }

    public PermissionBuilder setPermissions(String... permissions) {
        this.permissions = permissions;
        return this;
    }

    public PermissionBuilder setRationaleMessage(@StringRes int stringRes) {
        return setRationaleMessage(getText(stringRes));
    }

    private CharSequence getText(@StringRes int stringRes) {
        return context.getText(stringRes);
    }

    public PermissionBuilder setRationaleMessage(CharSequence rationaleMessage) {
        this.rationaleMessage = rationaleMessage;
        return this;
    }


    public PermissionBuilder setRationaleTitle(@StringRes int stringRes) {
        return setRationaleTitle(getText(stringRes));
    }

    public PermissionBuilder setRationaleTitle(CharSequence rationaleMessage) {
        this.rationaleTitle = rationaleMessage;
        return this;
    }

    public PermissionBuilder setDeniedMessage(@StringRes int stringRes) {
        return setDeniedMessage(getText(stringRes));
    }

    public PermissionBuilder setDeniedMessage(CharSequence denyMessage) {
        this.denyMessage = denyMessage;
        return this;
    }

    public PermissionBuilder setDeniedTitle(@StringRes int stringRes) {
        return setDeniedTitle(getText(stringRes));
    }

    public PermissionBuilder setDeniedTitle(CharSequence denyTitle) {
        this.denyTitle = denyTitle;
        return this;
    }

    public PermissionBuilder setGotoSettingButton(boolean hasSettingBtn) {
        this.hasSettingBtn = hasSettingBtn;
        return this;
    }

    public PermissionBuilder setGotoSettingButtonText(@StringRes int stringRes) {
        return setGotoSettingButtonText(getText(stringRes));
    }

    public PermissionBuilder setGotoSettingButtonText(CharSequence rationaleConfirmText) {
        this.settingButtonText = rationaleConfirmText;
        return this;
    }

    public PermissionBuilder setRationaleConfirmText(@StringRes int stringRes) {
        return setRationaleConfirmText(getText(stringRes));
    }

    public PermissionBuilder setRationaleConfirmText(CharSequence rationaleConfirmText) {
        this.rationaleConfirmText = rationaleConfirmText;
        return this;
    }

    public PermissionBuilder setDeniedCloseButtonText(CharSequence deniedCloseButtonText) {
        this.deniedCloseButtonText = deniedCloseButtonText;
        return this;
    }

    public PermissionBuilder setDeniedCloseButtonText(@StringRes int stringRes) {
        return setDeniedCloseButtonText(getText(stringRes));
    }
}
