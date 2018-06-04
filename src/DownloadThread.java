import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadThread extends Thread {
    private int threadNum;
    private int start;
    private int end;
    private String path;
    private String SaveFilePath;
    private boolean isFirstDownload=true;
    private String start_str;
    static String progress;
    static int progress1;

    public DownloadThread(int threadNum,int start,int end,String path,String saveFilePath){
        this.threadNum = threadNum;
        this.start = start;
        this.end = end;
        this.path = path;
        this.SaveFilePath = saveFilePath;
    }
    @Override
    public void run() {
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
                    randomAccessFile1.write(buffer,0,length);
                    total+=length;
                    threadDownload.PRESENT_SIZE+=length;
                    threadDownload.PROGRESS+=(float)length/ threadDownload.FILESIZE;

                    if(!String.format("%.1f",(float) threadDownload.PRESENT_SIZE/(1024*1024))
                            .equals(String.format("%.1f",(float) threadDownload.FILESIZE/(1024*1024)))){
                        double pro = threadDownload.PROGRESS*100;
                        progress=String.valueOf((int)pro);
                        progress1 = Integer.parseInt(progress);
                        System.out.println(String.format("%.2f",threadDownload.PROGRESS*100)+"%"
                        );
                    }

                    //这里有个bug就是下载完成无法显示100% 应该是浮点数的问题？只有手动设置了
                    if(flag==1&&String.format("%.1f",(float) threadDownload.PRESENT_SIZE/(1024*1024))
                            .equals(String.format("%.1f",(float) threadDownload.FILESIZE/(1024*1024)))){
                        flag=0;
                        progress1 = 100;
                        System.out.println("100% 下载完毕");
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