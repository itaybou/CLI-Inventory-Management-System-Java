package logic.datatypes;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;

/* Date Data type */
public class Date {

    private int day;
    private int month;
    private int year;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Date(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public String toString()
    {
        return String.format("%d-%s-%s", year, (month / 10 == 0) ? "0"+month : month+"", (day / 10 == 0) ? "0"+day : day+"");
    }

    public static Date getCurrentDate()
    {
        LocalDateTime now = LocalDateTime.now();
        return parseDate(formatter.format(now));
    }

    public static Date parseDate(String date)
    {
        if(date == null)
            return null;
        int d, y, m;
        StringTokenizer tokenizer = new StringTokenizer(date, "-");
        try {
            y = Integer.parseInt(tokenizer.nextToken());
            m = Integer.parseInt(tokenizer.nextToken());
            d = Integer.parseInt(tokenizer.nextToken());
        } catch (Exception e) {
            return null;
        }
        return new Date(d, m, y);
    }

    public static boolean checkLegalDate(Date date)
    {
        return (date.getDay() >= 1 && date.getDay() <=31) &&
                (date.getMonth() >= 1 && date.getMonth() <=12) &&
                date.getYear() >= Year.now().getValue();
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
