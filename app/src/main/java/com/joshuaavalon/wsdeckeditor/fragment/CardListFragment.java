package com.joshuaavalon.wsdeckeditor.fragment;


import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.activity.CardViewActivity;
import com.joshuaavalon.wsdeckeditor.model.Card;
import com.joshuaavalon.wsdeckeditor.repository.CardRepository;
import com.joshuaavalon.wsdeckeditor.view.ActionModeListener;
import com.joshuaavalon.wsdeckeditor.view.BaseRecyclerViewHolder;
import com.joshuaavalon.wsdeckeditor.view.SelectableAdapter;

import java.util.ArrayList;
import java.util.List;

public class CardListFragment extends Fragment {
    private static final String ARG_FILTER = "filter";

    private List<Card> resultCards;
    private CardRecyclerViewAdapter adapter;
    private ActionModeCallback actionModeCallback;
    @Nullable
    private ActionMode actionMode;

    @NonNull
    public static CardListFragment newInstance(@NonNull final CardRepository.Filter filter) {
        final CardListFragment fragment = new CardListFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_FILTER, filter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resultCards = new ArrayList<>();
        actionModeCallback = new ActionModeCallback();

        final Bundle args = getArguments();
        if (args == null) return;
        final CardRepository.Filter filter = args.getParcelable(ARG_FILTER);
        if (filter != null)
            resultCards.addAll(CardRepository.getCards(filter));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_card_list, container, false);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        adapter = new CardRecyclerViewAdapter(resultCards, new ActionModeListener() {
            @Override
            public void onItemClicked(final int position) {
                toggleSelection(position);
            }

            @Override
            public boolean onItemLongClicked(final int position) {
                if (!startActionMode()) return false;
                toggleSelection(position);
                return true;
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    private void toggleSelection(final int position) {
        if (actionMode == null) return;
        adapter.toggleSelection(position);
        int count = adapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    private boolean startActionMode() {
        final Activity parentActivity = getActivity();
        if (!(parentActivity instanceof AppCompatActivity)) return false;
        actionMode = ((AppCompatActivity) parentActivity).startSupportActionMode(actionModeCallback);
        return true;
    }

    private class CardRecyclerViewAdapter extends SelectableAdapter<Card, CardViewHolder> {
        @Nullable
        private final ActionModeListener actionModeListener;

        public CardRecyclerViewAdapter(@NonNull final List<Card> items,
                                       @Nullable final ActionModeListener actionModeListener) {
            super(items);
            this.actionModeListener = actionModeListener;
        }

        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_card, parent, false);
            return new CardViewHolder(view, actionModeListener);
        }

        @Override
        public void onBindViewHolder(CardViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            holder.linearLayout.setActivated(isSelected(position));
        }
    }

    private class CardViewHolder extends BaseRecyclerViewHolder<Card> implements View.OnLongClickListener {
        @NonNull
        private final ImageView imageView;
        @NonNull
        private final TextView nameTextView;
        @NonNull
        private final TextView serialTextView;
        @NonNull
        private final View itemView;
        private final LinearLayout linearLayout;
        @Nullable
        private final ActionModeListener actionModeListener;

        public CardViewHolder(@NonNull final View itemView, @Nullable final ActionModeListener actionModeListener) {
            super(itemView);
            this.itemView = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.card_image);
            nameTextView = (TextView) itemView.findViewById(R.id.card_name);
            serialTextView = (TextView) itemView.findViewById(R.id.card_serial);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.card_background);
            itemView.setOnLongClickListener(this);
            this.actionModeListener = actionModeListener;
        }

        @Override
        public void bind(final Card card) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionMode == null) {
/*
                        if (!(getActivity() instanceof Transactable)) return;
                        final Transactable transactable = (Transactable) getActivity();
                        final CardDetailFragment fragment = CardDetailFragment.newInstance(card.getSerial());
                        transactable.transactTo(fragment);
                        */
                        CardViewActivity.start(getContext(), Lists.newArrayList(
                                Iterables.transform(resultCards, new Function<Card, String>() {
                                    @Override
                                    public String apply(Card input) {
                                        return input.getSerial();
                                    }
                                })));
                    } else {
                        if (actionModeListener == null) return;
                        actionModeListener.onItemClicked(getAdapterPosition());
                    }
                }
            });
            final Bitmap bitmap = CardRepository.getImage(card.getImage(), card.getType());
            imageView.setImageBitmap(bitmap);
            nameTextView.setText(card.getName());
            serialTextView.setText(card.getSerial());
        }

        @Override
        public boolean onLongClick(View view) {
            return actionModeListener != null &&
                    actionModeListener.onItemLongClicked(getAdapterPosition());
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(final ActionMode actionMode, final Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.menu_cardlist_actionmode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(final ActionMode actionMode, final Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode actionMode, final MenuItem item) {
            switch (item.getItemId()) {
                case R.id.clear_all:
                    actionMode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(final ActionMode actionMode) {
            adapter.clearSelection();
            CardListFragment.this.actionMode = null;
        }
    }
}
