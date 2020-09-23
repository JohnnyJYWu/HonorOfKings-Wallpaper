import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @version V1.0
 * @author JohnnyJYWu
 * @description 文件下载器
 */

public class FileDownload {

    private static FileDownload instance;

    public static FileDownload getInstance() { //单例模式
        if (instance == null) {
            instance = new FileDownload();
        }

        return instance;
    }

    private LinkedList<HashMap<String, String>> linkedList = new LinkedList<>(); //任务队列

    /**
     * 添加任务进入任务队列
     */
    public void addTask(String imgUrl, String filePath, String fileName) { //下载地址、存储路径、文件名
        HashMap<String, String> map = new HashMap<>();
        map.put("imgUrl", imgUrl);
        map.put("filePath", filePath);
        map.put("fileName", fileName);
        linkedList.offer(map);
    }

    /**
     * 开始执行当前队列任务
     */
    public void startTask() {
        System.out.println("开始下载：共" + linkedList.size() + "项任务");

        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(8);

        while (!linkedList.isEmpty()) {
            final HashMap<String, String> map = linkedList.poll();
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    downloadImg(map.get("imgUrl"), map.get("filePath"), map.get("fileName"));
                }
            });
        }

        //等待下载完成
        fixedThreadPool.shutdown();
        while(true) {
            if(fixedThreadPool.isTerminated()) {
                fixedThreadPool.shutdownNow();
                System.out.println("下载完成");
                break;
            }
        }
    }

    /**
     * 下载图片
     */
    public void downloadImg(String imgUrl, String filePath, String fileName) { //下载地址、存储路径、文件名
        //创建文件的目录结构
        File files = new File(filePath);
        if(!files.exists()){ // 判断文件夹是否存在，如果不存在就创建一个文件夹
            files.mkdirs();
        }
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(imgUrl).build();
            ResponseBody body = client.newCall(request).execute().body();
            InputStream is = body.byteStream();

            // 创建文件
            File file = new File(filePath + "/" + fileName);
            FileOutputStream out = new FileOutputStream(file);
            int i = 0;
            while((i = is.read()) != -1){
                out.write(i);
            }
            is.close();
            out.close();

            System.out.println("下载完成：" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            failLog("下载失败："+ filePath + "/" + fileName);
        }
    }

    /**
     * 下载失败打印日志
     */
    public void failLog(String content) {
        String filePath = HokWallpaper.savePath + "/failLog.log";
        File file = new File(filePath);

        FileWriter fw;
        try {
            fw = new FileWriter(file, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(content);
            pw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 字符串写入文件
     */
    public void writeStringToFile(String content, String filePath) {
        File file = new File(filePath);
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            PrintStream ps = new PrintStream(out);
            ps.println(content);
            ps.flush();
            ps.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
