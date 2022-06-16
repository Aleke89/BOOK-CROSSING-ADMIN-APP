package kz.incubator.mss.reads.groups_menu.module;


import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.Comparator;

@IgnoreExtraProperties
public class Groups implements Serializable {
    String group_id;
    String group_name;
    int person_count;
    int sum_point;

    public Groups() {

    }

    public Groups(String group_id, String group_name, int person_count, int sum_point) {
        this.group_id = group_id;
        this.group_name = group_name;
        this.person_count = person_count;
        this.sum_point = sum_point;
    }

    public static Comparator<Groups> groupPlace = new Comparator<Groups>() {

        public int compare(Groups g1, Groups g2) {

            int group1 = g1.getSum_point() / (g1.getPerson_count() == 0?1:g1.getPerson_count());
            int group2 = g2.getSum_point() / (g2.getPerson_count() == 0?1:g2.getPerson_count());
            //ascending order
//            return user1.compareTo(user2);

            return group2 - group1;

            //descending order
            //return StudentName2.compareTo(StudentName1);
        }
    };

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public int getPerson_count() {
        return person_count;
    }

    public void setPerson_count(int person_count) {
        this.person_count = person_count;
    }

    public int getSum_point() {
        return sum_point;
    }

    public void setSum_point(int sum_point) {
        this.sum_point = sum_point;
    }
}
