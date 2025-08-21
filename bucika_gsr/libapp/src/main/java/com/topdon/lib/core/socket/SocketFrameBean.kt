package com.topdon.lib.core.socket

/**
 * TC007 Socket 一帧数据.
 * @param isMaxShow 最高温点是否显示
 * @param isMinShow 最低温点是否显示
 * @param isCenterShow 中心点是否显示
 * @param maxX 最高温点 X 轴坐标
 * @param maxY 最高温点 Y 轴坐标
 * @param maxValue 最高温点温度值，单位摄氏度*10
 * @param minX 最低温点 X 轴坐标
 * @param minY 最低温点 Y 轴坐标
 * @param minValue 最低温点温度值，单位摄氏度*10
 * @param centerX 中心点 X 轴坐标
 * @param centerY 中心点 Y 轴坐标
 * @param centerValue 中心点温度值，单位摄氏度*10
 * @param isMaxWarn 整帧高温点报警是否开启
 * @param isMinWarn 整帧低温点报警是否开启
 * @param isCenterWarn 整帧中心点报警是否开启
 *
 * @param isP1Show 点1是否显示
 * @param p1X 点1 X 轴坐标
 * @param p1Y 点1 Y 轴坐标
 * @param p1Value 点1温度值，单位摄氏度*10
 * @param isP1MaxWarn 点1高温点报警是否开启
 * @param isP1MinWarn 点1低温点报警是否开启
 * @param isP1CenterWarn 点1中心点报警是否开启
 *
 * @param isL1Show 线1是否显示
 * @param l1StartX 线1起始点 X 轴坐标
 * @param l1StartY 线1起始点 Y 轴坐标
 * @param l1EndX 线1终止点 X 轴坐标
 * @param l1EndY 线1终止点 Y 轴坐标
 * @param l1MaxX 线1最高温点 X 轴坐标
 * @param l1MaxY 线1最高温点 Y 轴坐标
 * @param l1MaxValue 线1最高温点温度值，单位摄氏度*10
 * @param l1MinX 线1最低温点 X 轴坐标
 * @param l1MinY 线1最低温点 Y 轴坐标
 * @param l1MinValue 线1最低温点温度值，单位摄氏度*10
 * @param l1AveValue 线1平均温，单位摄氏度*10
 * @param isL1MaxWarn 线1高温点报警是否开启
 * @param isL1MinWarn 线1低温点报警是否开启
 * @param isL1CenterWarn 线1中心点报警是否开启
 */
data class SocketFrameBean(
    //整帧测温结果
    val isMaxShow: Boolean,
    val isMinShow: Boolean,
    val isCenterShow: Boolean,
    val maxX: Int,
    val maxY: Int,
    val maxValue: Int,
    val minX: Int,
    val minY: Int,
    val minValue: Int,
    val centerX: Int,
    val centerY: Int,
    val centerValue: Int,
    val isMaxWarn: Boolean,
    val isMinWarn: Boolean,
    val isCenterWarn: Boolean,

    //点测温结果
    val isP1Show: Boolean,
    val p1X: Int,
    val p1Y: Int,
    val p1Value: Int,
    val isP1MaxWarn: Boolean,
    val isP1MinWarn: Boolean,
    val isP1CenterWarn: Boolean,
    val isP2Show: Boolean,
    val p2X: Int,
    val p2Y: Int,
    val p2Value: Int,
    val isP2MaxWarn: Boolean,
    val isP2MinWarn: Boolean,
    val isP2CenterWarn: Boolean,
    val isP3Show: Boolean,
    val p3X: Int,
    val p3Y: Int,
    val p3Value: Int,
    val isP3MaxWarn: Boolean,
    val isP3MinWarn: Boolean,
    val isP3CenterWarn: Boolean,

    //线测温结果
    val isL1Show: Boolean,
    val l1StartX: Int,
    val l1StartY: Int,
    val l1EndX: Int,
    val l1EndY: Int,
    val l1MaxX: Int,
    val l1MaxY: Int,
    val l1MaxValue: Int,
    val l1MinX: Int,
    val l1MinY: Int,
    val l1MinValue: Int,
    val l1AveValue: Int,
    val isL1MaxWarn: Boolean,
    val isL1MinWarn: Boolean,
    val isL1CenterWarn: Boolean,
    val isL2Show: Boolean,
    val l2StartX: Int,
    val l2StartY: Int,
    val l2EndX: Int,
    val l2EndY: Int,
    val l2MaxX: Int,
    val l2MaxY: Int,
    val l2MaxValue: Int,
    val l2MinX: Int,
    val l2MinY: Int,
    val l2MinValue: Int,
    val l2AveValue: Int,
    val isL2MaxWarn: Boolean,
    val isL2MinWarn: Boolean,
    val isL2CenterWarn: Boolean,
    val isL3Show: Boolean,
    val l3StartX: Int,
    val l3StartY: Int,
    val l3EndX: Int,
    val l3EndY: Int,
    val l3MaxX: Int,
    val l3MaxY: Int,
    val l3MaxValue: Int,
    val l3MinX: Int,
    val l3MinY: Int,
    val l3MinValue: Int,
    val l3AveValue: Int,
    val isL3MaxWarn: Boolean,
    val isL3MinWarn: Boolean,
    val isL3CenterWarn: Boolean,

    //区域测温结果
    val isR1Show: Boolean,
    val r1StartX: Int,
    val r1StartY: Int,
    val r1EndX: Int,
    val r1EndY: Int,
    val r1MaxX: Int,
    val r1MaxY: Int,
    val r1MaxValue: Int,
    val r1MinX: Int,
    val r1MinY: Int,
    val r1MinValue: Int,
    val r1AveValue: Int,
    val isR1MaxWarn: Boolean,
    val isR1MinWarn: Boolean,
    val isR1CenterWarn: Boolean,
    val isR2Show: Boolean,
    val r2StartX: Int,
    val r2StartY: Int,
    val r2EndX: Int,
    val r2EndY: Int,
    val r2MaxX: Int,
    val r2MaxY: Int,
    val r2MaxValue: Int,
    val r2MinX: Int,
    val r2MinY: Int,
    val r2MinValue: Int,
    val r2AveValue: Int,
    val isR2MaxWarn: Boolean,
    val isR2MinWarn: Boolean,
    val isR2CenterWarn: Boolean,
    val isR3Show: Boolean,
    val r3StartX: Int,
    val r3StartY: Int,
    val r3EndX: Int,
    val r3EndY: Int,
    val r3MaxX: Int,
    val r3MaxY: Int,
    val r3MaxValue: Int,
    val r3MinX: Int,
    val r3MinY: Int,
    val r3MinValue: Int,
    val r3AveValue: Int,
    val isR3MaxWarn: Boolean,
    val isR3MinWarn: Boolean,
    val isR3CenterWarn: Boolean,
) {

    constructor(byteArray: ByteArray) : this(
        isMaxShow = byteArray[0].toInt() and 0xff == 1,
        isMinShow = byteArray[1].toInt() and 0xff == 1,
        isCenterShow = byteArray[2].toInt() and 0xff == 1,
        maxX = (byteArray[4].toInt() and 0xff) or (byteArray[5].toInt() and 0xff shl 8),
        maxY = (byteArray[6].toInt() and 0xff) or (byteArray[7].toInt() and 0xff shl 8),
        maxValue = ((byteArray[8].toInt() and 0xff) or (byteArray[9].toInt() and 0xff shl 8)) - 2732,
        minX = (byteArray[10].toInt() and 0xff) or (byteArray[11].toInt() and 0xff shl 8),
        minY = (byteArray[12].toInt() and 0xff) or (byteArray[13].toInt() and 0xff shl 8),
        minValue = ((byteArray[14].toInt() and 0xff) or (byteArray[15].toInt() and 0xff shl 8)) - 2732,
        centerX = (byteArray[16].toInt() and 0xff) or (byteArray[17].toInt() and 0xff shl 8),
        centerY = (byteArray[18].toInt() and 0xff) or (byteArray[19].toInt() and 0xff shl 8),
        centerValue = ((byteArray[20].toInt() and 0xff) or (byteArray[21].toInt() and 0xff shl 8)) - 2732,
        isMaxWarn = byteArray[22].toInt() and 0xff == 1,
        isMinWarn = byteArray[23].toInt() and 0xff == 1,
        isCenterWarn = byteArray[24].toInt() and 0xff == 1,

        isP1Show = byteArray[26].toInt() and 0xff == 1,
        p1X = (byteArray[28].toInt() and 0xff) or (byteArray[29].toInt() and 0xff shl 8),
        p1Y = (byteArray[30].toInt() and 0xff) or (byteArray[31].toInt() and 0xff shl 8),
        p1Value = ((byteArray[32].toInt() and 0xff) or (byteArray[33].toInt() and 0xff shl 8)) - 2732,
        isP1MaxWarn = byteArray[34].toInt() and 0xff == 1,
        isP1MinWarn = byteArray[35].toInt() and 0xff == 1,
        isP1CenterWarn = byteArray[36].toInt() and 0xff == 1,
        isP2Show = byteArray[38].toInt() and 0xff == 1,
        p2X = (byteArray[40].toInt() and 0xff) or (byteArray[41].toInt() and 0xff shl 8),
        p2Y = (byteArray[42].toInt() and 0xff) or (byteArray[43].toInt() and 0xff shl 8),
        p2Value = ((byteArray[44].toInt() and 0xff) or (byteArray[45].toInt() and 0xff shl 8)) - 2732,
        isP2MaxWarn = byteArray[46].toInt() and 0xff == 1,
        isP2MinWarn = byteArray[47].toInt() and 0xff == 1,
        isP2CenterWarn = byteArray[48].toInt() and 0xff == 1,
        isP3Show = byteArray[50].toInt() and 0xff == 1,
        p3X = (byteArray[52].toInt() and 0xff) or (byteArray[53].toInt() and 0xff shl 8),
        p3Y = (byteArray[54].toInt() and 0xff) or (byteArray[55].toInt() and 0xff shl 8),
        p3Value = ((byteArray[56].toInt() and 0xff) or (byteArray[57].toInt() and 0xff shl 8)) - 2732,
        isP3MaxWarn = byteArray[58].toInt() and 0xff == 1,
        isP3MinWarn = byteArray[59].toInt() and 0xff == 1,
        isP3CenterWarn = byteArray[60].toInt() and 0xff == 1,

        isL1Show = byteArray[62].toInt() and 0xff == 1,
        l1StartX = (byteArray[64].toInt() and 0xff) or (byteArray[65].toInt() and 0xff shl 8),
        l1StartY = (byteArray[66].toInt() and 0xff) or (byteArray[67].toInt() and 0xff shl 8),
        l1EndX = (byteArray[68].toInt() and 0xff) or (byteArray[69].toInt() and 0xff shl 8),
        l1EndY = (byteArray[70].toInt() and 0xff) or (byteArray[71].toInt() and 0xff shl 8),
        l1MaxX = (byteArray[72].toInt() and 0xff) or (byteArray[73].toInt() and 0xff shl 8),
        l1MaxY = (byteArray[74].toInt() and 0xff) or (byteArray[75].toInt() and 0xff shl 8),
        l1MaxValue = ((byteArray[76].toInt() and 0xff) or (byteArray[77].toInt() and 0xff shl 8)) - 2732,
        l1MinX = (byteArray[78].toInt() and 0xff) or (byteArray[79].toInt() and 0xff shl 8),
        l1MinY = (byteArray[80].toInt() and 0xff) or (byteArray[81].toInt() and 0xff shl 8),
        l1MinValue = ((byteArray[82].toInt() and 0xff) or (byteArray[83].toInt() and 0xff shl 8)) - 2732,
        l1AveValue = ((byteArray[88].toInt() and 0xff) or (byteArray[89].toInt() and 0xff shl 8)) - 2732,
        isL1MaxWarn= byteArray[90].toInt() and 0xff == 1,
        isL1MinWarn= byteArray[91].toInt() and 0xff == 1,
        isL1CenterWarn= byteArray[92].toInt() and 0xff == 1,
        isL2Show = byteArray[94].toInt() and 0xff == 1,
        l2StartX = (byteArray[96].toInt() and 0xff) or (byteArray[97].toInt() and 0xff shl 8),
        l2StartY = (byteArray[98].toInt() and 0xff) or (byteArray[99].toInt() and 0xff shl 8),
        l2EndX = (byteArray[100].toInt() and 0xff) or (byteArray[101].toInt() and 0xff shl 8),
        l2EndY = (byteArray[102].toInt() and 0xff) or (byteArray[103].toInt() and 0xff shl 8),
        l2MaxX = (byteArray[104].toInt() and 0xff) or (byteArray[105].toInt() and 0xff shl 8),
        l2MaxY = (byteArray[106].toInt() and 0xff) or (byteArray[107].toInt() and 0xff shl 8),
        l2MaxValue = ((byteArray[108].toInt() and 0xff) or (byteArray[109].toInt() and 0xff shl 8)) - 2732,
        l2MinX = (byteArray[110].toInt() and 0xff) or (byteArray[111].toInt() and 0xff shl 8),
        l2MinY = (byteArray[112].toInt() and 0xff) or (byteArray[113].toInt() and 0xff shl 8),
        l2MinValue = ((byteArray[114].toInt() and 0xff) or (byteArray[115].toInt() and 0xff shl 8)) - 2732,
        l2AveValue = ((byteArray[120].toInt() and 0xff) or (byteArray[121].toInt() and 0xff shl 8)) - 2732,
        isL2MaxWarn= byteArray[122].toInt() and 0xff == 1,
        isL2MinWarn= byteArray[123].toInt() and 0xff == 1,
        isL2CenterWarn= byteArray[124].toInt() and 0xff == 1,
        isL3Show = byteArray[126].toInt() and 0xff == 1,
        l3StartX = (byteArray[128].toInt() and 0xff) or (byteArray[129].toInt() and 0xff shl 8),
        l3StartY = (byteArray[130].toInt() and 0xff) or (byteArray[131].toInt() and 0xff shl 8),
        l3EndX = (byteArray[132].toInt() and 0xff) or (byteArray[133].toInt() and 0xff shl 8),
        l3EndY = (byteArray[134].toInt() and 0xff) or (byteArray[135].toInt() and 0xff shl 8),
        l3MaxX = (byteArray[136].toInt() and 0xff) or (byteArray[137].toInt() and 0xff shl 8),
        l3MaxY = (byteArray[138].toInt() and 0xff) or (byteArray[139].toInt() and 0xff shl 8),
        l3MaxValue = ((byteArray[140].toInt() and 0xff) or (byteArray[141].toInt() and 0xff shl 8)) - 2732,
        l3MinX = (byteArray[142].toInt() and 0xff) or (byteArray[143].toInt() and 0xff shl 8),
        l3MinY = (byteArray[144].toInt() and 0xff) or (byteArray[145].toInt() and 0xff shl 8),
        l3MinValue = ((byteArray[146].toInt() and 0xff) or (byteArray[147].toInt() and 0xff shl 8)) - 2732,
        l3AveValue = ((byteArray[152].toInt() and 0xff) or (byteArray[153].toInt() and 0xff shl 8)) - 2732,
        isL3MaxWarn= byteArray[154].toInt() and 0xff == 1,
        isL3MinWarn= byteArray[155].toInt() and 0xff == 1,
        isL3CenterWarn= byteArray[156].toInt() and 0xff == 1,

        isR1Show = byteArray[158].toInt() and 0xff == 1,
        r1StartX = (byteArray[160].toInt() and 0xff) or (byteArray[161].toInt() and 0xff shl 8),
        r1StartY = (byteArray[162].toInt() and 0xff) or (byteArray[163].toInt() and 0xff shl 8),
        r1EndX = (byteArray[164].toInt() and 0xff) or (byteArray[165].toInt() and 0xff shl 8),
        r1EndY = (byteArray[166].toInt() and 0xff) or (byteArray[167].toInt() and 0xff shl 8),
        r1MaxX = (byteArray[168].toInt() and 0xff) or (byteArray[169].toInt() and 0xff shl 8),
        r1MaxY = (byteArray[170].toInt() and 0xff) or (byteArray[171].toInt() and 0xff shl 8),
        r1MaxValue = ((byteArray[172].toInt() and 0xff) or (byteArray[173].toInt() and 0xff shl 8)) - 2732,
        r1MinX = (byteArray[174].toInt() and 0xff) or (byteArray[175].toInt() and 0xff shl 8),
        r1MinY = (byteArray[176].toInt() and 0xff) or (byteArray[177].toInt() and 0xff shl 8),
        r1MinValue = ((byteArray[178].toInt() and 0xff) or (byteArray[179].toInt() and 0xff shl 8)) - 2732,
        r1AveValue = ((byteArray[184].toInt() and 0xff) or (byteArray[185].toInt() and 0xff shl 8)) - 2732,
        isR1MaxWarn= byteArray[186].toInt() and 0xff == 1,
        isR1MinWarn= byteArray[187].toInt() and 0xff == 1,
        isR1CenterWarn= byteArray[188].toInt() and 0xff == 1,
        isR2Show = byteArray[190].toInt() and 0xff == 1,
        r2StartX = (byteArray[192].toInt() and 0xff) or (byteArray[193].toInt() and 0xff shl 8),
        r2StartY = (byteArray[194].toInt() and 0xff) or (byteArray[195].toInt() and 0xff shl 8),
        r2EndX = (byteArray[196].toInt() and 0xff) or (byteArray[197].toInt() and 0xff shl 8),
        r2EndY = (byteArray[198].toInt() and 0xff) or (byteArray[199].toInt() and 0xff shl 8),
        r2MaxX = (byteArray[200].toInt() and 0xff) or (byteArray[201].toInt() and 0xff shl 8),
        r2MaxY = (byteArray[202].toInt() and 0xff) or (byteArray[203].toInt() and 0xff shl 8),
        r2MaxValue = ((byteArray[204].toInt() and 0xff) or (byteArray[205].toInt() and 0xff shl 8)) - 2732,
        r2MinX = (byteArray[206].toInt() and 0xff) or (byteArray[207].toInt() and 0xff shl 8),
        r2MinY = (byteArray[208].toInt() and 0xff) or (byteArray[209].toInt() and 0xff shl 8),
        r2MinValue = ((byteArray[210].toInt() and 0xff) or (byteArray[211].toInt() and 0xff shl 8)) - 2732,
        r2AveValue = ((byteArray[216].toInt() and 0xff) or (byteArray[217].toInt() and 0xff shl 8)) - 2732,
        isR2MaxWarn= byteArray[218].toInt() and 0xff == 1,
        isR2MinWarn= byteArray[219].toInt() and 0xff == 1,
        isR2CenterWarn= byteArray[220].toInt() and 0xff == 1,
        isR3Show = byteArray[222].toInt() and 0xff == 1,
        r3StartX = (byteArray[224].toInt() and 0xff) or (byteArray[225].toInt() and 0xff shl 8),
        r3StartY = (byteArray[226].toInt() and 0xff) or (byteArray[227].toInt() and 0xff shl 8),
        r3EndX = (byteArray[228].toInt() and 0xff) or (byteArray[229].toInt() and 0xff shl 8),
        r3EndY = (byteArray[230].toInt() and 0xff) or (byteArray[231].toInt() and 0xff shl 8),
        r3MaxX = (byteArray[232].toInt() and 0xff) or (byteArray[233].toInt() and 0xff shl 8),
        r3MaxY = (byteArray[234].toInt() and 0xff) or (byteArray[235].toInt() and 0xff shl 8),
        r3MaxValue = ((byteArray[236].toInt() and 0xff) or (byteArray[237].toInt() and 0xff shl 8)) - 2732,
        r3MinX = (byteArray[238].toInt() and 0xff) or (byteArray[239].toInt() and 0xff shl 8),
        r3MinY = (byteArray[240].toInt() and 0xff) or (byteArray[241].toInt() and 0xff shl 8),
        r3MinValue = ((byteArray[242].toInt() and 0xff) or (byteArray[243].toInt() and 0xff shl 8)) - 2732,
        r3AveValue = ((byteArray[248].toInt() and 0xff) or (byteArray[249].toInt() and 0xff shl 8)) - 2732,
        isR3MaxWarn= byteArray[250].toInt() and 0xff == 1,
        isR3MinWarn= byteArray[251].toInt() and 0xff == 1,
        isR3CenterWarn= byteArray[252].toInt() and 0xff == 1,
    )

    companion object {
        private fun Boolean.openText(): String = if (this) "开启" else "关闭"

        private fun Int.toCStr(): String = "${this / 10}${if (this % 10 == 0) "" else ".${this % 10}"}°C"
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        if (isMaxShow) {
            stringBuilder.append("高温点 ($maxX, $maxY) 温度${maxValue.toCStr()} 报警${isMaxWarn.openText()}\n")
        }
        if (isMinShow) {
            stringBuilder.append("低温点 ($minX, $minY) 温度${minValue.toCStr()} 报警${isMinWarn.openText()}\n")
        }
        if (isCenterShow) {
            stringBuilder.append("中心点 ($centerX, $centerY) 温度${centerValue.toCStr()} 报警${isCenterWarn.openText()}\n")
        }

        if (isP1Show) {
            stringBuilder.append("点1 ($p1X, $p1Y) 温度${p1Value.toCStr()}\n")
        }
        if (isP2Show) {
            stringBuilder.append("点2 ($p2X, $p2Y) 温度${p2Value.toCStr()}\n")
        }
        if (isP3Show) {
            stringBuilder.append("点3 ($p3X, $p3Y) 温度${p3Value.toCStr()}\n")
        }

        if (isL1Show) {
            stringBuilder.append("线1 ($l1StartX, $l1StartY)-($l1EndX, $l1EndY) ")
            stringBuilder.append("最低温${l1MinValue.toCStr()}($l1MinX, $l1MinY) 最高温${l1MaxValue.toCStr()}($l1MaxX, $l1MaxY) ")
            stringBuilder.append("平均温${l1AveValue.toCStr()}\n")
        }
        if (isL2Show) {
            stringBuilder.append("线2 ($l2StartX, $l2StartY)-($l2EndX, $l2EndY) ")
            stringBuilder.append("最低温${l2MinValue.toCStr()}($l2MinX, $l2MinY) 最高温${l2MaxValue.toCStr()}($l2MaxX, $l2MaxY) ")
            stringBuilder.append("平均温${l2AveValue.toCStr()}\n")
        }
        if (isL3Show) {
            stringBuilder.append("线3 ($l3StartX, $l3StartY)-($l3EndX, $l3EndY) ")
            stringBuilder.append("最低温${l3MinValue.toCStr()}($l3MinX, $l3MinY) 最高温${l3MaxValue.toCStr()}($l3MaxX, $l3MaxY) ")
            stringBuilder.append("平均温${l3AveValue.toCStr()}\n")
        }

        if (isR1Show) {
            stringBuilder.append("面1 ($r1StartX, $r1StartY)-($r1EndX, $r1EndY) ")
            stringBuilder.append("最低温${r1MinValue.toCStr()}($r1MinX, $r1MinY) 最高温${r1MaxValue.toCStr()}($r1MaxX, $r1MaxY) ")
            stringBuilder.append("平均温${r1AveValue.toCStr()}\n")
        }
        if (isR2Show) {
            stringBuilder.append("面2 ($r2StartX, $r2StartY)-($r2EndX, $r2EndY) ")
            stringBuilder.append("最低温${r2MinValue.toCStr()}($r2MinX, $r2MinY) 最高温${r2MaxValue.toCStr()}($r2MaxX, $r2MaxY) ")
            stringBuilder.append("平均温${l2AveValue.toCStr()}\n")
        }
        if (isR3Show) {
            stringBuilder.append("面3 ($r3StartX, $r3StartY)-($r3EndX, $r3EndY) ")
            stringBuilder.append("最低温${r3MinValue.toCStr()}($r3MinX, $r3MinY) 最高温${r3MaxValue.toCStr()}($r3MaxX, $r3MaxY) ")
            stringBuilder.append("平均温${r3AveValue.toCStr()}\n")
        }
        return stringBuilder.toString()
    }
}
