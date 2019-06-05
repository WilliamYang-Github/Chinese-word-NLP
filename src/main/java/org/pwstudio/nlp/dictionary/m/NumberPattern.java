package org.pwstudio.nlp.dictionary.m;

/**
 * 所有可能的数词形式
 * @author TylunasLi @ iyunwen.com
 *
 */
public enum NumberPattern
{
    /**
     * 整数小节，
     * 这一部分直接采用拼合的方式进行识别，以适应汉语的层次结构
     */
//  String[] numSections={"M",	/* 一 */
//          "LM",			/* 十五 */			"ML",			/* 二十 */
//          "MLM",			/* 二十五 */		"MLZM",			/* 一百零八 */
//          "MLML",			/* 二百二十 */		"MLMLM",		/* 二百二十一 */
//          "MLMLML",		/* 一千两百五十 */	"MLMLMLM",		/* 四千三百二十一 */
//          "ZM",			/*  */				"ZML"			/*  */
//	};

    /** 基本数词 */
    M(false, true, 1),      /* 一、七三一、整数小节 */
    L(false, true, 1),      /* 十/万 */
    ML(false, true, 1),     /* 三千万 */
    MLM(false, true, 1),    /* *万* */
    MLMLM(false, true, 1),  /* *亿*万* */
    A(false, true, 0),      /* 165 */
    AL(false, true, 1),     /* 4000万 */ 
    AB(false, true, 1),     /* 16K */

    /** 序数 */
    OM(true, true, 1),      /* 第一 第一一八 */
    OA(true, true, 1),      /* 第15 */
    OL(true, true, 1),      /* 第十 */
    OLK(true, false, 1),    /* 第十几 */
    OMH(true, false, 1),    /* 第五十来 */
    OMK(true, false, 1),    /* 第三十几 */
    OAH(true, false, 1),    /* 第30来 */
    OAK(true, false, 1),    /* 第30几 */

    /** 小数、分数 */
    MXM(false, true,-1),    /* 小数、分数：三分之一 */
    LXM(false, true,-1),    /* 百分之二 */
    MXMXM(false,true,-1),   /* 八分之六点五 */
    LXMXM(false,true,-1),   /* 百分之六点五 */
    LXLXM(false,true,-1),  /* 百分之十点五 */
    AXM(false,true, -1),    /* 4分之一 */
//  AXA(false,true, -1),    /* 这个一般是时间词！3点28 */
    ALXM(false,true, -1),   /* 4000万分之一 */
    ALXA(false,true, -1),   /* 4000万分之1 */ 

    /** 模糊数词 */
    LH(false, false, 1),    /* 十来（个） */
    LK(false, false, 1),    /* 十几 */
    MH(false, false, 1),    /* 五十来（个） */
    MK(false, false, 1),    /* 五十几（个） 三四十多（岁） */
    MHL(false, false, 1),   /* 五十余万 */
    MKL(false, false, 1),   /* 五十多万 */
    MLMH(false, false, 1),  /* 一万五千来（个） */
    MLMK(false, false, 1),  /* 一万五千多 */
    AH(false, false, 1),    /* 20来 */
    AK(false, false, 1),    /* 150多 */
    AKL(false, false, 1),   /* 150多万 */
    AHL(false, false, 1),   /* 150余万 */
    ABK(false, false, 1),   /* 5K多 */
    K(false, false, 0),     /* 几/多/半 */

    /** 模糊分数数词 */
    KXM(false, false, -1),  /* 几分之一 */
    KLXM(false, false, -1), /* 几千分之一 */
    MXK(false, false, -1),  /* 五十分之几 */
    LXK(false, false, -1),  /* 万分之几 */
    MXMK(false, false, -1), /* 五点六几 */
    ALXK(false, false, -1), /* 1000万分之几 */
    KXK(false, false, -1),  /* 几分之几 */

    /** 倍、百分数 */
    MU(false, true, 0),      /* 五倍 */
    MLU(false, true, 1),     /* 五千万倍 */    
    MUM(false, true,-1),     /* 三成五 */
    MXMU(false, true,-1),    /* 三点六倍 */
    LHU(false, false, 1),    /* 十余倍 */
    LKU(false, false, 1),    /* 十多倍 */
    MHU(false, false, 1),    /* 三十余倍 */
    MKU(false, false, 1),    /* 三十多倍 */
    MHLU(false, false, 1),   /* 三十余万倍 */
    MKLU(false, false, 1),   /* 三十多万倍 */
    MUK(false, false, 1),    /* 五倍多 */
    AU(false, true, 0),      /* 20倍 */
    ALU(false, true, 1),     /* 20万倍 */
    AKLU(false, false, 1),   /* 20多万倍 */
    AHU(false, false, 1),    /* 20余倍 */
    AKU(false, false, 1),    /* 20多倍 */
    AUK(false, false, 1),    /* 5倍多 */
    ;

    private NumberPattern(boolean ordinal, boolean exact, int integer) {
        this.ordinal = ordinal;
        this.exact = exact;
        this.integer = (byte)integer;
    }
    
    /**
     * 是否是序数词
     */
    private boolean ordinal;
    
    /**
     * 是否是精确数量
     */
    private boolean exact;
    
    /**
     * 是否为整数 0-未知 1-肯定 2-否定
     */
    private byte integer;

    public boolean isOrdinal() {
        return ordinal;
    }

    public boolean isExact() {
        return exact;
    }

    public byte isInteger() {
        return integer;
    }
    
}
