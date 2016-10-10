package repository.cassandraImpl;

import dto.EntryDto;
import model.Entry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import repository.EntryRepository;
import repository.PagingResult;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * CassandraEntryRepositoryImpl test Class
 *
 * @Author Legomin Vitaliy
 */
public class CassandraEntryRepositoryImplTest {

    private EntryRepository repository = new CassandraEntryRepositoryImpl();
    private UUID uuid1, uuid2;

    /**
     * inititializing uuid1 & uuid2 for testing
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        Entry e1 = new Entry("hello", "title", "2016-10-20", "false");
        uuid1 = repository.postEntry(e1);
        Entry e2 = new Entry("hello1", "title1", "2016-10-20", "true");
        uuid2 = repository.postEntry(e2);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testGetEntries() throws Exception {
        repository.setResultPerPage(1);
        PagingResult result = repository.getEntries(null);
        String pagingState = result.getPagingState();
        List<EntryDto> entryList = result.getPagingList();
        Assert.assertEquals(1, entryList.size());

        result = repository.getEntries(pagingState);
        entryList = result.getPagingList();
        Assert.assertEquals(1, entryList.size());
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testGetEntry() throws Exception {
        Entry e1 = repository.getEntry(uuid1);
        Assert.assertEquals(e1.getBody(), "hello");
        Assert.assertEquals(e1.getTitle(), "title");
        e1 = repository.getEntry(uuid2);
        Assert.assertEquals(e1.getBody(), "hello1");
        Assert.assertEquals(e1.getTitle(), "title1");

        Assert.assertEquals(null, repository.getEntry(UUID.randomUUID()));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testPutEntry() throws Exception {
        Entry e1 = new Entry();
        e1.setBody("hello3");
        e1.setTitle("title3");
        e1.setPrivateEntry(true);
        e1.setId(UUID.randomUUID());
        Assert.assertEquals(-1, repository.putEntry(e1));
        e1.setId(uuid1);
        Assert.assertEquals(0, repository.putEntry(e1));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testDeleteEntry() throws Exception {
        Assert.assertEquals(0, repository.deleteEntry(uuid1));
        Assert.assertEquals(-1, repository.deleteEntry(UUID.randomUUID()));
    }
}