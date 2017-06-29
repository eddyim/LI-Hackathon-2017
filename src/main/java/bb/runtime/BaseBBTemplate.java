package bb.runtime;

import bb.directives.DirectiveSet;

import java.util.LinkedList;
import java.util.List;

public class BaseBBTemplate {

    private List<DirectiveSet> _directiveSets = new LinkedList<>();

    public String toS(Object o) {
        return o == null ? "" : o.toString();
    }

    public void addDirectiveSet(DirectiveSet set) {
        _directiveSets.add(set);
    }

}
