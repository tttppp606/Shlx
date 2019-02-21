package innerClass;

/**
 * Created by tttppp606 on 2019/2/4.
 */
public class OutClass {
    public  impl doit(){
        return new impl() {
            @Override
            public void f() {
            }
        };
    }
}


