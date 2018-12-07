package com.liming.service;

import com.liming.domain.Config;
import com.liming.domain.PersonWithWorkdays;
import com.liming.domain.Workday;
import com.liming.utils.Utils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.CollationKey;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelService {

    public static Map<String, PersonWithWorkdays> load(Config config) throws Exception {
        //有序的Map,排序支持中文
        Map<String, PersonWithWorkdays> persons = new TreeMap<>(new Comparator() {
            Collator collator = Collator.getInstance();

            public int compare(Object element1, Object element2) {
                CollationKey key1 = collator.getCollationKey(element1.toString());
                CollationKey key2 = collator.getCollationKey(element2.toString());
                return key1.compareTo(key2);
            }
        });

        String fileName = config.getExcelFileName();
        FileInputStream fis = new FileInputStream(fileName);

        Workbook workbook = null;
        //判断excel的两种格式xls,xlsx
        if (fileName.toLowerCase().endsWith("xlsx")) {
            workbook = new XSSFWorkbook(fis);
        } else if (fileName.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook(fis);
        }

        Sheet sheet = workbook.getSheet(config.getExeclSheetName());
        if (null == sheet) {
            throw new Exception("Can NOT find sheet [ " + config.getExeclSheetName() + " ] within the Excel file:" + config.getExcelFileName());
        }

        //处理第一行，标题行，例如："每日统计 统计日期：2018-01-19 至 2018-11-03"
        String title = sheet.getRow(sheet.getFirstRowNum()).getCell(0).getStringCellValue().trim();
        String dateRegStr = "(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)";
        Pattern pattern = Pattern.compile(dateRegStr);
        Matcher m = pattern.matcher(title);
        String startDateLabel = null;
        String endDateLabel = null;
        if (m.find()) {
            startDateLabel = m.group();
        }
        if (m.find()) {
            endDateLabel = m.group();
        }
        if (null == startDateLabel
                || null == endDateLabel
                || !startDateLabel.equals(Utils.getDateY4Str(config.getStartDate()))
                || !endDateLabel.equals(Utils.getDateY4Str(config.getEndDate()))) {
            // throw new Exception("Dates information within Excel sheet title and the specified MISMATCH!");
            System.out.println("*WARN*: 请注意！正在处理的Excel文件中的起止日期信息与配置好的起止日期不一致！！！");
        }

        //找到表格头（包含列名LABEL），开始处理
        int tableHeadLineNo = -1;
        int nameColumnNo = -1;
        int workdayColumnNo = -1;
        int ontimeColumnNo = -1;
        int offtimeColumnNo = -1;
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                if (cell.getStringCellValue().equals(config.getPersonNameColumnLabel())
                        && nameColumnNo == -1) {
                    nameColumnNo = cell.getColumnIndex();
                }
                if (cell.getStringCellValue().equals(config.getWorkdayColumnLabel())
                        && workdayColumnNo == -1) {
                    workdayColumnNo = cell.getColumnIndex();
                }
                if (cell.getStringCellValue().equals(config.getOnlineTimeColumnLabel())
                        && ontimeColumnNo == -1) {
                    ontimeColumnNo = cell.getColumnIndex();
                }
                if (cell.getStringCellValue().equals(config.getOfflineTimeColumnLabel())
                        && offtimeColumnNo == -1) {
                    offtimeColumnNo = cell.getColumnIndex();
                }
            }

            if (nameColumnNo != -1 && ontimeColumnNo != -1 && offtimeColumnNo != -1) {
                tableHeadLineNo = row.getRowNum();
                break;
            } else {
                nameColumnNo = -1;
                ontimeColumnNo = -1;
                offtimeColumnNo = -1;
                workdayColumnNo = -1;
            }
        }

        if (-1 == tableHeadLineNo) {
            throw new Exception("Error: Excel sheet lack one of following columns: "
                    + config.getPersonNameColumnLabel() + ","
                    + config.getWorkdayColumnLabel() + ","
                    + config.getOnlineTimeColumnLabel() + ","
                    + config.getOfflineTimeColumnLabel());
        }

        for (int i = tableHeadLineNo + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            String personNameStr = row.getCell(nameColumnNo).getStringCellValue().trim();
            String workdayStr = row.getCell(workdayColumnNo).getStringCellValue().trim().split(" ")[0].trim();
            String ontimeStr = row.getCell(ontimeColumnNo).getStringCellValue().trim();
            String offtimeStr = row.getCell(offtimeColumnNo).getStringCellValue().trim();

            Date workdayDate;
            Date ontime = null;
            Date offtime = null;
            if (personNameStr.isEmpty()) {
                continue;
            } else if ((workdayDate = Utils.parseDateY2(workdayStr)) == null) {
                break;
            } else if ((workdayDate.before(config.getStartDate()) || workdayDate.after(new Date(config.getEndDate().getTime() + 24 * 3600 * 1000 - 1)))) {
                continue;
            } else if (!ontimeStr.isEmpty() && (ontime = Utils.extractAndParseTime(ontimeStr)) == null) {
                break;
            } else if (!offtimeStr.isEmpty() && (offtime = Utils.extractAndParseTime(offtimeStr)) == null) {
                break;
            }

            PersonWithWorkdays personWithWorkdays = persons.get(personNameStr);
            if (null == personWithWorkdays) {
                personWithWorkdays = new PersonWithWorkdays();
                personWithWorkdays.setName(personNameStr);
                persons.put(personWithWorkdays.getName(), personWithWorkdays);
            }
            if (personWithWorkdays.getWorkday(workdayDate) != null) {
                throw new Exception("Error: Dupicated work day items exist in same one sheet!");
            }
            personWithWorkdays.addWorkday(new Workday(workdayDate, ontime, offtime));
        }

        return persons;
    }

    public static void outputExcel(Config config, Map<String, PersonWithWorkdays> persons) throws Exception {


        HSSFWorkbook wb = new HSSFWorkbook();

        CellStyle nameCellStyle = wb.createCellStyle();
        nameCellStyle.setAlignment(HorizontalAlignment.CENTER); // 水平居中
        nameCellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中

        CellStyle tagCellStyle = wb.createCellStyle();
        tagCellStyle.setAlignment(HorizontalAlignment.CENTER); // 水平居中
        tagCellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        tagCellStyle.setWrapText(true);

        CellStyle workHoursCellStyle = wb.createCellStyle();
        workHoursCellStyle.setAlignment(HorizontalAlignment.CENTER); // 水平居中
        workHoursCellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        HSSFDataFormat df = wb.createDataFormat(); //此处设置数据格式  
        workHoursCellStyle.setDataFormat(df.getFormat("#,#0.00")); //小数点后保留两位，可以写contentStyle.setDataFormat(df.getFormat("#,#0.00"));

        CellStyle missingRecordCountCellStyle = wb.createCellStyle();
        missingRecordCountCellStyle.setAlignment(HorizontalAlignment.CENTER); // 水平居中
        missingRecordCountCellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        missingRecordCountCellStyle.setWrapText(true);

        CellStyle headLineCellStyle = wb.createCellStyle();
        headLineCellStyle.setAlignment(HorizontalAlignment.CENTER); // 水平居中
        headLineCellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        HSSFFont font = wb.createFont();
        font.setFontName("黑体");
        headLineCellStyle.setFont(font);
        headLineCellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.ORANGE.getIndex());//设置背景色失败，待修正
        headLineCellStyle.setFillBackgroundColor((short) 200);

        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日");
        String sheetName = sdf.format(config.getStartDate()) + "-" + sdf.format(config.getEndDate());
        HSSFSheet sheet = wb.createSheet(sheetName);

        //创建首行为标题行
        HSSFRow headRow = sheet.createRow(0);
        //将标题行的各单元格设置列名
        int nameColumn = 0;
        headRow.createCell(nameColumn).setCellValue("姓名");
        headRow.getCell(nameColumn).setCellStyle(headLineCellStyle);

        List<Date> workdayDateList = Utils.listAllDates(config.getStartDate(), config.getEndDate());
        List<Date> effectiveDateList =new ArrayList<>();
        for (PersonWithWorkdays person : persons.values()) {
            effectiveDateList = new ArrayList<>(person.getWorkdays().keySet());
            break;
        }
        boolean isDifferent=workdayDateList.retainAll(effectiveDateList);
        if(isDifferent){
            System.out.println("*WARN*: 部分数据缺失！没有找到您配置起止日期的全部数据！");
            System.out.println("已配置的日期列表:"+Utils.getDateListStr(Utils.listAllDates(config.getStartDate(), config.getEndDate())));
            System.out.println("有数据的日期列表:"+Utils.getDateListStr(workdayDateList));
            System.out.println("****请检查结果文件的日期是否满足预期！****");
        }
        int workdayColumnTotal = workdayDateList.size();
        for (int columnIndex = 1; columnIndex <= workdayColumnTotal; columnIndex++) {
            headRow.createCell(columnIndex).setCellValue(sdf.format(workdayDateList.get(columnIndex - 1)));
            headRow.getCell(columnIndex).setCellStyle(headLineCellStyle);
        }
        int workHoursTotalColumn = workdayColumnTotal + 1;
        headRow.createCell(workHoursTotalColumn).setCellValue("本周工时");
        headRow.getCell(workHoursTotalColumn).setCellStyle(headLineCellStyle);
        int missingRecordsTotalColumn = workHoursTotalColumn + 1;
        headRow.createCell(missingRecordsTotalColumn).setCellValue("本周缺卡");
        headRow.getCell(missingRecordsTotalColumn).setCellStyle(headLineCellStyle);
        //从第二行开始填写数据
        int rowIndex = 0;
        for (Map.Entry<String, PersonWithWorkdays> person : persons.entrySet()) {
            String personName = person.getKey();
            PersonWithWorkdays personWithWorkdays = person.getValue();
            HSSFRow personRow = sheet.createRow(++rowIndex);
            personRow.createCell(nameColumn).setCellValue(personName);
            personRow.getCell(nameColumn).setCellStyle(nameCellStyle);

            int columnIndex = 1;
            for (Date workdayDate : workdayDateList) {
                personRow.createCell(columnIndex).setCellValue(personWithWorkdays.getWorkday(workdayDate).getTag());
                personRow.getCell(columnIndex).setCellStyle(tagCellStyle);
                ++columnIndex;
            }
            if (columnIndex != workHoursTotalColumn) {
                throw new RuntimeException("Error: columns misplacements exit!");
            }


            personRow.createCell(workHoursTotalColumn).setCellValue(personWithWorkdays.getWorkHourTotal());
            personRow.getCell(workHoursTotalColumn).setCellStyle(workHoursCellStyle);

            personRow.createCell(missingRecordsTotalColumn).setCellValue(personWithWorkdays.getMissingRecordCounter());
            personRow.getCell(missingRecordsTotalColumn).setCellStyle(missingRecordCountCellStyle);
        }

        File outputExcelFile;
        String fileNameMain = config.getOutputFilePath() + "\\" + config.getOutputFileNamePrefix() + "(" + sdf.format(config.getStartDate()) + "-" + sdf.format(config.getEndDate()) + ")";
        String fileName = fileNameMain + ".xls";
        int code = 1;
        while ((outputExcelFile = new File(fileName)).exists()) {
            fileName = fileNameMain + "(" + code + ")" + ".xls";
            code++;
        }

        FileOutputStream fileOut = new FileOutputStream(outputExcelFile);

        wb.write(fileOut);

        fileOut.close();
        System.out.println("结果文件已经生成：" + outputExcelFile.getAbsolutePath());
        System.out.println("处理完毕！");

    }

}
