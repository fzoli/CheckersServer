package org.dyndns.fzoli.mill.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author zoli
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({org.dyndns.fzoli.mill.test.SecondTest.class, org.dyndns.fzoli.mill.test.FirstTest.class})
public class AllTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
