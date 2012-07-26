package org.dyndns.fzoli.mill.server.test.objectdb;

import java.util.Arrays;
import java.util.Collection;
import javax.persistence.EntityManager;
import static org.dyndns.fzoli.mill.server.test.objectdb.Util.*;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * ObjectDB first test.
 * 1. Open database connection and clear database.
 * 2. Creating players.
 * 3. a) Add player2 to player1's list.
 *    b) Add player1 to player2's list.
 * 4. Recreate database connection.
 * 5. Try read players.
 * 6. Close database connection.
 * @author zoli
 */
@RunWith(value = Parameterized.class)
public class FirstTest {
    
    private static EntityManager db;
    
    private boolean inverseAdd;
    
    public FirstTest(boolean inverseAdd) {
        this.inverseAdd = inverseAdd;
    }

    @Parameters
    public static Collection<Boolean[]> data() {
        Boolean[][] data = new Boolean[][] { { false }, { true } };
        return Arrays.asList(data);
    }
    
    @BeforeClass
    public static void openDatabase() throws Exception {
        System.out.println("Open database connection.");
        db = createEntityManager();
    }
    
    @Before
    public void initTest() throws Exception {
        System.out.println("Clear database.");
        clearDatabase(db);
    }
    
    @AfterClass
    public static void closeDatabase() throws Exception {
        System.out.println("Close database connection.");
        db.close();
    }
    
    @Test(timeout=10000)
    public void testOne() {
        
        System.out.println("Creating players.");
        Player p1 = new Player(PLAYER1);
        Player p2 = new Player(PLAYER2);
        save(db, p1);
        save(db, p2);
        
        System.out.println("Add player2 to player1's list.");
        p1.getFriendWishList().add(p2);
        save(db, p1);
        if (inverseAdd) {
            System.out.println("Add player1 to player2's list.");
            p2.getFriendWishList().add(p1);
            save(db, p2);
        }
        
        System.out.println("Recreate database connection.");
        db.close();
        db = createEntityManager();
        
        System.out.println("Try read players...");
        System.out.println("Read " + PLAYER1 + '.');
        getPlayer(db, PLAYER1);
        System.out.println("Read " + PLAYER2 + '.');
        getPlayer(db, PLAYER2);
        
    }
    
}