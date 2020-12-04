package org.woheller69.weather;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.woheller69.weather.database.DatabaseTest;


@RunWith(Suite.class)
@Suite.SuiteClasses({DatabaseTest.class})
public class AndroidTestSuite {
}
