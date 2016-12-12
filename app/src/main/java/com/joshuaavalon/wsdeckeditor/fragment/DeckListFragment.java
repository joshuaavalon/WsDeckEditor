package com.joshuaavalon.wsdeckeditor.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.joshuaavalon.android.view.ContentView;
import com.joshuaavalon.android.view.recyclerview.AnimatedRecyclerAdapter;
import com.joshuaavalon.android.view.recyclerview.BindingViewHolder;
import com.joshuaavalon.android.view.recyclerview.ViewHolderFactory;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.activity.DeckActivity;
import com.joshuaavalon.wsdeckeditor.sdk.card.Card;
import com.joshuaavalon.wsdeckeditor.sdk.deck.Deck;
import com.joshuaavalon.wsdeckeditor.sdk.deck.DeckMeta;
import com.joshuaavalon.wsdeckeditor.task.CircularCardImageLoadTask;
import com.joshuaavalon.wsdeckeditor.util.AnimeUtils;
import com.joshuaavalon.wsdeckeditor.view.CardImageHolder;
import com.joshuaavalon.wsdeckeditor.view.DialogUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@ContentView(R.layout.fragment_deck_list)
public class DeckListFragment extends BaseFragment implements SearchView.OnQueryTextListener {
    private static final int REQ_DECK = 1;
    private RecyclerView recyclerView;
    private AnimatedRecyclerAdapter<DeckMeta> adapter;
    private List<DeckMeta> decks;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        decks = getDeckRepository().meta();
        adapter = new AnimatedRecyclerAdapter<>(decks, new ViewHolderFactory<DeckMeta>() {
            @Override
            protected BindingViewHolder<DeckMeta> createViewHolder(View view, int viewType) {
                return new DeckListViewHolder(view);
            }

            @Override
            protected int getLayoutId(int viewType) {
                return R.layout.list_item_deck;
            }
        });
        recyclerView.setAdapter(adapter);
        if (getPreference().getSwipeRemove()) {
            final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                    new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                        @Override
                        public boolean onMove(final RecyclerView recyclerView,
                                              final RecyclerView.ViewHolder viewHolder,
                                              final RecyclerView.ViewHolder target) {
                            return false;
                        }

                        @Override
                        public void onSwiped(final RecyclerView.ViewHolder viewHolder,
                                             final int direction) {
                            if (!(viewHolder instanceof DeckListViewHolder)) return;
                            final DeckMeta deck = adapter.getModels().get(viewHolder.getAdapterPosition());
                            removeDeck(deck);
                        }
                    });
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }
        setHasOptionsMenu(true);
        initializeFab();
        return view;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getString(R.string.nav_deck_edit);
    }

    private void initializeFab() {
        final FloatingActionButton fab = ButterKnife.findById(getActivity(), R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.showCreateDeckDialog(getContext(), getDeckRepository(),
                        new DialogUtils.CreateDeckCallback() {
                            @Override
                            public void onCreate(@NonNull Deck deck) {
                                refreshDecks();
                            }
                        });
            }
        });
    }

    private void renameDeck(@NonNull final DeckMeta deck) {
        DialogUtils.showRenameDeckDialog(getContext(), deck, getDeckRepository());
    }

    private void removeDeck(@NonNull final DeckMeta deck) {
        getDeckRepository().remove(deck.getId());
        refreshDecks();
    }

    private void refreshDecks() {
        decks = getDeckRepository().meta();
        adapter.setModels(decks);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final List<DeckMeta> filteredModelList = filter(decks, query);
        adapter.setModels(filteredModelList);
        recyclerView.scrollToPosition(0);
        return true;
    }

    private List<DeckMeta> filter(List<DeckMeta> models, String query) {
        query = query.toLowerCase();
        final List<DeckMeta> filteredModelList = new ArrayList<>();
        for (DeckMeta model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQ_DECK) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        refreshDecks();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_deck, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
    }

    class DeckListViewHolder extends BindingViewHolder<DeckMeta> implements CardImageHolder {
        @BindView(R.id.text_view)
        TextView textView;
        @BindView(R.id.image_view)
        ImageView imageView;
        @BindView(R.id.deck_image)
        ImageView deckImageView;
        private String imageName;

        public DeckListViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void bind(final DeckMeta deck) {
            textView.setText(deck.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Intent intent = new Intent(getContext(), DeckActivity.class);
                    final Bundle args = new Bundle();
                    args.putLong(DeckActivity.ARG_ID, deck.getId());
                    intent.putExtras(args);
                    startActivityForResult(intent, REQ_DECK, AnimeUtils.createRevealOption(itemView));
                }
            });
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupMenu popup = new PopupMenu(getContext(), imageView);
                    popup.getMenuInflater().inflate(R.menu.deck_popup, popup.getMenu());
                    popup.setOnMenuItemClickListener(
                            new PopupMenu.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {
                                    final int id = item.getItemId();
                                    switch (id) {
                                        case R.id.popup_rename:
                                            renameDeck(deck);
                                            break;
                                        case R.id.popup_delete:
                                            removeDeck(deck);
                                            break;
                                    }
                                    return true;
                                }
                            });
                    popup.show();
                }
            });
            deckImageView.setImageDrawable(null);
            imageName = null;
            Card card = null;
            if (deck.getCover() != null) {
                card = getCardRepository().find(deck.getCover());
                if (card != null)
                    imageName = card.getImage();
            }
            new CircularCardImageLoadTask(getCardRepository(), this, card).execute(getResources());
        }

        @Override
        public void setImage(@NonNull Bitmap bitmap) {
            if (deckImageView != null)
                deckImageView.setImageBitmap(bitmap);
        }

        @Override
        public void setImage(@NonNull Drawable drawable) {
            if (deckImageView != null)
                deckImageView.setImageDrawable(drawable);
        }

        @NonNull
        @Override
        public String getImageName() {
            return imageName;
        }
    }
}
