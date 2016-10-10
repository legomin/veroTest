package repository;

import dto.EntryDto;

import java.io.Serializable;
import java.util.List;

/**
 * Class for returning result of paging list
 *
 * @Author Legomin Vitaliy
 */
public class PagingResult implements Serializable{
    public String pagingState;
    public List<EntryDto> pagingList;

    public PagingResult(String pagingState, List<EntryDto> pagingList) {
        this.pagingState = pagingState;
        this.pagingList = pagingList;
    }

    public PagingResult() {
    }

    public void setPagingState(String pagingState) {
        this.pagingState = pagingState;
    }

    public void setPagingList(List<EntryDto> pagingList) {
        this.pagingList = pagingList;
    }

    /**
     * @return paging state for next page
     */
    public String getPagingState() {
        return pagingState;
    }

    /**
     * @return list of current paging state
     */
    public List<EntryDto> getPagingList() {
        return pagingList;
    }

}
