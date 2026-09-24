package com.midland.saloon.Uaa.Model;

import com.midland.saloon.Setting.Model.Branch;
import com.midland.saloon.Setting.Model.Role;
import com.midland.saloon.Utils.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users", indexes = {
        // Every branch-scoped user lookup (sale notifications, staff
        // lists, online users, ...) filters on this column. With
        // ~1000 branches sharing the table, an unindexed lookup here
        // would mean a full scan of the whole users table every time.
        @Index(name = "idx_users_branch_uid", columnList = "branch_uid"),
        // Looked up once per authenticated request to build the principal,
        // so this is the single hottest lookup in the system.
        @Index(name = "idx_users_username", columnList = "username")
})
public class User extends BaseEntity {

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    @JsonIgnore
    private String password;

    @Column(name = "is_root")
    private Boolean isRoot = false;

    @Column(name = "is_blocked")
    private Boolean isBlocked = false;

    /**
     * Set when the account is created, cleared the moment the person chooses
     * their own password. Until then what is stored is the one-time code that
     * travelled to them over SMS - readable by anyone who saw that message,
     * and by whatever carried it. So while this is true the token they hold
     * opens exactly one door: setting a password.
     *
     * Defaults to false, which is what every account that already existed
     * before this was added reads as - they are not dragged through a change
     * they never needed.
     */
    @Column(name = "must_change_password")
    private Boolean mustChangePassword = false;

    /**
     * When the code above stops working. Null on an account that has already
     * set its own password - a password does not expire, only the code does.
     */
    @Column(name = "activation_expires_at")
    private LocalDateTime activationExpiresAt;

    /**
     * Wrong codes entered since the last one was issued. Six digits is a
     * million guesses, which is nothing without this.
     */
    @Column(name = "activation_attempts")
    private Integer activationAttempts = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_uid"),
            inverseJoinColumns = @JoinColumn(name = "role_uid")
    )
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private List<Role> roles;




    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "gender")
    private String gender;

    @Column(name = "email")
    private String email;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_uid")
    private Branch branch;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "profile_image")
    private String profileImage;


}
