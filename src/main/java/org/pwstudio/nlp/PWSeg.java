package org.pwstudio.nlp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Scanner;

import org.pwstudio.nlp.dictionary.py.Pinyin;
import org.pwstudio.nlp.seg.common.Term;
import org.pwstudio.nlp.tagging.PinyinTagger;

/**
 * 分词与拼音标注的主要接口
 * 也是命令行的处理逻辑。
 * @author TylunasLi
 *
 */
public class PWSeg {
    
    private static final String encoding = "UTF-8";

    public static void main(String[] args) {
        switch (args.length) {
            case 0: {
                playGround();
            }
            break;
            case 1: {
                File file = new File(args[0]);
                if (file.exists() && file.isFile()) {
                    segFile(file, null);
                } else {
                    System.out.println(segWithPinyin(args[0]));
                }
            }
            break;
            default: {
                File file = new File(args[0]);
                File outFile = new File(args[1]);
                if (file.exists() && file.isFile()) {
                    segFile(file, outFile);
                }
            }
        }
    }

    /**
     * 对文本文件进行分词处理
     * @param file
     * @param outFile
     */
    public static void segFile(File file, File outFile) {
        File newOutFile = outFile;
        if (newOutFile == null)
            newOutFile = new File(file.getAbsolutePath() + ".out");
        if (newOutFile.isDirectory())
            newOutFile = new File(outFile.getPath() + File.separator + file.getName() + ".out");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file),  encoding));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(newOutFile), encoding));
            String currentline = null;
            while ((currentline = reader.readLine()) != null) {
                //解析词典
                String result = seg(currentline);
                if (result != null) {
                    writer.write(result);
                    writer.write("\r\n");
                }
            }
            reader.close();
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * 分词并标注拼音
     * @param line
     * @return
     */
    public static String segWithPinyin(String line) {
        List<Term> terms = PwNLP.segment(line);
        StringBuilder builder = new StringBuilder(line.length()*6 + terms.size()*2);
        List<List<Pinyin>> termPinyinList = PinyinTagger.tagPinyin(terms);
        for (int idx = 0; idx<terms.size() && idx<termPinyinList.size(); idx++) {
            builder.append(terms.get(idx).word).append("\t");
            builder.append(terms.get(idx).nature.name()).append("\t");
            List<Pinyin> pinyinList = termPinyinList.get(idx);
            for (Pinyin pinyin : pinyinList) {
                builder.append(pinyin.name());
            }
            builder.append("\r\n");
        }
        return builder.toString();
    }

    public static String seg(String line) {
        List<Term> terms = PwNLP.segment(line);
        StringBuilder builder = new StringBuilder(line.length()*6 + terms.size()*2);
        for (Term term : terms) {
            builder.append(term.word).append("\r\n");
        }
        return builder.toString();
    }

    /**
     *  交互式的单句分析接口
     */
    private static void playGround() {
        Scanner scanner = new Scanner(System.in);
        String line;
        while ((line = scanner.nextLine()).length() > 0) {
            System.out.println(seg(line));
        }
        scanner.close();
    }

}
