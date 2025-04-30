package cn.moongn.coworkhub.common.utils;

import org.quartz.CronExpression;

public class CronUtils {

    /**
     * 验证cron表达式是否有效
     * @param cronExpression cron表达式
     * @return 是否有效
     */
    public static boolean isValid(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            return false;
        }

        try {
            return CronExpression.isValidExpression(cronExpression);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取cron表达式的描述
     * @param cronExpression cron表达式
     * @return 描述文本
     */
    public static String getDescription(String cronExpression) {
        if (!isValid(cronExpression)) {
            return "无效的表达式";
        }

        try {
            // 简单描述逻辑
            String[] parts = cronExpression.split("\\s+");
            if (parts.length < 6) {
                return "表达式格式不完整";
            }

            StringBuilder desc = new StringBuilder();
            String seconds = parts[0];
            String minutes = parts[1];
            String hours = parts[2];
            String dayOfMonth = parts[3];
            String month = parts[4];
            String dayOfWeek = parts[5];

            // 每天特定时间
            if (dayOfMonth.equals("*") && month.equals("*") && (dayOfWeek.equals("?") || dayOfWeek.equals("*"))) {
                desc.append("每天");
                if (!hours.equals("*")) {
                    if (hours.contains("/")) {
                        String[] hourParts = hours.split("/");
                        desc.append("每").append(hourParts[1]).append("小时");
                    } else if (hours.contains(",")) {
                        desc.append("在").append(hours.replace(",", "、")).append("点");
                    } else {
                        desc.append(hours).append("点");
                    }
                }

                if (!minutes.equals("*") && !minutes.equals("0")) {
                    if (minutes.contains("/")) {
                        String[] minParts = minutes.split("/");
                        desc.append("每").append(minParts[1]).append("分钟");
                    } else if (minutes.contains(",")) {
                        desc.append(minutes.replace(",", "、")).append("分");
                    } else {
                        desc.append(minutes).append("分");
                    }
                }

                desc.append("执行");
            }
            // 每周特定时间
            else if (dayOfMonth.equals("?") && month.equals("*") && !dayOfWeek.equals("?") && !dayOfWeek.equals("*")) {
                desc.append("每周");

                String weekDay = dayOfWeek;
                if (weekDay.equals("1") || weekDay.equals("MON")) {
                    desc.append("一");
                } else if (weekDay.equals("2") || weekDay.equals("TUE")) {
                    desc.append("二");
                } else if (weekDay.equals("3") || weekDay.equals("WED")) {
                    desc.append("三");
                } else if (weekDay.equals("4") || weekDay.equals("THU")) {
                    desc.append("四");
                } else if (weekDay.equals("5") || weekDay.equals("FRI")) {
                    desc.append("五");
                } else if (weekDay.equals("6") || weekDay.equals("SAT")) {
                    desc.append("六");
                } else if (weekDay.equals("7") || weekDay.equals("SUN")) {
                    desc.append("日");
                } else {
                    desc.append(weekDay);
                }

                desc.append("的").append(hours).append("点");

                if (!minutes.equals("0")) {
                    desc.append(minutes).append("分");
                }

                desc.append("执行");
            } else {
                return cronExpression;
            }

            return desc.toString();
        } catch (Exception e) {
            return cronExpression;
        }
    }
}