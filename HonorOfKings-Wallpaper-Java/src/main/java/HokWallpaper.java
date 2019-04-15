import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version V1.0
 * @author JohnnyJYWu
 * @description 王者荣耀官方壁纸信息获取、解析
 */

public class HokWallpaper {

    public static final String savePath = "E:\\test"; //壁纸保存路径

    public static void main(String[] args) {
        downloadAllWallpaper();
    }

    /**
     * 解析全部壁纸并执行下载任务
     */
    public static void downloadAllWallpaper() {
        JSONArray list = getAllList();
        for (int i = 0; i < list.size(); i ++) {
            analyzeWallpaper(list.getJSONObject(i));
        }
        FileDownload.getInstance().startTask();
        FileDownload.getInstance().writeStringToFile(list.toString(), savePath + "\\list.json");
    }

    /**
     * 解析选择的壁纸并执行下载任务
     */
    public static void downloadSelectedWallpaper(int page, int index) {
        JSONArray list = getList(page);
        analyzeWallpaper(list.getJSONObject(index));
        FileDownload.getInstance().startTask();
    }

    /**
     * 获取全部壁纸信息，目前共有17页，每页20张壁纸
     */
    public static JSONArray getAllList() {
        JSONArray list = new JSONArray();

        for (int i = 0; i < 18; i ++) {
            list.fluentAddAll(getList(i));
        }
        System.out.println(list);

        return list;
    }

    /**
     * 获取单页壁纸信息，每页20张壁纸，与官网同步
     */
    public static JSONArray getList(int page) {
        String url = "https://apps.game.qq.com/cgi-bin/ams/module/ishow/V1.0/query/workList_inc.cgi?activityId=2735&sVerifyCode=ABCD&sDataType=JSON&iListNum=20&totalpage=0"
                + "&page=" + page
                + "&iOrder=0&iSortNumClose=1&jsoncallback=jQuery17108245597921518668_1549947224644&iAMSActivityId=51991&_everyRead=true&iTypeId=2&iFlowId=267733&iActId=2735&iModuleId=2735&_=1549948866750";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Call call = client.newCall(request);

        String result = "";
        try {
            result = call.execute().body().string();
            result = URLDecoder.decode(result, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        result = result.substring(result.indexOf("(") + 1, result.indexOf(")"));

        JSONObject object = JSONObject.parseObject(result);
        JSONArray list = object.getJSONArray("List");
        System.out.println(list);

        return list;
    }

    private static final String[] size = {"", "_215_120", "_1024_768", "_1280_720", "_1280_1024",
            "_1440_900", "_1920_1080", "_1920_1200", "_1920_1440"}; //分辨率后缀

    /**
     * 解析单张壁纸信息
     */
    public static void analyzeWallpaper(JSONObject object) {
        String name = object.getString("sProdName"); //壁纸名称
        System.out.println(name);
        String time = object.getString("dtInputDT"); //更新时间
        System.out.println(time);

        //容错，去除多余空格
        if (name.charAt(0) == ' ') {
            name = name.substring(1, name.length() - 1);
        }
        if (name.charAt(name.length() - 1) == ' ') {
            name = name.substring(0, name.length() - 2);
        }
        //去除非法字符
        name = stringFilter(name);

        String filePath = savePath + "\\" + name;
        for (int No = 1; No <= 8; No ++) {
            String sProdImg = "sProdImgNo_" + No; //sProdImgNo_1~8分别代表该壁纸官方发布的8种分辨率图片
            sProdImg = object.getString(sProdImg);
            sProdImg = sProdImg.substring(0, sProdImg.length() - 3) + '0'; //将链接尾部的/200改为/0，所得链接为原图链接

            System.out.println(sProdImg);

            FileDownload.getInstance().addTask(sProdImg, filePath, name + size[No] + ".jpg");
        }
    }

    /**
     * 正则，过滤非法字符（创建文件夹）
     */
    public static String stringFilter(String str) {
        String regEx="[\\:*?\"<>|\\/\\\\]";//windows文件名不允许包含（\/:*?"<>|），使用正则消除
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("·").trim();
    }
}
