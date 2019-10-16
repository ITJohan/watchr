package ipren.watchr.repository;

import android.net.Uri;

import androidx.lifecycle.LiveData;

import com.google.android.gms.tasks.OnCompleteListener;

import ipren.watchr.dataHolders.FireComment;
import ipren.watchr.dataHolders.FireRating;
import ipren.watchr.dataHolders.PublicProfile;
import ipren.watchr.dataHolders.User;

public interface IMainRepository {
    int SEARCH_METHOD_MOVIE_ID = 0;
    int SEARCH_METHOD_USER_ID = 1;
    String FAVORITES_LIST = "Favorites";
    String WATCH_LATER_LIST = "Watch_Later";
    String WATCHED_LIST = "Watched";

    static IMainRepository getMainRepository() {
        return MainRepository.getMainRepository();
    }



    //Firebase auth functions

    LiveData<User> getUserLiveData();

    void registerUser(String email, String password, OnCompleteListener callback);

    void signOutUser();

    void loginUser(String email, String password, OnCompleteListener callback);

    void refreshUsr();

    void reSendVerificationEmail();

    void updateProfile(String userName, Uri pictureUri);

    //Firebase database functions
    LiveData<PublicProfile> getPublicProfile(String user_id);

    LiveData<FireComment[]> getComments(String movie_id, int searchMethod);

    LiveData<FireRating[]> getRatings(String movie_id, int searchMethod);

    void addMovieToList(String list, String movie_id, String user_id, OnCompleteListener callback);

    LiveData<String[]> getMovieList(String list, String user_id);

    void removeMovieFromList(String list, String movie_id, String user_id, OnCompleteListener callback);

    void rateMovie(int score, String movie_id, String user_id, OnCompleteListener callback);

    void removeRating(String rating_id, OnCompleteListener callback);

    void commentMovie(String text, String movie_id, String user_id, OnCompleteListener callback);

    void removeComment(String comment_id, OnCompleteListener callback);


}
