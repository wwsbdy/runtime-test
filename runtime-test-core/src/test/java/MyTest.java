import com.zj.runtimetest.utils.JsonUtil;
import com.zj.runtimetest.vo.RequestInfo;

/**
 * @author : jie.zhou
 * @date : 2025/7/2
 */
public class MyTest {

    public static void main(String[] args) {
        RequestInfo cache = new RequestInfo();
        cache.setStaticMethod(true);
        String jsonString = JsonUtil.toJsonString(cache);
        Object javaBean = JsonUtil.toJavaBean(jsonString, RequestInfo.class);
    }
}
