package tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.runners.Suite.SuiteClasses;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({TalkBoxAppTest.class, TalkBoxSimTest.class, TalkBoxDataTest.class, TryTest.class })


public class AllTests {}

