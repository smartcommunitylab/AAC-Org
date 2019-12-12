package it.smartcommunitylab;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test
 */
public class NiFiTenantsManagerTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public NiFiTenantsManagerTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( NiFiTenantsManagerTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testNiFiTenantsManager()
    {
        assertTrue( true );
    }
}
