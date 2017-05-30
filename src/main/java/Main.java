import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Main {
    public static void main(String[] args) {
        try {
            long beforeProgramStart = System.currentTimeMillis();
//            String[] args1 = {"src\\main\\resources\\test.txt", "java,facebook,home", "-c", "-v", "-w", "-e"};
            ConsoleReader reader = ConsoleReader.init(args);
            reader.start();
            System.out.println(reader.printAllResults());
            System.out.println(reader.printGlobalResult());
            long timeSpent = System.currentTimeMillis() - beforeProgramStart;
            System.out.println("Program works: " + timeSpent + " ms");
        }catch(RuntimeException e){
            log.warn("Wrong input parameters", e);
            System.out.println("Wrong input parameters\n" + ConsoleReader.getHelpMsg());
        } catch (IOException e) {
            log.error("Incorrect input path");
        }
    }


}
