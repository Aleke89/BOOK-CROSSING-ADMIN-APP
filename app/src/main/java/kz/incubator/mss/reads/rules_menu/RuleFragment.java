package kz.incubator.mss.reads.rules_menu;


import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import kz.incubator.mss.reads.MenuActivity;
import kz.incubator.mss.reads.R;

import static kz.incubator.mss.reads.MenuActivity.setTitle;


public class RuleFragment extends Fragment implements View.OnClickListener {

    View view;
    MenuActivity menuActivity;
    ArrayList<Rules> ruleList;
    RulesListAdapter ruleListAdapter;
    RecyclerView.LayoutManager linearLayoutManager;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Dialog addRuleDialog;
    Button addRuleBtn, cancelRuleBtn;
    ProgressBar progressBar;
    DatabaseReference databaseReference;
    Rules rules;

    @BindView(R.id.groupRecyclerView)
    RecyclerView ruleRecyclerView;
    @BindView(R.id.fabBtn)
    FloatingActionButton fabBtn;
    @BindView(R.id.llProgressBar)
    View progressLoading;

    public RuleFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_rule, container, false);
        ButterKnife.bind(this, view);
        setTitle(getString(R.string.menu_rules));

        initViews();
        return view;
    }

    public void initViews() {
        ruleList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(getActivity());
        ruleRecyclerView.setLayoutManager(linearLayoutManager);
        ruleRecyclerView.setHasFixedSize(true);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        fabBtn.setOnClickListener(this);

//        ruleList.add(new Rules("Дедлайн", "Таңдалған дедлайнды сақтау"));
//        ruleList.add(new Rules("Кітап", "Бір кітап аяқтамай екіншісіне көшпеу"));
//        ruleList.add(new Rules("Рецензия", "Рецензияны шарттарға сәйкес жазу"));

        addRulesChangeListener();
        ruleListAdapter = new RulesListAdapter(getActivity(), ruleList);
        ruleRecyclerView.setAdapter(ruleListAdapter);

//        ruleRecyclerView.addOnItemTouchListener(
//                new RecyclerItemClickListener(getActivity(), ruleRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(View view, final int pos) {
//                        Toast.makeText(getActivity(), "Click click", Toast.LENGTH_SHORT).show();
//
//                    }
//
//                    @Override
//                    public void onLongItemClick(View view, int position) {
//
//                    }
//                })
//        );

        progressLoading.setVisibility(View.GONE);
        setupSwipeRefresh();
    }

    EditText ruleTitle, ruleDesc;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fabBtn:
                addRuleDialog = new Dialog(getActivity());
                addRuleDialog.setContentView(R.layout.dialog_add_rule);
                addRuleDialog.setCancelable(false);
                addRuleDialog.setCanceledOnTouchOutside(false);

                progressBar = addRuleDialog.findViewById(R.id.progressBar);
                ruleTitle = addRuleDialog.findViewById(R.id.ruleTitle);
                ruleDesc = addRuleDialog.findViewById(R.id.ruleDesc);

                addRuleBtn = addRuleDialog.findViewById(R.id.addBtn);
                cancelRuleBtn = addRuleDialog.findViewById(R.id.cancelBtn);
                addRuleBtn.setOnClickListener(this);
                cancelRuleBtn.setOnClickListener(this);

                addRuleDialog.show();

                break;

            case R.id.cancelBtn:
                addRuleDialog.dismiss();
                break;

            case R.id.addBtn:


                String rTitle = ruleTitle.getText().toString();
                String rDesc = ruleDesc.getText().toString();
                String rId = getFId();

                if (TextUtils.isEmpty(rTitle)) {
                    ruleTitle.setError(getString(R.string.enter_rule_title_error));
                    return;
                }

                if (TextUtils.isEmpty(rDesc)) {
                    ruleDesc.setError(getString(R.string.enter_rule_desc_error));
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                cancelRuleBtn.setVisibility(View.INVISIBLE);
                addRuleBtn.setVisibility(View.INVISIBLE);

                Rules rules = new Rules(rId, rTitle, rDesc);
                databaseReference.child("rules_list").child(rId).setValue(rules).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getActivity(), getString(R.string.rule_added), Toast.LENGTH_SHORT).show();
                        addRuleDialog.dismiss();
                    }
                });
                break;
        }
    }

    public void addRulesChangeListener() {
        databaseReference.child("rules_list").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Rules rules = dataSnapshot.getValue(Rules.class);
                ruleList.add(rules);

//                Collections.sort(groupList, Groups.groupPlace);
                ruleListAdapter.notifyDataSetChanged();
                progressLoading.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                rules = dataSnapshot.getValue(Rules.class);
                for (int i = 0; i < ruleList.size(); i++) {
                    if (ruleList.get(i).getRuleId().equals(rules.getRuleId())) {
                        ruleList.set(i, rules);
                        ruleListAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                rules = dataSnapshot.getValue(Rules.class);
                for (int i = 0; i < ruleList.size(); i++) {
                    if (ruleList.get(i).getRuleId().equals(rules.getRuleId())) {
                        ruleList.remove(i);
                        ruleListAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setupSwipeRefresh() {
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    public void onItemsLoadComplete() {
        progressLoading.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(false);

    }

    public void refreshItems() {
        if (isOnline()) {
//            addRulesChangeListener();
        }

    }

    private boolean isOnline() {
        if (isNetworkAvailable()) {
            return true;

        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.inetConnection), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public String getFId() {
        Date date = new Date();
        String idN = "r" + date.getTime();
        return idN;
    }

}
