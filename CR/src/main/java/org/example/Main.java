package org.example;
import java.io.*;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
public class Main {

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("C:/Users/user/Desktop/data.txt"));

        Map<BroadcastsTime, List<Program>> schedule = new TreeMap<>();
        List<Program> allPrograms = new ArrayList<>();

        String currentChannel = "";
        for (String line : lines) {
            if (line.startsWith("#")) {
                currentChannel = line.substring(1);
            } else if (line.matches("\\d{2}:\\d{2}")) {
                BroadcastsTime time = new BroadcastsTime(line);
                String title = lines.get(lines.indexOf(line) + 1);
                Program program = new Program(currentChannel, time, title);
                schedule.computeIfAbsent(time, k -> new ArrayList<>()).add(program);
                allPrograms.add(program);
            }
        }

        // 6. Вывод всех программ в порядке возрастания канала и времени показа
        allPrograms.stream()
                .sorted(Comparator.comparing(Program::getChannel).thenComparing(Program::getTime))
                .forEach(System.out::println);

        // 7. Вывод всех программ, которые идут сейчас
        BroadcastsTime now = new BroadcastsTime(LocalTime.now().toString().substring(0, 5));
        allPrograms.stream()
                .filter(p -> p.getTime().equals(now))
                .forEach(System.out::println);

        // 8. Поиск программ по названию
        String searchTitle = "Новости";
        allPrograms.stream()
                .filter(p -> p.getTitle().contains(searchTitle))
                .forEach(System.out::println);

        // 9. Поиск программ определенного канала, которые идут сейчас
        String searchChannel = "Первый";
        allPrograms.stream()
                .filter(p -> p.getChannel().equals(searchChannel) && p.getTime().equals(now))
                .forEach(System.out::println);

        // 10. Поиск программ определенного канала в промежутке времени
        BroadcastsTime startTime = new BroadcastsTime("08:00");
        BroadcastsTime endTime = new BroadcastsTime("12:00");
        allPrograms.stream()
                .filter(p -> p.getChannel().equals(searchChannel) && p.getTime().between(startTime, endTime))
                .forEach(System.out::println);

        // 11. Сохранение отсортированных данных в файл формата .xlsx
        saveToExcel(allPrograms, "program_schedule.xlsx");
        System.out.println("Данные успешно записаны в файл program_schedule.xlsx");
    }
    private static void saveToExcel(List<Program> programs, String filename) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Program Schedule");
        int rowNum = 0;
        for (Program program : programs) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(program.getChannel());
            row.createCell(1).setCellValue(program.getTime().toString());
            row.createCell(2).setCellValue(program.getTitle());
        }
        try (FileOutputStream outputStream = new FileOutputStream(filename)) {
            workbook.write(outputStream);
        }
        workbook.close();
    }
}