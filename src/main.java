public class main {
    public static void main(String[] args) {

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

        FDownloader download = new FDownloader(listener)
                .URLPath("http://mpge.5nd.com/2016/2016-11-15/74847/1.mp3")
                .SavePath(null)
                .ThreadCount(4);

        download.start();

        try {
            Thread.sleep(1000);
            download.pause();
            Thread.sleep(1000);
            download.restart();
            Thread.sleep(1000);
            download.cancel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        download.start();
    }
}
