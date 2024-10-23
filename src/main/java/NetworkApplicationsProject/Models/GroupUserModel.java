package NetworkApplicationsProject.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class GroupUserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "userId")
    private UserModel userModel;

    @ManyToOne
    @JoinColumn(name = "groupId")
    private GroupModel groupModel;

}