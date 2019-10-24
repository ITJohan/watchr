package ipren.watchr.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ipren.watchr.R;
import ipren.watchr.activities.fragments.Adapters.MovieListAdapter;
import ipren.watchr.dataHolders.Movie;
import ipren.watchr.repository.IMovieRepository;
import ipren.watchr.repository.IUserDataRepository;
import ipren.watchr.viewModels.ListViewModel;

public class MovieListFragment extends Fragment {

    @BindView(R.id.movieList)
    RecyclerView movieList;
    @BindView(R.id.listError)
    TextView listError;
    @BindView(R.id.notLoggedInError)
    TextView notLoggedInError;
    @BindView(R.id.loadingView)
    ProgressBar loadingView;
    @BindView(R.id.refreshLayout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.emptyListView)
    TextView emptyListView;

    private ListViewModel listViewModel;
    private MovieListAdapter movieListAdapter;
    // Observer to update the movieList
    private Observer<List<Movie>> movieObserver = new Observer<List<Movie>>() {
        @Override
        public void onChanged(List<Movie> movies) {
            if (movies != null) {
                movieListAdapter.updateMovieList(movies);
                loadingView.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public MovieListFragment() {
        // Required
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_list, container, false);
        // Bind the layout variables
        ButterKnife.bind(this, view);

        return view;
    }

    /**
     * Sets up the list, decides which list to display.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get view model and set correct data
        listViewModel = ViewModelProviders.of(getActivity()).get(ListViewModel.class);
        String listType = this.getArguments().getString("listType");

        if (listViewModel.getUser().getValue() != null) {
            listViewModel.initMovieIdLists();
        }

        // Set list layout and adapter
        movieList.setLayoutManager(new LinearLayoutManager(getContext()));
        movieListAdapter = new MovieListAdapter(listViewModel, this);
        movieList.setAdapter(movieListAdapter);

        connectFilterButton();
        connectSearchView(listType);

        handleNavigation(listType);

        // Fetch fresh data from API on refresh
        refreshLayout.setOnRefreshListener(() -> {
            handleNavigation(listType);
            refreshLayout.setRefreshing(false);
        });
    }

    private void handleNavigation(String listType) {
        switch (listType) {
            case IMovieRepository.BROWSE_LIST:
                listViewModel.initBrowse();
                break;
            case IMovieRepository.RECOMMENDED_LIST:
                listViewModel.initRecommended();
                break;
            default:
                handleUserList(listType);
                listViewModel.getUser().observe(this, user -> handleUserList(listType));
        }
        listViewModel.getMovies().observe(getActivity(), movieObserver);
    }


    private void handleUserList(String listType) {
        // Check if user is logged in
        if (listViewModel.getUser().getValue() != null) {
            // Get the movie ids from user repo
            switch (listType) {
                case IUserDataRepository.WATCHED_LIST: listViewModel.getWatchedIds().observe(this, this::observeIds); break;
                case IUserDataRepository.WATCH_LATER_LIST: listViewModel.getWatchLaterIds().observe(this, this::observeIds); break;
                case IUserDataRepository.FAVORITES_LIST: listViewModel.getFavoritesIds().observe(this, this::observeIds); break;
            }
        } else {
            // Not logged in
            // TODO: @johan Bugfix: movie list shows instead of not logged in error when user logs out from a list view
            notLoggedInError.setVisibility(View.VISIBLE);
            movieList.setVisibility(View.GONE);
            loadingView.setVisibility(View.GONE);
            emptyListView.setVisibility(View.GONE);
        }
    }

    public void observeIds(String[] ids) {
        if (ids != null) {
            listViewModel.initUserMovieList(ids);
            listViewModel.getMovies().observe(this, movies -> {
                if (movies != null) {
                    movieListAdapter.updateMovieList(movies);
                    if (movies.size() == 0) {
                        emptyListView.setVisibility(View.VISIBLE);
                    } else {
                        movieList.setVisibility(View.VISIBLE);
                    }
                    notLoggedInError.setVisibility(View.GONE);
                    loadingView.setVisibility(View.GONE);
                }
            });
        }
    }

    /**
     * Find and setup the search bar
     */
    private void connectSearchView(String listType) {
        // Get the search view from toolbar and show
        SearchView searchView = getActivity().findViewById(R.id.toolbar_search);
        searchView.setVisibility(View.VISIBLE);

        // Clear text and focus
        searchView.setQuery("", false);
        searchView.clearFocus();

        // Get rid of magnifying glass on keyboard.
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Remove the current observer
                listViewModel.getMovies().removeObserver(movieObserver);
                // Load new movies from a query
                listViewModel.getMoviesFromQuery(query);
                // Observe them
                listViewModel.getMovies().observe(getActivity(), movieObserver);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                movieListAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    /**
     * Find and setup the filter button
     */
    private void connectFilterButton() {
        // Get the filter button from toolbar and show
        ImageButton filterBtn = getActivity().findViewById(R.id.toolbar_filter);
        filterBtn.setVisibility(View.VISIBLE);

        filterBtn.setOnClickListener(v -> {
            DrawerLayout filterDrawer = getActivity().findViewById(R.id.filter_drawer);
            if (filterDrawer.isDrawerOpen(GravityCompat.END)) {
                filterDrawer.closeDrawer(GravityCompat.END);
            } else {
                filterDrawer.openDrawer(GravityCompat.END);
            }
        });
    }
}
