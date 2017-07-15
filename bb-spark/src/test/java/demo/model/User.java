package demo.model;

import java.util.*;

import static demo.model.User.DietaryRestrictions.*;
import static demo.model.User.ExperienceLevel.*;
import static demo.model.User.FoodPreferences.*;
import static demo.model.User.Team.*;

/**
 * Created by edwardim on 7/14/17.
 */
public class User {
    private static final HashMap<String, FoodPreferences> FOOD_PREFERENCES;
    static {
        FOOD_PREFERENCES = new HashMap<>();
        FOOD_PREFERENCES.put("American", AMERICAN);
        FOOD_PREFERENCES.put("Chinese", CHINESE);
        FOOD_PREFERENCES.put("French", FRENCH);
        FOOD_PREFERENCES.put("Indian", INDIAN);
        FOOD_PREFERENCES.put("Italian", ITALIAN);
        FOOD_PREFERENCES.put("Japanese", JAPANESE);
        FOOD_PREFERENCES.put("Korean", KOREAN);
        FOOD_PREFERENCES.put("Mediterranean", MEDITERRANEAN);
        FOOD_PREFERENCES.put("Mexican", MEXICAN);
        FOOD_PREFERENCES.put("Spanish", SPANISH);
        FOOD_PREFERENCES.put("Thai", THAI);
        FOOD_PREFERENCES.put("Other", FoodPreferences.OTHER);

    }
    private static final HashMap<String, DietaryRestrictions> DIETARY_RESTRICTIONS;
    static {
        DIETARY_RESTRICTIONS = new HashMap<>();
        DIETARY_RESTRICTIONS.put("Beef", BEEF);
        DIETARY_RESTRICTIONS.put("Pork", PORK);
        DIETARY_RESTRICTIONS.put("Poultry", POULTRY);
        DIETARY_RESTRICTIONS.put("Seafood", SEAFOOD);
        DIETARY_RESTRICTIONS.put("Dairy", DAIRY);
        DIETARY_RESTRICTIONS.put("Gluten", GLUTEN);
        DIETARY_RESTRICTIONS.put("Peanut", PEANUT);
        DIETARY_RESTRICTIONS.put("Egg", EGG);
        DIETARY_RESTRICTIONS.put("Wheat", WHEAT);
        DIETARY_RESTRICTIONS.put("Soy", SOY);
        DIETARY_RESTRICTIONS.put("Shellfish", SHELLFISH);
        DIETARY_RESTRICTIONS.put("Other", DietaryRestrictions.OTHER);
    }
    private static final HashMap<String, ExperienceLevel> EXPERIENCE_LEVELS;
    static {
        EXPERIENCE_LEVELS = new HashMap<>();
        EXPERIENCE_LEVELS.put("Intern", INTERN);
        EXPERIENCE_LEVELS.put("Contractor", CONTRACTOR);
        EXPERIENCE_LEVELS.put("Low Management", LOW_MANAGEMENT);
        EXPERIENCE_LEVELS.put("Middle Management", MIDDLE_MANAGEMENT);
        EXPERIENCE_LEVELS.put("Senior Management", SENIOR_MANAGEMENT);
    }
    private static final HashMap<String, Team> TEAMS;
    static {
        TEAMS = new HashMap<>();
        TEAMS.put("Finance", FINANCE);
        TEAMS.put("Human Resources", HUMAN_RESOURCES);
        TEAMS.put("Marketing", MARKETING);
        TEAMS.put("Product Development", SOFTWARE);
        TEAMS.put("Sales", SALES);
    }
    private String _firstName;
    private String _lastName;
    private String _pictureName;
    private Team _team;
    private ExperienceLevel _experienceLevel;
    private List<DietaryRestrictions> _dietaryRestrictions;
    private List<FoodPreferences> _foodPreferences;
    private List<ExperienceLevel> _experiencePreferences;
    private List<Team> _teamPreferences;
    private boolean _accountCreated = false;


    public enum DietaryRestrictions {
        BEEF,
        PORK,
        POULTRY,
        SEAFOOD,
        DAIRY,
        GLUTEN,
        PEANUT,
        EGG,
        WHEAT,
        SOY,
        SHELLFISH,
        OTHER
    }

    public enum FoodPreferences {
        AMERICAN,
        CHINESE,
        FRENCH,
        INDIAN,
        ITALIAN,
        JAPANESE,
        KOREAN,
        MEDITERRANEAN,
        MEXICAN,
        SPANISH,
        THAI,
        OTHER,
    }

    public enum Team {
        FINANCE,
        HUMAN_RESOURCES,
        MARKETING,
        SOFTWARE,
        SALES,
    }

    public enum ExperienceLevel {
        INTERN,
        CONTRACTOR,
        JUNIOR,
        LOW_MANAGEMENT,
        MIDDLE_MANAGEMENT,
        SENIOR_MANAGEMENT,

    }

    public User(String firstName, String lastName, String team, String experienceLevel,
                List<String> dietaryRestrictions, List<String> foodPreferences,
                List<String> experiencePreferences, List<String> teamPreferences, String picName) {
        _firstName = firstName;
        _lastName = lastName;
        _team = parseTeam(team);
        _experienceLevel = parseExperienceLevel(experienceLevel);
        _dietaryRestrictions = parseDietaryRestrictions(dietaryRestrictions);
        _foodPreferences = parseFoodPreferences(foodPreferences);
        _experiencePreferences = parseExperiencePreferences(experiencePreferences);
        _teamPreferences = parseTeamPreferences(teamPreferences);
        _pictureName = picName;
    }

    private Team parseTeam(String team) {
        switch (team) {
            case "Finance":
                return FINANCE;
            case "Human Resources":
                return HUMAN_RESOURCES;
            case "Marketing":
                return MARKETING;
            case "Product Development":
                return SOFTWARE;
            case "Sales":
                return SALES;
            default:
                throw new RuntimeException("Invalid Team type: " + team);
        }
    }

    private ExperienceLevel parseExperienceLevel(String experienceLevel) {
        switch (experienceLevel) {
            case "Intern":
                return INTERN;
            case "Contractor":
                return CONTRACTOR;
            case "Junior":
                return JUNIOR;
            case "Low Management":
                return LOW_MANAGEMENT;
            case "Middle Management":
                return MIDDLE_MANAGEMENT;
            case "Senior Management":
                return SENIOR_MANAGEMENT;
            default:
                throw new RuntimeException("Invalid Experience Level: " + experienceLevel);
        }
    }

    private List<DietaryRestrictions> parseDietaryRestrictions(List<String> restrictions) {
        List<DietaryRestrictions> dietaryRestrictions = new ArrayList<>();
        for (String rest: restrictions) {
            if (DIETARY_RESTRICTIONS.containsKey(rest)) {
                dietaryRestrictions.add(DIETARY_RESTRICTIONS.get(rest));
            }
        }
        return dietaryRestrictions;
    }

    private List<FoodPreferences> parseFoodPreferences(List<String> preferences) {
        List<FoodPreferences> foodPreferences = new ArrayList<>();
        for (String pref: preferences) {
            if (FOOD_PREFERENCES.containsKey(pref)) {
                foodPreferences.add(FOOD_PREFERENCES.get(pref));
            }
        }
        return foodPreferences;
    }

    private List<ExperienceLevel> parseExperiencePreferences(List<String> preferences) {
        List<ExperienceLevel> experiencePreferences = new ArrayList<>();
        for (String pref: preferences) {
            if (EXPERIENCE_LEVELS.containsKey(pref)) {
                experiencePreferences.add(EXPERIENCE_LEVELS.get(pref));
            }
        }
        return experiencePreferences;
    }

    private List<Team> parseTeamPreferences(List<String> preferences) {
        List<Team> teamPreferences = new ArrayList<>();
        for (String pref: preferences) {
            if (TEAMS.containsKey(pref)) {
                teamPreferences.add(TEAMS.get(pref));
            }
        }
        return teamPreferences;
    }

    public String getFirstName() {
        return _firstName;
    }

    public String getLastName() {
        return _lastName;
    }

    public Team getTeam() {
        return _team;
    }

    public ExperienceLevel getExperienceLevel() {
        return _experienceLevel;
    }

    public List<DietaryRestrictions> getDietaryRestriction() {
        return _dietaryRestrictions;
    }

    public List<FoodPreferences> getFoodPreferences() {
        return _foodPreferences;
    }

    public List<ExperienceLevel> getExperiencePreferences() {
        return _experiencePreferences;
    }

    public List<Team> getTeamPreferences() {
        return _teamPreferences;
    }

    public boolean accountCreated() { return _accountCreated; }

    public void changeAccountStatus() { _accountCreated = !_accountCreated;  }

    public String getPictureName() {
        return _pictureName;
    }

    public double getMaxScore() {
        double maxScore = 0;
        maxScore += (_foodPreferences.size() * 5);
        maxScore += 10;
        return maxScore;
    }

    public static Set<String> getAllFoodPreferences() {
        return FOOD_PREFERENCES.keySet();
    }


}
