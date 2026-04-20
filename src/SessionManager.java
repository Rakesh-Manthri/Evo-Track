public class SessionManager {
    private static int loggedInUserId = -1;
    private static String loggedInName = null;
    private static String loggedInEmail = null;
    private static Integer loggedInOrgId = null;
    private static String accountType = null;
    private static String role = null;

    public static void setSession(int userId, String name, String email, Integer orgId, String type, String userRole) {
        loggedInUserId = userId;
        loggedInName = name;
        loggedInEmail = email;
        loggedInOrgId = orgId;
        accountType = type;
        role = userRole;
    }

    public static void clearSession() {
        loggedInUserId = -1;
        loggedInName = null;
        loggedInEmail = null;
        loggedInOrgId = null;
        accountType = null;
        role = null;
    }

    public static int getUserId() {
        return loggedInUserId;
    }

    public static String getName() {
        return loggedInName;
    }

    public static String getEmail() {
        return loggedInEmail;
    }
    
    public static Integer getOrgId() {
        return loggedInOrgId;
    }
    
    public static String getAccountType() {
        return accountType;
    }
    
    public static String getRole() {
        return role;
    }

    public static boolean isLoggedIn() {
        return loggedInUserId != -1;
    }
}
