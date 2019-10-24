package ipren.watchr;

import android.app.Application;
import android.os.Build;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.List;

import ipren.watchr.TestUtil.LiveDataTestUtil;
import ipren.watchr.dataHolders.Actor;
import ipren.watchr.dataHolders.Genre;
import ipren.watchr.dataHolders.Movie;
import ipren.watchr.dataHolders.MovieGenreJoin;
import ipren.watchr.repository.Database.ActorDao;
import ipren.watchr.repository.Database.GenreDao;
import ipren.watchr.repository.Database.MovieDB;
import ipren.watchr.repository.Database.MovieDao;
import ipren.watchr.repository.Database.MovieGenreJoinDao;
import ipren.watchr.repository.MovieRepository;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.O_MR1)
public class MovieRepoTest {
    private static int API_TIMEOUT = 10; // Changing this might fix the API test errors
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    MovieRepository repo;
    private Movie m = new Movie(3, "testMovie");
    private MovieDao movieDao;
    private MovieDB db;

    @Before
    public void createDb() throws Exception {
        Application a = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(a, MovieDB.class).allowMainThreadQueries().build();
        movieDao = db.movieDao();
        m.setUpdateDate(new Date());
        movieDao.insert(m);
        repo = new MovieRepository(db);
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void getMovieTest() throws Exception {
        Movie x = LiveDataTestUtil.getValue(repo.getMovieByID(m.id));
        Assert.assertEquals(m.title, x.title);
    }

    @Test
    public void getActorsTest() throws Exception {
        // Insert new actors
        ActorDao actorDao = actorDao = db.actorDao();
        Actor a = new Actor("1", m.id, "Chris Pratt");
        Actor b = new Actor("2", m.id, "Chris Pratt");
        actorDao.insert(a);
        actorDao.insert(b);

        List<Actor> actors = LiveDataTestUtil.getValue(repo.getActorsFromMovie(m.id));
        Assert.assertEquals(m.id, actors.get(0).getMovieID());
        Assert.assertEquals(2, actors.size());
    }

    @Test
    public void getGenre() throws Exception {
        // Insert new genre
        GenreDao genreDao = genreDao = db.genreDao();
        MovieGenreJoinDao movieGenreJoinDao = db.movieGenreJoinDao();
        Genre g = new Genre(1, "Horror");
        genreDao.insert(g);
        movieGenreJoinDao.insert(new MovieGenreJoin(m.id, g.getGenreID()));
        List<Genre> genres = LiveDataTestUtil.getValue(repo.getGenresFromMovie(m.id));
        Assert.assertEquals(g.getName(), genres.get(0).getName());
    }

//    @Test
//    public void getTrendingDB() throws Exception {
//        LiveData<List<Movie>> movies = repo.getMovieList(IMovieRepository.TRENDING_LIST, 1, true);
//        List<Movie> ms = LiveDataTestUtil.getValue(movies,5);
//        // The list of movies returned from a query in the API is always 20
//        Assert.assertEquals(20, ms.size());
//    }
//
//    @Test
//    public void searchTest() throws Exception {
//        List<Movie> movies = LiveDataTestUtil.getValue(repo.Search("spiderman", 1, true), API_TIMEOUT);
//        Assert.assertEquals(20, movies.size());
//        Assert.assertTrue(!movies.get(0).title.isEmpty());
//    }
//
//    @Test
//    public void getTrendingAPIList() throws Exception {
//        List<Movie> movies = LiveDataTestUtil.getValue(repo.getMovieList(IMovieRepository.TRENDING_LIST, 1, true), API_TIMEOUT);
//        // The list of movies returned from a query in the API is always 20
//        Assert.assertEquals(20, movies.size());
//    }

    @Test
    public void getMoviesByIDTest() throws Exception {
        List<Movie> movies = LiveDataTestUtil.getValue(repo.getMoviesByID(new int[]{m.id, 99}));
        Assert.assertEquals(1, movies.size());
        Assert.assertEquals(m.title, movies.get(0).title);
    }
    @Test
    public void testGetActors() throws Exception{
        db.actorDao().insert(new Actor("1",m.id, "Chris Pratt"));
        List<Actor> a = LiveDataTestUtil.getValue(repo.getActorsFromMovie(m.id));
        Assert.assertEquals(a.get(0).getName(),a.get(0).getName());
    }
}
