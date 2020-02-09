package com.ksu.serene.Controller.Homepage.Drafts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ksu.serene.Model.TextDraft;
import com.ksu.serene.Model.Medicine;
import com.ksu.serene.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
public class textDraftAdapter extends RecyclerView.Adapter<textDraftAdapter.MyViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(TextDraft item);
    }

    private List<TextDraft> mAdapter;
    //private final OnItemClickListener listener;

    public textDraftAdapter(List<TextDraft> mAdapter){//, OnItemClickListener listener) {
        //super(options);
        this.mAdapter = mAdapter;
        //this.listener = listener;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView Title;
        TextView Subj;

        public MyViewHolder (View v) {
            super(v);
            Title = (TextView) itemView.findViewById(R.id.text_title_name);
            Subj = (TextView) itemView.findViewById(R.id.text_draft_sub);
        }

        public void bind (final TextDraft textDraft , final textDraftAdapter.OnItemClickListener listener){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(textDraft);
                }
            });
        }
    }

    @NonNull
    @Override
    public textDraftAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.medicine_row , parent , false);
        return new textDraftAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull textDraftAdapter.MyViewHolder holder, int position) {
        TextDraft textDraft = mAdapter.get(position);
        holder.Title.setText(textDraft.getTitle());
        holder.Subj.setText(textDraft.getMessage());
    }

    @Override
    public int getItemCount() {
        return mAdapter.size();
    }


}
