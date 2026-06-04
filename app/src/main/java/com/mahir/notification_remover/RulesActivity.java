package com.mahir.notification_remover;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RulesActivity extends AppCompatActivity {

    private RulesManager rulesManager;
    private RulesAdapter adapter;
    private final List<Rule> rules = new ArrayList<>();
    private RecyclerView rulesRecyclerView;
    private TextView noRulesPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);

        setSupportActionBar(findViewById(R.id.rules_toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rulesManager = new RulesManager(this);

        noRulesPrompt = findViewById(R.id.no_rules_prompt);
        rulesRecyclerView = findViewById(R.id.rules_list);
        adapter = new RulesAdapter(rules, this::deleteRule);
        rulesRecyclerView.setAdapter(adapter);
        rulesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.add_keyword_fab);
        fab.setOnClickListener(v -> showAddKeywordDialog());

        loadRules();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadRules() {
        rules.clear();

        for (Map.Entry<String, String> entry : rulesManager.getBlockedPackages().entrySet()) {
            rules.add(new Rule(Rule.Type.APP, entry.getKey(), entry.getValue()));
        }
        for (String kw : rulesManager.getKeywords()) {
            rules.add(new Rule(Rule.Type.KEYWORD, kw, kw));
        }

        adapter.notifyDataSetChanged();
        boolean empty = rules.isEmpty();
        noRulesPrompt.setVisibility(empty ? View.VISIBLE : View.GONE);
        rulesRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void deleteRule(Rule rule) {
        if (rule.type == Rule.Type.APP) {
            rulesManager.unblockPackage(rule.value);
        } else {
            rulesManager.removeKeyword(rule.value);
        }
        loadRules();
    }

    private void showAddKeywordDialog() {
        EditText input = new EditText(this);
        input.setHint(getString(R.string.keyword_hint));
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_keyword_title)
                .setView(input)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String kw = input.getText().toString().trim();
                    if (!kw.isEmpty()) {
                        rulesManager.addKeyword(kw);
                        loadRules();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
