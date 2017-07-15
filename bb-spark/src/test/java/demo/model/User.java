package demo.model;

import java.util.List;

import static demo.model.User.Team.*;

/**
 * Created by edwardim on 7/14/17.
 */
class User {
    private String _firstName;
    private String _lastName;
    private Team _team;
    private ExperienceLevel _experienceLevel;
    private List<DietaryRestrictions> _dietaryRestrictions;
    private List<FoodPreferences> _foodPreferences;

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



}
