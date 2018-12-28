package tk.codme.chat24;

import android.app.Notification;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> mMessagesList;
    public MessageAdapter(List<Messages> mMessagesList){
        this.mMessagesList=mMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v=LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Messages c =mMessagesList.get(position);
        holder.messageText.setText(c.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText,timeText;
        public ImageView profileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText=(TextView)itemView.findViewById(R.id.message_text_layout);
            timeText=(TextView)itemView.findViewById(R.id.time_text_layout);
            profileImage=(ImageView)itemView.findViewById(R.id.message_image_layout);
        }

    }
}
