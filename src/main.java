public class main {
    public static void main(String[] args) {
        new threadDownload()
                .URLPath("http://mpge.5nd.com/2016/2016-11-15/74847/1.mp3")
                .SavePath(null)
                .ThreadCount(4)
                .download();
    }
}
