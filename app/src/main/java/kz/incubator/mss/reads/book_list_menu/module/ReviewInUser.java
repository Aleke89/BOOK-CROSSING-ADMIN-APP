package kz.incubator.mss.reads.book_list_menu.module;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class ReviewInUser implements Serializable {
    String fKey;
    String book_id;
    String review_text;
    int user_rate;
    int admin_rate;

    public ReviewInUser() {

    }

    public ReviewInUser(String fKey, String book_id, int user_rate, String review_text, int admin_rate) {
        this.fKey = fKey;
        this.book_id = book_id;
        this.review_text = review_text;
        this.user_rate = user_rate;
        this.admin_rate = admin_rate;
    }

    public int getAdmin_rate() {
        return admin_rate;
    }

    public void setAdmin_rate(int admin_rate) {
        this.admin_rate = admin_rate;
    }

    public int getUser_rate() {
        return user_rate;
    }

    public void setUser_rate(int user_rate) {
        this.user_rate = user_rate;
    }

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    public String getfKey() {
        return fKey;
    }

    public void setfKey(String fKey) {
        this.fKey = fKey;
    }

    public String getReview_text() {
        return review_text;
    }

    public void setReview_text(String review_text) {
        this.review_text = review_text;
    }
}
