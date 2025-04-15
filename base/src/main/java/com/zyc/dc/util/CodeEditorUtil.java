package com.zyc.dc.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import com.zyc.dc.dao.ProjectAccessModel;
import com.zyc.dc.dao.ProjectInfoModel;

public class CodeEditorUtil {

	//读取文件目录树
    public static Map<String, Object> readDirectoryStructure(ProjectInfoModel projectInfoModel, ProjectAccessModel projectAccessModel) throws IOException {
        File root = new File(projectInfoModel.getPath());
        String basePathFormatted = root.getPath().replace(File.separator, "/");
        Map<String, Object> result = readDirectory(root, basePathFormatted, projectAccessModel.getFilters());
        return result;
    }

    private static Map<String, Object> readDirectory(File dir, String basePath, Set<String> pathFilter) throws IOException {
        Map<String, Object> directoryData = new LinkedHashMap<>();
        File[] files = dir.listFiles();

        if (files != null) {
            // 按目录优先、按字母顺序排序
            Arrays.sort(files, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) {
                    return -1;
                }
                if (!f1.isDirectory() && f2.isDirectory()) {
                    return 1;
                }
                return f1.getName().compareToIgnoreCase(f2.getName());
            });

            for (File file : files) {
                // 获取文件或目录的相对路径
                String relativePath = getRelativePath(file, basePath);

                // 判断相对路径是否在过滤器中
                if (pathFilter != null && pathFilter.contains(relativePath)) {
                    continue; // 跳过被过滤的文件或目录
                }

                if (file.isDirectory()) {
                    // 递归读取子目录
                    directoryData.put(file.getName(), createFolderData(file, basePath, pathFilter));
                } else if (file.isFile()) {
                    // 读取文件
                    directoryData.put(file.getName(), createFileData(file));
                }
            }
        }
        return directoryData;
    }

    private static String getRelativePath(File file, String basePath) {
        // 获取文件的绝对路径并将路径分隔符统一为 /
        String absolutePath = file.getAbsolutePath().replace(File.separator, "/");
        // 获取相对于根目录的路径
        return absolutePath.replace(basePath, "");
    }

    private static Map<String, Object> createFolderData(File folder, String basePath, Set<String> pathFilter) throws IOException {
        Map<String, Object> folderData = new HashMap<>();
        folderData.put("type", "folder");
        // 读取文件夹内容
        folderData.put("children", readDirectory(folder, basePath, pathFilter));
        return folderData;
    }

    private static Map<String, Object> createFileData(File file) throws IOException {
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("type", "file");
        return fileData;
    }
}

