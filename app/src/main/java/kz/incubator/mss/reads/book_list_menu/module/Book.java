package kz.incubator.mss.reads.book_list_menu.module;


import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

import java.util.Comparator;

@IgnoreExtraProperties
public class Book implements Serializable {
    String firebaseKey;
    String name;
    String author;
    String desc;
    int page_number;
    String rating;
    String photo;
    String reserved;
    String qr_code;
    String imgStorageName;

    public Book() {

    }

    public Book(String idNumber, String author, String name) {
        this.firebaseKey = idNumber;
        this.author = author;
        this.name = name;
    }

    public Book(String firebaseKey, String name, String author, String desc, int page_number, String rating, String photo, String reserved, String qr_code, String imgStorageName) {
        this.firebaseKey = firebaseKey;
        this.name = name;
        this.author = author;
        this.desc = desc;
        this.page_number = page_number;
        this.rating = rating;
        this.photo = photo;
        this.reserved = reserved;
        this.qr_code = qr_code;
        this.imgStorageName = imgStorageName;
    }

    public static Comparator<Book> bookNameComprator = new Comparator<Book>() {

        public int compare(Book s1, Book s2) {
            String bookName1 = s1.getName().toUpperCase();
            String bookName2 = s2.getName().toUpperCase();

            //ascending order
            return bookName1.compareTo(bookName2);

            //descending order
            //return StudentName2.compareTo(StudentName1);
        }
    };

    public static Comparator<Book> bookAuthorComprator = new Comparator<Book>() {

        public int compare(Book s1, Book s2) {
            String bookAuthor1 = s1.getAuthor().toUpperCase();
            String bookAuthor2 = s2.getAuthor().toUpperCase();

            //ascending order
            return bookAuthor1.compareTo(bookAuthor2);

            //descending order
            //return StudentName2.compareTo(StudentName1);
        }
    };

    public static Comparator<Book> ratingComparator = new Comparator<Book>() {

        public int compare(Book s1, Book s2) {

            String rating1 = s1.getRating();
            String rating2 = s2.getRating();

	   /*For ascending order*/
            return rating2.compareTo(rating1);
//            return rollno1-rollno2;

	   /*For descending order*/
            //rollno2-rollno1;
        }
    };

    public String getImgStorageName() {
        return imgStorageName;
    }

    public void setImgStorageName(String imgStorageName) {
        this.imgStorageName = imgStorageName;
    }

    public String getQr_code() {
        return qr_code;
    }

    public void setQr_code(String qr_code) {
        this.qr_code = qr_code;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getPage_number() {
        return page_number;
    }

    public void setPage_number(int page_number) {
        this.page_number = page_number;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
}
