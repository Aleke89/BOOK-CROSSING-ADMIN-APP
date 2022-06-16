package kz.incubator.mss.reads.groups_menu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.book_list_menu.one_book_fragments.RecyclerItemClickListener;
import kz.incubator.mss.reads.database.StoreDatabase;
import kz.incubator.mss.reads.groups_menu.activities.GroupUsersActivity;
import kz.incubator.mss.reads.groups_menu.adapters.GroupListAdapter;
import kz.incubator.mss.reads.groups_menu.module.Groups;

import static kz.incubator.mss.reads.MenuActivity.setTitle;

public class GroupsFragment extends Fragment implements View.OnClickListener {
    View view;
    ArrayList<Groups> groupList;
    GroupListAdapter groupListAdapter;
    RecyclerView.LayoutManager linearLayoutManager;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Dialog addGroupDialog;
    Button addGroupBtn;
    EditText groupName;
    ProgressBar progressBar;
    DatabaseReference databaseReference;

    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    Dialog d;
    int position;

    @BindView(R.id.groupRecyclerView) RecyclerView groupRecyclerView;
    @BindView(R.id.fabBtn) FloatingActionButton fabBtn;
    @BindView(R.id.llProgressBar) View progressLoading;

    public GroupsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_groups, container, false);
        ButterKnife.bind(this, view);
        setTitle(getString(R.string.groups));

        initViews();
        checkVersion();

        return view;
    }

    public void initViews() {
        groupList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(getActivity());
        groupRecyclerView.setLayoutManager(linearLayoutManager);
        groupRecyclerView.setHasFixedSize(true);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

        fabBtn.setOnClickListener(this);

        addGroupsChangeListener();
        groupListAdapter = new GroupListAdapter(getActivity(), groupList);
        groupRecyclerView.setAdapter(groupListAdapter);
        groupRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), groupRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int pos) {

                        Intent intent = new Intent(getActivity(), GroupUsersActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("groupName", groupList.get(pos).getGroup_name());
                        bundle.putSerializable("groupId", groupList.get(pos).getGroup_id());
                        intent.putExtras(bundle);
                        getActivity().startActivity(intent);
                    }

                    @Override
                    public void onLongItemClick(View view, int pos) {
                        d = new Dialog(getActivity());
                        position = pos;

                        d.setContentView(R.layout.dialog_edit_subjects);
                        LinearLayout deleteLayout = d.findViewById(R.id.deleteLayout);
                        deleteLayout.setOnClickListener(GroupsFragment.this);
                        d.show();
                    }
                })
        );


        setupSwipeRefresh();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.deleteLayout:
                new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme)
                        .setTitle(groupList.get(position).getGroup_name())
                        .setMessage(getString(R.string.del_profile_subjects))

                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String groupId = groupList.get(position).getGroup_id();
                                databaseReference.child("group_list").child(groupId).removeValue();

                                Toast.makeText(getActivity(), getString(R.string.profile_subjects_deleted), Toast.LENGTH_SHORT).show();
                                d.dismiss();

                                groupList.remove(position);
                                groupListAdapter.notifyDataSetChanged();

                            }
                        })
                        .setNeutralButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                break;

            case R.id.fabBtn:
                addGroupDialog = new Dialog(getActivity());
                addGroupDialog.setContentView(R.layout.activity_add_group);

                groupName = addGroupDialog.findViewById(R.id.groupName);
                progressBar = addGroupDialog.findViewById(R.id.progressBar);

                addGroupBtn = addGroupDialog.findViewById(R.id.addBtn);
                addGroupBtn.setOnClickListener(this);

                addGroupDialog.show();

                break;

            case R.id.addBtn:
                String gName = groupName.getText().toString();
                String gId = getFId();

                if(!TextUtils.isEmpty(gName)){

                    addGroupBtn.setVisibility(View.GONE);
                    Groups groups = new Groups(gId, gName, 0, 0);
                    databaseReference.child("group_list").child(gId).setValue(groups).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getActivity(), getString(R.string.group_added), Toast.LENGTH_SHORT).show();
                            addGroupDialog.dismiss();
                        }
                    });

                }else{

                    groupName.setError(getString(R.string.enter_group_error));
                }

                break;
        }
    }

    Groups groups;
    public void addGroupsChangeListener(){
        databaseReference.child("group_list").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Groups groups = dataSnapshot.getValue(Groups.class);
                int ratingN = groups.getSum_point() / (groups.getPerson_count() == 0?1:groups.getPerson_count());

                Log.d("M_GroupsFragment", "group name: "+groups.getGroup_name());
                Log.d("M_GroupsFragment", "group sum point: "+ratingN);

                groupList.add(groups);
                Collections.sort(groupList, Groups.groupPlace);
                groupListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                groups = dataSnapshot.getValue(Groups.class);
                for(int i = 0; i < groupList.size(); i++){
                    if(groupList.get(i).getGroup_id().equals(groups.getGroup_id())){
                        groupList.set(i, groups);
                        Collections.sort(groupList, Groups.groupPlace);
                        groupListAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                groups = dataSnapshot.getValue(Groups.class);
                for(int i = 0; i < groupList.size(); i++){
                    if(groupList.get(i).getGroup_id().equals(groups.getGroup_id())){
                        groupList.remove(i);
                        Collections.sort(groupList, Groups.groupPlace);
                        groupListAdapter.notifyDataSetChanged();
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

    public void checkVersion() {
        Query myTopPostsQuery = databaseReference.child("user_ver");
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    String newVersion = dataSnapshot.getValue().toString();
                    if (!getDayCurrentVersion().equals(newVersion)) {
                        refreshUsersFromFirebase(newVersion);
                    } else {
                        onItemsLoadComplete();
                    }
                } else {
                    Toast.makeText(getActivity(), "Can not find user_ver firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void refreshUsersFromFirebase(String version) {
        new GetUsersAsyncTask(getActivity(), version, mSwipeRefreshLayout, progressLoading).execute();
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
            checkVersion();
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

    public String getDayCurrentVersion() {
        Cursor res = sqdb.rawQuery("SELECT user_ver FROM versions ", null);
        res.moveToNext();
        return res.getString(0);
    }

    public String getFId() {
        Date date = new Date();
        String idN = "i" + date.getTime();
        return idN;
    }
}
