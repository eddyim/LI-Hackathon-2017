package demo.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by edwardim on 7/15/17.
 */
public class UserBase {
    private static final List<User> USER_LIST = new ArrayList<>();
    private static final Map<String, Account> ACCOUNTS = new HashMap<>();
    static class Account {
        private String _username;
        private String _password;
        private User _user;

        Account(String name, String pass, User user) {
            if (user.accountCreated()) {
                throw new UserRuntimeException("User has already been created");
            }
            _username = name;
            _password = pass;
            _user = user;
            user.changeAccountStatus();
        }

        public String getUserName() {
            return _username;
        }

        public String getPassWord() {
            return _password;
        }

        public User getUser() {
            return _user;
        }
    }

    public static List<User> getAllUsers() {
        return USER_LIST;
    }

    private static User addUser(User user) {
        USER_LIST.add(user);
        return user;
    }

    private static User addUser(String firstName, String lastName, String team, String experienceLevel,
                               List<String> dietaryRestrictions, List<String> foodPreferences) {
        User toAdd = new User(firstName, lastName, team, experienceLevel, dietaryRestrictions, foodPreferences);
        USER_LIST.add(toAdd);
        return toAdd;
    }

    public static void addAccount(String username, String password, String firstName, String lastName,
                                  String team, String experienceLevel, List<String> dietaryRestrictions,
                                  List<String> foodPreferences) {
        if (ACCOUNTS.containsKey(username)) {
            throw new UserRuntimeException("ERROR: Username " + username + "already exists.");
        }
        User currentUser = addUser(firstName, lastName, team, experienceLevel, dietaryRestrictions, foodPreferences);
        Account currentAccount = new Account(username, password, currentUser);
        ACCOUNTS.put(username, currentAccount);
    }

    public static Account validateLogin(String username, String password) {
        if (!ACCOUNTS.containsKey(username)) {
            throw new UserRuntimeException("ERROR: Username " + username + "not found.");
        }
        Account toCheck = ACCOUNTS.get(username);
        if (toCheck.getPassWord().equals(password)) {
            return toCheck;
        }
        throw new UserRuntimeException("ERROR: Invalid Password");
    }
}
