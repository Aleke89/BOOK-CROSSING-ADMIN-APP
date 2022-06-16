package kz.incubator.mss.reads.settings_menu;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.book_list_menu.one_book_fragments.RecyclerItemClickListener;

import static kz.incubator.mss.reads.MenuActivity.setTitle;

public class SettingsFragment extends Fragment {
    View view;
    GridView gridView;
    ArrayList<Language> languages = new ArrayList<>();
    LanguageAdapter adapter;
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    String currentLanguage;
    public SettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        setTitle(getString(R.string.menu_change_language));

        initViews();
        currentLanguage = "en";

        return view;
    }

    public void initViews() {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        languages.add(new Language(R.drawable.languages_us_flag, "English", "eng"));
        languages.add(new Language(R.drawable.languages_kz_flag, "Қазақша", "kk"));
        languages.add(new Language(R.drawable.languages_rus_flag, "Русский", "ru"));

        adapter = new LanguageAdapter(getActivity(), languages);

        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int pos) {
                        Toast.makeText(getActivity(), getString(R.string.lang_chaned), Toast.LENGTH_SHORT).show();
                        String lanId = languages.get(pos).getIdentificator();
                        setLocale(lanId);
                        currentLanguage = lanId;
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                })
        );
    }

    public void setLocale(String localeName) {
        if (!localeName.equals(currentLanguage)) {
            Locale myLocale = new Locale(localeName);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);

            getActivity().finish();
            getActivity().startActivity(getActivity().getIntent());
        }
    }

}
