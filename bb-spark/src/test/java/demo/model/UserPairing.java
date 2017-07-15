package demo.model;

import demo.model.User.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edwardim on 7/15/17.
 */
public class UserPairing implements Comparable<UserPairing> {
    User _first;
    User _second;
    List<FoodPreferences> _sharedPreference = new ArrayList<>();
    double _score = 0;

    public UserPairing(User u1, User u2) {
        _first = u1;
        _second = u2;
        parseSharedPreferences();
        parseScore();
    }

    private void parseSharedPreferences() {
        for(FoodPreferences pref: _first.getFoodPreferences()) {
            if (_second.getFoodPreferences().contains(pref)) {
                _sharedPreference.add(pref);
            }
        }
    }

    private void parseScore() {
        _score += (5 * _sharedPreference.size());
        if (_first.getTeamPreferences().contains(_second.getTeam())) {
            _score += 5;
        }
        if (_second.getTeamPreferences().contains(_first.getTeam())) {
            _score += 5;
        }
        if (_first.getExperiencePreferences().contains(_second.getExperienceLevel())) {
            _score += 5;
        }
        if (_second.getTeamPreferences().contains(_first.getTeamPreferences())) {
            _score += 5;
        }
    }

    public double getScore() {
        return _score;
    }

    @Override
    public int compareTo(UserPairing o) {
        return (int) (this._score - o.getScore());
    }

}
