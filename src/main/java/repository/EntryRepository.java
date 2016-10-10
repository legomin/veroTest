package repository;

import model.Entry;

import java.util.List;
import java.util.UUID;

/**
 * Repository for work with main entity
 *
 * @Author Legomin Vitaliy
 */
public interface EntryRepository {
    /**
     * function for posting entity to db
     *
     * @param entry
     * @return UUID of posted entity
     */
    UUID postEntry(Entry entry);

    /**
     * function getting paging list of Entries (DTO & paging state)
     *
     * @param pagingState current paging state
     * @return list of EntryDto of current page, next page paging state
     */
    PagingResult getEntries(String pagingState);

    /**
     * get Entry by id
     *
     * @param entyId
     * @return
     */
    Entry getEntry(UUID entyId);

    /**
     * put entry to db
     *
     * @param entry
     * @return -1, if unsuccess, 0 if success
     */
    int putEntry(Entry entry);

    /**
     * delete entry from db
     *
     * @param entryId
     * @return -1, if unsuccess, 0 if success
     */
    int deleteEntry(UUID entryId);

    /**
     * set count records per page
     *
     * @param resultPerPage
     */
    void setResultPerPage(int resultPerPage);

    /**
     * get count records per page
     *
     * @return resultPerPage
     */
    int getResultPerPage();
}
