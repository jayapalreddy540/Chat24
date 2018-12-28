package tk.codme.chat24;

public class Messages {
    public void Messages(){

    }

    private String message,type,from;
    private long time;
    private boolean seen;
    public Messages(String message, boolean seen, long time, String type, String from){
        this.message=message;
        this.seen=seen;
        this.time=time;
        this.type=type;

        this.from=from;
    }

    public String getMessage() {
        return message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setMessage(String message) {
        this.message = message;

    }


}
