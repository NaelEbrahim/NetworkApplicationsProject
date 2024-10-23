package NetworkApplicationsProject.Models;

import NetworkApplicationsProject.Enums.GenderEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String userName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private GenderEnum gender;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModified;

    @JsonIgnore
    @OneToMany(mappedBy = "userModel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupUserModel> userGroups;

    @JsonIgnore
    @OneToMany(mappedBy = "groupOwner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupModel> userCreatedGroups;

    @JsonIgnore
    @OneToMany(mappedBy = "userModel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityModel> userActivities;

    @JsonIgnore
    @OneToMany(mappedBy = "fileOwner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileModel> userFiles;

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<TokenModel> tokenModel;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "roleId")
    private RoleModel roleModel;

}