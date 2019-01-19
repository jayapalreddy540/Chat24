package tk.codme.chat24;

public class Conv {
        public boolean seen;
        public long timestamp;
        public String thumb_image;

    public Conv(){

        }

        public boolean isSeen() {
            return seen;
        }

        public void setSeen(boolean seen) {
            this.seen = seen;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
        public String getThumb_image() { return thumb_image; }
        public void setThumb_image(String thumb_image) { this.thumb_image = thumb_image; }

        public Conv(boolean seen, long timestamp,String thumb_image) {
            this.seen = seen;
            this.timestamp = timestamp;
            this.thumb_image=thumb_image;
        }
}
