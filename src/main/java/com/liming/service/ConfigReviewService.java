package com.liming.service;

import com.liming.domain.Config;
import com.liming.utils.Utils;

import java.util.Scanner;


public class ConfigReviewService {

    public static void review(Config config) {


        System.out.println(config);
        Scanner sc;
        System.out.println("****请确认以上信息，并选择以下操作****");
        System.out.println("【1】：完全正确，继续执行");
        System.out.println("【2】：调整统计开始截止日期");
        System.out.println("【3】：配置有误，退出修正");

        do_while_loop_main:
        do {
            System.out.print("请输入您的选择：");
            sc = new Scanner(System.in);
            String inputStr = sc.nextLine().trim();
            switch (inputStr) {
                case "1":

                    break do_while_loop_main;
                case "2":
                    updateDateConfig(config);
                    break do_while_loop_main;
                case "3":
                    System.out.println("正在退出程序...");
                    System.exit(-1);
                default:
                    System.out.println("无效的输入！请输入数字【1,2,3】:");
            }
        } while (true);

    }

    private static void updateDateConfig(Config config) {

        Scanner sc;
        sc = new Scanner(System.in);
        String dateStr;

        System.out.println("当前设置的统计的开始日期为：" + Utils.getDateY4Str(config.getStartDate()));

        do_while_loop_1:
        do {
            System.out.print("请输入期望生效的开始日期，格式为YYYY-MM-DD ：");
            dateStr = sc.nextLine().trim();

            if (Utils.isValidDate(dateStr)) {
                config.setStartDate(Utils.parseDateY4(dateStr));
                System.out.println("修改成功！当前生效的开始日期为：" + Utils.getDateY4Str(config.getStartDate()));
                break;
            } else {
                System.out.println("输入日期无效，必须以正确格式输入一个有效的日期，如：2018-02-18");
                System.out.println("【1】：重新输入       【2】：放弃修改 ");
                System.out.print("请输入您的选择：");
                String inputStr = sc.nextLine().trim();
                switch (inputStr) {
                    case "1":
                        continue;
                    case "2":
                        System.out.println("您放弃了修改，开始日期仍是：" + Utils.getDateY4Str(config.getStartDate()));
                        break do_while_loop_1;
                    default:
                        System.out.println("无效的输入！请输入数字【1,2】:");
                }
            }
        } while (true);


        System.out.println("当前设置的统计的截止日期为：" + Utils.getDateY4Str(config.getEndDate()));

        do_while_loop_2:
        do {
            System.out.print("请输入期望生效的截止日期，格式为YYYY-MM-DD ：");
            dateStr = sc.nextLine().trim();

            if (Utils.isValidDate(dateStr)) {
                config.setEndDate(Utils.parseDateY4(dateStr));
                System.out.println("修改成功！当前生效的截止日期为：" + Utils.getDateY4Str(config.getEndDate()));
                break;
            } else {
                System.out.println("输入日期无效，必须以正确格式输入一个有效的日期，如：2018-02-18");
                System.out.println("【1】：重新输入       【2】：放弃修改 ");
                System.out.print("请输入您的选择：");
                String inputStr = sc.nextLine().trim();
                switch (inputStr) {
                    case "1":
                        continue;
                    case "2":
                        System.out.println("您放弃了修改，截止日期仍是：" + Utils.getDateY4Str(config.getEndDate()));
                        break do_while_loop_2;
                    default:
                        System.out.println("无效的输入！请输入数字【1,2】:");

                }
            }
        } while (true);
    }

}
