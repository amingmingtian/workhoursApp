package com.liming.domain;

import com.liming.utils.Utils;

import java.io.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;


public class Config {
    public static final String EXCEL_FILE_FULL_NAME = "excel.file.name";
    public static final String EXCEL_SHEET_NAME = "excel.sheet.name";
    public static final String EXCEL_PERSON_NAME_COLUMN_LABEL = "excel.person.name.label";
    public static final String EXCEL_WORKDAY_DATE_COLUMN_LABEL = "excel.workday.date.label";
    public static final String EXCEL_ONTIME_COLUMN_LABEL = "excel.ontime.label";
    public static final String EXCEL_OFFTIME_COLUMN_LABEL = "excel.offtime.label";
    public static final String CRITERIA_WORK_ON_TIME = "criteria.work.ontime";
    public static final String CRITERIA_WORK_OFF_TIME = "criteria.work.offtime";
    public static final String DEFAULT_WORK_ON_TIME = "default.work.ontime";
    public static final String DEFAULT_WORK_OFF_TIME = "default.work.offtime";
    public static final String OUTPUT_FILE_PATH = "output.file.path";
    public static final String OUTPUT_FILE_NAME_PREFIX = "output.file.name.prefix";

    private static String static_defaultFilePath;

    {
        static_defaultFilePath = System.getProperty("user.dir");
    }


    private String excelFileName;
    private String execlSheetName = "每日统计";
    private String personNameColumnLabel = "姓名";
    private String workdayColumnLabel = "日期";
    private String onlineTimeColumnLabel = "上班1打卡时间";
    private String offlineTimeColumnLabel = "下班1打卡时间";
    private Date criteriaOntime = Utils.parseTime("9:30");
    private Date criteriaOfftime = Utils.parseTime("18:00");
    private Date defaultOntime = Utils.parseTime("9:30");
    private Date defaultOfftime = Utils.parseTime("18:30");
    private Date startDate;
    private Date endDate;
    private String outputFilePath = static_defaultFilePath;
    private String outputFileNamePrefix = "亚信平台组人员本周考勤情况汇总";

    private static Config config;

    //singleton
    private Config() {
    }

    public static Config getConfig() throws Exception {
        if (null == config) {
            throw new Exception("Error: you should init the Config first by invoking: Config.initConfig(String configFilename)");
        }
        return config;
    }

    public static void initConfig(String configFilename) throws Exception {

        if (config != null) {
            throw new Exception("Error: Never init Config more than once!");
        }

        File configFile = new File(configFilename);
        if (!configFile.exists()) {
            throw new Exception("Can NOT find Config file : " + configFile.getPath());
        } else if (!configFile.isFile()) {
            throw new Exception("Invalid Config file (maybe a dir) : " + configFile.getPath());
        } else if (!configFile.canRead()) {
            throw new Exception("READ Permission is needed for file: " + configFile.getPath());
        }

        System.out.println("INFO: 本次处理使用的配置文件是: "+configFile.getAbsolutePath());
        Properties props = new Properties();
        InputStreamReader is = new InputStreamReader(new FileInputStream(configFile), "UTF-8");
        props.load(is);

        //唯一产生对象的地方
        config = new Config();


        String excelSheetNameStr = props.getProperty(EXCEL_SHEET_NAME);
        if (null == excelSheetNameStr || excelSheetNameStr.trim().isEmpty()) {
            System.out.println("INFO: No Valid " + EXCEL_SHEET_NAME + " is specified, use default: " + config.getExeclSheetName());
        } else {
            config.setExeclSheetName(excelSheetNameStr.trim());
        }

        String excelPersonNameLabelStr = props.getProperty(EXCEL_PERSON_NAME_COLUMN_LABEL);
        if (null == excelPersonNameLabelStr || excelPersonNameLabelStr.trim().isEmpty()) {
            System.out.println("INFO: No Valid " + EXCEL_PERSON_NAME_COLUMN_LABEL + " is specified, use default: " + config.getPersonNameColumnLabel());
        } else {
            config.setPersonNameColumnLabel(excelPersonNameLabelStr.trim());
        }
        String excelworkdayLabelStr = props.getProperty(EXCEL_WORKDAY_DATE_COLUMN_LABEL);
        if (null == excelworkdayLabelStr || excelworkdayLabelStr.trim().isEmpty()) {
            System.out.println("INFO: No Valid " + EXCEL_WORKDAY_DATE_COLUMN_LABEL + " is specified, use default: " + config.getOnlineTimeColumnLabel());
        } else {
            config.setWorkdayColumnLabel(excelworkdayLabelStr.trim());
        }
        String excelOntimeLabelStr = props.getProperty(EXCEL_ONTIME_COLUMN_LABEL);
        if (null == excelOntimeLabelStr || excelOntimeLabelStr.trim().isEmpty()) {
            System.out.println("INFO: No Valid " + EXCEL_ONTIME_COLUMN_LABEL + " is specified, use default: " + config.getOnlineTimeColumnLabel());
        } else {
            config.setOnlineTimeColumnLabel(excelOntimeLabelStr.trim());
        }

        String excelOfftimeLabelStr = props.getProperty(EXCEL_OFFTIME_COLUMN_LABEL);
        if (null == excelOfftimeLabelStr || excelOfftimeLabelStr.trim().isEmpty()) {
            System.out.println("INFO: No Valid " + EXCEL_OFFTIME_COLUMN_LABEL + " is specified, use default: " + config.getOfflineTimeColumnLabel());
        } else {
            config.setOfflineTimeColumnLabel(excelOfftimeLabelStr.trim());
        }

        String outputFilePathStr = props.getProperty(OUTPUT_FILE_PATH);
        if (null == outputFilePathStr || outputFilePathStr.trim().isEmpty()) {
            System.out.println("INFO: No Valid " + OUTPUT_FILE_PATH + " is specified, use default: " + config.getOutputFilePath());
        } else {
            File outputPath = new File(outputFilePathStr);
            if (outputPath.isDirectory() && outputPath.canWrite()) {
                config.setOutputFilePath(outputPath.getAbsolutePath());
            } else if (outputPath.mkdirs()) {
                config.setOutputFilePath(outputPath.getAbsolutePath());
            } else {
                config.setOutputFilePath(static_defaultFilePath);
            }
        }

        String outputFileNamePrefixStr = props.getProperty(OUTPUT_FILE_NAME_PREFIX);
        if (null == outputFileNamePrefixStr || outputFileNamePrefixStr.trim().isEmpty()) {
            System.out.println("INFO: No Valid " + OUTPUT_FILE_NAME_PREFIX + " is specified, use default: " + config.getOutputFileNamePrefix());
        } else {
            config.setOutputFileNamePrefix(outputFileNamePrefixStr.trim());
        }

        String criteriaOntimeStr = props.getProperty(CRITERIA_WORK_ON_TIME);
        if (null == criteriaOntimeStr || !Utils.isValidTime(criteriaOntimeStr.trim())) {
            System.out.println("INFO: No Valid " + CRITERIA_WORK_ON_TIME + " is specified, use default: " + config.getCriteriaOntime());
        } else {
            config.setCriteriaOntime(Utils.parseTime(criteriaOntimeStr.trim()));
        }

        String criteriaOfftimeStr = props.getProperty(CRITERIA_WORK_OFF_TIME);
        if (null == criteriaOfftimeStr || !Utils.isValidTime(criteriaOfftimeStr.trim())) {
            System.out.println("INFO: No Valid " + CRITERIA_WORK_OFF_TIME + " is specified, use default: " + config.getCriteriaOfftime());
        } else {
            config.setCriteriaOfftime(Utils.parseTime(criteriaOfftimeStr.trim()));
        }

        String defaultOntimeStr = props.getProperty(DEFAULT_WORK_ON_TIME);
        if (null == defaultOntimeStr || !Utils.isValidTime(defaultOntimeStr.trim())) {
            System.out.println("INFO: No Valid " + DEFAULT_WORK_ON_TIME + " is specified, use default: " + config.getDefaultOntime());
        } else {
            config.setDefaultOntime(Utils.parseTime(defaultOntimeStr.trim()));
        }

        String defaultOfftimeStr = props.getProperty(DEFAULT_WORK_OFF_TIME);
        if (null == defaultOfftimeStr || !Utils.isValidTime(defaultOfftimeStr.trim())) {
            System.out.println("INFO: No Valid " + DEFAULT_WORK_OFF_TIME + " is specified, use default: " + config.getDefaultOfftime());
        } else {
            config.setDefaultOfftime(Utils.parseTime(defaultOfftimeStr.trim()));
        }

        Date lastMonday = getDateOfLastWeek(1);
        Date lastFriday = getDateOfLastWeek(5);
        config.setStartDate(lastMonday);
        config.setEndDate(lastFriday);

        String finalExcelFileName = null;
        String excelFilenameStr = props.getProperty(EXCEL_FILE_FULL_NAME);
        File targetFileOrDir;
        if (null != excelFilenameStr && !excelFilenameStr.trim().isEmpty()){
            targetFileOrDir = new File(excelFilenameStr.trim()+File.separator);

            if (!targetFileOrDir.exists()) {
                System.out.println("Can NOT find the file:" + targetFileOrDir.getPath());
            } else if (targetFileOrDir.isFile()) {
                if (!targetFileOrDir.canRead()) {
                    System.out.println("Lack of READ permission of the file:" + targetFileOrDir.getPath());
                }
                finalExcelFileName = targetFileOrDir.getPath();
            } else if (targetFileOrDir.isDirectory()) {
                System.out.println("A directory is specified, try to scan excel file within it:" + targetFileOrDir.getPath());
                File excelFile = findLastModifiedExcelFileWithinDir(targetFileOrDir);
                if (null != excelFile) {
                    finalExcelFileName = excelFile.getPath();
                }
            }
        }

        if (null == finalExcelFileName) {
            System.out.println("INFO:  excel.file.name 未指定, 尝试从下列路径中寻找: " + static_defaultFilePath);
            File excelFile = findLastModifiedExcelFileWithinDir(new File(static_defaultFilePath));
            if (null != excelFile) {
                finalExcelFileName = excelFile.getPath();
            }
        }
        if (null == finalExcelFileName) {
            String errorInfo = "ERROR: .xls or .xlsx file NOT FOUND!";
            System.out.println(errorInfo);
            throw new FileNotFoundException(errorInfo);
        }
        config.setExcelFileName(finalExcelFileName);
    }

    private static File findLastModifiedExcelFileWithinDir(File dir) {
        if (!dir.isDirectory()) {
            return null;
        }

        File[] excelFiles = dir.listFiles((a, b) -> (b.toLowerCase().endsWith(".xls") || b.toLowerCase().endsWith(".xlsx")) && !b.contains(config.getOutputFileNamePrefix()));

        if (excelFiles.length > 0) {
            Arrays.sort(excelFiles, (oldFile, newFile) -> (int) (newFile.lastModified() - oldFile.lastModified()));
            return excelFiles[0];
        }
        return null;
    }

    public static Date getDateOfLastWeek(int ChineseWeekdayIndex) {

        if (ChineseWeekdayIndex < 1 || ChineseWeekdayIndex > 7) {
            throw new RuntimeException("ERROR: ChineseWeekdayIndex: " + ChineseWeekdayIndex + " is out of range [1-7] !!!");
        }

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int offset = ChineseWeekdayIndex - dayOfWeek;
        calendar.add(Calendar.DATE, offset - 7);
        String dateStr = Utils.getDateY4Str(calendar.getTime());
        return Utils.parseDateY4(dateStr);
    }


    @Override
    public String toString() {
        return "----Config Start----" +
                "\n****配置信息如下:****" +
                "\n 您要处理的Excel文件名称是：'" + excelFileName + '\'' +
                "\n 您要处理的Excel表格名称是：'" + execlSheetName + '\'' +
                "\n 人员名字对应表格列的标签是：'" + personNameColumnLabel + '\'' +
                "\n 上班日期对应表格列的标签是：'" + workdayColumnLabel + '\'' +
                "\n 上班时间对应表格列的标签是：'" + onlineTimeColumnLabel + '\'' +
                "\n 下班时间对应表格列的标签是：'" + offlineTimeColumnLabel + '\'' +
                "\n 上班时间（迟到判定标准）是：'" + Utils.getTimeStr(criteriaOntime) + '\'' +
                "\n 下班时间（早退判定标准）是：'" + Utils.getTimeStr(criteriaOfftime) + '\'' +
                "\n 上班缺卡默认时间（统计）是：'" + Utils.getTimeStr(defaultOntime) + '\'' +
                "\n 下班缺卡默认时间（统计）是：'" + Utils.getTimeStr(defaultOfftime) + '\'' +
                "\n 此次统计的开始日期是：'" + Utils.getDateY4Str(startDate) + '\'' +
                "\n 此次统计的截止日期是：'" + Utils.getDateY4Str(endDate) + '\'' +
                "\n 结果Excel文件的输出路径是：'" + outputFilePath + '\'' +
                "\n 结果Excel文件的名称模式是：'" + outputFileNamePrefix + "(MM月dd日-MM月dd日).xls"+'\'' +
                "\n-----End----";
    }

    public String getExcelFileName() {
        return excelFileName;
    }

    public void setExcelFileName(String excelFileName) {
        this.excelFileName = excelFileName;
    }

    public String getExeclSheetName() {
        return execlSheetName;
    }

    public void setExeclSheetName(String execlSheetName) {
        this.execlSheetName = execlSheetName;
    }

    public String getPersonNameColumnLabel() {
        return personNameColumnLabel;
    }

    public void setPersonNameColumnLabel(String personNameColumnLabel) {
        this.personNameColumnLabel = personNameColumnLabel;
    }

    public String getWorkdayColumnLabel() {
        return workdayColumnLabel;
    }

    public void setWorkdayColumnLabel(String workdayColumnLabel) {
        this.workdayColumnLabel = workdayColumnLabel;
    }

    public String getOnlineTimeColumnLabel() {
        return onlineTimeColumnLabel;
    }

    public void setOnlineTimeColumnLabel(String onlineTimeColumnLabel) {
        this.onlineTimeColumnLabel = onlineTimeColumnLabel;
    }

    public String getOfflineTimeColumnLabel() {
        return offlineTimeColumnLabel;
    }

    public void setOfflineTimeColumnLabel(String offlineTimeColumnLabel) {
        this.offlineTimeColumnLabel = offlineTimeColumnLabel;
    }

    public Date getCriteriaOntime() {
        return criteriaOntime;
    }

    public void setCriteriaOntime(Date criteriaOntime) {
        this.criteriaOntime = criteriaOntime;
    }

    public Date getCriteriaOfftime() {
        return criteriaOfftime;
    }

    public void setCriteriaOfftime(Date criteriaOfftime) {
        this.criteriaOfftime = criteriaOfftime;
    }

    public Date getDefaultOntime() {
        return defaultOntime;
    }

    public void setDefaultOntime(Date defaultOntime) {
        this.defaultOntime = defaultOntime;
    }

    public Date getDefaultOfftime() {
        return defaultOfftime;
    }

    public void setDefaultOfftime(Date defaultOfftime) {
        this.defaultOfftime = defaultOfftime;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public String getOutputFileNamePrefix() {
        return outputFileNamePrefix;
    }

    public void setOutputFileNamePrefix(String outputFileNamePrefix) {
        this.outputFileNamePrefix = outputFileNamePrefix;
    }

}
