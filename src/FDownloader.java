import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FDownloader {
    static float PROGRESS;//下载进度
    static int FILESIZE;
    static int PRESENT_SIZE;

    static int PRE_SIZE;
    private boolean isCancel=false;
    private boolean isPause=false;
    private boolean isRestart=false;


    //下载路径
    private String path;
    //下载存放目录
    private String SaveFilePath = "/";
    //线程数目
    private int threadCount = 3;

    private File file;

    DownloadListener listener;

    List<DownloadThread> threadlist = new ArrayList<>();
    DownloadThread[] threads = new DownloadThread[20];

    private static String info;

    public FDownloader(DownloadListener listener){
        this.listener = listener;
    }

    public FDownloader URLPath(String path){
        this.path = path;
        return this;
    }
    public FDownloader SavePath(String SaveFilePath){
        this.SaveFilePath = SaveFilePath;
        return this;
    }
    public FDownloader ThreadCount(int threadCount){
        this.threadCount = threadCount;
        return this;
    }


    public void start (){
        try {
            isPause = false;
            isCancel = false;
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            int code = conn.getResponseCode();
            if(code==200){
                int connectLength = conn.getContentLength();
                FILESIZE = conn.getContentLength();
                if(FILESIZE==0){
                    listener.onFailed();
                }
                info = "文件大小为:"+(float)connectLength/(1024*1024)+"mb";
                listener.onLoadInfo(info);
                file = new File(SaveFilePath,getFileName(url));
                RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
                randomAccessFile.setLength(connectLength)   ;
                
                int EachSize = connectLength/threadCount;
                for (int threadNum = 0; threadNum < threadCount; threadNum++) {
                    int start = threadNum * EachSize;
                    int end = (threadNum+1) * EachSize -1;
                    if(threadNum == threadCount - 1){//最后一个线程下载剩余全部
                        end = connectLength-1;
                    }
                    DownloadThread thread = new DownloadThread(threadNum,start,end,path,SaveFilePath);
                    thread.start();
                    threads[threadNum] = thread;
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

    public class DownloadThread extends Thread {
        private int threadNum;
        private int start;
        private int end;
        private String path;
        private String SaveFilePath;
        private String start_str;
        public int progress;

        public DownloadThread(int threadNum,int start,int end,String path,String saveFilePath){
            this.threadNum = threadNum;
            this.start = start;
            this.end = end;
            this.path = path;
            this.SaveFilePath = saveFilePath;
        }
        @Override
        public synchronized void run() {
            super.run();
            try {
                URL url = new URL(path);
                File downThreadFile = new File(SaveFilePath,"downThread_"+threadNum+".dt");
                RandomAccessFile randomAccessFile = null;
                if(downThreadFile.exists()){//如果存在了
                    randomAccessFile = new RandomAccessFile(downThreadFile,"rwd");
                    start_str = randomAccessFile.readLine();
                    if(null==start_str||"".equals(start_str)){//如果没下载过
                        this.start = start;
                    }else{
                        this.start = Integer.parseInt(start_str)-1;//设置下载起点
                    }
                }else{
                    randomAccessFile = new RandomAccessFile(downThreadFile,"rwd");
                }

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);

                //设置分段下载的头信息。。 Range:做分段数据请求  格式：Range=0-1024
                conn.setRequestProperty("Range","bytes="+start+"-"+end);
//                System.out.println("线程："+threadNum+"的下载起点是"+start+" 下载终点是"+end);
                if(conn.getResponseCode()==206){//206表示部分资源请求成功
                    InputStream inputStream = conn.getInputStream();
                    RandomAccessFile randomAccessFile1 = new RandomAccessFile(
                            new File(SaveFilePath,getFileName(url)),"rw");
                    randomAccessFile1.seek(start);

                    byte[] buffer = new byte[1024];
                    int length = -1;
                    int total = 0;
                    int flag=1;
                    while((length=inputStream.read(buffer))>0){
                        if(isCancel||isPause){//线程读取时跳出 可以提前结束线程 变相销毁线程
                            break;
                        }
                        randomAccessFile1.write(buffer,0,length);
                        total+=length;
                        PRESENT_SIZE+=length;
                        PROGRESS+=(float)length/ FILESIZE;

                        if(!isPause&&!isCancel){
                            if(!String.format("%.1f",(float) PRESENT_SIZE/(1024*1024))
                                    .equals(String.format("%.1f",(float) FILESIZE/(1024*1024)))){
                                double pro = PROGRESS*100;
                                progress=Integer.parseInt(String.valueOf((int)pro));
                                listener.onProgress(progress);
                            }

                            //这里有个bug就是下载完成无法显示100% 应该是浮点数的问题？只有手动设置了
                            if(flag==1&&String.format("%.1f",(float) PRESENT_SIZE/(1024*1024))
                                    .equals(String.format("%.1f",(float) FILESIZE/(1024*1024)))){
                                flag=0;
                                progress = 100;
                                listener.onProgress(progress);
                                listener.onSuccess();
                            }
                        }
                        randomAccessFile.seek(0);
                        randomAccessFile.write((start+total+"").getBytes("UTF-8"));
                    }
                    randomAccessFile.close();
                    randomAccessFile1.close();
                    inputStream.close();
                    cleanTempFile(downThreadFile);//清除临时文件
                }else{
                    System.out.println("code="+conn.getResponseCode());
                    listener.onFailed();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private synchronized void cleanTempFile(File file){
            file.delete();
        }
        private String getFileName(URL url){
            String filename = url.getFile();
            return filename.substring(filename.lastIndexOf("/")+1);
        }
    }

    public void pause()  {
        System.out.println("暂停啦～\n");
        if(!isPause){
            isPause = true;
            isRestart=false;
            isCancel=false;
        }
    }


    public synchronized void restart(){
        System.out.println("重新开始啦～\n");
        if(isPause&&!isRestart){
            start();
            isCancel=false;
            isPause = false;
            isRestart = true;
        }
    }

    public synchronized void cancel(){
        System.out.println("取消啦\n");
        if(!isCancel){
            if(isPause){
                ////若线程处于等待状态，则while循环处于阻塞状态，无法跳出循环，必须先唤醒线程，才能执行取消任务
                restart();
            }
            isCancel = true;
            isRestart=false;
            isPause=false;
            PROGRESS = 0;
            PRESENT_SIZE = 0;
            if(file.exists()){
                if(file.delete())
                    System.out.println("成功删除");
                else
                    System.out.println("删除失败");
            }
        }
    }
    interface DownloadListener{
        void onSuccess();
        void onFailed();
        void onProgress(int progress);
        void onLoadInfo(String info);
    }
}
