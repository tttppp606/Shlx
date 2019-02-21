import java.io.File;

/**
 * Created by tttppp606 on 2019/2/16.
 */
public class demo5 {
    public static void main(String[] args) {
        File file = new File("F:/test");
        String name = file.getName();
        System.out.println(name);
    }
}
