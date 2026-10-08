package com.midland.saloon.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

/**
 * Every entity's id is a UUID made in Java, so Spring Data cannot tell a new
 * row from an old one by looking at the id (it is never null). It then
 * merges: a SELECT by id before every INSERT - a sale with two services cost
 * some thirty of those. Persistable answers the question instead: an object
 * made with "new" is new until it is persisted, one Hibernate loaded is not.
 *
 * Setting a uid by hand (setUid, or a request body carrying one) marks the
 * object as not new, so it goes through merge exactly as before - the safe
 * way for code that rebuilds an existing row from its id.
 */
@MappedSuperclass
@NoArgsConstructor
@ToString
@Getter
@Setter
public class SupperEntity implements Persistable<String> {
    @Id
    @Column(name = "uid", nullable = false, unique = true)
    @Setter(AccessLevel.NONE)
    private String uid = java.util.UUID.randomUUID().toString();

    /** Not a column and not in any JSON - only whether save() should persist or merge. */
    @Transient
    @ToString.Exclude
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private transient boolean newEntity = true;

    public SupperEntity(String uid) {
        setUid(uid);
    }

    /** An explicit id means "this row may already exist" - save() merges it, as it always did. */
    public void setUid(String uid) {
        this.uid = uid;
        this.newEntity = false;
    }

    @Override
    @JsonIgnore
    public String getId() {
        return uid;
    }

    @Override
    @JsonIgnore
    public boolean isNew() {
        return newEntity;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.newEntity = false;
    }
}
