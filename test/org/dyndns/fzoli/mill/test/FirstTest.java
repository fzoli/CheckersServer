package org.dyndns.fzoli.mill.test;

import java.util.Arrays;
import java.util.Collection;
import org.dyndns.fzoli.mill.server.model.dao.PlayerDAO;
import org.dyndns.fzoli.mill.server.model.entity.Player;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author zoli
 */
@RunWith(value = Parameterized.class)
public class FirstTest {
    
    private static PlayerDAO dao;
    private String p1, p2;
    
    public FirstTest(String p1, String p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    @Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] { { "fzoli", "rex" }, { "rex", "asd" }, { "fzoli", "asd" }, { "asd", "fzoli" } };
        return Arrays.asList(data);
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        dao = new PlayerDAO();
    }
    
    @Test()
    public void init() throws InterruptedException {
        Player p = dao.getPlayer(p1);
        assertNotNull(p);
        assertTrue(p.getFriendList().contains(dao.getPlayer(p2)));
    }
    
}