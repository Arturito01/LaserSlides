package cat.merino.albert.laserslides;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class Utils {
    /***
     * Utility method to parse string for specific OSC types.
     * @param value
     * @return the value in int, float, string
     */
    public static Object simpleParse(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
        }

        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException nfe) {
        }

        return value;
    }

    public static String convertToString(List<Object> args) {
        StringBuilder res = new StringBuilder();
        if (args == null || args.size() < 1) {
            return res.toString();
        }

        for (int i = 0; i < args.size(); i += 1) {
            res.append(args.get(i).toString()).append(" ");
        }

        return res.substring(0, res.length() - 1);
    }

    /**
     * From this StackOverflow answer: http://stackoverflow.com/a/13007325
     *
     * @param useIPv4 false for v6
     * @return the ip address
     */
    public static String getIpAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = addr instanceof Inet4Address;
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim < 0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }
}
