package NetworkApplicationsProject.Tools;

import NetworkApplicationsProject.Enums.RolesEnum;
import NetworkApplicationsProject.Models.UserModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class HandleCurrentUserSession {

    public static UserModel getCurrentUser () {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserModel) authentication.getPrincipal();
    }

    public static RolesEnum getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserModel user = (UserModel) authentication.getPrincipal();
        return user.getRole();
    }

}