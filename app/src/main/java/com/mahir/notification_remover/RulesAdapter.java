package com.mahir.notification_remover;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.function.Consumer;

public class RulesAdapter extends RecyclerView.Adapter<RulesAdapter.ViewHolder> {
    private final List<Rule> rules;
    private final Consumer<Rule> onDelete;

    public RulesAdapter(List<Rule> rules, Consumer<Rule> onDelete) {
        this.rules = rules;
        this.onDelete = onDelete;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView typeLabel;
        final TextView nameLabel;
        final MaterialButton deleteButton;

        ViewHolder(View view) {
            super(view);
            typeLabel = view.findViewById(R.id.rule_type_label);
            nameLabel = view.findViewById(R.id.rule_name_label);
            deleteButton = view.findViewById(R.id.rule_delete_button);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rule_item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rule rule = rules.get(position);
        holder.typeLabel.setText(
                rule.type == Rule.Type.APP
                        ? holder.itemView.getContext().getString(R.string.rule_type_app)
                        : holder.itemView.getContext().getString(R.string.rule_type_keyword)
        );
        holder.nameLabel.setText(rule.displayName);
        holder.deleteButton.setOnClickListener(v -> onDelete.accept(rule));
    }

    @Override
    public int getItemCount() {
        return rules.size();
    }
}
