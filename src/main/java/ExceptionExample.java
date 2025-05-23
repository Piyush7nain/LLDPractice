import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ExceptionExample {

    public static void main(String[] args) {
        method1();
    }

    static int method1(){
        FileInputStream fis;
        try{
            int result = 0;
            fis = new FileInputStream("");

        }catch (FileNotFoundException e){
            System.out.println("Occurs in exception");
            e.printStackTrace();
            return 1;
        }finally {
            System.out.println("Finally");
        }
        return 0;
    }
}
