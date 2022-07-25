package xyz.destiall.caramel.app.ui;

// import sun.swing.FilePane;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public final class FileChooser extends JFileChooser {
    public FileChooser(String title, String[] extensions) {
        super(new File(""));
        File workingDirectory = new File(System.getProperty("user.dir"));
        for (String e : extensions) {
            setFileFilter(new FileNameExtensionFilter(e, e.substring(1)));
        }
        setCurrentDirectory(workingDirectory);
        setDialogTitle(title);
        setMultiSelectionEnabled(false);
        setVisible(true);
        setLocation(5, 5);
    }

    @Override
    protected JDialog createDialog(Component parent) throws HeadlessException {
        JDialog dialog = super.createDialog(parent);
        try {
            BufferedImage image = ImageIO.read(new File("logo_16.png"));
            dialog.setIconImage(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dialog;
    }

    @Override
    public void updateUI() {
        LookAndFeel old = UIManager.getLookAndFeel();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable ex) {
            old = null;
        }

        super.updateUI();

        /*
        if (old != null) {
            FilePane filePane = findFilePane(this);
            filePane.setViewType(FilePane.VIEWTYPE_DETAILS);
            filePane.setViewType(FilePane.VIEWTYPE_LIST);

            Color background = UIManager.getColor("Label.background");
            setBackground(background);
            setOpaque(true);

            try {
                UIManager.setLookAndFeel(old);
            } catch (UnsupportedLookAndFeelException ignored) {} // shouldn't get here
        }

         */
    }

    /*
    private static FilePane findFilePane(Container parent) {
        for (Component comp : parent.getComponents()) {
            if (FilePane.class.isInstance(comp)) {
                return (FilePane) comp;
            }
            if (comp instanceof Container) {
                Container cont = (Container) comp;
                if (cont.getComponentCount() > 0) {
                    FilePane found = findFilePane(cont);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }

        return null;
    }

     */
}
