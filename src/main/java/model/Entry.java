package model;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * main entity class
 *
 * @Author Legomin Vitaliy
 */
@Table(keyspace = "main", name = "entry",
        readConsistency = "QUORUM",
        writeConsistency = "QUORUM",
        caseSensitiveKeyspace = false,
        caseSensitiveTable = false)
public class Entry {

    @Column(name = "body")
    private String body;

    @Column(name = "title")
    private String title;

    @Column(name = "expires")
    private Date expires;

    @PartitionKey(0)
    @Column(name = "privateEntry")
    private Boolean privateEntry;

    @Column(name = "secret")
    private UUID id;

    @PartitionKey(1)
    @Column(name = "creationDate")
    private Date creationDate;

    public Entry(String body, String title, Date expires, Boolean privateEntry) {
        this.body = body;
        this.title = title;
        this.expires = expires;
        this.privateEntry = privateEntry == null ? false : privateEntry;
        this.creationDate = new Date();
    }

    public Entry(String body, String title, String expires, String privateEntry) {
        this.body = body;
        this.title = title;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (expires != null) {
            try {
                this.expires = dateFormat.parse(expires);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Boolean privateE = Boolean.parseBoolean(privateEntry);
        this.privateEntry = privateE == null ? false : privateE;
        this.creationDate = new Date();
    }


    public Entry() {
        this.creationDate = new Date();
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public Boolean getPrivateEntry() {
        return privateEntry;
    }

    public void setPrivateEntry(Boolean privateEntry) {
        this.privateEntry = privateEntry;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
