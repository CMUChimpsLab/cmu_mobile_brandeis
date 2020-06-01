package edu.cmu.policymanager.policychecker.tests;

public class ODPFactory {
    public static String createODPString() {
        return "[\n" +
                "\t{\n" +
                "\t\t\"uses\": \"android.permission.ACCESS_FINE_LOCATION\",\n" +
                "\t\t\"purpose\": \"Running Other Features\",\n" +
                "\t\t\"class\": \"edu.cmu.chimpslab.stacktracetest.MainActivity\",\n" +
                "\t\t\"method\": \"getLocation\",\n" +
                "\t\t\"for\": \"Testing the policy manager\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"uses\": \"android.permission.ACCESS_FINE_LOCATION\",\n" +
                "\t\t\"purpose\": \"Display Advertisement\",\n" +
                "\t\t\"class\": \"com.mopub.mobileads.MoPubView\",\n" +
                "\t\t\"method\": \"loadAd\",\n" +
                "\t\t\"for\": \"Generating revenue\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"uses\": \"android.permission.ACCESS_FINE_LOCATION\",\n" +
                "\t\t\"purpose\": \"Running Other Features\",\n" +
                "\t\t\"class\": \"edu.cmu.chimpslab.stacktracetest.MainActivity\",\n" +
                "\t\t\"method\": \"run\",\n" +
                "\t\t\"for\": \"Testing if we can catch this in a thread object\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"uses\": \"android.permission.ACCESS_FINE_LOCATION\",\n" +
                "\t\t\"purpose\": \"Securing Device\",\n" +
                "\t\t\"class\": \"edu.cmu.chimpslab.stacktracetest.PELocationService\",\n" +
                "\t\t\"method\": \"onStartCommand\",\n" +
                "\t\t\"for\": \"Testing if we can catch this in a service\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"uses\": \"android.permission.RECORD_AUDIO\",\n" +
                "\t\t\"purpose\": \"Running Other Features\",\n" +
                "\t\t\"class\": \"edu.cmu.chimpslab.stacktracetest.MainActivity\",\n" +
                "\t\t\"method\": \"onCreate\",\n" +
                "\t\t\"for\": \"Testing the policy manager\"\n" +
                "\t}"+
                "]";
    }

    public static String createODPFromManifest() {
        return "[\n" +
                "\t{\n" +
                "\t\t\"uses\": \"android.permission.ACCESS_COARSE_LOCATION\",\n" +
                "\t\t\"purpose\": \"Running Other Features\",\n" +
                "\t\t\"class\": \"*\",\n" +
                "\t\t\"method\": \"*\",\n" +
                "\t\t\"for\": \"Testing if we can catch this in a thread object\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"uses\": \"android.permission.ACCESS_FINE_LOCATION\",\n" +
                "\t\t\"purpose\": \"Securing Device\",\n" +
                "\t\t\"class\": \"*\",\n" +
                "\t\t\"method\": \"*\",\n" +
                "\t\t\"for\": \"Testing if we can catch this in a service\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"uses\": \"android.permission.RECORD_AUDIO\",\n" +
                "\t\t\"purpose\": \"Running Other Features\",\n" +
                "\t\t\"class\": \"*\",\n" +
                "\t\t\"method\": \"*\",\n" +
                "\t\t\"for\": \"Testing the policy manager\"\n" +
                "\t}"+
                "]";
    }
}