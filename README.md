#多线程下载工具类 
使用了builer模式 方便构造<br>

设置了回答监听 方便操作<br>

但是不支持一个downloader下载多个文件 想要下载多个文件只有开启多个downloader<br>

初始化监听
```java
      FDownloader.DownloadListener listener = new FDownloader.DownloadListener() {
                  @Override
                  public void onSuccess() {//成功
                      System.out.println("成功");
                  }

                  @Override
                  public void onFailed() {//失败
                      System.out.println("失败");
                  }

                  @Override
                  public void onProgress(int progress) {//回调进度
                      System.out.println(progress+"%");
                  }

                  @Override
                  public void onLoadInfo(String info) {//回调信息
                      System.out.println(info);
                  }
              };
```
```java
初始化Downloader并设置监听
    FDownloader download = new FDownloader(listener)
                    .URLPath("http://mpge.5nd.com/2016/2016-11-15/74847/1.mp3")
                    .SavePath(null)
                    .ThreadCount(4);
```

```java
提供的方法：
     download.start();
     download.pause();
     download.restart();
     download.cancel();

```
