# MultiThreadDownload
----
多线程下载工具类 
====
builer模式<br>
下载步骤：<br>
1、用HttpURLConnection获得文件大小<br>
2、利用RandowAccessFile可以很容易地操作文件<br>
3、给每个线程分配起始点<br>
4、启动下载线程<br>
5、判断有没有保存上次下载的临时文件<br>
6、启动线程下载的时候保存下载的位置信息<br>
7、下载完毕后删除这些临时文件<br>

增加了GUI界面 便于使用 不过有一些bug。。暂时没找到好的解决方法<br>

<img  height="300" src="https://github.com/fengxinyao1/MultiThreadDownload/blob/master/photo.png"/><br>
