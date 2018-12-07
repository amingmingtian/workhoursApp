package com.liming.domain;

import com.liming.utils.Utils;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;


public class PersonWithWorkdays {
    private String name;
    private Map<Date, Workday> workdays = new TreeMap<>();
    private boolean isStatsFinished;
    private float workHourTotal = 0;
    private int missingRecordCounter = 0;


    public void resetStatsFinishedFlag() {
        for (Workday workday : workdays.values()) {
            workday.setStatsFinished(false);
            workday.setWorkHours(0);
            workday.setTag(null);
        }

        isStatsFinished = false;
        workHourTotal = 0;
        missingRecordCounter = 0;
    }

    public void doStatisticsAndAddTagsForWorkdays(Config config) {
        doStatistics(config);
        tagWorkdays(config);

    }

    public void doStatistics(Config config) {

        for (Workday workday : workdays.values()) {

            if (workday.getOntime() == null && workday.getOfftime() == null) {
                workday.setWorkHours(0);
                workday.setStatsFinished(true);
                continue;
            }

            float dayWorkhours;
            if (workday.getOntime() == null) {
                dayWorkhours = Utils.calculateIntervalHours(config.getDefaultOntime(), workday.getOfftime());
            } else if (workday.getOfftime() == null) {
                dayWorkhours = Utils.calculateIntervalHours(workday.getOntime(), config.getDefaultOfftime());
            } else {
                dayWorkhours = Utils.calculateIntervalHours(workday.getOntime(), workday.getOfftime());
            }
            if (dayWorkhours > 24) {
                throw new RuntimeException("Error: impossible work hours (should < 24) : " + dayWorkhours);
            }
            workday.setWorkHours(dayWorkhours);
            workday.setStatsFinished(true);
            workHourTotal += dayWorkhours;
        }

        isStatsFinished = true;

    }

    public void tagWorkdays(Config config) {
        for (Workday workday : workdays.values()) {
            if (workday.getOntime() == null && workday.getOfftime() == null) {
                workday.setTag("旷工");
                continue;
            }

            if (workday.getOntime() == null) {
                missingRecordCounter++;
                workday.setTag("上班缺卡");
            } else if (workday.getOntime().after(config.getCriteriaOntime())) {
                workday.setTag("上班迟到");
            }

            if (workday.getOfftime() == null) {
                missingRecordCounter++;
                if (workday.getTag() == null) {
                    workday.setTag("下班缺卡");
                } else {
                    workday.setTag(workday.getTag() + "\r\n下班缺卡");
                }
            } else if (Utils.isAfternoon(workday.getOfftime()) && workday.getOfftime().before(config.getCriteriaOfftime())) {
                if (workday.getTag() == null) {
                    workday.setTag("下班早退");
                } else {
                    workday.setTag(workday.getTag() + "\r\n下班早退");
                }
            }

            if (workday.getTag() == null) {
                workday.setTag("正常");
            }
        }
    }


    public boolean isStatsFinished() {
        return isStatsFinished;
    }


    public float getWorkHourTotal() throws Exception {
        if (!isStatsFinished) {
            throw new Exception("Error: doStatistics() must go first!");
        }

        return workHourTotal;
    }

    public int getMissingRecordCounter() {
        return missingRecordCounter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Date, Workday> getWorkdays() {
        return workdays;
    }

    public Workday getWorkday(Date date) {
        return workdays.get(date);
    }

    public void addWorkday(Workday workday) throws Exception {
        if (isStatsFinished) {
            throw new Exception("Error: resetStatsFinishedFlag() before addWorkday() after statistics finished");
        }
        this.workdays.put(workday.getDate(), workday);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "name:'" + name + '\'' +
                ", workdays:{\"");
        for (Workday workday : workdays.values()) {
sb.append(workday + ",");
        }
        sb.setLength(sb.length()-1);
        sb.append('}');

        return sb.toString();}
}