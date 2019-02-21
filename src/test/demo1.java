import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by tttppp606 on 2019/1/31.
 */
public class demo1 {
    public static void main(String[] args) throws IOException {
        File file = new File("F:/test/test");
//        file.createNewFile();
        file.mkdirs();
        File test = new File(file, "文件");
//        test.createNewFile();
        File file1 = new File("F:/test/src");
    }
}
