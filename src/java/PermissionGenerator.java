import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import org.dyndns.fzoli.mill.common.Permission;

/**
 *
 * @author zoli
 */
public class PermissionGenerator extends JFrame implements ActionListener {

    private final JLabel lb = new JLabel("0");
    private final Permission[] permissions = Permission.values();
    private final List<JCheckBox> cbs = new ArrayList<JCheckBox>();
    
    public PermissionGenerator() {
        super("Permission-mask generator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.CENTER));
        p.add(new JLabel("Permission mask: "));
        p.add(lb);
        add(p, BorderLayout.NORTH);
        p = new JPanel(new GridLayout(permissions.length / 2, 2));
        add(p, BorderLayout.CENTER);
        for (Permission perm : permissions) {
            JCheckBox cb = new JCheckBox(perm.name());
            cb.addActionListener(this);
            cb.setFocusable(false);
            cbs.add(cb);
            p.add(cb);
        }
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    for (JCheckBox cb : cbs) {
                        cb.setSelected(!cb.isSelected());
                    }
                    actionPerformed(null);
                }
            }
            
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        int mask = 0;
        for (int i = 0; i < cbs.size(); i++) {
            JCheckBox cb = cbs.get(i);
            if (cb.isSelected()) {
                mask += Math.pow(2, i);
            }
        }
        lb.setText(Integer.toString(mask));
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception ex) {
            ;
        }
        new PermissionGenerator().setVisible(true);
    }

}