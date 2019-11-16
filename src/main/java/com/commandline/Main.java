package com.commandline;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {

    private static File dir;
    private static List<String> list = new ArrayList<>();
    private static final DateTimeFormatter DATE_FORMATTER_WITH_TIME = DateTimeFormatter
            .ofPattern("dd.MM.yyyy HH:mm");


    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        readInputFilePath(scanner, "Please Enter Input file path: ");
        readOutputFilePath(scanner, "Please Enter Output file path: ");

    }

    private static String readOutputFilePath(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            String inputTxt = scanner.nextLine();
            if (inputTxt != null) {
                try {
                    if (!StringUtils.substringAfterLast(inputTxt, ".").equalsIgnoreCase("txt")) {
                        throw new InputMismatchException();
                    }
                    writeToOutputFile(list, inputTxt);
                    return scanner.nextLine();
                } catch (InputMismatchException | IOException e) {
                    System.out.println("Wrong file path, try again");
                }
            }
        }
    }


    private static String readInputFilePath(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            String inputTxt = scanner.nextLine();
            if (inputTxt != null) {
                try {
                    if (!new File(inputTxt).exists()) {
                        throw new InputMismatchException();
                    }
                    BufferedReader br = new BufferedReader(new FileReader(inputTxt));

                    dir = new File(StringUtils.substringBeforeLast(inputTxt, "/"));

                    for (String line; (line = br.readLine()) != null; ) {
                        if (line.equalsIgnoreCase("dir") || line.equalsIgnoreCase("ls")) {
                            dirCommand(dir.getPath());
                        } else if (line.contains("mkdir")) {
                            mkdirCommand(dir.getPath(), line);
                        } else if (line.contains("cd")) {
                            cdCommand(line);
                        } else if (line.contains("mkfile")) {
                            mkfileCommand(line);
                        } else if (line.contains("rmdir")) {
                            rmdirCommand(line);
                        }
                    }
                    return scanner.nextLine();
                } catch (InputMismatchException | IOException e) {
                    System.out.println("Wrong file path, try again");
                }
            }
        }
    }

    private static void dirCommand(String path) throws IOException {
        File directory = new File(path);
        File[] content = directory.listFiles();
        int fileCounter = 0;
        int dirCounter = 0;
        if (content != null) {
            for (File object : content) {
                if (object.isFile()) {
                    list.add(createDirOutputStringLine(object.getName(), object.getPath(), false));
                    fileCounter++;
                } else {
                    list.add(createDirOutputStringLine(object.getName(), object.getPath(), true));
                    dirCounter++;
                }
            }
        }
        list.add(writeCounts(fileCounter, dirCounter) + System.lineSeparator() + System.lineSeparator());
    }

    private static String writeCounts(int fileCounter, int dirCounter) {
        StringBuilder sb = new StringBuilder();
        sb.append(fileCounter + " " + "File(s)");
        sb.append(System.getProperty("line.separator"));
        sb.append(dirCounter + " " + "Dir(s)");

        return sb.toString();
    }

    private static void writeToOutputFile(List<String> outputTxt, String fileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        for (String s : outputTxt) {
            writer.write(s);
        }
        writer.close();
    }

    private static String createDirOutputStringLine(String text, String path, boolean isDir) throws IOException {
        Path p = Paths.get(path);
        BasicFileAttributes view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
        StringBuilder sb = new StringBuilder(fileTimeToString(view.lastModifiedTime()));
        sb.append(" ");
        if (isDir) {
            sb.append("<DIR>");
        }
        sb.append(" ");
        sb.append(text);
        sb.append(System.getProperty("line.separator"));
        return sb.toString();
    }

    private static String fileTimeToString(FileTime fileTime) {
        String s = (String) parseToString(
                fileTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

        return s;
    }

    private static String parseToString(LocalDateTime localDateTime) {
        return localDateTime.format(DATE_FORMATTER_WITH_TIME);
    }

    private static void mkdirCommand(String directoryPath, String command) {
        String dirName = StringUtils.substringAfter(command, " ");
        File dir = new File(directoryPath + "/" + dirName);
        dir.mkdir();
    }

    private static void cdCommand(String command) {
        String dirName = StringUtils.substringAfter(command, " ");
        String path = dir.getPath();
        dir = new File(path + "/" + dirName);
        dir.exists();
    }

    private static void mkfileCommand(String command) throws IOException {
        String fileName = StringUtils.substringAfter(command, " ");
        String path = dir.getPath();
        File file = new File(path + "/" + fileName);
        file.createNewFile();
    }

    private static void rmdirCommand(String command) throws IOException {
        String currentPosition = dir.getPath();
        currentPosition = currentPosition.replace(".", "");
        String position = currentPosition;
        String p1 = StringUtils.substringBeforeLast(position, System.getProperty("file.separator"));
        File directory = new File(p1);
        if (directory.exists()) {
            File file = new File(p1);
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                System.out.println("Problem occurs when deleting the directory : " + p1);
                e.printStackTrace();
            }
        }
    }
}
