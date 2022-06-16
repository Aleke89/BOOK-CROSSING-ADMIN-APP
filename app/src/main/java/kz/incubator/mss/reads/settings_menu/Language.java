package kz.incubator.mss.reads.settings_menu;

public class Language {
    public int image;
    public String name;
    public String identificator;

    public Language(int image, String name, String identificator) {
        this.image = image;
        this.name = name;
        this.identificator = identificator;
    }

    public int getImage() {

        return image;
    }

    public String getName() {
        return name;
    }

    public String getIdentificator() {
        return identificator;
    }
}
