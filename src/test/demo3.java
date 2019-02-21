import java.math.BigDecimal;

/**
 * Created by tttppp606 on 2019/2/2.
 */
public class demo3 {
    public static void main(String[] args) {
        BigDecimal bigDecimal = new BigDecimal("12.3");
        int i = bigDecimal.intValue();
        System.out.println(i);
    }
}
