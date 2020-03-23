package com.mss.crawler.spiderjson.util;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Set;

/**
 * @Author wgsh
 * @Date wgshb on 2019/4/28 15:29
 */
public class ChromeDriverUtils {

	// 判断某个元素是否存在
	public static boolean doesWebElementExist(WebDriver driver, String xpath)
	{

		try
		{
			driver.findElement(By.xpath(xpath));
			return true;
		}
		catch (NoSuchElementException e)
		{
			return false;
		}
	}

	/**
	 * 获取指定的地址的返回值, 获取 JS 内容加载网页内容
	 */
	public static String getSourceByUrl(String url) {
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.addArguments("--headless");
		WebDriver driver = new ChromeDriver(chromeOptions);
		String result = "";
		try {
			driver.get(url);
			result = driver.getPageSource();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.quit();
		}

		return null==result?"":result;
	}

	/**
	 * 返回普通的 chromeDriver
	 * @return
	 */
	public static ChromeDriver getDefaultChromeDriver() {
		return new ChromeDriver();
	}

	/**
	 * 返回无头的 ChromeDriver
	 */
	public static ChromeDriver getHeadlessChromeDrver() {
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.addArguments("--headless");
		return new ChromeDriver(chromeOptions);
	}

	/**
	 * 切换视图
	 */
	public static WebDriver switchHandle(ChromeDriver driver) {
		WebDriver window = null;
		Set<String> windowHandles = driver.getWindowHandles();
		for (String windowhandle : windowHandles) {
			if (windowhandle != driver.getWindowHandle()) {
				window = driver.switchTo().window(windowhandle);
			}
		}
		return window;
	}

}
