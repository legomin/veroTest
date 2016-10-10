package repository.cassandraImpl;

import com.datastax.driver.core.*;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import dto.EntryDto;
import model.Entry;
import repository.EntryRepository;
import repository.PagingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of EntryRepository for Cassandra db
 *
 * @Author Legomin Vitaliy
 */
public class CassandraEntryRepositoryImpl implements EntryRepository {

    private static AtomicLong counter = new AtomicLong(0);

    private int resultPerPage = 100;
    private Session session;
    private Mapper<Entry> mapper;

    /**
     * initialise db connection
     */
    private void init() {
        Cluster cluster = null;
        try {
            cluster = Cluster.builder()
                    .addContactPoint("127.0.0.1")
                    .build();
            session = cluster.connect();

            MappingManager manager = new MappingManager(session);
            mapper = manager.mapper(Entry.class);

        }
        catch (Exception e) {
            e.printStackTrace();
            if (cluster != null) {
                cluster.close();
            }
        }
    }

    public CassandraEntryRepositoryImpl() {
        init();
    }

    /**
     * function for posting entity to db
     *
     * @param entry
     * @return UUID of posted entity
     */
    @Override
    public UUID postEntry(Entry entry) {
        entry.setId(UUID.randomUUID());
        mapper.save(entry);

        return entry.getId();
    }

    /**
     * function getting paging list of Entries (DTO & paging state)
     *
     * @param pagingState current paging state
     * @return list of EntryDto of current page, next page paging state
     */
    @Override
    public PagingResult getEntries(String pagingState) {
        Statement st = new SimpleStatement("SELECT * FROM entry where privateEntry = false order by creationDate desc");
        st.setFetchSize(this.resultPerPage);

        if (pagingState != null) {
            st.setPagingState(
                    PagingState.fromString(pagingState));
        }

        session.execute(new SimpleStatement("USE main"));

        ResultSet results = session.execute(st);

        PagingState nextPage = results.getExecutionInfo().getPagingState();

        Result<Entry> entries = mapper.map(results);

        List<Entry> result = new ArrayList<>();
        int remaining = entries.getAvailableWithoutFetching();

        for (Entry e : entries) {
            result.add(e);
            if (--remaining == 0) {
                break;
            }
        }

        return new PagingResult(nextPage == null ? null : nextPage.toString(), EntryDto.getListFromEntyList(result));
    }

    /**
     * get Entry by id
     *
     * @param entyId
     * @return
     */
    @Override
    public Entry getEntry(UUID entyId) {
        session.execute(new SimpleStatement("USE main"));
        Statement st = new SimpleStatement("SELECT * FROM entry where secret = "+entyId.toString() + "allow filtering");
        ResultSet results = session.execute(st);

        return mapper.map(results).one();
    }

    /**
     * put entry to db
     *
     * @param entry
     * @return -1, if unsuccess, 0 if success
     */
    @Override
    public int putEntry(Entry entry) {
        if (entry.getId() == null || getEntry(entry.getId()) == null) {
            return -1;
        }
        mapper.save(entry);
        return 0;
    }

    /**
     * delete entry from db
     *
     * @param entryId
     * @return -1, if unsuccess, 0 if success
     */
    @Override
    public int deleteEntry(UUID entryId) {
        Entry entry = getEntry(entryId);
        if (entry == null) {
            return -1;
        }

        mapper.delete(entry);
        return 0;
    }

    /**
     * set count records per page
     *
     * @param resultPerPage
     */
    @Override
    public void setResultPerPage(int resultPerPage) {
        this.resultPerPage = resultPerPage;
    }

    /**
     * get count records per page
     *
     * @return resultPerPage
     */
    @Override
    public int getResultPerPage() {
        return this.resultPerPage;
    }
}
