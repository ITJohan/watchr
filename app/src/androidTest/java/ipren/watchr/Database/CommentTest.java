package ipren.watchr.Database;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import ipren.watchr.TestUtil.LiveDataTestUtil;
import ipren.watchr.dataHolders.Comment;
import ipren.watchr.dataHolders.Movie;
import ipren.watchr.repository.Database.CommentDao;
import ipren.watchr.repository.Database.MovieDB;
import ipren.watchr.repository.Database.MovieDao;

public class CommentTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    private Movie m = new Movie(1, "testMovie");
    private Comment c = new Comment(0, 1, "username", "comment", "profilePicLink");
    private CommentDao commentDao;
    private MovieDao movieDao;
    private MovieDB db;

    @Before
    public void createDb() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), MovieDB.class).allowMainThreadQueries().build();
        commentDao = db.commentDao();
        movieDao = db.movieDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    private void insertDummyData() {
        movieDao.insert(m);
        commentDao.insert(c);
    }


    @Test
    public void insertTest() throws Exception {
        movieDao.insert(m);
        commentDao.insert(c);
        List<Comment> comments = LiveDataTestUtil.getValue(commentDao.getCommentsFromMovie(1));
        Assert.assertEquals("username", comments.get(0).getUsername());
    }

    @Test
    public void deleteTest() throws Exception {
        insertDummyData();
        movieDao.delete(m);
        List<Comment> comments = LiveDataTestUtil.getValue(commentDao.getCommentsFromMovie(1));
        Assert.assertEquals(0, comments.size());

        insertDummyData();
        commentDao.delete(c);
        Assert.assertEquals(0, LiveDataTestUtil.getValue(commentDao.getCommentsFromMovie(1)).size());
    }

    @Test
    public void updateTest() throws Exception {
        insertDummyData();
        Comment newC = new Comment(0, 1, "newUsername", "comment", "profilePicLink");
        commentDao.update(newC);
        Assert.assertEquals(newC.getUsername(), LiveDataTestUtil.getValue(commentDao.getCommentsFromMovie(1)).get(0).getUsername());
    }

}