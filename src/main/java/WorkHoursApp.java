import com.liming.domain.Config;
import com.liming.domain.PersonWithWorkdays;
import com.liming.service.ConfigReviewService;
import com.liming.service.ExcelService;


import java.io.IOException;
import java.util.Map;


public class WorkHoursApp {
    public static void main(String[] args) {


        System.out.println("欢迎使用WorkHoursApp制作考勤报表!");
        System.out.println("程序运行的当前路径为："+System.getProperty("user.dir"));

        String configFileName = null;
        if (args.length != 0) {
            switch (args[0].toLowerCase()) {
                case "-h":
                case "--help":
                    System.out.println("Usage: java " + System.getProperty("sun.java.command").split(" ")[0] + "  [$config-file]");
                    System.out.println("Note : lack of config file triggers SEARCHING 'conf.properties' file in path:" + System.getProperty("user.dir"));
                    return;
                default:
                    break;
            }

            configFileName = args[0];
        } else {

            System.out.println("INFO: 未指定配置文件参数，将从当前路径查找文件：conf.properties(默认文件名)");
            configFileName = System.getProperty("user.dir") + "/conf.properties";
        }

        //读取配置文件初始化配置信息
        Config config;
        try {
            Config.initConfig(configFileName);
            config = Config.getConfig();
        } catch (Exception e) {
            System.out.println("Init ERROR : Please have a check and retry! Prompt messages are given as following:");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        }

        //与操作员交互确认配置信息
        ConfigReviewService.review(config);

        System.out.println("请稍等，正在处理...");

        //从原始Excel抽取数据
        Map<String, PersonWithWorkdays> persons = null;
        try {
            persons = ExcelService.load(config);

        } catch (Exception e) {
            System.out.println("Excel Load ERROR: Please have a check and retry! Prompt messages are given as following:");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        }

        System.out.println("如需核对考勤数据，请参考以下信息：");
        //对人员考勤信息进行统计，同时输出信息到控制台以备操作员核对
        int i = 0;
        for (PersonWithWorkdays person : persons.values()) {
            person.doStatisticsAndAddTagsForWorkdays(config);
            try {
                System.out.println("no." + (i++) + " :" + person);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("正在生成Excel结果文件...");

        //将统计信息输出为Excel文件
        try {
            ExcelService.outputExcel(config, persons);
        } catch (Exception e) {
            System.out.println("Excel output ERROR: Please have a check and retry! Prompt messages are given as following:");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }


        System.out.println("按回车键退出...");
        try {  System.in.read(); } catch (IOException e) {e.printStackTrace(); }
    }


}
