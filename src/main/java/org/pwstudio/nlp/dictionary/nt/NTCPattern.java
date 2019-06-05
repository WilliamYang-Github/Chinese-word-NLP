package org.pwstudio.nlp.dictionary.nt;

/**
 * 企业字号识别的合法标记序列
 * @author TylunasLi
 *
 */
public enum NTCPattern {

    /**
     * 两字的企业字号
     */
    FE,
    S,
    
    /**
     * 三字的企业字号
     */
    FME,
    CE,
    FD,
    
    /**
     * 四字的企业字号
     */
    FMME,
    FIE,
    FMD,
    CME,

}
