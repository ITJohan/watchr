package ipren.watchr.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import ipren.watchr.dataHolders.Movie;
import ipren.watchr.dataHolders.MovieApiService;
import ipren.watchr.dataHolders.MovieList;

/**
 *
 */
public class ListViewModel extends AndroidViewModel {

    public MutableLiveData<List<Movie>> movies = new MutableLiveData<>();
    public MutableLiveData<Boolean> movieLoadError = new MutableLiveData<>();
    public MutableLiveData<Boolean> loading = new MutableLiveData<>();

    private MovieApiService movieService = new MovieApiService();
    // Collects disposable single observers and disposes them
    private CompositeDisposable disposable = new CompositeDisposable();

    // We use AndroidViewModel to get access to a context for storing data
    public ListViewModel(@NonNull Application application) {
        super(application);
    }

    public void refresh() {
        fetchFromRemote();
    }

    /**
     * Fetch movies from API on a new thread, then display it on the main thread
     */
    private void fetchFromRemote() {
        loading.setValue(true);
        disposable.add(
                movieService.getMovies("movie/top_rated?api_key=75e28ea896c86e2d5ef78b91e8500e22&language=en-US&page=1")
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<MovieList>() {
                            @Override
                            public void onSuccess(MovieList movieList) {
                                movies.setValue(movieList.movies);
                                movieLoadError.setValue(false);
                                loading.setValue(false);
                            }

                            @Override
                            public void onError(Throwable e) {
                                movieLoadError.setValue(true);
                                loading.setValue(false);
                                e.printStackTrace();
                            }
                        })
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }

    private void fetchFromDatabase() {

    }
}