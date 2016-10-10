package dto;

import model.Entry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Dto class for main entity
 * Objects of this class return to user
 *
 * @Author Legomin Vitaliy
 */
public class EntryDto {
    private String body;

    private String title;

    private Date expires;

    private Date creationDate;

    public EntryDto(String body, String title, Date expires, Date creationDate) {
        this.body = body;
        this.title = title;
        this.expires = expires;
        this.creationDate = creationDate;
    }

    public String getBody() {
        return body;
    }

    public String getTitle() {
        return title;
    }

    public Date getExpires() {
        return expires;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * function for transformation Entries list to EntryDtos list
     *
     * @param entryList
     * @return list of EntryDto objects
     */
    public static List<EntryDto> getListFromEntyList(List<Entry> entryList) {
        List<EntryDto> result = new ArrayList<>();

        for (Entry e : entryList) {
            result.add(new EntryDto(e.getBody(), e.getTitle(), e.getExpires(), e.getCreationDate()));
        }
        return result;
    }

}

