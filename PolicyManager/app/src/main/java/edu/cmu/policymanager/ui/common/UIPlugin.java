package edu.cmu.policymanager.ui.common;

/**
 * Created by Mike Czapik (Carnegie Mellon University) on 8/20/2018.
 *
 * Represents some portion of the policy manager UI that is interchangeable or
 * swappable between UI implementations. Each policy manager UI needs an install,
 * configure and runtime UI.
 */

public interface UIPlugin {
    public Class getUI();
}