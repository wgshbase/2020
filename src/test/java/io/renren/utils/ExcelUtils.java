package io.renren.utils;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author DevinLiu
 * 通用excel导入数据库
 */
public class ExcelUtils {

	/**
	 * 获取数据
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, String>> readExcel(File file) throws Exception {

		// 创建输入流，读取Excel
		InputStream is = new FileInputStream(file.getAbsolutePath());
		// jxl提供的Workbook类
		Workbook wb = Workbook.getWorkbook(is);
		// 只有一个sheet,直接处理
		//创建一个Sheet对象
		Sheet sheet = wb.getSheet(0);
		// 得到所有的行数
		int rows = sheet.getRows();
		// 所有的数据
		List<Map<String, String>> allData = new ArrayList<Map<String, String>>();
		Cell[] columnNames = sheet.getRow(0);
		List<String> columns = new ArrayList<>();
		columns.add("classification_number");
		columns.add("major_subject");
		columns.add("subject_segmentation");
		columns.add("translated_name");
		columns.add("isbn");
		columns.add("title");
		columns.add("deputy_title");
		columns.add("book_name");
		columns.add("conference_proceedings");
		columns.add("version");
		columns.add("author");
		columns.add("editor");
		columns.add("publishing_house");
		columns.add("publication_date");
		columns.add("number_pages");
		columns.add("binding");
		columns.add("currency");
		columns.add("price");
		columns.add("introduction");
		columns.add("language");
		columns.add("illus");
		columns.add("subject_heading");
		columns.add("reader_object");
		columns.add("fine_paperback_isbn_control");
		columns.add("size");
		/*for(int i = 0; i < columnNames.length; i++) {
			System.out.println(columnNames[i].getContents().trim());
		}*/
		// 越过第一行 它是列名称
		for (int j = 1; j < rows; j++) {

			Map<String, String> oneData = new HashMap<String,String>();
			oneData.put("xlsname", file.getName().substring(file.getName().lastIndexOf("-")+1, file.getName().lastIndexOf(".")));
			// 得到每一行的单元格的数据
			/*Cell[] cells = sheet.getRow(j);
			System.out.println(cells.length);
			for (int k = 0; k < cells.length; k++) {
				oneData.add(cells[k].getContents().trim());
			}*/
			for(int i = 0; i < columns.size(); i++) {
				Cell cell = sheet.getCell(i, j);
				oneData.put(columns.get(i), cell.getContents().trim());
			}
			// 存储每一条数据
			allData.add(oneData);
			// 打印出每一条数据
			//System.out.println(oneData);

		}
		return allData;

	}

	public static void main(String[] args) throws Exception {
		ExcelUtils eu = new ExcelUtils();
		System.out.println(ExcelUtils.readExcel(new File("C:\\Users\\wgshb\\Desktop\\军队-磁场环境.xls")).size());
		System.out.println(ExcelUtils.readExcel(new File("C:\\Users\\wgshb\\Desktop\\军队-核生化.xls")).size());
		System.out.println(ExcelUtils.readExcel(new File("C:\\Users\\wgshb\\Desktop\\军队-太空战场环境要素.xls")).size());

	}
}