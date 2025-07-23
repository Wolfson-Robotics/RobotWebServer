package org.wolfsonrobotics.RobotWebServer.robot;

import java.io.*;

// Limits the scope of the explorer to the given file path on construction
public class FileExplorer {

    private final String filePath;

    public FileExplorer(String filePath) throws IOException {
        this.filePath = new File(filePath).getCanonicalFile().getPath() + File.separator;
    }

    private String toAbsPath(String givenPath) throws IOException, IllegalAccessException {
        File absPath = new File(filePath, givenPath).getCanonicalFile();
        if (!(absPath.getPath() + File.separator).startsWith(filePath)) {
            throw new IllegalAccessException("Access denied: The given path " + givenPath + " is outside of the base directory.");
        }
        return filePath + "/" + givenPath;
    }
    private File toAbsFile(String filePath) throws IOException, IllegalAccessException {
        return new File(toAbsPath(filePath));
    }

    public boolean isFile(String subPath) throws IOException, IllegalAccessException {
        return toAbsFile(subPath).isFile() || subPath.contains(".");
    }
    public boolean isDirectory(String subPath) throws IOException, IllegalAccessException {
        return toAbsFile(subPath).isDirectory() || !subPath.contains(".");
    }

    public File[] dirListing(String subPath) throws IOException, IllegalAccessException {
        File subFile = toAbsFile(subPath);
        if (!subFile.exists()) throw new IOException("The given directory does not exist.");
        if (subFile.isFile()) throw new IOException("The given path is a file.");
        return toAbsFile(subPath).listFiles();
    }
    public File[] dirListing() throws IOException, IllegalAccessException {
        return dirListing("");
    }


    public boolean fileExists(String filePath) throws IOException, IllegalAccessException {
        return toAbsFile(filePath).exists();
    }

    // Tailored to .txt files
    public String getFile(String filePath) throws IOException, IllegalAccessException {
        StringBuilder content = new StringBuilder();

        BufferedReader reader = new BufferedReader(new FileReader(toAbsPath(filePath)));
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append(System.lineSeparator());
        }
        reader.close();
        return content.toString();
    }

    public void writeFile(String filePath, String content, boolean append) throws IOException, IllegalAccessException {
        FileWriter writer = new FileWriter(toAbsPath(filePath), append);
        writer.append(content);
        writer.close();
    }
    public void writeFile(String filePath, String content) throws IOException, IllegalAccessException {
        writeFile(filePath, content, true);
    }


    public void createFile(String filePath, String content) throws IOException, IllegalAccessException {
        File file = toAbsFile(filePath);
        if (file.exists()) {
            throw new IOException("File " + filePath + " already exists.");
        }
        if (!file.getParentFile().exists()) {
            throw new IOException("The directory to create the file in does not exist.");
        }
        if (!file.createNewFile()) {
            throw new IOException("Failed to create new file " + filePath);
        }
        writeFile(filePath, content, false);
    }
    public void createFile(String filePath) throws IOException, IllegalAccessException {
        createFile(filePath, "");
    }

    public void renameFile(String filePath, String newFilePath) throws IOException, IllegalAccessException {
        toAbsFile(filePath).renameTo(toAbsFile(newFilePath));
//        Files.move(Paths.get(toAbsPath(filePath)), Paths.get(toAbsPath(newFilePath)));
    }

    public void copyFile(String filePath, String newFilePath) throws IOException, IllegalAccessException {
        if (fileExists(newFilePath)) {
            throw new IOException("The provided path " + newFilePath + " to copy " + filePath + " to already exists.");
        }
//        Files.copy(Paths.get(toAbsPath(filePath)), Paths.get(toAbsPath(newFilePath)), StandardCopyOption.COPY_ATTRIBUTES);
        createFile(newFilePath, getFile(filePath));
    }

    public void deleteFile(String filePath) throws IOException, IllegalAccessException {
        if (!toAbsFile(filePath).delete()) throw new IOException("The provided path " + filePath + " could not be deleted.");
    }



    public void createDir(String dirPath) throws IOException, IllegalAccessException {
        if (fileExists(dirPath)) throw new IOException("The provided directory " + dirPath + " to create already exists.");
        if (!toAbsFile(dirPath).mkdirs()) throw new IOException("The provided directory " + dirPath + " could not be created.");
    }

    public void renameDir(String dirPath, String newDirPath) throws IOException, IllegalAccessException {
        copyDir(dirPath, newDirPath);
        deleteDir(dirPath);
    }

    public void copyDir(String dirPath, String newDirPath) throws IOException, IllegalAccessException {
        createDir(newDirPath);
        for (File file : dirListing(dirPath)) {
            String oldPath = dirPath + File.separator + file.getName();
            String newPath = newDirPath + File.separator + file.getName();
            if (file.isDirectory() &&
                    !new File(newPath).getCanonicalPath()
                            .startsWith(new File(dirPath).getCanonicalPath())) {
                copyDir(oldPath, newPath);
            } else {
                copyFile(oldPath, newPath);
            }
        }
    }

    public void deleteDir(String dirPath) throws IOException, IllegalAccessException {
        if (!fileExists(dirPath)) return;
        for (File file : dirListing(dirPath)) {
            String path = dirPath + File.separator + file.getName();
            if (file.isDirectory()) {
                deleteDir(path);
            } else {
                deleteFile(path);
            }
        }
        if (!toAbsFile(dirPath).delete()) throw new IOException("The provided directory " + dirPath + " could not be fully deleted.");
    }

}
