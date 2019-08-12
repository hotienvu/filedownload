## FileDownloader

### Usage 
``` 
mvn clean package

Usage: java -jar target/filedownload-1.0-SNAPSHOT.jar <options>
Example
java -jar target/filedownload-1.0-SNAPSHOT.jar \
	-u http://example.com/file.xml \
	-u file:///usr/local/file.xml \
	-o ./target \
	-t /tmp \
	-c download.url.scheme.file=com.vho.filedownload.downloader.local \
	-c download.file.overwrite=true \
	-c download.http.buffer.size.bytes=1024 \
	-p 1
  Options:
    --conf, -c
      Configuration for each download e.g. --conf
      download.http.buffer.size.bytes=1024
      Syntax: --confkey=value
      Default: {}
    --help, -h
      print help
  * --output-dir, -o
      output dir
      Default: <empty string>
    --parallel, -p
      number of workers to run. default is number of available CPUs
      Default: 8
    --tmp-dir, -t
      tmp dir
      Default: <empty string>
  * --url, -u
      urls to download
      Default: []
```

### Development

All files are saved to a tmp directory before being renamed to target destinations. Downloader downloads files as 
output streams to avoid OOM.

New protocols can be added by implementing `Downloader` interface and can be added dynamically by
passing the `url.scheme.` option e.g. if we want to dowload from HDFS: 
`--conf download.url.scheme.hdfs=com.vho.downloader.hdfs`. This will look for `com.vho.downloader.hdfs.DefaultDownloader` 
in the classpath. 

### TODO:
Multipart download e.g. using Http Ranges header
