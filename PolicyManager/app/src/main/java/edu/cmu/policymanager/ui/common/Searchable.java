package edu.cmu.policymanager.ui.common;

/**
 * Adds search capability to any class that implements this interface.
 *
 * Created by Mike Czapik (Carnegie Mellon University) on 8/13/2018.
 */

public interface Searchable {
    public boolean contains(final String[] keywords);
}
