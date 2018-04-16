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
                    new DownloadThread(threadNum,start,end).start();
                }
                randomAccessFile.close();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getFileName(URL url){
        String filename = url.getFile();
        return filename.substring(filename.lastIndexOf("/")+1);
    }

    private class DownloadThread extends Thread {
        private int threadNum;
        private int start;
        private int end;

        public DownloadThread(int threadNum,int start,int end){
            this.threadNum = threadNum;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            super.run();
//            System.out.println("线程"+threadNum+"开始下载");
            try {
                URL url = new URL(path);
                File downThreadFile = new File(SaveFilePath,"downThread_"+threadNum+".dt");
                RandomAccessFile randomAccessFile = null;
                if(downThreadFile.exists()){//如果存在了
                    randomAccessFile = new RandomAccessFile(downThreadFile,"rwd");
                    String start_str = randomAccessFile.readLine();
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
                    /*
                    文件写入本地
                     */
                    byte[] buffer = new byte[1024];
                    int length = -1;
                    int total = 0;
                    int flag=1;
                    while((length=inputStream.read(buffer))>0){
                        randomAccessFile1.write(buffer,0,length);
                        total+=length;
                        PRESENT_SIZE+=length;
                        PROGRESS+=(float)length/FILESIZE;
                        if(!String.format("%.1f",(float)PRESENT_SIZE/(1024*1024))
                                .equals(String.format("%.1f",(float)FILESIZE/(1024*1024)))){
                            System.out.println(String.format("%.2f",PROGRESS*100)+"%");
                        }
                        if(flag==1&&String.format("%.1f",(float)PRESENT_SIZE/(1024*1024))
                                .equals(String.format("%.1f",(float)FILESIZE/(1024*1024)))){
                            flag=0;
                            System.out.println("100%");//强迫症患者
                            System.out.println("下载完毕！");
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
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private synchronized void cleanTempFile(File file){
        file.delete();
    }

}
