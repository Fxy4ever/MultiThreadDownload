import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class threadDownload {
    static float PROGRESS=0;
    static int FILESIZE=0;
    static int PRESENT_SIZE=0;

    //下载路径
    private String path;
    //下载存放目录
    private String SaveFilePath = "/";
    //线程数目
    private int threadCount = 3;

    public static String info;

    public threadDownload(){}

    public threadDownload URLPath(String path){
        this.path = path;
        return this;
    }
    public threadDownload SavePath(String SaveFilePath){
        this.SaveFilePath = SaveFilePath;
        return this;
    }
    public threadDownload ThreadCount(int threadCount){
        this.threadCount = threadCount;
        return this;
    }
    public void download (){
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            int code = conn.getResponseCode();
            if(code==200){
                int connectLength = conn.getContentLength();
                FILESIZE = conn.getContentLength();
                info = "文件大小为:"+(float)connectLength/(1024*1024)+"mb";
                System.out.println("文件大小为:"+(float)connectLength/(1024*1024)+"mb");
                RandomAccessFile randomAccessFile = new RandomAccessFile(new File(SaveFilePath,getFileName(url)),"rw");
                randomAccessFile.setLength(connectLength);
                
                int EachSize = connectLength/threadCount;
                for (int threadNum = 0; threadNum < threadCount; threadNum++) {
                    int start = threadNum * EachSize;
                    int end = (threadNum+1) * EachSize -1;
                    if(threadNum == threadCount - 1){//最后一个线程下载剩余全部
                        end = connectLength-1;
                    }
                    new DownloadThread(threadNum,start,end,path,SaveFilePath).start();
                }
                randomAccessFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getFileName(URL url){
        String filename = url.getFile();
        return filename.substring(filename.lastIndexOf("/")+1);
    }


}
