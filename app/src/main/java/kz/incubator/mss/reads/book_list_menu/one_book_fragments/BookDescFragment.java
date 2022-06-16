package kz.incubator.mss.reads.book_list_menu.one_book_fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import kz.incubator.mss.reads.R;
import kz.incubator.mss.reads.book_list_menu.activities.OneBookAcvitiy;

public class BookDescFragment extends Fragment {

    static TextView bookDesc;
    View view;
    OneBookAcvitiy oneBookAcvitiy;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_book_desc, container, false);
        initialize();
        return view;
    }

    public void initialize(){
        bookDesc = view.findViewById(R.id.bookDesc);
        oneBookAcvitiy = new OneBookAcvitiy();

        bookDesc.setText(oneBookAcvitiy.getBookDesc());
    }

    public static void setDesc(String desc){
        bookDesc.setText(desc);
    }
}


