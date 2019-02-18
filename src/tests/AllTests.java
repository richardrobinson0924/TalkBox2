package tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({CustomDataViewTest.class, TalkBoxAppTest.class, TalkBoxDataTest.class,
           TalkBoxSimTest.class , TryTest.class, TTSWizardTest.class })

public class AllTests {}