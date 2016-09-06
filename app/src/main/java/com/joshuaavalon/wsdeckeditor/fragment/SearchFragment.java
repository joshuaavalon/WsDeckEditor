package com.joshuaavalon.wsdeckeditor.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Optional;
import com.joshuaavalon.fluentquery.Condition;
import com.joshuaavalon.wsdeckeditor.Handler;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.StringUtils;
import com.joshuaavalon.wsdeckeditor.activity.Transactable;
import com.joshuaavalon.wsdeckeditor.repository.model.CardFilter;
import com.joshuaavalon.wsdeckeditor.repository.model.CardFilterItem;
import com.joshuaavalon.wsdeckeditor.repository.model.CardFilterItemFactory;
import com.joshuaavalon.wsdeckeditor.view.AnimatedRecyclerAdapter;
import com.joshuaavalon.wsdeckeditor.view.BaseRecyclerViewHolder;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends BaseFragment {
    private static final String ARG_FILTER = "Filter_Key";
    private static final String KEY_FILTER = "filters";
    private ConditionAdapter adapter;
    private ArrayList<CardFilterItem> filters;

    @NonNull
    public static SearchFragment newInstance(@Nullable final ArrayList<CardFilterItem> filters) {
        final SearchFragment fragment = new SearchFragment();
        if (filters != null) {
            final Bundle args = new Bundle();
            args.putParcelableArrayList(ARG_FILTER, filters);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search, container, false);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (savedInstanceState != null)
            filters = savedInstanceState.getParcelableArrayList(KEY_FILTER);
        if (filters == null)
            if (getArguments() != null)
                filters = getArguments().getParcelableArrayList(ARG_FILTER);
        if (filters == null)
            filters = new ArrayList<>();
        adapter = new ConditionAdapter(filters);
        recyclerView.setAdapter(adapter);
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
                        if (!(viewHolder instanceof ConditionViewHolder)) return;
                        filters.remove(viewHolder.getAdapterPosition());
                        adapter.setModels(new ArrayList<>(filters));
                    }
                });
        itemTouchHelper.attachToRecyclerView(recyclerView);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_FILTER, filters);
    }

    private void submit() {
        if (!(getActivity() instanceof Transactable)) return;
        final Transactable transactable = (Transactable) getActivity();
        transactable.transactTo(CardListFragment.newInstance(new CardFilter(filters)));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_search:
                submit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (fab == null) return;
        fab.show();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConditionDialog();
            }
        });
        fab.setImageResource(R.drawable.ic_add_white_24dp);
    }

    @Override
    public void onPause() {
        super.onPause();
        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (fab != null)
            fab.hide();
    }

    private void showConditionDialog() {
        new MaterialDialog.Builder(getContext())
                .title("Choose one")
                .items(StringUtils.getStringResourceList(CardFilterItemFactory.CardFilterItemType.class))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        showCardFilterItemDialog(CardFilterItemFactory.CardFilterItemType.values()[position]);
                    }
                })
                .show();
    }

    private void showCardFilterItemDialog(@NonNull final CardFilterItemFactory.CardFilterItemType type) {
        final CardFilterItem filterItem = CardFilterItemFactory.createFilterItem(type);
        filterItem.getDialog(getContext(), new Handler<Void>() {
            @Override
            public void handle(Void object) {
                final Optional<Condition> conditionOptional = filterItem.toCondition();
                if (!conditionOptional.isPresent() || filters.contains(filterItem)) return;
                filters.add(filterItem);
                adapter.setModels(new ArrayList<>(filters));
            }
        });
    }

    private static class ConditionViewHolder extends BaseRecyclerViewHolder<CardFilterItem> {
        @NonNull
        private final TextView titleTextView;
        @NonNull
        private final TextView contentTextView;

        public ConditionViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
            contentTextView = (TextView) itemView.findViewById(R.id.content_text_view);
        }

        @Override
        public void bind(CardFilterItem obj) {
            titleTextView.setText(obj.getTitle());
            contentTextView.setText(obj.getContent());
        }
    }

    private static class ConditionAdapter extends AnimatedRecyclerAdapter<CardFilterItem, ConditionViewHolder> {
        protected ConditionAdapter(@NonNull final List<CardFilterItem> models) {
            super(models);
        }

        @Override
        public ConditionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_condition, parent, false);
            return new ConditionViewHolder(view);
        }
    }
}
