/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/8 17:14</create-date>
 *
 * <copyright file="FolderWalker.java">
 * This source is subject to the Apache License 2.0. see http://www.apache.org/licenses/LICENSE-2.0
 * </copyright>
 */
package org.pwstudio.nlp.corpus.io;


import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static org.pwstudio.nlp.utility.Predefine.logger;
/**
 * 遍历目录工具类
 * @author hankcs
 */
public class FolderWalker
{
    /**
     * 打开一个目录，获取全部的文件名
     * @param path
     * @return
     */
    public static List<File> open(String path)
    {
        List<File> fileList = new LinkedList<File>();
        File folder = new File(path);
        handleFolder(folder, fileList);
        return fileList;
    }

    private static void handleFolder(File folder, List<File> fileList)
    {
        File[] fileArray = folder.listFiles();
        if (fileArray != null)
        {
            for (File file : fileArray)
            {
                if (file.isFile() && !file.getName().startsWith(".")) // 过滤隐藏文件
                {
                    fileList.add(file);
                }
                else
                {
                    handleFolder(file, fileList);
                }
            }
        }
    }

//    public static void main(String[] args)
//    {
//        List<File> fileList = FolderWalker.open("D:\\Doc\\语料库\\2014");
//        for (File file : fileList)
//        {
//            System.out.println(file);
//        }
//    }

}
