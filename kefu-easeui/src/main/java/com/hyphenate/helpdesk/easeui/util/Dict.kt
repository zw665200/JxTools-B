package com.hyphenate.helpdesk.easeui.util

object Dict {

    @ExperimentalStdlibApi
    fun shieldSensitiveWords(word: String): Boolean {
        val words = word.replace(" ", "").replace("~", "").replace("!", "")
            .replace("#", "").replace("$", "").replace("%", "")
            .replace("^", "").replace("&", "").replace("*", "")
            .replace(".", "").replace("。", "").replace(",", "")
            .replace("，", "").replace("/", "").replace("<", "")
            .replace(">", "").replace(":", "").replace(";", "")
            .replace("：", "").replace("；", "")

        return words.contains("党") || words.contains("妈") || words.contains("逼") || words.contains("日")
                || words.contains("棺材") || words.contains("骗") || words.contains("煞笔") || words.contains("死")
                || words.contains("骚") || words.contains("台湾") || words.contains("香港") || words.contains("屁")
                || words.contains("屁") || words.contains("屌") || words.contains("反动") || words.contains("独立")
                || words.contains("杀") || words.contains("家") || words.contains("罪") || words.contains("派出所")
                || words.contains("国家") || words.contains("政府") || words.lowercase().contains("Pi") || words.contains("神经病")
                || words.contains("大便") || words.lowercase().contains("bi") || words.contains("屎") || words.contains("尿")
                || words.contains("黄") || words.contains("赌") || words.contains("毒") || words.contains("卖")
                || words.lowercase().contains("vpn") || words.contains("妹") || words.contains("哔")
                || words.contains("罪") || words.contains("罪") || words.contains("操") || words.contains("草")
                || words.contains("狗") || words.contains("嫖") || words.lowercase().contains("tmd") || words.contains("习近平")
                || words.contains("病") || words.contains("闭嘴") || words.contains("草") || words.contains("艹")
                || words.contains("叼") || words.contains("敏感") || words.contains("壮阳") || words.contains("补肾")
                || words.contains("全家") || words.contains("癌") || words.contains("刀") || words.contains("枪")
                || words.contains("伊斯兰") || words.contains("佛") || words.contains("色") || words.contains("飞机")
                || words.contains("杀") || words.contains("砍") || words.contains("国务院") || words.contains("革命")
                || words.contains("中纪委") || words.contains("翔") || words.contains("敏感") || words.contains("领导")
                || words.contains("机关") || words.contains("人民币") || words.contains("胸") || words.contains("女")
                || words.contains("爷") || words.contains("姐") || words.contains("爸") || words.lowercase().contains("bing")
                || words.contains("智障") || words.contains("新疆") || words.contains("旗") || words.contains("祖宗")
                || words.contains("十八代") || words.contains("cao") || words.contains("港独") || words.contains("台独")
                || words.contains("藏独") || words.contains("寂寞") || words.lowercase().contains("sb") || words.contains("B")
                || words.contains("脑残") || words.contains("脑瘫") || words.contains("粑粑") || words.lowercase().contains("shabi")
                || words.contains("穷疯了") || words.lowercase().contains("guancai") || words.lowercase().contains("pianqian")
                || words.contains("畜生") || words.contains("畜牲") || words.contains("坑")
                || words.contains("子孙") || words.contains("报应") || words.lowercase().contains("baoying")
                || words.lowercase().contains("taiwan") || words.lowercase().contains("xianggang") || words.lowercase().contains("gangdu")
                || words.contains("j把") || words.lowercase().contains("j8") || words.contains("鸡") || words.contains("尼玛")
    }
}