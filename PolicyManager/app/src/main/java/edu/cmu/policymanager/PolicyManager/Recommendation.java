package edu.cmu.policymanager.PolicyManager;

import edu.cmu.policymanager.PolicyManager.policies.UserPolicy;

public final class Recommendation {
    public final double crowdStatistics;
    public final UserPolicy.Policy crowdSetting;

    public Recommendation(final double crowdStatistics,
                          final UserPolicy.Policy crowdSetting) {
        if(crowdStatistics <= 0 || crowdStatistics > 100) {
            throw new IllegalArgumentException("Crowd statistic is " + crowdStatistics + " which should be in the range of (0, 100]");
        }

        this.crowdStatistics = crowdStatistics;
        this.crowdSetting = crowdSetting;
    }

    @Override
    public String toString() {
        return crowdStatistics + "% of PE Android users set \"" + settingToString(crowdSetting) + "\"";
    }

    private String settingToString(final UserPolicy.Policy policy) {
        String settingString = policy == UserPolicy.Policy.ALLOW ? "ON" : "N/A";
        settingString = policy == UserPolicy.Policy.ASK ? "ASK" : settingString;
        settingString = policy == UserPolicy.Policy.DENY ? "OFF" : settingString;

        return settingString;
    }
}