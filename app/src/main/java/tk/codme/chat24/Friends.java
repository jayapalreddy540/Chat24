package tk.codme.chat24;

public class Friends {
    public String date;
    public String name,thumb_image,online;
    public Friends(){
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public String getOnline() {
        return online;
    }

    public Friends(String name,String date, String thumb_image, String online) {
        this.date = date;
        this.name=name;
        this.thumb_image = thumb_image;
        this.online = online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }


    public Friends(String date){this.date=date;}
    public String getDate(){ return date;}
    public void setDate(String date){this.date=date;}
}
