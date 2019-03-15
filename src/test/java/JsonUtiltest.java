import com.mmall.common.ServerResponse;
import com.mmall.util.JsonUtil;

/**
 * Created by tttppp606 on 2019/3/6.
 */
public class JsonUtiltest {
    public static void main(String[] args) {
        ServerResponse<Object> serverResponse = ServerResponse.createByErrorMessage("错误");
        String string = JsonUtil.obj2String(serverResponse);
        System.out.println(string);
    }
}
