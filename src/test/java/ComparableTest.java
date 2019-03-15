import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by tttppp606 on 2019/3/14.
 */
public class ComparableTest implements Comparable {
    private String str1 = "a";
    private String str2 = "c";

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    public static void main(String[] args) {
        ComparableTest comparableTest = new ComparableTest();
        HashMap<Object, Object> map = Maps.newHashMap();
        Set<Map.Entry<Object, Object>> entrySet = map.entrySet();
        map.put(1,"a");
        map.put(2,"b");
        map.put(3,"c");

    }

}
