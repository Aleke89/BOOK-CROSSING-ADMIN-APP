package kz.incubator.mss.reads.rules_menu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import kz.incubator.mss.reads.R;

public class RulesListAdapter extends RecyclerView.Adapter<RulesListAdapter.MyTViewHolder> {

    private Context context;
    private List<Rules> dataList;
    TypedArray colorStore;
    DatabaseReference databaseReference;

    public class MyTViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.backgroundColor)
        RelativeLayout backgroundColor;
        @BindView(R.id.ruleNumber) TextView ruleNumber;
        @BindView(R.id.ruleTitle) TextView ruleTitle;
        @BindView(R.id.ruleDesc) TextView ruleDesc;
        @BindView(R.id.textViewOptions) TextView textViewOptions;

        public MyTViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public RulesListAdapter(Context context, List<Rules> dataList) {
        this.context = context;
        this.dataList = dataList;
        colorStore = context.getResources().obtainTypedArray(R.array.colorStore);
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rules, parent, false);

        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyTViewHolder holder, int position) {

        final Rules rule = dataList.get(position);

        holder.ruleNumber.setText("Rule #"+(position+1));
        holder.ruleTitle.setText(rule.getTitle());
        holder.ruleDesc.setText(rule.getDesc());

        int color = colorStore.getResourceId(position, 0);

        holder.backgroundColor.setBackgroundColor(context.getResources().getColor(color));
        holder.textViewOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popup = new PopupMenu(context, holder.textViewOptions);
                popup.inflate(R.menu.rule_options_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_delete:
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle(context.getString(R.string.rule_delete));
                                builder.setMessage(rule.getTitle()+"\n"+rule.getDesc());

                                builder.setCancelable(false)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                                String rId = rule.getRuleId();

                                                databaseReference.child("rules_list").child(rId).removeValue();
                                                Toast.makeText(context, context.getString(R.string.rule_deleted), Toast.LENGTH_SHORT).show();

                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                                return true;
                            case R.id.action_edit:

                                final Dialog addRuleDialog = new Dialog(context);
                                addRuleDialog.setContentView(R.layout.dialog_add_rule);
                                addRuleDialog.setCancelable(false);
                                addRuleDialog.setCanceledOnTouchOutside(false);

                                final ProgressBar progressBar = addRuleDialog.findViewById(R.id.progressBar);
                                final EditText ruleTitle = addRuleDialog.findViewById(R.id.ruleTitle);
                                final EditText ruleDesc = addRuleDialog.findViewById(R.id.ruleDesc);

                                ruleTitle.setText(rule.getTitle());
                                ruleDesc.setText(rule.getDesc());

                                final Button addRuleBtn = addRuleDialog.findViewById(R.id.addBtn);
                                final Button cancelRuleBtn = addRuleDialog.findViewById(R.id.cancelBtn);
                                addRuleBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        String rTitle = ruleTitle.getText().toString();
                                        String rDesc = ruleDesc.getText().toString();
                                        String rId = rule.getRuleId();

                                        if (TextUtils.isEmpty(rTitle)) {
                                            ruleTitle.setError(context.getString(R.string.enter_rule_title_error));
                                            return;
                                        }

                                        if (TextUtils.isEmpty(rDesc)) {
                                            ruleDesc.setError(context.getString(R.string.enter_rule_desc_error));
                                            return;
                                        }

                                        progressBar.setVisibility(View.VISIBLE);
                                        cancelRuleBtn.setVisibility(View.INVISIBLE);
                                        addRuleBtn.setVisibility(View.INVISIBLE);
                                        Rules rules = new Rules(rId, rTitle, rDesc);
                                        databaseReference.child("rules_list").child(rId).setValue(rules).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(context, context.getString(R.string.rule_changed), Toast.LENGTH_SHORT).show();
                                                addRuleDialog.dismiss();
                                            }
                                        });
                                    }
                                });
                                cancelRuleBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        addRuleDialog.dismiss();
                                    }
                                });

                                addRuleDialog.show();

                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

}