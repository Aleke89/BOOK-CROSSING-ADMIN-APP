package kz.incubator.mss.reads.about_us_menu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

import kz.incubator.mss.reads.R;

public class AboutUsFragment extends Fragment {
    View view;
    GridView gridView;
    ArrayList<Moderator> moderators = new ArrayList<>();
    ModeratorsAdapter adapter;

    public AboutUsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_moderator, container, false);
        gridView = view.findViewById(R.id.gridView);
        initGrid();
        return view;
    }

    public void initGrid() {
        initializeWidgets();
        adapter = new ModeratorsAdapter(getActivity(), moderators);
        gridView.setAdapter(adapter);
    }

    public void initializeWidgets() {
        moderators.add(new Moderator(R.drawable.alnur, "Rysbek Alnur",      getString(R.string.ceo_mds_program), R.color.colorPrimary));
        moderators.add(new Moderator(R.drawable.madi, "Berikkazy Madi",    getString(R.string.ceo_mds_reads), R.color.colorPrimary));
    }
}
