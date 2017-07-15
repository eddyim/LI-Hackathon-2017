package demo.model;

import java.util.*;

import static demo.model.User.DietaryRestrictions.*;
import static demo.model.User.ExperienceLevel.*;
import static demo.model.User.FoodPreferences.*;
import static demo.model.User.Team.*;

/**
 * Created by edwardim on 7/14/17.
 */
class User {
    private static final HashMap<String, FoodPreferences> FOOD_PREFERENCES;
    static {
        FOOD_PREFERENCES = new HashMap<>();
        FOOD_PREFERENCES.put("American", AMERICAN);
        FOOD_PREFERENCES.put("Chinese", CHINESE);
        FOOD_PREFERENCES.put("French", FRENCH);
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
    private String _firstName;
    private String _lastName;
    private Team _team;
    private ExperienceLevel _experienceLevel;
    private List<DietaryRestrictions> _dietaryRestrictions;
    private List<FoodPreferences> _foodPreferences;
    private boolean _accountCreated = false;

    enum DietaryRestrictions {
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

    enum FoodPreferences {
        AMERICAN,
        CHINESE,
        FRENCH,
        ITALIAN,
        JAPANESE,
        KOREAN,
        MEDITERRANEAN,
        MEXICAN,
        SPANISH,
        THAI,
        OTHER,
    }

    enum Team {
        FINANCE,
        HUMAN_RESOURCES,
        MARKETING,
        PRODUCT_DEVELOPMENT,
        SALES,
    }

    enum ExperienceLevel {
        INTERN,
        CONTRACTOR,
        JUNIOR,
        LOW_MANAGEMENT,
        MIDDLE_MANAGEMENT,
        SENIOR_MANAGEMENT,

    }

    public User(String firstName, String lastName, String team, String experienceLevel) {
        _firstName = firstName;
        _lastName = lastName;
        _team = parseTeam(team);
        _experienceLevel = parseExperienceLevel(experienceLevel);
    }

    public User(String firstName, String lastName, String team, String experienceLevel,
                List<String> dietaryRestrictions, List<String> foodPreferences) {
        _firstName = firstName;
        _lastName = lastName;
        _team = parseTeam(team);
        _experienceLevel = parseExperienceLevel(experienceLevel);
        _dietaryRestrictions = parseDietaryRestrictions(dietaryRestrictions);
        _foodPreferences = parseFoodPreferences(foodPreferences);
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
                return PRODUCT_DEVELOPMENT;
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

    public boolean accountCreated() { return _accountCreated; }

    public void changeAccountStatus() { _accountCreated = !_accountCreated;  }




}
