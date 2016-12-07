package com.joshuaavalon.wsdeckeditor.view.recycler.viewholder;

import android.view.View;
import android.widget.TextView;

import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.activity.ResultActivity;

import butterknife.BindView;

public class ExpansionViewHolder extends BaseViewHolder<String> {
    @BindView(R.id.text_view)
    TextView textView;

    public ExpansionViewHolder(final View itemView) {
        super(itemView);
    }

    @Override
    public void bind(final String title) {
        textView.setText(title);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResultActivity.start(itemView.getContext(), title);
            }
        });
    }
}
