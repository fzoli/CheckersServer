package org.dyndns.fzoli.mill.server.test.objectdb;

import java.util.List;
import javax.persistence.EntityManager;
import static org.dyndns.fzoli.mill.server.test.objectdb.Util.*;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ObjectDB second test.
 * 1. Create database connection.
 * 2. Check whether FirstTest finished.
 * 3. Clear messages.
 * 4. Try read players.
 * 5. Create two messages.
 * 6. Reading back and testing.
 * @author zoli
 */
public class SecondTest {
    
    private static EntityManager db;
    
    public SecondTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("Create database connection.");
        db = createEntityManager();
        assertEquals("There should be exactly 2 players in the database. Please run FirstTest!", 2, getCount(db, Player.class));
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("Close database connection.");
        db.close();
    }
    
    @Test(timeout=10000)
    public void testTwo() {
        
        System.out.println("Clear messages.");
        remove(db, Message.class);
        
        System.out.println("Try read players...");
        Player p1 = getPlayer(db, PLAYER1);
        assertNotNull(p1);
        Player p2 = getPlayer(db, PLAYER2);
        assertNotNull(p2);
        
        System.out.println("Create message 1.");
        Message m1 = new Message(p2, "message 1");
        assertTrue(save(db, m1));
        p1.getPostedMessages().add(m1);
        assertTrue(save(db, p1));
        
        System.out.println("Create message 2.");
        Message m2 = new Message(p1, "message 2");
        assertTrue(save(db, m2));
        p2.getPostedMessages().add(m2);
        assertTrue(save(db, p2));
        
        System.out.println("Testing...");
        List<Message> messages = getMessages(db, PLAYER1);
        assertNotNull(messages);
        assertEquals(1, messages.size());
        assertEquals(1, p1.getReceivedMessages().size());
        assertEquals(OnlineStatus.ONLINE, p1.getOnlineStatus());
        
    }
     
}