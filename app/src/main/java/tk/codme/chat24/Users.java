package tk.codme.chat24;

public class Users {
    public String name;
    public String image;
    public String status;
    public String thumb_image;
    public String online;


    public Users(){

    }
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thunb_image) {
        this.thumb_image = thumb_image;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }
   // public String getOnline() {return online; }

   // public void setOnline(String online) { this.online = online; }

    public Users(String name, String image, String status,String online) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumb_image=thumb_image;
       // this.online=online;
    }

    public void setName(String name) {
        this.name = name;
    }

}
