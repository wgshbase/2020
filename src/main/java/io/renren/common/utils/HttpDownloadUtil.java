package io.renren.common.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

/**
 * 文件下载工具类
 *
 * @author hexin
 * @date 2018/09/29
 */
public class HttpDownloadUtil {

    private HttpDownloadUtil() {
    }

    /**
     * 准备文件下载
     *
     * @param displayName 客户端显示的下载文件名
     */
    public static void prepareDownload(String displayName, HttpServletRequest request, HttpServletResponse response) {
        displayName = displayName.trim();
        try {
            boolean isIE = request.getHeader("User-Agent").toUpperCase().contains("MSIE");
            if (isIE) {
                displayName = URLEncoder.encode(displayName, "UTF-8");
            } else {
                displayName = new String(displayName.getBytes(), "ISO-8859-1");
            }
            // name.getBytes("UTF-8")处理Safari的乱码问题
            // display = new String(isIE ? filename.getBytes() : display.getBytes("UTF-8"), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
        }

        response.reset();
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Disposition", "attachment; filename=\"" + displayName + "\"");
    }

    /**
     * 文件下载
     *
     * <pre>
     * download("/home/test.txt", null, request, response)      ===> test.txt
     * download("/home/test.txt", "", request, response)        ===> test.txt
     * download("/home/test.txt", "new", request, response)     ===> new.txt
     * download("/home/test.txt", "new.csv", request, response) ===> new.csv
     * </pre>
     *
     * @param filepath 文件本地绝对路径
     * @param display  客户端显示的文件名。如果不包含扩展文件名则自动追加本地文件的扩展文件名，如果包含则不修改
     */
    public static void download(String filepath, String display, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        download(new File(filepath), display, request, response);
    }

    /**
     * 文件下载
     *
     * <pre>
     * download(new File("/home/test.txt"), null, request, response)      ===> test.txt
     * download(new File("/home/test.txt"), "", request, response)        ===> test.txt
     * download(new File("/home/test.txt"), "new", request, response)     ===> new.txt
     * download(new File("/home/test.txt"), "new.csv", request, response) ===> new.csv
     * </pre>
     *
     * @param file    文件本地绝对路径
     * @param display 客户端显示的文件名。如果不包含扩展文件名则自动追加本地文件的扩展文件名，如果包含则不修改
     */
    public static void download(File file, String display, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (display == null) {
            display = file.getName();
        }
        // 文件下载
        download(new FileInputStream(file), display, request, response);
    }

    /**
     * 文件流下载
     *
     * @param display 客户端显示的文件名，不能为空。如：file.txt
     */
    public static void download(InputStream in, String display, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareDownload(display, request, response);
        // 设置文件大小
        response.addHeader("Content-Length", String.valueOf(in.available()));

        BufferedInputStream bis = null;
        OutputStream out = null;
        try {
            bis = new BufferedInputStream(in);
            out = response.getOutputStream();
            byte[] b = new byte[1024];
            int len;
            while ((len = in.read(b)) != -1) {
                out.write(b, 0, len);
            }
            out.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            close(out);
            close(bis);
        }
    }

    public static void download(Write write, String display, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareDownload(display, request, response);

        OutputStream out = null;
        try {
            out = response.getOutputStream();
            write.write(out);
            out.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            close(out);
        }
    }

    /**
     * 安全关闭
     */
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
            }
        }
    }

    public interface Write {
        /**
         * 无需处理out的关闭问题
         */
        void write(OutputStream out) throws IOException;
    }

}
