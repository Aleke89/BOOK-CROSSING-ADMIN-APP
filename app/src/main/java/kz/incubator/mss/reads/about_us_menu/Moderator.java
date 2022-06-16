package kz.incubator.mss.reads.about_us_menu;

public class Moderator {
    public int image;
    public String name;
    public String desc;
    public int color;

    public Moderator(int image, String name, String desc, int color) {
        this.image = image;
        this.name = name;
        this.desc = desc;
        this.color = color;
    }

    public int getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public int getColor() {
        return color;
    }
}
