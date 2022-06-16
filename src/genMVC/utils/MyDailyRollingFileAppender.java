package genMVC.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.FileAppender;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Layout;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;
/**
 * 类描述:日志文件生成类
 *          1、根据日期拆分
 *          2、根据大小拆分
 *          3、拆分后的日志压缩
 *
 * @Author:wangjinhui
 * @date:2018年12月03日
 * @Version:1.1.0
 */
public class MyDailyRollingFileAppender extends FileAppender {

    static final int TOP_OF_TROUBLE = -1;
    static final int TOP_OF_MINUTE = 0;
    static final int TOP_OF_HOUR = 1;
    static final int HALF_DAY = 2;
    static final int TOP_OF_DAY = 3;
    static final int TOP_OF_WEEK = 4;
    static final int TOP_OF_MONTH = 5;
    private String datePattern = "'.'yyyy-MM-dd";
    private String scheduledFilename;//上一次生成的文件名称
    private long nextCheck = System.currentTimeMillis() - 1L;//下一次的校验时间
    Date now = new Date();
    SimpleDateFormat sdf;
    RollingCalendar rc = new RollingCalendar();
    int checkPeriod = -1;
    static final TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");

    protected long maxFileSize = 10485760L;//文件的最大大小
    private long nextRollover = 0L;//下一次处理文件压缩的大小
    private int fileIndex = 0;//生成的文件的下标
    private String fileNamePrefix;//文件名称前缀
    private String fileNameSuffix;//文件名称后缀

    public MyDailyRollingFileAppender() {
    }

    public MyDailyRollingFileAppender(Layout layout, String filename, String datePattern) throws IOException {
        super(layout, filename, true);
        this.datePattern = datePattern;
        this.activateOptions();
    }

    public void setDatePattern(String pattern) {
        this.datePattern = pattern;
    }

    public String getDatePattern() {
        return this.datePattern;
    }

    public void activateOptions() {
        super.activateOptions();
        if (this.datePattern != null && this.fileName != null) {
            this.now.setTime(System.currentTimeMillis());
            this.sdf = new SimpleDateFormat(this.datePattern);
            int type = this.computeCheckPeriod();
            this.printPeriodicity(type);
            this.rc.setType(type);
            File file = new File(this.fileName);
//            this.scheduledFilename = this.fileNamePrefix + this.sdf.format(new Date(file.lastModified())) + "_" + fileIndex + this.fileNameSuffix;
            //给参数fileNamePrefix与fileNameSuffix赋值初始值
            if (StringUtils.isEmpty(this.fileNamePrefix) || StringUtils.isEmpty(this.fileNameSuffix)) {
                this.setFileNamePrefix(this.fileName);
                this.setFileNameSuffix(this.fileName);
            }
            this.scheduledFilename = initScheduleFilename(this.sdf.format(new Date(file.lastModified())));
        } else {
            LogLog.error("Either File or DatePattern options are not set for appender [" + this.name + "].");
        }

    }

    /**
     * 利用递归确定此次在生成文件时的名称
     * @param lastModifiedDate
     * @return
     */
    public String initScheduleFilename(String lastModifiedDate){
        scheduledFilename = this.fileNamePrefix + lastModifiedDate + "_" + fileIndex + this.fileNameSuffix;
        File target  = new File(scheduledFilename + ".zip");
        if (target.exists()) {
            fileIndex++;
            return initScheduleFilename(lastModifiedDate);
        } else {
            return scheduledFilename;
        }

    }

    void printPeriodicity(int type) {
        switch(type) {
            case 0:
                LogLog.debug("Appender [" + this.name + "] to be rolled every minute.");
                break;
            case 1:
                LogLog.debug("Appender [" + this.name + "] to be rolled on top of every hour.");
                break;
            case 2:
                LogLog.debug("Appender [" + this.name + "] to be rolled at midday and midnight.");
                break;
            case 3:
                LogLog.debug("Appender [" + this.name + "] to be rolled at midnight.");
                break;
            case 4:
                LogLog.debug("Appender [" + this.name + "] to be rolled at start of week.");
                break;
            case 5:
                LogLog.debug("Appender [" + this.name + "] to be rolled at start of every month.");
                break;
            default:
                LogLog.warn("Unknown periodicity for appender [" + this.name + "].");
        }

    }

    int computeCheckPeriod() {
        RollingCalendar rollingCalendar = new RollingCalendar(gmtTimeZone, Locale.getDefault());
        Date epoch = new Date(0L);
        if (this.datePattern != null) {
            for(int i = 0; i <= 5; ++i) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(this.datePattern);
                simpleDateFormat.setTimeZone(gmtTimeZone);
                String r0 = simpleDateFormat.format(epoch);
                rollingCalendar.setType(i);
                Date next = new Date(rollingCalendar.getNextCheckMillis(epoch));
                String r1 = simpleDateFormat.format(next);
                if (r0 != null && r1 != null && !r0.equals(r1)) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * 重新生成文件的方法
     * @throws IOException
     */
    void rollOver() throws IOException {
        if (this.datePattern == null) {
            this.errorHandler.error("Missing DatePattern option in rollOver().");
        } else {
            String datedFilename = this.fileNamePrefix + this.sdf.format(this.now) + "_" + fileIndex + this.fileNameSuffix;
            fileIndex++;
            if (!this.scheduledFilename.equals(datedFilename)) {
                long size = ((CountingQuietWriter)this.qw).getCount();
                this.nextRollover = size + this.maxFileSize;
                this.closeFile();

                File file = new File(this.fileName);

                //新建压缩文件
                FileInputStream fis = null;
                ZipOutputStream out = null;
                byte[] buf = new byte[1024];
                try {
                    fis = new FileInputStream(file);
                    out = new ZipOutputStream(new FileOutputStream(scheduledFilename + ".zip"));
                    out.putNextEntry(new ZipEntry(file.getPath()));
                    LogLog.debug(fileName + " -> " + scheduledFilename + ".zip");

                    int len;
                    while ((len = fis.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    fis.close();

                    LogLog.debug(fileName + " -> " + scheduledFilename + ".zip successful!");

                } catch (Exception e) {
                    LogLog.error("Failed to zip [" + this.fileName + "] is error.");
                } finally {
                    if (out != null) {
                        out.closeEntry();
                        out.close();
                    }
                    if (fis != null)
                        fis.close();
                }

                //压缩文件生成后，源文件删除
                file.delete();

                //新建日志文件
                try {
                    this.setFile(this.fileName, true, this.bufferedIO, this.bufferSize);
                    this.nextRollover = 0L;
                } catch (IOException var6) {
                    this.errorHandler.error("setFile(" + this.fileName + ", true) call failed.");
                }

                //重新赋值前一次的文件名称
                this.scheduledFilename = datedFilename;
            }
        }
    }

    protected void setQWForFiles(Writer writer) {
        this.qw = new CountingQuietWriter(writer, this.errorHandler);
    }

    public synchronized void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize) throws IOException {
        super.setFile(fileName, append, this.bufferedIO, this.bufferSize);
        if (append) {
            File f = new File(fileName);
            ((CountingQuietWriter)this.qw).setCount(f.length());
        }
    }

    protected void subAppend(LoggingEvent event) {
        long n = System.currentTimeMillis();
        long size = ((CountingQuietWriter)this.qw).getCount();
        if (n >= this.nextCheck) {
            this.now.setTime(n);
            this.nextCheck = this.rc.getNextCheckMillis(this.now);

            try {
                this.rollOver();
                fileIndex = 0;
            } catch (IOException var5) {
                if (var5 instanceof InterruptedIOException) {
                    Thread.currentThread().interrupt();
                }

                LogLog.error("rollOver() failed.", var5);
            }
        } else if (size >= this.maxFileSize && size >= this.nextRollover) {
            try {
                this.rollOver();
            } catch (IOException var5) {
                if (var5 instanceof InterruptedIOException) {
                    Thread.currentThread().interrupt();
                }
                LogLog.error("rollOver() failed.", var5);
            }
        }

        super.subAppend(event);
    }

    public void setMaxFileSize(String value) {
        this.maxFileSize = OptionConverter.toFileSize(value, this.maxFileSize + 1L);
    }

    public void setFileNamePrefix(String fileNamePrefix) {
        this.fileNamePrefix = fileName.substring(0, fileName.lastIndexOf("."));
    }

    public void setFileNameSuffix(String fileNameSuffix) {
        this.fileNameSuffix = fileName.substring(fileName.lastIndexOf("."));
    }
}