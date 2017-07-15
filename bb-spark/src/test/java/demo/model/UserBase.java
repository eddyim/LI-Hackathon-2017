package demo.model;

import java.util.*;

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
                               List<String> dietaryRestrictions, List<String> foodPreferences,
                                List<String> experiencePreferences, List<String> teamPreferences) {
        User toAdd = new User(firstName, lastName, team, experienceLevel,
                dietaryRestrictions, foodPreferences, experiencePreferences, teamPreferences);
        USER_LIST.add(toAdd);
        return toAdd;
    }

    public static void addAccount(String username, String password, String firstName, String lastName,
                                  String team, String experienceLevel, List<String> dietaryRestrictions,
                                  List<String> foodPreferences, List<String> experiencePreferences,
                                  List<String> teamPreferences) {
        if (ACCOUNTS.containsKey(username)) {
            throw new UserRuntimeException("ERROR: Username " + username + "already exists.");
        }
        User currentUser = addUser(firstName, lastName, team, experienceLevel,
                dietaryRestrictions, foodPreferences, experiencePreferences, teamPreferences);
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

    public static void addDummyData() {
        addAccount("edwardim", "ardin1", "Edward", "Im",
                "Product Development", "Intern", new ArrayList<String>(),
                new ArrayList<String>(Arrays.asList("Chinese", "Korean", "Mexican", "Italian")),
                new ArrayList<String>(Arrays.asList("Intern", "Middle Management", "Senior Management")),
                new ArrayList<String>(Arrays.asList("Product Development", "Sales", "Marketing")));
        addUser("Sonia", "Uppal", "Product Development", "Intern",
                new ArrayList<String>(Arrays.asList("Beef")),
                new ArrayList<String>(Arrays.asList("Mexican", "Japanese", "Indian", "Thai")),
                new ArrayList<String>(Arrays.asList("Intern", "Low Management", "Contractor")),
                new ArrayList<String>(Arrays.asList("Product Development", "Sales", "Human Resources")));
        addUser("Ardin", "Lo", "Marketing", "Intern", new ArrayList<String>(),
                new ArrayList<String>(Arrays.asList("Chinese", "Japanese", "Korean", "Thai")),
                new ArrayList<String>(Arrays.asList("Intern", "Middle Management", "Senior Management")),
                new ArrayList<String>(Arrays.asList("Marketing")));
        addUser("Christine", "Deng", "Marketing", "Intern", new ArrayList<String>(),
                new ArrayList<String>(Arrays.asList("American", "Chinese", "Japanese", "Korean", "Thai")),
                new ArrayList<String>(Arrays.asList("Intern", "Contractor", "Senior Management")),
                new ArrayList<String>(Arrays.asList("Product Development", "Sales", "Marketing")));
        addUser("Melissa", "Cai", "Finance", "Intern", new ArrayList<String>(),
                new ArrayList<>(Arrays.asList("American", "Chinese", "Japanese", "Korean")),
                new ArrayList<String>(Arrays.asList("Intern", "Middle Management", "Senior Management")),
                new ArrayList<String>(Arrays.asList("Product Development", "Sales", "Marketing")));
    }

    public static List<UserPairing> getTopPairings(User user) {
        List<UserPairing> pairings = new ArrayList<UserPairing>();
        for (User u: USER_LIST) {
            if ((!user.getFirstName().equals(u.getFirstName())) || (!user.getLastName().equals(u.getLastName()))) {
                pairings.add(new UserPairing(user, u));
            }
        }
        Collections.sort(pairings, Collections.reverseOrder());
        return pairings;
    }
}
