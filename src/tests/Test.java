package tests;

public class Test {

    public static void main (String [] args){

        Result result  =  JUnitCore.runClasses(AllTests.class);

        for (Failure failure: result.getFailures()){
            System.out.println(failure.toString());

        }
        System.out.println(result.wasSuccessful());

    }
}
