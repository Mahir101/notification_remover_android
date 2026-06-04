package com.mahir.notification_remover;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends NotificationListenerActivity implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView notificationList;
    NotificationListAdapter notificationListAdapter;
    NotificationListener notificationListener;
    BottomSheet notificationServicePermissionBottomSheet;
    TextView noItemsPromptView;
    int notificationIconColor;
    RulesManager rulesManager;
    KillSwitchManager killSwitchManager;
    MaterialCardView killSwitchBanner;
    TextView killSwitchStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        rulesManager = new RulesManager(this);
        killSwitchManager = new KillSwitchManager(this);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(this::onMenuItemSelected);

        notificationServicePermissionBottomSheet = new BottomSheet();
        notificationServicePermissionBottomSheet.show(getSupportFragmentManager(), BottomSheet.TAG);
        notificationServicePermissionBottomSheet.hide(true);

        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        notificationList = findViewById(R.id.notification_list);
        noItemsPromptView = findViewById(R.id.no_items_prompt);

        killSwitchBanner = findViewById(R.id.kill_switch_banner);
        killSwitchStatusText = findViewById(R.id.kill_switch_status_text);
        Button killSwitchOffButton = findViewById(R.id.kill_switch_off_button);
        killSwitchOffButton.setOnClickListener(v -> deactivateKillSwitch());

        FloatingActionButton killSwitchFab = findViewById(R.id.kill_switch_fab);
        killSwitchFab.setOnClickListener(v -> {
            if (killSwitchManager.isActive()) {
                deactivateKillSwitch();
            } else {
                showKillSwitchDialog();
            }
        });

        notificationIconColor = MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorPrimary, Color.BLACK
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateKillSwitchBanner();
    }

    private boolean onMenuItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_rules) {
            startActivity(new Intent(this, RulesActivity.class));
            return true;
        }
        return false;
    }

    @Override
    public void onRefresh() {
        if (notificationListener == null) return;
        notificationListener.retrieveCurrentStatusBarNotifications();
        updateMainScreen();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void askForNotificationServicePermission(boolean needPermission) {
        if (needPermission) {
            notificationServicePermissionBottomSheet.hide(false);
            notificationServicePermissionBottomSheet.setOnClickListener(
                    (View v) -> jumpToNotificationServicePermissionSettingPage()
            );
        } else {
            notificationServicePermissionBottomSheet.hide(true);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onNotificationListenerServiceStarted(NotificationListener listener) {
        this.notificationListener = listener;

        listener.retrieveCurrentStatusBarNotifications();

        notificationListAdapter = new NotificationListAdapter(notificationListener, notificationIconColor);
        notificationListAdapter.setOnLongPressListener(this::showBlockAppDialog);
        notificationList.setAdapter(notificationListAdapter);
        notificationList.setLayoutManager(new LinearLayoutManager(this));
        notificationListener.registerListenerCallback((l) -> updateMainScreen());
        updateMainScreen();
        updateKillSwitchBanner();
    }

    @Override
    public void onNotificationListenerServiceStopped() {
        notificationListener = null;
    }

    private void showKillSwitchDialog() {
        String[] labels = {
                getString(R.string.ks_30min),
                getString(R.string.ks_1hour),
                getString(R.string.ks_2hours),
                getString(R.string.ks_4hours),
                getString(R.string.ks_permanent)
        };
        long[] durations = {
                KillSwitchManager.DURATION_30_MIN,
                KillSwitchManager.DURATION_1_HOUR,
                KillSwitchManager.DURATION_2_HOURS,
                KillSwitchManager.DURATION_4_HOURS,
                KillSwitchManager.DURATION_PERMANENT
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.kill_switch_for)
                .setItems(labels, (dialog, which) -> {
                    if (notificationListener == null) return;
                    killSwitchManager.activate(notificationListener, durations[which]);
                    notificationListener.retrieveCurrentStatusBarNotifications();
                    updateMainScreen();
                    updateKillSwitchBanner();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deactivateKillSwitch() {
        if (notificationListener != null) {
            killSwitchManager.deactivate(notificationListener);
            notificationListener.retrieveCurrentStatusBarNotifications();
            updateMainScreen();
        } else {
            killSwitchManager.deactivate(null);
        }
        updateKillSwitchBanner();
        Toast.makeText(this, R.string.kill_switch_deactivated, Toast.LENGTH_SHORT).show();
    }

    private void updateKillSwitchBanner() {
        if (killSwitchManager.isActive()) {
            killSwitchBanner.setVisibility(View.VISIBLE);
            long expiresAt = killSwitchManager.getExpiresAt();
            if (expiresAt == 0) {
                killSwitchStatusText.setText(R.string.kill_switch_active_permanent);
            } else {
                String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date(expiresAt));
                killSwitchStatusText.setText(getString(R.string.kill_switch_active_until, time));
            }
        } else {
            killSwitchBanner.setVisibility(View.GONE);
        }
    }

    private void showBlockAppDialog(NotificationItem item) {
        String packageName = item.getSbn().getPackageName();
        String appName;
        try {
            appName = AppUtils.getAppName(this, packageName);
        } catch (PackageManager.NameNotFoundException e) {
            appName = packageName;
        }
        String finalAppName = appName;

        new AlertDialog.Builder(this)
                .setTitle(R.string.block_app_title)
                .setMessage(getString(R.string.block_app_message, finalAppName))
                .setPositiveButton(R.string.block, (dialog, which) -> {
                    rulesManager.blockPackage(packageName, finalAppName);
                    if (notificationListener != null) {
                        notificationListener.hideOngoingNotification(item.getSbn());
                        notificationListener.retrieveCurrentStatusBarNotifications();
                    }
                    updateMainScreen();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateMainScreen() {
        if (notificationListAdapter == null) return;
        notificationListAdapter.notifyDataSetChanged();

        if (notificationListAdapter.getItemCount() == 0) {
            swipeRefreshLayout.setVisibility(View.GONE);
            noItemsPromptView.setVisibility(View.VISIBLE);
        } else {
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            noItemsPromptView.setVisibility(View.GONE);
        }
    }
}
