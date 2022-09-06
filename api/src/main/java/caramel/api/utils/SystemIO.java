package caramel.api.utils;

import javax.swing.*;

public final class SystemIO {
    private SystemIO() {}

    public static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            final int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        } return Integer.parseInt(version);
    }

    public static int showPopupMessage(final String title, final String message, final String... options) {
        final JFrame frame = new JFrame();
        final int ret = JOptionPane.showOptionDialog(frame, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, new Object[] {options}, options != null ? options[0] : null);
        frame.dispose();
        return ret;
    }
}
